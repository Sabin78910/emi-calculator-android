package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedLoansTest {
    private val home = SavedLoan("Home", SavedInputs("5000000", "9.5", "20", TenureUnit.YEARS))
    private val car = SavedLoan("Car", SavedInputs("800000", "11", "60", TenureUnit.MONTHS))

    @Test fun roundTrip() {
        val list = listOf(home, car)
        assertEquals(list, SavedLoans.deserialize(SavedLoans.serialize(list)))
    }

    @Test fun roundTripSpecialCharactersInName() {
        val odd = SavedLoan("A\tB\nC\\D", car.inputs)
        assertEquals(listOf(odd), SavedLoans.deserialize(SavedLoans.serialize(listOf(odd))))
    }

    @Test fun emptyAndNullDeserialize() {
        assertTrue(SavedLoans.deserialize(null).isEmpty())
        assertTrue(SavedLoans.deserialize("").isEmpty())
        assertEquals("", SavedLoans.serialize(emptyList()))
    }

    @Test fun malformedRecordsAreSkipped() {
        val text = "garbage\n" + SavedLoans.serialize(listOf(car)) + "\nX\tabc\t1\t1\tMONTHS"
        assertEquals(listOf(car), SavedLoans.deserialize(text))
    }

    @Test fun addAppends() {
        assertEquals(listOf(home, car), SavedLoans.add(listOf(home), car))
    }

    @Test fun addWithSameNameReplaces() {
        val updated = SavedLoan("home", car.inputs)
        assertEquals(listOf(updated), SavedLoans.add(listOf(home), updated))
    }

    @Test fun blankNameIgnored() {
        assertEquals(listOf(home), SavedLoans.add(listOf(home), SavedLoan("  ", car.inputs)))
    }

    @Test fun removeAtIndex() {
        assertEquals(listOf(car), SavedLoans.removeAt(listOf(home, car), 0))
        assertEquals(listOf(home, car), SavedLoans.removeAt(listOf(home, car), 5))
    }
}
