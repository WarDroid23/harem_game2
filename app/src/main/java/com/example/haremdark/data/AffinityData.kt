package com.example.haremdark.data

import com.example.haremdark.models.Character

data class AffinityTierInfo(
    val level: Int,
    val title: String,
    val subtitle: String,
    val minPoints: Int,
    val maxPoints: Int,
    val perkDescription: String,
    val combatBonusDescription: String = "Základní bojové zapojení bez pasivních bonusů",
    val icon: String,
    val colorHex: Long = 0xFFE91E63,
    val unlockedPerks: List<String> = listOf(perkDescription),
    val cosmeticReward: String? = null
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
            combatBonusDescription = "⚔️ Žádný bojový bonus (nedůvěra)",
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
            combatBonusDescription = "🛡️ Pasivní boj: +10 Max HP & +2 obrana dominia",
            icon = "🗝️",
            colorHex = 0xFF4CAF50,
            unlockedPerks = listOf("+10% zisk zlata ze správy komnat", "Odemčena nová pasivní vyznání a myšlenky"),
            cosmeticReward = "🎀 Stužka poslušnosti"
        ),
        AffinityTierInfo(
            level = 3,
            title = "Důvěrnice komnat",
            subtitle = "Začíná vnímat tvou přízeň a svěřuje ti své tajné touhy.",
            minPoints = 71,
            maxPoints = 120,
            perkDescription = "+15% efektivita intimních rituálů.",
            combatBonusDescription = "⚡ Pasivní boj: +25 Max HP, +10% Crit pro dívku & +4% globální Crit pána",
            icon = "💎",
            colorHex = 0xFF00BCD4,
            unlockedPerks = listOf("+15% efektivita intimních rituálů", "+10% zisk temné energie při rozkoši"),
            cosmeticReward = "💍 Ocelový prsten oddanosti"
        ),
        AffinityTierInfo(
            level = 4,
            title = "Oddaná milenka",
            subtitle = "Planoucí vášeň a touha trávit každou noc v tvém objetí.",
            minPoints = 121,
            maxPoints = 180,
            perkDescription = "+20% šance na zplození dědice a +10 SE.",
            combatBonusDescription = "💖 Pasivní boj: +40 Max HP, +15% Útok & regenerace +3 HP/kolo",
            icon = "🔥",
            colorHex = 0xFFFF4081,
            unlockedPerks = listOf("+20% šance na zplození dědice", "+10 max Sexuální energie v harému", "Svěřování nejhlubších tajemství"),
            cosmeticReward = "👑 Hedvábný závoj milenky"
        ),
        AffinityTierInfo(
            level = 5,
            title = "Spřízněná duše",
            subtitle = "Nerozlučné pouto duší. Žije a dýchá jen pro tvé dominium.",
            minPoints = 181,
            maxPoints = 250,
            perkDescription = "+25% obrana a útok pána v soubojích.",
            combatBonusDescription = "🔥 Pasivní boj: +60 Max HP, +25% Poškození & +10 Obrana pána i dívky",
            icon = "💖",
            colorHex = 0xFFE040FB,
            unlockedPerks = listOf("+25% bonus k útoku v soubojích", "+30% loajalita a imunita vůči vzpourám"),
            cosmeticReward = "✨ Očarovaný náhrdelník duší"
        ),
        AffinityTierInfo(
            level = 6,
            title = "Věčná královna",
            subtitle = "Absolutní splynutí mysli a srdce. Pánova pravá vládkyně.",
            minPoints = 251,
            maxPoints = 9999,
            perkDescription = "+50% pasivní příjem a nezlomná věrnost.",
            combatBonusDescription = "👑 Pasivní boj: +100 Max HP, +40% Masivní poškození, +15 Obrana & +5 TE/kolo",
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

    fun getScenarioForArchetype(archetypeId: String, characterName: String): DialogueScenario {
        return when (archetypeId) {
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
            "touha" -> DialogueScenario(
                prompt = "„Pane... dnes je v ložnici takový chlad. Smím ti být blíž? Moje kůže touží po tvém hřejivém doteku...“",
                options = listOf(
                    DialogueOption("„Pak tě zahřeji svým vlastním tělem. Pojď ke mně.“", "Její dech se zrychlí a s vášnivým povzdechem se ti tiskne do náruče.", 20, trust = 12, submissiveness = 5),
                    DialogueOption("„Vrať se ke své práci. Rozkoš a teplo si musíš nejprve zasloužit.“", "Kousne se do rtu, odmítnutí v ní rozpaluje ještě divočejší plamen touhy.", 14, submissiveness = 15, fear = 5),
                    DialogueOption("„Možná by ti pomohlo nosit víc těžkého dřeva do krbu.“", "Rozesměje se tvému škádlení a hravě do tebe strčí ramínkem.", 16, loyalty = 10, trust = 10)
                )
            )
            "odvazna" -> DialogueScenario(
                prompt = "„Pane... čistím svou čepel a přemýšlím. Troufl by sis se mnou bojovat bez doprovodu svých stráží?“",
                options = listOf(
                    DialogueOption("„Vyzývám tě k souboji tělo na tělo. Ukaž, co umíš.“", "Její oči zazáří bojovým nadšením. Respektuje tvou odvahu utkat se s ní jako rovný s rovným.", 22, trust = 15, loyalty = 15),
                    DialogueOption("„Tvá síla i tvá zbraň patří mně. Nauč se sklonit hlavu.“", "Zatne zuby, vzdoruje tvé moci, ale její instinkty ji nutí sklopit zrak.", 10, submissiveness = 15, broken = 6, fear = 5),
                    DialogueOption("„Tvá čepel je jen hračka. Jsi jen slabá žena v mé moci.“", "Hněvivě sevře jílec, ale tvá chladná převaha ji vnitřně znejistí.", -2, fear = 12, broken = 5)
                )
            )
            "sukuba" -> DialogueScenario(
                prompt = "„Můj pane... tvá duše voní tak lahodně a hříšně. Smím si vzít jen malý, sladký doušek tvé životní síly?“",
                options = listOf(
                    DialogueOption("„Spolkni svůj hlad. To já ovládám tvou démonickou podstatu.“", "Gaspne údivem, tvá vnitřní síla a nadvláda ji vzrušuje víc než samotná krev.", 22, submissiveness = 18, loyalty = 12, fear = 8),
                    DialogueOption("„Můžeš se napít, pokud mi za to slíbíš svou absolutní věrnost.“", "Sladce se usměje, její stíny se k tobě lísají jako věrný pes.", 16, trust = 18, loyalty = 15),
                    DialogueOption("„Zpátky, nestvůro! Tvůj vliv na mou mysl neplatí.“", "Odtáhne se s uraženým, ale nesmírně fascinovaným výrazem. Tvůj odpor ji přitahuje.", 12, fear = 12, submissiveness = 8)
                )
            )
            "draci_divka" -> DialogueScenario(
                prompt = "„Můj dračí duch se neskloní před nikým, kdo sám neprošel skutečným ohněm. Co jsi dokázal ty, smrtelníku?“",
                options = listOf(
                    DialogueOption("„Kráčím ohněm stínů a podmaňuji si říše. Ty se skloníš přede mnou.“", "Dlouze se ti zadívá do očí a nakonec pomalu skloní hlavu. Tvůj žhnoucí pohled ji přemohl.", 24, loyalty = 18, submissiveness = 12, broken = 6),
                    DialogueOption("„Chápu tvou pýchu, ale zde jsi v bezpečí. Společně vybudujeme neporazitelné dominium.“", "Její napjaté tělo se uvolní. V jejích očích se poprvé objeví hluboká důvěra.", 18, trust = 20, loyalty = 16),
                    DialogueOption("„Tvůj oheň bude sloužit mé obraně v řetězech, pokud se nepodvolíš dobrovolně.“", "Zavrčí a vyfoukne malý obláček dýmu, ale tvá nekompromisní ruka v ní vzbuzuje respekt k moci.", 12, submissiveness = 15, broken = 10, fear = 10)
                )
            )
            "nymfomanka" -> DialogueScenario(
                prompt = "„Pane... já se tak hrozně stydím. Moje tělo mě neposlouchá... každá tvá myšlenka nebo pohled mě uvádí do šílenství...“",
                options = listOf(
                    DialogueOption("„Pak ti dám přesně to, po čem tvá hříšná povaha touží.“", "Vykřikne potěšením a vrhá se k tvým nohám s nenasytným pohledem.", 20, submissiveness = 15, trust = 10),
                    DialogueOption("„Trestem za tvou nenasytnost bude odpírání mé přítomnosti.“", "Kňučí a škemrá o milost, její mysl je zcela fixovaná na tebe.", 14, submissiveness = 18, broken = 8, fear = 6),
                    DialogueOption("„Uklidni se a soustřeď svou energii na obranu našeho hradu.“", "Snaží se ovládnout svůj zrychlený dech, vděčná za tvé pevné vedení.", 12, loyalty = 15, trust = 12)
                )
            )
            "ticha_panenka" -> DialogueScenario(
                prompt = "„... (Sedí tiše se sklopenou hlavou, čeká na tvé slovo a jemně si pohrává s lemem šatů)“",
                options = listOf(
                    DialogueOption("„Pohlaď ji po vlasech a řekni: Jsi hodná holčička.“", "Její oči se rozsvítí čistou radostí a na tváři se jí objeví vzácný, tichý úsměv.", 18, trust = 18, loyalty = 15, submissiveness = 8),
                    DialogueOption("„Přikaž jí, aby klečela bez hnutí po zbytek večera.“", "Bez jediného slova okamžitě plní tvůj příkaz, její odevzdanost je absolutní.", 14, submissiveness = 20, fear = 8),
                    DialogueOption("„Zeptej se jí na její sny a minulost.“", "Na moment se ti podívá do očí s hlubokým smutkem, poté jen ticho zatřese hlavou.", 10, trust = 12, loyalty = 8)
                )
            )
            "krvava_subka" -> DialogueScenario(
                prompt = "„Pane... viděla jsem tvůj bič. Moje záda pálí touhou po tvém hněvu. Nech mě prosím krvácet pro tvou potěchu...“",
                options = listOf(
                    DialogueOption("„Vyhovět její žádosti a tvrdě ji potrestat.“", "S každým úderem její tvář plní extatický úsměv, bolest je pro ni nejvyšším darem.", 22, submissiveness = 18, broken = 8, fear = 5),
                    DialogueOption("„Odmítnout s tím, že její bolest patří jen mně a já rozhoduji, kdy ji dostane.“", "Třese se touhou, tvé odepření bolesti ji psychicky zcela podmaňuje.", 18, submissiveness = 20, loyalty = 12),
                    DialogueOption("„Ošetřit její staré šrámy teplým olejem.“", "Je zmatená tvou něhou, pláče vděčností, která zasahuje její nejhlubší nitro.", 16, trust = 22, loyalty = 15, submissiveness = -5)
                )
            )
            "posedla" -> DialogueScenario(
                prompt = "„Něco hluboko ve mně je zlomené... Jsem jako prázdná nádoba. Použij mě, můj pane, naplň mě svou temnotou, abych mohla existovat...“",
                options = listOf(
                    DialogueOption("„Budeš mou věrnou stínovou loutkou. Tvá mysl patří mně.“", "Její oči ztratí poslední zbytky pochybností, odevzdává se ti jako dokonalá schránka.", 24, submissiveness = 20, broken = 12, loyalty = 15),
                    DialogueOption("„Pomohu ti najít tvé ztracené já. Společně tě vyléčíme.“", "Chytí se tvé ruky jako stébla trávy v bouři, její loajalita se stává neochvějnou.", 16, trust = 24, loyalty = 20, submissiveness = -8),
                    DialogueOption("„Zneužít její prázdnoty a uvalit na ni těžkou práci.“", "Tiše a bez odporu plní úkoly, její prázdný pohled je zcela oddaný tvé vůli.", 12, submissiveness = 18, broken = 15, fear = 10)
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

