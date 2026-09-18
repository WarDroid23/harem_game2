package com.example.haremdark.data

import com.example.haremdark.models.Character
import com.example.haremdark.ui.components.CharacterEmotionType
import kotlinx.serialization.Serializable

@Serializable
data class CharacterVoiceLine(
    val id: String,
    val title: String,
    val category: String, // "Pozdrav", "Věrnost", "Rozkoš", "Boj", "Vzpomínka"
    val requiredAffinityLevel: Int,
    val requiredLoyalty: Int = 0,
    val czechText: String,
    val emotionTrigger: CharacterEmotionType,
    val archetypeId: String? = null,
    val pitch: Float = 1.0f,
    val speechRate: Float = 0.95f
)

@Serializable
data class ArchiveEmotionInfo(
    val emotionType: CharacterEmotionType,
    val title: String,
    val description: String,
    val unlockedByDefault: Boolean = true,
    val requiredAffinityLevel: Int = 1
)

object CharacterArchiveCatalog {

    val EMOTIONS_ARCHIVE = listOf(
        ArchiveEmotionInfo(
            emotionType = CharacterEmotionType.BLUSH,
            title = "😳 Červenání & Stud",
            description = "Dívka zčervená rozpaky, když se dotkneš její kůže nebo ji pochválíš.",
            requiredAffinityLevel = 1
        ),
        ArchiveEmotionInfo(
            emotionType = CharacterEmotionType.CHEER,
            title = "🎉 Jásot & Nadšení",
            description = "Planoucí radost při oslavě vítězství nebo obdržení vzácného daru.",
            requiredAffinityLevel = 2
        ),
        ArchiveEmotionInfo(
            emotionType = CharacterEmotionType.LOVE,
            title = "💖 Vroucí Láska",
            description = "Sladké vyznání a hluboká vášeň v očích dívky.",
            requiredAffinityLevel = 3
        ),
        ArchiveEmotionInfo(
            emotionType = CharacterEmotionType.SHY,
            title = "🙈 Plachost & Rozpaky",
            description = "Jemné váhání a skrytý úsměv pod pohledem svůdce.",
            requiredAffinityLevel = 2
        ),
        ArchiveEmotionInfo(
            emotionType = CharacterEmotionType.SPARKLE,
            title = "✨ Okouzlení & Mystika",
            description = "Kouzlo okamžiku a mystická aura svírající srdce.",
            requiredAffinityLevel = 4
        )
    )

