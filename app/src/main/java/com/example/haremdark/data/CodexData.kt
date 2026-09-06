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
        )
    )

    fun checkUnlocks(state: GameSave): Set<String> {
        val newUnlocks = state.player.unlockedCodexIds.toMutableSet()
        val player = state.player
        
        // Initial unlocks
        newUnlocks.add("lore_world")
        
        // Conditions
        if (player.killCount > 0) newUnlocks.add("bestiary_goblin")
        if (state.defeatedBosses.isNotEmpty()) newUnlocks.add("bestiary_vampire")
        if (player.gold >= 1000) newUnlocks.add("npc_merchant")
        if (state.characters.any { it.klient == "Inkviziční legie" } || state.player.unlockedCodexIds.contains("npc_inquisitor")) {
            newUnlocks.add("npc_inquisitor")
        }
        
        return newUnlocks
    }
}
