package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrepaymentTest {
    @Test fun zeroPrepaymentMatchesBaseline() {
        val r = Emi.withPrepayment(500_000.0, 12.0, 60, 0.0, 12)
        assertEquals(0.0, r.interestSaved, 0.01)
        assertEquals(0, r.monthsSaved)
        assertEquals(60, r.schedule.size)
    }

    @Test fun prepaymentSavesInterestAndShortensSchedule() {
        val base = Emi.calculate(500_000.0, 12.0, 60)
        val r = Emi.withPrepayment(500_000.0, 12.0, 60, 100_000.0, 12)
        assertTrue(r.interestSaved > 0)
        assertTrue(r.monthsSaved > 0)
        assertTrue(r.schedule.size < 60)
        assertEquals(base.totalInterest - r.totalInterest, r.interestSaved, 0.01)
        assertEquals(0.0, r.schedule.last().balance, 0.01)
    }

    @Test fun prepaymentLargerThanBalanceClosesLoan() {
        val r = Emi.withPrepayment(100_000.0, 10.0, 12, 1_000_000.0, 3)
        assertEquals(3, r.schedule.size)
        assertEquals(0.0, r.schedule.last().balance, 0.0001)
        assertEquals(9, r.monthsSaved)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativePrepayment() { Emi.withPrepayment(1000.0, 5.0, 12, -1.0, 2) }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsMonthOutsideTenure() { Emi.withPrepayment(1000.0, 5.0, 12, 100.0, 13) }
}
