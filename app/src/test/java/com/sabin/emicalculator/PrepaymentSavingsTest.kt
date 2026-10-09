package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Test

class PrepaymentSavingsTest {
    private val result = Emi.withPrepayment(500_000.0, 12.0, 60, 100_000.0, 12)

    @Test fun messageUsesCalculatedSavings() {
        val msg = PrepaymentSavings.message(result.interestSaved, result.monthsSaved, 1f)
        assertEquals(
            "You save NPR %,.2f interest and %d months".format(result.interestSaved, result.monthsSaved), msg)
    }

    @Test fun countUpScalesWithProgress() {
        assertEquals("You save NPR 0.00 interest and 0 months", PrepaymentSavings.message(1000.0, 10, 0f))
        assertEquals("You save NPR 500.00 interest and 5 months", PrepaymentSavings.message(1000.0, 10, 0.5f))
    }

    @Test fun progressIsClamped() {
        assertEquals("You save NPR 1,000.00 interest and 10 months", PrepaymentSavings.message(1000.0, 10, 2f))
    }

    @Test fun singularMonth() {
        assertEquals("You save NPR 10.00 interest and 1 month", PrepaymentSavings.message(10.0, 1, 1f))
    }
}
