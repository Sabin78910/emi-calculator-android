package com.sabin.emicalculator

data class RateChangeResult(val schedule: List<ScheduleRow>, val newEmi: Double, val totalInterest: Double)

object RateChange {
    /** Rate switches to [newRate] after the EMI of [changeMonth]; EMI is recomputed on the remaining balance and months. */
    fun apply(principal: Double, annualRatePercent: Double, months: Int, changeMonth: Int, newRate: Double): RateChangeResult {
        require(changeMonth in 1 until months) { "Change month must be within the tenure" }
        require(newRate >= 0) { "Rate cannot be negative" }
        val before = Emi.schedule(principal, annualRatePercent, months).take(changeMonth)
        val remaining = before.last().balance
        val newEmi = if (remaining <= 0.0) 0.0 else Emi.calculate(remaining, newRate, months - changeMonth).monthlyEmi
        val after = if (remaining <= 0.0) emptyList()
        else Emi.schedule(remaining, newRate, months - changeMonth).map { it.copy(month = it.month + changeMonth) }
        val rows = before + after
        return RateChangeResult(rows, newEmi, rows.sumOf { it.interest })
    }
}
