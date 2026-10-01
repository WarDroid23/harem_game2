package com.example.haremdark.data

import com.example.haremdark.R
import com.example.haremdark.models.BondColorPalette
import com.example.haremdark.models.BondPortraitVariant
import com.example.haremdark.models.BondTierMilestone
import com.example.haremdark.models.Character

object BondTierCatalog {

    // ==========================================
    // 🎨 BOND COLOR PALETTES
    // ==========================================

    val PALETTE_DEFAULT = BondColorPalette(
        id = "default",
        name = "Temná Ocel & Železo",
        requiredAffinityLevel = 1,
        icon = "⛓️",
        primaryColorHex = 0xFF78909C,
        secondaryColorHex = 0xFF455A64,
        accentColorHex = 0xFFB0BEC5,
        glowColorHex = 0x4078909C,
        cardGradientHexes = listOf(0xFF263238, 0xFF182026, 0xFF0E1418),
        description = "Základní zdrženlivá temná ocel vyjadřující chladný odstup a zajetí.",
        titleReward = "Pouto Železa",
        statPerkSummary = "Základní vizuální styl bez dodatečných bonusů"
    )

    val PALETTE_EMERALD = BondColorPalette(
        id = "emerald_servant",
        name = "Smaragdový Květ Oddanosti",
        requiredAffinityLevel = 2,
        icon = "🌿",
        primaryColorHex = 0xFF4CAF50,
        secondaryColorHex = 0xFF2E7D32,
        accentColorHex = 0xFF81C784,
        glowColorHex = 0x554CAF50,
        cardGradientHexes = listOf(0xFF1B3820, 0xFF102615, 0xFF09170C),
        description = "Svěží zelená aura vyjadřující rodící se poslušnost a péči v komnatách.",
        titleReward = "Pouto Smaragdu",
        statPerkSummary = "+10 Max HP • +10% Zisk zlata ze správy"
    )

    val PALETTE_CYAN = BondColorPalette(
        id = "cyan_confidant",
        name = "Azurový Soumrak Důvěry",
        requiredAffinityLevel = 3,
        icon = "💎",
        primaryColorHex = 0xFF00E5FF,
        secondaryColorHex = 0xFF0097A7,
        accentColorHex = 0xFF80DEEA,
        glowColorHex = 0x5500E5FF,
        cardGradientHexes = listOf(0xFF0F323D, 0xFF082029, 0xFF041217),
        description = "Mrazivě čistá azurová zář odhalující tajné touhy a důvěrné rozmluvy.",
        titleReward = "Pouto Křišťálu",
        statPerkSummary = "+25 Max HP • +8% Bojový Útok • +10% Crit"
    )

    val PALETTE_CRIMSON = BondColorPalette(
        id = "crimson_passion",
        name = "Karmínová Vášeň & Samet",
        requiredAffinityLevel = 4,
        icon = "💖",
        primaryColorHex = 0xFFFF4081,
        secondaryColorHex = 0xFFC2185B,
        accentColorHex = 0xFFFF80AB,
        glowColorHex = 0x66FF4081,
        cardGradientHexes = listOf(0xFF38101E, 0xFF240A13, 0xFF14050A),
        description = "Hluboká sametově růžová a karmínová barva planoucí nespoutanou vášní.",
        titleReward = "Pouto Vášně",
        statPerkSummary = "+40 Max HP • +15% Útok • +3 HP/kolo Regenerace"
    )

    val PALETTE_ASTRAL = BondColorPalette(
        id = "astral_violet",
        name = "Astrální Ametyst Spříznění",
        requiredAffinityLevel = 5,
        icon = "🌌",
        primaryColorHex = 0xFFE040FB,
        secondaryColorHex = 0xFF7B1FA2,
        accentColorHex = 0xFFEA80FC,
        glowColorHex = 0x66E040FB,
        cardGradientHexes = listOf(0xFF2E0C38, 0xFF1D0724, 0xFF100314),
        description = "Mystická fialová mlhovina symbolizující věčné spojení duší a absolutní věrnost.",
        titleReward = "Pouto Hvězd",
        statPerkSummary = "+60 Max HP • +25% Útok • +10 Obrana • +5 HP/kolo"
    )

