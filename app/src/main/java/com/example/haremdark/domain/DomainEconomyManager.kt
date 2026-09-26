package com.example.haremdark.domain

import com.example.haremdark.models.Building
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave
import com.example.haremdark.models.Player
import java.io.Serializable

data class ResourceProductionRate(
    val goldPerDay: Int,
    val woodPerDay: Int,
    val stonePerDay: Int,
    val manaPerDay: Int,
    val sexEnergyPerDay: Int,
    val darkEnergyPerDay: Int,
    val maxWoodCapacity: Int,
    val maxStoneCapacity: Int,
    val maxManaCapacity: Int,
    val maxGoldCapacity: Int
) : Serializable

data class EconomyStructureDef(
    val type: String,
    val name: String,
    val icon: String,
    val category: String, // "Suroviny", "Magie & Energie", "Moc & Pokladnice", "Sklad"
    val description: String,
    val baseWoodProduction: Int = 0,
    val baseStoneProduction: Int = 0,
    val baseManaProduction: Int = 0,
    val baseGoldProduction: Int = 0,
    val baseSexEnergyProduction: Int = 0,
    val baseStorageCapacity: Int = 0,
    val requiredWoodCost: (level: Int) -> Int,
    val requiredStoneCost: (level: Int) -> Int,
    val requiredGoldCost: (level: Int) -> Int,
    val requiredDarkCost: (level: Int) -> Int = { 0 },
    val recommendedArchetype: String = "subka",
    val workerSynergyDescription: String = ""
) : Serializable

object DomainEconomyManager {

    val STRUCTURES = listOf(
        EconomyStructureDef(
            type = "pila",
            name = "Dřevorubecký tábor a Pila",
            icon = "🪵",
            category = "Suroviny",
            description = "Těžba a zpracování černého dřeva pro výstavbu budov a opevnění dominia.",
            baseWoodProduction = 60,
            requiredWoodCost = { level -> level * 30 + 20 },
            requiredStoneCost = { level -> level * 40 + 30 },
            requiredGoldCost = { level -> level * 120 + 80 },
            recommendedArchetype = "odvazna",
            workerSynergyDescription = "+40% těžba dřeva při přiřazení Odvážné/Bojovnice"
        ),
        EconomyStructureDef(
            type = "kamenolom",
            name = "Žulový a Mramorový lom",
            icon = "🪨",
            category = "Suroviny",
            description = "Lámání masivních kamenných kvádrů pro stavbu hradeb, věží a honosných sálů.",
            baseStoneProduction = 50,
            requiredWoodCost = { level -> level * 50 + 40 },
            requiredStoneCost = { level -> level * 25 + 20 },
            requiredGoldCost = { level -> level * 140 + 90 },
            recommendedArchetype = "draci_divka",
            workerSynergyDescription = "+45% těžba kamene při přiřazení Dračí princezny"
        ),
        EconomyStructureDef(
            type = "manova_vez",
            name = "Věž Arcana & Manové zřídlo",
            icon = "🔮",
            category = "Magie & Energie",
            description = "Akumulace surové astrální many z trhlin reality pro kouzla a rituály.",
            baseManaProduction = 25,
            requiredWoodCost = { level -> level * 45 + 35 },
            requiredStoneCost = { level -> level * 65 + 50 },
            requiredGoldCost = { level -> level * 200 + 150 },
            requiredDarkCost = { level -> level * 5 + 3 },
            recommendedArchetype = "touha",
            workerSynergyDescription = "+50% zisk many při přiřazení Toužící čarodějky / Sukuby"
        ),
        EconomyStructureDef(
            type = "zelezny_dul",
            name = "Zlaté doly a Mincovna",
            icon = "💰",
            category = "Moc & Pokladnice",
            description = "Ražba císařských mincí a zpracování rudy pro neomezené bohatství říše.",
            baseGoldProduction = 120,
            requiredWoodCost = { level -> level * 40 + 30 },
            requiredStoneCost = { level -> level * 55 + 40 },
            requiredGoldCost = { level -> level * 100 + 75 },
            recommendedArchetype = "slechticna",
            workerSynergyDescription = "+35% výnos zlata při přiřazení Zlomené šlechtičny"
        ),
        EconomyStructureDef(
            type = "sklad_surovin",
            name = "Velká zásobárna a Sklady",
            icon = "📦",
            category = "Sklad",
            description = "Rozšiřuje maximální kapacitu uskladnění dřeva, kamene, many a zlata o +500 na úroveň.",
            baseStorageCapacity = 500,
            requiredWoodCost = { level -> level * 60 + 50 },
            requiredStoneCost = { level -> level * 60 + 50 },
            requiredGoldCost = { level -> level * 150 + 100 },
            recommendedArchetype = "manipulativni",
            workerSynergyDescription = "+25% kapacita skladů při přiřazení Manipulativní správkyně"
        ),
        EconomyStructureDef(
            type = "komnaty_rozkose",
            name = "Lázně a Harémové komnaty",
            icon = "🛁",
            category = "Magie & Energie",
            description = "Oáza smyslnosti a odpočinku generující sexuální energii a harmonii celého harému.",
            baseSexEnergyProduction = 20,
            requiredWoodCost = { level -> level * 40 + 30 },
            requiredStoneCost = { level -> level * 40 + 30 },
            requiredGoldCost = { level -> level * 180 + 120 },
            recommendedArchetype = "subka",
            workerSynergyDescription = "+50% obnova energie a +15 harmonie při přiřazení Submisivní otrokyně"
        )
    )

