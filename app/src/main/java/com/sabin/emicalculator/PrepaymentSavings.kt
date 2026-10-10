package com.sabin.emicalculator

object PrepaymentSavings {
    /** Celebration text; [progress] (0..1) scales the figures for the count-up animation. */
    fun message(interestSaved: Double, monthsSaved: Int, progress: Float): String {
        val p = progress.coerceIn(0f, 1f).toDouble()
        val months = (monthsSaved * p).toInt()
        return "Save NPR %,.2f, finish %d %s early".format(
            interestSaved * p, months, if (months == 1) "month" else "months")
    }
}
