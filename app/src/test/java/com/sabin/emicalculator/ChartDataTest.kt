package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartDataTest {
    @Test fun sharesSumToOneHundred() {
        val s = ChartData.shares(1_000_000.0, 250_000.0)
        assertEquals(100.0, s.principalPercent + s.interestPercent, 1e-9)
        assertEquals(80.0, s.principalPercent, 1e-9)
    }

    @Test fun zeroInterestIsAllPrincipal() {
        val s = ChartData.shares(1000.0, 0.0)
        assertEquals(100.0, s.principalPercent, 1e-9)
        assertEquals(0.0, s.interestPercent, 1e-9)
    }

    @Test fun yearlyBalanceStartsAtPrincipalAndEndsAtZero() {
        val pts = ChartData.yearlyBalance(1_000_000.0, Emi.schedule(1_000_000.0, 10.0, 36))
        assertEquals(listOf(0, 1, 2, 3), pts.map { it.year })
        assertEquals(1_000_000.0, pts.first().balance, 1e-9)
        assertEquals(0.0, pts.last().balance, 0.01)
    }

    @Test fun partialFinalYearIsIncluded() {
        val pts = ChartData.yearlyBalance(100_000.0, Emi.schedule(100_000.0, 12.0, 18))
        assertEquals(listOf(0, 1, 2), pts.map { it.year })
        assertEquals(0.0, pts.last().balance, 0.01)
    }

    @Test fun balanceNeverIncreases() {
        val pts = ChartData.yearlyBalance(500_000.0, Emi.schedule(500_000.0, 9.0, 120))
        assertTrue(pts.zipWithNext().all { (a, b) -> b.balance <= a.balance })
    }

    @Test fun emptyScheduleGivesOnlyStartPoint() {
        assertEquals(1, ChartData.yearlyBalance(1000.0, emptyList()).size)
    }

    @Test fun descriptionsAreSpoken() {
        val s = ChartData.shares(750.0, 250.0)
        assertEquals("Principal 75 percent, interest 25 percent of total payment", ChartData.donutDescription(s))
        val pts = ChartData.yearlyBalance(1000.0, Emi.schedule(1000.0, 0.0, 12))
        assertEquals("Outstanding balance falls from 1,000 to 0 over 1 years", ChartData.lineDescription(pts))
    }
}
