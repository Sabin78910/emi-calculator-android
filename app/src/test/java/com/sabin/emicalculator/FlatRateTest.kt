package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FlatRateTest {
    @Test fun flatEmiMatchesFormula() {
        assertEquals((100_000.0 + 100_000.0 * 0.10 * 3) / 36, FlatRate.flatEmi(100_000.0, 10.0, 36), 1e-9)
    }

    @Test fun tenPercentFlatForThreeYearsIsAboutEighteenPercentReducing() {
        assertEquals(17.9, FlatRate.reducingRateFromFlat(100_000.0, 10.0, 36), 0.1)
    }

    @Test fun reducingRateReproducesFlatEmi() {
        val annual = FlatRate.reducingRateFromFlat(250_000.0, 8.0, 48)
        assertEquals(FlatRate.flatEmi(250_000.0, 8.0, 48), Emi.calculate(250_000.0, annual, 48).monthlyEmi, 0.01)
    }

    @Test fun zeroRateIsZeroBothWays() {
        assertEquals(0.0, FlatRate.reducingRateFromFlat(100_000.0, 0.0, 24), 1e-6)
        assertEquals(0.0, FlatRate.flatFromReducing(100_000.0, 0.0, 24), 1e-9)
    }

    @Test fun oneMonthTenureRatesAreEqual() {
        assertEquals(12.0, FlatRate.reducingRateFromFlat(50_000.0, 12.0, 1), 1e-4)
        assertEquals(12.0, FlatRate.flatFromReducing(50_000.0, 12.0, 1), 1e-6)
    }

    @Test fun roundTrip() {
        val reducing = FlatRate.reducingRateFromFlat(300_000.0, 9.5, 60)
        assertEquals(9.5, FlatRate.flatFromReducing(300_000.0, reducing, 60), 1e-4)
    }

    @Test fun flatIsLowerThanReducingForSameCost() {
        assertEquals(true, FlatRate.flatFromReducing(100_000.0, 18.0, 36) < 18.0)
    }

    @Test fun invalidInputThrows() {
        assertThrows(IllegalArgumentException::class.java) { FlatRate.flatEmi(0.0, 10.0, 12) }
        assertThrows(IllegalArgumentException::class.java) { FlatRate.flatEmi(1000.0, -1.0, 12) }
        assertThrows(IllegalArgumentException::class.java) { FlatRate.reducingRateFromFlat(1000.0, 10.0, 0) }
        assertThrows(IllegalArgumentException::class.java) { FlatRate.flatFromReducing(-5.0, 10.0, 12) }
    }
}
