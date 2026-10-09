package com.sabin.emicalculator

data class SavedLoan(val name: String, val inputs: SavedInputs, val paidMonths: Int = 0, val reminder: Reminder? = null)

/** Pure list operations and text serialization for saved loans (one tab-separated record per line). */
object SavedLoans {
    fun serialize(loans: List<SavedLoan>): String = loans.joinToString("\n") {
        listOf(escape(it.name), it.inputs.principal, it.inputs.rate, it.inputs.tenure, it.inputs.unit.name, it.paidMonths.toString(),
            it.reminder?.dueDay?.toString().orEmpty(), it.reminder?.daysBefore?.toString().orEmpty())
            .joinToString("\t")
    }

    /** Skips records that are malformed or have non-positive numbers. */
    fun deserialize(text: String?): List<SavedLoan> =
        text.orEmpty().lines().mapNotNull { line ->
            val f = line.split("\t")
            if ((f.size != 5 && f.size != 6 && f.size != 8) || f[0].isBlank()) return@mapNotNull null
            val unit = TenureUnit.values().firstOrNull { it.name == f[4] } ?: return@mapNotNull null
            val inputs = SavedInputs.parse(f[1], f[2], f[3], f[4])
            val valid = listOf(f[1], f[2], f[3]).all { it.toDoubleOrNull()?.let { v -> v.isFinite() && v > 0 } == true }
            val due = f.getOrNull(6)?.toIntOrNull()?.takeIf { it in Reminder.DUE_DAYS }
            val before = f.getOrNull(7)?.toIntOrNull()?.takeIf { it in Reminder.DAYS_BEFORE }
            val reminder = if (due != null && before != null) Reminder(due, before) else null
            if (valid) SavedLoan(unescape(f[0]), inputs.copy(unit = unit), f.getOrNull(5)?.toIntOrNull()?.coerceAtLeast(0) ?: 0, reminder) else null
        }

    /** Adds a loan; an existing loan with the same name (case-insensitive) is replaced in place. Blank names are ignored. */
    fun add(loans: List<SavedLoan>, loan: SavedLoan): List<SavedLoan> {
        val named = loan.copy(name = loan.name.trim())
        if (named.name.isEmpty()) return loans
        val i = loans.indexOfFirst { it.name.equals(named.name, ignoreCase = true) }
        return if (i >= 0) loans.toMutableList().also { it[i] = named } else loans + named
    }

    fun removeAt(loans: List<SavedLoan>, index: Int): List<SavedLoan> =
        if (index in loans.indices) loans.filterIndexed { i, _ -> i != index } else loans

    /** Marks one more month paid for the loan at [index], capped at its tenure. */
    fun markPaid(loans: List<SavedLoan>, index: Int): List<SavedLoan> =
        loans.mapIndexed { i, l ->
            if (i == index) l.copy(paidMonths = PayoffProgress.markPaid(l.inputs, l.paidMonths)) else l
        }

    /** Turns the reminder for the loan at [index] on, changes it, or off (null). */
    fun setReminder(loans: List<SavedLoan>, index: Int, reminder: Reminder?): List<SavedLoan> =
        loans.mapIndexed { i, l -> if (i == index) l.copy(reminder = reminder) else l }

    private fun escape(s: String) = buildString {
        for (c in s) when (c) {
            '\\' -> append("\\\\"); '\t' -> append("\\t"); '\n' -> append("\\n"); '\r' -> append("\\r")
            else -> append(c)
        }
    }

    private fun unescape(s: String) = buildString {
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 1 < s.length) {
                i++
                append(when (s[i]) { 't' -> '\t'; 'n' -> '\n'; 'r' -> '\r'; else -> s[i] })
            } else append(c)
            i++
        }
    }
}
