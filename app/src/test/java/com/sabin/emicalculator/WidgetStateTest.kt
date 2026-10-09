package com.sabin.emicalculator

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetStateTest {
    private val today = LocalDate.of(2026, 1, 15)
    private val loan = SavedLoan("Home", SavedInputs("120000", "0", "12", TenureUnit.MONTHS), paidMonths = 2)

    @Test fun emptyWhenNoLoans() {
        assertEquals(WidgetState.Empty, WidgetState.of(emptyList(), today))
    }

    @Test fun usesFirstLoanAsMain() {
        val other = loan.copy(name = "Car")
        val s = WidgetState.of(listOf(loan, other), today) as WidgetState.Active
        assertEquals("Home", s.loanName)
    }

    @Test fun mapsDueDateAmountAndCountdown() {
        val s = WidgetState.of(listOf(loan), today) as WidgetState.Active
        assertEquals("NPR 10,000.00", s.emi)
        assertEquals("Feb 15, 2026", s.nextDue)
        assertEquals(10, s.monthsLeft)
        assertEquals("Nov 2026", s.debtFree)
        assertEquals(16, s.percentPaid)
        assertTrue(!s.done)
    }

    @Test fun paidOffLoanIsDone() {
        val s = WidgetState.of(listOf(loan.copy(paidMonths = 12)), today) as WidgetState.Active
        assertTrue(s.done)
        assertNull(s.nextDue)
        assertEquals(100, s.percentPaid)
    }

    @Test fun invalidInputsAreEmpty() {
        val bad = loan.copy(inputs = loan.inputs.copy(principal = "x"))
        assertEquals(WidgetState.Empty, WidgetState.of(listOf(bad), today))
    }
}
