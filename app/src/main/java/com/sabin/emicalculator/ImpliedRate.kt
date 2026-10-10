package com.sabin.emicalculator

object ImpliedRate {
    private const val ZERO_TOLERANCE = 1e-6

    /** Annual rate (%) at which the standard EMI formula yields [emi], found by bisection. */
    fun annualRatePercent(principal: Double, emi: Double, months: Int): Double {
        require(principal > 0) { "Principal must be positive" }
        require(emi > 0) { "EMI must be positive" }
        require(months > 0) { "Tenure must be at least 1 month" }
        val diff = emi * months - principal
        require(diff >= -ZERO_TOLERANCE * principal) { "EMI × tenure is below the principal" }
        if (diff <= ZERO_TOLERANCE * principal) return 0.0
        var lo = 0.0
        var hi = 100.0
        while (Emi.calculate(principal, hi, months).monthlyEmi < emi && hi < 1e7) hi *= 2
        repeat(200) {
            val mid = (lo + hi) / 2
            if (Emi.calculate(principal, mid, months).monthlyEmi < emi) lo = mid else hi = mid
        }
        return (lo + hi) / 2
    }
}
