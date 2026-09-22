package com.example.haremdark.domain

import com.example.haremdark.data.StaticData
import com.example.haremdark.models.Character
import com.example.haremdark.models.CharacterAttributes
import java.util.UUID
import kotlin.random.Random

data class RecruitTier(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val goldCost: Int,
    val manaCost: Int,
    val icon: String,
    val badge: String,
    val colorHex: Long,
    val rarityDescription: String
)

data class RecruitResult(
    val success: Boolean,
    val message: String,
    val character: Character? = null
)

object CharacterGenerator {

    val RECRUIT_TIERS = listOf(
        RecruitTier(
            id = "basic",
            title = "Průzkum okolí",
            subtitle = "Pátrání ve stínech provincií",
            description = "Vyšli hlídky do podhradí a temných uliček. Rychlé a levné nalezení běžných dívek pro chod dominia.",
            goldCost = 250,
            manaCost = 0,
            icon = "🧭",
            badge = "Běžný nábor",
            colorHex = 0xFF81C784,
            rarityDescription = "Běžné (1★) až Vzácné (2★)"
        ),
        RecruitTier(
            id = "advanced",
            title = "Černý trh & Zajatci",
            subtitle = "Aukce zajatých šlechtičen a válečnic",
            description = "Nákup z tajných konvojů a válečných tažení. Zaručeně talentovanější dcery se zvýšenými atributy.",
            goldCost = 600,
            manaCost = 20,
            icon = "⛓️",
            badge = "Vzácný nábor",
            colorHex = 0xFF4FC3F7,
            rarityDescription = "Vzácné (2★) až Epické (3★)"
        ),
        RecruitTier(
            id = "elite",
            title = "Temný rituál vyvolání",
            subtitle = "Přivolání z jiné dimenze či starodávné krve",
            description = "Mystický rituál vyžadující krev i čistou manu. Přivolává mocné sukuby, dračí princezny a legendární bytosti.",
            goldCost = 1500,
            manaCost = 50,
            icon = "🔮",
            badge = "Elitní rituál",
            colorHex = 0xFFBA68C8,
            rarityDescription = "Epické (3★), Legendární (4★) i Mytické (5★)"
        )
    )

    private val FIRST_NAMES = listOf(
        "Cleopatra", "Valeria", "Morgana", "Elena", "Lyra", "Selena", "Astrid",
        "Lilith", "Aria", "Cassandra", "Roxana", "Diana", "Nyx", "Seraphina",
        "Kaelen", "Vespera", "Morrigan", "Sybilla", "Yvaine", "Ravenna",
        "Beatrix", "Ophelia", "Isolde", "Genevieve", "Katarina", "Nadia",
        "Althea", "Elowen", "Zephyra", "Freya", "Aurelia", "Sylvia",
        "Rowena", "Dahlia", "Cynthia", "Iris", "Nocturna", "Thyra",
        "Sera", "Vex", "Thalia", "Kaelia", "Rina", "Myra", "Zora", "Tess"
    )

    private val EPITHETS = listOf(
        "ze Stínů", "z Mlžného hvozdu", "Temné krve", "Krvavá lilie",
        "z Císařství", "Ledová", "Noční květ", "ze Zlatého dvora",
        "Beze jména", "Bouřná", "Černá růže", "z Černé věže", "Divoká"
    )

    private val EXTRA_TRAITS = listOf(
        "Bystrá", "Krásná", "Odolná", "Vášnivá", "Tichá", "Věrná", "Půvabná",
        "Ostražitá", "Hrdá", "Smyslná", "Oddaná", "Tajnůstkářská", "Žárlivá",
        "Nespoutaná", "Hravá", "Křehká", "Vytrvalá", "Kouzelná"
    )

