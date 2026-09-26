package com.example.haremdark.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.haremdark.models.Character

data class ThemeAuraInfo(
    val name: String,
    val subtitle: String,
    val icon: String,
    val accentColor: Color,
    val isDynamic: Boolean = false
)

val LocalThemeAura = staticCompositionLocalOf {
    ThemeAuraInfo(
        name = "Temné dominium",
        subtitle = "Základní temná aura",
        icon = "🏰",
        accentColor = DominionPrimary,
        isDynamic = false
    )
}

object DynamicThemeManager {

    val AVAILABLE_THEMES = listOf(
        "Dynamický (Aktivní dívka)" to DragonFirePrimary,
        "Dynamický (Aktivní region)" to RegionHvozdPrimary,
        "Dynamický (Chytrý mix)" to SuccubusPrimary,
        "Temné dominium" to DominionPrimary,
        "Krvavý trůn" to BloodPrimary,
        "Ledová panenka" to IcePrimary,
        "Zelený had" to EmeraldPrimary,
        "Růžový hedváb" to SilkPrimary,
        "Monochrom" to MonoPrimary
    )

    fun getAuraInfo(
        themeName: String,
        activeCharacter: Character?,
        activeDomainId: String?
    ): ThemeAuraInfo {
        return when (themeName) {
            "Dynamický (Aktivní dívka)" -> {
                val name = activeCharacter?.name ?: "Dominium"
                val arch = activeCharacter?.archetypeId?.lowercase() ?: ""
                val (archName, icon, color) = when {
                    arch.contains("draci") || name.lowercase().contains("aurel") -> Triple("Dračí plamen", "🔥", DragonFirePrimary)
                    arch.contains("sukuba") || arch.contains("touha") || name.lowercase().contains("lilith") -> Triple("Stínová sukuba", "😈", SuccubusPrimary)
                    arch.contains("knezkyn") || name.lowercase().contains("elena") -> Triple("Měsíční kněžka", "✨", PriestessPrimary)
                    arch.contains("chladna") || arch.contains("siren") || arch.contains("ice") -> Triple("Ledová siréna", "❄️", SirenPrimary)
                    arch.contains("slechticna") || arch.contains("princezn") -> Triple("Císařská koruna", "👑", NoblePrimary)
                    else -> Triple("Zmijí stín", "🐍", ViperPrimary)
                }
                ThemeAuraInfo(
                    name = "Aura dívky: $name",
                    subtitle = "Vliv živlu: $archName",
                    icon = icon,
                    accentColor = color,
                    isDynamic = true
                )
            }
            "Dynamický (Aktivní region)" -> {
                val (regName, icon, color) = when (activeDomainId) {
                    "hostinec_u_krvave_panny" -> Triple("Hostinec U Krvavé Panny", "🍺", RegionTavernPrimary)
                    "ruiny_chramu" -> Triple("Ruiny starého chrámu", "🔮", RegionTemplePrimary)
                    "stoky_doupata" -> Triple("Městské podsvětí & Stoky", "🐀", RegionSewersPrimary)
                    "mesicni_pristav" -> Triple("Měsíční přístav", "⚓", RegionHarborPrimary)
                    "tabor_zoldnerek" -> Triple("Tábor Černých Růží", "⚔️", RegionCampPrimary)
                    "slechticke_panstvi" -> Triple("Šlechtické panství", "🏰", RegionPalacePrimary)
                    "krvave_katakomby" -> Triple("Krvavé katakomby", "🩸", RegionCatacombsPrimary)
                    else -> Triple("Temný hvozd", "🌲", RegionHvozdPrimary)
                }
                ThemeAuraInfo(
                    name = "Atmosféra: $regName",
                    subtitle = "Aktivní provincie dominia",
                    icon = icon,
                    accentColor = color,
                    isDynamic = true
                )
            }
            "Dynamický (Chytrý mix)" -> {
                val girlName = activeCharacter?.name ?: "Oblíbenkyně"
                ThemeAuraInfo(
                    name = "Harmonie: $girlName & Region",
                    subtitle = "Propojení energie provincie s aurou dívky",
                    icon = "🌌",
                    accentColor = SuccubusPrimary,
                    isDynamic = true
                )
            }
            "Krvavý trůn" -> ThemeAuraInfo("Krvavý trůn", "Krvavě rudé a zlaté odstíny", "👑", BloodPrimary)
            "Ledová panenka" -> ThemeAuraInfo("Ledová panenka", "Mrazivě modrá a břidlice", "❄️", IcePrimary)
            "Zelený had" -> ThemeAuraInfo("Zelený had", "Smaragdová a jedová zeleň", "🐍", EmeraldPrimary)
            "Růžový hedváb" -> ThemeAuraInfo("Růžový hedváb", "Sametová růže a noc", "🌸", SilkPrimary)
            "Monochrom" -> ThemeAuraInfo("Monochrom", "Temná platina a popel", "🔘", MonoPrimary)
            else -> ThemeAuraInfo("Temné dominium", "Tradiční obsidián a karmín", "🏰", DominionPrimary)
        }
    }

