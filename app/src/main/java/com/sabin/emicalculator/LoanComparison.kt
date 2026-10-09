package com.sabin.emicalculator

data class LoanInput(val principal: Double, val annualRatePercent: Double, val months: Int)

/** Differences are loan B minus loan A (negative means B is cheaper). */
data class ComparisonResult(
    val a: EmiResult,
    val b: EmiResult,
    val emiDifference: Double,
    val interestDifference: Double,
)

object LoanComparison {
    fun compare(a: LoanInput, b: LoanInput): ComparisonResult {
        val ra = Emi.calculate(a.principal, a.annualRatePercent, a.months)
        val rb = Emi.calculate(b.principal, b.annualRatePercent, b.months)
        return ComparisonResult(ra, rb, rb.monthlyEmi - ra.monthlyEmi, rb.totalInterest - ra.totalInterest)
    }
}
