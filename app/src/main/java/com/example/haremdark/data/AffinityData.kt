package com.example.haremdark.data

import com.example.haremdark.models.Concubine

data class AffinityTierInfo(
    val level: Int,
    val title: String,
    val subtitle: String,
    val minPoints: Int,
    val maxPoints: Int,
    val perkDescription: String,
    val icon: String,
    val colorHex: Long = 0xFFE91E63,
    val unlockedPerks: List<String> = listOf(perkDescription)
)

object AffinityData {

    val TIERS = listOf(
        AffinityTierInfo(
            level = 1,
            title = "Zajatkyně & Cizinka",
            subtitle = "Odtažitá, nedůvěřivá a sledující každý tvůj krok.",
            minPoints = 0,
            maxPoints = 30,
            perkDescription = "Základní poslušnost ze strachu.",
            icon = "⛓️",
            colorHex = 0xFF9E9E9E,
            unlockedPerks = listOf("Základní poslušnost ze strachu", "Možnost dávat dary a mluvit v komnatách")
        ),
        AffinityTierInfo(
            level = 2,
            title = "Pokorná služebná",
            subtitle = "Učí se plnit pánovy rozkazy a přijímá své postavení.",
            minPoints = 31,
            maxPoints = 70,
            perkDescription = "+10% zisk zlata ze správy komnat.",
            icon = "🗝️",
            colorHex = 0xFF4CAF50,
            unlockedPerks = listOf("+10% zisk zlata ze správy komnat", "Odemčena nová pasivní vyznání a myšlenky")
        ),
        AffinityTierInfo(
            level = 3,
            title = "Důvěrnice komnat",
            subtitle = "Začíná vnímat tvou přízeň a svěřuje ti své tajné touhy.",
            minPoints = 71,
            maxPoints = 120,
            perkDescription = "+15% efektivita intimních rituálů.",
            icon = "💎",
            colorHex = 0xFF00BCD4,
            unlockedPerks = listOf("+15% efektivita intimních rituálů", "+10% zisk temné energie při rozkoši")
        ),
        AffinityTierInfo(
            level = 4,
            title = "Oddaná milenka",
            subtitle = "Planoucí vášeň a touha trávit každou noc v tvém objetí.",
            minPoints = 121,
            maxPoints = 180,
            perkDescription = "+20% šance na zplození dědice a +10 SE.",
            icon = "🔥",
            colorHex = 0xFFFF4081,
            unlockedPerks = listOf("+20% šance na zplození dědice", "+10 max Sexuální energie v harému", "Svěřování nejhlubších tajemství")
        ),
        AffinityTierInfo(
            level = 5,
            title = "Spřízněná duše",
            subtitle = "Nerozlučné pouto duší. Žije a dýchá jen pro tvé dominium.",
            minPoints = 181,
            maxPoints = 250,
            perkDescription = "+25% obrana a útok pána v soubojích.",
            icon = "💖",
            colorHex = 0xFFE040FB,
            unlockedPerks = listOf("+25% bonus k útoku v soubojích", "+30% loajalita a imunita vůči vzpourám")
        ),
        AffinityTierInfo(
            level = 6,
            title = "Věčná královna",
            subtitle = "Absolutní splynutí mysli a srdce. Pánova pravá vládkyně.",
            minPoints = 251,
            maxPoints = 9999,
            perkDescription = "+50% pasivní příjem a nezlomná věrnost.",
            icon = "👑",
            colorHex = 0xFFFFD700,
            unlockedPerks = listOf("+50% celkový příjem dominia", "Absolutní nesmrtelná oddanost", "Titul Věčná královna harému")
        )
    )

    val PASSIVE_DIALOGUES: Map<Int, List<String>> = mapOf(
        1 to listOf(
            "„Prosím, pane... neubližuj mi, udělám vše, co mi přikážeš.“",
            "„Když se na mě díváš, mé tělo se bezděčně chvěje bázní.“"
        ),
        2 to listOf(
            "„Tvé doteky už nejsou tak děsivé... zvykám si na tvou pevnou ruku.“",
            "„Připravila jsem tvé lože přesně tak, jak to máš rád, můj pane.“"
        ),
        3 to listOf(
            "„Cítím se bezpečně jen tehdy, když mě držíš pevně v náručí.“",
            "„Tvé dary mě hřejí u srdce. Nikdo se o mě nikdy takto nestaral.“"
        ),
        4 to listOf(
            "„Má kůže prahne po tvých polibcích. Jsem jen tvá, celým svým tělem.“",
            "„Když nejsi v komnatách, počítám každou minutu do tvého návratu.“"
        ),
        5 to listOf(
            "„Jsi můj bůh a můj jediný ochránce. Má duše navždy patří tvému jménu.“",
            "„I kdyby se proti tobě postavil celý svět, já budu klečet po tvém boku.“"
        ),
        6 to listOf(
            "„Navždy tvá královna a nejposlušnější otrokyně v jednom. Vládni mi navěky.“",
            "„Tvé dominium je naším společným královstvím rozkoše a moci.“"
        )
    )

