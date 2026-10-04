package com.example.haremdark.domain

import com.example.haremdark.data.StaticData
import com.example.haremdark.models.*
import kotlin.random.Random

/**
 * Procedural and curated Random Narrative Event Generator.
 * Creates rich branching encounters, skill-check dilemmas, and high-impact rewards.
 */
object RandomNarrativeEventGenerator {

    /**
     * Generates a contextual narrative event based on player level, active party, and random seed.
     */
    fun generateEvent(
        gameState: GameSave,
        preferredCategory: EventCategory? = null
    ): NarrativeEvent {
        val player = gameState.player
        val characters = gameState.characters
        val randomCompanion = characters.randomOrNull()?.name ?: "Tvá důvěrnice"

        val category = preferredCategory ?: EventCategory.entries.random()
        val rarity = pickRarity()

        return when (category) {
            EventCategory.WILDERNESS -> generateWildernessEvent(rarity, randomCompanion, player)
            EventCategory.METROPOLIS -> generateMetropolisEvent(rarity, randomCompanion, player)
            EventCategory.HAREM_COURT -> generateHaremCourtEvent(rarity, randomCompanion, player)
            EventCategory.OCCULT_ANOMALY -> generateOccultEvent(rarity, randomCompanion, player)
            EventCategory.INQUISITION -> generateInquisitionEvent(rarity, randomCompanion, player)
            EventCategory.UNDERGROUND -> generateUndergroundEvent(rarity, randomCompanion, player)
        }
    }

    private fun pickRarity(): EventRarity {
        val roll = Random.nextInt(100)
        return when {
            roll < 45 -> EventRarity.COMMON
            roll < 75 -> EventRarity.UNCOMMON
            roll < 90 -> EventRarity.RARE
            roll < 97 -> EventRarity.EPIC
            else -> EventRarity.LEGENDARY
        }
    }

