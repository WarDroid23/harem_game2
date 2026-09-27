package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
enum class SkillBranch(val title: String, val icon: String, val description: String) {
    COMBAT("Bojová síla", "⚔️", "Útočné pasivní buffy a zničující fyzické chvaty"),
    ARCANE("Aktivní magie", "🔮", "Aktivní elementalní kouzla, blesky a posvátné léčení"),
    DEFENSE("Obrana & Život", "🛡️", "Max HP, snížení poškození a ochranné štíty"),
    DOMINANCE("Dominance & Harém", "👑", "Zlaté příjmy, náklonnost a harémová inspirace")
}

@Serializable
data class SkillNode(
    val id: String,
    val title: String,
    val description: String,
    val cost: Int = 1, // Skill points cost
    val xpCost: Int = 100, // Accumulated XP cost
    val requiresId: String? = null,
    val icon: String = "✨",
    val isActiveCombatSkill: Boolean = false, // Active skill vs Passive buff
    val damageMultiplier: Float = 0f,
    val manaCost: Int = 0,
    val cooldownTurns: Int = 0,
    val minLevel: Int = 1,
    val minRarity: String = "Common", // Common, Rare, Epic, Legendary
    val branch: SkillBranch = SkillBranch.COMBAT,
    val statBonusSummary: String = ""
)

