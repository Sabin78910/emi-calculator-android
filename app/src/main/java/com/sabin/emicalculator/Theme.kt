package com.sabin.emicalculator

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private fun c(v: Long) = Color(v)

fun brandLightColorScheme(): ColorScheme = BrandPalette.Light.let {
    lightColorScheme(
        primary = c(it.primary), onPrimary = c(it.onPrimary),
        primaryContainer = c(it.primaryContainer), onPrimaryContainer = c(it.onPrimaryContainer),
        secondary = c(it.secondary), onSecondary = c(it.onSecondary),
        tertiary = c(it.tertiary), onTertiary = c(it.onTertiary),
        secondaryContainer = c(it.secondaryContainer), onSecondaryContainer = c(it.onSecondaryContainer),
        background = c(it.background), onBackground = c(it.onBackground),
        surface = c(it.surface), onSurface = c(it.onSurface),
        surfaceVariant = c(it.surfaceVariant), onSurfaceVariant = c(it.onSurfaceVariant),
    )
}

fun brandDarkColorScheme(): ColorScheme = BrandPalette.Dark.let {
    darkColorScheme(
        primary = c(it.primary), onPrimary = c(it.onPrimary),
        primaryContainer = c(it.primaryContainer), onPrimaryContainer = c(it.onPrimaryContainer),
        secondary = c(it.secondary), onSecondary = c(it.onSecondary),
        tertiary = c(it.tertiary), onTertiary = c(it.onTertiary),
        secondaryContainer = c(it.secondaryContainer), onSecondaryContainer = c(it.onSecondaryContainer),
        background = c(it.background), onBackground = c(it.onBackground),
        surface = c(it.surface), onSurface = c(it.onSurface),
        surfaceVariant = c(it.surfaceVariant), onSurfaceVariant = c(it.onSurfaceVariant),
    )
}

val ExpressiveShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun EmiTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val context = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> brandDarkColorScheme()
        else -> brandLightColorScheme()
    }
    MaterialTheme(colorScheme = scheme, shapes = ExpressiveShapes, content = content)
}
