package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class LoanTransferTest {
    private fun transfer(newRate: Double = 8.0, newMonths: Int = 120, fee: Double = 0.0) =
        LoanTransfer.calculate(1_000_000.0, 11.0, 120, newRate, newMonths, fee)

    @Test fun lowerRateSavesMoney() {
        val r = transfer()
        assertTrue(r.newEmi < r.currentEmi)
        assertTrue(r.newInterest < r.currentInterest)
        assertEquals(r.currentInterest - r.newInterest, r.netSaving, 0.01)
        assertEquals(Emi.calculate(1_000_000.0, 11.0, 120).monthlyEmi, r.currentEmi, 0.001)
    }

    @Test fun feeIsSubtractedFromSaving() {
        val free = transfer()
        val paid = transfer(fee = 20_000.0)
        assertEquals(free.netSaving - 20_000.0, paid.netSaving, 0.01)
    }

    @Test fun higherFeeDelaysBreakEven() {
        val low = transfer(fee = 10_000.0).breakEvenMonth
        val high = transfer(fee = 50_000.0).breakEvenMonth
        assertNotNull(low); assertNotNull(high)
        assertTrue(high!! > low!!)
    }

    @Test fun breakEvenIsFeeOverMonthlySaving() {
        val r = transfer(fee = 30_000.0)
        val expected = Math.ceil(30_000.0 / (r.currentEmi - r.newEmi)).toInt()
        assertEquals(expected, r.breakEvenMonth)
    }

    @Test fun feeTooHighGivesNull() {
        val r = transfer(fee = 10_000_000.0)
        assertNull(r.breakEvenMonth)
        assertTrue(r.netSaving < 0)
    }

    @Test fun equalRatesAndZeroFeeGiveZeroSaving() {
        val r = transfer(newRate = 11.0)
        assertEquals(0.0, r.netSaving, 0.01)
        assertNull(r.breakEvenMonth)
    }

    @Test fun higherRateCostsMore() {
        val r = transfer(newRate = 13.0)
        assertTrue(r.netSaving < 0)
        assertNull(r.breakEvenMonth)
    }

    @Test fun longerTenureLowersEmiButNeedsRealSaving() {
        val r = transfer(newRate = 9.0, newMonths = 180)
        assertTrue(r.newEmi < r.currentEmi)
        if (r.netSaving <= 0) assertNull(r.breakEvenMonth) else assertNotNull(r.breakEvenMonth)
    }

    @Test fun feeFromPercentOfBalance() {
        assertEquals(10_000.0, LoanTransfer.feeFromPercent(1_000_000.0, 1.0), 0.001)
    }

    @Test fun invalidInputIsRejected() {
        listOf(
            { LoanTransfer.calculate(0.0, 11.0, 120, 8.0, 120, 0.0) },
            { LoanTransfer.calculate(-5.0, 11.0, 120, 8.0, 120, 0.0) },
            { LoanTransfer.calculate(1e6, -1.0, 120, 8.0, 120, 0.0) },
            { LoanTransfer.calculate(1e6, 11.0, 0, 8.0, 120, 0.0) },
            { LoanTransfer.calculate(1e6, 11.0, 120, -1.0, 120, 0.0) },
            { LoanTransfer.calculate(1e6, 11.0, 120, 8.0, 0, 0.0) },
            { LoanTransfer.calculate(1e6, 11.0, 120, 8.0, 120, -1.0) },
            { LoanTransfer.feeFromPercent(1e6, -1.0) },
        ).forEach { call ->
            try { call(); fail("expected IllegalArgumentException") } catch (_: IllegalArgumentException) {}
        }
    }

    @Test fun summaryTextIsPlainBothWays() {
        assertTrue(LoanTransfer.verdict(transfer(fee = 30_000.0)).startsWith("Saves"))
        assertTrue(LoanTransfer.verdict(transfer(newRate = 13.0)).startsWith("Costs you more"))
        assertTrue(LoanTransfer.verdict(transfer(fee = 10_000_000.0)).contains("does not pay back"))
        assertTrue(LoanTransfer.verdict(transfer(newRate = 11.0)).startsWith("No difference"))
    }

    @Test fun countUpScalesFigures() {
        val r = transfer(fee = 30_000.0)
        assertTrue(LoanTransfer.verdict(r, 0f).contains("NPR 0.00"))
        assertEquals(LoanTransfer.verdict(r), LoanTransfer.verdict(r, 1f))
    }
}
