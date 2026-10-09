package com.sabin.emicalculator

import java.time.LocalDate
import java.time.YearMonth

/** Opt-in EMI reminder for one saved loan: [daysBefore] days ahead of the [dueDay] of each month. */
data class Reminder(val dueDay: Int, val daysBefore: Int = DEFAULT_DAYS_BEFORE) {
    companion object {
        const val DEFAULT_DAYS_BEFORE = 2
        val DUE_DAYS = 1..31
        val DAYS_BEFORE = 0..7
    }
}

/** The single reminder notification for [date], listing every loan due that day (max one reminder a day). */
data class ReminderPlan(val date: LocalDate, val loanNames: List<String>)

object ReminderSchedule {
    /** First reminder date on or after [today]. A due day past month-end falls on the month's last day. */
    fun next(reminder: Reminder, today: LocalDate): LocalDate {
        var month = YearMonth.from(today).minusMonths(1)
        while (true) {
            val due = month.atDay(reminder.dueDay.coerceAtMost(month.lengthOfMonth()))
            val remindOn = due.minusDays(reminder.daysBefore.toLong())
            if (!remindOn.isBefore(today)) return remindOn
            month = month.plusMonths(1)
        }
    }

    /** The earliest upcoming reminder across unfinished loans with a reminder, or null if none. */
    fun plan(loans: List<SavedLoan>, today: LocalDate): ReminderPlan? {
        val dated = loans.mapNotNull { loan ->
            val reminder = loan.reminder ?: return@mapNotNull null
            val progress = PayoffProgress.of(loan.inputs, loan.paidMonths)
            if (progress == null || progress.monthsLeft == 0) null else loan.name to next(reminder, today)
        }
        val date = dated.minOfOrNull { it.second } ?: return null
        return ReminderPlan(date, dated.filter { it.second == date }.map { it.first })
    }
}
