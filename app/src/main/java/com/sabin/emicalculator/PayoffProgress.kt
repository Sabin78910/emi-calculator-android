package com.sabin.emicalculator

import java.time.LocalDate

/** Payoff progress for a saved loan after [paidMonths] EMIs. */
data class PayoffProgress(val paidMonths: Int, val totalMonths: Int, val fractionPaid: Double) {
    val monthsLeft: Int get() = totalMonths - paidMonths

    companion object {
        val MILESTONES = listOf(25, 50, 75, 100)

        /** Null when the loan inputs are invalid. [paidMonths] is clamped to 0..tenure. */
        fun of(inputs: SavedInputs, paidMonths: Int): PayoffProgress? {
            val p = inputs.principal.toDoubleOrNull() ?: return null
            val r = inputs.rate.toDoubleOrNull() ?: return null
            val n = inputs.unit.parseToMonths(inputs.tenure) ?: return null
            val schedule = runCatching { Emi.schedule(p, r, n) }.getOrNull() ?: return null
            val paid = paidMonths.coerceIn(0, n)
            val balance = if (paid == 0) p else schedule[paid - 1].balance
            val fraction = if (paid == n) 1.0 else (1 - balance / p).coerceIn(0.0, 1.0)
            return PayoffProgress(paid, n, fraction)
        }

        fun debtFreeDate(today: LocalDate, monthsLeft: Int): LocalDate = today.plusMonths(monthsLeft.toLong())

        /** Paid-month count after marking one more month paid, capped at the tenure. */
        fun markPaid(inputs: SavedInputs, paidMonths: Int): Int {
            val n = inputs.unit.parseToMonths(inputs.tenure) ?: return paidMonths
            return (paidMonths + 1).coerceAtMost(n)
        }

        /** Milestone percentages reached at [fraction]. */
        fun milestones(fraction: Double): List<Int> = MILESTONES.filter { fraction + 1e-9 >= it / 100.0 }

        /** The highest milestone newly crossed going from [before] to [after], or null. */
        fun newMilestone(before: Double, after: Double): Int? =
            (milestones(after) - milestones(before).toSet()).maxOrNull()
    }
}
