package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Test

class PrepaymentSavingsTest {
    private val result = Emi.withPrepayment(500_000.0, 12.0, 60, 100_000.0, 12)

    @Test fun messageUsesCalculatedSavings() {
        val msg = PrepaymentSavings.message(result.interestSaved, result.monthsSaved, 1f)
        assertEquals(
            "Save NPR %,.2f, finish %d months early".format(result.interestSaved, result.monthsSaved), msg)
    }

    @Test fun countUpScalesWithProgress() {
        assertEquals("Save NPR 0.00, finish 0 months early", PrepaymentSavings.message(1000.0, 10, 0f))
        assertEquals("Save NPR 500.00, finish 5 months early", PrepaymentSavings.message(1000.0, 10, 0.5f))
    }

    @Test fun progressIsClamped() {
        assertEquals("Save NPR 1,000.00, finish 10 months early", PrepaymentSavings.message(1000.0, 10, 2f))
    }

    @Test fun singularMonth() {
        assertEquals("Save NPR 10.00, finish 1 month early", PrepaymentSavings.message(10.0, 1, 1f))
    }
}