    /**
     * Rolls random character rarity according to recruitment tier RNG rules.
     */
    fun rollRarity(tierId: String): Int {
        val roll = Random.nextInt(100)
        return when (tierId) {
            "elite" -> when {
                roll < 4 -> 5   // 4% Mythic
                roll < 22 -> 4  // 18% Legendary
                roll < 70 -> 3  // 48% Epic
                else -> 2       // 30% Rare
            }
            "advanced" -> when {
                roll < 5 -> 4   // 5% Legendary
                roll < 25 -> 3  // 20% Epic
                roll < 85 -> 2  // 60% Rare
                else -> 1       // 15% Common
            }
            else -> when { // "basic" or default
                roll < 5 -> 3   // 5% Epic
                roll < 30 -> 2  // 25% Rare
                else -> 1       // 70% Common
            }
        }
    }

    /**
     * Generates a fully formed Character with RNG attributes, personality, traits, and archetype.
     */
    fun generateRandomCharacter(tierId: String = "basic"): Character {
        val rarity = rollRarity(tierId)

        // 1. Name RNG
        val baseName = FIRST_NAMES.random()
        val fullName = if (Random.nextInt(100) < 30) {
            "$baseName ${EPITHETS.random()}"
        } else {
            baseName
        }

        // 2. Archetype RNG
        val availableArchetypes = StaticData.ARCHETYPES.keys.toList()
        val archetypeId = when {
            tierId == "elite" && Random.nextBoolean() -> {
                // Higher chance for exotic archetypes on elite
                listOf("sukuba", "draci_divka", "krvava_subka", "slechticna", "nymfomanka").random()
            }
            else -> availableArchetypes.random()
        }

        val archetype = StaticData.ARCHETYPES[archetypeId]

        // 3. Age RNG
        val age = Random.nextInt(18, 27)

        // 4. Role based on archetype
        val role = when (archetypeId) {
            "odvazna", "vzdorna", "draci_divka" -> listOf("Válečnice", "Gladiátorka", "Strážkyně").random()
            "sukuba", "touha", "nymfomanka" -> listOf("Kurtizána", "Kněžka rozkoše", "Společnice").random()
            "slechticna", "manipulativni", "chladna" -> listOf("Intrikánka", "Diplomatka", "Šlechtična").random()
            "subka", "zlomena", "ticha_panenka", "ustrasena" -> listOf("Služka", "Komorná", "Léčitelka").random()
            "krvava_subka", "posedla", "hysterialni" -> listOf("Krvavá kněžka", "Čarodějka stínů", "Vražedkyně").random()
            else -> "Členka harému"
        }

        // 5. Attributes RNG calculation
        val isCombatArchetype = archetypeId in listOf("odvazna", "draci_divka", "krvava_subka", "vzdorna")
        val isSubmissiveArchetype = archetypeId in listOf("subka", "zlomena", "ticha_panenka", "ustrasena")
        val isSensualArchetype = archetypeId in listOf("touha", "sukuba", "nymfomanka")

        // Strength (Síla)
        val baseStrength = Random.nextInt(12, 22)
        val strengthBonus = (if (isCombatArchetype) Random.nextInt(6, 12) else 0) + (rarity * 4)
        val rolledStrength = (baseStrength + strengthBonus).coerceIn(10, 99)

        // Loyalty (Loajalita)
        val baseLoyalty = Random.nextInt(20, 36)
        val loyaltyBonus = (if (isSubmissiveArchetype) Random.nextInt(8, 16) else 0) +
                (if (archetypeId == "vzdorna") -10 else 0) +
                (rarity * 3)
        val rolledLoyalty = (baseLoyalty + loyaltyBonus).coerceIn(5, 95)

        // Affection (Náklonnost / Srdce)
        val baseAffection = Random.nextInt(38, 62)
        val affectionBonus = (if (isSensualArchetype) Random.nextInt(8, 15) else 0) +
                (if (archetypeId == "zlomena") -10 else 0) +
                (rarity * 3)
        val rolledAffection = (baseAffection + affectionBonus).coerceIn(15, 95)

        // Obedience (Poslušnost)
        val baseObedience = Random.nextInt(20, 42) + (if (isSubmissiveArchetype) 15 else 0)
        val rolledObedience = baseObedience.coerceIn(5, 95)

        // Morale
        val rolledMorale = Random.nextInt(45, 75)

        // Lust (Touha)
        val baseLust = Random.nextInt(30, 60) + (if (isSensualArchetype) 20 else 0)
        val rolledLust = baseLust.coerceIn(10, 95)

        // Fear (Strach)
        val rolledFear = (if (archetypeId == "ustrasena") Random.nextInt(35, 60) else Random.nextInt(15, 35)).coerceIn(5, 80)

        // Defense
        val rolledDefense = (Random.nextInt(8, 16) + (rarity * 3)).coerceIn(5, 70)

        // HP & Mana
        val calculatedHp = 100 + (rarity * 20) + (rolledStrength * 2)
        val calculatedMana = 40 + (rarity * 15) + (if (archetypeId in listOf("posedla", "sukuba")) 25 else 0)

        // 6. Traits RNG
        val archetypeTraits = StaticData.getTraitsForArchetype(archetypeId)
        val combinedTraits = (archetypeTraits + EXTRA_TRAITS.shuffled()).distinct().take(Random.nextInt(2, 4)).toMutableList()

        // 7. Initial Status Icon & Mood
        val (initialMood, initialIcon) = when {
            archetypeId == "subka" -> Pair("poslušná", "🥺")
            archetypeId == "vzdorna" -> Pair("vzdorná", "😤")
            archetypeId == "touha" || archetypeId == "sukuba" -> Pair("vzrušená", "🫦")
            archetypeId == "zlomena" -> Pair("apatická", "😶")
            archetypeId == "draci_divka" -> Pair("hrdá", "🔥")
            else -> Pair("zvědavá", "😐")
        }

        // 8. Affinity setup
        val initialAffinity = 15 + ((rarity - 1) * 12) + Random.nextInt(0, 10)

        // 9. Initial Skills
        val initialSkills = mutableMapOf(
            "combat" to (if (isCombatArchetype) Random.nextInt(1, rarity + 1) else Random.nextInt(0, rarity)),
            "defense" to Random.nextInt(0, rarity),
            "production" to Random.nextInt(0, rarity),
            "rental" to (if (isSensualArchetype) Random.nextInt(1, rarity + 1) else Random.nextInt(0, rarity))
        )

        val uniqueId = "c_${UUID.randomUUID().toString().take(8)}"

        val attributesObj = CharacterAttributes(
            strength = rolledStrength,
            loyalty = rolledLoyalty,
            affection = rolledAffection,
            obedience = rolledObedience,
            morale = rolledMorale,
            lust = rolledLust,
            fear = rolledFear,
            defense = rolledDefense
        )

        return Character(
            id = uniqueId,
            name = fullName,
            age = age,
            archetypeId = archetypeId,
            rarity = rarity,
            hp = calculatedHp,
            maxHp = calculatedHp,
            mana = calculatedMana,
            maxMana = calculatedMana,
            srdce = rolledAffection,
            poslusnost = rolledObedience,
            vlhkost = 40 + Random.nextInt(0, 30),
            submisivita = if (isSubmissiveArchetype) 55 + Random.nextInt(0, 25) else 30 + Random.nextInt(0, 20),
            loajalita = rolledLoyalty,
            strength = rolledStrength,
            attributes = attributesObj,
            nalada = initialMood,
            statusIcon = initialIcon,
            morale = rolledMorale,
            plodnost = Random.nextInt(35, 75),
            duvera = rolledLoyalty / 2,
            touha = rolledLust,
            strach = rolledFear,
            role = role,
            level = rarity,
            xp = 0,
            skillPoints = (rarity - 1).coerceAtLeast(0),
            skills = initialSkills,
            affinityPoints = initialAffinity,
            affinityLevel = 1,
            traits = combinedTraits
        )
    }
}
