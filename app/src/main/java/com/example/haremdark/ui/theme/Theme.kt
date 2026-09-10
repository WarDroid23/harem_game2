package com.example.haremdark.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun HaremDarkTheme(
    themeName: String = "Temné dominium",
    isLightMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isLightMode) {
        when (themeName) {
            "Krvavý trůn" -> androidx.compose.material3.lightColorScheme(
                primary = BloodPrimary,
                onPrimary = Color.White,
                secondary = Color(0xFFC2185B),
                background = Color(0xFFFDF7F7),
                surface = Color(0xFFF9EBEB),
                surfaceVariant = Color(0xFFF3D5D5),
                onBackground = Color(0xFF3E1F1F),
                onSurface = Color(0xFF3E1F1F)
            )
            "Ledová panenka" -> androidx.compose.material3.lightColorScheme(
                primary = IcePrimary,
                onPrimary = Color.White,
                secondary = Color(0xFF0288D1),
                background = Color(0xFFF0F8FF),
                surface = Color(0xFFE3F2FD),
                surfaceVariant = Color(0xFFBBDEFB),
                onBackground = Color(0xFF0D2C42),
                onSurface = Color(0xFF0D2C42)
            )
            "Zelený had" -> androidx.compose.material3.lightColorScheme(
                primary = EmeraldPrimary,
                onPrimary = Color.White,
                secondary = Color(0xFF388E3C),
                background = Color(0xFFF4FBF4),
                surface = Color(0xFFE8F5E9),
                surfaceVariant = Color(0xFFC8E6C9),
                onBackground = Color(0xFF143016),
                onSurface = Color(0xFF143016)
            )
            "Růžový hedváb" -> androidx.compose.material3.lightColorScheme(
                primary = SilkPrimary,
                onPrimary = Color.White,
                secondary = Color(0xFFC2185B),
                background = Color(0xFFFFF4F6),
                surface = Color(0xFFFCE4EC),
                surfaceVariant = Color(0xFFF8BBD0),
                onBackground = Color(0xFF490E2F),
                onSurface = Color(0xFF490E2F)
            )
            "Monochrom" -> androidx.compose.material3.lightColorScheme(
                primary = Color(0xFF616161),
                onPrimary = Color.White,
                secondary = Color(0xFF757575),
                background = Color(0xFFFAFAFA),
                surface = Color(0xFFF5F5F5),
                surfaceVariant = Color(0xFFE0E0E0),
                onBackground = Color(0xFF212121),
                onSurface = Color(0xFF212121)
            )
            else -> androidx.compose.material3.lightColorScheme( // "Temné dominium" light
                primary = DominionPrimary,
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFFEBEE),
                secondary = Color(0xFF7A1C1C),
                background = Color(0xFFFFF9FA),
                surface = Color(0xFFFFEBEE),
                surfaceVariant = Color(0xFFFFCDD2),
                onBackground = Color(0xFF2C0F12),
                onSurface = Color(0xFF2C0F12)
            )
        }
    } else {
        when (themeName) {
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
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
