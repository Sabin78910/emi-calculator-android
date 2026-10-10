package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoanPresetTest {
    @Test fun fourPresetsInOrder() =
        assertEquals(listOf("Home", "Car", "Personal", "Education"), LoanPreset.ALL.map { it.name })

    @Test fun presetsFitSliderRanges() = LoanPreset.ALL.forEach {
        assertTrue(it.name, it.amount.toFloat() in SliderRange.AMOUNT.min..SliderRange.AMOUNT.max)
        assertTrue(it.name, it.ratePercent.toFloat() in SliderRange.RATE.min..SliderRange.RATE.max)
        assertTrue(it.name, it.tenureMonths.toFloat() in SliderRange.TENURE.min..SliderRange.TENURE.max)
    }

    @Test fun appliesInMonths() {
        val s = LoanPreset.HOME.toInputs(TenureUnit.MONTHS)
        assertEquals(LoanPreset.HOME.amount.toString(), s.principal)
        assertEquals(LoanPreset.HOME.tenureMonths.toString(), s.tenure)
        assertEquals(TenureUnit.MONTHS, s.unit)
    }

    @Test fun appliesInYears() {
        val s = LoanPreset.HOME.toInputs(TenureUnit.YEARS)
        assertEquals((LoanPreset.HOME.tenureMonths / 12).toString(), s.tenure)
    }

    @Test fun rateIsFormattedWithOneDecimal() = assertEquals("9.5", LoanPreset("X", 100000, 9.5, 12).toInputs(TenureUnit.MONTHS).rate)

    @Test fun exampleRateLabelSaysExample() = assertEquals("Home • example 9.5%", LoanPreset.HOME.label())
}

class InputValidationTest {
    private val r = SliderRange(1f, 30f)

    @Test fun blankIsRequired() = assertEquals(InputError.EMPTY, InputValidation.check("", r))
    @Test fun nonNumberIsInvalid() = assertEquals(InputError.INVALID, InputValidation.check(".", r))
    @Test fun belowMin() = assertEquals(InputError.TOO_LOW, InputValidation.check("0.5", r))
    @Test fun aboveMax() = assertEquals(InputError.TOO_HIGH, InputValidation.check("31", r))
    @Test fun validIsNull() { assertNull(InputValidation.check("1", r)); assertNull(InputValidation.check("30", r)) }
}