    fun calculateProduction(
        gameState: GameSave,
        assignedWorkers: Map<String, String?> = emptyMap()
    ): ResourceProductionRate {
        val player = gameState.player
        val buildings = gameState.buildings
        val characters = gameState.characters
        val domainLvl = gameState.domainExpansionLevel.coerceAtLeast(1)

        val lumberLvl = buildings.find { it.type == "pila" }?.level ?: 1
        val quarryLvl = buildings.find { it.type == "kamenolom" }?.level ?: 1
        val manaLvl = buildings.find { it.type == "manova_vez" || it.type == "laborator" }?.level ?: 1
        val goldLvl = buildings.find { it.type == "zelezny_dul" || it.type == "mine" }?.level ?: 1
        val haremLvl = buildings.find { it.type == "komnaty_rozkose" || it.type == "zahrady" }?.level ?: 1
        val storageLvl = buildings.find { it.type == "sklad_surovin" }?.level ?: 1

        // Worker multipliers
        val lumberChar = characters.find { it.id == assignedWorkers["pila"] }
        val quarryChar = characters.find { it.id == assignedWorkers["kamenolom"] }
        val manaChar = characters.find { it.id == assignedWorkers["manova_vez"] }
        val goldChar = characters.find { it.id == assignedWorkers["zelezny_dul"] }
        val haremChar = characters.find { it.id == assignedWorkers["komnaty_rozkose"] }

        val woodMult = 1.0f + (if (lumberChar?.archetypeId in listOf("odvazna", "vzdorna")) 0.4f else if (lumberChar != null) 0.2f else 0f)
        val stoneMult = 1.0f + (if (quarryChar?.archetypeId in listOf("draci_divka", "krvava_subka")) 0.45f else if (quarryChar != null) 0.2f else 0f)
        val manaMult = 1.0f + (if (manaChar?.archetypeId in listOf("touha", "sukuba", "posedla")) 0.5f else if (manaChar != null) 0.25f else 0f)
        val goldMult = 1.0f + (if (goldChar?.archetypeId in listOf("slechticna", "manipulativni")) 0.35f else if (goldChar != null) 0.2f else 0f)
        val sexMult = 1.0f + (if (haremChar?.archetypeId in listOf("subka", "nymfomanka", "ticha_panenka")) 0.5f else if (haremChar != null) 0.2f else 0f)

        val woodPerDay = ((40 + lumberLvl * 35 + domainLvl * 15) * woodMult).toInt()
        val stonePerDay = ((30 + quarryLvl * 30 + domainLvl * 12) * stoneMult).toInt()
        val manaPerDay = ((15 + manaLvl * 20 + domainLvl * 10) * manaMult).toInt()
        val goldPerDay = ((80 + goldLvl * 60 + domainLvl * 30) * goldMult).toInt()
        val sexEnergyPerDay = ((15 + haremLvl * 12 + characters.size * 3) * sexMult).toInt()
        val darkEnergyPerDay = 10 + domainLvl * 5

        val baseCap = 500 + storageLvl * 500 + domainLvl * 250

        return ResourceProductionRate(
            goldPerDay = goldPerDay,
            woodPerDay = woodPerDay,
            stonePerDay = stonePerDay,
            manaPerDay = manaPerDay,
            sexEnergyPerDay = sexEnergyPerDay,
            darkEnergyPerDay = darkEnergyPerDay,
            maxWoodCapacity = baseCap,
            maxStoneCapacity = baseCap,
            maxManaCapacity = baseCap / 2,
            maxGoldCapacity = baseCap * 5
        )
    }

    fun harvestResources(
        player: Player,
        rates: ResourceProductionRate
    ): HarvestResult {
        val woodGained = (rates.woodPerDay * 0.5f).toInt().coerceAtLeast(15)
        val stoneGained = (rates.stonePerDay * 0.5f).toInt().coerceAtLeast(10)
        val manaGained = (rates.manaPerDay * 0.5f).toInt().coerceAtLeast(5)
        val goldGained = (rates.goldPerDay * 0.5f).toInt().coerceAtLeast(40)
        val sexGained = (rates.sexEnergyPerDay * 0.5f).toInt().coerceAtLeast(8)
        val darkGained = (rates.darkEnergyPerDay * 0.5f).toInt().coerceAtLeast(4)

        player.wood = (player.wood + woodGained).coerceAtMost(rates.maxWoodCapacity)
        player.stone = (player.stone + stoneGained).coerceAtMost(rates.maxStoneCapacity)
        player.mana = (player.mana + manaGained).coerceAtMost(rates.maxManaCapacity)
        player.gold = (player.gold + goldGained).coerceAtMost(rates.maxGoldCapacity)
        player.sexEnergy = (player.sexEnergy + sexGained).coerceAtMost(player.maxSexEnergy)
        player.darkEnergy = (player.darkEnergy + darkGained).coerceAtMost(player.maxDarkEnergy)

        return HarvestResult(
            woodGained = woodGained,
            stoneGained = stoneGained,
            manaGained = manaGained,
            goldGained = goldGained,
            sexEnergyGained = sexGained,
            darkEnergyGained = darkGained
        )
    }
}

data class HarvestResult(
    val woodGained: Int,
    val stoneGained: Int,
    val manaGained: Int,
    val goldGained: Int,
    val sexEnergyGained: Int,
    val darkEnergyGained: Int
) : Serializable
