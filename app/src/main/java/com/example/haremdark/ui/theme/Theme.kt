package com.example.haremdark.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun HaremDarkTheme(
    themeName: String = "Temné dominium",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "Krvavý trůn" -> darkColorScheme(
            primary = BloodPrimary,
            onPrimary = Color.White,
            secondary = BloodSecondary,
            background = BloodBackground,
            surface = BloodSurface,
            surfaceVariant = BloodSurfaceVariant,
            onBackground = Color(0xFFFDE8E9),
            onSurface = Color(0xFFFDE8E9)
        )
        "Ledová panenka" -> darkColorScheme(
            primary = IcePrimary,
            onPrimary = Color.Black,
            secondary = IceSecondary,
            background = IceBackground,
            surface = IceSurface,
            surfaceVariant = IceSurfaceVariant,
            onBackground = Color(0xFFE1F5FE),
            onSurface = Color(0xFFE1F5FE)
        )
        "Zelený had" -> darkColorScheme(
            primary = EmeraldPrimary,
            onPrimary = Color.Black,
            secondary = EmeraldSecondary,
            background = EmeraldBackground,
            surface = EmeraldSurface,
            surfaceVariant = EmeraldSurfaceVariant,
            onBackground = Color(0xFFE8F5E9),
            onSurface = Color(0xFFE8F5E9)
        )
        "Růžový hedváb" -> darkColorScheme(
            primary = SilkPrimary,
            onPrimary = Color.White,
            secondary = SilkSecondary,
            background = SilkBackground,
            surface = SilkSurface,
            surfaceVariant = SilkSurfaceVariant,
            onBackground = Color(0xFFFCE4EC),
            onSurface = Color(0xFFFCE4EC)
        )
        "Monochrom" -> darkColorScheme(
            primary = MonoPrimary,
            onPrimary = Color.Black,
            secondary = MonoSecondary,
            background = MonoBackground,
            surface = MonoSurface,
            surfaceVariant = MonoSurfaceVariant,
            onBackground = Color(0xFFEEEEEE),
            onSurface = Color(0xFFEEEEEE)
        )
        else -> darkColorScheme( // "Temné dominium" default
            primary = DominionPrimary,
            onPrimary = DominionOnPrimary,
            primaryContainer = DominionPrimaryContainer,
            secondary = DominionSecondary,
            background = DominionBackground,
            surface = DominionSurface,
            surfaceVariant = DominionSurfaceVariant,
            onBackground = DominionTextPrimary,
            onSurface = DominionTextPrimary
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