    // --- 1. WILDERNESS ENCOUNTERS ---
    private fun generateWildernessEvent(rarity: EventRarity, companion: String, player: Player): NarrativeEvent {
        val events = listOf(
            NarrativeEvent(
                id = "wild_shrine_${System.currentTimeMillis()}",
                title = "Ztracená svatyně v mlze",
                icon = "🌲",
                category = EventCategory.WILDERNESS,
                rarity = rarity,
                locationTag = "Temný hvozd",
                narrativeStory = "Při průzkumu neprobádaných stezek narazila tvá družina na starobylý kamenný oltář porostlý světélkujícím mechem. $companion tě upozorňuje na temné pulzování vycházející ze štěrbiny pod podstavcem.",
                involvedCompanionName = companion,
                choices = listOf(
                    NarrativeEventChoice(
                        id = "shrine_sacrifice",
                        choiceText = "Obětovat temnou energii oltáři",
                        description = "Nalij 15 bodů Temné Energie do prastarých run a probuď spící požehnání.",
                        icon = "🔮",
                        costDarkEnergy = 15,
                        successOutcome = EventOutcome(
                            outcomeTitle = "Probuzení starých bohů",
                            outcomeNarrative = "Runy se rozzářily purpurovým plamenem! Oltář se otevřel a vydal prastarý poklad a esenci many.",
                            goldDelta = 140,
                            manaDelta = 45,
                            haremExp = 35,
                            playerXp = 30,
                            materialsReward = mapOf("stone" to 25, "wood" to 20),
                            logSummary = "Oltář odměnil tvou oběť 140 zlatými a esencí many."
                        )
                    ),
                    NarrativeEventChoice(
                        id = "shrine_explore",
                        choiceText = "Vypáčit kryptu násilím",
                        description = "Pokus se otevřít zapečetěnou komoru pomocí dovednosti temnoty a hrubé síly.",
                        icon = "⛏️",
                        skillCheck = SkillCheckRequirement("temnota", "Temná magie", reqLevel = 2, baseSuccessPercent = 75),
                        successOutcome = EventOutcome(
                            outcomeTitle = "Úspěšné prolomení pečetě",
                            outcomeNarrative = "Soustředěným úderem temné magie jsi rozbil ochrannou pečeť a nalezl vzácný artefakt!",
                            goldDelta = 200,
                            itemReward = InventoryItem("amulet_stinu", "Amulet stínů", "Prastarý šperk posilující magické útoky o 15%.", 1, 150, "artifact", "📿", "Vzácný", "+15% Magie"),
                            playerXp = 45,
                            haremExp = 40,
                            logSummary = "Prolomil jsi pečeť svatyně a získal Amulet stínů!"
                        ),
                        failureOutcome = EventOutcome(
                            outcomeTitle = "Magický protiúder",
                            outcomeNarrative = "Obranná kletba svatyně explodovala! Utrpěl jsi zranění a tvé zásoby energie byly poškozeny.",
                            playerXp = 10,
                            moraleDelta = -5,
                            darkEnergyDelta = 10,
                            logSummary = "Kletba svatyně selhala a zranila tě."
                        )
                    ),
                    NarrativeEventChoice(
                        id = "shrine_leave",
                        choiceText = "Zaznamenat souřadnice a odejít",
                        description = "Bezpečně se stáhni a zakresli polohu svatyně do mapy dominia.",
                        icon = "📜",
                        successOutcome = EventOutcome(
                            outcomeTitle = "Bezpečný návrat",
                            outcomeNarrative = "Zapsal jsi poznatky o svatyni do kroniky průzkumu a posílil reputaci obezřetného vůdce.",
                            reputationDelta = 8,
                            playerXp = 15,
                            logSummary = "Zaznamenal jsi svatyni do mapy dominia (+8 Reputace)."
                        )
                    )
                )
            ),
            NarrativeEvent(
                id = "wild_knightess_${System.currentTimeMillis()}",
                title = "Zraněná rytířka na útěku",
                icon = "🛡️",
                category = EventCategory.WILDERNESS,
                rarity = EventRarity.RARE,
                locationTag = "Okraj hvozdu",
                narrativeStory = "V houštině nacházíš mladou rytířku v pobořeném brnění se znakem královské gardy. Krvácí ze sečné rány a prchá před pronásledovateli z inkvizice. $companion navrhuje rozhodnout o jejím osudu.",
                involvedCompanionName = companion,
                choices = listOf(
                    NarrativeEventChoice(
                        id = "knight_recruit",
                        choiceText = "Ošetřit rány a nabídnout azyl v sídle",
                        description = "Využij léčivé zásoby a nabídni jí bezpečí v tvém harému pod přísahou věrnosti.",
                        icon = "💖",
                        costGold = 35,
                        costSexEnergy = 10,
                        successOutcome = EventOutcome(
                            outcomeTitle = "Nová bojovnice v harému",
                            outcomeNarrative = "Rytířka vděčně přijala tvé ošetření. Dojatá tvou velkorysostí přísahala svůj meč tvému dominiu!",
                            recruitedGirlArchetype = "bojovnice",
                            recruitedGirlName = "Valeria, Bývalá gardistka",
                            recruitedGirlRole = "Osobní strážkyně",
                            haremExp = 50,
                            playerXp = 40,
                            moraleDelta = 15,
                            logSummary = "Zachránil jsi Valerii a získal ji jako novou bojovnici harému!"
                        )
                    ),
                    NarrativeEventChoice(
                        id = "knight_ransom",
                        choiceText = "Vydat ji inkvizici za vysokou odměnu",
                        description = "Zajisti ji a předej inkvizičnímu komisaři výměnou za zlatý měšec a přízeň církve.",
                        icon = "💰",
                        successOutcome = EventOutcome(
                            outcomeTitle = "Krvavá odměna",
                            outcomeNarrative = "Inkvizice ti vyplatila tučnou odměnu za dopadení uprchlice. Vliv církve v kraji mírně stoupl.",
                            goldDelta = 260,
                            influenceDelta = 15,
                            moraleDelta = -8,
                            playerXp = 25,
                            logSummary = "Vydal jsi uprchlou rytířku inkvizici za 260 zlata."
                        )
                    ),
                    NarrativeEventChoice(
                        id = "knight_strip",
                        choiceText = "Zabavit její výstroj a nechat ji jít",
                        description = "Odzbroj ji, vezmi cenné železo a stříbro a ponech ji jejímu osudu.",
                        icon = "⚔️",
                        successOutcome = EventOutcome(
                            outcomeTitle = "Válečná kořist",
                            outcomeNarrative = "Zabavil jsi kvalitní ocelové brnění a zásoby, které poslouží v tvé zbrojnici.",
                            materialsReward = mapOf("iron" to 45, "stone" to 15),
                            goldDelta = 60,
                            playerXp = 20,
                            logSummary = "Získal jsi 45 železa a výstroj z rytířky."
                        )
                    )
                )
            )
        )
        return events.random()
    }

