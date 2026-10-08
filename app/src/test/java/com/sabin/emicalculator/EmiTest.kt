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
}
