package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingTest {
    @Test fun shownOnFirstRun() {
        assertTrue(Onboarding.shouldShow(null))
        assertTrue(Onboarding.shouldShow(false))
    }

    @Test fun notShownOnceCompleted() {
        assertFalse(Onboarding.shouldShow(true))
    }

    @Test fun hasThreePagesWithLastOneActionable() {
        assertEquals(3, Onboarding.PAGES.size)
        assertTrue(Onboarding.PAGES.last().isAction)
        assertTrue(Onboarding.PAGES.dropLast(1).none { it.isAction })
    }

    @Test fun onlyLastPageHidesSkip() {
        assertTrue(Onboarding.showSkip(0))
        assertTrue(Onboarding.showSkip(1))
        assertFalse(Onboarding.showSkip(2))
    }
}
