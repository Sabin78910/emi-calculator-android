package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Test

class AffordabilityTest {
    @Test fun forwardEmiMatchesInput() {
        val p = Affordability.maxPrincipal(25_000.0, 10.5, 60)
        assertEquals(25_000.0, Emi.calculate(p, 10.5, 60).monthlyEmi, 1.0)
    }

    @Test fun zeroRateMultipliesEmiByMonths() {
        assertEquals(120_000.0, Affordability.maxPrincipal(10_000.0, 0.0, 12), 0.0001)
    }

    @Test fun roundTripsAcrossRatesAndTenures() {
        for (rate in listOf(0.0, 4.0, 12.0, 24.0)) for (n in listOf(1, 12, 240)) {
            val p = Affordability.maxPrincipal(8_000.0, rate, n)
            assertEquals(8_000.0, Emi.calculate(p, rate, n).monthlyEmi, 1.0)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsZeroEmi() { Affordability.maxPrincipal(0.0, 5.0, 12) }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeRate() { Affordability.maxPrincipal(1000.0, -1.0, 12) }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsZeroMonths() { Affordability.maxPrincipal(1000.0, 5.0, 0) }
}