    val PALETTE_IMPERIAL = BondColorPalette(
        id = "imperial_gold",
        name = "Císařské Zlato Vládkyně",
        requiredAffinityLevel = 6,
        icon = "👑",
        primaryColorHex = 0xFFFFD700,
        secondaryColorHex = 0xFFFFA000,
        accentColorHex = 0xFFFFE082,
        glowColorHex = 0x80FFD700,
        cardGradientHexes = listOf(0xFF3B2E0A, 0xFF261D05, 0xFF140E02),
        description = "Nejvyšší imperiální zlatá aura pro suverénní panovnici a pravou královnu harému.",
        titleReward = "Pouto Koruny",
        statPerkSummary = "+100 Max HP • +40% Útok • +15 Obrana • +8 HP/kolo • +5 TE/kolo"
    )

    val ALL_PALETTES = listOf(
        PALETTE_DEFAULT,
        PALETTE_EMERALD,
        PALETTE_CYAN,
        PALETTE_CRIMSON,
        PALETTE_ASTRAL,
        PALETTE_IMPERIAL
    )

    // ==========================================
    // 🖼️ UNLOCKED PORTRAIT VARIANTS
    // ==========================================

    val PORTRAIT_VARIANTS = listOf(
        // Elena Variants
        BondPortraitVariant(
            id = "portrait_elena_base",
            title = "Elena (Plachá Služka)",
            requiredAffinityLevel = 1,
            targetArchetypeId = "subka",
            drawableRes = R.drawable.img_slave_elena_portrait_1790459398016,
            badgeIcon = "⛓️",
            description = "Základní portrét Eleny s ostýchavým pohledem a runovým obojkem.",
            auraParticleType = "NONE"
        ),
        BondPortraitVariant(
            id = "portrait_elena_confidant",
            title = "Elena: Důvěrnice Hvězd",
            requiredAffinityLevel = 3,
            targetArchetypeId = "subka",
            drawableRes = R.drawable.img_slave_elena_portrait_1790459398016,
            badgeIcon = "💎",
            description = "Elena s jemným úsměvem a zářícíma očima v azurovém světle.",
            auraParticleType = "CYAN_CRYSTAL"
        ),
        BondPortraitVariant(
            id = "portrait_elena_astral_empress",
            title = "Elena: Astrální Císařovna Luny",
            requiredAffinityLevel = 5,
            targetArchetypeId = "subka",
            drawableRes = R.drawable.img_skin_elena_astral_empress_1790460204605,
            badgeIcon = "🌌",
            description = "Nádherná císařská róba z hvězdného hedvábí a diamantová koruna oddanosti.",
            auraParticleType = "ASTRAL_SHIMMER"
        ),

        // Aurelia Variants
        BondPortraitVariant(
            id = "portrait_aurelia_base",
            title = "Aurelia (Ohnivá Gladiátorka)",
            requiredAffinityLevel = 1,
            targetArchetypeId = "odvazna",
            drawableRes = R.drawable.img_slave_aurelia_portrait_1790459410403,
            badgeIcon = "⚔️",
            description = "Aurelia s plamenným odhodláním a bojovými jizvami v aréně.",
            auraParticleType = "NONE"
        ),
        BondPortraitVariant(
            id = "portrait_aurelia_passion",
            title = "Aurelia: Dračí Milenka Vášně",
            requiredAffinityLevel = 4,
            targetArchetypeId = "odvazna",
            drawableRes = R.drawable.img_slave_aurelia_portrait_1790459410403,
            badgeIcon = "🔥",
            description = "Aurelia s uvolněnými vlasy a planoucím pohledem věrné milenky.",
            auraParticleType = "HEART_PULSE"
        ),
        BondPortraitVariant(
            id = "portrait_aurelia_inferno_empress",
            title = "Aurelia: Vládkyně Inferna",
            requiredAffinityLevel = 5,
            targetArchetypeId = "odvazna",
            drawableRes = R.drawable.img_skin_aurelia_inferno_empress_1790460216995,
            badgeIcon = "👑",
            description = "Zlaté dračí rohy, kalená výzbroj a suverénní aura válečné královny.",
            auraParticleType = "GOLDEN_FLAMES"
        ),

        // Lilith Variants
        BondPortraitVariant(
            id = "portrait_lilith_base",
            title = "Lilith (Svůdná Sukuba)",
            requiredAffinityLevel = 1,
            targetArchetypeId = "touha",
            drawableRes = R.drawable.img_slave_lilith_portrait_1790459422573,
            badgeIcon = "💜",
            description = "Základní portrét Lilith se svůdným pohledem a nočními rohy.",
            auraParticleType = "NONE"
        ),
        BondPortraitVariant(
            id = "portrait_lilith_abyss_queen",
            title = "Lilith: Královna Propasti a Rozkoše",
            requiredAffinityLevel = 5,
            targetArchetypeId = "touha",
            drawableRes = R.drawable.img_skin_lilith_abyss_queen_1790460228042,
            badgeIcon = "👑",
            description = "Korzet z temného hedvábí, démonická koruna a fialová aura rozkoše.",
            auraParticleType = "ASTRAL_SHIMMER"
        ),

        // Carmilla Variants
        BondPortraitVariant(
            id = "portrait_carmilla_base",
            title = "Carmilla (Upíří Šlechtična)",
            requiredAffinityLevel = 1,
            targetArchetypeId = "slechticna",
            drawableRes = R.drawable.img_slave_vampire_queen_1790459434350,
            badgeIcon = "🩸",
            description = "Aristokratická vznešenost a ledový pohled upíří paní.",
            auraParticleType = "NONE"
        ),
        BondPortraitVariant(
            id = "portrait_carmilla_blood_sovereign",
            title = "Carmilla: Krvavá Suverénka",
            requiredAffinityLevel = 5,
            targetArchetypeId = "slechticna",
            drawableRes = R.drawable.img_skin_carmilla_blood_sovereign_1790460239235,
            badgeIcon = "👑",
            description = "Královská rubínová róba a vládkyně upíří dynastie.",
            auraParticleType = "GOLDEN_FLAMES"
        ),

        // Universal Valkyrie Dominion
        BondPortraitVariant(
            id = "portrait_valkyrie_golden_dominion",
            title = "Zlatá Valkýra Věčného Dominia",
            requiredAffinityLevel = 6,
            targetArchetypeId = "all",
            drawableRes = R.drawable.img_skin_valkyrie_golden_dominion_1790460252949,
            badgeIcon = "👑",
            description = "Absolutní transcendence a svatá válečnice pánovy říše.",
            auraParticleType = "GOLDEN_FLAMES"
        )
    )