    // --- 2. METROPOLIS ENCOUNTERS ---
    private fun generateMetropolisEvent(rarity: EventRarity, companion: String, player: Player): NarrativeEvent {
        return NarrativeEvent(
            id = "metro_auction_${System.currentTimeMillis()}",
            title = "Tajná aukce v Černém salonu",
            icon = "🏛️",
            category = EventCategory.METROPOLIS,
            rarity = rarity,
            locationTag = "Horní město",
            narrativeStory = "Do tvých rukou se dostala zapečetěná pozvánka do podzemního salonu cechu pašeráků. Mezi šlechtici v maskách se draží vzácné zboží, zakázané grimoáry a zajaté dívky. $companion tě provází v přestrojení.",
            involvedCompanionName = companion,
            choices = listOf(
                NarrativeEventChoice(
                    id = "auction_bid",
                    choiceText = "Vydražit exotickou dívku z Východu",
                    description = "Přeplať místní boháče a získej tajemnou cizinku do svého sídla.",
                    icon = "👑",
                    costGold = 130,
                    successOutcome = EventOutcome(
                        outcomeTitle = "Triumf na aukci",
                        outcomeNarrative = "Za potlesku přihlížejících jsi získal exotickou dívku Lyannu, která ovládá starodávná kouzla.",
                        recruitedGirlArchetype = "carodejka",
                        recruitedGirlName = "Lyanna z Východních písků",
                        recruitedGirlRole = "Magická společnice",
                        influenceDelta = 20,
                        haremExp = 45,
                        playerXp = 35,
                        logSummary = "Vydražil jsi Lyannu z Východních písků (+20 Vliv, nová dívka)!"
                    )
                ),
                NarrativeEventChoice(
                    id = "auction_blackmail",
                    choiceText = "Koupit kompromitující spisy na šlechtu",
                    description = "Investuj zlato do tajných dokumentů odhalujících korupci městských radních.",
                    icon = "📜",
                    costGold = 75,
                    successOutcome = EventOutcome(
                        outcomeTitle = "Moc nad metropolí",
                        outcomeNarrative = "Získané dokumenty ti dávají obrovskou páku na městskou radu. Tvůj vliv v metropoli prudce stoupl!",
                        influenceDelta = 35,
                        reputationDelta = 15,
                        playerXp = 30,
                        logSummary = "Zakoupil jsi tajné spisy šlechty (+35 Vliv, +15 Reputace)!"
                    )
                ),
                NarrativeEventChoice(
                    id = "auction_steal",
                    choiceText = "Využít zmatku a vyloupit pokladnu",
                    description = "Použij dovednost vyjednávání a svádění k odlákání stráží a krádeži truhly.",
                    icon = "💰",
                    skillCheck = SkillCheckRequirement("vyjednavani", "Vyjednávání", reqLevel = 2, baseSuccessPercent = 70),
                    successOutcome = EventOutcome(
                        outcomeTitle = "Dokonalá loupež",
                        outcomeNarrative = "Zatímco $companion zaměstnala stráže, ty jsi nepozorovaně vynesl truhlu plnou zlaťáků a šperků!",
                        goldDelta = 240,
                        itemReward = InventoryItem("zlata_koruna", "Zlatá koruna", "Skvostná koruna z dražby.", 1, 120, "artifact", "👑", "Vzácný", "+15 Vliv"),
                        playerXp = 40,
                        haremExp = 30,
                        logSummary = "Vyloupil jsi aukční truhlu a získal 240 zlata a Zlatou korunu!"
                    ),
                    failureOutcome = EventOutcome(
                        outcomeTitle = "Odhaleni při krádeži!",
                        outcomeNarrative = "Stráže vás přistihly! Musel jsi zaplatit pokutu a ztratil část vlivu u místních syndikátů.",
                        goldDelta = -60,
                        influenceDelta = -15,
                        moraleDelta = -5,
                        logSummary = "Krádež na aukci selhala (-60 Zlato, -15 Vliv)."
                    )
                )
            )
        )
    }

