package com.example.haremdark.data

import com.example.haremdark.domain.CharacterEmotionType
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.domain.VoiceManager
import com.example.haremdark.models.Character

/**
 * Data representation of a unique flavor dialogue tied to a specific affinity milestone.
 */
data class MilestoneDialogue(
    val id: String,
    val milestoneThreshold: Int,
    val text: String,
    val title: String,
    val emotion: CharacterEmotionType = CharacterEmotionType.LOVE,
    val audioPitchTag: String = "Střední tónina"
)

/**
 * Metadata for a milestone tier.
 */
data class MilestoneHeaderInfo(
    val threshold: Int,
    val title: String,
    val subtitle: String,
    val icon: String,
    val colorHex: Long
)

/**
 * Catalog and trigger engine for unique audio and text-based flavor dialogues
 * across the 6 specific affinity milestones (30, 70, 120, 180, 250, 350).
 */
object MilestoneFlavorDialogueCatalog {

    val MILESTONE_THRESHOLDS = listOf(30, 70, 120, 180, 250, 350)

    fun getHeaderInfo(threshold: Int): MilestoneHeaderInfo {
        return when (threshold) {
            30 -> MilestoneHeaderInfo(30, "Pokorná služebná", "První zlom nedůvěry a přijetí služby", "⛓️", 0xFF4CAF50)
            70 -> MilestoneHeaderInfo(70, "Důvěrnice komnat", "Sdílení tajemství a vnitřní otevřenost", "💎", 0xFF00BCD4)
            120 -> MilestoneHeaderInfo(120, "Oddaná milenka", "Planoucí vášeň a nerozlučné tělesné pouto", "💖", 0xFFFF4081)
            180 -> MilestoneHeaderInfo(180, "Paní komnat", "Spřízněná duše a věrnost dominia", "👑", 0xFFB388FF)
            250 -> MilestoneHeaderInfo(250, "Věčné splynutí", "Magické provázání duší a myšlenek", "✨", 0xFFE040FB)
            350 -> MilestoneHeaderInfo(350, "Věčná královna", "Absolutní suverénní harmonie panství", "🌌", 0xFFFFD700)
            else -> MilestoneHeaderInfo(threshold, "Milník pouta", "Hluboké pouto s pánem", "⭐", 0xFFFF80AB)
        }
    }

