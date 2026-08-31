package com.example.haremdark.data

import com.example.haremdark.models.*

data class GameInteraction(
    val id: String,
    val name: String,
    val type: String, // "odmena", "trest", "intimni", "alchymie"
    val description: String,
    val energyCost: Int = 0,
    val darkCost: Int = 0,
    val goldCost: Int = 0,
    val minPhase: Int = 0,
    val requiresPartner: Boolean = false,
    val requiresWife: Boolean = false,
    val requiresFavorite: Boolean = false,
    val effectDescription: String,
    val applyEffect: (Concubine, Player) -> String
)

object GameContent {

    val REWARDS = listOf(
        GameInteraction(
            id = "drobna",
            name = "Drobná náklonnost",
            type = "odmena",
            description = "Pohladění po tváři, tichá pochvala a prst ve vlasech.",
            energyCost = 2,
            effectDescription = "+5 Loajalita, +4 Důvěra, +3 Touha, -3 Strach",
            applyEffect = { c, _ ->
                c.loajalita = (c.loajalita + 5).coerceAtMost(100)
                c.duvera = (c.duvera + 4).coerceAtMost(100)
                c.touha = (c.touha + 3).coerceAtMost(100)
                c.strach = (c.strach - 3).coerceAtLeast(0)
                "${c.name} se při tvém jemném doteku tiše zachvěla a její oči roztály vděčností."
            }
        ),
        GameInteraction(
            id = "pochvala",
            name = "Veřejná pochvala",
            type = "odmena",
            description = "Před celým harémem vyzdvihneš její poslušnost a půvab.",
            energyCost = 4,
            effectDescription = "+8 Loajalita, +5 Důvěra, +4 Ponížení, +3 Submisivita",
            applyEffect = { c, _ ->
                c.loajalita = (c.loajalita + 8).coerceAtMost(100)
                c.duvera = (c.duvera + 5).coerceAtMost(100)
                c.humiliation = (c.humiliation + 4).coerceAtMost(100)
                c.submisivita = (c.submisivita + 3).coerceAtMost(100)
                "${c.name} zčervenala před zraky ostatních a s poklonou přijala tvou veřejnou chválu."
            }
        ),
        GameInteraction(
            id = "dar",
            name = "Luxusní dar",
            type = "odmena",
            description = "Šperk, jemné hedvábí či parfém podtrhující její postavení v harému.",
            goldCost = 40,
            effectDescription = "+9 Loajalita, +7 Důvěra, +6 Srdce, +5 Touha, -5 Strach",
            applyEffect = { c, p ->
                p.gold = (p.gold - 40).coerceAtLeast(0)
                c.loajalita = (c.loajalita + 9).coerceAtMost(100)
                c.duvera = (c.duvera + 7).coerceAtMost(100)
                c.srdce = (c.srdce + 6).coerceAtMost(100)
                c.touha = (c.touha + 5).coerceAtMost(100)
                c.strach = (c.strach - 5).coerceAtLeast(0)
                "${c.name} s rozechvěním přijala tvůj dar a tiskne si ho na hruď jako znak tvé přízně."
            }
        ),
        GameInteraction(
            id = "privilegium",
            name = "Privilegium postele",
            type = "odmena",
            description = "Smí spát celou noc po tvém boku v pánově ložnici.",
            energyCost = 12,
            minPhase = 1,
            effectDescription = "+14 Loajalita, +12 Důvěra, +10 Srdce, +8 Touha, -8 Strach",
            applyEffect = { c, _ ->
                c.loajalita = (c.loajalita + 14).coerceAtMost(100)
                c.duvera = (c.duvera + 12).coerceAtMost(100)
                c.srdce = (c.srdce + 10).coerceAtMost(100)
                c.touha = (c.touha + 8).coerceAtMost(100)
                c.strach = (c.strach - 8).coerceAtLeast(0)
                "${c.name} strávila noc schoulená u tvého těla a její srdce bije jen pro tebe."
            }
        ),
        GameInteraction(
            id = "vzacna",
            name = "Vzácná noc rozkoše",
            type = "odmena",
            description = "Celá noc vyhrazená jen pro ni, doprovázená vínem a slastí.",
            goldCost = 80,
            energyCost = 20,
            minPhase = 2,
            effectDescription = "+20 Loajalita, +16 Důvěra, +14 Srdce, +12 Touha, +6 Submisivita",
            applyEffect = { c, p ->
                p.gold = (p.gold - 80).coerceAtLeast(0)
                c.loajalita = (c.loajalita + 20).coerceAtMost(100)
                c.duvera = (c.duvera + 16).coerceAtMost(100)
                c.srdce = (c.srdce + 14).coerceAtMost(100)
                c.touha = (c.touha + 12).coerceAtMost(100)
                c.submisivita = (c.submisivita + 6).coerceAtMost(100)
                "${c.name} prožila noc plnou extáze a její oddanost tvému dominium dosáhla nových výšin."
            }
        ),
        GameInteraction(
            id = "spolecna_koupel",
            name = "Společná koupel s oleji",
            type = "odmena",
            description = "Voňavá lázeň, masáž horkými oleji a tichá intimita.",
            goldCost = 25,
            energyCost = 10,
            minPhase = 1,
            effectDescription = "+14 Důvěra, +10 Srdce, +9 Loajalita, +6 Touha, -10 Strach",
            applyEffect = { c, p ->
                p.gold = (p.gold - 25).coerceAtLeast(0)
                c.duvera = (c.duvera + 14).coerceAtMost(100)
                c.srdce = (c.srdce + 10).coerceAtMost(100)
                c.loajalita = (c.loajalita + 9).coerceAtMost(100)
                c.touha = (c.touha + 6).coerceAtMost(100)
                c.strach = (c.strach - 10).coerceAtLeast(0)
                "${c.name} ti ve vodní lázni pečlivě omývala tělo a s úctou se poddala tvým dotekům."
            }
        ),
        GameInteraction(
            id = "privilegium_oblibene",
            name = "Privilegium oblíbenkyně ★",
            type = "odmena",
            description = "Exkluzivní péče a pocty vyhrazené pouze pro tvou zvolenou oblíbenkyni.",
            energyCost = 10,
            goldCost = 25,
            requiresFavorite = true,
            effectDescription = "+16 Loajalita, +12 Důvěra, +10 Touha, +8 Srdce",
            applyEffect = { c, p ->
                p.gold = (p.gold - 25).coerceAtLeast(0)
                c.loajalita = (c.loajalita + 16).coerceAtMost(100)
                c.duvera = (c.duvera + 12).coerceAtMost(100)
                c.touha = (c.touha + 10).coerceAtMost(100)
                c.srdce = (c.srdce + 8).coerceAtMost(100)
                "★ Jako tvá vyvolená oblíbenkyně ${c.name} září pýchou a oddaností."
            }
        ),
        GameInteraction(
            id = "manzelska_noc",
            name = "Manželská svátost 💍",
            type = "odmena",
            description = "Noc vyhrazená jen manželskému svazku a prohloubení věčného pouta.",
            energyCost = 22,
            requiresWife = true,
            effectDescription = "+28 Loajalita, +22 Důvěra, +20 Srdce, +12 Touha",
            applyEffect = { c, _ ->
                c.loajalita = (c.loajalita + 28).coerceAtMost(100)
                c.duvera = (c.duvera + 22).coerceAtMost(100)
                c.srdce = (c.srdce + 20).coerceAtMost(100)
                c.touha = (c.touha + 12).coerceAtMost(100)
                "💍 Tvá manželka ${c.name} v tvém náručí znovu stvrdila slib věrnosti na život a na smrt."
            }
        )
    )

