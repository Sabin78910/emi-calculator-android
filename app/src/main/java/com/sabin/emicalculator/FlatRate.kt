package com.sabin.emicalculator

object FlatRate {
    private fun validate(principal: Double, ratePercent: Double, months: Int) {
        require(principal > 0) { "Principal must be positive" }
        require(ratePercent >= 0) { "Rate cannot be negative" }
        require(months > 0) { "Tenure must be at least 1 month" }
    }

    /** Flat EMI = (P + P·rate·years) / months. */
    fun flatEmi(principal: Double, flatRatePercent: Double, months: Int): Double {
        validate(principal, flatRatePercent, months)
        return (principal + principal * flatRatePercent / 100 * months / 12) / months
    }

    /** Annual reducing-balance rate whose standard EMI equals the flat EMI, found by bisection. */
    fun reducingRateFromFlat(principal: Double, flatRatePercent: Double, months: Int): Double {
        val target = flatEmi(principal, flatRatePercent, months)
        var lo = 0.0
        var hi = 100.0
        while (Emi.calculate(principal, hi, months).monthlyEmi < target && hi < 1e6) hi *= 2
        repeat(100) {
            val mid = (lo + hi) / 2
            if (Emi.calculate(principal, mid, months).monthlyEmi < target) lo = mid else hi = mid
        }
        return (lo + hi) / 2
    }

    /** Flat rate that costs the same total interest as the given reducing-balance rate. */
    fun flatFromReducing(principal: Double, reducingRatePercent: Double, months: Int): Double {
        validate(principal, reducingRatePercent, months)
        val interest = Emi.calculate(principal, reducingRatePercent, months).totalInterest
        return interest / principal / (months / 12.0) * 100
    }
}
