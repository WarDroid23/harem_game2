package com.example.haremdark.data

import com.example.haremdark.models.Character

data class AffinityTierInfo(
    val level: Int,
    val title: String,
    val stageName: String = "Acquaintance",
    val subtitle: String,
    val minPoints: Int,
    val maxPoints: Int,
    val perkDescription: String,
    val combatBonusDescription: String = "Základní bojové zapojení bez pasivních bonusů",
    val hpBonus: Int = 0,
    val attackBonusPercent: Int = 0,
    val defenseBonus: Int = 0,
    val critBonusPercent: Int = 0,
    val hpRegenBonus: Int = 0,
    val darkEnergyBonus: Int = 0,
    val icon: String,
    val colorHex: Long = 0xFFE91E63,
    val unlockedPerks: List<String> = listOf(perkDescription),
    val cosmeticReward: String? = null
)

data class CharacterSpecificBuff(
    val id: String,
    val archetypeId: String,
    val name: String,
    val icon: String,
    val tierRequired: Int,
    val description: String,
    val perkEffectSummary: String,
    val hpBonus: Int = 0,
    val attackBonusPercent: Float = 0f,
    val defenseBonus: Int = 0,
    val critBonusPercent: Int = 0,
    val hpRegenPerTurn: Int = 0,
    val darkEnergyRegenPerTurn: Int = 0,
    val specialEffectTag: String = "" // "DAMAGE_REDUCTION", "REGEN", "WARD", "PARTY_WARD", "INVULNERABLE_BARRIER", "ARMOR_PIERCE", "EXECUTE", "ENEMY_WEAKEN", "SUPREME_COMMAND", "COUNTER_ATTACK", "BLEED", "DOUBLE_STRIKE", "STUN_OPENER", "LIFESTEAL", "BLIND", "SIPHON_AURA", "DOMINATION_AURA", "BURN", "HELL_STORM", "HELL_EXECUTE", "ELEMENT_RESIST", "MELT_ARMOR", "RETALIATION_BURST", "METEOR_APOCALYPSE"
)

data class AffinityDialogueEntry(
    val tier: Int,
    val tierTitle: String,
    val category: String, // "Pozdrav", "Bojový pokřik", "Intimní vyznání", "Pouto duší"
    val text: String,
    val isUnlocked: Boolean
)

object AffinityData {