    val PUNISHMENTS = listOf(
        GameInteraction(
            id = "lehky_trest",
            name = "Lehký trest (Spoutání & pokárání)",
            type = "trest",
            description = "Krátké spoutání rukou za záda a přísné pokárání za neposlušnost.",
            energyCost = 5,
            darkCost = 0,
            effectDescription = "+8 Strach, +6 Submisivita, +5 Ponížení",
            applyEffect = { c, _ ->
                c.strach = (c.strach + 8).coerceAtMost(100)
                c.submisivita = (c.submisivita + 6).coerceAtMost(100)
                c.humiliation = (c.humiliation + 5).coerceAtMost(100)
                c.hp = (c.hp - 2).coerceAtLeast(1)
                "${c.name} se po pokárání tiše třese v koutě se sklopenou hlavou."
            }
        ),
        GameInteraction(
            id = "vyprask",
            name = "Střední trest (Výprask bičíkem)",
            type = "trest",
            description = "Důrazný výprask na holou kůži pro upevnění autority pána.",
            energyCost = 10,
            darkCost = 3,
            effectDescription = "+14 Strach, +12 Submisivita, +12 Ponížení, +6 Závislost na bolesti, +3 Zlomení",
            applyEffect = { c, _ ->
                c.strach = (c.strach + 14).coerceAtMost(100)
                c.submisivita = (c.submisivita + 12).coerceAtMost(100)
                c.humiliation = (c.humiliation + 12).coerceAtMost(100)
                c.painAddiction = (c.painAddiction + 6).coerceAtMost(100)
                c.broken = (c.broken + 3).coerceAtMost(100)
                c.hp = (c.hp - 6).coerceAtLeast(1)
                "${c.name} pod ranami pláče a prosí o slitování tvého dominia."
            }
        ),
        GameInteraction(
            id = "bicovani",
            name = "Tvrdý trest (Krvavý bič & temná cela)",
            type = "trest",
            description = "Tvrdé bičování zanechávající stopy a následná izolace v temné kobce.",
            energyCost = 15,
            darkCost = 8,
            effectDescription = "+20 Strach, +18 Submisivita, +16 Ponížení, +12 Bolest, +8 Zlomení, +6 Jizvy",
            applyEffect = { c, _ ->
                c.strach = (c.strach + 20).coerceAtMost(100)
                c.submisivita = (c.submisivita + 18).coerceAtMost(100)
                c.humiliation = (c.humiliation + 16).coerceAtMost(100)
                c.painAddiction = (c.painAddiction + 12).coerceAtMost(100)
                c.broken = (c.broken + 8).coerceAtMost(100)
                c.scarred = (c.scarred + 6).coerceAtMost(100)
                c.hp = (c.hp - 14).coerceAtLeast(1)
                "${c.name} byla zbičována do krve. Její vůle vzdorovat byla těžce nalomena."
            }
        ),
        GameInteraction(
            id = "extremni_trest",
            name = "Extrémní trest (Zlomení mysli & cejch)",
            type = "trest",
            description = "Temný rituál naprosté degradace, asfyxie a vtlačení cejchu vlastnictví.",
            energyCost = 20,
            darkCost = 15,
            effectDescription = "+28 Strach, +22 Submisivita, +20 Ponížení, +18 Bolest, +15 Zlomení, +8 Mindbreak, Cejch pána",
            applyEffect = { c, p ->
                c.strach = (c.strach + 28).coerceAtMost(100)
                c.submisivita = (c.submisivita + 22).coerceAtMost(100)
                c.humiliation = (c.humiliation + 20).coerceAtMost(100)
                c.painAddiction = (c.painAddiction + 18).coerceAtMost(100)
                c.broken = (c.broken + 15).coerceAtMost(100)
                c.mindbreak = (c.mindbreak + 8).coerceAtMost(100)
                c.scarred = (c.scarred + 12).coerceAtMost(100)
                c.ownedMark = true
                c.hp = (c.hp - 24).coerceAtLeast(1)
                p.inquisitionInfluence = (p.inquisitionInfluence + 4).coerceAtMost(100)
                "${c.name} má na těle vypálený tvůj znak. Její oči ztratily poslední jiskru odporu."
            }
        )
    )

