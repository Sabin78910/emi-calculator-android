package com.sabin.emicalculator

import kotlin.math.roundToInt

object HeroFormat {
    fun money(v: Double): String {
        val fixed = "%.2f".format(java.util.Locale.US, Math.abs(v))
        val int = fixed.substringBefore('.')
        val grouped = if (int.length <= 3) int else
            int.dropLast(3).reversed().chunked(2).joinToString(",").reversed() + "," + int.takeLast(3)
        val sign = if (v < 0 && fixed.any { it in '1'..'9' }) "-" else ""
        return "NPR $sign$grouped.${fixed.substringAfter('.')}"
    }

    fun debtFreeChip(today: java.time.LocalDate, months: Int): String {
        val d = PayoffProgress.debtFreeDate(today, months)
        val m = d.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH)
        return "Debt-free $m ${d.year}"
    }

    /** Value shown at [progress] (0..1, clamped) of the count-up animation. */
    fun countUp(target: Double, progress: Float): Double = target * progress.coerceIn(0f, 1f)

    fun legend(s: Shares): String =
        "Principal ${s.principalPercent.roundToInt()}%  •  Interest ${s.interestPercent.roundToInt()}%"

    fun description(r: EmiResult): String =
        "Monthly EMI ${money(r.monthlyEmi)}. Total interest ${money(r.totalInterest)}. Total payable ${money(r.totalPayment)}"
}
