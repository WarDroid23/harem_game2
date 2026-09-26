package com.example.haremdark.domain

import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.DomainData
import com.example.haremdark.data.StaticData
import com.example.haremdark.models.Character
import com.example.haremdark.models.Player
import java.io.Serializable
import java.util.UUID

data class ProceduralChoice(
    val id: String,
    val title: String,
    val description: String,
    val approach: String, // "DOMINANT", "ROMANTIC", "PASSIONATE", "STRATEGIC"
    val icon: String,
    val reactionMonologue: String,
    val affinityBonus: Int,
    val loyaltyBonus: Int = 0,
    val desireBonus: Int = 0,
    val obedienceBonus: Int = 0,
    val trustBonus: Int = 0,
    val resourceBonus: Map<String, Int> = emptyMap(), // "wood", "stone", "mana", "gold", "sexEnergy"
    val outcomeSummary: String
) : Serializable

data class ProceduralEventScenario(
    val id: String = UUID.randomUUID().toString(),
    val characterId: String,
    val characterName: String,
    val characterArchetype: String,
    val affinityTierLevel: Int,
    val affinityTierTitle: String,
    val locationId: String,
    val locationName: String,
    val locationIcon: String,
    val eventTitle: String,
    val sceneSettingDescription: String,
    val characterInitialMonologue: String,
    val choices: List<ProceduralChoice>,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

data class ProceduralOutcomeResult(
    val selectedChoice: ProceduralChoice,
    val characterName: String,
    val monologueResponse: String,
    val affinityGained: Int,
    val statSummaryText: String,
    val resourceRewardSummary: String
) : Serializable

object ProceduralHaremEventEngine {

    fun generateProceduralEvent(
        character: Character,
        locationId: String,
        player: Player
    ): ProceduralEventScenario {
        val tier = AffinityData.getTierForPoints(character.affinityPoints)
        val archetype = character.archetypeId
        val locationName = when (locationId) {
            "temny_hvozd" -> "🌲 Mlžný Temný hvozd"
            "hostinec_u_krvave_panny" -> "🍷 Hostinec U Krvavé Panny"
            "ruiny_chramu" -> "🏛️ Ruiny chrámu Luny"
            "krvave_katakomby" -> "🩸 Krvavé katakomby"
            "astralni_citadela" -> "✨ Astrální citadela"
            "komnaty_haremu" -> "🕯️ Soukromé komnaty harému"
            "lazne_rozkose" -> "🛁 Horké lázně rozkoše"
            "alchymisticka_laborator" -> "⚗️ Alchymistické zahrady a laboratoř"
            "kamenolom" -> "🪨 Kamenolom a hradby dominia"
            "pila" -> "🪵 Dřevorubecký tábor a lesní pila"
            else -> "👑 Trůnní sál dominia"
        }
        val locationIcon = when {
            locationId.contains("hvozd") || locationId.contains("pila") -> "🌲"
            locationId.contains("hostinec") -> "🍷"
            locationId.contains("chram") || locationId.contains("astral") -> "✨"
            locationId.contains("katakomb") -> "🩸"
            locationId.contains("lazne") -> "🛁"
            locationId.contains("alchym") -> "⚗️"
            locationId.contains("kamen") -> "🪨"
            else -> "👑"
        }

        val scenarioContext = buildContextualSituation(character, tier.level, locationId)
        val choices = buildBranchingChoices(character, tier.level, locationId)

        return ProceduralEventScenario(
            characterId = character.id,
            characterName = character.name,
            characterArchetype = character.archetypeId,
            affinityTierLevel = tier.level,
            affinityTierTitle = tier.title,
            locationId = locationId,
            locationName = locationName,
            locationIcon = locationIcon,
            eventTitle = scenarioContext.first,
            sceneSettingDescription = scenarioContext.second,
            characterInitialMonologue = scenarioContext.third,
            choices = choices
        )
    }

    private fun buildContextualSituation(
        character: Character,
        tierLevel: Int,
        locationId: String
    ): Triple<String, String, String> {
        val name = character.name

        return when {
            locationId.contains("lazne") -> {
                val title = if (tierLevel >= 4) "🛁 Horká lázeň sdílené extáze" else "🛁 Očistná vodní lázeň"
                val setting = "V parou zahalené mramorové lázni stoupá vůně jasmínových a nočních olejů. $name sedí na okraji bazénu, její mokrá pokožka se leskne ve svitu pochodní a kapky vody stékají po jejích křivkách."
                val monologue = when (tierLevel) {
                    1, 2 -> "„Můj pane... voda je příjemně horká. Pokud si přeješ, umyji ti záda a budu ti sloužit, jak přikážeš.“"
                    3, 4 -> "„Čekala jsem, až za mnou přijdeš... voda je horká, ale mé tělo hoří ještě víc, když tě vidím vstupovat.“"
                    else -> "„Pojď ke mně do vody, můj věčný vládce. Nech mě hýčkat tvé tělo a splynout s tebou v lázni rozkoše.“"
                }
                Triple(title, setting, monologue)
            }
            locationId.contains("alchym") -> {
                val title = if (tierLevel >= 4) "⚗️ Mystická rezonance esencí" else "⚗️ Noční příprava elixírů"
                val setting = "Mezi skleněnými baňkami a fialově dýmajícími káděmi připravuje $name vzácné extrakty z bylin a many. Vzduch je nasycený omamnou sladkou vůní a magickou statickou elektřinou."
                val monologue = when (tierLevel) {
                    1, 2 -> "„Dokončuji várku hojivých silic a many pro pevnost. Prosím, dohlédni na mé dávkování, pane.“"
                    3, 4 -> "„Přidala jsem do lektvaru pár kapek své vlastní touhy... ochutnej se mnou, jak sladce pálí na rtech.“"
                    else -> "„Tato esence spojuje naši životní sílu. Společně ovládáme zákony alchymie i samotného osudu.“"
                }
                Triple(title, setting, monologue)
            }
            locationId.contains("pila") || locationId.contains("hvozd") -> {
                val title = if (tierLevel >= 4) "🪵 Šepot stromů pod ochranou pána" else "🪵 Těžba dřeva a stíny lesa"
                val setting = "V hloubi lesa se ozývá rytmický zvuk sekání a padajících kmenů černého dřeva. $name utírá pot ze svého čela a s obdivem sleduje, jak z divočiny vyrůstá tvé dominium."
                val monologue = when (tierLevel) {
                    1, 2 -> "„Les je plný divokých šelem, ale když jsi nablízku, vím, že mě žádné nebezpečí neohrozí.“"
                    3, 4 -> "„Tvrdá práce mi rozbušila krev v žilách... cítím tvůj pohled na svém těle a chci ti dokázat svou sílu.“"
                    else -> "„Každý kmen, který zde pokácíme, poslouží k vybudování naší věčné říše stínů.“"
                }
                Triple(title, setting, monologue)
            }
            locationId.contains("kamen") || locationId.contains("katakomb") -> {
                val title = if (tierLevel >= 4) "🪨 Hradby krve a železa" else "🪨 Dozor v kamenolomu a kobkách"
                val setting = "U masivních žulových bloků a temných katakomb dohlíží $name na opevnění dominia. Pochodně vrhají dlouhé stíny na její postavu a chladný kámen rezonuje její oddaností."
                val monologue = when (tierLevel) {
                    1, 2 -> "„Kameny jsou těžké, ale tvá autorita drží všechny v naprosté kázni. Jsem připravena plnit další příkazy.“"
                    3, 4 -> "„Tento kámen odolá jakémukoliv obléhání. Stejně jako mé pouto k tobě, které žádná síla nezlomí.“"
                    else -> "„Na těchto základech vystavíme trůn, před kterým poklekne celý známý svět.“"
                }
                Triple(title, setting, monologue)
            }
            else -> {
                val title = if (tierLevel >= 4) "👑 Audience oddanosti v komnatách" else "🕯️ Noční setkání v sídle"
                val setting = "V tichu soukromých komnat plápolá krb a vrhá zlatavé odlesky na hedvábné polštáře. $name pokleká před tvým křeslem a čeká na tvé slovo."
                val monologue = when (tierLevel) {
                    1, 2 -> "„Přišla jsem, jak jsi poručil, můj pane. Tvá přítomnost ve mně vyvolává bázeň i touhu po tvém uznání.“"
                    3, 4 -> "„Každá chvíle bez tebe je pro mě prázdná. Řekni mi, co si žádáš, a já ti to s radostí splním.“"
                    else -> "„Můj králi, mé srdce i tělo jsou navždy tvým domovem. Spolu vládneme tomuto světu.“"
                }
                Triple(title, setting, monologue)
            }
        }
    }

    private fun buildBranchingChoices(
        character: Character,
        tierLevel: Int,
        locationId: String
    ): List<ProceduralChoice> {
        val name = character.name

        val choiceDominant = ProceduralChoice(
            id = "choice_dominant",
            title = "👑 Pevný příkaz a upevnění autority",
            description = "Předveď svou nekompromisní nadvládu a vyžaduj bezpodmínečnou poslušnost.",
            approach = "DOMINANT",
            icon = "👑",
            reactionMonologue = "„Ano, můj pane... tvá vůle je mým zákonem. Ráda se skláním před tvou silou.“",
            affinityBonus = 12 + tierLevel * 3,
            loyaltyBonus = 15,
            obedienceBonus = 14,
            resourceBonus = mapOf("wood" to 25, "stone" to 20, "gold" to 30),
            outcomeSummary = "+${12 + tierLevel * 3} Náklonnost, +15 Loajalita, +14 Poslušnost, +25🪵 Dřevo, +20🪨 Kámen"
        )

        val choiceRomantic = ProceduralChoice(
            id = "choice_romantic",
            title = "💖 Něžné pohlazení a slova důvěry",
            description = "Obejmi ji, pochval její oddanost a projev upřímnou něhu vládce.",
            approach = "ROMANTIC",
            icon = "💖",
            reactionMonologue = "„Tvé doteky mě hřejí u srdce... nikdy jsem nevěřila, že v temném pánovi najdu takovou něhu.“",
            affinityBonus = 20 + tierLevel * 4,
            loyaltyBonus = 12,
            trustBonus = 18,
            resourceBonus = mapOf("gold" to 50, "mana" to 15),
            outcomeSummary = "+${20 + tierLevel * 4} Náklonnost, +18 Důvěra, +12 Loajalita, +50💰 Zlato, +15🔮 Mana"
        )

        val choicePassionate = ProceduralChoice(
            id = "choice_passionate",
            title = "🔥 Vášnivé objetí a tělesná rozkoš",
            description = "Strhni ji k sobě a dopřejte si intenzivní okamžik nenasytné touhy.",
            approach = "PASSIONATE",
            icon = "🔥",
            reactionMonologue = "„Ach... tvůj žár mě spaluje! Vezmi si mě celou, patřím jen tobě!“",
            affinityBonus = 16 + tierLevel * 3,
            desireBonus = 22,
            resourceBonus = mapOf("sexEnergy" to 25, "mana" to 10),
            outcomeSummary = "+${16 + tierLevel * 3} Náklonnost, +22 Touha, +25⚡ Sexuální energie, +10🔮 Mana"
        )

        val choiceStrategic = ProceduralChoice(
            id = "choice_strategic",
            title = "⚗️ Společné plánování a rozvoj zdrojů",
            description = "Zapoj ji do správy dominia, těžby surovin a magických rituálů.",
            approach = "STRATEGIC",
            icon = "⚗️",
            reactionMonologue = "„Je mi ctí podílet se na budování tvé říše. Naše dominium bude vzkvétat!“",
            affinityBonus = 14 + tierLevel * 2,
            loyaltyBonus = 10,
            trustBonus = 12,
            resourceBonus = mapOf("wood" to 40, "stone" to 35, "mana" to 20, "gold" to 60),
            outcomeSummary = "+${14 + tierLevel * 2} Náklonnost, +40🪵 Dřevo, +35🪨 Kámen, +20🔮 Mana, +60💰 Zlato"
        )

        return listOf(choiceDominant, choiceRomantic, choicePassionate, choiceStrategic)
    }

    fun applyChoiceOutcome(
        scenario: ProceduralEventScenario,
        choice: ProceduralChoice,
        character: Character,
        player: Player
    ): ProceduralOutcomeResult {
        // Apply affinity & character stats
        character.affinityPoints = (character.affinityPoints + choice.affinityBonus).coerceAtMost(100)
        character.loajalita = (character.loajalita + choice.loyaltyBonus).coerceAtMost(100)
        character.poslusnost = (character.poslusnost + choice.obedienceBonus).coerceAtMost(100)
        character.duvera = (character.duvera + choice.trustBonus).coerceAtMost(100)
        character.touha = (character.touha + choice.desireBonus).coerceAtMost(100)
        character.srdce = (character.srdce + choice.affinityBonus / 2).coerceAtMost(100)

        // Apply resources to player
        choice.resourceBonus["wood"]?.let { player.wood += it }
        choice.resourceBonus["stone"]?.let { player.stone += it }
        choice.resourceBonus["mana"]?.let { player.mana = (player.mana + it).coerceAtMost(player.maxMana) }
        choice.resourceBonus["gold"]?.let { player.gold += it }
        choice.resourceBonus["sexEnergy"]?.let { player.sexEnergy = (player.sexEnergy + it).coerceAtMost(player.maxSexEnergy) }

        val resSummary = choice.resourceBonus.entries.joinToString(", ") { (k, v) ->
            when (k) {
                "wood" -> "+$v🪵"
                "stone" -> "+$v🪨"
                "mana" -> "+$v🔮"
                "gold" -> "+$v💰"
                "sexEnergy" -> "+$v⚡"
                else -> "+$v $k"
            }
        }

        return ProceduralOutcomeResult(
            selectedChoice = choice,
            characterName = character.name,
            monologueResponse = choice.reactionMonologue,
            affinityGained = choice.affinityBonus,
            statSummaryText = choice.outcomeSummary,
            resourceRewardSummary = if (resSummary.isNotEmpty()) "Získané suroviny: $resSummary" else ""
        )
    }
}
