package com.sabin.emicalculator

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PayoffProgressTest {
    private val loan = SavedInputs("120000", "12", "12", TenureUnit.MONTHS)

    @Test fun zeroPaidIsZeroProgress() {
        val p = PayoffProgress.of(loan, 0)!!
        assertEquals(0.0, p.fractionPaid, 1e-9)
        assertEquals(12, p.monthsLeft)
    }

    @Test fun halfwayPaidIsLessThanHalfOfPrincipalWhenInterestFront() {
        val p = PayoffProgress.of(loan, 6)!!
        assertTrue(p.fractionPaid in 0.4..0.5)
        assertEquals(6, p.monthsLeft)
    }

    @Test fun fullyPaidIsComplete() {
        val p = PayoffProgress.of(loan, 12)!!
        assertEquals(1.0, p.fractionPaid, 1e-9)
        assertEquals(0, p.monthsLeft)
    }

    @Test fun paidMonthsAreClamped() {
        assertEquals(12, PayoffProgress.of(loan, 99)!!.paidMonths)
        assertEquals(0, PayoffProgress.of(loan, -3)!!.paidMonths)
    }

    @Test fun invalidLoanIsNull() {
        assertEquals(null, PayoffProgress.of(SavedInputs("x", "12", "12", TenureUnit.MONTHS), 1))
    }

    @Test fun debtFreeDateAddsMonthsLeft() {
        assertEquals(LocalDate.of(2027, 4, 9), PayoffProgress.debtFreeDate(LocalDate.of(2026, 10, 9), 6))
        assertEquals(LocalDate.of(2026, 10, 9), PayoffProgress.debtFreeDate(LocalDate.of(2026, 10, 9), 0))
    }

    @Test fun markPaidIncrementsUpToTenure() {
        assertEquals(1, PayoffProgress.markPaid(loan, 0))
        assertEquals(12, PayoffProgress.markPaid(loan, 12))
    }

    @Test fun milestonesReached() {
        assertEquals(emptyList<Int>(), PayoffProgress.milestones(0.24))
        assertEquals(listOf(25), PayoffProgress.milestones(0.25))
        assertEquals(listOf(25, 50, 75), PayoffProgress.milestones(0.76))
        assertEquals(listOf(25, 50, 75, 100), PayoffProgress.milestones(1.0))
    }

    @Test fun newMilestoneOnlyWhenCrossed() {
        assertEquals(null, PayoffProgress.newMilestone(0.10, 0.20))
        assertEquals(25, PayoffProgress.newMilestone(0.20, 0.30))
        assertEquals(100, PayoffProgress.newMilestone(0.9, 1.0))
        assertEquals(null, PayoffProgress.newMilestone(0.30, 0.40))
    }
}
