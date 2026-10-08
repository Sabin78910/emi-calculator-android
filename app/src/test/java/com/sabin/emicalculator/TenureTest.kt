package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TenureTest {
    @Test fun monthsUnchanged() { assertEquals(18, TenureUnit.MONTHS.toMonths(18.0)) }
    @Test fun yearsToMonths() { assertEquals(60, TenureUnit.YEARS.toMonths(5.0)) }
    @Test fun fractionalYears() { assertEquals(18, TenureUnit.YEARS.toMonths(1.5)) }
    @Test fun parseYears() { assertEquals(24, TenureUnit.YEARS.parseToMonths("2")) }
    @Test fun parseInvalid() { assertNull(TenureUnit.MONTHS.parseToMonths("abc")) }
}