    val TIERS = listOf(
        AffinityTierInfo(
            level = 1,
            title = "Zajatkyně & Cizinka",
            stageName = "Acquaintance (Cizinka)",
            subtitle = "Odtažitá, nedůvěřivá a sledující každý tvůj krok.",
            minPoints = 0,
            maxPoints = 30,
            perkDescription = "Základní poslušnost ze strachu.",
            combatBonusDescription = "⚔️ Žádný bojový bonus (nedůvěra)",
            hpBonus = 0,
            attackBonusPercent = 0,
            defenseBonus = 0,
            critBonusPercent = 0,
            hpRegenBonus = 0,
            darkEnergyBonus = 0,
            icon = "⛓️",
            colorHex = 0xFF9E9E9E,
            unlockedPerks = listOf("Základní poslušnost ze strachu", "Možnost dávat dary a mluvit v komnatách")
        ),
        AffinityTierInfo(
            level = 2,
            title = "Pokorná služebná",
            stageName = "Friend / Companion (Služebná)",
            subtitle = "Učí se plnit pánovy rozkazy a přijímá své postavení.",
            minPoints = 31,
            maxPoints = 70,
            perkDescription = "+10% zisk zlata ze správy komnat.",
            combatBonusDescription = "🛡️ Pasivní boj: +10 Max HP, +4% Útok & +2 Obrana",
            hpBonus = 10,
            attackBonusPercent = 4,
            defenseBonus = 2,
            critBonusPercent = 2,
            hpRegenBonus = 0,
            darkEnergyBonus = 0,
            icon = "🗝️",
            colorHex = 0xFF4CAF50,
            unlockedPerks = listOf("+10% zisk zlata ze správy komnat", "Odemčena nová pasivní vyznání a myšlenky"),
            cosmeticReward = "🎀 Stužka poslušnosti"
        ),
        AffinityTierInfo(
            level = 3,
            title = "Důvěrnice komnat",
            stageName = "Confidant (Důvěrnice)",
            subtitle = "Začíná vnímat tvou přízeň a svěřuje ti své tajné touhy.",
            minPoints = 71,
            maxPoints = 120,
            perkDescription = "+15% efektivita intimních rituálů.",
            combatBonusDescription = "⚡ Pasivní boj: +25 Max HP, +8% Útok, +4 Obrana & +10% Crit",
            hpBonus = 25,
            attackBonusPercent = 8,
            defenseBonus = 4,
            critBonusPercent = 10,
            hpRegenBonus = 0,
            darkEnergyBonus = 0,
            icon = "💎",
            colorHex = 0xFF00BCD4,
            unlockedPerks = listOf("+15% efektivita intimních rituálů", "+10% zisk temné energie při rozkoši"),
            cosmeticReward = "💍 Ocelový prsten oddanosti"
        ),
        AffinityTierInfo(
            level = 4,
            title = "Oddaná milenka",
            stageName = "Devoted Partner (Milenka)",
            subtitle = "Planoucí vášeň a touha trávit každou noc v tvém objetí.",
            minPoints = 121,
            maxPoints = 180,
            perkDescription = "+20% šance na zplození dědice a +10 SE.",
            combatBonusDescription = "💖 Pasivní boj: +40 Max HP, +15% Útok, +6 Obrana & +3 HP/kolo",
            hpBonus = 40,
            attackBonusPercent = 15,
            defenseBonus = 6,
            critBonusPercent = 6,
            hpRegenBonus = 3,
            darkEnergyBonus = 0,
            icon = "🔥",
            colorHex = 0xFFFF4081,
            unlockedPerks = listOf("+20% šance na zplození dědice", "+10 max Sexuální energie v harému", "Svěřování nejhlubších tajemství"),
            cosmeticReward = "👑 Hedvábný závoj milenky"
        ),
        AffinityTierInfo(
            level = 5,
            title = "Spřízněná duše",
            stageName = "Soulmate (Spřízněná duše)",
            subtitle = "Nerozlučné pouto duší. Žije a dýchá jen pro tvé dominium.",
            minPoints = 181,
            maxPoints = 250,
            perkDescription = "+25% obrana a útok pána v soubojích.",
            combatBonusDescription = "🔥 Pasivní boj: +60 Max HP, +25% Útok, +10 Obrana, +10% Crit & +5 HP/kolo",
            hpBonus = 60,
            attackBonusPercent = 25,
            defenseBonus = 10,
            critBonusPercent = 10,
            hpRegenBonus = 5,
            darkEnergyBonus = 0,
            icon = "💖",
            colorHex = 0xFFE040FB,
            unlockedPerks = listOf("+25% bonus k útoku v soubojích", "+30% loajalita a imunita vůči vzpourám"),
            cosmeticReward = "✨ Očarovaný náhrdelník duší"
        ),
        AffinityTierInfo(
            level = 6,
            title = "Věčná královna",
            stageName = "Eternal Sovereign (Královna)",
            subtitle = "Absolutní splynutí mysli a srdce. Pánova pravá vládkyně.",
            minPoints = 251,
            maxPoints = 9999,
            perkDescription = "+50% pasivní příjem a nezlomná věrnost.",
            combatBonusDescription = "👑 Pasivní boj: +100 Max HP, +40% Útok, +15 Obrana, +15% Crit, +8 HP/kolo & +5 TE/kolo",
            hpBonus = 100,
            attackBonusPercent = 40,
            defenseBonus = 15,
            critBonusPercent = 15,
            hpRegenBonus = 8,
            darkEnergyBonus = 5,
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

    fun getDialoguesForTier(tier: Int, archetype: String): List<String> = getDialoguesForTier(archetype, tier)

    fun getDialoguesForTier(archetype: String, tier: Int): List<String> {
        return when (archetype) {
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

            "sukuba" -> when (tier) {
                1 -> listOf(
                    "„Och... stíny tvého dominia mě chytily. Chceš mou magii, nebo mé tělo, drahý pane?“",
                    "„Myslíš si, že mě ovládneš? Já krmím touhy, neposlouchám obyčejné smrtelníky.“"
                )
                2 -> listOf(
                    "„Tvá energie je nečekaně lahodná. Rituály s tebou mi dávají víc síly, než přiznám.“",
                    "„Začíná mě bavit hrát tvou zajatkyni. No tak, co ode mě dnes večer žádáš?“"
                )
                3 -> listOf(
                    "„Cítím, jak se naše duše prolínají v čistém plamenu touhy. Už nechci odletět zpět.“",
                    "„Dary od tebe mají chuť tvého triumfu nad mou démonickou pýchou. Miluji to.“"
                )
                4 -> listOf(
                    "„Jsem tvá sukuba, tvůj věčný hřích. Spal mé tělo svou nadvládou!“",
                    "„Ostatní v harému žárlí, když vidí, jak nenasytně pohlcuješ mou energii.“"
                )
                5 -> listOf(
                    "„Moje pekelná krev tepe v souladu s tvým srdcem. Jsi mým absolutním vládcem.“",
                    "„Společně si podmaníme každou duši v tomto ubohém světě. Budou škemrat o tvůj dotek.“"
                )
                else -> listOf(
                    "„Královna stínů a rozkoše. Tvůj trůn je mým oltářem a mé tělo tvou svatyní.“",
                    "„Věčně spojené duše v nenasytné extázi. Ty a já navždy vládci temna.“"
                )
            }

            "draci_divka" -> when (tier) {
                1 -> listOf(
                    "„Můj oheň spálí tvé řetězy, jakmile polevíš v ostražitosti. Nedotýkej se mého ocasu!“",
                    "„Jsem dračí krev, ne nějaká levná hračka pro tvůj harém.“"
                )
                2 -> listOf(
                    "„Respektuji tvou bojovnost. Tvé dominium má solidní obranu, ale já se podřídit nehodlám.“",
                    "„Tohle víno je dobré... skoro tak horké jako tekoucí láva mých domovských hor.“"
                )
                3 -> listOf(
                    "„Dračí srdce bije jen pro ty, kteří mají sílu ho zkrotit. Ty ji máš... začínám ti věřit.“",
                    "„Moje šupiny žhnou, když stojíš blízko. To není hněv... to je něco jiného.“"
                )
                4 -> listOf(
                    "„Vezmi si mou hrdost, můj pane. Chci cítit tvůj plamen, který je silnější než můj.“",
                    "„Budu tvou osobní stráží v každé bitvě. Moje dračí síla je tvým štítem.“"
                )
                5 -> listOf(
                    "„Moje krev, můj dech i můj oheň patří tobě. Jsem spřízněná s tvou temnou duší.“",
                    "„Naše spojenectví je věčné. Žádný drak ani člověk tě už nedokáže zranit, když stojím po tvém boku.“"
                )
                else -> listOf(
                    "„Dračí královna tvého harému. Naše křídla zastíní slunce nad celou touto zemí.“",
                    "„Tvá vůle je mým ohněm. Spálíme všechny, kteří ti odmítnou vzdát hold.“"
                )
            }

            "chladna" -> when (tier) {
                1 -> listOf(
                    "„Tvé dominium je chladné, ale mé srdce je ještě chladnější. Nic ze mě nedostaneš.“",
                    "„Pouta jsou jen fyzická záležitost. Má mysl zůstává nedotčena.“"
                )
                2 -> listOf(
                    "„Postřehla jsem tvou důslednost. Pokud dodržíš své sliby, budu respektovat tvůj řád.“",
                    "„Tento čaj a teplé přikrývky... ulevují od chladu. Děkuji.“"
                )
                3 -> listOf(
                    "„Led v mé hrudi pomalu taje pod tvým pohledem. Děsí mě to, ale neodvracím se.“",
                    "„Mé analytické schopnosti jsou ti k dispozici. Pomohu ti v plánování bitev.“"
                )
                4 -> listOf(
                    "„Dovolila jsem ti nahlédnout za mé hradby. Jsi jediný, kdo spatřil mou zranitelnost.“",
                    "„Mé tělo hoří mrazivou touhou jen pro tvé ruce. Zahřej mě, pane.“"
                )
                5 -> listOf(
                    "„Má oddanost je absolutní jako věčný mráz. Žádná pochybnost, žádné zaváhání.“",
                    "„Budu tvou chladnou myslí a neochvějnou rádkyní po celou věčnost.“"
                )
                else -> listOf(
                    "„Mrazivá královna po tvém boku. Naše vláda bude klidná, neúprosná a věčná.“",
                    "„Všechny nepřátele tvého dominia proměníme v krystaly ledu.“"
                )
            }

            "ustrasena" -> when (tier) {
                1 -> listOf(
                    "„P-prosím... neubližuj mi! Udělám cokoliv, jen na mě nekřič...“",
                    "„Schovávám se v koutě, když slyším tvé těžké kroky... bojím se.“"
                )
                2 -> listOf(
                    "„Dnes jsi byl na mě hodný... děkuji, pane. Možná tu nejsem v takovém nebezpečí.“",
                    "„Přinesla jsem ti vodu a vyčistila plášť. Dělám to správně?“"
                )
                3 -> listOf(
                    "„Když mě vezmeš do náruče, strach ze světa zmizí. Cítím se u tebe chráněná.“",
                    "„Děkuji ti za krásný dar... nikdo mi nikdy nic tak hezkého nedal.“"
                )
                4 -> listOf(
                    "„Už se tě nebojím, můj pane. Děsí mě jen představa, že bych tě ztratila.“",
                    "„Chci spát schoulená na tvé hrudi každou noc. Jsi můj štít.“"
                )
                5 -> listOf(
                    "„Moje srdce i život patří tobě. Pro tvůj úsměv překonám veškerý svůj strach.“",
                    "„Věřím ti bez výhrad. Kamkoliv mě povedeš, tam tě budu následovat.“"
                )
                else -> listOf(
                    "„Královna, kterou jsi zachránil před temnotou světa. Navždy tvá pokorná družka.“",
                    "„S tebou po boku se už nebojím ničeho, můj mocný ochránce a vládce.“"
                )
            }

            "vzdorna" -> when (tier) {
                1 -> listOf(
                    "„Myslíš si, že mě zlomíš? Raději zemřu, než abych ti líbala nohy!“",
                    "„Počkej, až se otočíš zády. Najdu způsob, jak tě zničit.“"
                )
                2 -> listOf(
                    "„Uznávám, že máš pevnou ruku. Ale respekt si musíš zasloužit, otrokáři.“",
                    "„Tvé dary mě neopijí rohlíkem. I když... to víno je překvapivě dobré.“"
                )
                3 -> listOf(
                    "„Bojovat proti tobě mě vyčerpává... možná proto, že hluboko uvnitř obdivuji tvou sílu.“",
                    "„Budeš-li mě potřebovat v boji, budu krýt tvůj bok. Ne proto, že musíš, ale proto, že chci.“"
                )
                4 -> listOf(
                    "„Zkrotil jsi mou divokost, ale neuhasil můj oheň. Nyní hoří jen a jen pro tebe.“",
                    "„Pokoř mě dnes v noci znovu. Miluji ten pocit, když mě tvá síla přemůže.“"
                )
                5 -> listOf(
                    "„Má hrdost už nebojuje proti tobě, ale bojuje ZA tebe. Jsem tvůj nejostřejší meč.“",
                    "„Nikomu nedovolím, aby tě zneuctil. Společně roztrháme každého rebela.“"
                )
                else -> listOf(
                    "„Rebelská královna tvého temného impéria. Společně svrhneme jakéhokoliv boha.“",
                    "„Podřídila jsem se jedinému muži v celém vesmíru – tobě.“"
                )
            }

            "zlomena" -> when (tier) {
                1 -> listOf(
                    "„...udělej se mnou, co chceš. Na mně už nezáleží.“",
                    "„Cítím jen prázdnotu. Žádná bolest mě už nemůže překvapit.“"
                )
                2 -> listOf(
                    "„Tvé doteky nejsou kruté... proč se ke mně chováš jinak než ostatní?“",
                    "„Děkuji za jídlo a šaty. Už jsem zapomněla, jaké to je mít střechu nad hlavou.“"
                )
                3 -> listOf(
                    "„Začínám znovu cítit tlukot svého srdce. Tvá přítomnost ve mně zažíhá novou jiskru.“",
                    "„Prosím, neopouštěj mě. Jsi jediný smysl, který v tomto prázdném světě mám.“"
                )
                4 -> listOf(
                    "„Vdechls mi nový život. Jsem tvým znovuzrozeným stvořením a patřím ti celou duší.“",
                    "„Každý tvůj polibek slepuje střepy mé rozbité minulosti.“"
                )
                5 -> listOf(
                    "„Z prachu jsi mě pozvedl k nebesům. Má oddanost nezná hranic ani konce.“",
                    "„Jsi můj spasitel a můj pán. Pro tvé blaho obětuji všechno.“"
                )
                else -> listOf(
                    "„Znovuzrozená královna ze stínů. Společně vládneme z trosek minulosti.“",
                    "„Má nová duše září tvým temným světlem, můj věčný vládce.“"
                )
            }

            "manipulativni" -> when (tier) {
                1 -> listOf(
                    "„Máš zajímavou moc, pane... uvidíme, který z nás bude tahat za nitky.“",
                    "„Úsměvy nic nestojí. Sleduj mé oči, pokud chceš vědět, co si myslím.“"
                )
                2 -> listOf(
                    "„Pochopil jsi mou hodnotu. Moje intriky ti mohou získat víc území než celá armáda.“",
                    "„Šperky máš vybrané se vkusem. Víš, jak si získat ženu mých kvalit.“"
                )
                3 -> listOf(
                    "„Odkládám masku... před tebou už nemusím předstírat. Vidíš mě takovou, jaká skutečně jsem.“",
                    "„Pojďme naplánovat pád tvých nepřátel společně. Mám pro tebe pár chutných informací.“"
                )
                4 -> listOf(
                    "„Chytila jsem se do vlastní sítě – propadla jsem tvému kouzlu. A vůbec se mi nechce ven.“",
                    "„Využiji každou lest tohoto světa, abych zajistila tvé absolutní vítězství.“"
                )
                5 -> listOf(
                    "„Mé intriky slouží výhradně tvé slávě. Dva geniální predátoři na jednom trůně.“",
                    "„Nikdo nás nepřelstí. Naše spojenectví je neporazitelné.“"
                )
                else -> listOf(
                    "„Císařovna intrik a stínů. Celý svět bude tancovat přesně tak, jak my dva zapískáme.“",
                    "„Vládnu po tvém boku a všechny nitky osudu vedou přímo do tvých rukou.“"
                )
            }

            "hysterialni" -> when (tier) {
                1 -> listOf(
                    "„Nenávidím tě! Ne, počkej... nedívej se tak na mě! Zblázním se z tebe!“",
                    "„Všechno se hroutí! Proč mě tu držíš?! Nech mě být... nebo ne, zůstaň!“"
                )
                2 -> listOf(
                    "„Tvá klidná síla mě uklidňuje... vedle tebe se mé bouře trochu utišují.“",
                    "„Omlouvám se za ten výbuch. Děkuji, že jsi mě nepotrestal příliš krutě.“"
                )
                3 -> listOf(
                    "„Miluji tě a zároveň se tě bojím! Ale bez tebe nemůžu dýchat ani vteřinu!“",
                    "„Obejmi mě pevně, ať vím, že se nerozpadnu na kusy!“"
                )
                4 -> listOf(
                    "„Má vášeň je šílená a spalující. Chci tě celého, tady a teď, bez zábran!“",
                    "„Když mě líbáš, chaos v mé hlavě utichne a vystřídá ho čistá extáze.“"
                )
                5 -> listOf(
                    "„Tvé objetí je mou jedinou kotvou v realitě. Jsem tvá oddaná bouře.“",
                    "„Všechny mé emoce, divoké i něžné, se slévají v jednu nekonečnou lásku k tobě.“"
                )
                else -> listOf(
                    "„Královna nespoutaného živlu. Naše vášeň zboří hranice tohoto světa.“",
                    "„Navždy spojená v extatickém šílenství na tvém trůnu rozkoše.“"
                )
            }

            "nymfomanka" -> when (tier) {
                1 -> listOf(
                    "„Tyto řetězy... mmm, kůže se mi chvěje vzrušením. Co se mnou uděláš?“",
                    "„Máš tvrdý pohled. Přistup blíž a ukaž mi, co skutečně dokážeš.“"
                )
                2 -> listOf(
                    "„Každá noc s tebou je lepší než ta předchozí. Mé tělo tě žádá znovu a znovu.“",
                    "„Parfémy a hedvábí... víš přesně, co rozpaluje mou krev.“"
                )
                3 -> listOf(
                    "„Už to není jen chtíč... začínám tě milovat celým svým nenasytným tělem.“",
                    "„Udělej si ze mě svou soukromou hračku kdykoliv si vzpomeneš.“"
                )
                4 -> listOf(
                    "„Mé lůno touží po tvém séměti, můj pane. Chci nosit tvého dědice!“",
                    "„Spal mě svou vášní! Nikdy nebudu mít dost tvých doteků!“"
                )
                5 -> listOf(
                    "„Absolutní extáze ve službách tvého dominia. Má duše je propojena s tvou rozkoší.“",
                    "„Každý můj vzdech a každý záchvěv těla je modlitbou k tvému jménu.“"
                )
                else -> listOf(
                    "„Královna věčného orgasmu a touhy. Naše lože je oltářem temné moci.“",
                    "„Vládnu s tebou v nekonečném kruhu slasti, dominance a plození.“"
                )
            }

            "ticha_panenka" -> when (tier) {
                1 -> listOf(
                    "„...“ (Tiše sklopí zrak a čeká na pokyn)",
                    "„...udělám, co si přeješ...“"
                )
                2 -> listOf(
                    "„...tvé ruce jsou teplé... děkuji...“",
                    "„...uklidila jsem komnaty... spokojený...?“"
                )
                3 -> listOf(
                    "„...když jsi blízko, cítím se celistvá...“",
                    "„...dar od tebe nosím u sebe... střežím ho jako poklad...“"
                )
                4 -> listOf(
                    "„...má duše se probudila k životu... pro tebe...“",
                    "„...chci slyšet tvůj hlas... říkej mi, že jsem jen tvá panenka...“"
                )
                5 -> listOf(
                    "„...mé srdce bije jen na tvůj povel... věčně oddaná...“",
                    "„...budu tvým tichým stínem, který nikdy nezradí...“"
                )
                else -> listOf(
                    "„...tichá královna tvého trůnu... dokonalá a navždy tvá...“",
                    "„...společně v tichu a moci vládneme temnému světu...“"
                )
            }

            "krvava_subka" -> when (tier) {
                1 -> listOf(
                    "„Biješ mě málo... chci cítit, že jsem skutečně tvým majetkem!“",
                    "„Krev a bolest... to je jediný jazyk, kterému mé tělo rozumí.“"
                )
                2 -> listOf(
                    "„Tvé tresty jsou sladší než med. Děkuji za každou jizvu, pane.“",
                    "„Když vidím tvůj hněv, mé tělo plane vzrušením.“"
                )
                3 -> listOf(
                    "„Už nechci trpět pro nikoho jiného. Jen tvá ruka má právo zraňovat mé tělo.“",
                    "„V aréně budu krvácet pro tvé vítězství s úsměvem na rtech.“"
                )
                4 -> listOf(
                    "„Tvé znamení je vypáleno do mé duše. Jsem tvá krvavá oběť i zbraň.“",
                    "„Vezmi si mě hrubě a bez slitování. Právě v tom nacházím svůj ráj.“"
                )
                5 -> listOf(
                    "„Má krev a tvá krev jsou navždy propojeny v temném rituálu oddanosti.“",
                    "„Položím život v mukách, bude-li to tvým přáním, můj milovaný tyrane.“"
                )
                else -> listOf(
                    "„Krvavá královna zkázy. Společně vykoupeme nepřátele v jejich vlastní krvi.“",
                    "„Trpět a vládnout po tvém boku po celou věčnost.“"
                )
            }

            "posedla" -> when (tier) {
                1 -> listOf(
                    "„Hlasy v mé hlavě křičí tvé jméno... co jsi se mnou provedl?!“",
                    "„Temnota uvnitř mě se natahuje k tvým stínům... bojím se toho spojení.“"
                )
                2 -> listOf(
                    "„Tvá přítomnost utišuje šepot démonů. Dáváš mému šílenství řád.“",
                    "„Energie v tvých komnatách sytí mou temnou entitu.“"
                )
                3 -> listOf(
                    "„Mé entity tě přijaly jako svého pána. Naše duše se začínají proplétat.“",
                    "„Dovol mi vypustit stíny na tvé nepřátele. Roztrhají je pro tvé potěšení.“"
                )
                4 -> listOf(
                    "„Už nejsem rozpolcená. Já i démon v mé hrudi milujeme jen tebe.“",
                    "„Propojme naše stíny v rituálu temného znovuzrození.“"
                )
                5 -> listOf(
                    "„Posedlá tvou láskou a tvou vůlí. Jsme tvou nejmocnější okultní zbraní.“",
                    "„Žádné kouzlo na světě nedokáže zlomit pouto, které nás spojuje.“"
                )
                else -> listOf(
                    "„Temná královna propasti. Naše společná moc zastíní i hvězdy na nebi.“",
                    "„Věčné panství stínů a démonické oddanosti po tvém boku.“"
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
    }

    val CHARACTER_SPECIFIC_BUFFS: Map<String, List<CharacterSpecificBuff>> = mapOf(
        "subka" to listOf(
            CharacterSpecificBuff("subka_1", "subka", "Bázlivá poslušnost", "⛓️", 1, "Snaží se nezavinit hněv pána.", "+5 Max HP", hpBonus = 5),
            CharacterSpecificBuff("subka_2", "subka", "Oddaná záštita", "🛡️", 2, "Kryje pána vlastním tělem a absorbuje zranění.", "+15 HP, +3 Obrana, -5% DMG pro pána", hpBonus = 15, defenseBonus = 3, specialEffectTag = "DAMAGE_REDUCTION"),
            CharacterSpecificBuff("subka_3", "subka", "Léčivá modlitba", "💖", 3, "Její oddaná péče hojí pánovy rány v boji.", "+35 HP, +5 Obrana, +4 HP/kolo", hpBonus = 35, defenseBonus = 5, hpRegenPerTurn = 4, specialEffectTag = "REGEN"),
            CharacterSpecificBuff("subka_4", "subka", "Sebeobětující štít", "✨", 4, "Vrhá se před smrtící útoky nepřátel.", "+60 HP, +8 Obrana, +6 HP/kolo, šance vykrýt krit", hpBonus = 60, defenseBonus = 8, hpRegenPerTurn = 6, specialEffectTag = "WARD"),
            CharacterSpecificBuff("subka_5", "subka", "Aura svatého pouta", "🌟", 5, "Nezlomné pouto poskytuje trvalou ochranu celé družině.", "+100 HP, +15 Obrana, +10 HP/kolo, +10% odolnost", hpBonus = 100, defenseBonus = 15, hpRegenPerTurn = 10, specialEffectTag = "PARTY_WARD"),
            CharacterSpecificBuff("subka_6", "subka", "Božská strážkyně věčnosti", "👑", 6, "Absolutní podvolení přeměněné v nezlomný štít dominia.", "+180 HP, +25 Obrana, +15 HP/kolo, bariéra 40 DMG", hpBonus = 180, defenseBonus = 25, hpRegenPerTurn = 15, specialEffectTag = "INVULNERABLE_BARRIER")
        ),
        "slechticna" to listOf(
            CharacterSpecificBuff("slech_1", "slechticna", "Urozený odstup", "📜", 1, "Sleduje bojiště s aristokratickou přísností.", "+2% Crit", critBonusPercent = 2),
            CharacterSpecificBuff("slech_2", "slechticna", "Taktické velení", "⚔️", 2, "Znalost taktiky zvyšuje údernost spojenců.", "+5% Crit, +6% Útok družiny", attackBonusPercent = 0.06f, critBonusPercent = 5),
            CharacterSpecificBuff("slech_3", "slechticna", "Průraz zbroje", "🗡️", 3, "Přesně ví, kde má nepřátelská zbroj slabiny.", "+10% Crit, +12% Útok, ignoruje 15% obrany", attackBonusPercent = 0.12f, critBonusPercent = 10, specialEffectTag = "ARMOR_PIERCE"),
            CharacterSpecificBuff("slech_4", "slechticna", "Královská poprava", "👑", 4, "Nemilosrdné údery proti oslabeným nepřátelům.", "+15% Crit, +20% Útok, +25% DMG pod 50% HP", attackBonusPercent = 0.20f, critBonusPercent = 15, specialEffectTag = "EXECUTE"),
            CharacterSpecificBuff("slech_5", "slechticna", "Císařský dekret", "⚜️", 5, "Její rozkazy lámou morálku i obranu nepřátel.", "+22% Crit, +30% Útok, nepřátelé ztrácí 15% DEF", attackBonusPercent = 0.30f, critBonusPercent = 22, specialEffectTag = "ENEMY_WEAKEN"),
            CharacterSpecificBuff("slech_6", "slechticna", "Vládkyně krvavého trůnu", "💎", 6, "Absolutní autorita přetavuje každý útok ve fatální zkázu.", "+35% Crit, +45% Útok, +50% Krit DMG, +25% Zlato", attackBonusPercent = 0.45f, critBonusPercent = 35, specialEffectTag = "SUPREME_COMMAND")
        ),
        "odvazna" to listOf(
            CharacterSpecificBuff("odv_1", "odvazna", "Válečnický instinkt", "⚔️", 1, "Bojovnice zvyklá na tvrdý střet.", "+4% Útok", attackBonusPercent = 0.04f),
            CharacterSpecificBuff("odv_2", "odvazna", "Průlomový šik", "🛡️", 2, "Vyráží do první linie s divokým řevem.", "+8% Útok, +3 Obrana, 10% šance na protiútok", attackBonusPercent = 0.08f, defenseBonus = 3, specialEffectTag = "COUNTER_ATTACK"),
            CharacterSpecificBuff("odv_3", "odvazna", "Berserkerův zápal", "🔥", 3, "Čím těžší boj, tím zuřivěji dopadají její čepele.", "+15% Útok, +20 HP, +8% Crit", attackBonusPercent = 0.15f, hpBonus = 20, critBonusPercent = 8),
            CharacterSpecificBuff("odv_4", "odvazna", "Krvavá rána", "🩸", 4, "Její zásahy rozrývají těla a způsobují krvácení.", "+25% Útok, +30 HP, útoky způsobují Krvácení", attackBonusPercent = 0.25f, hpBonus = 30, specialEffectTag = "BLEED"),
            CharacterSpecificBuff("odv_5", "odvazna", "Valkýra dominia", "⚡", 5, "Bojová zuřivost přechází v mistrovskou eleganci smrti.", "+40% Útok, +50 HP, 25% šance na dvojitý zásah", attackBonusPercent = 0.40f, hpBonus = 50, specialEffectTag = "DOUBLE_STRIKE"),
            CharacterSpecificBuff("odv_6", "odvazna", "Nesmrtelná válečná bohyně", "💥", 6, "Žádný nepřítel neodolá jejímu válečnému běsnění.", "+65% Útok, +80 HP, +15% Crit, omráčí cíl 1. úderem", attackBonusPercent = 0.65f, hpBonus = 80, critBonusPercent = 15, specialEffectTag = "STUN_OPENER")
        ),
        "touha" to listOf(
            CharacterSpecificBuff("touha_1", "touha", "Smyslný pohled", "💋", 1, "Odvádí pozornost nepřátel svůdnými gesty.", "-3 Útok nepřítele", specialEffectTag = "DISTRACT"),
            CharacterSpecificBuff("touha_2", "touha", "Vysávání esence", "🔮", 2, "Při útocích saje životní sílu a posiluje pána.", "+8% Útok, vysává 6 HP, +2 Temná energie", attackBonusPercent = 0.08f, hpRegenPerTurn = 2, darkEnergyRegenPerTurn = 2, specialEffectTag = "LIFESTEAL"),
            CharacterSpecificBuff("touha_3", "touha", "Omamná mlha", "💜", 3, "Nepřátelé propadají halucinacím a ztrácejí přesnost.", "+14% Útok, nepřítel má -15% přesnost, +4 TE", attackBonusPercent = 0.14f, darkEnergyRegenPerTurn = 4, specialEffectTag = "BLIND"),
            CharacterSpecificBuff("touha_4", "touha", "Temná extáze", "🔥", 4, "Rozkoš z boje přináší mohutný nárůst temné magie.", "+22% Útok, vysává 12 HP, +6 Temná energie/kolo", attackBonusPercent = 0.22f, hpRegenPerTurn = 4, darkEnergyRegenPerTurn = 6, specialEffectTag = "LIFESTEAL"),
            CharacterSpecificBuff("touha_5", "touha", "Kletba nenasytnosti", "🖤", 5, "Pouto rozkoše láme vůli a vysává život nepřátel.", "+35% Útok, nepřítel -20% DEF & ATK, vysává 18 HP/kolo", attackBonusPercent = 0.35f, hpRegenPerTurn = 6, darkEnergyRegenPerTurn = 8, specialEffectTag = "SIPHON_AURA"),
            CharacterSpecificBuff("touha_6", "touha", "Královna nenasytné slasti", "👑", 6, "Dokonalé spojení smyslů. Pán čerpá bezednou moc.", "+50% Útok, +12 TE/kolo, vysává 30 HP/útok, zmate bosse", attackBonusPercent = 0.50f, darkEnergyRegenPerTurn = 12, specialEffectTag = "DOMINATION_AURA")
        ),
        "sukuba" to listOf(
            CharacterSpecificBuff("suk_1", "sukuba", "Démonická jiskra", "😈", 1, "Pekelný původ posiluje útoky temnotou.", "+5% Útok, +2 Temná energie", attackBonusPercent = 0.05f, darkEnergyRegenPerTurn = 2),
            CharacterSpecificBuff("suk_2", "sukuba", "Pekelné pohlcení", "🔥", 2, "Saje krev zraněných protivníků.", "+10% Útok, útoky vysávají 8 HP, +3 TE", attackBonusPercent = 0.10f, darkEnergyRegenPerTurn = 3, specialEffectTag = "LIFESTEAL"),
            CharacterSpecificBuff("suk_3", "sukuba", "Pekelný plamen", "🌋", 3, "Zapaluje zbroj a maso nepřátel pekelným ohněm.", "+18% Útok, zapaluje cíl na 3 kola (15 DMG/kolo)", attackBonusPercent = 0.18f, specialEffectTag = "BURN"),
            CharacterSpecificBuff("suk_4", "sukuba", "Pakt zatracených", "🩸", 4, "Démonická smlouva posiluje úder a oslabuje odpor.", "+28% Útok, +8% Crit, nepřátelé +15% magic DMG", attackBonusPercent = 0.28f, critBonusPercent = 8),
            CharacterSpecificBuff("suk_5", "sukuba", "Podsvětní bouře", "🖤", 5, "Vyvolává vír stínů a plamenů podsvětí.", "+42% Útok, vysává 20 HP, +8 TE/kolo, plošný popel", attackBonusPercent = 0.42f, hpRegenPerTurn = 6, darkEnergyRegenPerTurn = 8, specialEffectTag = "HELL_STORM"),
            CharacterSpecificBuff("suk_6", "sukuba", "Arcidémonická vládkyně zkázy", "👑", 6, "Vládne peklu po tvém boku. Okamžitá poprava pod 18% HP.", "+60% Útok, +15 TE/kolo, 35 HP Life-Steal, poprava <18% HP", attackBonusPercent = 0.60f, darkEnergyRegenPerTurn = 15, specialEffectTag = "HELL_EXECUTE")
        ),
        "draci_divka" to listOf(
            CharacterSpecificBuff("draci_1", "draci_divka", "Dračí šupiny", "🐉", 1, "Přirozená tvrdost dračí kůže.", "+6 Obrana", defenseBonus = 6),
            CharacterSpecificBuff("draci_2", "draci_divka", "Žhnoucí dech", "🔥", 2, "Vyfukuje plameny a zvyšuje útočnou sílu.", "+10 Obrana, +8% Útok, ohnivé poškození", attackBonusPercent = 0.08f, defenseBonus = 10),
            CharacterSpecificBuff("draci_3", "draci_divka", "Dračí pancíř", "🛡️", 3, "Odolává drtivým úderům i magii živlů.", "+18 Obrana, +40 HP, -15% poškození od elementů", defenseBonus = 18, hpBonus = 40, specialEffectTag = "ELEMENT_RESIST"),
            CharacterSpecificBuff("draci_4", "draci_divka", "Dračí hněv", "🌋", 4, "Vztek dračí krve spaluje zbroj nepřítele.", "+28 Obrana, +18% Útok, taví zbroj (-12 DEF)", attackBonusPercent = 0.18f, defenseBonus = 28, specialEffectTag = "MELT_ARMOR"),
            CharacterSpecificBuff("draci_5", "draci_divka", "Pradávné dračí srdce", "💥", 5, "Probuzená krev prastarých draků.", "+45 Obrana, +80 HP, +30% Útok, exploze při zásahu", attackBonusPercent = 0.30f, defenseBonus = 45, hpBonus = 80, specialEffectTag = "RETALIATION_BURST"),
            CharacterSpecificBuff("draci_6", "draci_divka", "Císařovna dračí apokalypsy", "👑", 6, "Neporazitelná dračí vládkyně oblohy. Pán a dračice jsou nezastavitelní.", "+70 Obrana, +140 HP, +50% Útok, nezranitelnost 1. kolo, meteorický déšť", attackBonusPercent = 0.50f, defenseBonus = 70, hpBonus = 140, specialEffectTag = "METEOR_APOCALYPSE")
        ),
        "chladna" to listOf(
            CharacterSpecificBuff("chlad_1", "chladna", "Ledový pohled", "❄️", 1, "Zpomaluje pohyby nepřátel.", "+3 Obrana, -5% rychlost cíle", defenseBonus = 3),
            CharacterSpecificBuff("chlad_2", "chladna", "Mrazivý štít", "🛡️", 2, "Krystalický štít absorbuje fyzické údery.", "+8 Obrana, +15 HP", defenseBonus = 8, hpBonus = 15),
            CharacterSpecificBuff("chlad_3", "chladna", "Ledové ostny", "🧊", 3, "Odrážejí poškození zpět na útočníka.", "+15 Obrana, 15% reflect poškození", defenseBonus = 15, specialEffectTag = "DAMAGE_REFLECT"),
            CharacterSpecificBuff("chlad_4", "chladna", "Tříštivý mráz", "💎", 4, "Zmrazení zbroje nepřátel usnadňuje jejich zničení.", "+22 Obrana, +15% Crit, šance zmrazit cíl", defenseBonus = 22, critBonusPercent = 15, specialEffectTag = "FREEZE"),
            CharacterSpecificBuff("chlad_5", "chladna", "Věčný permafrost", "🌨️", 5, "Celé bojiště pokrývá smrtící chlad.", "+35 Obrana, +60 HP, nepřátelé mají -25% útok", defenseBonus = 35, hpBonus = 60, specialEffectTag = "SLOW_AURA"),
            CharacterSpecificBuff("chlad_6", "chladna", "Sněhová královna zkázy", "👑", 6, "Absolutní nula. Zmrazí všechny nepřátele v prvním kole.", "+55 Obrana, +120 HP, +30% Crit, zmrazení všech bossů", defenseBonus = 55, hpBonus = 120, critBonusPercent = 30, specialEffectTag = "ABSOLUTE_ZERO")
        ),
        "ustrasena" to listOf(
            CharacterSpecificBuff("ustr_1", "ustrasena", "Bázlivý úskok", "🐇", 1, "Rychle uskakuje před útoky.", "+5% Šance na úhyb", specialEffectTag = "DODGE"),
            CharacterSpecificBuff("ustr_2", "ustrasena", "Záchvěv pudu sebezáchovy", "✨", 2, "V nebezpečí instinktivně léčí pána.", "+15 HP, +3 HP/kolo", hpBonus = 15, hpRegenPerTurn = 3),
            CharacterSpecificBuff("ustr_3", "ustrasena", "Plaché štěstí", "🍀", 3, "Nepředvídatelné šťastné zásahy v panice.", "+25 HP, +12% Crit, +8% úhyb", hpBonus = 25, critBonusPercent = 12, specialEffectTag = "LUCKY_STRIKE"),
            CharacterSpecificBuff("ustr_4", "ustrasena", "Ochranná náruč", "💖", 4, "Překonává strach, aby zachránila pána.", "+50 HP, +10 Obrana, vyléčí 30 HP při poklesu pod 25%", hpBonus = 50, defenseBonus = 10, specialEffectTag = "EMERGENCY_HEAL"),
            CharacterSpecificBuff("ustr_5", "ustrasena", "Anděl spásy", "🕊️", 5, "Z plachého děvčete se stává zázračná ochránkyně.", "+80 HP, +15 Obrana, +8 HP/kolo celé družině", hpBonus = 80, defenseBonus = 15, hpRegenPerTurn = 8, specialEffectTag = "DIVINE_REGEN"),
            CharacterSpecificBuff("ustr_6", "ustrasena", "Něžná královna světla", "👑", 6, "Pouto lásky zahnalo veškerý strach. Oživí pána při fatálním zranění.", "+150 HP, +25 Obrana, +15 HP/kolo, jednorázové znovuzrození pána s 50% HP", hpBonus = 150, defenseBonus = 25, hpRegenPerTurn = 15, specialEffectTag = "RESURRECTION")
        ),
        "vzdorna" to listOf(
            CharacterSpecificBuff("vzd_1", "vzdorna", "Zatvrzelost", "👊", 1, "Vzdoruje bolesti i ranám.", "+5 Obrana", defenseBonus = 5),
            CharacterSpecificBuff("vzd_2", "vzdorna", "Odvetný úder", "⚔️", 2, "Okamžitě oplácí každý obdržený úder.", "+10 Obrana, 15% šance na protiútok", defenseBonus = 10, specialEffectTag = "COUNTER_ATTACK"),
            CharacterSpecificBuff("vzd_3", "vzdorna", "Nezlomná vůle", "🛡️", 3, "Odmítá padnout k zemi.", "+20 Obrana, +35 HP, imunita vůči omráčení", defenseBonus = 20, hpBonus = 35, specialEffectTag = "STUN_IMMUNITY"),
            CharacterSpecificBuff("vzd_4", "vzdorna", "Rebelský hněv", "🔥", 4, "Čím méně má životů, tím větší způsobuje poškození.", "+30 Obrana, až +40% Útok při nízkém HP", defenseBonus = 30, specialEffectTag = "ENRAGE"),
            CharacterSpecificBuff("vzd_5", "vzdorna", "Věrný štítonoš rebelie", "⚡", 5, "Přísahala věrnost pánovi a chrání ho do posledního dechu.", "+45 Obrana, +70 HP, přebírá 30% poškození způsobeného pánovi", defenseBonus = 45, hpBonus = 70, specialEffectTag = "DAMAGE_TRANSFER"),
            CharacterSpecificBuff("vzd_6", "vzdorna", "Nepokořitelná válečná paní", "👑", 6, "Vzdor přetavený v nezdolnou pevnost dominia.", "+75 Obrana, +130 HP, +30% Útok, nelze zabít jedním útokem", defenseBonus = 75, hpBonus = 130, attackBonusPercent = 0.30f, specialEffectTag = "UNDYING_WILL")
        ),
        "zlomena" to listOf(
            CharacterSpecificBuff("zlom_1", "zlomena", "Necitlivost", "⛓️", 1, "Necítí bolest jako dříve.", "+4 Obrana, +10 HP", defenseBonus = 4, hpBonus = 10),
            CharacterSpecificBuff("zlom_2", "zlomena", "Prázdná schránka", "🖤", 2, "Absorbuje temné energie bez odporu.", "+8 Obrana, +25 HP, +2 Temná energie", defenseBonus = 8, hpBonus = 25, darkEnergyRegenPerTurn = 2),
            CharacterSpecificBuff("zlom_3", "zlomena", "Pouto utrpení", "🩸", 3, "Pohlcuje negativní efekty seslané na pána.", "+15 Obrana, +45 HP, absorbuje krvácení a kletby", defenseBonus = 15, hpBonus = 45, specialEffectTag = "CLEANSE_AURA"),
            CharacterSpecificBuff("zlom_4", "zlomena", "Temné znovuzrození", "🔮", 4, "Pánova vůle naplňuje prázdnou schránku děsivou silou.", "+25 Obrana, +18% Útok, +4 Temná energie/kolo", defenseBonus = 25, attackBonusPercent = 0.18f, darkEnergyRegenPerTurn = 4),
            CharacterSpecificBuff("zlom_5", "zlomena", "Loutka stínů", "👤", 5, "Zcela podřízena pánovým příkazům provádí perfektní údery.", "+40 Obrana, +30% Útok, +12% Crit, ignoruje bolest", defenseBonus = 40, attackBonusPercent = 0.30f, critBonusPercent = 12),
            CharacterSpecificBuff("zlom_6", "zlomena", "Přízračná královna prázdnoty", "👑", 6, "Z popela povstala nehmotná entita chránící pána.", "+65 Obrana, +110 HP, +40% Útok, 25% šance zcela ignorovat útok", defenseBonus = 65, hpBonus = 110, attackBonusPercent = 0.40f, specialEffectTag = "VOID_FORM")
        ),
        "manipulativni" to listOf(
            CharacterSpecificBuff("manip_1", "manipulativni", "Odhalení slabiny", "👁️", 1, "Všímá si mezer v taktice protivníka.", "+3% Crit", critBonusPercent = 3),
            CharacterSpecificBuff("manip_2", "manipulativni", "Jedovatá čepel", "🐍", 2, "Potírá zbraně oslabujícím jedem.", "+6% Útok, útoky aplikují slabý jed", attackBonusPercent = 0.06f, specialEffectTag = "POISON"),
            CharacterSpecificBuff("manip_3", "manipulativni", "Zákeřná past", "🕸️", 3, "Připravuje pasti, které znehybní nepřátele.", "+12% Útok, 20% šance omráčit cíl na 1 kolo", attackBonusPercent = 0.12f, specialEffectTag = "TRAP_STUN"),
            CharacterSpecificBuff("manip_4", "manipulativni", "Kradež kořisti", "💰", 4, "Během boje krade nepřátelům cennosti a léky.", "+18% Útok, +10% Crit, +30% Zlato ze souboje", attackBonusPercent = 0.18f, critBonusPercent = 10, specialEffectTag = "GOLD_STEAL"),
            CharacterSpecificBuff("manip_5", "manipulativni", "Mistr loutek", "🎭", 5, "Zmate nepřátele tak, že zaútočí na své spojence.", "+28% Útok, nepřátelé mají šanci zranit sami sebe", attackBonusPercent = 0.28f, specialEffectTag = "CONFUSION"),
            CharacterSpecificBuff("manip_6", "manipulativni", "Královna stínových pavučin", "👑", 6, "Genialita intrik garantuje triumf dříve než bitva začne.", "+45% Útok, +25% Crit, nepřátelé začínají s -30% HP", attackBonusPercent = 0.45f, critBonusPercent = 25, specialEffectTag = "PRE_STRIKE")
        ),
        "hysterialni" to listOf(
            CharacterSpecificBuff("hyst_1", "hysterialni", "Nepředvídatelný výpad", "⚡", 1, "Náhlý záchvat hněvu překvapí cíl.", "+4% Útok, +2% Crit", attackBonusPercent = 0.04f, critBonusPercent = 2),
            CharacterSpecificBuff("hyst_2", "hysterialni", "Chaos v boji", "🌪️", 2, "Chaotické útoky zmátnou nepřátelskou obranu.", "+8% Útok, +6% Crit", attackBonusPercent = 0.08f, critBonusPercent = 6),
            CharacterSpecificBuff("hyst_3", "hysterialni", "Běsnící výkřik", "📢", 3, "Pronikavý křik otřese vůlí nepřátel.", "+15% Útok, nepřátelé mají -10% útok", attackBonusPercent = 0.15f, specialEffectTag = "TERRIFY"),
            CharacterSpecificBuff("hyst_4", "hysterialni", "Extatický záchvat", "💥", 4, "Bojové šílenství přináší lavinu ran.", "+25% Útok, +15% Crit, šance na trojitý úder", attackBonusPercent = 0.25f, critBonusPercent = 15, specialEffectTag = "TRIPLE_STRIKE"),
            CharacterSpecificBuff("hyst_5", "hysterialni", "Nezvladatelná smršť", "🌪️", 5, "Divoká lavina ran drtí vše v dosahu.", "+38% Útok, +20% Crit, plošné krvácení", attackBonusPercent = 0.38f, critBonusPercent = 20, specialEffectTag = "AOE_BLEED"),
            CharacterSpecificBuff("hyst_6", "hysterialni", "Královna nespoutaného chaosu", "👑", 6, "Absolutní výbušná síla nekontrolovatelných emocí.", "+60% Útok, +30% Crit, každý kritický zásah léčí pána", attackBonusPercent = 0.60f, critBonusPercent = 30, specialEffectTag = "CHAOS_HEAL")
        ),
        "nymfomanka" to listOf(
            CharacterSpecificBuff("nymf_1", "nymfomanka", "Vzrušující dotek", "💋", 1, "Její přítomnost naplňuje vzduch vášní.", "+2 Temná energie za kolo", darkEnergyRegenPerTurn = 2),
            CharacterSpecificBuff("nymf_2", "nymfomanka", "Slastné vyčerpání", "💖", 2, "Vysává výdrž a sílu nepřátel.", "+6% Útok, snižuje výdrž nepřítele o 10%", attackBonusPercent = 0.06f),
            CharacterSpecificBuff("nymf_3", "nymfomanka", "Omamný feromon", "🌸", 3, "Vůně rozkoše paralyzuje nepřátelské vojáky.", "+12% Útok, 15% šance ochromit nepřítele", attackBonusPercent = 0.12f, specialEffectTag = "CHARM_PARALYSIS"),
            CharacterSpecificBuff("nymf_4", "nymfomanka", "Orgastický výbuch", "🔥", 4, "Extáze z přítomnosti pána exploduje v mohutné kouzlo.", "+20% Útok, +5 Temná energie/kolo, vysává 15 HP", attackBonusPercent = 0.20f, darkEnergyRegenPerTurn = 5, specialEffectTag = "LIFESTEAL"),
            CharacterSpecificBuff("nymf_5", "nymfomanka", "Nenasytná posedlost", "🖤", 5, "Touha po pánovi jí dává nadlidskou energii a rychlost.", "+32% Útok, +20% Rychlost, +8 TE/kolo", attackBonusPercent = 0.32f, darkEnergyRegenPerTurn = 8),
            CharacterSpecificBuff("nymf_6", "nymfomanka", "Bohyně věčné rozkoše a plození", "👑", 6, "Neukojitelná touha přetvořená v bezedný zdroj temné moci.", "+50% Útok, +15 TE/kolo, nepřátelé ztrácejí 50% obrany okouzlením", attackBonusPercent = 0.50f, darkEnergyRegenPerTurn = 15, specialEffectTag = "SIREN_SEDUCTION")
        ),
        "ticha_panenka" to listOf(
            CharacterSpecificBuff("ticha_1", "ticha_panenka", "Bezhlučný krok", "🤫", 1, "Pohybuje se bez jediného zvuku.", "+3% Útok, +5% Úhyb", attackBonusPercent = 0.03f),
            CharacterSpecificBuff("ticha_2", "ticha_panenka", "Jedová jehlice", "🪡", 2, "Zasazuje přesné údery do slabin.", "+8% Útok, aplikuje smrtící toxin", attackBonusPercent = 0.08f, specialEffectTag = "POISON"),
            CharacterSpecificBuff("ticha_3", "ticha_panenka", "Tichý atentát", "🗡️", 3, "Útoky ze zálohy způsobují obrovské škody.", "+16% Útok, +15% Crit při plném HP nepřítele", attackBonusPercent = 0.16f, critBonusPercent = 15, specialEffectTag = "AMBUSH"),
            CharacterSpecificBuff("ticha_4", "ticha_panenka", "Stínové loutkářství", "🎭", 4, "Vytváří iluze a odvádí pozornost od pána.", "+24% Útok, +12 Obrana, 20% šance vyhnout se zásahu", attackBonusPercent = 0.24f, defenseBonus = 12, specialEffectTag = "ILLUSION_DODGE"),
            CharacterSpecificBuff("ticha_5", "ticha_panenka", "Smrtící balet", "🩰", 5, "Ladné a tiché pohyby přinášejí rychlou smrt.", "+38% Útok, +20% Crit, útoky ignorují 25% obrany", attackBonusPercent = 0.38f, critBonusPercent = 20, specialEffectTag = "ARMOR_PIERCE"),
            CharacterSpecificBuff("ticha_6", "ticha_panenka", "Královna tiché popravy", "👑", 6, "Dokonalá vražedná zbraň dominia. Zlikviduje nepřítele dřív, než vykřikne.", "+60% Útok, +35% Crit, okamžitá poprava pod 20% HP", attackBonusPercent = 0.60f, critBonusPercent = 35, specialEffectTag = "EXECUTE")
        ),
        "krvava_subka" to listOf(
            CharacterSpecificBuff("krv_1", "krvava_subka", "Krvavá radost", "🩸", 1, "Bolest ji posiluje v útoku.", "+5% Útok", attackBonusPercent = 0.05f),
            CharacterSpecificBuff("krv_2", "krvava_subka", "Masochistická zuřivost", "🔪", 2, "Při každém obdrženém zranění stoupá její útočná síla.", "+10% Útok, +5% útok za každý utržený zásah", attackBonusPercent = 0.10f, specialEffectTag = "PAIN_TO_POWER"),
            CharacterSpecificBuff("krv_3", "krvava_subka", "Krvavá hostina", "🩸", 3, "Pije krev svých protivníků.", "+18% Útok, útoky vysávají 10 HP", attackBonusPercent = 0.18f, specialEffectTag = "LIFESTEAL"),
            CharacterSpecificBuff("krv_4", "krvava_subka", "Obětní pouto", "⛓️", 4, "Slastně přijímá zranění určená pro pána.", "+28% Útok, +40 HP, kryje 25% DMG pána", attackBonusPercent = 0.28f, hpBonus = 40, specialEffectTag = "DAMAGE_TRANSFER"),
            CharacterSpecificBuff("krv_5", "krvava_subka", "Krvavý rituál rozkoše", "🔥", 5, "Koupel v nepřátelské krvi jí propůjčuje děsivou sílu.", "+42% Útok, +20 HP Life-Steal, masivní krvácení cíle", attackBonusPercent = 0.42f, specialEffectTag = "VAMPIRIC_FRENZY"),
            CharacterSpecificBuff("krv_6", "krvava_subka", "Královna karmínového martyria", "👑", 6, "Nesmrtelná v bolesti a slasti. S každým zraněním narůstá ničivost.", "+65% Útok, +80 HP, 30 HP Life-Steal, zranění pána spouští berserk", attackBonusPercent = 0.65f, hpBonus = 80, specialEffectTag = "CRIMSON_BERSERK")
        ),
        "posedla" to listOf(
            CharacterSpecificBuff("pos_1", "posedla", "Šepot stínů", "👁️", 1, "Démonické hlasy varují před útoky.", "+2 Temná energie, +3 Obrana", darkEnergyRegenPerTurn = 2, defenseBonus = 3),
            CharacterSpecificBuff("pos_2", "posedla", "Dotek propasti", "🖤", 2, "Stínová magie spaluje duši protivníka.", "+8% Útok, +3 TE/kolo, temné poškození", attackBonusPercent = 0.08f, darkEnergyRegenPerTurn = 3),
            CharacterSpecificBuff("pos_3", "posedla", "Démonická transformace", "👿", 3, "Částečně uvolňuje démona uvnitř.", "+18% Útok, +25 HP, +5 TE/kolo", attackBonusPercent = 0.18f, hpBonus = 25, darkEnergyRegenPerTurn = 5),
            CharacterSpecificBuff("pos_4", "posedla", "Vír zatracení", "🌀", 4, "Pohlcuje magii nepřátel a posiluje pána.", "+28% Útok, snižuje magii cíle o 25%, +8 TE/kolo", attackBonusPercent = 0.28f, darkEnergyRegenPerTurn = 8, specialEffectTag = "MANA_DRAIN"),
            CharacterSpecificBuff("pos_5", "posedla", "Eldritchova hrůza", "🐙", 5, "Vzhled prastarých monster ochromí celé bojiště.", "+42% Útok, +50 HP, nepřátelé mají 30% šanci ztratit tah", attackBonusPercent = 0.42f, hpBonus = 50, specialEffectTag = "FEAR_STUN"),
            CharacterSpecificBuff("pos_6", "posedla", "Královna bezedné temnoty", "👑", 6, "Plně ovládnutý démon slouží pánovi. Zkáza celého světa v jednom gestu.", "+65% Útok, +100 HP, +15 TE/kolo, vyvolává stínovou apokalypsu", attackBonusPercent = 0.65f, hpBonus = 100, darkEnergyRegenPerTurn = 15, specialEffectTag = "VOID_APOCALYPSE")
        )
    )

    fun getCharacterSpecificBuff(archetypeId: String, tier: Int): CharacterSpecificBuff {
        val list = CHARACTER_SPECIFIC_BUFFS[archetypeId] ?: CHARACTER_SPECIFIC_BUFFS["subka"]!!
        val clampedTier = tier.coerceIn(1, 6)
        return list.firstOrNull { it.tierRequired == clampedTier } ?: list.last()
    }

    fun getAllBuffsForArchetype(archetypeId: String): List<CharacterSpecificBuff> {
        return CHARACTER_SPECIFIC_BUFFS[archetypeId] ?: CHARACTER_SPECIFIC_BUFFS["subka"]!!
    }

    fun getUnlockedBuffs(archetypeId: String, currentTier: Int): List<CharacterSpecificBuff> {
        return getAllBuffsForArchetype(archetypeId).filter { it.tierRequired <= currentTier }
    }

    fun getNextLockedBuff(archetypeId: String, currentTier: Int): CharacterSpecificBuff? {
        return getAllBuffsForArchetype(archetypeId).firstOrNull { it.tierRequired > currentTier }
    }

    fun getUnlockedDialogueLibrary(character: Character): List<AffinityDialogueEntry> {
        val currentTier = getLevelForPoints(character.affinityPoints)
        val result = mutableListOf<AffinityDialogueEntry>()
        val categories = listOf("Pozdrav a oslovení", "Bojový pokřik", "Intimní vyznání", "Pouto duší")

        for (tier in 1..6) {
            val tierInfo = TIERS.find { it.level == tier } ?: TIERS.first()
            val lines = getDialoguesForTier(character.archetypeId, tier)
            lines.forEachIndexed { index, line ->
                val category = categories.getOrElse(index % categories.size) { "Vyznání oddanosti" }
                result.add(
                    AffinityDialogueEntry(
                        tier = tier,
                        tierTitle = tierInfo.title,
                        category = category,
                        text = line,
                        isUnlocked = tier <= currentTier
                    )
                )
            }
        }
        return result
    }

    fun getPassiveDialogues(character: Character): List<String> {
        val tier = getLevelForPoints(character.affinityPoints)
        return getDialoguesForTier(character.archetypeId, tier)
    }

    fun getRandomActiveDialogue(character: Character): String {
        val list = getPassiveDialogues(character)
        return list.randomOrNull() ?: "„Můj pane, má oddanost patří jen tobě.“"
    }

    fun getRandomActiveDialogue(affinityPoints: Int, archetypeId: String = "subka"): String {
        val tier = getLevelForPoints(affinityPoints)
        val lines = getDialoguesForTier(archetypeId, tier)
        return lines.randomOrNull() ?: (PASSIVE_DIALOGUES[tier]?.randomOrNull() ?: "„Můj pane, má oddanost patří jen tobě.“")
    }

    fun getAffinityCombatBonuses(tier: Int): AffinityCombatBonus {
        return when {
            tier >= 6 -> AffinityCombatBonus(hpBonus = 100, dmgMultiplierBonus = 0.40f, defenseBonus = 15, critBonus = 15, regenBonus = 8, darkEnergyBonus = 5)
            tier >= 5 -> AffinityCombatBonus(hpBonus = 60, dmgMultiplierBonus = 0.25f, defenseBonus = 10, critBonus = 10, regenBonus = 5, darkEnergyBonus = 0)
            tier >= 4 -> AffinityCombatBonus(hpBonus = 40, dmgMultiplierBonus = 0.15f, defenseBonus = 6, critBonus = 6, regenBonus = 3, darkEnergyBonus = 0)
            tier >= 3 -> AffinityCombatBonus(hpBonus = 25, dmgMultiplierBonus = 0.08f, defenseBonus = 4, critBonus = 10, regenBonus = 0, darkEnergyBonus = 0)
            tier >= 2 -> AffinityCombatBonus(hpBonus = 10, dmgMultiplierBonus = 0.04f, defenseBonus = 2, critBonus = 2, regenBonus = 0, darkEnergyBonus = 0)
            else -> AffinityCombatBonus(hpBonus = 0, dmgMultiplierBonus = 0.0f, defenseBonus = 0, critBonus = 0, regenBonus = 0, darkEnergyBonus = 0)
        }
    }

    fun getScenarioForArchetype(archetypeId: String, characterName: String, tier: Int): DialogueScenario {
        return when (tier) {
            1, 2 -> when (archetypeId) {
                "subka" -> DialogueScenario(
                    prompt = "„Můj drahý pane... poklekám před tebou. Daří se mi dobře plnit mé úkoly? Chci pro tebe být dokonalou...“",
                    options = listOf(
                        DialogueOption("„Dělej víc a mluv méně. Tvou jedinou starostí je poslušnost.“", "S povzdechem a chvěním přijímá tvůj chladný rozkaz.", 14, submissiveness = 12, fear = 6, loyalty = 8),
                        DialogueOption("„Zasloužíš si pochvalu, děláš mi velkou radost.“", "Její tváře zrudnou štěstím a v jejích očích se zalesknou slzy vděku.", 16, trust = 15, submissiveness = -4, loyalty = 12),
                        DialogueOption("„Tvůj osud závisí na mém rozmaru. Pamatuj na to.“", "Zatají dech, její submisivní srdce divoce bije nadšením z tvé nadvlády.", 18, submissiveness = 18, broken = 5, fear = 8)
                    )
                )
                "slechticna" -> DialogueScenario(
                    prompt = "„Tyto komnaty jsou sice čisté, ale v mém rodném paláci jsme měli stropy zdobené zlatem a hedvábný nábytek...“",
                    options = listOf(
                        DialogueOption("„Tvůj dřívější palác je minulostí. Nyní jsi ozdobou mého dominia.“", "Překvapeně zamrká, tvá pevná sebejistota v ní vzbuzuje nečekaný respekt.", 18, loyalty = 12, submissiveness = 8, trust = 5),
                        DialogueOption("„Stěžuj si dál a nechám tě spát v chladném sklepě na slámě.“", "Hrdě se narovná, ale v očích se jí mihne záblesk skutečné bázně.", -3, fear = 15, broken = 6),
                        DialogueOption("„Zajistím, aby tvé komnaty byly co nejluxusnější.“", "Jemně se usměje, oceňuje tvou snahu, i když tě v duchu považuje za trochu slabého.", 10, trust = 18, submissiveness = -10, loyalty = 10)
                    )
                )
                "odvazna" -> DialogueScenario(
                    prompt = "„Díváš se na mě, jako bych byla jen další trofej na tvé zdi. Raději bych s tebou zkřížila meč, než tu nečinně postávala!“",
                    options = listOf(
                        DialogueOption("„Vezmi si cvičnou zbraň. Zkrotím tě přímo v aréně.“", "Její oči vzplanou bojovým zápalem a divokým respektem k tvé odvaze.", 20, trust = 12, loyalty = 15, submissiveness = 5),
                        DialogueOption("„Tvá divokost je to, co na tobě obdivuji. Nechci tě zlomit, chci tě po svém boku.“", "Na okamžik znejistí a na tváři se jí objeví nečekané uzardění.", 18, trust = 18, loyalty = 14),
                        DialogueOption("„Tady velím já. Skloň zbraň a nauč se poslouchat.“", "Zaťne zuby, ale poslechne. Bojovnice v ní cítí tvou neústupnou autoritu.", 12, submissiveness = 15, fear = 8)
                    )
                )
                "touha" -> DialogueScenario(
                    prompt = "„Cítím žár, který z tebe sálá, pane... bojíš se přistoupit blíž a dotknout se mě?“",
                    options = listOf(
                        DialogueOption("„Já se nebojím ničeho. Přistup blíž a ukaž mi, co dokážeš.“", "Její rty se zvlní v hříšném úsměvu, chvěje se vzrušením.", 20, trust = 15, loyalty = 15, submissiveness = 10),
                        DialogueOption("„Hraješ nebezpečnou hru. Já jsem ten, kdo zde určuje pravidla.“", "Zatají dech, tvá dominance v ní probouzí nezkrotnou touhu být podmaněna.", 18, submissiveness = 16, loyalty = 12),
                        DialogueOption("„Ztiš své choutky a soustřeď se na své povinnosti.“", "Zklamaně si povzdechne, ale její zájem o tebe tím jen roste.", 12, submissiveness = 10, fear = 5)
                    )
                )
                "sukuba" -> DialogueScenario(
                    prompt = "„Tvé dominium voní hříchem a mocí. Chceš mou démonickou duši, nebo jen mé lahodné tělo, drahý pane?“",
                    options = listOf(
                        DialogueOption("„Chci obojí. Všechno, čím jsi, náleží výhradně mně.“", "Její démonické oči zaplanou temným ohněm absolutního nadšení.", 22, loyalty = 18, submissiveness = 14),
                        DialogueOption("„Démoni poslouchají můj hlas. Naučím tě poslušnosti peklu navzdory.“", "Olízne si rty. Tvá tvrdost a temná vůle ji neskutečně přitahují.", 18, submissiveness = 18, loyalty = 12),
                        DialogueOption("„Jsi pro mě cennou spojenkyní. Společně ovládneme temnotu.“", "Přikývne s respektem. Cení si partnerství v temných uměních.", 16, trust = 18, loyalty = 12)
                    )
                )
                "draci_divka" -> DialogueScenario(
                    prompt = "„Dračí oheň v mém nitru nikdy neuhasne. Nedívej se na mě shora, pokud nemáš křídla!“",
                    options = listOf(
                        DialogueOption("„Můj stín je větší než jakákoliv křídla. I draci se sklánějí před mým trůnem.“", "Její šupiny zajiskří. Cítí v tobě alfa predátora a skloní hlavu.", 20, loyalty = 16, submissiveness = 14),
                        DialogueOption("„Dračí krev je vzácná. Cením si tvé hrdosti a nabídnu ti čestné místo.“", "Překvapeně zvedne oči, dotklo se to její vrozené dračí cti.", 18, trust = 20, loyalty = 14),
                        DialogueOption("„Pokud se nepodvolíš, tvůj oheň udusím okovy z černého železa.“", "Zavrčí, ale v jejích očích zahlédneš jiskru podvolení před silou.", 10, fear = 15, submissiveness = 15)
                    )
                )
                else -> DialogueScenario(
                    prompt = "„Můj pane... uvažuji o své budoucnosti v tvém dominiu. Co ode mě skutečně očekáváš?“",
                    options = listOf(
                        DialogueOption("„Očekávám absolutní věrnost a poslušnost bez otázek.“", "Skloní hlavu a tichým hlasem potvrzuje své podvolení tvé vůli.", 14, submissiveness = 12, loyalty = 10, fear = 5),
                        DialogueOption("„Chci tě chránit a vybudovat pro tebe bezpečné místo.“", "Na chvíli zjihne a v jejích očích zahlédneš jiskru skutečné důvěry.", 16, trust = 15, loyalty = 12),
                        DialogueOption("„Jsi mou majetkovou hračkou, nic víc mě nezajímá.“", "Smířeně sklopí zrak, její hrdost pomalu umírá, ale podřizuje se osudu.", 10, submissiveness = 15, broken = 8, fear = 10)
                    )
                )
            }
            3, 4 -> when (archetypeId) {
                "subka" -> DialogueScenario(
                    prompt = "„Pane... přinesla jsem ti víno a přemýšlela jsem. Moje místo je zde, po tvém boku... cítím to čím dál silněji.“",
                    options = listOf(
                        DialogueOption("„Tvé místo je u mých nohou, tam kde tě mohu cítit.“", "Slastně vydechne a okamžitě klesá na kolena, vděčná za tvůj nárok.", 22, submissiveness = 20, loyalty = 15),
                        DialogueOption("„Jsi víc než jen služebná. Jsi mou pravou rukou v komnatách.“", "Její srdce zaplesá, cítí, že její oddanost začíná mít skutečnou váhu.", 25, trust = 20, loyalty = 18, submissiveness = -5),
                        DialogueOption("„Pij se mnou. Chci slyšet tvé nejvnitřnější myšlenky.“", "Nejistě, ale s velkou radostí přijímá tvé pozvání do tvé blízkosti.", 20, trust = 25, loyalty = 15)
                    )
                )
                "slechticna" -> DialogueScenario(
                    prompt = "„Můj pane... uvažovala jsem o tvém vítězství nad mým rodem. Možná to tak mělo být. Ty vládneš s ohněm, který můj otec nikdy neměl...“",
                    options = listOf(
                        DialogueOption("„Tvůj rod byl slabý. Jen silní jako já si zaslouží tvou krásu a oddanost.“", "Dlouze se ti zadívá do očí a poprvé v nich vidíš něco jiného než pýchu – opravdový obdiv.", 24, loyalty = 20, trust = 12),
                        DialogueOption("„Před tebou leží nová budoucnost. Jako královna mého stínu.“", "Pousměje se, titul 'Královna stínu' v ní probouzí její staré ambice v nové formě.", 26, loyalty = 22, submissiveness = 10),
                        DialogueOption("„Přestaň mluvit o minulosti a ukaž mi svou dnešní věrnost.“", "Tiše přikývne a začne plnit tvá přání s nečekanou precizností.", 20, submissiveness = 15, fear = 8)
                    )
                )
                else -> DialogueScenario(
                    prompt = "„Cítím, jak se naše pouto prohlubuje, můj pane. Dřív jsem se tě bála, teď se na tvůj příchod těším...“",
                    options = listOf(
                        DialogueOption("„Těš se dál. Má přítomnost je tvou největší odměnou.“", "Její tvář se rozzáří a s úctou sleduje každý tvůj pohyb.", 22, loyalty = 15, trust = 15),
                        DialogueOption("„A co bys udělala, kdybych tě dnes nechal o samotě?“", "Její výraz posmutní, je na tobě už emočně zcela závislá.", 18, submissiveness = 20, loyalty = 12),
                        DialogueOption("„Pojď blíž a ukaž mi, jak moc ses těšila.“", "S radostným povzdechem zkracuje vzdálenost mezi vámi.", 24, trust = 20, loyalty = 18)
                    )
                )
            }
            else -> when (archetypeId) {
                "subka" -> DialogueScenario(
                    prompt = "„Můj božský pane... jsi smyslem mého bytí. Mé tělo, má duše, můj dech... vše patří jen tobě. Co si přeješ, abych pro tebe dnes byla?“",
                    options = listOf(
                        DialogueOption("„Buď mou absolutní loutkou. Zapomeň na vlastní vůli.“", "Její oči ztratí poslední záblesk pochybnosti. Je zcela tvá, prázdná schránka naplněná tvým rozkazem.", 30, submissiveness = 30, broken = 15, loyalty = 25),
                        DialogueOption("„Buď mou milovanou družkou, se kterou budu vládnout všem.“", "Pláče radostí, cítí se být na vrcholu světa jako tvá vyvolená.", 35, trust = 30, loyalty = 35),
                        DialogueOption("„Buď matkou mých budoucích dědiců temnoty.“", "S posvátnou úctou přijímá tvůj nejvyšší nárok na její život.", 40, loyalty = 40, trust = 25, submissiveness = 10)
                    )
                )
                "slechticna" -> DialogueScenario(
                    prompt = "„Vládče... moji předkové mě varovali před temnotou, ale v tvém objetí jsem našla pravou svobodu. Svobodu sloužit někomu, kdo je skutečně hoden mé koruny.“",
                    options = listOf(
                        DialogueOption("„Tvá koruna nyní září jen pro mě. Společně budeme nesmrtelní.“", "Její oddanost dosahuje absolutní výše. Už nejsi jejím věznitelem, jsi jejím bohem.", 35, loyalty = 40, trust = 30),
                        DialogueOption("„Poklekni a polib mou ruku jako symbol tvého konečného podrobení.“", "Dělá to s neuvěřitelnou grácií a pokorou, její pýcha se proměnila v čistou věrnost.", 32, submissiveness = 25, loyalty = 30),
                        DialogueOption("„Tvé aristokratické srdce teď bije v rytmu mých kroků.“", "Vášnivě tě líbá, potvrzujíc, že její život je navždy spjat s tvým.", 38, trust = 35, loyalty = 30)
                    )
                )
                else -> DialogueScenario(
                    prompt = "„Můj pane, má láska k tobě překonala vše – strach, hrdost i čas. Jsi mým osudem. Kamkoliv půjdeš, půjdu s tebou, i do nejhlubšího pekla.“",
                    options = listOf(
                        DialogueOption("„A tam také společně ovládneme vše stinné a mocné.“", "Její víra v tebe je neotřesitelná. Jste jedna duše v dvou tělech.", 40, loyalty = 40, trust = 40),
                        DialogueOption("„Zůstaň klečet v mém stínu a buď mým věčným klidem.“", "S tichou radostí a odevzdaností plní tvou vůli, nacházejíc v tom absolutní mír.", 35, submissiveness = 30, loyalty = 35),
                        DialogueOption("„Tvá věrnost je mou největší trofejí.“", "Dívá se na tebe s nepopsatelným obdivem a láskou v očích.", 38, trust = 35, loyalty = 35)
                    )
                )
            }
        }
    }
}

data class DialogueOption(
    val text: String,
    val feedback: String,
    val affinity: Int,
    val submissiveness: Int = 0,
    val loyalty: Int = 0,
    val trust: Int = 0,
    val fear: Int = 0,
    val broken: Int = 0
)

data class DialogueScenario(
    val prompt: String,
    val options: List<DialogueOption>
)

data class AffinityCombatBonus(
    val hpBonus: Int,
    val dmgMultiplierBonus: Float,
    val defenseBonus: Int,
    val critBonus: Int,
    val regenBonus: Int,
    val darkEnergyBonus: Int
)

