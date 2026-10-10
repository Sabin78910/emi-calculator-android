package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Test

class NumericInputTest {
    @Test fun stripsNonDigits() {
        assertEquals("51000003", NumericInput.filter("-51-00000r3", allowDecimal = false))
        assertEquals("12", NumericInput.filter("1.2", allowDecimal = false))
    }

    @Test fun keepsSingleDecimalPoint() {
        assertEquals("108", NumericInput.filter("1.0=d8", allowDecimal = false))
        assertEquals("1.08", NumericInput.filter("1.0=d8", allowDecimal = true))
        assertEquals("1.23", NumericInput.filter("1.2.3", allowDecimal = true))
    }

    @Test fun convertTenureWhenSwitchingUnit() {
        assertEquals("5", TenureUnit.convertText("60", TenureUnit.MONTHS, TenureUnit.YEARS))
        assertEquals("60", TenureUnit.convertText("5", TenureUnit.YEARS, TenureUnit.MONTHS))
        assertEquals("1", TenureUnit.convertText("6", TenureUnit.MONTHS, TenureUnit.YEARS))
        assertEquals("30", TenureUnit.convertText("360", TenureUnit.MONTHS, TenureUnit.YEARS))
        assertEquals("30", TenureUnit.convertText("999", TenureUnit.MONTHS, TenureUnit.YEARS))
        assertEquals("360", TenureUnit.convertText("40", TenureUnit.YEARS, TenureUnit.MONTHS))
        assertEquals("", TenureUnit.convertText("", TenureUnit.MONTHS, TenureUnit.YEARS))
    }

    @Test fun sliderRangePerUnit() {
        assertEquals(SliderRange(1f, 30f), TenureUnit.YEARS.sliderRange)
        assertEquals(SliderRange.TENURE, TenureUnit.MONTHS.sliderRange)
    }
}