    /**
     * Returns the list of unique flavor dialogues for a specific milestone and archetype.
     */
    fun getDialoguesForMilestone(milestone: Int, archetypeId: String): List<MilestoneDialogue> {
        val archeKey = archetypeId.lowercase()
        return when (milestone) {
            30 -> when (archeKey) {
                "subka" -> listOf(
                    MilestoneDialogue("m30_sub_1", 30, "Můj pane... tvé pevné vedení mi dává klid. Pokorně skládám svůj strach k tvým nohám.", "Přijetí poslušnosti", CharacterEmotionType.BLUSH),
                    MilestoneDialogue("m30_sub_2", 30, "Už se nechvěji děsem, ale touhou zalíbit se ti. Tvá přísnost je mým bezpečným řádem.", "Tiché podvolení", CharacterEmotionType.SHY),
                    MilestoneDialogue("m30_sub_3", 30, "Každý tvůj pohled mě učí, kde je mé pravé místo. Budu ti sloužit s čistým srdcem.", "Slib oddanosti", CharacterEmotionType.LOVE)
                )
                "odvazna" -> listOf(
                    MilestoneDialogue("m30_odv_1", 30, "Můj meč sice padl do tvých rukou, ale tvá síla si získala můj první respekt. Přijímám tvou vůli.", "Válečnický respekt", CharacterEmotionType.CHEER),
                    MilestoneDialogue("m30_odv_2", 30, "Nečekala jsem, že tvůj pohled dokáže zkrotit mou hrdost. Zůstávám stát po tvém boku, pane.", "Uznání nadvlády", CharacterEmotionType.BLUSH),
                    MilestoneDialogue("m30_odv_3", 30, "Zajatkyně už nejsem. Nyní jsem bojovnicí, která si zvyká na tvůj velící hlas.", "Zkrocená hrdost", CharacterEmotionType.LOVE)
                )
                else -> listOf(
                    MilestoneDialogue("m30_gen_1", 30, "Už se tě tolik nebojím, můj pane... Zvykám si na tvůj hlas a začínám chápat, co ode mě žádáš.", "První důvěra", CharacterEmotionType.BLUSH),
                    MilestoneDialogue("m30_gen_2", 30, "Mé ruce jsou připraveny sloužit tvému panství. Chci ti dokázat svou užitečnost.", "Ochotná služba", CharacterEmotionType.CHEER),
                    MilestoneDialogue("m30_gen_3", 30, "Když na mě pohlédneš, cítím chvění. Dříve to byl děs, ale dnes je to touha nezklamat tě.", "Svěřená poslušnost", CharacterEmotionType.LOVE)
                )
            }

            70 -> when (archeKey) {
                "subka" -> listOf(
                    MilestoneDialogue("m70_sub_1", 70, "Nikomu jsem nesvěřila své nejhlubší touhy... jen tobě. Věřím ti víc než komukoliv jinému.", "Tajná vyznání", CharacterEmotionType.SHY),
                    MilestoneDialogue("m70_sub_2", 70, "Tvé vlídné doteky léčí mé rány. Cítím se s tebou tak křehká a přesto chráněná.", "Hřejivé bezpečí", CharacterEmotionType.LOVE),
                    MilestoneDialogue("m70_sub_3", 70, "Když mě hladíš po vlasech, zapomínám na celý svět. Má důvěra v tebe je bezmezná.", "Oddaná něha", CharacterEmotionType.BLUSH)
                )
                "odvazna" -> listOf(
                    MilestoneDialogue("m70_odv_1", 70, "Kryji ti záda v každé potyčce. Má čepel nezná strach, dokud vím, že bojuješ za mnou.", "Bojová spřízněnost", CharacterEmotionType.CHEER),
                    MilestoneDialogue("m70_odv_2", 70, "Začínám chápat tvé plány, pane. Důvěřuji tvým taktickým rozkazům i tvému srdci.", "Válečná rada", CharacterEmotionType.LOVE),
                    MilestoneDialogue("m70_odv_3", 70, "Nikdy jsem se před nikým nesklonila tak ráda jako před tvou moudrostí.", "Hluboké uznání", CharacterEmotionType.BLUSH)
                )
                else -> listOf(
                    MilestoneDialogue("m70_gen_1", 70, "Tvé doteky již nejsou poutem, ale teplem, které vyhledávám. Důvěřuji ti víc než svému strachu.", "Prolomení hradeb", CharacterEmotionType.LOVE),
                    MilestoneDialogue("m70_gen_2", 70, "Když usínám v komnatách, myslím na tvou přízeň. V mém srdci jsi získal trvalé místo.", "Tiché přiznání", CharacterEmotionType.BLUSH),
                    MilestoneDialogue("m70_gen_3", 70, "Tvá slova mají moc vyléčit mé pochybnosti. Jsem tvou věrnou důvěrnicí, pane.", "Důvěrné pouto", CharacterEmotionType.CHEER)
                )
            }

            120 -> listOf(
                MilestoneDialogue("m120_1", 120, "Mé srdce buší jen pro tebe! Každý tvůj polibek ve mně zažíhá plamen, který nedokážu uhasit.", "Spalující vášeň", CharacterEmotionType.LOVE),
                MilestoneDialogue("m120_2", 120, "Už nechci patřit nikomu jinému. Vezmi si mé tělo i duši – jsem celá tvá, můj milovaný vládce.", "Absolutní odevzdání", CharacterEmotionType.BLUSH),
                MilestoneDialogue("m120_3", 120, "Tvá náruč je jediné místo, kde se cítím v bezpečí i v sladké extázi zároveň. Miluji tě celou svou bytostí.", "Milostná přísaha", CharacterEmotionType.SPARKLE)
            )

            180 -> listOf(
                MilestoneDialogue("m180_1", 180, "Cítím tvůj dech i myšlenky dřív, než promluvíš. Naše duše se propletly v nerozlučný uzel.", "Splynutí duší", CharacterEmotionType.SPARKLE),
                MilestoneDialogue("m180_2", 180, "Společně vybudujeme říši, která přetrvá věky. Budu stát po tvém boku v temnotě i na výsluní moci.", "Paní dominia", CharacterEmotionType.CHEER),
                MilestoneDialogue("m180_3", 180, "I kdyby se proti nám postavil celý svět, má věrnost nikdy nezakolísá. Patříme k sobě navždy.", "Věčné spojenectví", CharacterEmotionType.LOVE)
            )

            250 -> listOf(
                MilestoneDialogue("m250_1", 250, "Žádná magie, žádný čas ani smrt nedokáže přetnout pouto, které nás spojuje. Jsem tvá věčná družka.", "Nesmrtelné pouto", CharacterEmotionType.SPARKLE),
                MilestoneDialogue("m250_2", 250, "Tvá temná energie protéká mými žilami. Jsme jedno tělo a jedna vůle v tomto krutém světě.", "Magická rezonance", CharacterEmotionType.LOVE),
                MilestoneDialogue("m250_3", 250, "Všechny mé staré vzpomínky vybledly. Můj skutečný život začal až ve chvíli, kdy jsi mě přijal k sobě.", "Znovuzrozená láska", CharacterEmotionType.BLUSH)
            )

            350 -> listOf(
                MilestoneDialogue("m350_1", 350, "Vládni se mnou tomuto světu, můj císaři! Naše pouto dosáhlo božské dokonalosti a žádná moc ho nezlomí.", "Císařská koruna", CharacterEmotionType.SPARKLE),
                MilestoneDialogue("m350_2", 350, "Jsem tvou královnou, tvou zbraní i tvým bezpečným přístavem. Naše láska proměnila temnotu v nehynoucí záři.", "Královna noci", CharacterEmotionType.LOVE),
                MilestoneDialogue("m350_3", 350, "Věčný Mistr a jeho královna. Korunoval jsi mou duši a já ti odevzdávám celé své věčné bytí.", "Absolutní harmonie", CharacterEmotionType.CHEER)
            )

            else -> listOf(
                MilestoneDialogue("m_gen", milestone, "Můj pane, má oddanost patří navždy tvému dominium.", "Hlas srdce", CharacterEmotionType.LOVE)
            )
        }
    }

    /**
     * Returns all unlocked milestone thresholds for a character.
     */
    fun getUnlockedMilestones(affinityPoints: Int): List<Int> {
        return MILESTONE_THRESHOLDS.filter { affinityPoints >= it }
    }

    /**
     * Triggers unique audio and voice playback for a milestone flavor dialogue.
     */
    fun triggerMilestoneAudio(
        dialogueText: String,
        archetypeId: String?,
        milestoneThreshold: Int
    ) {
        // 1. Play musical chime tuned to milestone tier
        val tierIndex = when (milestoneThreshold) {
            30 -> 2
            70 -> 3
            120 -> 4
            180 -> 5
            250 -> 6
            350 -> 6
            else -> 1
        }
        SoundEffectManager.playFlavorDialogueChime(tierIndex)

        // 2. Play spoken audio via Text-To-Speech with archetype pitch modulation
        VoiceManager.speak(dialogueText, archetypeId)

        // 3. Tactile haptic feedback
        HapticManager.vibrateClick()
    }
}
