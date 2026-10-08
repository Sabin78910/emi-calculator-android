package com.sabin.emicalculator

import kotlin.math.pow

data class EmiResult(val monthlyEmi: Double, val totalPayment: Double, val totalInterest: Double)
data class ScheduleRow(val month: Int, val principal: Double, val interest: Double, val balance: Double)

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

    /** Keeps the EMI fixed; a one-time [prepayment] after month [atMonth] shortens the schedule. */
    fun scheduleWithPrepayment(
        principal: Double, annualRatePercent: Double, months: Int,
        prepayment: Double, atMonth: Int,
    ): List<ScheduleRow> {
        require(prepayment >= 0) { "Prepayment cannot be negative" }
        require(atMonth in 1..months) { "Prepayment month must be within the tenure" }
        val emi = calculate(principal, annualRatePercent, months).monthlyEmi
        val r = annualRatePercent / 12 / 100
        var balance = principal
        val rows = mutableListOf<ScheduleRow>()
        for (m in 1..months) {
            val interest = balance * r
            var principalPart = (emi - interest).coerceAtMost(balance)
            if (m == atMonth) principalPart = (principalPart + prepayment).coerceAtMost(balance)
            balance = if (m == months || balance - principalPart < 1e-6) 0.0 else balance - principalPart
            rows.add(ScheduleRow(m, principalPart, interest, balance))
            if (balance == 0.0) break
        }
        return rows
    }

    fun interestSaved(
        principal: Double, annualRatePercent: Double, months: Int,
        prepayment: Double, atMonth: Int,
    ): Double = schedule(principal, annualRatePercent, months).sumOf { it.interest } -
        scheduleWithPrepayment(principal, annualRatePercent, months, prepayment, atMonth).sumOf { it.interest }
}
