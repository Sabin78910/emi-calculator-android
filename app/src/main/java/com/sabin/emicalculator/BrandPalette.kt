package com.sabin.emicalculator

/** Fallback colours (ARGB) used when dynamic colour is unavailable. Seed: indigo #4F46E5. */
object BrandPalette {
    const val SEED = 0xFF4F46E5

    object Light {
        const val primary = SEED
        const val onPrimary = 0xFFFFFFFF
        const val primaryContainer = 0xFFE0E0FF
        const val onPrimaryContainer = 0xFF07006C
        const val secondary = 0xFF5D5C72
        const val onSecondary = 0xFFFFFFFF
        const val secondaryContainer = 0xFFE2E0F9
        const val onSecondaryContainer = 0xFF1A1A2C
        const val background = 0xFFFCF8FF
        const val onBackground = 0xFF1B1B21
        const val surface = 0xFFFCF8FF
        const val onSurface = 0xFF1B1B21
        const val surfaceVariant = 0xFFE4E1EC
        const val onSurfaceVariant = 0xFF46464F
    }

    object Dark {
        const val primary = 0xFFC0C1FF
        const val onPrimary = 0xFF1000A9
        const val primaryContainer = 0xFF3626CE
        const val onPrimaryContainer = 0xFFE0E0FF
        const val secondary = 0xFFC6C4DD
        const val onSecondary = 0xFF2F2F42
        const val secondaryContainer = 0xFF454559
        const val onSecondaryContainer = 0xFFE2E0F9
        const val background = 0xFF131318
        const val onBackground = 0xFFE4E1E9
        const val surface = 0xFF131318
        const val onSurface = 0xFFE4E1E9
        const val surfaceVariant = 0xFF46464F
        const val onSurfaceVariant = 0xFFC7C5D0
    }
}