    // --- 3. HAREM COURT ENCOUNTERS ---
    private fun generateHaremCourtEvent(rarity: EventRarity, companion: String, player: Player): NarrativeEvent {
        return NarrativeEvent(
            id = "harem_rivalry_${System.currentTimeMillis()}",
            title = "Žárlivost v nočních komnatách",
            icon = "💖",
            category = EventCategory.HAREM_COURT,
            rarity = rarity,
            locationTag = "Komnaty panství",
            narrativeStory = "Mezi dívkami v harému vypukl spor o to, kdo má právo strávit noc po tvém boku. $companion tě žádá o urovnání sporu a nastolení pořádku dříve, než napětí přeroste v otevřenou rivalitu.",
            involvedCompanionName = companion,
            choices = listOf(
                NarrativeEventChoice(
                    id = "harem_harmony_ritual",
                    choiceText = "Uspořádat společný rituál smíření",
                    description = "Pozvi obě soupeřky do lázní na noční rituál rozkoše a sjednoť jejich touhy.",
                    icon = "✨",
                    costSexEnergy = 20,
                    successOutcome = EventOutcome(
                        outcomeTitle = "Absolutní harmonie harému",
                        outcomeNarrative = "Společná noc plná vášně rozpustila veškerou zášť. Pouto mezi dívkami zesílilo a morálka harému dosáhla vrcholu!",
                        haremExp = 60,
                        moraleDelta = 25,
                        sexEnergyDelta = -15,
                        playerXp = 35,
                        logSummary = "Rituál smíření obnovil harmonii harému (+60 Harém EXP, +25 Morálka)!"
                    )
                ),
                NarrativeEventChoice(
                    id = "harem_strict_discipline",
                    choiceText = "Uplatnit tvrdou disciplínu a trest",
                    description = "Nekompromisně potlač vzdor, ulož přísný trest a upevni svou neotřesitelnou dominanci.",
                    icon = "⛓️",
                    skillCheck = SkillCheckRequirement("dominance", "Dominance", reqLevel = 1, baseSuccessPercent = 85),
                    successOutcome = EventOutcome(
                        outcomeTitle = "Poddajnost zjednána",
                        outcomeNarrative = "Tvůj přísný pohled a autorita zjednaly okamžitý klid. Dívky se s pokorou sklonily a jejich poslušnost vzrostla.",
                        moraleDelta = 10,
                        haremExp = 35,
                        playerXp = 25,
                        logSummary = "Zjednal jsi pořádek pevnou rukou (+10 Dominance)."
                    )
                ),
                NarrativeEventChoice(
                    id = "harem_lavish_gifts",
                    choiceText = "Uplatit obě strany luxusními parfémy",
                    description = "Rozděl mezi dívky drahé šperky a voňavé esence, aby zapomněly na hádky.",
                    icon = "🌹",
                    costGold = 50,
                    successOutcome = EventOutcome(
                        outcomeTitle = "Sladký klid",
                        outcomeNarrative = "Zlaté náramky a půlnoční parfémy vykouzlily na tvářích dívek úsměv. Hádka byla zažehnána.",
                        moraleDelta = 15,
                        haremExp = 30,
                        playerXp = 20,
                        logSummary = "Dary obnovily mír v harému (-50 Zlato, +15 Morálka)."
                    )
                )
            )
        )
    }

