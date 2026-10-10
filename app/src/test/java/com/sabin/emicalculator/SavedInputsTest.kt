package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Test

class SavedInputsTest {
    @Test fun nullsFallBackToDefaults() {
        assertEquals(SavedInputs.DEFAULT, SavedInputs.parse(null, null, null, null))
    }

    @Test fun validValuesAreRestored() {
        val s = SavedInputs.parse("250000", "8.5", "5", "YEARS")
        assertEquals(SavedInputs("250000", "8.5", "5", TenureUnit.YEARS), s)
    }

    @Test fun invalidFieldsFallBackIndividually() {
        val s = SavedInputs.parse("abc", "-3", "0", "WEEKS")
        assertEquals(SavedInputs.DEFAULT, s)
        val t = SavedInputs.parse("1000", "x", "24", "MONTHS")
        assertEquals(SavedInputs("1000", SavedInputs.DEFAULT.rate, "24", TenureUnit.MONTHS), t)
    }

    @Test fun nonFiniteRejected() {
        assertEquals(SavedInputs.DEFAULT, SavedInputs.parse("NaN", "Infinity", "NaN", null))
    }

    @Test fun feeRestoredAndValidated() {
        assertEquals("1.5", SavedInputs.parse("1000", "10", "12", "MONTHS", "1.5").fee)
        assertEquals("0", SavedInputs.parse("1000", "10", "12", "MONTHS", "11").fee)
        assertEquals("0", SavedInputs.parse("1000", "10", "12", "MONTHS", null).fee)
    }
}
