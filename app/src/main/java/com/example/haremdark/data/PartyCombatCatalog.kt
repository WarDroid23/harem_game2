package com.example.haremdark.data

import com.example.haremdark.R
import com.example.haremdark.models.*

object PartyCombatCatalog {

    /**
     * Generate skills appropriate for a Harem Character based on their archetype, corruption, and stats.
     */
    fun getSkillsForCharacter(
        archetypeId: String,
        corruptionPhase: Int = 1,
        affinityLevel: Int = 1,
        characterName: String = "Bojovnice"
    ): List<PartyCombatSkill> {
        val baseSkills = mutableListOf<PartyCombatSkill>()

        // Archetype signature skill 1
        when (archetypeId) {
            "krvava_subka" -> {
                baseSkills.add(
                    PartyCombatSkill(
                        id = "blood_frenzy",
                        name = "Krvavá zuřivost",
                        icon = "🩸",
                        description = "Sek s vysokým bonusem ke kritickému poškození. Způsobuje krvácení.",
                        manaCost = 15,
                        cooldownTurns = 1,
                        targetType = SkillTargetType.SINGLE_ENEMY,
                        category = SkillCategory.PHYSICAL_ATTACK,
                        powerMultiplier = 1.45f,
                        baseDamageBonus = 12,
                        appliedStatus = CombatStatusEffect("bleed_1", "Krvácení", "🩸", "BLEED", value = 14, durationTurns = 3, description = "Ztrácí 14 HP na začátku každého kola"),
                        animationType = "BLEED_STRIKE",
                        voiceQuote = "Bolest mě jen posiluje!"
                    )
                )
                baseSkills.add(
                    PartyCombatSkill(
                        id = "gladiator_spin",
                        name = "Vířivá poprava",
                        icon = "⚔️",
                        description = "Zasáhne všechny nepřátele v přední linii a sníží jejich obranu.",
                        manaCost = 28,
                        cooldownTurns = 2,
                        targetType = SkillTargetType.ALL_ENEMIES,
                        category = SkillCategory.PHYSICAL_ATTACK,
                        powerMultiplier = 1.15f,
                        baseDamageBonus = 8,
                        appliedStatus = CombatStatusEffect("def_down", "Rozťatá zbroj", "🛡️", "DEF_BUFF", value = -6, durationTurns = 2, description = "Snížená obrana o 6"),
                        animationType = "SLASH"
                    )
                )
            }
            "posedla" -> {
                baseSkills.add(
                    PartyCombatSkill(
                        id = "void_blast",
                        name = "Výboj nicoty",
                        icon = "🔮",
                        description = "Koncentrovaný proud temné magie ignorující 40% nepřátelské obrany.",
                        manaCost = 18,
                        cooldownTurns = 1,
                        targetType = SkillTargetType.SINGLE_ENEMY,
                        category = SkillCategory.DARK_MAGIC,
                        powerMultiplier = 1.55f,
                        baseDamageBonus = 15,
                        animationType = "DARK_BURST",
                        voiceQuote = "Stíny tě pohltí, červe!"
                    )
                )
                baseSkills.add(
                    PartyCombatSkill(
                        id = "agony_hex",
                        name = "Kletba agonie",
                        icon = "👁️",
                        description = "Zasáhne všechny nepřátele a vysává z nich životní sílu do party.",
                        manaCost = 30,
                        cooldownTurns = 3,
                        targetType = SkillTargetType.ALL_ENEMIES,
                        category = SkillCategory.DARK_MAGIC,
                        powerMultiplier = 0.95f,
                        healAmount = 22,
                        appliedStatus = CombatStatusEffect("poison_hex", "Černý mor", "🧪", "POISON", value = 16, durationTurns = 2),
                        animationType = "SHADOW_CURSE"
                    )
                )
            }
            "ticha_panenka", "subka" -> {
                baseSkills.add(
                    PartyCombatSkill(
                        id = "sacred_balm",
                        name = "Něžné ošetření",
                        icon = "✨",
                        description = "Okamžitě vyléčí zraněnou společnici a poskytne ochranný štít.",
                        manaCost = 16,
                        cooldownTurns = 1,
                        targetType = SkillTargetType.SINGLE_ALLY,
                        category = SkillCategory.HOLY_HEAL,
                        healAmount = 42,
                        appliedStatus = CombatStatusEffect("shield_ally", "Ochranná aura", "🛡️", "SHIELD", value = 25, durationTurns = 2),
                        animationType = "HAREM_SUPPORT",
                        voiceQuote = "Postarám se o tebe..."
                    )
                )
                baseSkills.add(
                    PartyCombatSkill(
                        id = "devotion_prayer",
                        name = "Modlitba oddanosti",
                        icon = "💖",
                        description = "Vyléčí celý tým a obnoví 15 Many všem družkám.",
                        manaCost = 32,
                        cooldownTurns = 3,
                        targetType = SkillTargetType.ALL_ALLIES,
                        category = SkillCategory.HOLY_HEAL,
                        healAmount = 28,
                        animationType = "HAREM_SUPPORT"
                    )
                )
            }
            "slechticna", "chladna" -> {
                baseSkills.add(
                    PartyCombatSkill(
                        id = "commanding_shout",
                        name = "Panský rozkaz",
                        icon = "👑",
                        description = "Zvyšuje útočnou sílu a šanci na kritický zásah celého týmu na 3 kola.",
                        manaCost = 20,
                        cooldownTurns = 2,
                        targetType = SkillTargetType.ALL_ALLIES,
                        category = SkillCategory.SUPPORT_BUFF,
                        appliedStatus = CombatStatusEffect("noble_inspire", "Vznešená inspirace", "⚔️", "ATK_BUFF", value = 10, durationTurns = 3),
                        animationType = "HAREM_SUPPORT",
                        voiceQuote = "K zemi! Nemáte právo před námi stát!"
                    )
                )
                baseSkills.add(
                    PartyCombatSkill(
                        id = "rapier_pierce",
                        name = "Rapiérový výpad",
                        icon = "🗡️",
                        description = "Přesné bodnutí do slabin zbroje s 40% šancí na omráčení.",
                        manaCost = 18,
                        cooldownTurns = 2,
                        targetType = SkillTargetType.SINGLE_ENEMY,
                        category = SkillCategory.PHYSICAL_ATTACK,
                        powerMultiplier = 1.35f,
                        baseDamageBonus = 10,
                        appliedStatus = CombatStatusEffect("pierce_stun", "Omráčení", "💫", "STUN", value = 1, durationTurns = 1),
                        animationType = "SLASH"
                    )
                )
            }
            "nymfomanka", "touha" -> {
                baseSkills.add(
                    PartyCombatSkill(
                        id = "seductive_charm",
                        name = "Vábivý pohled",
                        icon = "💋",
                        description = "Zmate a omráčí nepřítele na 1 kolo a drasticky sníží jeho útok.",
                        manaCost = 22,
                        cooldownTurns = 2,
                        targetType = SkillTargetType.SINGLE_ENEMY,
                        category = SkillCategory.HEX_DEBUFF,
                        powerMultiplier = 0.5f,
                        baseDamageBonus = 8,
                        appliedStatus = CombatStatusEffect("charm_stun", "Zamilované omráčení", "💫", "STUN", value = 1, durationTurns = 1),
                        animationType = "SHADOW_CURSE",
                        voiceQuote = "Budeš se dívat jen na mě..."
                    )
                )
                baseSkills.add(
                    PartyCombatSkill(
                        id = "aphrodisiac_mist",
                        name = "Afrodiziakální mlha",
                        icon = "🌸",
                        description = "Rozpráší omamný parfém, který oslabí všechny nepřátele a naplní Harémové kombo o +25 bodů.",
                        manaCost = 26,
                        cooldownTurns = 3,
                        targetType = SkillTargetType.ALL_ENEMIES,
                        category = SkillCategory.HEX_DEBUFF,
                        appliedStatus = CombatStatusEffect("mist_weaken", "Omámení", "🌸", "ATK_BUFF", value = -8, durationTurns = 2),
                        animationType = "CHAR_SPECIAL"
                    )
                )
            }
            "odvazna", "vzdorna" -> {
                baseSkills.add(
                    PartyCombatSkill(
                        id = "shield_bash",
                        name = "Úder pavézou",
                        icon = "🛡️",
                        description = "Tvrdý náraz štítem, který přitáhne útoky nepřítele na tuto dívku (Taunt) a posílí její obranu.",
                        manaCost = 14,
                        cooldownTurns = 1,
                        targetType = SkillTargetType.SINGLE_ENEMY,
                        category = SkillCategory.PHYSICAL_ATTACK,
                        powerMultiplier = 1.2f,
                        baseDamageBonus = 10,
                        appliedStatus = CombatStatusEffect("taunt_guard", "Provokace & Kryt", "🛡️", "TAUNT", value = 18, durationTurns = 2),
                        animationType = "DEFEND",
                        voiceQuote = "Jestli chceš mého pána, musíš projít přese mě!"
                    )
                )
                baseSkills.add(
                    PartyCombatSkill(
                        id = "crushing_blow",
                        name = "Drtič lebek",
                        icon = "💥",
                        description = "Masivní rozmach těžkou zbraní s drtivým dopadem.",
                        manaCost = 24,
                        cooldownTurns = 2,
                        targetType = SkillTargetType.SINGLE_ENEMY,
                        category = SkillCategory.PHYSICAL_ATTACK,
                        powerMultiplier = 1.7f,
                        baseDamageBonus = 20,
                        animationType = "HEAVY_STRIKE"
                    )
                )
            }
            else -> {
                // Default versatile skills
                baseSkills.add(
                    PartyCombatSkill(
                        id = "swift_strike",
                        name = "Bleskový výpad",
                        icon = "🗡️",
                        description = "Rychlý ztečný útok s bonusem k šanci na zásah.",
                        manaCost = 12,
                        cooldownTurns = 1,
                        targetType = SkillTargetType.SINGLE_ENEMY,
                        category = SkillCategory.PHYSICAL_ATTACK,
                        powerMultiplier = 1.3f,
                        baseDamageBonus = 8,
                        animationType = "SLASH"
                    )
                )
                baseSkills.add(
                    PartyCombatSkill(
                        id = "encourage_bond",
                        name = "Pouto věrnosti",
                        icon = "💖",
                        description = "Posílí obranu a uzdraví 25 HP vybranému spojenci.",
                        manaCost = 20,
                        cooldownTurns = 2,
                        targetType = SkillTargetType.SINGLE_ALLY,
                        category = SkillCategory.HOLY_HEAL,
                        healAmount = 25,
                        appliedStatus = CombatStatusEffect("bond_guard", "Pouto", "🛡️", "DEF_BUFF", value = 8, durationTurns = 2),
                        animationType = "HAREM_SUPPORT"
                    )
                )
            }
        }

        // Ultimate or Corruption bonus skill
        if (corruptionPhase >= 3 || affinityLevel >= 3) {
            baseSkills.add(
                PartyCombatSkill(
                    id = "unleashed_dominion_${archetypeId}",
                    name = "Vroucí temnota ($characterName)",
                    icon = "🔥",
                    description = "Uvolní zapovězenou moc temného dominia. Udělí masivní poškození všem nepřátelům.",
                    manaCost = 38,
                    cooldownTurns = 4,
                    targetType = SkillTargetType.ALL_ENEMIES,
                    category = SkillCategory.DARK_MAGIC,
                    powerMultiplier = 1.65f,
                    baseDamageBonus = 25,
                    animationType = "CHAR_SPECIAL",
                    voiceQuote = "Můj život i má duše patří tobě!"
                )
            )
        }

        return baseSkills
    }