    // --- 4. OCCULT ANOMALY ENCOUNTERS ---
    private fun generateOccultEvent(rarity: EventRarity, companion: String, player: Player): NarrativeEvent {
        return NarrativeEvent(
            id = "occult_rift_${System.currentTimeMillis()}",
            title = "Trhlina v astrálním závoji",
            icon = "🔮",
            category = EventCategory.OCCULT_ANOMALY,
            rarity = EventRarity.EPIC,
            locationTag = "Podzemní rituální síň",
            narrativeStory = "Uprostřed noci se v komnatách otevřela pulzující purpurová trhlina, ze které prosakuje surová temná energie. $companion cítí, jak astrální vír přitahuje bytosti z jiných sfér.",
            involvedCompanionName = companion,
            choices = listOf(
                NarrativeEventChoice(
                    id = "occult_absorb",
                    choiceText = "Vstoupit do víru a pohltit temnou moc",
                    description = "Otevři své tělo proudu energie a nasaj prastarou magii přímo do svého jádra.",
                    icon = "⚡",
                    successOutcome = EventOutcome(
                        outcomeTitle = "Temná metamorfóza",
                        outcomeNarrative = "Síla stínů zaplavila tvé žíly! Získal jsi obrovské množství Temné Energie a esence many.",
                        darkEnergyDelta = 50,
                        manaDelta = 60,
                        playerXp = 50,
                        haremExp = 30,
                        logSummary = "Pohltil jsi astrální trhlinu (+50 Temná Energie, +60 Mana)!"
                    )
                ),
                NarrativeEventChoice(
                    id = "occult_ritual_seal",
                    choiceText = "Zpečetit trhlinu společným zaříkáváním",
                    description = "Využij magické schopnosti dívek a dovednost temnoty k bezpečnému uzavření portálu.",
                    icon = "🛡️",
                    skillCheck = SkillCheckRequirement("temnota", "Temná magie", reqLevel = 2, baseSuccessPercent = 80),
                    successOutcome = EventOutcome(
                        outcomeTitle = "Mistrovské zpečetění",
                        outcomeNarrative = "Dokonale zkoordinovaný rituál uzavřel trhlinu a zkrystalizoval zbytkovou energii do drahocenných krystalů.",
                        itemReward = InventoryItem("temny_krystal", "Temný krystal moci", "Vzácný krystal zvyšující temnou energii.", 1, 180, "artifact", "🔮", "Epický", "+25 Max TE"),
                        reputationDelta = 20,
                        influenceDelta = 15,
                        playerXp = 45,
                        logSummary = "Zpečetil jsi astrální trhlinu a získal Temný krystal moci!"
                    ),
                    failureOutcome = EventOutcome(
                        outcomeTitle = "Magické vyčerpání",
                        outcomeNarrative = "Rituál byl vyčerpávající a trhlina při kolapsu spálila část tvých zásob many.",
                        manaDelta = -20,
                        playerXp = 15,
                        logSummary = "Při pečetění došlo k výboji (-20 Mana)."
                    )
                )
            )
        )
    }

    // --- 5. INQUISITION ENCOUNTERS ---
    private fun generateInquisitionEvent(rarity: EventRarity, companion: String, player: Player): NarrativeEvent {
        return NarrativeEvent(
            id = "inq_inquisitor_${System.currentTimeMillis()}",
            title = "Inkviziční hlídka u bran",
            icon = "⚖️",
            category = EventCategory.INQUISITION,
            rarity = rarity,
            locationTag = "Vnější brána dominia",
            narrativeStory = "K tvému panství dorazila ozbrojená eskadra inkvizitorů v čele s přísným vyšetřovatelem. Tvrdí, že zachytili stopy zakázané magie a požadují okamžitou prohlídku komnat. $companion je připravena k boji.",
            involvedCompanionName = companion,
            choices = listOf(
                NarrativeEventChoice(
                    id = "inq_bribe",
                    choiceText = "Podplatit vyšetřovatele těžkým měšcem",
                    description = "Nabídni veliteli hlídky diskrétní dar ze zlata výměnou za čistý protokol.",
                    icon = "💰",
                    costGold = 100,
                    successOutcome = EventOutcome(
                        outcomeTitle = "Prohlídka odvolána",
                        outcomeNarrative = "Zlato spolehlivě umlčelo svaté pochybnosti. Inkvizitor s úsměvem podepsal bezúhonnost tvého panství.",
                        influenceDelta = 15,
                        reputationDelta = 10,
                        inquisitionAlertDelta = -10,
                        playerXp = 25,
                        logSummary = "Podplatil jsi inkvizici (-100 Zlato, sníženo podezření)."
                    )
                ),
                NarrativeEventChoice(
                    id = "inq_seduce_turn",
                    choiceText = "Nalákat velitele do salonu a uhranout ho",
                    description = "Využij svádění a omamné afrodiziakum k získání vlivu a informátora přímo v řadách inkvizice.",
                    icon = "🍷",
                    skillCheck = SkillCheckRequirement("svadeni", "Svádění", reqLevel = 2, baseSuccessPercent = 75),
                    successOutcome = EventOutcome(
                        outcomeTitle = "Dvojitý agent v inkvizici",
                        outcomeNarrative = "Pod vlivem nočních rozkoší a magie velitel propadl tvému kouzlu. Nyní ti bude pravidelně donášet!",
                        influenceDelta = 35,
                        reputationDelta = 20,
                        haremExp = 40,
                        playerXp = 45,
                        logSummary = "Získal jsi vysoce postaveného špeha v inkvizici (+35 Vliv)!"
                    ),
                    failureOutcome = EventOutcome(
                        outcomeTitle = "Odhalená lest",
                        outcomeNarrative = "Inkvizitor prohlédl tvůj záměr a hrozil zatčením. Musel jsi zaplatit vysokou pokutu.",
                        goldDelta = -140,
                        influenceDelta = -10,
                        inquisitionAlertDelta = 15,
                        logSummary = "Lest na inkvizitora selhala (-140 Zlato, +Podezření)."
                    )
                )
            )
        )
    }

