package com.sabin.emicalculator

import org.junit.Assert.assertEquals
import org.junit.Test

class SliderRangeTest {
    private val r = SliderRange(1f, 30f)

    @Test fun positionParsesAndClamps() {
        assertEquals(8.5f, r.position("8.5"), 0f)
        assertEquals(30f, r.position("99"), 0f)
        assertEquals(1f, r.position("-4"), 0f)
    }

    @Test fun invalidTextFallsBackToMin() = assertEquals(1f, r.position("abc"), 0f)

    @Test fun textFormatting() {
        assertEquals("12", r.text(12.2f))
        assertEquals("8.50", r.text(8.5f, 2))
        assertEquals("30", r.text(100f))
    }
}
