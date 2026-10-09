package com.sabin.emicalculator

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Display state of the home-screen widget, derived from the main (first) saved loan. */
sealed interface WidgetState {
    object Empty : WidgetState

    data class Active(
        val loanName: String,
        val emi: String,
        /** Null once the loan is fully paid. */
        val nextDue: String?,
        val monthsLeft: Int,
        val debtFree: String,
        val percentPaid: Int,
    ) : WidgetState {
        val done: Boolean get() = monthsLeft == 0
    }

    companion object {
        /** The next EMI is assumed due one month from [today], matching the debt-free countdown. */
        fun of(loans: List<SavedLoan>, today: LocalDate): WidgetState {
            val loan = loans.firstOrNull() ?: return Empty
            val progress = PayoffProgress.of(loan.inputs, loan.paidMonths) ?: return Empty
            val p = loan.inputs.principal.toDoubleOrNull() ?: return Empty
            val r = loan.inputs.rate.toDoubleOrNull() ?: return Empty
            val emi = runCatching { Emi.calculate(p, r, progress.totalMonths) }.getOrNull() ?: return Empty
            val done = progress.monthsLeft == 0
            return Active(
                loanName = loan.name,
                emi = HeroFormat.money(emi.monthlyEmi),
                nextDue = if (done) null else today.plusMonths(1).format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)),
                monthsLeft = progress.monthsLeft,
                debtFree = PayoffProgress.debtFreeDate(today, progress.monthsLeft)
                    .format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)),
                percentPaid = (progress.fractionPaid * 100).toInt(),
            )
        }
    }
}