    // --- 6. UNDERGROUND ENCOUNTERS ---
    private fun generateUndergroundEvent(rarity: EventRarity, companion: String, player: Player): NarrativeEvent {
        return NarrativeEvent(
            id = "und_syndicate_${System.currentTimeMillis()}",
            title = "Zúčtování v nočních docích",
            icon = "🗡️",
            category = EventCategory.UNDERGROUND,
            rarity = rarity,
            locationTag = "Měsíční přístav",
            narrativeStory = "Pašerácký syndikát z doků zadržel zásilku zbraní a stavebního materiálu určenou pro tvé dominium. $companion vypátrala jejich tajné skladiště za přístavními sklady.",
            involvedCompanionName = companion,
            choices = listOf(
                NarrativeEventChoice(
                    id = "und_assault",
                    choiceText = "Přepadnout skladiště a zabavit veškeré zboží",
                    description = "Vtrhni se svou gardou do skladu a silou získej suroviny i zlato.",
                    icon = "⚔️",
                    skillCheck = SkillCheckRequirement("boj", "Bojové umění", reqLevel = 2, baseSuccessPercent = 80),
                    successOutcome = EventOutcome(
                        outcomeTitle = "Drtivé vítězství v docích",
                        outcomeNarrative = "Pašeráci se rozprchli! Získal jsi zpět své suroviny a navíc vyraboval jejich tajnou pokladnici.",
                        goldDelta = 180,
                        materialsReward = mapOf("wood" to 60, "iron" to 35, "stone" to 20),
                        playerXp = 40,
                        haremExp = 30,
                        logSummary = "Dobil jsi pašerácký sklad (+180 Zlato, +60 Dřevo, +35 Železo)!"
                    ),
                    failureOutcome = EventOutcome(
                        outcomeTitle = "Tvrdý odpor",
                        outcomeNarrative = "Pašeráci kladli tuhý odpor. Podařilo se ti získat část surovin, ale utrpěl jsi ztráty.",
                        materialsReward = mapOf("wood" to 20),
                        moraleDelta = -5,
                        playerXp = 15,
                        logSummary = "Střet v docích přinesl částečnou kořist (+20 Dřevo)."
                    )
                ),
                NarrativeEventChoice(
                    id = "und_negotiate_tax",
                    choiceText = "Vyjednat trvalý tribut a ochranu",
                    description = "Použij diplomacii a nabídni syndikátu ochranu před městskou gardou výměnou za pravidelný podíl.",
                    icon = "🤝",
                    skillCheck = SkillCheckRequirement("vyjednavani", "Vyjednávání", reqLevel = 2, baseSuccessPercent = 75),
                    successOutcome = EventOutcome(
                        outcomeTitle = "Nová mafiánská smlouva",
                        outcomeNarrative = "Syndikát uznal tvou svrchovanost. Tvé dominium nyní dostává pravidelné dodávky a tvůj vliv v podsvětí vzrostl.",
                        influenceDelta = 30,
                        goldDelta = 120,
                        materialsReward = mapOf("iron" to 25),
                        playerXp = 35,
                        logSummary = "Uzavřel jsi spojenectví se syndikátem (+30 Vliv, +120 Zlato)!"
                    )
                )
            )
        )
    }
}
