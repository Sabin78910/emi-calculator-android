package com.sabin.emicalculator

import kotlin.math.pow

data class StepUpResult(
    val schedule: List<ScheduleRow>,
    val firstEmi: Double,
    val lastEmi: Double,
    val totalInterest: Double,
    /** Flat-EMI interest minus step-up interest; negative means step-up costs extra. */
    val interestDifference: Double,
)

object StepUp {
    /** EMI grows by [stepPercent] after every [intervalMonths]; the first EMI is solved so the loan clears in [months]. */
    fun calculate(principal: Double, annualRatePercent: Double, months: Int, stepPercent: Double, intervalMonths: Int): StepUpResult {
        require(principal > 0) { "Principal must be positive" }
        require(annualRatePercent >= 0) { "Rate cannot be negative" }
        require(months > 0) { "Tenure must be at least 1 month" }
        require(stepPercent in 0.0..50.0) { "Step must be between 0 and 50%" }
        require(intervalMonths in 1..60) { "Interval must be between 1 and 60 months" }
        val r = annualRatePercent / 12 / 100
        val growth = 1 + stepPercent / 100
        fun factor(m: Int) = growth.pow((m - 1) / intervalMonths)
        val presentValue = (1..months).sumOf { factor(it) / (1 + r).pow(it) }
        val firstEmi = principal / presentValue
        var balance = principal
        val rows = (1..months).map { m ->
            val interest = balance * r
            val payment = if (m == months) balance + interest else firstEmi * factor(m)
            val principalPart = (payment - interest).coerceIn(0.0, balance)
            balance = if (m == months) 0.0 else (balance - principalPart).coerceAtLeast(0.0)
            ScheduleRow(m, principalPart, interest, balance)
        }
        val total = rows.sumOf { it.interest }
        val last = rows.last()
        return StepUpResult(rows, firstEmi, last.principal + last.interest, total, Emi.calculate(principal, annualRatePercent, months).totalInterest - total)
    }
}
