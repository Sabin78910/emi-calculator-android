package com.sabin.emicalculator

import kotlin.math.pow

object Affordability {
    /** Inverse EMI: P = EMI·((1+r)^n − 1) / (r·(1+r)^n), r = monthly rate. */
    fun maxPrincipal(maxMonthlyEmi: Double, annualRatePercent: Double, months: Int): Double {
        require(maxMonthlyEmi > 0) { "EMI must be positive" }
        require(annualRatePercent >= 0) { "Rate cannot be negative" }
        require(months > 0) { "Tenure must be at least 1 month" }
        val r = annualRatePercent / 12 / 100
        if (r == 0.0) return maxMonthlyEmi * months
        val growth = (1 + r).pow(months)
        return maxMonthlyEmi * (growth - 1) / (r * growth)
    }
}