object SkillTreeData {
    val nodes = listOf(
        // === BOJOVÁ SÍLA (COMBAT) ===
        SkillNode(
            id = "comb_1",
            title = "Drtivý sek",
            description = "Zvyšuje útočnou sílu postavy o +15 v fyzických soubojích.",
            cost = 1,
            xpCost = 100,
            icon = "⚔️",
            branch = SkillBranch.COMBAT,
            statBonusSummary = "+15 Útok"
        ),
        SkillNode(
            id = "comb_2",
            title = "Krvavá čepel",
            description = "Kritické Zásahy způsobují o +25% vyšší destruktivní poškození.",
            cost = 2,
            xpCost = 200,
            requiresId = "comb_1",
            icon = "🩸",
            branch = SkillBranch.COMBAT,
            statBonusSummary = "+25% Crit Damage"
        ),
        SkillNode(
            id = "comb_active_1",
            title = "Smrtící výpad",
            description = "Aktivní bojový chvat: Rychlý výpad za 2.2x poškození s 20% šancí na omráčení.",
            cost = 2,
            xpCost = 250,
            requiresId = "comb_1",
            icon = "🗡️",
            isActiveCombatSkill = true,
            damageMultiplier = 2.2f,
            manaCost = 15,
            cooldownTurns = 2,
            branch = SkillBranch.COMBAT,
            statBonusSummary = "2.2x Poškození, 15 MP"
        ),
        SkillNode(
            id = "comb_3",
            title = "Bojová zuřivost",
            description = "Pasivně zvyšuje šanci na kritický zásah o +15% a rychlost v souboji.",
            cost = 3,
            xpCost = 350,
            requiresId = "comb_2",
            icon = "🔥",
            branch = SkillBranch.COMBAT,
            statBonusSummary = "+15% Crit Rate"
        ),
        SkillNode(
            id = "comb_active_2",
            title = "Bouře mečů",
            description = "Zničující plošná schopnost: Zasáhne všechny nepřátele za 3.5x poškození.",
            cost = 4,
            xpCost = 500,
            requiresId = "comb_active_1",
            icon = "🌪️",
            isActiveCombatSkill = true,
            damageMultiplier = 3.5f,
            manaCost = 35,
            cooldownTurns = 4,
            branch = SkillBranch.COMBAT,
            statBonusSummary = "3.5x AOE Poškození, 35 MP"
        ),

        // === AKTIVNÍ MAGIE (ARCANE) ===
        SkillNode(
            id = "arc_active_1",
            title = "Ohnivá koule",
            description = "Aktivní plamenné kouzlo: Vystřelí ohnivou kouli způsobující 1.8x ohnivé poškození.",
            cost = 1,
            xpCost = 100,
            icon = "🔥",
            isActiveCombatSkill = true,
            damageMultiplier = 1.8f,
            manaCost = 10,
            cooldownTurns = 1,
            branch = SkillBranch.ARCANE,
            statBonusSummary = "1.8x Fire Damage, 10 MP"
        ),
        SkillNode(
            id = "arc_1",
            title = "Magická rezonance",
            description = "Zvyšuje maximální zásobu many o +30 a pasivní regeneraci MP v každém kole.",
            cost = 1,
            xpCost = 120,
            icon = "🔮",
            branch = SkillBranch.ARCANE,
            statBonusSummary = "+30 MP, +3 MP/kolo"
        ),
        SkillNode(
            id = "arc_active_2",
            title = "Bleskový řetěz",
            description = "Aktivní bleskové kouzlo: Způsobí 2.5x bleskové poškození a omráčí cíl.",
            cost = 2,
            xpCost = 220,
            requiresId = "arc_active_1",
            icon = "⚡",
            isActiveCombatSkill = true,
            damageMultiplier = 2.5f,
            manaCost = 20,
            cooldownTurns = 2,
            branch = SkillBranch.ARCANE,
            statBonusSummary = "2.5x Shock Damage, 20 MP"
        ),
        SkillNode(
            id = "arc_active_3",
            title = "Posvátný azyl",
            description = "Aktivní posvátné léčení: Obnoví 40% Max HP & 20 MP celé bojové družině.",
            cost = 3,
            xpCost = 350,
            requiresId = "arc_1",
            icon = "✨",
            isActiveCombatSkill = true,
            damageMultiplier = 0f,
            manaCost = 25,
            cooldownTurns = 3,
            branch = SkillBranch.ARCANE,
            statBonusSummary = "Obnova 40% HP & 20 MP, 25 MP"
        ),
        SkillNode(
            id = "arc_active_4",
            title = "Temná supernova",
            description = "Mocné finální kouzlo: Zničí nepřátele extrémní energií za 4.5x poškození.",
            cost = 5,
            xpCost = 600,
            requiresId = "arc_active_2",
            icon = "🌑",
            isActiveCombatSkill = true,
            damageMultiplier = 4.5f,
            manaCost = 50,
            cooldownTurns = 5,
            branch = SkillBranch.ARCANE,
            statBonusSummary = "4.5x Ultimate Dark Damage, 50 MP"
        ),

        // === OBRANA & ŽIVOT (DEFENSE) ===
        SkillNode(
            id = "def_1",
            title = "Železná vůle",
            description = "Pasivní buff: Zvyšuje maximální zdraví o +40 HP a fyzickou obranu o +10.",
            cost = 1,
            xpCost = 100,
            icon = "🛡️",
            branch = SkillBranch.DEFENSE,
            statBonusSummary = "+40 HP, +10 Obrana"
        ),
        SkillNode(
            id = "def_2",
            title = "Aura neprostupnosti",
            description = "Pasivní buff: Snižuje veškeré utrpené poškození od nepřátel o -15%.",
            cost = 2,
            xpCost = 200,
            requiresId = "def_1",
            icon = "🏰",
            branch = SkillBranch.DEFENSE,
            statBonusSummary = "-15% Utrpené poškození"
        ),
        SkillNode(
            id = "def_3",
            title = "Druhý dech",
            description = "Pasivní záchrana: Při klesnutí pod 20% HP automaticky obnoví 30% Max HP.",
            cost = 3,
            xpCost = 350,
            requiresId = "def_2",
            icon = "💖",
            branch = SkillBranch.DEFENSE,
            statBonusSummary = "Auto-heal 30% HP v krizi"
        ),
        SkillNode(
            id = "def_active_1",
            title = "Světelný štít",
            description = "Aktivní obranné kouzlo: Vytvoří neprostupný štít pohlcující až 120 poškození.",
            cost = 3,
            xpCost = 300,
            requiresId = "def_1",
            icon = "🌟",
            isActiveCombatSkill = true,
            damageMultiplier = 0f,
            manaCost = 20,
            cooldownTurns = 3,
            branch = SkillBranch.DEFENSE,
            statBonusSummary = "Aktivní Štít 120 HP, 20 MP"
        ),

        // === DOMINANCE & HARÉM (DOMINANCE) ===
        SkillNode(
            id = "dom_1",
            title = "Zlatý obchod",
            description = "Pasivní buff: Zvyšuje zisk zlata z pronájmu, dolů a misí o +20%.",
            cost = 1,
            xpCost = 100,
            icon = "💰",
            branch = SkillBranch.DOMINANCE,
            statBonusSummary = "+20% Zlato"
        ),
        SkillNode(
            id = "dom_2",
            title = "Okouzlující aura",
            description = "Pasivní buff: Zrychluje růst Náklonnosti a důvěry s Pánem o +25%.",
            cost = 2,
            xpCost = 180,
            requiresId = "dom_1",
            icon = "💋",
            branch = SkillBranch.DOMINANCE,
            statBonusSummary = "+25% Rychlost Náklonnosti"
        ),
        SkillNode(
            id = "dom_3",
            title = "Královská inspirace",
            description = "Pasivní aura: Zvyšuje morálku celé družiny o +15 a bojovou bojovnost.",
            cost = 3,
            xpCost = 320,
            requiresId = "dom_2",
            icon = "👑",
            branch = SkillBranch.DOMINANCE,
            statBonusSummary = "+15 Morálka Harému"
        ),
        SkillNode(
            id = "dom_active_1",
            title = "Extáze váSelection",
            description = "Aktivní podpora: Okamžitě obnoví +40 SE Pánovi a doplní morálku harému.",
            cost = 4,
            xpCost = 450,
            requiresId = "dom_3",
            icon = "💖",
            isActiveCombatSkill = true,
            damageMultiplier = 0f,
            manaCost = 0,
            cooldownTurns = 4,
            branch = SkillBranch.DOMINANCE,
            statBonusSummary = "Obnova +40 SE, +20 Morálka"
        )
    )
}