    /**
     * Player (Lord) battle skills.
     */
    fun getPlayerSkills(combatSkillLevel: Int = 1, darkMagicLevel: Int = 1): List<PartyCombatSkill> {
        return listOf(
            PartyCombatSkill(
                id = "player_shadow_blade",
                name = "Čepel Dominia",
                icon = "🗡️",
                description = "Mocný úder očarovanou čepelí za zvýšené poškození.",
                manaCost = 10,
                cooldownTurns = 0,
                targetType = SkillTargetType.SINGLE_ENEMY,
                category = SkillCategory.PHYSICAL_ATTACK,
                powerMultiplier = 1.4f,
                baseDamageBonus = 12 + combatSkillLevel * 4,
                animationType = "SLASH"
            ),
            PartyCombatSkill(
                id = "player_command_attack",
                name = "Válečný rozkaz pána",
                icon = "👑",
                description = "Pán zavelí k frontálnímu útoku! Zvýší útok všech družek o 25% a naplní +20 Combo bodů.",
                manaCost = 20,
                cooldownTurns = 2,
                targetType = SkillTargetType.ALL_ALLIES,
                category = SkillCategory.SUPPORT_BUFF,
                appliedStatus = CombatStatusEffect("lord_command", "Pánův rozkaz", "⚔️", "ATK_BUFF", value = 12 + combatSkillLevel * 3, durationTurns = 3),
                animationType = "HAREM_SUPPORT"
            ),
            PartyCombatSkill(
                id = "player_dark_burst",
                name = "Temný výboj",
                icon = "🔮",
                description = "Prudký zásah stínovou magií ignorující brnění.",
                manaCost = 22,
                cooldownTurns = 1,
                targetType = SkillTargetType.SINGLE_ENEMY,
                category = SkillCategory.DARK_MAGIC,
                powerMultiplier = 1.6f,
                baseDamageBonus = 18 + darkMagicLevel * 5,
                animationType = "DARK_BURST"
            ),
            PartyCombatSkill(
                id = "player_soul_drain",
                name = "Vysátí duše & Léčení",
                icon = "🖤",
                description = "Vysaje životy z nepřítele a rozdělí uzdravení mezi všechny členy party.",
                manaCost = 30,
                cooldownTurns = 3,
                targetType = SkillTargetType.SINGLE_ENEMY,
                category = SkillCategory.DARK_MAGIC,
                powerMultiplier = 1.25f,
                healAmount = 30 + darkMagicLevel * 4,
                animationType = "SOUL_DRAIN"
            )
        )
    }

