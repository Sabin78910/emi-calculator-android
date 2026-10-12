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

class ContrastTest {
    private fun lum(argb: Long): Double {
        fun ch(shift: Int): Double {
            val v = ((argb shr shift) and 0xFF) / 255.0
            return if (v <= 0.03928) v / 12.92 else Math.pow((v + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * ch(16) + 0.7152 * ch(8) + 0.0722 * ch(0)
    }
    private fun ratio(a: Long, b: Long): Double {
        val (hi, lo) = lum(a).coerceAtLeast(lum(b)) to lum(a).coerceAtMost(lum(b))
        return (hi + 0.05) / (lo + 0.05)
    }

    @Test fun goldMilestoneChipIsReadableInBothThemes() {
        assert(ratio(BrandPalette.Light.secondary, BrandPalette.Light.onSecondary) >= 4.5)
        assert(ratio(BrandPalette.Dark.secondary, BrandPalette.Dark.onSecondary) >= 4.5)
    }

    @Test fun heroSurfaceHasContrastInBothThemes() {
        assert(ratio(BrandPalette.Light.heroSurface, BrandPalette.Light.onHero) >= 4.5)
        assert(ratio(BrandPalette.Dark.heroSurface, BrandPalette.Dark.onHero) >= 4.5)
    }
}
