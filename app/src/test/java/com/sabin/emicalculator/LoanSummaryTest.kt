package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Test

class LoanSummaryTest {
    @Test fun textListsEmiInterestTotalAndTenure() {
        val result = Emi.calculate(100_000.0, 10.0, 12)
        val expected = """
            Loan summary
            Monthly EMI: NPR 8,791.59
            Total interest: NPR 5,499.06
            Total payable: NPR 105,499.06
            Tenure: 12 months (1 year)
        """.trimIndent()
        assertEquals(expected, LoanSummary.text(result, 12))
    }

    @Test fun tenureOfOneMonthIsSingular() {
        val result = Emi.calculate(1000.0, 0.0, 1)
        assertEquals("Tenure: 1 month", LoanSummary.text(result, 1).lines().last())
    }

    @Test fun tenureShowsYearsWhenWholeYears() {
        val result = Emi.calculate(100_000.0, 10.0, 24)
        assertEquals("Tenure: 24 months (2 years)", LoanSummary.text(result, 24).lines().last())
    }
}