    /**
     * Determine Role for Character Archetype.
     */
    fun getRoleForArchetype(archetypeId: String): CombatRole {
        return when (archetypeId) {
            "odvazna", "vzdorna" -> CombatRole.TANK_GUARDIAN
            "krvava_subka" -> CombatRole.PHYSICAL_DPS
            "posedla" -> CombatRole.DARK_SORCERESS
            "ticha_panenka", "subka", "ustrasena" -> CombatRole.HEALER_PRIESTESS
            "nymfomanka", "touha" -> CombatRole.SIREN_DEBUFFER
            "slechticna", "chladna", "manipulativni" -> CombatRole.ASSASSIN_BLADE
            else -> CombatRole.PHYSICAL_DPS
        }
    }

    /**
     * Determine synergies based on party composition.
     */
    fun calculateSynergies(party: List<PartyMember>): List<PartySynergy> {
        val synergies = mutableListOf<PartySynergy>()
        val roles = party.map { it.role }

        // Trinity synergy
        if (roles.contains(CombatRole.TANK_GUARDIAN) && roles.contains(CombatRole.HEALER_PRIESTESS) && (roles.contains(CombatRole.PHYSICAL_DPS) || roles.contains(CombatRole.DARK_SORCERESS))) {
            synergies.add(
                PartySynergy(
                    id = "holy_trinity",
                    name = "Dokonalá bojová formace",
                    icon = "⭐",
                    description = "Vyvážená družina: +15% Útok, +15% Obrana a +5 Mana regenerace za kolo.",
                    attackBonusPercent = 0.15f,
                    defenseBonusPercent = 0.15f,
                    manaRegenBonus = 5
                )
            )
        }

        // Bloodlust coven
        val dpsCount = party.count { it.role == CombatRole.PHYSICAL_DPS || it.role == CombatRole.ASSASSIN_BLADE }
        if (dpsCount >= 2) {
            synergies.add(
                PartySynergy(
                    id = "blood_coven",
                    name = "Sesterstvo krve & Oceli",
                    icon = "🩸",
                    description = "2+ Útočnice v týmu: +20% k poškození a +15% šance na kritický úder.",
                    attackBonusPercent = 0.20f,
                    critBonusPercent = 15
                )
            )
        }

        // Dark Sorcery Circle
        val mageCount = party.count { it.role == CombatRole.DARK_SORCERESS || it.role == CombatRole.SIREN_DEBUFFER }
        if (mageCount >= 2) {
            synergies.add(
                PartySynergy(
                    id = "shadow_circle",
                    name = "Konvent stínových čarodějek",
                    icon = "🔮",
                    description = "2+ Magické bytosti: +25% síla kouzel a kleteb, +8 Mana regenerace.",
                    attackBonusPercent = 0.25f,
                    manaRegenBonus = 8
                )
            )
        }

        // Full Harem Vanguard (4 members)
        if (party.size >= 4) {
            synergies.add(
                PartySynergy(
                    id = "full_squad",
                    name = "Neporazitelná armáda Dominia",
                    icon = "👑",
                    description = "Plný 4-členný tým: +10% ke všem statistikám a rychlejší nabíjení Harémového comba.",
                    attackBonusPercent = 0.10f,
                    defenseBonusPercent = 0.10f,
                    critBonusPercent = 5
                )
            )
        }

        return synergies
    }

