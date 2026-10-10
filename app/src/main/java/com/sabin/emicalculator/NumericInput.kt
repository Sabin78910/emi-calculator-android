package com.sabin.emicalculator

object NumericInput {
    /** Keeps digits and, when [allowDecimal], only the first decimal point. */
    fun filter(text: String, allowDecimal: Boolean): String {
        var seenDot = false
        return text.filter { c ->
            when {
                c.isDigit() && c in '0'..'9' -> true
                c == '.' && allowDecimal && !seenDot -> { seenDot = true; true }
                else -> false
            }
        }
    }
}