    val INTIMATE = listOf(
        GameInteraction(
            id = "svadeni",
            name = "Pánovo svádění",
            type = "intimni",
            description = "Pomalé laškování, dráždivé šeptání a probuzení touhy.",
            energyCost = 8,
            effectDescription = "+12 Touha, +15 Vlhkost, +6 Důvěra, +5 Submisivita",
            applyEffect = { c, _ ->
                c.touha = (c.touha + 12).coerceAtMost(100)
                c.vlhkost = (c.vlhkost + 15).coerceAtMost(100)
                c.duvera = (c.duvera + 6).coerceAtMost(100)
                c.submisivita = (c.submisivita + 5).coerceAtMost(100)
                "${c.name} těžce oddychuje a její tělo plně reaguje na tvé dotyky."
            }
        ),
        GameInteraction(
            id = "eroticka_noc",
            name = "Noc tělesné rozkoše",
            type = "intimni",
            description = "Nespoutaná noc v komnatách pána, prohlubující tělesné pouto a šanci na potomka.",
            energyCost = 18,
            darkCost = 2,
            effectDescription = "+20 Touha, +15 Loajalita, +10 Submisivita, šance na otěhotnění",
            applyEffect = { c, _ ->
                c.touha = (c.touha + 20).coerceAtMost(100)
                c.loajalita = (c.loajalita + 15).coerceAtMost(100)
                c.submisivita = (c.submisivita + 10).coerceAtMost(100)
                if (!c.tehotna && (1..100).random() <= c.plodnost) {
                    c.tehotna = true
                    c.dnyTehotenstvi = 0
                    "${c.name} se zcela odevzdala tvé vášni. V jejím lůně počal nový život!"
                } else {
                    "${c.name} se po noci plné vášně vyčerpaně a šťastně tiskne k tvému rameni."
                }
            }
        ),
        GameInteraction(
            id = "mindbreak_ritual",
            name = "Rituál temné hypnózy",
            type = "intimni",
            description = "Použití temné energie k přepsání jejího vnímání světa v tvůj prospěch.",
            energyCost = 15,
            darkCost = 12,
            minPhase = 3,
            effectDescription = "+12 Mindbreak, +10 Zlomení, +15 Submisivita, +10 Loajalita",
            applyEffect = { c, _ ->
                c.mindbreak = (c.mindbreak + 12).coerceAtMost(100)
                c.broken = (c.broken + 10).coerceAtMost(100)
                c.submisivita = (c.submisivita + 15).coerceAtMost(100)
                c.loajalita = (c.loajalita + 10).coerceAtMost(100)
                "${c.name} v transu opakuje tvé jméno jako jedinou modlitbu."
            }
        )
    )

