package com.sabin.emicalculator

import kotlin.math.pow

object ProcessingFee {
    const val MAX_PERCENT = 10.0

    /** Blank means no fee; otherwise a number in 0..[MAX_PERCENT], or null when invalid. */
    fun parse(text: String): Double? {
        if (text.isBlank()) return 0.0
        return text.toDoubleOrNull()?.takeIf { it.isFinite() && it in 0.0..MAX_PERCENT }
    }

    fun amount(principal: Double, feePercent: Double): Double = principal * feePercent / 100

    fun totalCost(totalInterest: Double, fee: Double): Double = totalInterest + fee

    /** Annualised (monthly IRR × 12) percent rate on the net amount received, found by bisection. */
    fun effectiveRate(principal: Double, emi: Double, months: Int, feePercent: Double): Double {
        val net = principal - amount(principal, feePercent)
        fun pv(i: Double) = if (i == 0.0) emi * months else emi * (1 - (1 + i).pow(-months)) / i
        if (pv(0.0) <= net) return 0.0
        var lo = 0.0
        var hi = 1.0
        while (pv(hi) > net && hi < 1e6) hi *= 2
        repeat(200) {
            val mid = (lo + hi) / 2
            if (pv(mid) > net) lo = mid else hi = mid
        }
        return (lo + hi) / 2 * 12 * 100
    }
}
