package com.example.haremdark.data

import com.example.haremdark.models.GameSave

data class CodexEntry(
    val id: String,
    val category: String, // "Lore", "Bestiář", "Postavy"
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: String = "📜",
    val unlockCondition: String = "Neznámé"
)

object CodexData {
    val ENTRIES = listOf(
        CodexEntry(
            "lore_world",
            "Lore",
            "Zlomený svět",
            "Počátky věčné noci",
            "Svět upadl do věčné temnoty poté, co Sluneční císař podlehl korupci. Města se proměnila v ruiny, ve kterých přežívají jen ti nejsilnější, nebo ti, kteří se podvolili Temným pánům.",
            "🌑",
            "Odemčeno od začátku"
        ),
        CodexEntry(
            "lore_magic",
            "Lore",
            "Krvavá magie",
            "Zdroj moci",
            "Magie v této éře již nečerpá z many světa, ale z esence samotného života a utrpení. Krvavá magie vyžaduje oběti, ale její síla je absolutní.",
            "🩸",
            "Objevuje se při prvním rituálu"
        ),
        CodexEntry(
            "bestiary_goblin",
            "Bestiář",
            "Zkažený Skřet",
            "Běžná havěť",
            "Tvorové, kteří kdysi obývali hluboké jeskyně. Temnota je zmutovala v krvelačné bestie. Mají ostré drápy a útočí v houfech.",
            "👹",
            "Poraz svého prvního nepřítele"
        ),
        CodexEntry(
            "bestiary_vampire",
            "Bestiář",
            "Noční Stín (Upír)",
            "Děti Noci",
            "Vznešení a smrtelně nebezpeční. Upíři ovládají umění iluzí a pijí krev svých obětí. Jejich kůže je odolná proti běžným zbraním.",
            "🦇",
            "Poraz prvního bosse (Upířího Lorda)"
        ),
        CodexEntry(
            "npc_merchant",
            "Postavy",
            "Záhadný Kupec",
            "Obchodník s dušemi",
            "Nikdo neví, odkud se bere, ale vždy má to, co potřebuješ... za odpovídající cenu. Říká se, že neslouží žádnému pánovi, pouze zlatu.",
            "🎒",
            "Získej alespoň 1000 zlatých"
        ),
        CodexEntry(
            "npc_inquisitor",
            "Postavy",
            "Veleinkvizitorka",
            "Soudkyně slabých",
            "Vede Inkviziční legii. Je neúprosná a trestá každý projev slabosti. Mnoho dívek z tvého harému pochází z jejích 'očistných' tažení.",
            "⚔️",
            "Pronajmi dívku Inkviziční legii"
        ),
        CodexEntry(
            "item_elixir",
            "Předměty",
            "Elixír touhy",
            "Alchymistické afrodiziakum",
            "Tento fialově světélkující roztok dokáže probudit touhu i v tom nejchladnějším srdci. Je klíčovým nástrojem pro prohlubování pouta s dívkami.",
            "🔮",
            "Získej první Elixír touhy"
        ),
        CodexEntry(
            "item_collar",
            "Předměty",
            "Zlatý obojek",
            "Symbol vlastnictví",
            "Obojek vytepaný z ryzího zlata a posázený rubíny. Pro dívku, která jej nosí, znamená absolutní odevzdání se svému pánu.",
            "👑",
            "Získej Legendární zlatý obojek"
        ),
        CodexEntry(
            "event_eclipse",
            "Události",
            "Zatmění věčnosti",
            "Den, kdy Slunce zemřelo",
            "Před sto lety nastalo úplné zatmění, které nikdy neskončilo. Od té doby vládne světu Temnota a monstra z podsvětí.",
            "🌑",
            "Přečti si o historii světa"
        ),
        CodexEntry(
            "event_harem_open",
            "Události",
            "Otevření komnat",
            "Počátek tvého vzestupu",
            "Den, kdy jsi poprvé vstoupil do svého sídla a přijal první dívku. Byl to první krok k vybudování tvého Temného Dominia.",
            "🗝️",
            "Odemkni svou první postavu"
        ),
        // Elemental Affinities
        CodexEntry(
            "element_fire",
            "Živly",
            "Oheň – Prvotní Plamen Popela",
            "Žár Věčného Ohně a popela",
            "Prvotní oheň byl zažehnut Slunečním fénixem Ignisem ještě v První Éře. V temném světě se stáhl do hlubin lávových žil a krve bojovníků. Působí status Spálení (Burn) a taví krunýře.",
            "🔥",
            "Dokonči elementární trénink Ohně v komnatách afinity"
        ),
        CodexEntry(
            "element_water",
            "Živly",
            "Voda – Bezedný Proud Zapomnění",
            "Pramen života, hlubin a neurotoxinů",
            "Zrozeno ze slz bohyně Nereidy. Voda v temné éře představuje neúprosný proud obrušující skály i smrtící jed. Přináší status Otravy (Poison) a regeneraci many družiny.",
            "💧",
            "Dokonči elementární trénink Vody v komnatách afinity"
        ),
        CodexEntry(
            "element_earth",
            "Živly",
            "Země – Nezlomný Titánův Monolit",
            "Žula, rudy a kosti padlých civilizací",
            "Odštěpek ze žulového těla titána Gorgonotha. Minerální paměť tisíců padlých říší propůjčuje absolutní redukci plošného zranění a uzemnění elektrických výbojů.",
            "🪨",
            "Dokonči elementární trénink Země v komnatách afinity"
        ),
        CodexEntry(
            "element_air",
            "Živly",
            "Vzduch – Vichřice Neviditelných Čepelí",
            "Dech svobody a bleskový reflex",
            "Pozůstatky Nebeských věží roztrhané pánem vichřic Vaelthasem. Poskytuje neuvěřitelnou šanci na úskok, zrychlení tahů a způsobuje vyražením dechu status Omráčení (Stun).",
            "🌪️",
            "Dokonči elementární trénink Vzduchu v komnatách afinity"
        ),
        CodexEntry(
            "element_ice",
            "Živly",
            "Led – Věčný Permafrost Absolutní Nuly",
            "Zastavený čas a krystalická kontrola",
            "Zbytky mrazivého srdce boha Moroze z okamžiku Věčného Zatmění. Krystalizuje vlhkost v okolí, drtí svaly mrazivou křečí a spouští status Zmrazení (Freeze).",
            "❄️",
            "Dokonči elementární trénink Ledu v komnatách afinity"
        ),
        CodexEntry(
            "element_lightning",
            "Živly",
            "Blesk – Bouřný Úder Nebeského Kladiva",
            "Elektrická bouře a bleskové tempo",
            "Jiskra z rozbitého Nebeského kovadla titánky Astrape. Blesk je myšlenka vesmíru koncentrovaná do mikrosekundy. Spouští status Šok (Shock) a řetězové blesky mezi nepřáteli.",
            "⚡",
            "Dokonči elementární trénink Blesku v komnatách afinity"
        ),
        CodexEntry(
            "element_dark",
            "Živly",
            "Temnota – Hladová Prázdnota Propasti",
            "Vysávání duší a kletby stínů",
            "Živoucí hladová esence vržená padlým Sluncem a arcidémonem Malakorem. Proniká fyzickými štíty, saje vitalitu nepřátel (Lifesteal) a zesiluje Doménovou expanzi.",
            "🔮",
            "Dokonči elementární trénink Temnoty v komnatách afinity"
        ),
        CodexEntry(
            "element_holy",
            "Živly",
            "Světlo – Svatá Záře Ztraceného Úsvitu",
            "Očistný paprsek a ochrana serafů",
            "Fragmenty zrcadla Slunečního chrámu chráněné serafkou Solarií. Spaluje démony a nemrtvé na prach, automaticky očišťuje veškeré negativní stavy a vytváří posvátné štíty.",
            "✨",
            "Dokonči elementární trénink Světla v komnatách afinity"
        ),
        CodexEntry(
            "element_physical",
            "Živly",
            "Fyzický – Kalená Ocel a Krev Válečníků",
            "Hrubá síla, svaly a ostří bez many",
            "Zrozeno v první kovárně mistra Kováře Vulkana z lidské krve a oceli. Působí status Krvácení (Bleed), čisté prorážení zbroje a funguje nezávisle na maně.",
            "⚔️",
            "Dokonči elementární trénink Fyzického živlu v komnatách afinity"
        )
    )

