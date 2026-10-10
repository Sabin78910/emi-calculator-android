package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class StepUpTest {
    @Test fun zeroStepEqualsFlatEmi() {
        val r = StepUp.calculate(500000.0, 10.0, 60, 0.0, 12)
        val flat = Emi.calculate(500000.0, 10.0, 60)
        assertEquals(flat.monthlyEmi, r.firstEmi, 0.01)
        assertEquals(flat.monthlyEmi, r.lastEmi, 0.01)
        assertEquals(flat.totalInterest, r.totalInterest, 0.5)
        assertEquals(0.0, r.interestDifference, 0.5)
    }

    @Test fun handCalculatedZeroRateExample() {
        // 1200 over 12 months, +10% every 6 months: E0 * (6 + 6*1.1) = 1200
        val r = StepUp.calculate(1200.0, 0.0, 12, 10.0, 6)
        assertEquals(1200.0 / 12.6, r.firstEmi, 1e-6)
        assertEquals(1200.0 / 12.6 * 1.1, r.lastEmi, 1e-6)
        assertEquals(0.0, r.totalInterest, 1e-6)
        assertEquals(12, r.schedule.size)
    }

    @Test fun fullyPaysOffWithGrowingEmiAndLowerFirstEmi() {
        val r = StepUp.calculate(1_000_000.0, 9.0, 120, 5.0, 12)
        assertEquals(120, r.schedule.size)
        assertEquals(0.0, r.schedule.last().balance, 0.005)
        assertTrue(r.firstEmi < Emi.calculate(1_000_000.0, 9.0, 120).monthlyEmi)
        assertTrue(r.lastEmi > r.firstEmi)
        // Slower early repayment costs more interest than flat.
        assertTrue(r.interestDifference < 0)
    }

    @Test fun invalidInputRejected() {
        listOf(
            { StepUp.calculate(0.0, 10.0, 12, 5.0, 12) },
            { StepUp.calculate(1000.0, -1.0, 12, 5.0, 12) },
            { StepUp.calculate(1000.0, 10.0, 0, 5.0, 12) },
            { StepUp.calculate(1000.0, 10.0, 12, -1.0, 12) },
            { StepUp.calculate(1000.0, 10.0, 12, 50.1, 12) },
            { StepUp.calculate(1000.0, 10.0, 12, 5.0, 0) },
            { StepUp.calculate(1000.0, 10.0, 12, 5.0, 61) },
        ).forEach {
            try { it(); fail("expected IllegalArgumentException") } catch (_: IllegalArgumentException) {}
        }
    }
}