    fun getCharacterColorScheme(character: Character?, isLightMode: Boolean): ColorScheme {
        val arch = character?.archetypeId?.lowercase() ?: ""
        val name = character?.name?.lowercase() ?: ""

        return when {
            arch.contains("draci") || name.contains("aurel") || arch.contains("fire") -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = DragonFirePrimary,
                        onPrimary = Color.White,
                        secondary = DragonFireSecondary,
                        background = Color(0xFFFFF7F5),
                        surface = Color(0xFFFFECE6),
                        surfaceVariant = Color(0xFFFFD9CE),
                        onBackground = Color(0xFF3A1008),
                        onSurface = Color(0xFF3A1008)
                    )
                } else {
                    darkColorScheme(
                        primary = DragonFirePrimary,
                        onPrimary = Color.White,
                        secondary = DragonFireSecondary,
                        background = DragonFireBackground,
                        surface = DragonFireSurface,
                        surfaceVariant = DragonFireSurfaceVariant,
                        onBackground = Color(0xFFFFEBE6),
                        onSurface = Color(0xFFFFEBE6)
                    )
                }
            }
            arch.contains("sukuba") || arch.contains("touha") || name.contains("lilith") || arch.contains("nymfo") -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = SuccubusPrimary,
                        onPrimary = Color.White,
                        secondary = SuccubusSecondary,
                        background = Color(0xFFFCF5FF),
                        surface = Color(0xFFF7E8FF),
                        surfaceVariant = Color(0xFFEFD1FC),
                        onBackground = Color(0xFF2E093D),
                        onSurface = Color(0xFF2E093D)
                    )
                } else {
                    darkColorScheme(
                        primary = SuccubusPrimary,
                        onPrimary = Color.White,
                        secondary = SuccubusSecondary,
                        background = SuccubusBackground,
                        surface = SuccubusSurface,
                        surfaceVariant = SuccubusSurfaceVariant,
                        onBackground = Color(0xFFFBEBFF),
                        onSurface = Color(0xFFFBEBFF)
                    )
                }
            }
            arch.contains("knezkyn") || name.contains("elena") || arch.contains("holy") || arch.contains("svata") -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = PriestessPrimary,
                        onPrimary = Color.White,
                        secondary = PriestessSecondary,
                        background = Color(0xFFF6F6FF),
                        surface = Color(0xFFEDEDFF),
                        surfaceVariant = Color(0xFFDCDDFF),
                        onBackground = Color(0xFF14143D),
                        onSurface = Color(0xFF14143D)
                    )
                } else {
                    darkColorScheme(
                        primary = PriestessPrimary,
                        onPrimary = Color.White,
                        secondary = PriestessSecondary,
                        background = PriestessBackground,
                        surface = PriestessSurface,
                        surfaceVariant = PriestessSurfaceVariant,
                        onBackground = Color(0xFFEBEBFF),
                        onSurface = Color(0xFFEBEBFF)
                    )
                }
            }
            arch.contains("chladna") || arch.contains("siren") || arch.contains("ice") || arch.contains("voda") || arch.contains("panenk") -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = SirenPrimary,
                        onPrimary = Color.Black,
                        secondary = SirenSecondary,
                        background = Color(0xFFF0FBFF),
                        surface = Color(0xFFE0F7FF),
                        surfaceVariant = Color(0xFFBEEFFF),
                        onBackground = Color(0xFF042733),
                        onSurface = Color(0xFF042733)
                    )
                } else {
                    darkColorScheme(
                        primary = SirenPrimary,
                        onPrimary = Color.Black,
                        secondary = SirenSecondary,
                        background = SirenBackground,
                        surface = SirenSurface,
                        surfaceVariant = SirenSurfaceVariant,
                        onBackground = Color(0xFFE1F8FF),
                        onSurface = Color(0xFFE1F8FF)
                    )
                }
            }
            arch.contains("slechticna") || arch.contains("princezn") || name.contains("komtes") || arch.contains("kralovn") -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = Color(0xFFC59B27),
                        onPrimary = Color.White,
                        secondary = NobleSecondary,
                        background = Color(0xFFFFFDF5),
                        surface = Color(0xFFFFF8E1),
                        surfaceVariant = Color(0xFFFFECB3),
                        onBackground = Color(0xFF332704),
                        onSurface = Color(0xFF332704)
                    )
                } else {
                    darkColorScheme(
                        primary = NoblePrimary,
                        onPrimary = Color.Black,
                        secondary = NobleSecondary,
                        background = NobleBackground,
                        surface = NobleSurface,
                        surfaceVariant = NobleSurfaceVariant,
                        onBackground = Color(0xFFFFF8E7),
                        onSurface = Color(0xFFFFF8E7)
                    )
                }
            }
            else -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = ViperPrimary,
                        onPrimary = Color.Black,
                        secondary = ViperSecondary,
                        background = Color(0xFFF3FCF5),
                        surface = Color(0xFFE5F8E9),
                        surfaceVariant = Color(0xFFC7F0D0),
                        onBackground = Color(0xFF072B11),
                        onSurface = Color(0xFF072B11)
                    )
                } else {
                    darkColorScheme(
                        primary = ViperPrimary,
                        onPrimary = Color.Black,
                        secondary = ViperSecondary,
                        background = ViperBackground,
                        surface = ViperSurface,
                        surfaceVariant = ViperSurfaceVariant,
                        onBackground = Color(0xFFE8FCEF),
                        onSurface = Color(0xFFE8FCEF)
                    )
                }
            }
        }
    }

    fun getRegionColorScheme(regionId: String?, isLightMode: Boolean): ColorScheme {
        return when (regionId) {
            "hostinec_u_krvave_panny" -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = RegionTavernPrimary,
                        onPrimary = Color.White,
                        secondary = RegionTavernSecondary,
                        background = Color(0xFFFFF7F3),
                        surface = Color(0xFFFFEBE3),
                        surfaceVariant = Color(0xFFFFD5C6),
                        onBackground = Color(0xFF3E1508),
                        onSurface = Color(0xFF3E1508)
                    )
                } else {
                    darkColorScheme(
                        primary = RegionTavernPrimary,
                        onPrimary = Color.White,
                        secondary = RegionTavernSecondary,
                        background = RegionTavernBackground,
                        surface = RegionTavernSurface,
                        surfaceVariant = RegionTavernSurfaceVariant,
                        onBackground = Color(0xFFFFECE4),
                        onSurface = Color(0xFFFFECE4)
                    )
                }
            }
            "ruiny_chramu" -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = RegionTemplePrimary,
                        onPrimary = Color.White,
                        secondary = RegionTempleSecondary,
                        background = Color(0xFFFAF3FC),
                        surface = Color(0xFFF4E5FA),
                        surfaceVariant = Color(0xFFE8C8F3),
                        onBackground = Color(0xFF2C0A38),
                        onSurface = Color(0xFF2C0A38)
                    )
                } else {
                    darkColorScheme(
                        primary = RegionTemplePrimary,
                        onPrimary = Color.White,
                        secondary = RegionTempleSecondary,
                        background = RegionTempleBackground,
                        surface = RegionTempleSurface,
                        surfaceVariant = RegionTempleSurfaceVariant,
                        onBackground = Color(0xFFF8E7FF),
                        onSurface = Color(0xFFF8E7FF)
                    )
                }
            }
            "stoky_doupata" -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = RegionSewersPrimary,
                        onPrimary = Color.White,
                        secondary = RegionSewersSecondary,
                        background = Color(0xFFFFF9F2),
                        surface = Color(0xFFFFF0DD),
                        surfaceVariant = Color(0xFFFFDEB8),
                        onBackground = Color(0xFF382305),
                        onSurface = Color(0xFF382305)
                    )
                } else {
                    darkColorScheme(
                        primary = RegionSewersPrimary,
                        onPrimary = Color.Black,
                        secondary = RegionSewersSecondary,
                        background = RegionSewersBackground,
                        surface = RegionSewersSurface,
                        surfaceVariant = RegionSewersSurfaceVariant,
                        onBackground = Color(0xFFFFF1DF),
                        onSurface = Color(0xFFFFF1DF)
                    )
                }
            }
            "mesicni_pristav" -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = RegionHarborPrimary,
                        onPrimary = Color.White,
                        secondary = RegionHarborSecondary,
                        background = Color(0xFFF0FAFC),
                        surface = Color(0xFFE0F4F8),
                        surfaceVariant = Color(0xFFBCE7F0),
                        onBackground = Color(0xFF042630),
                        onSurface = Color(0xFF042630)
                    )
                } else {
                    darkColorScheme(
                        primary = RegionHarborPrimary,
                        onPrimary = Color.Black,
                        secondary = RegionHarborSecondary,
                        background = RegionHarborBackground,
                        surface = RegionHarborSurface,
                        surfaceVariant = RegionHarborSurfaceVariant,
                        onBackground = Color(0xFFDEF6FA),
                        onSurface = Color(0xFFDEF6FA)
                    )
                }
            }
            "tabor_zoldnerek" -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = Color(0xFF607D8B),
                        onPrimary = Color.White,
                        secondary = RegionCampSecondary,
                        background = Color(0xFFF5F7F8),
                        surface = Color(0xFFECEFF1),
                        surfaceVariant = Color(0xFFCFD8DC),
                        onBackground = Color(0xFF1E2528),
                        onSurface = Color(0xFF1E2528)
                    )
                } else {
                    darkColorScheme(
                        primary = RegionCampPrimary,
                        onPrimary = Color.Black,
                        secondary = RegionCampSecondary,
                        background = RegionCampBackground,
                        surface = RegionCampSurface,
                        surfaceVariant = RegionCampSurfaceVariant,
                        onBackground = Color(0xFFECEFF1),
                        onSurface = Color(0xFFECEFF1)
                    )
                }
            }
            "slechticke_panstvi" -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = Color(0xFFC59B27),
                        onPrimary = Color.White,
                        secondary = RegionPalaceSecondary,
                        background = Color(0xFFFFFDF5),
                        surface = Color(0xFFFFF8E1),
                        surfaceVariant = Color(0xFFFFECB3),
                        onBackground = Color(0xFF332704),
                        onSurface = Color(0xFF332704)
                    )
                } else {
                    darkColorScheme(
                        primary = RegionPalacePrimary,
                        onPrimary = Color.Black,
                        secondary = RegionPalaceSecondary,
                        background = RegionPalaceBackground,
                        surface = RegionPalaceSurface,
                        surfaceVariant = RegionPalaceSurfaceVariant,
                        onBackground = Color(0xFFFFF8E7),
                        onSurface = Color(0xFFFFF8E7)
                    )
                }
            }
            "krvave_katakomby" -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = RegionCatacombsPrimary,
                        onPrimary = Color.White,
                        secondary = RegionCatacombsSecondary,
                        background = Color(0xFFFFF4F5),
                        surface = Color(0xFFFFE8E9),
                        surfaceVariant = Color(0xFFFFD0D3),
                        onBackground = Color(0xFF3E0A0E),
                        onSurface = Color(0xFF3E0A0E)
                    )
                } else {
                    darkColorScheme(
                        primary = RegionCatacombsPrimary,
                        onPrimary = Color.White,
                        secondary = RegionCatacombsSecondary,
                        background = RegionCatacombsBackground,
                        surface = RegionCatacombsSurface,
                        surfaceVariant = RegionCatacombsSurfaceVariant,
                        onBackground = Color(0xFFFFE5E7),
                        onSurface = Color(0xFFFFE5E7)
                    )
                }
            }
            else -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = RegionHvozdPrimary,
                        onPrimary = Color.White,
                        secondary = RegionHvozdSecondary,
                        background = Color(0xFFF4FAF5),
                        surface = Color(0xFFE7F5E9),
                        surfaceVariant = Color(0xFFCBEACC),
                        onBackground = Color(0xFF0F2E14),
                        onSurface = Color(0xFF0F2E14)
                    )
                } else {
                    darkColorScheme(
                        primary = RegionHvozdPrimary,
                        onPrimary = Color.Black,
                        secondary = RegionHvozdSecondary,
                        background = RegionHvozdBackground,
                        surface = RegionHvozdSurface,
                        surfaceVariant = RegionHvozdSurfaceVariant,
                        onBackground = Color(0xFFE8F7EA),
                        onSurface = Color(0xFFE8F7EA)
                    )
                }
            }
        }
    }

    fun resolveColorScheme(
        themeName: String,
        isLightMode: Boolean,
        activeCharacter: Character?,
        activeDomainId: String?
    ): ColorScheme {
        return when (themeName) {
            "Dynamický (Aktivní dívka)" -> getCharacterColorScheme(activeCharacter, isLightMode)
            "Dynamický (Aktivní region)" -> getRegionColorScheme(activeDomainId, isLightMode)
            "Dynamický (Chytrý mix)" -> {
                val charScheme = getCharacterColorScheme(activeCharacter, isLightMode)
                val regScheme = getRegionColorScheme(activeDomainId, isLightMode)
                if (isLightMode) {
                    lightColorScheme(
                        primary = charScheme.primary,
                        onPrimary = charScheme.onPrimary,
                        secondary = regScheme.primary,
                        background = regScheme.background,
                        surface = regScheme.surface,
                        surfaceVariant = regScheme.surfaceVariant,
                        onBackground = regScheme.onBackground,
                        onSurface = regScheme.onSurface
                    )
                } else {
                    darkColorScheme(
                        primary = charScheme.primary,
                        onPrimary = charScheme.onPrimary,
                        secondary = regScheme.primary,
                        background = regScheme.background,
                        surface = regScheme.surface,
                        surfaceVariant = regScheme.surfaceVariant,
                        onBackground = regScheme.onBackground,
                        onSurface = regScheme.onSurface
                    )
                }
            }
            "Krvavý trůn" -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = BloodPrimary,
                        onPrimary = Color.White,
                        secondary = Color(0xFFC2185B),
                        background = Color(0xFFFDF7F7),
                        surface = Color(0xFFF9EBEB),
                        surfaceVariant = Color(0xFFF3D5D5),
                        onBackground = Color(0xFF3E1F1F),
                        onSurface = Color(0xFF3E1F1F)
                    )
                } else {
                    darkColorScheme(
                        primary = BloodPrimary,
                        onPrimary = Color.White,
                        secondary = BloodSecondary,
                        background = BloodBackground,
                        surface = BloodSurface,
                        surfaceVariant = BloodSurfaceVariant,
                        onBackground = Color(0xFFFDE8E9),
                        onSurface = Color(0xFFFDE8E9)
                    )
                }
            }
            "Ledová panenka" -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = IcePrimary,
                        onPrimary = Color.White,
                        secondary = Color(0xFF0288D1),
                        background = Color(0xFFF0F8FF),
                        surface = Color(0xFFE3F2FD),
                        surfaceVariant = Color(0xFFBBDEFB),
                        onBackground = Color(0xFF0D2C42),
                        onSurface = Color(0xFF0D2C42)
                    )
                } else {
                    darkColorScheme(
                        primary = IcePrimary,
                        onPrimary = Color.Black,
                        secondary = IceSecondary,
                        background = IceBackground,
                        surface = IceSurface,
                        surfaceVariant = IceSurfaceVariant,
                        onBackground = Color(0xFFE1F5FE),
                        onSurface = Color(0xFFE1F5FE)
                    )
                }
            }
            "Zelený had" -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = EmeraldPrimary,
                        onPrimary = Color.White,
                        secondary = Color(0xFF388E3C),
                        background = Color(0xFFF4FBF4),
                        surface = Color(0xFFE8F5E9),
                        surfaceVariant = Color(0xFFC8E6C9),
                        onBackground = Color(0xFF143016),
                        onSurface = Color(0xFF143016)
                    )
                } else {
                    darkColorScheme(
                        primary = EmeraldPrimary,
                        onPrimary = Color.Black,
                        secondary = EmeraldSecondary,
                        background = EmeraldBackground,
                        surface = EmeraldSurface,
                        surfaceVariant = EmeraldSurfaceVariant,
                        onBackground = Color(0xFFE8F5E9),
                        onSurface = Color(0xFFE8F5E9)
                    )
                }
            }
            "Růžový hedváb" -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = SilkPrimary,
                        onPrimary = Color.White,
                        secondary = Color(0xFFC2185B),
                        background = Color(0xFFFFF4F6),
                        surface = Color(0xFFFCE4EC),
                        surfaceVariant = Color(0xFFF8BBD0),
                        onBackground = Color(0xFF490E2F),
                        onSurface = Color(0xFF490E2F)
                    )
                } else {
                    darkColorScheme(
                        primary = SilkPrimary,
                        onPrimary = Color.White,
                        secondary = SilkSecondary,
                        background = SilkBackground,
                        surface = SilkSurface,
                        surfaceVariant = SilkSurfaceVariant,
                        onBackground = Color(0xFFFCE4EC),
                        onSurface = Color(0xFFFCE4EC)
                    )
                }
            }
            "Monochrom" -> {
                if (isLightMode) {
                    lightColorScheme(
                        primary = Color(0xFF616161),
                        onPrimary = Color.White,
                        secondary = Color(0xFF757575),
                        background = Color(0xFFFAFAFA),
                        surface = Color(0xFFF5F5F5),
                        surfaceVariant = Color(0xFFE0E0E0),
                        onBackground = Color(0xFF212121),
                        onSurface = Color(0xFF212121)
                    )
                } else {
                    darkColorScheme(
                        primary = MonoPrimary,
                        onPrimary = Color.Black,
                        secondary = MonoSecondary,
                        background = MonoBackground,
                        surface = MonoSurface,
                        surfaceVariant = MonoSurfaceVariant,
                        onBackground = Color(0xFFEEEEEE),
                        onSurface = Color(0xFFEEEEEE)
                    )
                }
            }
            else -> { // "Temné dominium" default
                if (isLightMode) {
                    lightColorScheme(
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
                } else {
                    darkColorScheme(
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
        }
    }
}

@Composable
fun HaremDarkTheme(
    themeName: String = "Temné dominium",
    isLightMode: Boolean = false,
    activeCharacter: Character? = null,
    activeDomainId: String? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = DynamicThemeManager.resolveColorScheme(
        themeName = themeName,
        isLightMode = isLightMode,
        activeCharacter = activeCharacter,
        activeDomainId = activeDomainId
    )

    val auraInfo = DynamicThemeManager.getAuraInfo(
        themeName = themeName,
        activeCharacter = activeCharacter,
        activeDomainId = activeDomainId
    )

    CompositionLocalProvider(LocalThemeAura provides auraInfo) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
