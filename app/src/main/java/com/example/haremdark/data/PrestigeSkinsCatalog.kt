package com.example.haremdark.data

import com.example.haremdark.R
import com.example.haremdark.models.Character
import java.io.Serializable

data class VisualSkinDefinition(
    val id: String,
    val name: String,
    val title: String,
    val characterArchetypeId: String, // "subka", "odvazna", "touha", "slechticna", "draci_divka", etc. or "all"
    val targetCharacterName: String,
    val tier: String, // "Mýtický", "Císařský", "Božský", "Gotický"
    val tierIcon: String, // "👑", "🌌", "🩸", "✨"
    val drawableRes: Int,
    val prestigeCost: Int,
    val description: String,
    val loreStory: String,
    val combatPerkSummary: String,
    val attackBonusPercent: Float = 0f,
    val defenseBonusPercent: Float = 0f,
    val critBonusPercent: Int = 0,
    val magicBonusPercent: Float = 0f,
    val affinityGainBonusPercent: Float = 0f,
    val auraColorHex: Long = 0xFFFF4081,
    val signatureQuote: String
) : Serializable

object PrestigeSkinsCatalog {

    val ALL_SKINS: List<VisualSkinDefinition> = listOf(
        VisualSkinDefinition(
            id = "skin_elena_astral_empress",
            name = "Elena: Astrální Císařovna Luny",
            title = "Císařský Astrální Oděv & Hvězdná Koruna",
            characterArchetypeId = "subka",
            targetCharacterName = "Elena",
            tier = "Božský",
            tierIcon = "🌌",
            drawableRes = R.drawable.img_skin_elena_astral_empress_1790460204605,
            prestigeCost = 5,
            description = "Epické roucho protkané astrálním stříbrem a hvězdným prachem. Elena se proměňuje z plaché otrokyně v nedotknutelnou císařovnu noci.",
            loreStory = "Když Elena přijala své místo po pánově boku, starobylá lunární magie rozzářila její stříbrné vlasy. Její runový obojek byl nahrazen korunou z hvězdného křišťálu.",
            combatPerkSummary = "+25% Astrální Magie • +15% Léčení družiny • +20% Zisk náklonnosti",
            attackBonusPercent = 0.05f,
            defenseBonusPercent = 0.15f,
            magicBonusPercent = 0.25f,
            affinityGainBonusPercent = 0.20f,
            auraColorHex = 0xFFB388FF,
            signatureQuote = "„Můj pane... v tvém objetí jsem našla světlo všech hvězd.“"
        ),
        VisualSkinDefinition(
            id = "skin_aurelia_inferno_empress",
            name = "Aurelia: Dračí Vládkyně Inferna",
            title = "Plamenná Zbroj z Dračích Šupin & Zlaté Rohy",
            characterArchetypeId = "odvazna",
            targetCharacterName = "Aurelia",
            tier = "Císařský",
            tierIcon = "🔥",
            drawableRes = R.drawable.img_skin_aurelia_inferno_empress_1790460216995,
            prestigeCost = 6,
            description = "Masivní pozlacená zbroj kalená v magmatu dračích vulkánů. Aureliiny zlaté rohy a ohnivé oči nahánějí nepřátelům v aréně čirý děs.",
            loreStory = "Plameny v Aureliině srdci se spojily s temnou vůlí jejího pána. Její pouta se roztavila a proměnila v nezdolnou dračí výzbroj suverénní gladiátorky.",
            combatPerkSummary = "+25% Fyzický Útok • +20% Šance na Kritický úder • +10% Průraz zbroje",
            attackBonusPercent = 0.25f,
            critBonusPercent = 20,
            defenseBonusPercent = 0.10f,
            auraColorHex = 0xFFFF5722,
            signatureQuote = "„Bojuji jen pro tebe, můj králi! Spálíme každého, kdo se nám postaví!“"
        ),
        VisualSkinDefinition(
            id = "skin_lilith_abyss_queen",
            name = "Lilith: Královna Propasti a Rozkoše",
            title = "Královský Korzet z Nočního Hedvábí & Runová Koruna",
            characterArchetypeId = "touha",
            targetCharacterName = "Lilith",
            tier = "Mýtický",
            tierIcon = "💜",
            drawableRes = R.drawable.img_skin_lilith_abyss_queen_1790460228042,
            prestigeCost = 8,
            description = "Nádherný korzet z černého hedvábí a purpurového sametu s pulzujícími runami démonické touhy a temné moci.",
            loreStory = "Lilith vystoupala na vrchol démonické hierarchie. Její dotek dokáže nepřátele v aréně okamžitě paralyzovat a zlomit jejich vůli.",
            combatPerkSummary = "+30% Temná Magie • +25% Zisk sexuální energie • +15% Omráčení nepřátel",
            magicBonusPercent = 0.30f,
            attackBonusPercent = 0.10f,
            critBonusPercent = 15,
            affinityGainBonusPercent = 0.25f,
            auraColorHex = 0xFFAB47BC,
            signatureQuote = "„Pojď blíž... nech mě naplnit tvé sny nepředstavitelnou temnou rozkoší.“"
        ),
        VisualSkinDefinition(
            id = "skin_carmilla_blood_sovereign",
            name = "Carmilla: Gotická Krvavá Suverénka",
            title = "Královská Rubínová Róba & Gotický Diadém",
            characterArchetypeId = "slechticna",
            targetCharacterName = "Carmilla",
            tier = "Gotický",
            tierIcon = "🩸",
            drawableRes = R.drawable.img_skin_carmilla_blood_sovereign_1790460239235,
            prestigeCost = 10,
            description = "Luxusní gotická róba barvy čerstvé krve s vyšívaným zlatem. Carmillina aristokratická pýcha se stala chloubou celého dominia.",
            loreStory = "Upíří princezna znovu usedla na krvavý trůn. Zlomená minulost pominula a její aristokratický půvab velí celým armádám v aréně.",
            combatPerkSummary = "+20% Sání života (Lifesteal) • +15% Všechny atributy • +25% Prestiž dominia",
            attackBonusPercent = 0.15f,
            defenseBonusPercent = 0.15f,
            magicBonusPercent = 0.15f,
            critBonusPercent = 10,
            auraColorHex = 0xFFD32F2F,
            signatureQuote = "„Krev našich nepřátel naplní mé poháry. Jsem tvou věčnou královnou krve.“"
        ),
        VisualSkinDefinition(
            id = "skin_valkyrie_golden_dominion",
            name = "Valeriana / Victoria: Zlatá Valkýra Dominia",
            title = "Posvátná Zlatá Zbroj Padlých Andělů & Světelná Aura",
            characterArchetypeId = "all",
            targetCharacterName = "Všechny válečnice",
            tier = "Božský",
            tierIcon = "👑",
            drawableRes = R.drawable.img_skin_valkyrie_golden_dominion_1790460252949,
            prestigeCost = 12,
            description = "Nádherná zlatá zbroj a andělská křídla vykovaná ze samotné podstaty sluneční a astrální síly.",
            loreStory = "Posvátná strážkyně říše, která svou neochvějnou věrností a zlatým mečem chrání harém i dominium před všemi pohromami.",
            combatPerkSummary = "+20% Týmová obrana • +15% Odolnost proti všem živlům • +20% Rychlost",
            defenseBonusPercent = 0.20f,
            attackBonusPercent = 0.15f,
            magicBonusPercent = 0.15f,
            auraColorHex = 0xFFFFD700,
            signatureQuote = "„Můj meč a má duše patří tomuto dominiu. Společně ovládneme celý svět!“"
        )
    )

