package com.sabin.emicalculator

/** Fallback colours (ARGB) used when dynamic colour is unavailable. Seed: indigo #4F46E5. */
object BrandPalette {
    const val SEED = 0xFF4F46E5
    const val NAVY = 0xFF0B1B3F
    const val EMERALD = 0xFF10B981
    const val GOLD = 0xFFF5B301
    /** Principal slice on the hero card: light indigo, readable on the deep indigo surface. */
    const val HERO_PRINCIPAL = 0xFFC0C1FF

    object Light {
        const val primary = SEED
        const val onPrimary = 0xFFFFFFFF
        const val primaryContainer = 0xFFE0E0FF
        const val onPrimaryContainer = NAVY
        const val secondary = GOLD
        const val onSecondary = NAVY
        const val tertiary = EMERALD
        const val onTertiary = NAVY
        const val secondaryContainer = 0xFFE2E0F9
        const val onSecondaryContainer = 0xFF1A1A2C
        const val background = 0xFFFCF8FF
        const val onBackground = 0xFF1B1B21
        const val surface = 0xFFFCF8FF
        const val onSurface = 0xFF1B1B21
        const val surfaceVariant = 0xFFE4E1EC
        const val onSurfaceVariant = 0xFF46464F
        const val heroSurface = 0xFF2B2A8C
        const val onHero = 0xFFFFFFFF
    }

    object Dark {
        const val primary = 0xFFC0C1FF
        const val onPrimary = 0xFF1000A9
        const val primaryContainer = 0xFF3626CE
        const val onPrimaryContainer = 0xFFE0E0FF
        const val secondary = GOLD
        const val onSecondary = NAVY
        const val tertiary = EMERALD
        const val onTertiary = NAVY
        const val secondaryContainer = 0xFF454559
        const val onSecondaryContainer = 0xFFE2E0F9
        const val background = NAVY
        const val onBackground = 0xFFE4E1E9
        const val surface = NAVY
        const val onSurface = 0xFFE4E1E9
        const val surfaceVariant = 0xFF46464F
        const val onSurfaceVariant = 0xFFC7C5D0
        const val heroSurface = 0xFF2A2D8F
        const val onHero = 0xFFFFFFFF
    }
}