    /**
     * Pre-defined Encounter Squads for Arena & Map Expeditions.
     */
    data class PartyEncounterDefinition(
        val id: String,
        val title: String,
        val tierName: String,
        val icon: String,
        val location: String,
        val recommendedLevel: Int,
        val backgroundRes: Int,
        val enemies: List<CombatEnemy>,
        val rewardGold: Int,
        val rewardXp: Int,
        val rewardPrestige: Int,
        val lootTable: List<String>,
        val description: String
    )

    val ENCOUNTERS = listOf(
        PartyEncounterDefinition(
            id = "arena_tier_1",
            title = "Gladiátorský křest & Vlčí smečka",
            tierName = "Aréna • 1. Stupeň",
            icon = "🐺",
            location = "Tréninkový písek arény",
            recommendedLevel = 1,
            backgroundRes = R.drawable.img_arena_battle,
            enemies = listOf(
                CombatEnemy(
                    id = "gladiator_novice",
                    name = "Gladiátorský novic",
                    icon = "🗡️",
                    title = "Bojovník arény",
                    archetype = "Běžný",
                    hp = 85,
                    maxHp = 85,
                    attack = 14,
                    defense = 6,
                    speed = 10,
                    rewardGold = 35,
                    rewardXp = 20
                ),
                CombatEnemy(
                    id = "war_hound",
                    name = "Válečný bojový pes",
                    icon = "🐕",
                    title = "Cvičená bestie",
                    archetype = "Rychlý",
                    hp = 65,
                    maxHp = 65,
                    attack = 16,
                    defense = 4,
                    speed = 16,
                    rewardGold = 25,
                    rewardXp = 15
                )
            ),
            rewardGold = 120,
            rewardXp = 60,
            rewardPrestige = 2,
            lootTable = listOf("hojivy_balzam", "gift_roses"),
            description = "Úvodní zkouška pro tvůj nově sestavený harémový tým. Utkáte se s novicem a jeho zuřivým psem."
        ),
        PartyEncounterDefinition(
            id = "arena_tier_2",
            title = "Hlídka Černého syndikátu",
            tierName = "Podsvětí • 2. Stupeň",
            icon = "🗡️",
            location = "Zadní uličky metropole",
            recommendedLevel = 2,
            backgroundRes = R.drawable.img_mercenary_camp,
            enemies = listOf(
                CombatEnemy(
                    id = "syndicate_enforcer",
                    name = "Podsvětní vymahač",
                    icon = "🪓",
                    title = "Těžkooděnec",
                    archetype = "Tank",
                    hp = 140,
                    maxHp = 140,
                    attack = 18,
                    defense = 12,
                    speed = 9,
                    rewardGold = 60,
                    rewardXp = 35
                ),
                CombatEnemy(
                    id = "shadow_rogue",
                    name = "Vražedkyně z dýk",
                    icon = "🗡️",
                    title = "Stínový zabiják",
                    archetype = "DPS",
                    hp = 95,
                    maxHp = 95,
                    attack = 22,
                    defense = 6,
                    speed = 18,
                    rewardGold = 50,
                    rewardXp = 30
                ),
                CombatEnemy(
                    id = "poison_alchemist",
                    name = "Temný travič",
                    icon = "🧪",
                    title = "Podpora",
                    archetype = "Mág",
                    hp = 80,
                    maxHp = 80,
                    attack = 15,
                    defense = 5,
                    speed = 12,
                    rewardGold = 45,
                    rewardXp = 25
                )
            ),
            rewardGold = 260,
            rewardXp = 130,
            rewardPrestige = 5,
            lootTable = listOf("hojivy_balzam", "elixir_touhy", "gift_perfume"),
            description = "Trojčlenná přepadová skupina syndikátu, která se pokusila přepadnout tvé dominium ze zadních uliček."
        ),
        PartyEncounterDefinition(
            id = "arena_tier_3",
            title = "Inkviziční očistná jednotka",
            tierName = "Inkvizice • 3. Stupeň",
            icon = "⚜️",
            location = "Ruiny starého chrámu",
            recommendedLevel = 3,
            backgroundRes = R.drawable.img_alchemy_lab,
            enemies = listOf(
                CombatEnemy(
                    id = "inquisitor_crusader",
                    name = "Křižácký paladin",
                    icon = "🛡️",
                    title = "Svatý štít",
                    archetype = "Tank",
                    hp = 180,
                    maxHp = 180,
                    attack = 20,
                    defense = 16,
                    speed = 8,
                    rewardGold = 90,
                    rewardXp = 50
                ),
                CombatEnemy(
                    id = "inquisitor_purifier",
                    name = "Očistná kněžka světla",
                    icon = "✨",
                    title = "Léčitelka",
                    archetype = "Healer",
                    hp = 110,
                    maxHp = 110,
                    attack = 14,
                    defense = 8,
                    speed = 11,
                    rewardGold = 80,
                    rewardXp = 45
                ),
                CombatEnemy(
                    id = "inquisitor_marksman",
                    name = "Fanatický kušiník",
                    icon = "🏹",
                    title = "Střelec",
                    archetype = "DPS",
                    hp = 100,
                    maxHp = 100,
                    attack = 25,
                    defense = 7,
                    speed = 15,
                    rewardGold = 85,
                    rewardXp = 45
                )
            ),
            rewardGold = 450,
            rewardXp = 240,
            rewardPrestige = 10,
            lootTable = listOf("elixir_touhy", "serum_poslusnost", "kralovska_listina"),
            description = "Fanatičtí vojáci inkvizice vyslaní spálit tvé komnaty a popravit tvé poddané."
        ),
        PartyEncounterDefinition(
            id = "arena_tier_4",
            title = "Inkvizitor Černé pečeti & Osobní garda",
            tierName = "Vysoký Boss • 4. Stupeň",
            icon = "👑",
            location = "Severní pevnost",
            recommendedLevel = 4,
            backgroundRes = R.drawable.img_arena_battle,
            enemies = listOf(
                CombatEnemy(
                    id = "high_inquisitor_boss",
                    name = "Inkvizitor Černé pečeti",
                    icon = "👑",
                    title = "Vysoký inkvizitor",
                    archetype = "Boss",
                    hp = 260,
                    maxHp = 260,
                    attack = 30,
                    defense = 18,
                    speed = 14,
                    isBoss = true,
                    rewardGold = 220,
                    rewardXp = 150,
                    loreDescription = "Vládne svatým ohněm a černou pečetí ničící temná kouzla."
                ),
                CombatEnemy(
                    id = "inquisitor_guard_1",
                    name = "Osobní gardista",
                    icon = "⚔️",
                    title = "Elitní zbrojíř",
                    archetype = "Tank",
                    hp = 130,
                    maxHp = 130,
                    attack = 20,
                    defense = 14,
                    speed = 10,
                    rewardGold = 70,
                    rewardXp = 40
                ),
                CombatEnemy(
                    id = "inquisitor_guard_2",
                    name = "Bojový mnich",
                    icon = "⚡",
                    title = "Kouzelník",
                    archetype = "Mág",
                    hp = 115,
                    maxHp = 115,
                    attack = 24,
                    defense = 8,
                    speed = 13,
                    rewardGold = 70,
                    rewardXp = 40
                )
            ),
            rewardGold = 850,
            rewardXp = 450,
            rewardPrestige = 18,
            lootTable = listOf("cerna_pecet", "drahy_obojek", "serum_poslusnost"),
            description = "Smrtící střet s vysokým inkvizitorem. Pouze dokonale sehraný a loajální harém má šanci zvítězit!"
        ),
        PartyEncounterDefinition(
            id = "arena_tier_5",
            title = "Arcidémon Behemoth & Pekelný rituál",
            tierName = "Legendární Pán Pekel • 5. Stupeň",
            icon = "🔥",
            location = "Trhlina v propasti",
            recommendedLevel = 5,
            backgroundRes = R.drawable.img_dark_banner,
            enemies = listOf(
                CombatEnemy(
                    id = "archdemon_behemoth_boss",
                    name = "Arcidémon Behemoth",
                    icon = "😈",
                    title = "Pán Propasti",
                    archetype = "Legendární Boss",
                    hp = 380,
                    maxHp = 380,
                    attack = 36,
                    defense = 22,
                    speed = 15,
                    isBoss = true,
                    rewardGold = 450,
                    rewardXp = 300,
                    loreDescription = "Prastará démonická bytost schopná drtit kamenné zdi a zapalovat duše."
                ),
                CombatEnemy(
                    id = "succubus_witch_1",
                    name = "Pekelná svůdnice Lilith",
                    icon = "💋",
                    title = "Démonická kurtizána",
                    archetype = "Debuffer",
                    hp = 140,
                    maxHp = 140,
                    attack = 26,
                    defense = 10,
                    speed = 17,
                    rewardGold = 120,
                    rewardXp = 80
                ),
                CombatEnemy(
                    id = "succubus_witch_2",
                    name = "Kněžka lávy Morwen",
                    icon = "🔥",
                    title = "Pyromantka",
                    archetype = "Mág",
                    hp = 130,
                    maxHp = 130,
                    attack = 28,
                    defense = 9,
                    speed = 14,
                    rewardGold = 120,
                    rewardXp = 80
                )
            ),
            rewardGold = 1500,
            rewardXp = 800,
            rewardPrestige = 35,
            lootTable = listOf("drahy_obojek", "temny_klic", "serum_poslusnost", "kralovska_listina"),
            description = "Nejvyšší výzva v celém dominiu! Zdoláním propasti prokážeš, že tvůj harém vládne celému světu."
        )
    )
}