    fun getTierForPoints(points: Int): AffinityTierInfo {
        return TIERS.lastOrNull { points >= it.minPoints } ?: TIERS.first()
    }

    fun getLevelForPoints(points: Int): Int {
        return getTierForPoints(points).level
    }

    fun getProgressInTier(points: Int): Pair<Int, Int> {
        val tier = getTierForPoints(points)
        val currentInTier = (points - tier.minPoints).coerceAtLeast(0)
        val tierSpan = if (tier.level == 6) 100 else (tier.maxPoints - tier.minPoints)
        return Pair(currentInTier, tierSpan)
    }

    fun getPassiveDialogues(concubine: Concubine): List<String> {
        val tier = getLevelForPoints(concubine.affinityPoints)
        val archetype = concubine.archetypeId

        val baseDialogues = when (archetype) {
            "subka" -> when (tier) {
                1 -> listOf(
                    "„Prosím, pane... neubližuj mi, udělám vše, co mi přikážeš.“",
                    "„Když se na mě díváš, mé tělo se bezděčně chvěje bázní.“"
                )
                2 -> listOf(
                    "„Tvé doteky už nejsou tak děsivé... zvykám si na tvou pevnou ruku.“",
                    "„Připravila jsem tvé lože přesně tak, jak to máš rád, můj pane.“"
                )
                3 -> listOf(
                    "„Cítím se bezpečně jen tehdy, když mě držíš pevně v náručí.“",
                    "„Tvé dary mě hřejí u srdce. Nikdo se o mě nikdy takto nestaral.“"
                )
                4 -> listOf(
                    "„Má kůže prahne po tvých polibcích. Jsem jen tvá, celým svým tělem.“",
                    "„Když nejsi v komnatách, počítám každou minutu do tvého návratu.“"
                )
                5 -> listOf(
                    "„Jsi můj bůh a můj jediný ochránce. Má duše navždy patří tvému jménu.“",
                    "„I kdyby se proti tobě postavil celý svět, já budu klečet po tvém boku.“"
                )
                else -> listOf(
                    "„Navždy tvá královna a nejposlušnější otrokyně v jednom. Vládni mi navěky.“",
                    "„Tvé dominium je naším společným královstvím rozkoše a moci.“"
                )
            }

            "slechticna" -> when (tier) {
                1 -> listOf(
                    "„Myslíš si, že pouta a zlatý obojek zlomí mou urozenou krev? Pche...“",
                    "„Můj otec by tě za toto ponížení nechal předhodit divé zvěři.“"
                )
                2 -> listOf(
                    "„Uznávám, že tvá pevnost má jistou temnou noblesu. Ale sluha ze mě nebude.“",
                    "„Pokud mi nabízíš dary, měly by být hodné dámy mého původu.“"
                )
                3 -> listOf(
                    "„Možná jsem tě zpočátku podcenila... tvá dominance má v sobě podmanivou sílu.“",
                    "„Moji dvořané byli zbabělci. Ty jsi skutečný vládce, ač temný a nemilosrdný.“"
                )
                4 -> listOf(
                    "„Moje hrdost se před tebou rozpadá na prach... a poprvé v životě mi to nevadí.“",
                    "„Vezmi si mě dnes v noci bez ohledů na můj původ. Chci cítit tvůj nárok.“"
                )
                5 -> listOf(
                    "„Moje koruna patřila jinému světu, ale mé srdce navždy náleží tvému trůnu.“",
                    "„Společně podrobíme všechny šlechtické rody, které se ti opováží vzdorovat.“"
                )
                else -> listOf(
                    "„Královna po boku temného pána. Nikdo na tomto světě se nám nevyrovná.“",
                    "„Zlaté sály tvého dominia září mou oddaností a tvou nesmrtelnou mocí.“"
                )
            }

            "touha" -> when (tier) {
                1 -> listOf(
                    "„Cítím tvou temnou magii... ale nevěřím, že mě dokážeš skutečně uspokojit.“",
                    "„Zajetí je jen hra. Uvidíme, kdo z nás dvou bude nakonec ovládat koho.“"
                )
                2 -> listOf(
                    "„Tvůj dotek má v sobě podivný žár. Mé tělo na tebe začíná reagovat samo od sebe.“",
                    "„Elixíry, které pro mě vaříš, mi rozproudily krev v žilách... pokračuj.“"
                )
                3 -> listOf(
                    "„Každý pohled tvých očí mě spaluje. Už nemohu myslet na nic jiného než na tvou náruč.“",
                    "„Nauč mě tvá temná kouzla. Propojíme naše energie v rituálu slasti.“"
                )
                4 -> listOf(
                    "„Spal mě svou vášní, můj pane! Má touha je bezedná a patří jen tobě.“",
                    "„Když se ve mně probudí žár, jedině tvé tělo dokáže utišit mou žízeň.“"
                )
                5 -> listOf(
                    "„Naše magie a naše těla jsou jedno. Vládneme temnotě a neuhasitelné rozkoši.“",
                    "„Zplodíme dědice, jehož žilami poteče čistá esence temného plamene.“"
                )
                else -> listOf(
                    "„Královna nenasytné vášně a temného ohně. Společně spálíme celý svět.“",
                    "„V mé náruči nikdy nepocítíš únavu ani chlad, můj věčný pane.“"
                )
            }

            "odvazna" -> when (tier) {
                1 -> listOf(
                    "„Sundej mi ty okovy a postav se mi se zbraní v ruce, jestli máš odvahu!“",
                    "„Bojovnice se nevzdává. Čekám jen na okamžik tvé nepozornosti.“"
                )
                2 -> listOf(
                    "„Bojuješ lépe, než jsem čekala. Respektuji sílu, ale podřízená ti nebudu.“",
                    "„Tvé dary... oceňuji kvalitní ocel a víno víc než šperky.“"
                )
                3 -> listOf(
                    "„Zvláštní... vedle tebe cítím sílu, jakou jsem u žádného jiného muže nezažila.“",
                    "„V aréně tě budu krýt. Moje čepel patří tvému dominium.“"
                )
                4 -> listOf(
                    "„Jsi jediný muž, před kterým jsem ochotná sklonit hlavu a odložit zbroj.“",
                    "„Boj na bojišti i v tvé ložnici... obojí mi rozbuší srdce jako nic jiného.“"
                )
                5 -> listOf(
                    "„Položím za tebe život bez zaváhání. Moje tělo i můj meč jsou tvým štítem.“",
                    "„Tvá krev a má krev jsou spojeny v nezlomném válečném poutu.“"
                )
                else -> listOf(
                    "„Válečná královna dominia. Budeme bok po boku drtit všechny nepřátele.“",
                    "„Žádná armáda nezastaví naši společnou vůli a sílu.“"
                )
            }

            else -> when (tier) {
                1 -> listOf(
                    "„Nevím, co se mnou zamýšlíš... ale budu tě tiše pozorovat.“",
                    "„Jsem tvou zajatkyní. Má slova jsou v této chvíli zbytečná.“"
                )
                2 -> listOf(
                    "„Tvá péče o mě předčila mé obavy. Děkuji ti za milosrdenství.“",
                    "„Přijímám své místo v tvém harému a budu ti věrně sloužit.“"
                )
                3 -> listOf(
                    "„Cítím, jak se led kolem mého srdce pod tvou péčí pomalu rozpouští.“",
                    "„Dnes v noci bych chtěla být po tvém boku déle než obvykle.“"
                )
                4 -> listOf(
                    "„Má oddanost patří jen tobě. V tvém objetí jsem našla svůj nový domov.“",
                    "„Nikdy mě neopouštěj... bez tebe by můj život ztratil veškerý smysl.“"
                )
                5 -> listOf(
                    "„Jsi mé světlo v temnotě dominia. Má láska k tobě překonává všechny hranice.“",
                    "„Má duše i tělo jsou tvé na věky věků.“"
                )
                else -> listOf(
                    "„Společně vládneme temnému dominium jako nerozluční pán a paní.“",
                    "„Naše pouto je věčné a nepřemožitelné žádnou silou na tomto světě.“"
                )
            }
        }

        return baseDialogues
    }

    fun getRandomActiveDialogue(concubine: Concubine): String {
        val list = getPassiveDialogues(concubine)
        return list.randomOrNull() ?: "„Můj pane, má oddanost patří jen tobě.“"
    }

    fun getRandomActiveDialogue(affinityPoints: Int, archetypeId: String = "subka"): String {
        val tier = getLevelForPoints(affinityPoints)
        val lines = PASSIVE_DIALOGUES[tier] ?: listOf("„Můj pane, má oddanost patří jen tobě.“")
        return lines.randomOrNull() ?: "„Můj pane, má oddanost patří jen tobě.“"
    }
}
