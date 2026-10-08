package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Test

class EmiTest {
    @Test fun knownValue() {
        // 100,000 at 10% for 12 months -> EMI ≈ 8791.59
        assertEquals(8791.59, Emi.calculate(100_000.0, 10.0, 12).monthlyEmi, 0.01)
    }

    @Test fun zeroRateSplitsEvenly() {
        assertEquals(1000.0, Emi.calculate(12_000.0, 0.0, 12).monthlyEmi, 0.0001)
    }

    @Test fun scheduleEndsAtZero() {
        val s = Emi.schedule(500_000.0, 12.0, 60)
        assertEquals(60, s.size)
        assertEquals(0.0, s.last().balance, 0.01)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsZeroMonths() { Emi.calculate(1000.0, 5.0, 0) }

    @Test fun prepaymentShortensSchedule() {
        val base = Emi.schedule(500_000.0, 12.0, 60)
        val pre = Emi.scheduleWithPrepayment(500_000.0, 12.0, 60, 100_000.0, 12)
        assertEquals(true, pre.size < base.size)
        assertEquals(0.0, pre.last().balance, 0.01)
    }

    @Test fun prepaymentSavesInterest() {
        val saved = Emi.interestSaved(500_000.0, 12.0, 60, 100_000.0, 12)
        val base = Emi.schedule(500_000.0, 12.0, 60).sumOf { it.interest }
        val pre = Emi.scheduleWithPrepayment(500_000.0, 12.0, 60, 100_000.0, 12).sumOf { it.interest }
        assertEquals(base - pre, saved, 0.01)
        assertEquals(true, saved > 0)
    }

    @Test fun zeroPrepaymentSavesNothing() {
        assertEquals(0.0, Emi.interestSaved(100_000.0, 10.0, 12, 0.0, 3), 0.0001)
        assertEquals(12, Emi.scheduleWithPrepayment(100_000.0, 10.0, 12, 0.0, 3).size)
    }

    @Test fun prepaymentLargerThanBalanceClearsLoan() {
        val s = Emi.scheduleWithPrepayment(100_000.0, 10.0, 12, 1_000_000.0, 3)
        assertEquals(3, s.size)
        assertEquals(0.0, s.last().balance, 0.0001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPrepaymentMonthOutOfRange() { Emi.scheduleWithPrepayment(1000.0, 5.0, 12, 100.0, 13) }
}
