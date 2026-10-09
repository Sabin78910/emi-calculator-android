package com.sabin.emicalculator

import kotlin.math.pow

data class EmiResult(val monthlyEmi: Double, val totalPayment: Double, val totalInterest: Double)
data class ScheduleRow(val month: Int, val principal: Double, val interest: Double, val balance: Double)

data class PrepaymentResult(
    val schedule: List<ScheduleRow>,
    val totalInterest: Double,
    val interestSaved: Double,
    val monthsSaved: Int,
)

object Emi {
    /** EMI = P·r·(1+r)^n / ((1+r)^n − 1), r = monthly rate. */
    fun calculate(principal: Double, annualRatePercent: Double, months: Int): EmiResult {
        require(principal > 0) { "Principal must be positive" }
        require(annualRatePercent >= 0) { "Rate cannot be negative" }
        require(months > 0) { "Tenure must be at least 1 month" }
        val r = annualRatePercent / 12 / 100
        val emi = if (r == 0.0) principal / months
        else principal * r * (1 + r).pow(months) / ((1 + r).pow(months) - 1)
        val total = emi * months
        return EmiResult(emi, total, total - principal)
    }

    fun schedule(principal: Double, annualRatePercent: Double, months: Int): List<ScheduleRow> {
        val emi = calculate(principal, annualRatePercent, months).monthlyEmi
        val r = annualRatePercent / 12 / 100
        var balance = principal
        return (1..months).map { m ->
            val interest = balance * r
            val principalPart = emi - interest
            balance = (balance - principalPart).coerceAtLeast(0.0)
            ScheduleRow(m, principalPart, interest, balance)
        }
    }

    /** Keeps the EMI fixed; a one-time [prepayment] after month [atMonth] reduces the balance and shortens the loan. */
    fun scheduleWithPrepayment(
        principal: Double, annualRatePercent: Double, months: Int, prepayment: Double, atMonth: Int,
    ): PrepaymentResult {
        require(prepayment >= 0) { "Prepayment cannot be negative" }
        require(atMonth in 1..months) { "Prepayment month must be within the tenure" }
        val base = calculate(principal, annualRatePercent, months)
        val emi = base.monthlyEmi
        val r = annualRatePercent / 12 / 100
        var balance = principal
        val rows = mutableListOf<ScheduleRow>()
        var m = 0
        while (balance > 0.005 && m < months) {
            m++
            val interest = balance * r
            var principalPart = (emi - interest).coerceAtMost(balance)
            if (m == atMonth) principalPart = (principalPart + prepayment).coerceAtMost(balance)
            balance = if (balance - principalPart < 0.005) 0.0 else balance - principalPart
            rows += ScheduleRow(m, principalPart, interest, balance)
        }
        val interest = rows.sumOf { it.interest }
        return PrepaymentResult(rows, interest, base.totalInterest - interest, months - rows.size)
    }
}
