package com.example.haremdark.domain

import com.example.haremdark.data.AffinityMilestoneBonusCatalog
import com.example.haremdark.data.HaremAffinityResourceSummary
import com.example.haremdark.models.GameSave
import com.example.haremdark.models.Player

class DomainResourceManager {

    data class DailyYield(
        val gold: Int,
        val wood: Int,
        val stone: Int,
        val iron: Int,
        val mana: Int,
        val manaEssence: Int,
        val populationGrowth: Int,
        val affinityBonusGold: Int = 0,
        val affinityBonusMana: Int = 0,
        val affinityBonusManaEssence: Int = 0,
        val affinityBonusMaterials: Int = 0,
        val affinityBonusPopulation: Int = 0,
        val affinitySummary: HaremAffinityResourceSummary? = null
    )

    fun calculateDailyYield(state: GameSave): DailyYield {
        var gold = 0
        var wood = 0
        var stone = 0
        var iron = 0
        var mana = 0
        var manaEssence = 0
        var populationGrowth = 0

        val buildings = state.buildings
        val territories = state.territories
        
        // Base income from territories
        gold += territories.filter { it.level > 0 }.sumOf { it.baseIncome * it.level }

        buildings.forEach { b ->
            when (b.type) {
                "trznice" -> gold += b.level * 25
                "drevohorec" -> wood += b.level * 15
                "kamenolom" -> stone += b.level * 10
                "zelezny_dul" -> iron += b.level * 5
                "chram_temnoty" -> {
                    mana += b.level * 5
                    manaEssence += b.level * 2
                }
                "ubytovny" -> populationGrowth += b.level * 2
            }
        }

        // Base population growth if player has population
        if (state.player.population < state.player.maxPopulation) {
            populationGrowth += 1 // Natural growth
        }

        // --- PASSIVE RESOURCE GENERATION FROM HAREM AFFINITY MILESTONES ---
        val affinityYield = AffinityMilestoneBonusCatalog.calculateTotalHaremYield(state.characters)
        gold += affinityYield.totalGold
        wood += affinityYield.totalWood
        stone += affinityYield.totalStone
        iron += affinityYield.totalIron
        mana += affinityYield.totalMana
        manaEssence += affinityYield.totalManaEssence
        populationGrowth += affinityYield.totalPopulationGrowth

        val totalAffinityMaterials = affinityYield.totalWood + affinityYield.totalStone + affinityYield.totalIron

        return DailyYield(
            gold = gold,
            wood = wood,
            stone = stone,
            iron = iron,
            mana = mana,
            manaEssence = manaEssence,
            populationGrowth = populationGrowth,
            affinityBonusGold = affinityYield.totalGold,
            affinityBonusMana = affinityYield.totalMana,
            affinityBonusManaEssence = affinityYield.totalManaEssence,
            affinityBonusMaterials = totalAffinityMaterials,
            affinityBonusPopulation = affinityYield.totalPopulationGrowth,
            affinitySummary = affinityYield
        )
    }

    fun applyYield(player: Player, yield: DailyYield) {
        player.gold += yield.gold
        player.wood += yield.wood
        player.stone += yield.stone
        player.iron += yield.iron
        player.mana = (player.mana + yield.mana).coerceAtMost(player.maxMana)
        player.manaEssence += yield.manaEssence
        
        val newPop = player.population + yield.populationGrowth
        player.population = newPop.coerceAtMost(player.maxPopulation)
    }
}
