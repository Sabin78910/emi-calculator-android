package com.sabin.emicalculator

/** Example loan shapes; the rates are illustrative, not offers. */
data class LoanPreset(val name: String, val amount: Int, val ratePercent: Double, val tenureMonths: Int) {
    fun toInputs(unit: TenureUnit): SavedInputs = SavedInputs(
        amount.toString(),
        "%.1f".format(java.util.Locale.US, ratePercent),
        (tenureMonths / unit.monthsPerUnit).toString(),
        unit,
    )

    fun label(): String = "$name • example ${"%.1f".format(java.util.Locale.US, ratePercent)}%"

    companion object {
        val HOME = LoanPreset("Home", 5_000_000, 9.5, 240)
        val CAR = LoanPreset("Car", 2_000_000, 11.0, 60)
        val PERSONAL = LoanPreset("Personal", 500_000, 14.0, 36)
        val EDUCATION = LoanPreset("Education", 1_000_000, 10.0, 84)
        val ALL = listOf(HOME, CAR, PERSONAL, EDUCATION)
    }
}

enum class InputError { EMPTY, INVALID, TOO_LOW, TOO_HIGH }

object InputValidation {
    fun check(text: String, range: SliderRange): InputError? {
        if (text.isBlank()) return InputError.EMPTY
        val v = text.toFloatOrNull() ?: return InputError.INVALID
        return when {
            v < range.min -> InputError.TOO_LOW
            v > range.max -> InputError.TOO_HIGH
            else -> null
        }
    }
}
