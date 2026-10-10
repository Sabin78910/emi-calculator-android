package com.sabin.emicalculator

import kotlin.math.roundToInt

enum class TenureUnit(val label: String, val monthsPerUnit: Int) {
    MONTHS("Months", 1),
    YEARS("Years", 12);

    fun toMonths(value: Double): Int = (value * monthsPerUnit).roundToInt()

    fun parseToMonths(text: String): Int? = text.toDoubleOrNull()?.let { toMonths(it) }

    val sliderRange: SliderRange get() = if (this == YEARS) SliderRange(1f, 30f) else SliderRange.TENURE

    companion object {
        /** Re-expresses a tenure [text] in the [to] unit, clamped to its slider range; blank/invalid is kept as is. */
        fun convertText(text: String, from: TenureUnit, to: TenureUnit): String {
            val v = text.toDoubleOrNull() ?: return text
            val converted = v * from.monthsPerUnit / to.monthsPerUnit
            return Math.round(converted).coerceIn(to.sliderRange.min.toLong(), to.sliderRange.max.toLong()).toString()
        }
    }
}
