import re

with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'r') as f:
    text = f.read()

achievement_classes = """
@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val badgeIcon: String,
    val isTitle: Boolean = false
)

object AchievementList {
    val allAchievements = listOf(
        Achievement("ach_harem_10", "Sběratel krásy", "Získej alespoň 10 dívek do svého harému.", "👥", true),
        Achievement("ach_harem_20", "Pán harému", "Shromáždi ohromných 20 dívek ve svém harému.", "👑", true),
        Achievement("ach_affinity_total", "Casanova podsvětí", "Dosáhni celkové náklonnosti (Affinity) 250 napříč harémem.", "💖", true),
        Achievement("ach_boss_slayer", "Ničitel bossů", "Poraz alespoň 3 bosse ve výpravách.", "💀", true),
        Achievement("ach_arena_champion", "Král Arény", "Dostaň tvou dívku na úroveň 10 pomocí arénových bojů.", "⚔️", true),
        Achievement("ach_wealthy", "Midasův dotek", "Našetři alespoň 10 000 zlatých.", "💰", true),
        Achievement("ach_domain_max", "Temný vládce", "Vylepši svou Pevnost na úroveň 5.", "🏰", true),
        Achievement("ach_blood_sister", "Krvavá přísaha", "Získej dívku se vztahem 'Krvavá sestra'.", "🩸", true)
    )
}
"""

if "data class Achievement" not in text:
    text = text.replace("@Serializable\ndata class Player(", achievement_classes + "\n@Serializable\ndata class Player(")

target = """    var equippedWeaponIndex: Int = 0,
    var weapons: MutableList<Weapon> = mutableListOf("""

replacement = """    var equippedWeaponIndex: Int = 0,
    var activeTitle: String? = null,
    var unlockedAchievements: MutableList<String> = mutableListOf(),
    var weapons: MutableList<Weapon> = mutableListOf("""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'w') as f:
    f.write(text)
