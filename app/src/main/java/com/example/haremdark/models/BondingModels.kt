package com.example.haremdark.models

import kotlinx.serialization.Serializable

@Serializable
enum class BondingLevel(val title: String, val threshold: Int) {
    STRANGER("Cizinka", 0),
    ACQUAINTANCE("Známá", 50),
    COMPANION("Družka", 150),
    LOVER("Milenka", 300),
    SOULMATE("Spřízněná duše", 500);

    companion object {
        fun fromAffinity(affinity: Int): BondingLevel {
            return entries.filter { affinity >= it.threshold }.maxByOrNull { it.threshold } ?: STRANGER
        }
    }
}

@Serializable
data class BondingBonus(
    val id: String,
    val title: String,
    val description: String,
    val statBonus: Map<String, Int>
)

object BondingBonuses {
    val levelBonuses = mapOf(
        BondingLevel.COMPANION to BondingBonus("bonus_comp", "Harémové pouto", "Zvýšení efektivity výcviku o 10%.", mapOf("training_efficiency" to 10)),
        BondingLevel.LOVER to BondingBonus("bonus_lover", "Milostné souznění", "Produkce zlata zvýšena o 5%.", mapOf("gold_production" to 5)),
        BondingLevel.SOULMATE to BondingBonus("bonus_soul", "Absolutní oddanost", "Snížení nákladů na údržbu o 10%.", mapOf("upkeep_reduction" to 10))
    )
}
