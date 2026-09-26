package com.example.haremdark.models

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Combat and thematic roles for characters in the Lord's harem.
 */
@Serializable
enum class HaremRole(
    val id: String,
    val title: String,
    val icon: String,
    val description: String
) {
    WARRIOR("warrior", "Válečnice", "⚔️", "Přímý boj zblízka a drtivá fyzická síla"),
    GUARDIAN("guardian", "Ochránkyně", "🛡️", "Těžká obrana, štíty a neprostupná ochrana Pána"),
    SORCERESS("sorceress", "Čarodějka", "🔮", "Temná magie, plošná kouzla a živlové útoky"),
    ASSASSIN("assassin", "Vražedkyně", "🗡️", "Rychlé smrtící údery, přepady a kritické zásahy"),
    HEALER("healer", "Kněžka", "✨", "Léčení zranění, obnova many a posvátná požehnání"),
    SIREN("siren", "Siréna", "💋", "Svádění, oslabování nepřátel a debuffy morálky"),
    CONSORT("consort", "Družka", "👑", "Správa dominiu, diplomatická podpora a inspirace"),
    SCHOLAR("scholar", "Alchymistka", "🧪", "Výroba lektvarů, výzkum zakázaných svitků a artefaktů"),
    COMPANION("companion", "Společnice", "💖", "Oddaná společnice a citová opora harému");

    companion object {
        fun fromString(value: String): HaremRole {
            return entries.find { 
                it.name.equals(value, ignoreCase = true) || 
                it.id.equals(value, ignoreCase = true) || 
                it.title.equals(value, ignoreCase = true) 
            } ?: COMPANION
        }
    }
}

/**
 * Data class representing a character residing in the player's harem.
 *
 * Core attributes required:
 * @property name The name of the character.
 * @property affection Affection/bond rating with the Lord (0 - 100+).
 * @property powerLevel Combat and magical power rating.
 * @property role Tactical and social role within the harem.
 */
@Serializable
data class HaremCharacter(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var affection: Int = 50,
    var powerLevel: Int = 100,
    var role: HaremRole = HaremRole.COMPANION,
    var archetypeId: String = "draci_divka",
    var title: String = "",
    var level: Int = 1,
    var loyalty: Int = 50,
    var obedience: Int = 50,
    var currentHp: Int = 100,
    var maxHp: Int = 100,
    var currentMana: Int = 50,
    var maxMana: Int = 50,
    var element: Element = Element.DARK,
    var status: String = "Aktivní",
    var mood: String = "Spokojená",
    var isFavorite: Boolean = false,
    var isWife: Boolean = false,
    var bio: String = "",
    var avatarIcon: String = "👑",
    var unlockedSkills: List<String> = emptyList(),
    var availableSkillPoints: Int = 0,
    var statBonuses: Map<String, Int> = emptyMap(),
    var interactionHistory: MutableList<InteractionLogEntry> = mutableListOf(),
    var morale: Int = 50,
    var statHistory: MutableList<StatRecord> = mutableListOf()
) {
    val moraleMultiplier: Float
        get() = when {
            morale >= 80 -> 1.2f
            morale >= 60 -> 1.1f
            morale >= 40 -> 1.0f
            morale >= 20 -> 0.9f
            else -> 0.8f
        }

    val bondingLevel: BondingLevel
        get() = BondingLevel.fromAffinity(affection)

    val activeBonuses: List<BondingBonus>
        get() = BondingBonuses.levelBonuses.filter { (level, _) -> 
            affection >= level.threshold 
        }.values.toList()

    /**
     * Human-readable affection stage description.
     */
    val affectionStage: String
        get() = when {
            affection >= 90 -> "Věčná láska"
            affection >= 75 -> "Hluboká oddanost"
            affection >= 50 -> "Vřelá náklonnost"
            affection >= 25 -> "Zaujatá"
            else -> "Obezřetná"
        }

    /**
     * Tier classification based on current power level.
     */
    val powerTier: String
        get() = when {
            powerLevel >= 500 -> "Mýtická"
            powerLevel >= 300 -> "Legendární"
            powerLevel >= 200 -> "Elitní"
            powerLevel >= 100 -> "Zkušená"
            else -> "Nováček"
        }

    /**
     * Whether the character is alive and fit for party expeditions.
     */
    val isCombatReady: Boolean
        get() = currentHp > 0 && status != "Zraněná" && status != "V bezvědomí"

    /**
     * Formatted role display with icon.
     */
    val formattedRole: String
        get() = "${role.icon} ${role.title}"
}

