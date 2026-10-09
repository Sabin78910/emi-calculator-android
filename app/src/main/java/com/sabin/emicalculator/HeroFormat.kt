package com.sabin.emicalculator

import kotlin.math.roundToInt

object HeroFormat {
    fun money(v: Double): String = "NPR %,.2f".format(java.util.Locale.US, v)

    /** Value shown at [progress] (0..1, clamped) of the count-up animation. */
    fun countUp(target: Double, progress: Float): Double = target * progress.coerceIn(0f, 1f)

    fun legend(s: Shares): String =
        "Principal ${s.principalPercent.roundToInt()}%  •  Interest ${s.interestPercent.roundToInt()}%"

    fun description(r: EmiResult): String =
        "Monthly EMI ${money(r.monthlyEmi)}. Total interest ${money(r.totalInterest)}. Total payable ${money(r.totalPayment)}"
}