    // ==========================================
    // 🏆 COMPLETE BOND TIER MILESTONES (1 - 6)
    // ==========================================

    val BOND_TIERS: List<BondTierMilestone> = listOf(
        BondTierMilestone(
            tierLevel = 1,
            name = "Cizinka & Zajatkyně",
            stageTitle = "Tier I • Pouto Železa",
            subtitle = "Nedůvěřivá a bázlivá. Sleduje tvé kroky a přijímá tvou vůli.",
            icon = "⛓️",
            minAffinityPoints = 0,
            maxAffinityPoints = 30,
            palette = PALETTE_DEFAULT,
            unlockedPortraitVariants = PORTRAIT_VARIANTS.filter { it.requiredAffinityLevel == 1 },
            combatPerkSummary = "Základní bojová poslušnost",
            haremPerkSummary = "Možnost rozmluv a předávání darů v komnatách",
            dialogueQuote = "„Poslechnu tvé rozkazy, pane... nedáváš mi jinou volbu.“"
        ),
        BondTierMilestone(
            tierLevel = 2,
            name = "Pokorná Služebná",
            stageTitle = "Tier II • Smaragdový Květ",
            subtitle = "Přivyká na tvou přítomnost a stará se o tebe s pečlivostí.",
            icon = "🌿",
            minAffinityPoints = 31,
            maxAffinityPoints = 70,
            palette = PALETTE_EMERALD,
            unlockedPortraitVariants = PORTRAIT_VARIANTS.filter { it.requiredAffinityLevel in 1..2 },
            combatPerkSummary = "+10 Max HP • +4% Útok • +2 Obrana",
            haremPerkSummary = "+10% Zisk zlata ze správy • Odemčena Smaragdová Paleta",
            dialogueQuote = "„Tvé doteky již nejsou tak děsivé... ráda ti budu sloužit.“"
        ),
        BondTierMilestone(
            tierLevel = 3,
            name = "Důvěrnice Komnat",
            stageTitle = "Tier III • Azurový Soumrak",
            subtitle = "Začíná vnímat tvou přízeň a svěřuje ti svá tajná přání a sny.",
            icon = "💎",
            minAffinityPoints = 71,
            maxAffinityPoints = 120,
            palette = PALETTE_CYAN,
            unlockedPortraitVariants = PORTRAIT_VARIANTS.filter { it.requiredAffinityLevel in 1..3 },
            combatPerkSummary = "+25 Max HP • +8% Útok • +4 Obrana • +10% Crit",
            haremPerkSummary = "+15% Zisk energie při rituálech • Azurový krystalový portrét",
            dialogueQuote = "„Pane, když jsi blízko, cítím zvláštní klid, který jsem nikdy nepoznala.“"
        ),
        BondTierMilestone(
            tierLevel = 4,
            name = "Oddaná Milenka",
            stageTitle = "Tier IV • Karmínová Vášeň",
            subtitle = "Planoucí vášeň a touha trávit každou noc v tvém objetí.",
            icon = "💖",
            minAffinityPoints = 121,
            maxAffinityPoints = 180,
            palette = PALETTE_CRIMSON,
            unlockedPortraitVariants = PORTRAIT_VARIANTS.filter { it.requiredAffinityLevel in 1..4 },
            combatPerkSummary = "+40 Max HP • +15% Útok • +6 Obrana • +3 HP/kolo Regenerace",
            haremPerkSummary = "+20% Šance na potomka • +10 Max Sexuální energie • Karmínová Paleta",
            dialogueQuote = "„Mé tělo i mé srdce patří tobě... spal mě svým žárem!“"
        ),
        BondTierMilestone(
            tierLevel = 5,
            name = "Spřízněná Duše",
            stageTitle = "Tier V • Astrální Ametyst",
            subtitle = "Nerozlučné pouto duší. Žije a dýchá jen pro tvou slávu a dominium.",
            icon = "🌌",
            minAffinityPoints = 181,
            maxAffinityPoints = 250,
            palette = PALETTE_ASTRAL,
            unlockedPortraitVariants = PORTRAIT_VARIANTS.filter { it.requiredAffinityLevel in 1..5 },
            combatPerkSummary = "+60 Max HP • +25% Útok • +10 Obrana • +10% Crit • +5 HP/kolo",
            haremPerkSummary = "+25% Poškození v bitvách • Nezlomná loajalita • Astrální róba Císařovny",
            dialogueQuote = "„Jsme jedno tělo a jedna duše, můj králi. Pro tvůj trůn zničím svět.“"
        ),
        BondTierMilestone(
            tierLevel = 6,
            name = "Věčná Královna",
            stageTitle = "Tier VI • Císařské Zlato",
            subtitle = "Absolutní transcendentní splynutí mysli a moci. Pravá vládkyně říše.",
            icon = "👑",
            minAffinityPoints = 251,
            maxAffinityPoints = 9999,
            palette = PALETTE_IMPERIAL,
            unlockedPortraitVariants = PORTRAIT_VARIANTS,
            combatPerkSummary = "+100 Max HP • +40% Útok • +15 Obrana • +15% Crit • +8 HP & +5 TE/kolo",
            haremPerkSummary = "+50% Celkový příjem říše • Zlatá Koruna & Zlatá Valkýra Vzhled",
            dialogueQuote = "„Budeme vládnout věčnosti společně. Koruna moci patří nám!“"
        )
    )

