package com.sabin.emicalculator

data class MoratoriumResult(
    val schedule: List<ScheduleRow>,
    val interestOnlyPayment: Double,
    val postEmi: Double,
    val totalInterest: Double,
    val extraInterest: Double,
)

object Moratorium {
    /** Pays interest only for the first [moratoriumMonths] of [months]; the EMI is then computed on the full balance over the rest. */
    fun apply(principal: Double, annualRatePercent: Double, months: Int, moratoriumMonths: Int): MoratoriumResult {
        require(moratoriumMonths in 0 until months) { "Moratorium must be shorter than the tenure" }
        val base = Emi.calculate(principal, annualRatePercent, months)
        val interestOnly = principal * annualRatePercent / 12 / 100
        val grace = (1..moratoriumMonths).map { ScheduleRow(it, 0.0, interestOnly, principal) }
        val remaining = months - moratoriumMonths
        val after = Emi.schedule(principal, annualRatePercent, remaining).map { it.copy(month = it.month + moratoriumMonths) }
        val rows = grace + after
        val total = rows.sumOf { it.interest }
        return MoratoriumResult(
            rows, interestOnly, Emi.calculate(principal, annualRatePercent, remaining).monthlyEmi,
            total, total - base.totalInterest,
        )
    }
}
