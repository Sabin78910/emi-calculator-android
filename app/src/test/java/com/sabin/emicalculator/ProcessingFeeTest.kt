package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessingFeeTest {
    private fun emi(p: Double, r: Double, n: Int) = Emi.calculate(p, r, n).monthlyEmi

    @Test fun zeroFeeEqualsNominalRate() {
        assertEquals(12.0, ProcessingFee.effectiveRate(500000.0, emi(500000.0, 12.0, 60), 60, 0.0), 1e-6)
    }

    @Test fun zeroRateZeroFeeIsZero() {
        assertEquals(0.0, ProcessingFee.effectiveRate(1200.0, 100.0, 12, 0.0), 1e-9)
    }

    @Test fun feeRaisesEffectiveRate() {
        val e = ProcessingFee.effectiveRate(100000.0, emi(100000.0, 12.0, 12), 12, 1.0)
        // Independently verified: PV of the EMIs at the result must equal the net amount.
        val i = e / 1200
        val pv = emi(100000.0, 12.0, 12) * (1 - Math.pow(1 + i, -12.0)) / i
        assertEquals(99000.0, pv, 0.01)
        assertTrue(e in 13.0..14.0)
    }

    @Test fun highRateEdgeCase() {
        val e = ProcessingFee.effectiveRate(100000.0, emi(100000.0, 60.0, 6), 6, 10.0)
        assertTrue(e > 60.0 && e.isFinite())
    }

    @Test fun feeAmount() {
        assertEquals(5000.0, ProcessingFee.amount(500000.0, 1.0), 1e-9)
    }

    @Test fun totalCostIsInterestPlusFee() {
        assertEquals(35000.0, ProcessingFee.totalCost(30000.0, 5000.0), 1e-9)
    }

    @Test fun validation() {
        assertEquals(0.0, ProcessingFee.parse("")!!, 0.0)
        assertEquals(2.5, ProcessingFee.parse("2.5")!!, 0.0)
        assertEquals(10.0, ProcessingFee.parse("10")!!, 0.0)
        assertEquals(null, ProcessingFee.parse("10.1"))
        assertEquals(null, ProcessingFee.parse("abc"))
    }
}