    fun getTierForAffinity(points: Int): BondTierMilestone {
        return BOND_TIERS.lastOrNull { points >= it.minAffinityPoints } ?: BOND_TIERS.first()
    }

    fun getTierByLevel(level: Int): BondTierMilestone {
        return BOND_TIERS.firstOrNull { it.tierLevel == level } ?: BOND_TIERS.first()
    }

    fun getPaletteById(paletteId: String): BondColorPalette {
        return ALL_PALETTES.firstOrNull { it.id == paletteId } ?: PALETTE_DEFAULT
    }

    fun getAvailablePortraitsForCharacter(character: Character): List<BondPortraitVariant> {
        val tier = getTierForAffinity(character.affinityPoints)
        val archetype = character.archetypeId
        return PORTRAIT_VARIANTS.filter { variant ->
            (variant.targetArchetypeId == "all" || variant.targetArchetypeId == archetype)
        }
    }

    fun getActivePortraitRes(character: Character): Int {
        // If character has selected a custom portrait variant
        if (character.customPortraitVariantId.isNotBlank() && character.customPortraitVariantId != "default") {
            val variant = PORTRAIT_VARIANTS.firstOrNull { it.id == character.customPortraitVariantId }
            if (variant != null && character.affinityLevel >= variant.requiredAffinityLevel) {
                return variant.drawableRes
            }
        }

        // If equipped skin from prestige catalog exists
        if (character.equippedSkin.isNotBlank() && character.equippedSkin != "default") {
            val skin = PrestigeSkinsCatalog.getSkinById(character.equippedSkin)
            if (skin != null) {
                return skin.drawableRes
            }
        }

        // Default archetype portrait
        return StaticData.getPortraitForArchetype(character.archetypeId)
    }

    fun getActivePalette(character: Character): BondColorPalette {
        return getPaletteById(character.customPaletteId)
    }
}
