package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ImpliedRateTest {
    @Test fun roundTripWithEmi() {
        val emi = Emi.calculate(500_000.0, 9.5, 60).monthlyEmi
        assertEquals(9.5, ImpliedRate.annualRatePercent(500_000.0, emi, 60), 0.001)
    }

    @Test fun zeroRateWhenEmiTimesMonthsEqualsPrincipal() {
        assertEquals(0.0, ImpliedRate.annualRatePercent(120_000.0, 10_000.0, 12), 1e-9)
        assertEquals(0.0, ImpliedRate.annualRatePercent(100_000.0, 100_000.0 / 3 + 1e-9, 3), 1e-9)
    }

    @Test fun veryHighRate() {
        val emi = Emi.calculate(100_000.0, 480.0, 24).monthlyEmi
        assertEquals(480.0, ImpliedRate.annualRatePercent(100_000.0, emi, 24), 0.001)
    }

    @Test fun convergenceWithinTolerance() {
        for (rate in listOf(0.5, 7.25, 18.0, 36.0)) {
            val emi = Emi.calculate(250_000.0, rate, 36).monthlyEmi
            assertEquals(rate, ImpliedRate.annualRatePercent(250_000.0, emi, 36), 0.0005)
        }
    }

    @Test fun emiBelowPrincipalOverTenureThrows() {
        assertThrows(IllegalArgumentException::class.java) { ImpliedRate.annualRatePercent(100_000.0, 1_000.0, 12) }
    }

    @Test fun nonPositiveInputThrows() {
        assertThrows(IllegalArgumentException::class.java) { ImpliedRate.annualRatePercent(0.0, 1000.0, 12) }
        assertThrows(IllegalArgumentException::class.java) { ImpliedRate.annualRatePercent(1000.0, 0.0, 12) }
        assertThrows(IllegalArgumentException::class.java) { ImpliedRate.annualRatePercent(1000.0, 100.0, 0) }
    }
}
