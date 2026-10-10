package com.sabin.emicalculator

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeTest {
    @Test fun seedIsIndigo() = assertEquals(0xFF4F46E5, BrandPalette.SEED)

    @Test fun lightFallbackUsesSeedAsPrimary() {
        val s = brandLightColorScheme()
        assertEquals(Color(0xFF4F46E5), s.primary)
        assertEquals(Color.White, s.onPrimary)
    }

    @Test fun darkFallbackDiffersFromLight() {
        val d = brandDarkColorScheme()
        assertEquals(Color(BrandPalette.Dark.primary), d.primary)
        assertEquals(Color(BrandPalette.Dark.background), d.background)
        assert(d.background != brandLightColorScheme().background)
    }
}

class FintechPaletteTest {
    @Test fun lightSchemeHasNavyEmeraldAndGold() {
        val s = brandLightColorScheme()
        assertEquals(Color(BrandPalette.NAVY), s.onPrimaryContainer)
        assertEquals(Color(BrandPalette.EMERALD), s.tertiary)
        assertEquals(Color(BrandPalette.GOLD), s.secondary)
    }

    @Test fun darkSchemeUsesNavyBackground() {
        assertEquals(Color(BrandPalette.NAVY), brandDarkColorScheme().background)
    }
}