/**
 * Extension mapper to convert a legacy [Character] model into a clean [HaremCharacter].
 */
fun Character.toHaremCharacter(): HaremCharacter {
    val mappedRole = when {
        role.contains("Válečnice", ignoreCase = true) || archetypeId in listOf("odvazna", "vzdorna") -> HaremRole.WARRIOR
        role.contains("Ochránkyně", ignoreCase = true) || role.contains("Tank", ignoreCase = true) -> HaremRole.GUARDIAN
        role.contains("Kouzelnice", ignoreCase = true) || archetypeId in listOf("touha", "nymfomanka") -> HaremRole.SORCERESS
        role.contains("Vražedkyně", ignoreCase = true) || archetypeId in listOf("krvava_subka", "zlomena") -> HaremRole.ASSASSIN
        role.contains("Kněžka", ignoreCase = true) || archetypeId == "knezkyn" -> HaremRole.HEALER
        role.contains("Siréna", ignoreCase = true) || archetypeId == "sukuba" -> HaremRole.SIREN
        jeManzelkou || partnerka -> HaremRole.CONSORT
        else -> HaremRole.fromString(role)
    }

    val calculatedPower = (strength * 4) + (level * 15) + (hp / 2) + (attributes.strength * 2)

    val charElement = when (archetypeId) {
        "sukuba", "krvava_subka" -> Element.DARK
        "chladna" -> Element.ICE
        "draci_divka" -> Element.FIRE
        "subka" -> Element.WATER
        "touha" -> Element.LIGHTNING
        "knezkyn" -> Element.HOLY
        "vzdorna" -> Element.EARTH
        else -> Element.PHYSICAL
    }

    return HaremCharacter(
        id = id,
        name = name,
        affection = attributes.affection.coerceAtLeast(affinityPoints),
        powerLevel = calculatedPower.coerceAtLeast(10),
        role = mappedRole,
        archetypeId = archetypeId,
        title = if (jeManzelkou) "Královská manželka" else if (partnerka) "Milostnice" else mappedRole.title,
        level = level,
        loyalty = loajalita,
        obedience = poslusnost,
        currentHp = hp,
        maxHp = maxHp,
        currentMana = mana,
        maxMana = maxMana,
        element = charElement,
        status = if (hp <= 0) "V bezvědomí" else if (tehotna) "Těhotná" else if (naNajmu) "Na misi" else "Aktivní",
        mood = nalada,
        isFavorite = oblibena || isPinned,
        isWife = jeManzelkou,
        bio = "$name - hrdinka harému s unikátní povahou a oddaností Pánovi dominia.",
        avatarIcon = when (archetypeId) {
            "draci_divka" -> "🐉"
            "sukuba" -> "😈"
            "chladna" -> "❄️"
            "vzdorna" -> "🛡️"
            "knezkyn" -> "✨"
            "krvava_subka" -> "🩸"
            else -> "👑"
        },
        unlockedSkills = unlockedCombatSkills.toList(),
        morale = morale,
        statHistory = statHistory.toMutableList()
    )
}

/**
 * Extension mapper to sync changes back into the legacy [Character] model.
 */
fun HaremCharacter.applyToCharacter(target: Character) {
    target.name = name
    target.attributes.affection = affection
    target.affinityPoints = affection
    target.loajalita = loyalty
    target.loyalty = loyalty
    target.poslusnost = obedience
    target.level = level
    target.hp = currentHp
    target.maxHp = maxHp
    target.mana = currentMana
    target.maxMana = maxMana
    target.oblibena = isFavorite
    target.jeManzelkou = isWife
    target.role = role.title
    target.nalada = mood
    target.morale = morale
    target.statHistory = statHistory.toMutableList()
}
