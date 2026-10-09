package com.sabin.emicalculator

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderTest {
    private val inputs = SavedInputs("800000", "11", "60", TenureUnit.MONTHS)

    @Test fun defaultsToTwoDaysBefore() = assertEquals(2, Reminder(10).daysBefore)

    @Test fun reminderLaterThisMonth() =
        assertEquals(LocalDate.of(2026, 10, 18), ReminderSchedule.next(Reminder(20), LocalDate.of(2026, 10, 9)))

    @Test fun todayCountsIfReminderIsToday() =
        assertEquals(LocalDate.of(2026, 10, 18), ReminderSchedule.next(Reminder(20), LocalDate.of(2026, 10, 18)))

    @Test fun passedReminderRollsToNextMonth() =
        assertEquals(LocalDate.of(2026, 11, 18), ReminderSchedule.next(Reminder(20), LocalDate.of(2026, 10, 19)))

    @Test fun dueDay31ClampsToFebruaryEnd() =
        assertEquals(LocalDate.of(2027, 2, 26), ReminderSchedule.next(Reminder(31), LocalDate.of(2027, 2, 1)))

    @Test fun dueDay31ClampsInLeapYear() =
        assertEquals(LocalDate.of(2028, 2, 27), ReminderSchedule.next(Reminder(31), LocalDate.of(2028, 2, 1)))

    @Test fun dueDay30InThirtyOneDayMonth() =
        assertEquals(LocalDate.of(2026, 10, 28), ReminderSchedule.next(Reminder(30), LocalDate.of(2026, 10, 1)))

    @Test fun reminderCrossesIntoPreviousMonth() =
        assertEquals(LocalDate.of(2026, 11, 28), ReminderSchedule.next(Reminder(1, 3), LocalDate.of(2026, 11, 2)))

    @Test fun reminderCrossesYearBoundary() =
        assertEquals(LocalDate.of(2026, 12, 30), ReminderSchedule.next(Reminder(1, 2), LocalDate.of(2026, 12, 15)))

    @Test fun planPicksEarliestAndGroupsSameDay() {
        val loans = listOf(
            SavedLoan("Home", inputs, reminder = Reminder(20)),
            SavedLoan("Car", inputs, reminder = Reminder(20)),
            SavedLoan("Bike", inputs, reminder = Reminder(25)),
            SavedLoan("Off", inputs),
        )
        val plan = ReminderSchedule.plan(loans, LocalDate.of(2026, 10, 9))!!
        assertEquals(LocalDate.of(2026, 10, 18), plan.date)
        assertEquals(listOf("Home", "Car"), plan.loanNames)
    }

    @Test fun planIgnoresFinishedLoans() {
        val done = SavedLoan("Done", inputs, paidMonths = 60, reminder = Reminder(20))
        assertNull(ReminderSchedule.plan(listOf(done), LocalDate.of(2026, 10, 9)))
    }

    @Test fun planNullWhenNoReminders() = assertNull(ReminderSchedule.plan(listOf(SavedLoan("A", inputs)), LocalDate.of(2026, 10, 9)))

    @Test fun reminderSurvivesSerialization() {
        val loans = listOf(SavedLoan("Home", inputs, 3, Reminder(31, 5)), SavedLoan("Car", inputs))
        assertEquals(loans, SavedLoans.deserialize(SavedLoans.serialize(loans)))
    }

    @Test fun legacyRecordsHaveNoReminder() =
        assertNull(SavedLoans.deserialize("Car\t800000\t11\t60\tMONTHS\t2").single().reminder)

    @Test fun invalidReminderFieldsAreDropped() {
        assertNull(SavedLoans.deserialize("Car\t800000\t11\t60\tMONTHS\t2\t40\t2").single().reminder)
    }

    @Test fun setReminderTogglesOnlyTheTarget() {
        val loans = listOf(SavedLoan("A", inputs), SavedLoan("B", inputs))
        val on = SavedLoans.setReminder(loans, 1, Reminder(5))
        assertNull(on[0].reminder)
        assertEquals(Reminder(5), on[1].reminder)
        assertNull(SavedLoans.setReminder(on, 1, null)[1].reminder)
    }
}
