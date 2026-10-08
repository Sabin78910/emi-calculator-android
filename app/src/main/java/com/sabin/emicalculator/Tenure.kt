package com.sabin.emicalculator

import kotlin.math.roundToInt

enum class TenureUnit(val label: String, private val monthsPerUnit: Int) {
    MONTHS("Months", 1),
    YEARS("Years", 12);

    fun toMonths(value: Double): Int = (value * monthsPerUnit).roundToInt()

    fun parseToMonths(text: String): Int? = text.toDoubleOrNull()?.let { toMonths(it) }
}
