package com.example.haremdark.domain

import com.example.haremdark.models.GameSave
import com.example.haremdark.models.Player

class DomainResourceManager {

    data class DailyYield(
        val gold: Int,
        val wood: Int,
        val stone: Int,
        val iron: Int,
        val mana: Int,
        val populationGrowth: Int
    )

    fun calculateDailyYield(state: GameSave): DailyYield {
        var gold = 0
        var wood = 0
        var stone = 0
        var iron = 0
        var mana = 0
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
                "chram_temnoty" -> mana += b.level * 5
                "ubytovny" -> populationGrowth += b.level * 2
            }
        }

        // Base population growth if player has population
        if (state.player.population < state.player.maxPopulation) {
            populationGrowth += 1 // Natural growth
        }

        return DailyYield(gold, wood, stone, iron, mana, populationGrowth)
    }

    fun applyYield(player: Player, yield: DailyYield) {
        player.gold += yield.gold
        player.wood += yield.wood
        player.stone += yield.stone
        player.iron += yield.iron
        player.mana = (player.mana + yield.mana).coerceAtMost(player.maxMana)
        
        val newPop = player.population + yield.populationGrowth
        player.population = newPop.coerceAtMost(player.maxPopulation)
    }
}
