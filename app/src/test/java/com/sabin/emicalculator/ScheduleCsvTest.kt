package com.sabin.emicalculator

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduleCsvTest {
    @Test fun headerIsFirstLine() {
        val csv = ScheduleCsv.build(Emi.schedule(1000.0, 10.0, 3))
        assertEquals("Month,EMI,Principal,Interest,Balance", csv.lines().first())
    }

    @Test fun oneRowPerMonth() {
        val rows = Emi.schedule(100000.0, 12.0, 24)
        val lines = ScheduleCsv.build(rows).trimEnd().lines()
        assertEquals(25, lines.size)
    }

    @Test fun valuesRoundedToTwoDecimals() {
        val csv = ScheduleCsv.build(listOf(ScheduleRow(1, 1234.5678, 89.0149, 9876.5)))
        assertEquals("1,1323.58,1234.57,89.01,9876.50", csv.lines()[1])
    }

    @Test fun zeroInterestLoan() {
        val lines = ScheduleCsv.build(Emi.schedule(1200.0, 0.0, 4)).trimEnd().lines()
        assertEquals("1,300.00,300.00,0.00,900.00", lines[1])
        assertEquals("4,300.00,300.00,0.00,0.00", lines[4])
    }

    @Test fun decimalPointIsLocaleIndependent() {
        val old = Locale.getDefault()
        try {
            Locale.setDefault(Locale.GERMANY)
            assertEquals("1,11.50,10.50,1.00,5.00", ScheduleCsv.build(listOf(ScheduleRow(1, 10.5, 1.0, 5.0))).lines()[1])
        } finally { Locale.setDefault(old) }
    }
}
