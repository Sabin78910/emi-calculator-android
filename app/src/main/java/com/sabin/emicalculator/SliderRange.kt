package com.sabin.emicalculator

/** Slider bounds for a numeric input, kept in sync with its text field. */
data class SliderRange(val min: Float, val max: Float) {
    /** Slider position for the field text; invalid text falls back to [min], out-of-range is clamped. */
    fun position(text: String): Float = (text.toFloatOrNull() ?: min).coerceIn(min, max)

    /** Field text for a slider position, without a trailing ".0" for whole values. */
    fun text(value: Float, decimals: Int = 0): String =
        if (decimals == 0) Math.round(value.coerceIn(min, max)).toString()
        else "%.${decimals}f".format(java.util.Locale.US, value.coerceIn(min, max))

    companion object {
        val AMOUNT = SliderRange(10_000f, 10_000_000f)
        val RATE = SliderRange(1f, 30f)
        val TENURE = SliderRange(1f, 360f)
    }
}
