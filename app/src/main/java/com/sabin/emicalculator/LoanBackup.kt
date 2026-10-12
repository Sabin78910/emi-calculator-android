package com.sabin.emicalculator

sealed interface BackupResult {
    data class Success(val loans: List<SavedLoan>) : BackupResult
    enum class Failure : BackupResult { Corrupt, UnsupportedVersion }
}

/** Versioned JSON backup of saved loans (with payments made). Pure Kotlin: no org.json, so it is unit tested on the JVM. */
object LoanBackup {
    const val VERSION = 1

    fun encode(loans: List<SavedLoan>): String = buildString {
        append("{\"version\":$VERSION,\"loans\":[")
        loans.forEachIndexed { n, l ->
            if (n > 0) append(',')
            append("{\"name\":").append(quote(l.name))
            append(",\"principal\":").append(quote(l.inputs.principal))
            append(",\"rate\":").append(quote(l.inputs.rate))
            append(",\"tenure\":").append(quote(l.inputs.tenure))
            append(",\"unit\":").append(quote(l.inputs.unit.name))
            append(",\"fee\":").append(quote(l.inputs.fee))
            append(",\"paidMonths\":").append(l.paidMonths)
            l.reminder?.let { append(",\"dueDay\":${it.dueDay},\"daysBefore\":${it.daysBefore}") }
            append('}')
        }
        append("]}")
    }

    /** Never throws; any invalid file yields a [BackupResult.Failure] so callers change nothing. */
    fun decode(text: String?): BackupResult {
        val root = (runCatching { Json(text.orEmpty()).parseDocument() }.getOrNull() as? Map<*, *>) ?: return BackupResult.Failure.Corrupt
        val version = root["version"] as? Double ?: return BackupResult.Failure.Corrupt
        if (version != VERSION.toDouble()) return BackupResult.Failure.UnsupportedVersion
        val items = root["loans"] as? List<*> ?: return BackupResult.Failure.Corrupt
        val loans = items.map { toLoan(it) ?: return BackupResult.Failure.Corrupt }
        return BackupResult.Success(loans)
    }

    /**
     * Merges [incoming] into [existing]. A loan is identified by its trimmed, case-insensitive name; an existing loan is
     * replaced in place by the backup copy unless it is further along, so merging never duplicates or loses progress.
     */
    fun merge(existing: List<SavedLoan>, incoming: List<SavedLoan>): List<SavedLoan> =
        incoming.fold(existing) { acc, loan ->
            val current = acc.firstOrNull { it.name.trim().equals(loan.name.trim(), ignoreCase = true) }
            val keep = if (current != null && current.paidMonths > loan.paidMonths) loan.copy(paidMonths = current.paidMonths) else loan
            SavedLoans.add(acc, keep)
        }

    private fun toLoan(item: Any?): SavedLoan? {
        val o = item as? Map<*, *> ?: return null
        val name = (o["name"] as? String)?.takeIf { it.isNotBlank() } ?: return null
        val principal = o["principal"] as? String ?: return null
        val rate = o["rate"] as? String ?: return null
        val tenure = o["tenure"] as? String ?: return null
        if (listOf(principal, rate, tenure).any { it.toDoubleOrNull()?.let { v -> v.isFinite() && v > 0 } != true }) return null
        val unit = TenureUnit.values().firstOrNull { it.name == o["unit"] } ?: return null
        val fee = (o["fee"] as? String)?.takeIf { ProcessingFee.parse(it) != null && it.isNotBlank() } ?: "0"
        val paid = (o["paidMonths"] as? Double)?.toInt()?.coerceAtLeast(0) ?: 0
        val due = (o["dueDay"] as? Double)?.toInt()?.takeIf { it in Reminder.DUE_DAYS }
        val before = (o["daysBefore"] as? Double)?.toInt()?.takeIf { it in Reminder.DAYS_BEFORE }
        val reminder = if (due != null && before != null) Reminder(due, before) else null
        return SavedLoan(name, SavedInputs(principal, rate, tenure, unit, fee), paid, reminder)
    }

    private fun quote(s: String) = buildString {
        append('"')
        for (c in s) when {
            c == '"' -> append("\\\"")
            c == '\\' -> append("\\\\")
            c < ' ' -> append("\\u%04x".format(c.code))
            else -> append(c)
        }
        append('"')
    }

    /** Minimal strict JSON parser: objects, arrays, strings, numbers, booleans, null. */
    private class Json(private val s: String) {
        private var i = 0

        fun parseDocument(): Any? {
            val v = value()
            ws()
            require(i == s.length)
            return v
        }

        private fun ws() { while (i < s.length && s[i].isWhitespace()) i++ }

        private fun value(): Any? {
            ws()
            require(i < s.length)
            return when (s[i]) {
                '{' -> obj()
                '[' -> arr()
                '"' -> str()
                't' -> literal("true", true)
                'f' -> literal("false", false)
                'n' -> literal("null", null)
                else -> num()
            }
        }

        private fun literal(word: String, v: Any?): Any? {
            require(s.startsWith(word, i)); i += word.length; return v
        }

        private fun num(): Double {
            val start = i
            while (i < s.length && (s[i].isDigit() || s[i] in "+-.eE")) i++
            return s.substring(start, i).toDouble()
        }

        private fun str(): String {
            i++
            val sb = StringBuilder()
            while (true) {
                require(i < s.length)
                val c = s[i++]
                when {
                    c == '"' -> return sb.toString()
                    c == '\\' -> {
                        require(i < s.length)
                        when (val e = s[i++]) {
                            'n' -> sb.append('\n'); 't' -> sb.append('\t'); 'r' -> sb.append('\r')
                            'b' -> sb.append('\b'); 'f' -> sb.append('\u000c')
                            'u' -> { require(i + 4 <= s.length); sb.append(s.substring(i, i + 4).toInt(16).toChar()); i += 4 }
                            '"', '\\', '/' -> sb.append(e)
                            else -> throw IllegalArgumentException()
                        }
                    }
                    c < ' ' -> throw IllegalArgumentException()
                    else -> sb.append(c)
                }
            }
        }

        private fun arr(): List<Any?> {
            i++
            val out = mutableListOf<Any?>()
            ws()
            if (s.getOrNull(i) == ']') { i++; return out }
            while (true) {
                out += value(); ws()
                require(i < s.length)
                when (s[i++]) { ',' -> continue; ']' -> return out; else -> throw IllegalArgumentException() }
            }
        }

        private fun obj(): Map<String, Any?> {
            i++
            val out = linkedMapOf<String, Any?>()
            ws()
            if (s.getOrNull(i) == '}') { i++; return out }
            while (true) {
                ws()
                require(s.getOrNull(i) == '"')
                val k = str(); ws()
                require(s.getOrNull(i++) == ':')
                out[k] = value(); ws()
                require(i < s.length)
                when (s[i++]) { ',' -> continue; '}' -> return out; else -> throw IllegalArgumentException() }
            }
        }
    }
}
