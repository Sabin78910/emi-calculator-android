package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoanComparisonTest {
    @Test fun differencesAreBMinusA() {
        val a = LoanInput(100_000.0, 10.0, 12)
        val b = LoanInput(100_000.0, 12.0, 24)
        val c = LoanComparison.compare(a, b)
        assertEquals(Emi.calculate(100_000.0, 10.0, 12), c.a)
        assertEquals(Emi.calculate(100_000.0, 12.0, 24), c.b)
        assertEquals(c.b.monthlyEmi - c.a.monthlyEmi, c.emiDifference, 1e-9)
        assertEquals(c.b.totalInterest - c.a.totalInterest, c.interestDifference, 1e-9)
        assertTrue(c.emiDifference < 0)
        assertTrue(c.interestDifference > 0)
    }

    @Test fun identicalLoansHaveZeroDifference() {
        val l = LoanInput(50_000.0, 8.0, 36)
        val c = LoanComparison.compare(l, l)
        assertEquals(0.0, c.emiDifference, 1e-9)
        assertEquals(0.0, c.interestDifference, 1e-9)
    }

    @Test(expected = IllegalArgumentException::class) fun invalidInputRejected() {
        LoanComparison.compare(LoanInput(0.0, 10.0, 12), LoanInput(1.0, 10.0, 12))
    }
}
