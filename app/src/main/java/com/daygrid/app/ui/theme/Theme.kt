package com.daygrid.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── DayGrid Palette ───────────────────────────────────────────────
val DayGridBackground     = Color(0xFF101010)
val DayGridSurface        = Color(0xFF1B1B1B)
val DayGridSurfaceVariant = Color(0xFF242424)
val DayGridOrange         = Color(0xFFFF8A00)
val DayGridOrangeVariant  = Color(0xFFFF6B00)
val DayGridNeutralCell    = Color(0xFF454545)
val DayGridTextPrimary    = Color(0xFFF5F5F5)
val DayGridTextSecondary  = Color(0xFFA7A7A7)
val DayGridTextTertiary   = Color(0xFF606060)
val DayGridUnproductive   = Color(0xFF2A2A2A)
val DayGridNeutralMark    = Color(0xFF3A3A3A)
val DayGridPastUnmarked   = Color(0xFF1E1E1E)
val DayGridFuture         = Color(0xFF141414)

private val DayGridColorScheme = darkColorScheme(
    primary = DayGridOrange,
    onPrimary = DayGridBackground,
    primaryContainer = Color(0xFF2E1A00),
    onPrimaryContainer = DayGridOrange,
    secondary = DayGridNeutralCell,
    onSecondary = DayGridTextPrimary,
    background = DayGridBackground,
    onBackground = DayGridTextPrimary,
    surface = DayGridSurface,
    onSurface = DayGridTextPrimary,
    surfaceVariant = DayGridSurfaceVariant,
    onSurfaceVariant = DayGridTextSecondary,
    outline = DayGridNeutralCell,
    error = Color(0xFFCF6679),
    onError = Color(0xFF1B0007)
)

@Composable
fun DayGridTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DayGridColorScheme,
        content = content
    )
}
