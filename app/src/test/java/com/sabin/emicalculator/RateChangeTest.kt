package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RateChangeTest {
    @Test fun sameRateMatchesPlainSchedule() {
        val res = RateChange.apply(1_000_000.0, 9.0, 120, 24, 9.0)
        val plain = Emi.schedule(1_000_000.0, 9.0, 120)
        assertEquals(plain.size, res.schedule.size)
        plain.zip(res.schedule).forEach { (a, b) -> assertEquals(a.balance, b.balance, 0.01) }
        assertEquals(Emi.calculate(1_000_000.0, 9.0, 120).monthlyEmi, res.newEmi, 0.01)
        assertEquals(plain.sumOf { it.interest }, res.totalInterest, 0.1)
    }

    @Test fun higherRateRaisesEmiAndInterest() {
        val res = RateChange.apply(1_000_000.0, 9.0, 120, 24, 12.0)
        assertTrue(res.newEmi > Emi.calculate(1_000_000.0, 9.0, 120).monthlyEmi)
        assertTrue(res.totalInterest > Emi.calculate(1_000_000.0, 9.0, 120).totalInterest)
    }

    @Test fun balanceReachesZeroAtLastMonth() {
        val res = RateChange.apply(500_000.0, 10.0, 60, 12, 7.0)
        assertEquals(60, res.schedule.size)
        assertEquals(0.0, res.schedule.last().balance, 0.01)
    }

    @Test fun rowsBeforeChangeUseOldRate() {
        val res = RateChange.apply(500_000.0, 10.0, 60, 12, 7.0)
        assertEquals(500_000.0 * 10.0 / 1200, res.schedule[0].interest, 0.001)
        val bal12 = res.schedule[11].balance
        assertEquals(bal12 * 7.0 / 1200, res.schedule[12].interest, 0.001)
    }

    @Test fun zeroNewRateWorks() {
        val res = RateChange.apply(120_000.0, 10.0, 12, 6, 0.0)
        assertEquals(0.0, res.schedule.last().balance, 0.01)
    }

    @Test fun invalidChangeMonthRejected() {
        listOf(0, -1, 60, 61).forEach { m ->
            assertTrue(runCatching { RateChange.apply(500_000.0, 10.0, 60, m, 8.0) }.isFailure)
        }
        assertTrue(runCatching { RateChange.apply(500_000.0, 10.0, 60, 12, -1.0) }.isFailure)
    }
}
