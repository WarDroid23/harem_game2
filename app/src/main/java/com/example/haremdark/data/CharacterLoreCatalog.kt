package com.example.haremdark.data

import androidx.compose.ui.graphics.Color
import com.example.haremdark.models.Character

enum class RelationshipConnectionType(
    val title: String,
    val icon: String,
    val defaultColorHex: Long
) {
    LORD_BOND("Pouto k Pánovi", "👑", 0xFFFFD700),
    SYNERGY("Bojová Synergie", "🔗", 0xFF00E676),
    RIVALRY("Rivalita v Harému", "⚔️", 0xFFFF3D00),
    LORE("Mystická Vzpomínka", "📜", 0xFFE040FB),
    SOUL_BOND("Spříznění Duší", "💖", 0xFFFF4081)
}

data class CharacterLoreConnection(
    val id: String,
    val char1Archetype: String?, // null = any archetype or specific ID
    val char2Archetype: String?,
    val char1Name: String? = null,
    val char2Name: String? = null,
    val title: String,
    val type: RelationshipConnectionType,
    val requiredMinAffinitySum: Int = 4, // sum of affinity levels of both characters
    val requiredMinLoyaltySum: Int = 40,
    val loreDescription: String,
    val synergyBonusText: String,
    val customColorHex: Long? = null
) {
    fun isUnlocked(char1: Character, char2: Character): Boolean {
        val sumAffinity = char1.affinityLevel + char2.affinityLevel
        val sumLoyalty = char1.loajalita + char2.loajalita
        return sumAffinity >= requiredMinAffinitySum && sumLoyalty >= requiredMinLoyaltySum
    }
}

object CharacterLoreCatalog {

    val CONNECTIONS: List<CharacterLoreConnection> = listOf(
        CharacterLoreConnection(
            id = "conn_valk_sub",
            char1Archetype = "valkyra",
            char2Archetype = "subka",
            title = "Bitevní Sestry Stínů",
            type = RelationshipConnectionType.SYNERGY,
            requiredMinAffinitySum = 4,
            requiredMinLoyaltySum = 50,
            loreDescription = "Valkýřina bojová záštita kryje křehkou submisivní dívku v bitevní vřavě. Společně vytvářejí nezlomný štít.",
            synergyBonusText = "+15% Kritický Zásah v Soubojích & +10 Max HP pro obě dívky"
        ),
        CharacterLoreConnection(
            id = "conn_knez_sub",
            char1Archetype = "knezka",
            char2Archetype = "subka",
            title = "Půlnoční Rituál",
            type = RelationshipConnectionType.LORE,
            requiredMinAffinitySum = 5,
            requiredMinLoyaltySum = 60,
            loreDescription = "Kněžka využívá jemnou životní energii dívky ke zklidnění temných duchů v Dominijní svatyni.",
            synergyBonusText = "+20 SE Regenerace v Harému & +10% Zisk Temné Energie"
        ),
        CharacterLoreConnection(
            id = "conn_valk_knez",
            char1Archetype = "valkyra",
            char2Archetype = "knezka",
            title = "Rivalita Meče a Magie",
            type = RelationshipConnectionType.RIVALRY,
            requiredMinAffinitySum = 4,
            requiredMinLoyaltySum = 40,
            loreDescription = "Ostrý střet mezi hrdým válečným mečem a tajemnými stínovými kouzly. Souboj o přízeň Pána zvyšuje jejich bojový zápal.",
            synergyBonusText = "+20 Fyzický & Magický Útok, ale -5 Fyzická Obrana"
        ),
        CharacterLoreConnection(
            id = "conn_any_soulmate",
            char1Archetype = null,
            char2Archetype = null,
            title = "Svaté Pouto Harému",
            type = RelationshipConnectionType.SOUL_BOND,
            requiredMinAffinitySum = 9,
            requiredMinLoyaltySum = 140,
            loreDescription = "Dvě dívky dosáhly vrcholu spřízněnosti s Pánem. Jejich duše rezonují v dokonalé harmonii s celým dominia.",
            synergyBonusText = "+25% Celkový Příjem Zlata & +15% Všechny Atributy Pána"
        ),
        CharacterLoreConnection(
            id = "conn_sub_sub",
            char1Archetype = "subka",
            char2Archetype = "subka",
            title = "Sesterství Poslušnosti",
            type = RelationshipConnectionType.SYNERGY,
            requiredMinAffinitySum = 3,
            requiredMinLoyaltySum = 30,
            loreDescription = "Dívky sdílejí společné komnaty a pomáhají si navzájem plnit veškerá přání Pána Dominia.",
            synergyBonusText = "+10% Loajalita & -15% Náklady na Trénink"
        ),
        CharacterLoreConnection(
            id = "conn_valk_valk",
            char1Archetype = "valkyra",
            char2Archetype = "valkyra",
            title = "Bitevní Smečka Valkýr",
            type = RelationshipConnectionType.SYNERGY,
            requiredMinAffinitySum = 6,
            requiredMinLoyaltySum = 80,
            loreDescription = "Kombinovaná síla více válečnic v jedné linii rozráží nepřátelské šikády jako vichřice.",
            synergyBonusText = "+30% Poškození Družiny v Aréně & +15% Rychlost"
        ),
        CharacterLoreConnection(
            id = "conn_knez_knez",
            char1Archetype = "knezka",
            char2Archetype = "knezka",
            title = "Kruh Temného Okultismu",
            type = RelationshipConnectionType.LORE,
            requiredMinAffinitySum = 6,
            requiredMinLoyaltySum = 80,
            loreDescription = "Mystický kruh kněžek manipuluje časem a osudem pro absolutní dominanci Pána nad temným světem.",
            synergyBonusText = "+30% Zisk Zkušeností & +25 SE Max Limit"
        )
    )

    fun getActiveConnectionsForParty(characters: List<Character>): List<Pair<CharacterLoreConnection, Pair<Character, Character>>> {
        val result = mutableListOf<Pair<CharacterLoreConnection, Pair<Character, Character>>>()
        for (i in characters.indices) {
            for (j in i + 1 until characters.size) {
                val c1 = characters[i]
                val c2 = characters[j]
                for (conn in CONNECTIONS) {
                    val matchesArchetype = (conn.char1Archetype == null && conn.char2Archetype == null) ||
                            (conn.char1Archetype.equals(c1.archetypeId, ignoreCase = true) && conn.char2Archetype.equals(c2.archetypeId, ignoreCase = true)) ||
                            (conn.char1Archetype.equals(c2.archetypeId, ignoreCase = true) && conn.char2Archetype.equals(c1.archetypeId, ignoreCase = true))
                    val matchesName = (conn.char1Name == null || conn.char1Name.equals(c1.name, ignoreCase = true) || conn.char1Name.equals(c2.name, ignoreCase = true)) &&
                            (conn.char2Name == null || conn.char2Name.equals(c1.name, ignoreCase = true) || conn.char2Name.equals(c2.name, ignoreCase = true))

                    if (matchesArchetype && matchesName) {
                        result.add(Pair(conn, Pair(c1, c2)))
                    }
                }
            }
        }
        return result
    }
}
