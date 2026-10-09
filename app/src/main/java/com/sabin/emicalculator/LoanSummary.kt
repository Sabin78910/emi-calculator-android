package com.sabin.emicalculator

import java.util.Locale

object LoanSummary {
    fun text(result: EmiResult, months: Int): String = listOf(
        "Loan summary",
        "Monthly EMI: NPR ${money(result.monthlyEmi)}",
        "Total interest: NPR ${money(result.totalInterest)}",
        "Total payable: NPR ${money(result.totalPayment)}",
        "Tenure: ${tenure(months)}",
    ).joinToString("\n")

    private fun money(v: Double) = String.format(Locale.US, "%,.2f", v)

    private fun tenure(months: Int): String {
        val base = if (months == 1) "1 month" else "$months months"
        val years = months / 12
        return if (months % 12 == 0 && years > 0) "$base ($years ${if (years == 1) "year" else "years"})" else base
    }
}
