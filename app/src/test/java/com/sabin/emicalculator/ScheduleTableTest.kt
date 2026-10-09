package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduleTableTest {
    @Test fun headerHasFourColumns() {
        assertEquals(listOf("Month", "Principal", "Interest", "Balance"), ScheduleTable.HEADER)
    }

    @Test fun cellsUseThousandsSeparatorsAndNoDecimals() {
        val row = ScheduleRow(12, 1234567.4, 89012.6, 9876543.0)
        assertEquals(listOf("12", "1,234,567", "89,013", "9,876,543"), ScheduleTable.cells(row))
    }

    @Test fun cellsAreLocaleIndependent() {
        val old = java.util.Locale.getDefault()
        try {
            java.util.Locale.setDefault(java.util.Locale.GERMANY)
            assertEquals("1,000", ScheduleTable.cells(ScheduleRow(1, 1000.0, 0.0, 0.0))[1])
        } finally { java.util.Locale.setDefault(old) }
    }
}
