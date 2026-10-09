package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Test

class HeroFormatTest {
    @Test fun moneyGroupsAndRounds() {
        assertEquals("NPR 1,234,567.50", HeroFormat.money(1234567.5))
        assertEquals("NPR 0.00", HeroFormat.money(0.0))
    }

    @Test fun countUpScalesAndClamps() {
        assertEquals(0.0, HeroFormat.countUp(1000.0, 0f), 0.0)
        assertEquals(500.0, HeroFormat.countUp(1000.0, 0.5f), 1e-9)
        assertEquals(1000.0, HeroFormat.countUp(1000.0, 1.7f), 0.0)
        assertEquals(0.0, HeroFormat.countUp(1000.0, -1f), 0.0)
    }

    @Test fun countUpMoneyUsesProgress() {
        assertEquals("NPR 500.00", HeroFormat.money(HeroFormat.countUp(1000.0, 0.5f)))
    }

    @Test fun legendShowsRoundedPercents() {
        assertEquals("Principal 75%  •  Interest 25%", HeroFormat.legend(Shares(75.0, 25.0)))
        assertEquals("Principal 33%  •  Interest 67%", HeroFormat.legend(Shares(33.4, 66.6)))
    }

    @Test fun heroDescriptionListsAllFigures() {
        val r = EmiResult(1000.0, 1200.0, 200.0)
        assertEquals(
            "Monthly EMI NPR 1,000.00. Total interest NPR 200.00. Total payable NPR 1,200.00",
            HeroFormat.description(r))
    }
}