    val ALL_VOICE_LINES = listOf(
        // SUBMISIVNÍ DÍVKA (SUBKA)
        CharacterVoiceLine(
            id = "sub_greet_1",
            title = "Ranní Přivítání",
            category = "Pozdrav",
            requiredAffinityLevel = 1,
            requiredLoyalty = 10,
            czechText = "Můj pane... jsem plně připravena plnit tvá přání v dnešním dni.",
            emotionTrigger = CharacterEmotionType.BLUSH,
            archetypeId = "subka",
            pitch = 1.15f
        ),
        CharacterVoiceLine(
            id = "sub_devotion_1",
            title = "Slib Poslušnosti",
            category = "Věrnost",
            requiredAffinityLevel = 2,
            requiredLoyalty = 25,
            czechText = "Když mě držíš pevně v náručí, veškerý strach ze světa mizí.",
            emotionTrigger = CharacterEmotionType.LOVE,
            archetypeId = "subka",
            pitch = 1.2f
        ),
        CharacterVoiceLine(
            id = "sub_pleasure_1",
            title = "Šepot v Komnatách",
            category = "Rozkoš",
            requiredAffinityLevel = 3,
            requiredLoyalty = 40,
            czechText = "Tvé doteky mě pálí na kůži jako sladký oheň... prosím, nepřestávej.",
            emotionTrigger = CharacterEmotionType.SHY,
            archetypeId = "subka",
            pitch = 1.25f
        ),
        CharacterVoiceLine(
            id = "sub_max_1",
            title = "Vyznání Spřízněné Duše",
            category = "Vzpomínka",
            requiredAffinityLevel = 5,
            requiredLoyalty = 80,
            czechText = "Jsi můj bůh i můj jediný smysl života. Moje srdce patří navždy jen tobě!",
            emotionTrigger = CharacterEmotionType.SPARKLE,
            archetypeId = "subka",
            pitch = 1.2f
        ),

        // HRDÁ BOJOVNICE (VALKYRA)
        CharacterVoiceLine(
            id = "valk_greet_1",
            title = "Bitevní Zdravice",
            category = "Pozdrav",
            requiredAffinityLevel = 1,
            requiredLoyalty = 15,
            czechText = "Můj meč je ostrý a mé tělo připraveno k boji. Co poroučíš?",
            emotionTrigger = CharacterEmotionType.CHEER,
            archetypeId = "valkyra",
            pitch = 0.95f
        ),
        CharacterVoiceLine(
            id = "valk_battle_1",
            title = "Válečný Pokřik",
            category = "Boj",
            requiredAffinityLevel = 2,
            requiredLoyalty = 30,
            czechText = "Za našeho Pána! Nepřátelé padnou pod mým mečem!",
            emotionTrigger = CharacterEmotionType.CHEER,
            archetypeId = "valkyra",
            pitch = 0.9f
        ),
        CharacterVoiceLine(
            id = "valk_love_1",
            title = "Nezlomné Pouto Valkýry",
            category = "Věrnost",
            requiredAffinityLevel = 4,
            requiredLoyalty = 60,
            czechText = "Nepoklekla bych před žádným králem světa... ale tebou se nechám vést s hrdostí.",
            emotionTrigger = CharacterEmotionType.LOVE,
            archetypeId = "valkyra",
            pitch = 1.0f
        ),

        // TEMNÁ KNĚŽKA (KNEZKA)
        CharacterVoiceLine(
            id = "knez_greet_1",
            title = "Mystický Šepot",
            category = "Pozdrav",
            requiredAffinityLevel = 1,
            requiredLoyalty = 15,
            czechText = "Stíny ti žehnají, můj pane. Hvězdy dnes věští náš triumf.",
            emotionTrigger = CharacterEmotionType.SPARKLE,
            archetypeId = "knezka",
            pitch = 1.05f
        ),
        CharacterVoiceLine(
            id = "knez_ritual_1",
            title = "Hříšná Modlitba",
            category = "Rozkoš",
            requiredAffinityLevel = 3,
            requiredLoyalty = 45,
            czechText = "Mé tělo je tvým oltářem a tvá touha mým jediným zasvěcením.",
            emotionTrigger = CharacterEmotionType.LOVE,
            archetypeId = "knezka",
            pitch = 1.0f
        ),

        // UNIVERSAL / GENERAL VOICE LINES
        CharacterVoiceLine(
            id = "gen_gift_1",
            title = "Díky za Dar",
            category = "Pozdrav",
            requiredAffinityLevel = 1,
            requiredLoyalty = 5,
            czechText = "Děkuji ti z celého srdce! Tvá pozornost mě hřeje u duše.",
            emotionTrigger = CharacterEmotionType.CHEER,
            archetypeId = null,
            pitch = 1.1f
        ),
        CharacterVoiceLine(
            id = "gen_affinity_max",
            title = "Věčný Slib Harému",
            category = "Vzpomínka",
            requiredAffinityLevel = 5,
            requiredLoyalty = 75,
            czechText = "Naše příběhy se vryly do paměti věčnosti. Budu stát po tvém boku navěky.",
            emotionTrigger = CharacterEmotionType.SPARKLE,
            archetypeId = null,
            pitch = 1.1f
        )
    )

    fun getVoiceLinesForCharacter(character: Character): List<CharacterVoiceLine> {
        return ALL_VOICE_LINES.filter { line ->
            line.archetypeId == null || line.archetypeId.equals(character.archetypeId, ignoreCase = true)
        }
    }
}
