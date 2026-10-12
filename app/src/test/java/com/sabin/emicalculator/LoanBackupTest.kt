package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoanBackupTest {
    private val home = SavedLoan("Home", SavedInputs("5000000", "9.5", "20", TenureUnit.YEARS, "1"), 12, Reminder(5, 2))
    private val car = SavedLoan("Car \"Fast\"\n\\ नेपाल", SavedInputs("800000", "11", "60", TenureUnit.MONTHS), 3)

    private fun ok(r: BackupResult) = (r as BackupResult.Success).loans

    @Test fun roundTrip() {
        assertEquals(listOf(home, car), ok(LoanBackup.decode(LoanBackup.encode(listOf(home, car)))))
    }

    @Test fun emptyListRoundTrips() {
        assertTrue(ok(LoanBackup.decode(LoanBackup.encode(emptyList()))).isEmpty())
    }

    @Test fun wrongVersionIsRejected() {
        val text = LoanBackup.encode(listOf(home)).replace("\"version\":1", "\"version\":99")
        assertEquals(BackupResult.Failure.UnsupportedVersion, LoanBackup.decode(text))
    }

    @Test fun corruptInputIsRejected() {
        listOf("", "   ", "not json", "{\"version\":1,\"loans\":[", "[]", "{\"version\":1}", "{\"version\":1,\"loans\":[{\"name\":\"X\"}]}")
            .forEach { assertEquals(it, BackupResult.Failure.Corrupt, LoanBackup.decode(it)) }
    }

    @Test fun nonPositiveNumbersAreCorrupt() {
        val text = LoanBackup.encode(listOf(home)).replace("5000000", "-5")
        assertEquals(BackupResult.Failure.Corrupt, LoanBackup.decode(text))
    }

    @Test fun mergeKeepsExistingAndAddsNewWithoutDuplicates() {
        val backupHome = home.copy(name = "HOME", paidMonths = 30)
        val merged = LoanBackup.merge(listOf(home), listOf(backupHome, car))
        assertEquals(listOf(backupHome, car), merged)
        assertEquals(merged, LoanBackup.merge(merged, listOf(backupHome, car)))
    }

    @Test fun mergeKeepsFurtherPaymentProgress() {
        val ahead = home.copy(paidMonths = 40)
        val merged = LoanBackup.merge(listOf(ahead), listOf(home))
        assertEquals(40, merged.single().paidMonths)
    }

    @Test fun mergeIntoEmptyAndDuplicateInBackup() {
        assertEquals(listOf(home), LoanBackup.merge(emptyList(), listOf(home, home)))
    }
}