    val BOSSES = listOf(
        Boss(
            id = "strazce_hvezdne_brany",
            name = "Strážce hvězdné brány",
            location = "Observatoř",
            hp = 145,
            maxHp = 145,
            attack = 15,
            defense = 10,
            rewardGold = 420,
            rewardXp = 140,
            phaseName = "Světelný štít",
            description = "Mocný obránce starobylé observatoře střežící astronomické vědomosti a magické relikvie."
        ),
        Boss(
            id = "kapitan_zeleznich_flotily",
            name = "Kapitán železné flotily",
            location = "Molo měsíčního přístavu",
            hp = 185,
            maxHp = 185,
            attack = 19,
            defense = 13,
            rewardGold = 560,
            rewardXp = 190,
            phaseName = "Ocelová paluba",
            description = "Otrlý námořní velitel kontrolující pašerácké trasy a přepravu vzácných zajatkyň."
        ),
        Boss(
            id = "inkvizitor_cerne_peceti",
            name = "Inkvizitor Černé pečeti",
            location = "Severní hranice dominia",
            hp = 220,
            maxHp = 220,
            attack = 24,
            defense = 16,
            rewardGold = 750,
            rewardXp = 250,
            phaseName = "Svatý oheň a černá pečeť",
            description = "Fanatický vysoký inkvizitor pověřený vyhlazením tvého temného kultu a osvobozením otrokyň."
        )
    )

    val QUESTS = listOf(
        Quest(
            id = "quest_1",
            title = "Založení harémového dominia",
            category = "Příběh",
            description = "Shromáždi ve své pevnosti alespoň 2 otrokyně a vybuduj Harémové komnaty.",
            reqLevel = 1,
            reqConcubines = 2,
            rewardGold = 250,
            rewardXp = 100,
            rewardReputation = 5
        ),
        Quest(
            id = "quest_2",
            title = "Vliv v podsvětí",
            category = "Podsvětí",
            description = "Ovládni alespoň 3 mafiánská území a vycvič špiona.",
            reqLevel = 2,
            reqGold = 100,
            rewardGold = 400,
            rewardXp = 180,
            rewardDarkEnergy = 25
        ),
        Quest(
            id = "quest_3",
            title = "Zlomení urozené krve",
            category = "Příběh",
            description = "Přiveď alespoň jednu otrokyni do fáze zkázanosti 4 a jmenuj svou oblíbenkyni.",
            reqLevel = 3,
            rewardGold = 600,
            rewardXp = 260,
            rewardReputation = 10
        ),
        Quest(
            id = "quest_4",
            title = "Úplatek inkvizičního tribunálu",
            category = "Inkvizice",
            description = "Sníž vliv inkvizice a upevni své politické postavení ve městě.",
            reqLevel = 4,
            reqGold = 500,
            rewardGold = 850,
            rewardXp = 350,
            rewardDarkEnergy = 40
        )
    )