    fun checkUnlocks(state: GameSave): Set<String> {
        val newUnlocks = state.player.unlockedCodexIds.toMutableSet()
        val player = state.player
        
        // Initial unlocks
        newUnlocks.add("lore_world")
        newUnlocks.add("event_eclipse")
        
        // Conditions
        if (player.killCount > 0) newUnlocks.add("bestiary_goblin")
        if (state.defeatedBosses.isNotEmpty()) newUnlocks.add("bestiary_vampire")
        if (player.gold >= 1000) newUnlocks.add("npc_merchant")
        if (state.characters.any { it.klient == "Inkviziční legie" } || state.player.unlockedCodexIds.contains("npc_inquisitor")) {
            newUnlocks.add("npc_inquisitor")
        }
        
        // New conditions
        if (player.items.any { it.id == "elixir_touhy" }) newUnlocks.add("item_elixir")
        if (player.items.any { it.id == "drahy_obojek" }) newUnlocks.add("item_collar")
        if (state.characters.isNotEmpty()) newUnlocks.add("event_harem_open")

        // Elemental affinities discovered during training
        ElementalCodexData.ENTRIES.forEach { elemEntry ->
            if (ElementalCodexData.isDiscovered(state, elemEntry.element)) {
                newUnlocks.add(elemEntry.id)
            }
        }
        
        return newUnlocks
    }
}
