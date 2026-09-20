package com.example.haremdark.data

import com.example.haremdark.models.BestiaryEntry
import com.example.haremdark.models.DropChance

object BestiaryCatalog {
    val ALL_ENTRIES = listOf(
        BestiaryEntry(
            enemyId = "gladiator_novice",
            name = "Gladiátorský novic",
            icon = "🗡️",
            title = "Bojovník arény",
            description = "Nezkušený rekrut trénující v písku arény. Důvěřuje své oceli, ale chybí mu obratnost proti sehranému týmu.",
            weaknesses = listOf("Fyzický útok", "Krvácení"),
            commonDrops = listOf("Zlato", "Opotřebovaný meč"),
            rareDrops = listOf("Železný fragment"),
            dropChances = listOf(
                DropChance("Zlato (35-50)", 90, "Měna", false),
                DropChance("Opotřebovaný meč", 65, "Vybavení", false),
                DropChance("Železný fragment", 25, "Úlomek výbavy", true)
            ),
            isDiscovered = false,
            hp = 85,
            attack = 14,
            defense = 6,
            speed = 10,
            element = "Fyzický",
            isBoss = false
        ),
        BestiaryEntry(
            enemyId = "war_hound",
            name = "Válečný bojový pes",
            icon = "🐕",
            title = "Cvičená bestie",
            description = "Zuřivá šelma vycvičená k trhání nepřátel v přední linii. Útočí vysokou rychlostí.",
            weaknesses = listOf("Ohnivá kletba", "Omráčení"),
            commonDrops = listOf("Surová kůže", "Zlato"),
            rareDrops = listOf("Dračí dráp"),
            dropChances = listOf(
                DropChance("Surová kůže", 85, "Materiál", false),
                DropChance("Zlato (20-35)", 80, "Měna", false),
                DropChance("Dračí dráp", 15, "Vzácný surovina", true)
            ),
            isDiscovered = false,
            hp = 65,
            attack = 16,
            defense = 4,
            speed = 16,
            element = "Fyzický",
            isBoss = false
        ),
        BestiaryEntry(
            enemyId = "syndicate_enforcer",
            name = "Podsvětní vymahač",
            icon = "🪓",
            title = "Těžkooděnec",
            description = "Brutální bijec Černého syndikátu oblečený v těžké plátové zbroji s obouruční sekerou.",
            weaknesses = listOf("Magie", "Kyselina"),
            commonDrops = listOf("Zlato", "Těžká zbroj"),
            rareDrops = listOf("Ocelový plát"),
            dropChances = listOf(
                DropChance("Zlato (50-80)", 95, "Měna", false),
                DropChance("Těžká zbroj", 50, "Vybavení", false),
                DropChance("Ocelový plát", 20, "Úlomek výbavy", true)
            ),
            isDiscovered = false,
            hp = 140,
            attack = 18,
            defense = 12,
            speed = 9,
            element = "Fyzický",
            isBoss = false
        ),
        BestiaryEntry(
            enemyId = "shadow_rogue",
            name = "Vražedkyně z dýk",
            icon = "🗡️",
            title = "Stínový zabiják",
            description = "Smrtící agentka pohybující se v šeru. Specializuje se na rychlé a kritické údery otrávenými dýkami.",
            weaknesses = listOf("Plošné útoky", "Odhalení"),
            commonDrops = listOf("Jed", "Stříbrné mince"),
            rareDrops = listOf("Stínový drahokam"),
            dropChances = listOf(
                DropChance("Jed", 70, "Alchymie", false),
                DropChance("Stříbrné mince", 85, "Měna", false),
                DropChance("Stínový drahokam", 18, "Vzácný drahokam", true)
            ),
            isDiscovered = false,
            hp = 95,
            attack = 22,
            defense = 6,
            speed = 18,
            element = "Temnota",
            isBoss = false
        ),
        BestiaryEntry(
            enemyId = "poison_alchemist",
            name = "Temný travič",
            icon = "🧪",
            title = "Podpora",
            description = "Alchymista zkažený temnou magií, který míchá smrtící lektvary a toxické výpary.",
            weaknesses = listOf("Světlo", "Omráčení"),
            commonDrops = listOf("Lektvar", "Bylinky"),
            rareDrops = listOf("Etiopský elixír"),
            dropChances = listOf(
                DropChance("Bylinky", 90, "Alchymie", false),
                DropChance("Léčivý lektvar", 60, "Spotřební", false),
                DropChance("Etiopský elixír", 12, "Vzácný elixír", true)
            ),
            isDiscovered = false,
            hp = 80,
            attack = 15,
            defense = 5,
            speed = 12,
            element = "Jed",
            isBoss = false
        ),
        BestiaryEntry(
            enemyId = "inquisitor_captain",
            name = "Inkviziční kapitán",
            icon = "⚖️",
            title = "Fanatický velitel",
            description = "Neoblomný velitel Svaté inkvizice s posvátným mečem a nezlomnou vůlí.",
            weaknesses = listOf("Temná magie", "Krvácení"),
            commonDrops = listOf("Svatý symbol", "Zlato"),
            rareDrops = listOf("Sluneční kříž"),
            dropChances = listOf(
                DropChance("Zlato (150-250)", 100, "Měna", false),
                DropChance("Svatý symbol", 75, "Artefakt", false),
                DropChance("Sluneční kříž", 35, "Legendární fragment", true)
            ),
            isDiscovered = false,
            hp = 220,
            attack = 26,
            defense = 16,
            speed = 11,
            element = "Svatý",
            isBoss = true
        ),
        BestiaryEntry(
            enemyId = "zealot_priest",
            name = "Inkviziční zealot",
            icon = "📜",
            title = "Fanatický kněz",
            description = "Náboženský fanatik posilující své druhy modlitbami a sesílající ohnivé blesky.",
            weaknesses = listOf("Fyzický útok", "Přerušení"),
            commonDrops = listOf("Modlitební kniha"),
            rareDrops = listOf("Krystal víry"),
            dropChances = listOf(
                DropChance("Modlitební kniha", 80, "Svitek", false),
                DropChance("Krystal víry", 22, "Vzácný krystal", true)
            ),
            isDiscovered = false,
            hp = 110,
            attack = 20,
            defense = 8,
            speed = 13,
            element = "Svatý",
            isBoss = false
        ),
        BestiaryEntry(
            enemyId = "combat_automaton",
            name = "Dimeritový golem",
            icon = "🤖",
            title = "Magický strážce",
            description = "Starověký konstrukt z oceli a dimeritu. Odolný vůči všem běžným magickým útokům.",
            weaknesses = listOf("Těžký útok", "Elektřina"),
            commonDrops = listOf("Dimeritový šrot", "Kovové ozubení"),
            rareDrops = listOf("Jádro golema"),
            dropChances = listOf(
                DropChance("Dimeritový šrot", 100, "Surovina", false),
                DropChance("Kovové ozubení", 90, "Materiál", false),
                DropChance("Jádro golema", 40, "Legendární jádro", true)
            ),
            isDiscovered = false,
            hp = 300,
            attack = 30,
            defense = 24,
            speed = 7,
            element = "Kov",
            isBoss = true
        )
    )
}