    val ALCHEMY_RECIPES = listOf(
        AlchemyRecipe(
            id = "brew_touha",
            name = "Elixír divoké touhy",
            description = "Bylinný odvar vyvolávající okamžité vzrušení a poddajnost.",
            goldCost = 35,
            darkCost = 5,
            resultItem = InventoryItem("elixir_touhy", "Elixír touhy", "Okamžitě zvyšuje touhu a vlhkost.", 1, 40)
        ),
        AlchemyRecipe(
            id = "brew_healing",
            name = "Regenerační temný balzám",
            description = "Hojí zranění po bojích i přísných trestech.",
            goldCost = 25,
            darkCost = 4,
            resultItem = InventoryItem("hojivy_balzam", "Hojivý balzám", "Uzdravuje 35 HP.", 1, 25)
        ),
        AlchemyRecipe(
            id = "brew_poslusnost",
            name = "Sérum absolutní poslušnosti",
            description = "Koncentrovaná esence podlamující vůli vzdorovat.",
            goldCost = 70,
            darkCost = 15,
            resultItem = InventoryItem("serum_poslusnost", "Sérum poslušnosti", "Trvale posiluje loajalitu a submisivitu.", 1, 90)
        )
    )

    fun createInitialSave(): GameSave {
        val initialConcubines = listOf(
            Concubine(
                id = "c_1",
                name = "Valeria",
                age = 21,
                archetypeId = "slechticna",
                hp = 95,
                maxHp = 100,
                srdce = 65,
                poslusnost = 35,
                vlhkost = 45,
                submisivita = 40,
                loajalita = 35,
                duvera = 30,
                touha = 55,
                strach = 25,
                broken = 15,
                fazeZkazenosti = 1,
                role = "Urozená zajatkyně"
            ),
            Concubine(
                id = "c_2",
                name = "Lilith",
                age = 19,
                archetypeId = "touha",
                hp = 100,
                maxHp = 100,
                srdce = 75,
                poslusnost = 50,
                vlhkost = 65,
                submisivita = 60,
                loajalita = 55,
                duvera = 45,
                touha = 75,
                strach = 15,
                broken = 25,
                fazeZkazenosti = 2,
                role = "Dívka rozkoše",
                oblibena = true
            )
        )

        val buildings = listOf(
            Building("komnaty", 1, "Harémové komnaty", "Ubytování pro otrokyně a soukromé ložnice pána.", 150),
            Building("lazne", 1, "Voňavé lázně", "Regenerují HP otrokyň a zvyšují jejich vlhkost.", 200),
            Building("mucirna", 0, "Temná kobka & mučírna", "Zvyšuje efektivitu trestů a zisk temné energie.", 250),
            Building("laborator", 0, "Alchymistická laboratoř", "Umožňuje vaření sér, elixírů a afrodisiak.", 300),
            Building("zahrady", 0, "Zahrady rozkoše", "Poskytují pasivní bonus k náladě a loajalitě harému.", 350)
        )

        val territories = listOf(
            MafiaTerritory("trznice", "Centrální tržnice", 1, 60, 45),
            MafiaTerritory("doky", "Přístavní doky", 1, 50, 60),
            MafiaTerritory("nocni_ctvrt", "Noční čtvrť nevěstinců", 0, 40, 75),
            MafiaTerritory("zlata_ulicka", "Zlatá ulička lichvářů", 0, 30, 100),
            MafiaTerritory("podsveti", "Hluboké podsvětí", 0, 20, 140)
        )

        return GameSave(
            saveDate = "Den 1 - Založení dominia",
            slotNumber = 1,
            player = Player(),
            concubines = initialConcubines,
            haremLevel = 1,
            haremExp = 25,
            haremMaxExp = 100,
            buildings = buildings,
            territories = territories,
            defeatedBosses = emptyList(),
            currentTheme = "Temné dominium",
            completedQuests = emptyList(),
            gameLog = listOf(
                "Temné dominium bylo založeno v srdci staré pevnosti.",
                "Valeria a Lilith byly uvedeny do tvých harémových komnat."
            )
        )
    }
}
