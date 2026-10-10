package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class MoratoriumTest {
    @Test fun zeroMonthsMatchesPlainEmi() {
        val res = Moratorium.apply(1_000_000.0, 9.0, 120, 0)
        val plain = Emi.calculate(1_000_000.0, 9.0, 120)
        assertEquals(plain.monthlyEmi, res.postEmi, 0.01)
        assertEquals(plain.totalInterest, res.totalInterest, 0.1)
        assertEquals(0.0, res.extraInterest, 0.1)
        assertEquals(120, res.schedule.size)
    }

    @Test fun positiveMonthsRaiseEmiAndInterest() {
        val res = Moratorium.apply(1_000_000.0, 9.0, 120, 6)
        val plain = Emi.calculate(1_000_000.0, 9.0, 120)
        assertTrue(res.postEmi > plain.monthlyEmi)
        assertTrue(res.totalInterest > plain.totalInterest)
        assertEquals(res.totalInterest - plain.totalInterest, res.extraInterest, 0.1)
        assertEquals(1_000_000.0 * 0.09 / 12, res.interestOnlyPayment, 0.01)
    }

    @Test fun moratoriumRowsPayNoPrincipalAndBalanceEndsZero() {
        val res = Moratorium.apply(500_000.0, 10.0, 60, 12)
        assertEquals(60, res.schedule.size)
        res.schedule.take(12).forEach {
            assertEquals(0.0, it.principal, 0.0)
            assertEquals(500_000.0, it.balance, 0.01)
        }
        assertEquals(13, res.schedule[12].month)
        assertEquals(0.0, res.schedule.last().balance, 0.01)
    }

    @Test fun invalidMoratoriumRejected() {
        assertThrows(IllegalArgumentException::class.java) { Moratorium.apply(500_000.0, 10.0, 60, 60) }
        assertThrows(IllegalArgumentException::class.java) { Moratorium.apply(500_000.0, 10.0, 60, 61) }
        assertThrows(IllegalArgumentException::class.java) { Moratorium.apply(500_000.0, 10.0, 60, -1) }
    }
}