    fun getSkinById(skinId: String): VisualSkinDefinition? {
        return ALL_SKINS.find { it.id == skinId }
    }

    fun getSkinsForCharacter(character: Character): List<VisualSkinDefinition> {
        return ALL_SKINS.filter { skin ->
            skin.characterArchetypeId == "all" ||
                    skin.characterArchetypeId == character.archetypeId ||
                    skin.characterArchetypeId == character.archetype ||
                    skin.targetCharacterName.contains(character.name, ignoreCase = true) ||
                    (character.archetypeId in listOf("subka", "ticha_panenka", "ustrasena") && skin.characterArchetypeId == "subka") ||
                    (character.archetypeId in listOf("odvazna", "draci_divka", "krvava_subka") && skin.characterArchetypeId == "odvazna") ||
                    (character.archetypeId in listOf("touha", "sukuba", "nymfomanka", "posedla") && skin.characterArchetypeId == "touha") ||
                    (character.archetypeId in listOf("slechticna", "chladna", "manipulativni") && skin.characterArchetypeId == "slechticna")
        }
    }

    fun getSkinDrawable(skinId: String?, archetypeId: String): Int {
        if (skinId == null || skinId == "default") {
            return StaticData.getPortraitForArchetype(archetypeId)
        }
        val def = getSkinById(skinId)
        return def?.drawableRes ?: StaticData.getPortraitForArchetype(archetypeId)
    }
}
