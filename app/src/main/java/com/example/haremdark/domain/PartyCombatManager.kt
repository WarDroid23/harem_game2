package com.example.haremdark.domain

import com.example.haremdark.data.PartyCombatCatalog
import com.example.haremdark.models.*
import kotlin.random.Random

object PartyCombatManager {

    fun getElementMultiplier(attacker: Element, defender: Element): Float {
        return when (attacker) {
            Element.FIRE -> when (defender) {
                Element.EARTH, Element.ICE -> 1.25f
                Element.WATER -> 0.75f
                else -> 1.0f
            }
            Element.WATER -> when (defender) {
                Element.FIRE -> 1.25f
                Element.AIR, Element.ICE -> 0.75f
                else -> 1.0f
            }
            Element.EARTH -> when (defender) {
                Element.AIR, Element.LIGHTNING -> 1.25f
                Element.FIRE -> 0.75f
                else -> 1.0f
            }
            Element.AIR -> when (defender) {
                Element.WATER, Element.EARTH -> 1.25f
                Element.LIGHTNING -> 0.75f
                else -> 1.0f
            }
            Element.ICE -> when (defender) {
                Element.WATER, Element.AIR -> 1.25f
                Element.FIRE -> 0.75f
                else -> 1.0f
            }
            Element.LIGHTNING -> when (defender) {
                Element.WATER, Element.AIR -> 1.25f
                Element.EARTH -> 0.75f
                else -> 1.0f
            }
            Element.DARK -> when (defender) {
                Element.HOLY, Element.PHYSICAL -> 1.30f
                Element.DARK -> 0.80f
                else -> 1.0f
            }
            Element.HOLY -> when (defender) {
                Element.DARK -> 1.30f
                Element.HOLY -> 0.80f
                else -> 1.0f
            }
            Element.PHYSICAL -> when (defender) {
                Element.ICE, Element.EARTH -> 1.10f
                else -> 1.0f
            }
        }
    }

    fun getMatchupType(multiplier: Float): ElementalMatchupType {
        return when {
            multiplier >= 1.28f -> ElementalMatchupType.EXTREME_WEAKNESS
            multiplier >= 1.15f -> ElementalMatchupType.SUPER_EFFECTIVE
            multiplier <= 0.72f -> ElementalMatchupType.EXTREME_RESISTANCE
            multiplier <= 0.88f -> ElementalMatchupType.RESISTED
            else -> ElementalMatchupType.NEUTRAL
        }
    }

    private fun tryApplyStatusEffect(attacker: PartyMember, target: Any, isCrit: Boolean) {
        if (!isCrit) return

        val effectType = when(attacker.element) {
            Element.FIRE -> "BURN"
            Element.WATER -> "POISON"
            Element.AIR -> "STUN"
            else -> null
        }

        if (effectType != null) {
            val duration = if (effectType == "STUN") 1 else 3
            val effect = CombatStatusEffect(
                id = "${effectType}_${System.currentTimeMillis()}",
                name = when(effectType) { "BURN" -> "Hoření"; "POISON" -> "Otrávení"; "STUN" -> "Omráčení"; else -> "" },
                icon = when(effectType) { "BURN" -> "🔥"; "POISON" -> "🧪"; "STUN" -> "⚡"; else -> "" },
                type = effectType,
                value = 10, // Default DoT value
                durationTurns = duration,
                maxDuration = duration
            )

            when (target) {
                is PartyMember -> target.statusEffects.add(effect)
                is CombatEnemy -> target.statusEffects.add(effect)
            }
        }
    }

    fun triggerDomainExpansion(session: PartyCombatSession, character: PartyMember): PartyCombatSession {
        // Example logic: Only high-lineage/affinity characters can trigger
        val domain = DomainExpansionEffect(
            id = "dom_${System.currentTimeMillis()}",
            name = "Doména: ${character.name}'s Lineage",
            icon = "🌌",
            description = "Pole vlivu ${character.name} posiluje spojence.",
            statsModifier = 1.3f,
            environmentStatChange = "Všechny útoky +30%",
            originLineage = character.archetypeId
        )
        return session.copy(activeDomainExpansion = domain)
    }

    fun createSession(
        selectedCharacterIds: List<String>,
        allCharacters: List<Character>,
        player: Player,
        haremLevel: Int,
        includePlayerAsLeader: Boolean,
        encounterDef: PartyCombatCatalog.PartyEncounterDefinition
    ): PartyCombatSession {
        val partyList = mutableListOf<PartyMember>()

        if (includePlayerAsLeader) {
            val playerCombat = player.skills["boj"] ?: 1
            val playerDark = player.skills["temnota"] ?: 1
            val equippedWeapon = player.weapons.getOrNull(player.equippedWeaponIndex) ?: player.weapons.firstOrNull()
            val weaponDmg = equippedWeapon?.damage ?: 15

            val playerFormation = FormationPosition.fromString(player.partyFormationMap["player"] ?: "MID")

            val playerMember = PartyMember(
                id = "player",
                name = "Pán Dominia",
                isPlayer = true,
                archetypeId = "player",
                role = CombatRole.PHYSICAL_DPS,
                formationPosition = playerFormation,
                hp = player.hp,
                maxHp = player.maxHp,
                mana = (player.darkEnergy + player.sexEnergy).coerceAtMost(100),
                maxMana = 100,
                attack = 22 + playerCombat * 3 + weaponDmg / 2,
                defense = 14 + (player.skills["obrana"] ?: 0) * 2,
                speed = 14,
                critRatePercent = 15 + playerCombat * 2,
                skills = PartyCombatCatalog.getPlayerSkills(playerCombat, playerDark),
                loyaltyTierName = "👑 Pán",
                loyaltyValue = 100,
                loyaltyCombatBonusDmg = 1.0f,
                loyaltyCombatBonusDef = 1.0f,
                loyaltyCombatBonusCrit = 0,
                loyaltyAssistChancePercent = 0,
                loyaltyProtectLordChancePercent = 0,
                loyaltyCombatPerkTag = "LORD",
                loyaltyCombatDescription = "Pán a vládce dominia",
                affinityBonusDmg = 1.25f,
                element = Element.DARK,
                elementalMultipliers = mapOf("DARK" to 1.25f, "PHYSICAL" to 1.10f),
                favoriteWeaponIcon = "👑"
            )
            partyList.add(playerMember)
        }

        // Add selected harem girls
        selectedCharacterIds.forEach { charId ->
            val char = allCharacters.find { it.id == charId }
            if (char != null) {
                val role = PartyCombatCatalog.getRoleForArchetype(char.archetype)
                val combatSkill = char.skills["combat"] ?: 5
                val eqBonusAtk = char.equipment.values.filterNotNull().sumOf { it.combatBonus }
                val eqBonusHp = char.equipment.values.filterNotNull().sumOf { it.hpBonus }

                val affinityTier = com.example.haremdark.data.AffinityData.getTierForPoints(char.affinityPoints)
                val combatBonuses = com.example.haremdark.data.AffinityData.getAffinityCombatBonuses(affinityTier.level)
                val charSpecificBuff = com.example.haremdark.data.AffinityData.getCharacterSpecificBuff(char.archetypeId, affinityTier.level)
                val affinityDmgMult = (1.0f + (affinityTier.level * 0.05f) + (if (char.oblibena) 0.10f else 0f)) * (1.0f + combatBonuses.dmgMultiplierBonus + charSpecificBuff.attackBonusPercent)
                val passiveBonuses = com.example.haremdark.data.CharacterSkillCatalog.calculatePassiveBonuses(char)
                val unlockedActiveSkills = com.example.haremdark.data.CharacterSkillCatalog.getUnlockedActiveSkills(char)
                val baseSkills = PartyCombatCatalog.getSkillsForCharacter(
                    archetypeId = char.archetype,
                    corruptionPhase = char.fazeZkazenosti,
                    affinityLevel = affinityTier.level,
                    characterName = char.name
                )
                val allSkills = (baseSkills + unlockedActiveSkills).distinctBy { it.id }

                val loyaltyBonus = com.example.haremdark.data.LoyaltyCombatData.getBonusForLoyalty(char.loajalita)
                val baseAtk = 18 + combatSkill * 2 + eqBonusAtk + char.fazeZkazenosti * 3 + passiveBonuses.attackBonus
                val finalAtk = ((baseAtk * (1.0f + combatBonuses.dmgMultiplierBonus + charSpecificBuff.attackBonusPercent)) * loyaltyBonus.attackMultiplier).toInt()
                val baseDef = 10 + (char.skills["defense"] ?: 3) + (if (role == CombatRole.TANK_GUARDIAN) 8 else 0) + passiveBonuses.defenseBonus + combatBonuses.defenseBonus + charSpecificBuff.defenseBonus
                val finalDef = (baseDef * loyaltyBonus.defenseMultiplier).toInt()
                val finalCrit = 10 + (if (role == CombatRole.PHYSICAL_DPS || role == CombatRole.ASSASSIN_BLADE) 15 else 0) + passiveBonuses.critBonus + combatBonuses.critBonus + charSpecificBuff.critBonusPercent + loyaltyBonus.critBonusPercent

                // Determine formation position
                val chosenFormation = if (player.partyFormationMap.containsKey(char.id)) {
                    FormationPosition.fromString(player.partyFormationMap[char.id])
                } else if (char.preferredFormation.isNotBlank() && char.preferredFormation != "MID") {
                    FormationPosition.fromString(char.preferredFormation)
                } else {
                    when (role) {
                        CombatRole.TANK_GUARDIAN -> FormationPosition.FRONT_LINE
                        CombatRole.DARK_SORCERESS, CombatRole.HEALER_PRIESTESS, CombatRole.SIREN_DEBUFFER -> FormationPosition.BACK_LINE
                        else -> FormationPosition.MID_LINE
                    }
                }

                val member = PartyMember(
                    id = char.id,
                    name = char.name,
                    isPlayer = false,
                    archetypeId = char.archetype,
                    role = role,
                    formationPosition = chosenFormation,
                    hp = char.hp + eqBonusHp + passiveBonuses.hpBonus + combatBonuses.hpBonus + charSpecificBuff.hpBonus,
                    maxHp = char.maxHp + eqBonusHp + passiveBonuses.hpBonus + combatBonuses.hpBonus + charSpecificBuff.hpBonus,
                    mana = 50 + char.fazeZkazenosti * 5,
                    maxMana = 50 + char.fazeZkazenosti * 5,
                    attack = finalAtk,
                    defense = finalDef,
                    speed = 12 + (if (role == CombatRole.ASSASSIN_BLADE) 6 else 0) + passiveBonuses.speedBonus,
                    critRatePercent = finalCrit.coerceAtLeast(1),
                    skills = allSkills,
                    loyaltyTierName = "${loyaltyBonus.icon} ${loyaltyBonus.title}",
                    loyaltyValue = char.loajalita,
                    loyaltyCombatBonusDmg = loyaltyBonus.attackMultiplier,
                    loyaltyCombatBonusDef = loyaltyBonus.defenseMultiplier,
                    loyaltyCombatBonusCrit = loyaltyBonus.critBonusPercent,
                    loyaltyAssistChancePercent = loyaltyBonus.assistStrikeChancePercent,
                    loyaltyProtectLordChancePercent = loyaltyBonus.protectLordChancePercent,
                    loyaltyCombatPerkTag = loyaltyBonus.perkTag,
                    loyaltyCombatDescription = loyaltyBonus.summaryText,
                    affinityBonusDmg = affinityDmgMult,
                    element = when (char.archetypeId) {
                        "sukuba", "krvava_subka" -> Element.DARK
                        "chladna" -> Element.ICE
                        "draci_divka" -> Element.FIRE
                        "subka" -> Element.WATER
                        "touha" -> Element.LIGHTNING
                        "knezkyn" -> Element.HOLY
                        "vzdorna" -> Element.EARTH
                        else -> Element.PHYSICAL
                    },
                    elementalMultipliers = char.elementalMultipliers.toMap(),
                    favoriteWeaponIcon = if (role == CombatRole.TANK_GUARDIAN) "🛡️" else if (role == CombatRole.DARK_SORCERESS) "🔮" else "🗡️",
                    relationshipTierLevel = affinityTier.level,
                    relationshipStageName = "${affinityTier.icon} ${affinityTier.stageName}",
                    relationshipCombatDescription = "${affinityTier.combatBonusDescription} | ${charSpecificBuff.icon} ${charSpecificBuff.name}: ${charSpecificBuff.perkEffectSummary}",
                    combatRegenBonus = combatBonuses.regenBonus + charSpecificBuff.hpRegenPerTurn + loyaltyBonus.hpRegenPerTurn,
                    characterSpecificBuffName = charSpecificBuff.name,
                    characterSpecificBuffIcon = charSpecificBuff.icon,
                    characterSpecificBuffSummary = charSpecificBuff.perkEffectSummary,
                    specialEffectTag = charSpecificBuff.specialEffectTag,
                    strategy = char.preferredCombatRole
                )

                // Apply strategy modifiers to base stats
                when (member.strategy) {
                    CombatStrategy.AGGRESSIVE -> {
                        member.attack = (member.attack * 1.20f).toInt()
                        member.defense = (member.defense * 0.85f).toInt()
                    }
                    CombatStrategy.DEFENSIVE -> {
                        member.defense = (member.defense * 1.20f).toInt()
                        member.attack = (member.attack * 0.85f).toInt()
                    }
                    CombatStrategy.SUPPORT -> {
                        member.attack = (member.attack * 0.80f).toInt()
                    }
                    CombatStrategy.BALANCED -> {}
                }
                
                partyList.add(member)
            }
        }

        // Apply Slot Bonuses based on index
        partyList.forEachIndexed { index, member ->
            val posIndex = if (includePlayerAsLeader && member.isPlayer) -1 else if (includePlayerAsLeader) index else index + 1
            when (posIndex) {
                1 -> { // Vanguard (Slot 1)
                    member.hp += (member.maxHp * 0.15).toInt()
                    member.defense += (member.defense * 0.20).toInt()
                }
                2 -> { // Flank (Slot 2)
                    member.attack += (member.attack * 0.10).toInt()
                    member.speed += (member.speed * 0.10).toInt()
                }
                3 -> { // Rearguard (Slot 3)
                    member.critRatePercent += 15
                }
                4 -> { // Support (Slot 4)
                    member.speed += 5
                    member.defense += 5
                }
            }
        }

        // Deep copy enemies and assign enemy formations
        val clonedEnemies = encounterDef.enemies.mapIndexed { idx, enemy ->
            val enemyFormation = if (enemy.isBoss || idx == 0) FormationPosition.FRONT_LINE else if (idx % 2 == 1) FormationPosition.MID_LINE else FormationPosition.BACK_LINE
            enemy.copy(
                formationPosition = enemyFormation,
                statusEffects = mutableListOf()
            )
        }
        val synergies = PartyCombatCatalog.calculateSynergies(partyList)
        val elementalSynergies = ElementalSynergyManager.calculateActiveSynergies(partyList)
        val combatWeather = CombatWeather.getWeatherForLocation(encounterDef.location)
        val environmentalHazard = EnvironmentalHazard.getHazardForLocation(encounterDef.location)
        val teamFormationSynergy = TeamFormationSynergy.calculateFormationSynergy(partyList)

        SoundEffectManager.playCombat(CombatSound.COMBAT_START)

        val startingCombo = if (teamFormationSynergy.name.contains("Útočný", ignoreCase = true)) 35 else 20

        val startsWithArena = encounterDef.id.startsWith("arena")
        val backgroundRes = if (startsWithArena) {
            when (player.domainExpansionLevel) {
                1 -> com.example.haremdark.R.drawable.img_arena_battle
                2 -> com.example.haremdark.R.drawable.img_arena_domain_2
                3 -> com.example.haremdark.R.drawable.img_arena_domain_3
                4 -> com.example.haremdark.R.drawable.img_arena_domain_4
                else -> com.example.haremdark.R.drawable.img_arena_domain_5
            }
        } else {
            encounterDef.backgroundRes
        }

        val initialLogs = mutableListOf(
            CombatLogEntry(
                turn = 1,
                type = "system",
                message = "🛡️ Formace týmu: ${teamFormationSynergy.icon} ${teamFormationSynergy.name} (${teamFormationSynergy.teamBuffDescription}).",
                actor = "Taktická formace",
                actionName = teamFormationSynergy.name
            ),
            CombatLogEntry(
                turn = 1,
                type = "system",
                message = "⚔️ Střet začíná! Terén: ${environmentalHazard.icon} ${environmentalHazard.name} (${environmentalHazard.terrainModifierDesc}).",
                actor = "Terénní prostředí",
                actionName = environmentalHazard.title
            ),
            CombatLogEntry(
                turn = 1,
                type = "system",
                message = "🌤️ Počasí na bojišti: ${combatWeather.icon} ${combatWeather.name} (${combatWeather.description}).",
                actor = "Meteorologická anomálie",
                actionName = "Klima bojiště"
            )
        )

        elementalSynergies.forEach { syn ->
            initialLogs.add(
                CombatLogEntry(
                    turn = 1,
                    type = "player_support",
                    message = "✨ [Elementární Synergie: ${syn.icon} ${syn.name}] Aktivní pasivní štít! ${syn.bonusPerkDescription} (Rezonance: ${syn.participatingMemberNames.joinToString()})",
                    actor = "Elementární Rezonance",
                    actionName = syn.name
                )
            )
        }

        return PartyCombatSession(
            id = "combat_${System.currentTimeMillis()}",
            encounterTitle = encounterDef.title,
            encounterLocation = encounterDef.location,
            backgroundDrawableRes = backgroundRes,
            party = partyList,
            enemies = clonedEnemies,
            currentTurnIndex = 0,
            currentRound = 1,
            isEnemyPhase = false,
            selectedTargetEnemyIndex = 0,
            selectedTargetAllyIndex = 0,
            haremComboGauge = startingCombo,
            activeSynergies = synergies,
            elementalSynergies = elementalSynergies,
            weather = combatWeather,
            environmentalHazard = environmentalHazard,
            hazardCountdown = environmentalHazard.triggerIntervalTurns,
            teamFormationSynergy = teamFormationSynergy,
            combatLogs = initialLogs,
            isFinished = false,
            isVictory = false
        )
    }

    /**
     * Execute a regular basic attack for the currently active party member.
     */
    fun executeBasicAttack(
        session: PartyCombatSession,
        targetEnemyIndex: Int
    ): Pair<PartyCombatSession, String> {
        val activeMember = session.currentActiveMember ?: return Pair(session, "Není na tahu žádná bojovnice!")
        val aliveEnemies = session.enemies.filter { it.isAlive }
        val target = aliveEnemies.getOrNull(targetEnemyIndex) ?: aliveEnemies.firstOrNull()
            ?: return Pair(session, "Žádný platný cíl!")

        // Calculate damage with combo chain bonus and formation modifiers
        val newChain = session.comboChainCount + 1
        val comboDmgMult = 1.0f + (newChain * 0.04f)
        val comboCritBonus = newChain * 2
        val formCritBonus = activeMember.formationPosition.critModifierPercent
        val isCrit = Random.nextInt(100) < (activeMember.critRatePercent + (session.activeSynergies.sumOf { it.critBonusPercent }) + comboCritBonus + formCritBonus)
        val critMult = if (isCrit) 1.65f else 1.0f
        val formAtkBonus = 1.0f + activeMember.formationPosition.physicalAttackModifierPercent
        val synergyAtkBonus = (1.0f + session.activeSynergies.map { it.attackBonusPercent }.sum()) * formAtkBonus
        val buffAtk = activeMember.statusEffects.filter { it.type == "ATK_BUFF" }.sumOf { it.value }

        val isHesitant = (!activeMember.isPlayer && activeMember.loyaltyValue < 30 && Random.nextInt(100) < 20)
        val loyaltyDmgMod = if (isHesitant) 0.75f else 1.0f

        val affinityMultiplier = activeMember.elementalMultipliers[activeMember.element.name]
            ?: activeMember.affinityBonusDmg

        val elementMult = getElementMultiplier(activeMember.element, target.element)
        val matchupType = getMatchupType(elementMult)

        val baseAtkVal = activeMember.attack + buffAtk + Random.nextInt(-2, 4)
        val defMitigation = (target.defense * 0.4f).toInt()

        val rawDmg = (baseAtkVal * synergyAtkBonus * comboDmgMult) * affinityMultiplier * elementMult
        val finalDmg = (((rawDmg - defMitigation) * critMult) * loyaltyDmgMod).toInt().coerceAtLeast(6)

        val rawWithoutAffinity = (baseAtkVal * synergyAtkBonus * comboDmgMult) * 1.0f * elementMult
        val finalWithoutAffinity = (((rawWithoutAffinity - defMitigation) * critMult) * loyaltyDmgMod).toInt().coerceAtLeast(6)
        val affinityBonusGained = (finalDmg - finalWithoutAffinity).coerceAtLeast(0)

        val rawWithoutMatchup = (baseAtkVal * synergyAtkBonus * comboDmgMult) * affinityMultiplier * 1.0f
        val finalWithoutMatchup = (((rawWithoutMatchup - defMitigation) * critMult) * loyaltyDmgMod).toInt().coerceAtLeast(6)
        val matchupBonusGained = finalDmg - finalWithoutMatchup

        val formulaStr = "[Báze: $baseAtkVal] × [Afinita: ${"%.2f".format(affinityMultiplier)}x] × [Živel: ${"%.2f".format(elementMult)}x]${if (isCrit) " × [Krit: 1.65x]" else ""} - [Obrana: $defMitigation] = $finalDmg DMG"

        val tacticalNote = buildString {
            if (affinityBonusGained > 0) {
                append("Trénink afinity přidal +${affinityBonusGained} DMG (${"%.2f".format(affinityMultiplier)}x). ")
            }
            when (matchupType) {
                ElementalMatchupType.EXTREME_WEAKNESS, ElementalMatchupType.SUPER_EFFECTIVE -> {
                    append("${activeMember.element.name} zasáhl slabinu ${target.element.name}! (+${matchupBonusGained} DMG)")
                }
                ElementalMatchupType.EXTREME_RESISTANCE, ElementalMatchupType.RESISTED -> {
                    append("${target.element.name} odolal živlu ${activeMember.element.name} (${matchupBonusGained} DMG).")
                }
                ElementalMatchupType.NEUTRAL -> {
                    append("Neutrální elementární interakce.")
                }
            }
        }

        val breakdown = ElementalDamageBreakdown(
            turn = session.combatLogs.size + 1,
            round = session.currentRound,
            attackerName = activeMember.name,
            attackerElement = activeMember.element,
            defenderName = target.name,
            defenderElement = target.element,
            isPlayerPartyAttacker = true,
            actionName = "Základní útok",
            basePower = baseAtkVal,
            affinityMultiplier = affinityMultiplier,
            elementMatchupMultiplier = elementMult,
            matchupType = matchupType,
            isCritical = isCrit,
            critMultiplier = critMult,
            comboMultiplier = comboDmgMult,
            synergyMultiplier = synergyAtkBonus,
            targetDefense = target.defense,
            defenseMitigation = defMitigation,
            rawCalculatedDamage = rawDmg.toInt(),
            finalDamage = finalDmg,
            affinityBonusDamageGained = affinityBonusGained,
            matchupBonusDamageGained = matchupBonusGained,
            formulaDisplay = formulaStr,
            tacticalNote = tacticalNote
        )

        // Apply damage to enemy
        target.hp = (target.hp - finalDmg).coerceAtLeast(0)
        tryApplyStatusEffect(activeMember, target, isCrit)
        
        // Trigger visual effect for elemental affinity
        if (elementMult > 1.0f) {
            // FxManager would be ideal, but we access fxState via UI layer.
            // Since we are in the domain layer, this needs to be passed via UI session or event.
            // For now, assume a simple event listener or direct trigger if available.
        }

        // Charge Harem Combo (Devotion tier increases combo gain by 50%)
        val comboLoyaltyBonus = if (activeMember.loyaltyValue >= 95) 1.5f else 1.0f
        val newCombo = (session.haremComboGauge + ((if (isCrit) 18 else 10) * comboLoyaltyBonus).toInt()).coerceAtMost(session.maxHaremComboGauge)

        // Sound effect
        if (isCrit) {
            if (newChain >= 3) {
                SoundEffectManager.playCombat(CombatSound.CRITICAL_SUPERNOVA)
            } else {
                SoundEffectManager.playCombat(CombatSound.CRITICAL_HIT)
            }
        } else {
            SoundEffectManager.playCombat(CombatSound.PLAYER_SLASH)
        }

        val critTag = if (isCrit) " 💥 KRITICKÝ ZÁSAH!" else ""
        val chainTag = if (newChain > 1) " 🔥 [Kombo x$newChain]" else ""
        val hesitationTag = if (isHesitant) " ⚠️ [Zaváhání z nízké loajality -25%]" else ""
        val logMsg = "🗡️ ${activeMember.name} zaútočila na ${target.name} a udělila $finalDmg poškození!$critTag$chainTag$hesitationTag"

        val newLog = CombatLogEntry(
            turn = session.currentRound,
            type = if (isCrit) "player_special" else "player_attack",
            message = logMsg,
            actor = activeMember.name,
            actionName = "Základní útok (Kombo x$newChain)",
            damageDealt = finalDmg,
            damageCalculation = formulaStr,
            elementalBreakdown = breakdown
        )

        // Check for Devotion Assist from loyal harem companions
        val assistLogs = mutableListOf<CombatLogEntry>()
        if (target.isAlive) {
            val loyalAssisters = session.party.filter { it.isAlive && it.id != activeMember.id && it.loyaltyAssistChancePercent > 0 }
            for (assister in loyalAssisters) {
                if (target.isAlive && Random.nextInt(100) < assister.loyaltyAssistChancePercent) {
                    val assistDmg = ((assister.attack * 0.45f) * assister.affinityBonusDmg).toInt().coerceAtLeast(6)
                    target.hp = (target.hp - assistDmg).coerceAtLeast(0)
                    assistLogs.add(
                        CombatLogEntry(
                            turn = session.currentRound,
                            type = "player_special",
                            message = "💖 [Věrný protiúder] ${assister.name} (Loajalita ${assister.loyaltyValue}%) z oddanosti k Pánu asistovala bleskovým výpadem za $assistDmg poškození!",
                            actor = assister.name,
                            actionName = "Věrný protiúder",
                            damageDealt = assistDmg
                        )
                    )
                }
            }
        }

        val updatedLogs = listOf(newLog) + assistLogs + session.combatLogs

        var nextSession = session.copy(
            haremComboGauge = newCombo,
            comboChainCount = newChain,
            combatLogs = updatedLogs
        )

        // Check if all enemies defeated
        if (nextSession.enemies.none { it.isAlive }) {
            return handleVictory(nextSession)
        }

        // Advance party turn or transition to enemy phase
        return advanceTurn(nextSession)
    }

    /**
     * Execute a skill action by the active member.
     */
    fun executeSkill(
        session: PartyCombatSession,
        skillId: String,
        targetIndex: Int
    ): Pair<PartyCombatSession, String> {
        val activeMember = session.currentActiveMember ?: return Pair(session, "Není na tahu žádná bojovnice!")
        val skill = activeMember.skills.find { it.id == skillId } ?: return Pair(session, "Neznámá dovednost!")

        if (activeMember.mana < skill.manaCost) {
            return Pair(session, "Nedostatek many na dovednost ${skill.name} (vyžaduje ${skill.manaCost} MP)!")
        }

        // Deduct Mana
        activeMember.mana = (activeMember.mana - skill.manaCost).coerceAtLeast(0)

        val newLogs = mutableListOf<CombatLogEntry>()
        var comboGain = 12

        val newChain = session.comboChainCount + 1
        val comboDmgMult = 1.0f + (newChain * 0.04f)
        val synergyAtk = (1.0f + session.activeSynergies.map { it.attackBonusPercent }.sum()) * comboDmgMult

        when (skill.targetType) {
            SkillTargetType.SINGLE_ENEMY -> {
                val aliveEnemies = session.enemies.filter { it.isAlive }
                val target = aliveEnemies.getOrNull(targetIndex) ?: aliveEnemies.firstOrNull()
                if (target != null) {
                    val affinityMultiplier = activeMember.elementalMultipliers[activeMember.element.name]
                        ?: activeMember.affinityBonusDmg
                    val elementMult = getElementMultiplier(activeMember.element, target.element)
                    val matchupType = getMatchupType(elementMult)

                    val baseAtkVal = ((activeMember.attack * skill.powerMultiplier) + skill.baseDamageBonus).toInt()
                    val defMitigation = (target.defense * 0.3f).toInt()
                    val rawDmg = baseAtkVal * affinityMultiplier * synergyAtk * elementMult
                    val finalDmg = (rawDmg - defMitigation).toInt().coerceAtLeast(10)

                    val rawWithoutAffinity = baseAtkVal * 1.0f * synergyAtk * elementMult
                    val finalWithoutAffinity = (rawWithoutAffinity - defMitigation).toInt().coerceAtLeast(10)
                    val affinityBonusGained = (finalDmg - finalWithoutAffinity).coerceAtLeast(0)

                    val rawWithoutMatchup = baseAtkVal * affinityMultiplier * synergyAtk * 1.0f
                    val finalWithoutMatchup = (rawWithoutMatchup - defMitigation).toInt().coerceAtLeast(10)
                    val matchupBonusGained = finalDmg - finalWithoutMatchup

                    val formulaStr = "[Dovednost: $baseAtkVal] × [Afinita: ${"%.2f".format(affinityMultiplier)}x] × [Živel: ${"%.2f".format(elementMult)}x] - [Obrana: $defMitigation] = $finalDmg DMG"

                    val tacticalNote = buildString {
                        if (affinityBonusGained > 0) {
                            append("Trénink afinity přidal +${affinityBonusGained} DMG (${"%.2f".format(affinityMultiplier)}x). ")
                        }
                        when (matchupType) {
                            ElementalMatchupType.EXTREME_WEAKNESS, ElementalMatchupType.SUPER_EFFECTIVE -> {
                                append("${activeMember.element.name} udeřil do slabiny ${target.element.name}! (+${matchupBonusGained} DMG)")
                            }
                            ElementalMatchupType.EXTREME_RESISTANCE, ElementalMatchupType.RESISTED -> {
                                append("${target.element.name} částečně ztlumil ${activeMember.element.name} (${matchupBonusGained} DMG).")
                            }
                            ElementalMatchupType.NEUTRAL -> {
                                append("Neutrální elementární interakce.")
                            }
                        }
                    }

                    val breakdown = ElementalDamageBreakdown(
                        turn = session.combatLogs.size + 1,
                        round = session.currentRound,
                        attackerName = activeMember.name,
                        attackerElement = activeMember.element,
                        defenderName = target.name,
                        defenderElement = target.element,
                        isPlayerPartyAttacker = true,
                        actionName = skill.name,
                        basePower = baseAtkVal,
                        affinityMultiplier = affinityMultiplier,
                        elementMatchupMultiplier = elementMult,
                        matchupType = matchupType,
                        isCritical = false,
                        comboMultiplier = comboDmgMult,
                        synergyMultiplier = synergyAtk,
                        targetDefense = target.defense,
                        defenseMitigation = defMitigation,
                        rawCalculatedDamage = rawDmg.toInt(),
                        finalDamage = finalDmg,
                        affinityBonusDamageGained = affinityBonusGained,
                        matchupBonusDamageGained = matchupBonusGained,
                        formulaDisplay = formulaStr,
                        tacticalNote = tacticalNote
                    )

                    target.hp = (target.hp - finalDmg).coerceAtLeast(0)

                    if (skill.appliedStatus != null) {
                        target.statusEffects.add(skill.appliedStatus.copy())
                    }

                    if (skill.healAmount > 0) {
                        val healBonus = if (activeMember.strategy == CombatStrategy.SUPPORT) 1.25f else 1.0f
                        val finalHeal = (skill.healAmount * healBonus).toInt()
                        activeMember.hp = (activeMember.hp + finalHeal).coerceAtMost(activeMember.maxHp)
                        SoundEffectManager.playCombat(CombatSound.HEAL_RESTORE)
                    } else if (skill.category == SkillCategory.DARK_MAGIC) {
                        SoundEffectManager.playCombat(CombatSound.DARK_SPELL)
                    } else {
                        SoundEffectManager.playCombat(CombatSound.SKILL_ACTIVATION)
                    }

                    newLogs.add(
                        CombatLogEntry(
                            turn = session.currentRound,
                            type = if (skill.category == SkillCategory.DARK_MAGIC) "player_spell" else "player_special",
                            message = "${skill.icon} ${activeMember.name} použila '${skill.name}' na ${target.name} za $finalDmg poškození!",
                            actor = activeMember.name,
                            actionName = skill.name,
                            damageDealt = finalDmg,
                            damageCalculation = formulaStr,
                            elementalBreakdown = breakdown,
                            narrativeText = skill.voiceQuote ?: "${activeMember.name} soustředila svou sílu do zničujícího úderu."
                        )
                    )

                    // Devotion assist on skill
                    if (target.isAlive) {
                        val loyalAssisters = session.party.filter { it.isAlive && it.id != activeMember.id && it.loyaltyAssistChancePercent > 0 }
                        for (assister in loyalAssisters) {
                            if (target.isAlive && Random.nextInt(100) < assister.loyaltyAssistChancePercent) {
                                val assistDmg = ((assister.attack * 0.40f) * assister.affinityBonusDmg).toInt().coerceAtLeast(6)
                                target.hp = (target.hp - assistDmg).coerceAtLeast(0)
                                newLogs.add(
                                    CombatLogEntry(
                                        turn = session.currentRound,
                                        type = "player_special",
                                        message = "💖 [Věrný protiúder] ${assister.name} (Loajalita ${assister.loyaltyValue}%) navázala na dovednost '${skill.name}' asistenčním úderem za $assistDmg DMG!",
                                        actor = assister.name,
                                        actionName = "Věrný protiúder",
                                        damageDealt = assistDmg
                                    )
                                )
                            }
                        }
                    }
                }
            }
            SkillTargetType.ALL_ENEMIES -> {
                val aliveEnemies = session.enemies.filter { it.isAlive }
                var totalDmg = 0
                aliveEnemies.forEach { enemy ->
                    val elementMult = getElementMultiplier(activeMember.element, enemy.element)
                    val rawDmg = ((activeMember.attack * skill.powerMultiplier) + skill.baseDamageBonus) * activeMember.affinityBonusDmg * synergyAtk * elementMult
                    val finalDmg = (rawDmg - (enemy.defense * 0.25f)).toInt().coerceAtLeast(8)
                    enemy.hp = (enemy.hp - finalDmg).coerceAtLeast(0)
                    totalDmg += finalDmg

                    if (skill.appliedStatus != null) {
                        enemy.statusEffects.add(skill.appliedStatus.copy())
                    }
                }

                if (skill.healAmount > 0) {
                    val healBonus = if (activeMember.strategy == CombatStrategy.SUPPORT) 1.25f else 1.0f
                    val finalHeal = (skill.healAmount * healBonus).toInt()
                    session.party.filter { it.isAlive }.forEach { ally ->
                        ally.hp = (ally.hp + finalHeal).coerceAtMost(ally.maxHp)
                    }
                }

                comboGain = 20
                SoundEffectManager.playCombat(CombatSound.DARK_SPELL)

                newLogs.add(
                    CombatLogEntry(
                        turn = session.currentRound,
                        type = "player_spell",
                        message = "${skill.icon} ${activeMember.name} zasáhla celou skupinu nepřátel dovedností '${skill.name}' za celkem $totalDmg poškození!",
                        actor = activeMember.name,
                        actionName = skill.name,
                        damageDealt = totalDmg
                    )
                )
            }
            SkillTargetType.SINGLE_ALLY -> {
                val aliveParty = session.aliveParty
                val targetAlly = aliveParty.getOrNull(targetIndex) ?: activeMember
                if (skill.healAmount > 0) {
                    val healBonus = if (activeMember.strategy == CombatStrategy.SUPPORT) 1.25f else 1.0f
                    val finalHeal = (skill.healAmount * healBonus).toInt()
                    val prevHp = targetAlly.hp
                    targetAlly.hp = (targetAlly.hp + finalHeal).coerceAtMost(targetAlly.maxHp)
                    val healed = targetAlly.hp - prevHp

                    if (skill.appliedStatus != null) {
                        targetAlly.statusEffects.add(skill.appliedStatus.copy())
                    }

                    SoundEffectManager.playCombat(CombatSound.SHIELD_BLOCK)

                    newLogs.add(
                        CombatLogEntry(
                            turn = session.currentRound,
                            type = "player_heal",
                            message = "${skill.icon} ${activeMember.name} vyléčila spojenkyni ${targetAlly.name} o +$healed HP!",
                            actor = activeMember.name,
                            actionName = skill.name
                        )
                    )
                }
            }
            SkillTargetType.ALL_ALLIES -> {
                val healBonus = if (activeMember.strategy == CombatStrategy.SUPPORT) 1.25f else 1.0f
                val finalHeal = (skill.healAmount * healBonus).toInt()
                session.aliveParty.forEach { ally ->
                    if (skill.healAmount > 0) {
                        ally.hp = (ally.hp + finalHeal).coerceAtMost(ally.maxHp)
                    }
                    if (skill.appliedStatus != null) {
                        ally.statusEffects.add(skill.appliedStatus.copy())
                    }
                    ally.mana = (ally.mana + 10).coerceAtMost(ally.maxMana)
                }

                SoundEffectManager.playCombat(CombatSound.SHIELD_BLOCK)

                newLogs.add(
                    CombatLogEntry(
                        turn = session.currentRound,
                        type = "player_support",
                        message = "${skill.icon} ${activeMember.name} požehnala celému týmu dovedností '${skill.name}'!",
                        actor = activeMember.name,
                        actionName = skill.name
                    )
                )
            }
            SkillTargetType.SELF -> {
                if (skill.appliedStatus != null) {
                    activeMember.statusEffects.add(skill.appliedStatus.copy())
                }
                if (skill.healAmount > 0) {
                    activeMember.hp = (activeMember.hp + skill.healAmount).coerceAtMost(activeMember.maxHp)
                }
                newLogs.add(
                    CombatLogEntry(
                        turn = session.currentRound,
                        type = "player_support",
                        message = "${skill.icon} ${activeMember.name} aktivovala '${skill.name}'!",
                        actor = activeMember.name,
                        actionName = skill.name
                    )
                )
            }
        }

        val updatedCombo = (session.haremComboGauge + comboGain).coerceAtMost(session.maxHaremComboGauge)
        var nextSession = session.copy(
            haremComboGauge = updatedCombo,
            comboChainCount = newChain,
            combatLogs = newLogs + session.combatLogs
        )

        // Check if all enemies defeated
        if (nextSession.enemies.none { it.isAlive }) {
            return handleVictory(nextSession)
        }

        return advanceTurn(nextSession)
    }

    /**
     * Set active member to Defending stance (Takes 60% less damage and recovers Mana).
     */
    fun executeDefend(session: PartyCombatSession): Pair<PartyCombatSession, String> {
        val activeMember = session.currentActiveMember ?: return Pair(session, "Není na tahu žádná bojovnice!")

        activeMember.isDefending = true
        activeMember.mana = (activeMember.mana + 15).coerceAtMost(activeMember.maxMana)

        SoundEffectManager.playCombat(CombatSound.SHIELD_BLOCK)

        val log = CombatLogEntry(
            turn = session.currentRound,
            type = "player_defend",
            message = "🛡️ ${activeMember.name} zaujala obranný postoj (-60% poškození, +15 MP).",
            actor = activeMember.name,
            actionName = "Obrana & Kryt"
        )

        val nextSession = session.copy(
            comboChainCount = 0,
            combatLogs = listOf(log) + session.combatLogs
        )

        return advanceTurn(nextSession)
    }

    /**
     * Execute full Harem Ultimate Combo attack ("Nespoutaný hněv Harému Dominia").
     */
    fun executeHaremComboUltimate(session: PartyCombatSession): Pair<PartyCombatSession, String> {
        if (!session.isComboReady) {
            return Pair(session, "Harémové kombo ještě není plně nabito (${session.haremComboGauge}/100)!")
        }

        val aliveParty = session.aliveParty
        val aliveEnemies = session.aliveEnemies
        if (aliveParty.isEmpty() || aliveEnemies.isEmpty()) return Pair(session, "Nelze provést kombo!")

        // Calculate combined party power
        val totalPartyAtk = aliveParty.sumOf { it.attack }
        val comboDmgPerEnemy = (totalPartyAtk * 1.8f / aliveEnemies.size.coerceAtLeast(1) + 40).toInt()

        aliveEnemies.forEach { enemy ->
            enemy.hp = (enemy.hp - comboDmgPerEnemy).coerceAtLeast(0)
            enemy.statusEffects.add(CombatStatusEffect("stun_combo", "Omráčení z komba", "💫", "STUN", value = 1, durationTurns = 1))
        }

        // Heal party & restore Mana
        aliveParty.forEach { ally ->
            ally.hp = (ally.hp + 35).coerceAtMost(ally.maxHp)
            ally.mana = (ally.mana + 25).coerceAtMost(ally.maxMana)
        }

        SoundEffectManager.playCombat(CombatSound.BOSS_SPECIAL)

        val names = aliveParty.joinToString(", ") { it.name }
        val comboLog = CombatLogEntry(
            turn = session.currentRound,
            type = "player_special",
            message = "💖🔥 HARÉMOVÉ ULTIMATE KOMBO! Celá družina ($names) provedla společný zničující výpad za celkem ${comboDmgPerEnemy * aliveEnemies.size} poškození!",
            actor = "Celý Harém Dominia",
            actionName = "Nespoutaný hněv Harému",
            damageDealt = comboDmgPerEnemy * aliveEnemies.size,
            narrativeText = "Spojené pouto lásky, poslušnosti a temné magie proťalo bojiště v oslnivé vlně extáze a zkázy!"
        )

        val nextSession = session.copy(
            haremComboGauge = 0,
            combatLogs = listOf(comboLog) + session.combatLogs
        )

        if (nextSession.enemies.none { it.isAlive }) {
            return handleVictory(nextSession)
        }

        return advanceTurn(nextSession)
    }

    /**
     * Use a consumable item on an ally.
     */
    fun executeUseItem(
        session: PartyCombatSession,
        item: InventoryItem,
        targetAllyIndex: Int
    ): Pair<PartyCombatSession, String> {
        val target = session.aliveParty.getOrNull(targetAllyIndex) ?: session.aliveParty.firstOrNull()
            ?: return Pair(session, "Žádný cíl pro předmět!")

        var healVal = 50
        var msg = "🧪 Použit ${item.name} na ${target.name} (+50 HP)."

        if (item.id.contains("balzam")) {
            healVal = 60
            target.hp = (target.hp + healVal).coerceAtMost(target.maxHp)
            msg = "🧪 Hojivý balzám uzdravil ${target.name} o +60 HP!"
        } else if (item.id.contains("elixir")) {
            target.mana = (target.mana + 40).coerceAtMost(target.maxMana)
            target.hp = (target.hp + 30).coerceAtMost(target.maxHp)
            msg = "🔮 Elixír touhy obnovil ${target.name} +40 MP a +30 HP!"
        } else {
            target.hp = (target.hp + healVal).coerceAtMost(target.maxHp)
        }

        SoundEffectManager.playCombat(CombatSound.HEAL_RESTORE)

        val log = CombatLogEntry(
            turn = session.currentRound,
            type = "player_heal",
            message = msg,
            actor = "Předmět",
            actionName = item.name
        )

        val nextSession = session.copy(
            combatLogs = listOf(log) + session.combatLogs
        )

        return advanceTurn(nextSession)
    }

    /**
     * Advance party turn or trigger enemy phase when all living party members have acted.
     */
    private fun advanceTurn(session: PartyCombatSession): Pair<PartyCombatSession, String> {
        var updatedSession = session
        
        // Handle Domain Expansion duration
        updatedSession.activeDomainExpansion?.let { domain ->
            if (domain.durationTurns > 1) {
                updatedSession = updatedSession.copy(
                    activeDomainExpansion = domain.copy(durationTurns = domain.durationTurns - 1)
                )
            } else {
                updatedSession = updatedSession.copy(activeDomainExpansion = null)
            }
        }

        val aliveParty = updatedSession.aliveParty
        if (aliveParty.isEmpty()) {
            return handleDefeat(updatedSession)
        }

        val nextIndex = updatedSession.currentTurnIndex + 1

        if (nextIndex < aliveParty.size) {
            // Next party member's turn
            val nextSession = updatedSession.copy(currentTurnIndex = nextIndex)
            return Pair(nextSession, "Tah: ${nextSession.currentActiveMember?.name}")
        } else {
            // Party phase complete -> Run Enemy Phase!
            return executeEnemyPhase(updatedSession)
        }
    }

    /**
     * Execute turns for all living enemies.
     */
    private fun executeEnemyPhase(session: PartyCombatSession): Pair<PartyCombatSession, String> {
        val aliveEnemies = session.enemies.filter { it.isAlive }
        var aliveParty = session.party.filter { it.isAlive }

        if (aliveEnemies.isEmpty()) return handleVictory(session)
        if (aliveParty.isEmpty()) return handleDefeat(session)

        val enemyLogs = mutableListOf<CombatLogEntry>()

        aliveEnemies.forEach { enemy ->
            if (enemy.isStunned) {
                enemyLogs.add(
                    CombatLogEntry(
                        turn = session.currentRound,
                        type = "system",
                        message = "💫 ${enemy.name} je omráčen a nemůže jednat!",
                        actor = enemy.name,
                        actionName = "Omráčení"
                    )
                )
                return@forEach
            }

            // Tactic-based AI
            val synergyLevel = session.activeSynergies.size // Using number of active synergies as a proxy
            val hpRatio = enemy.hpPercent

            // Adjust behavior based on tactic
            val isAggressive = when (enemy.tactic) {
                EnemyTactic.AGGRESSIVE -> hpRatio < 0.8f || synergyLevel > 1
                EnemyTactic.BALANCED -> hpRatio < 0.5f && synergyLevel > 2
                EnemyTactic.DEFENSIVE -> hpRatio < 0.2f
                EnemyTactic.CAUTIOUS -> false
            }
            
            // Tactic affects damage or special chance
            val specialChanceModifier = when (enemy.tactic) {
                EnemyTactic.AGGRESSIVE -> 1.5f
                EnemyTactic.BALANCED -> 1.0f
                EnemyTactic.DEFENSIVE -> 0.5f
                EnemyTactic.CAUTIOUS -> 0.2f
            }

            // Find target (Prefer taunting tanks, or lowest HP)
            val taunters = aliveParty.filter { p -> p.statusEffects.any { it.type == "TAUNT" } }
            val target = if (taunters.isNotEmpty() && enemy.tactic != EnemyTactic.AGGRESSIVE) {
                taunters.random()
            } else {
                // Aggressive enemies prefer lowest HP, others prefer highest defense/others
                if (isAggressive) aliveParty.minByOrNull { it.hp } ?: aliveParty.random()
                else aliveParty.random()
            }

            // Calculate enemy damage
            val isSpecial = (enemy.isBoss || isAggressive) && Random.nextInt(100) < (enemy.specialAttackChance * specialChanceModifier)
            val baseDmg = if (isSpecial) (enemy.attack * (if(isAggressive) 1.8f else 1.5f)).toInt() else enemy.attack + Random.nextInt(-2, 4)
            val defMitigation = target.defense * 0.4f
            val guardReduction = if (target.isDefending) 0.40f else 1.0f

            val elementMult = getElementMultiplier(enemy.element, target.element)
            val matchupType = getMatchupType(elementMult)
            val baseMitigatedDmg = (((baseDmg - defMitigation) * guardReduction) * elementMult).toInt().coerceAtLeast(6)

            // Elemental Synergy passive damage resistance
            val synergyResult = ElementalSynergyManager.calculateDamageMitigation(
                incomingDamage = baseMitigatedDmg,
                attackerElement = enemy.element,
                activeSynergies = session.elementalSynergies
            )
            val rawFinalDmg = synergyResult.finalDamageAfterSynergy.coerceAtLeast(4)

            val synergyFormulaTag = if (synergyResult.mitigatedDamageAmount > 0) {
                " - [Synergie: -${(synergyResult.totalMitigationRatio * 100).toInt()}% (-${synergyResult.mitigatedDamageAmount} DMG)]"
            } else ""
            val formulaStr = "[Útok: $baseDmg] × [Živel: ${"%.2f".format(elementMult)}x]${if (target.isDefending) " × [Obranný postoj: 0.4x]" else ""} - [Obrana: ${(defMitigation).toInt()}]$synergyFormulaTag = $rawFinalDmg DMG"
            val tacticalNote = buildString {
                when (matchupType) {
                    ElementalMatchupType.EXTREME_WEAKNESS, ElementalMatchupType.SUPER_EFFECTIVE -> {
                        append("Nepřítel (${enemy.element.name}) zasáhl slabinu obránce (${target.element.name})!")
                    }
                    ElementalMatchupType.EXTREME_RESISTANCE, ElementalMatchupType.RESISTED -> {
                        append("Obránce (${target.element.name}) odolal nepřátelskému živlu (${enemy.element.name})!")
                    }
                    ElementalMatchupType.NEUTRAL -> {
                        append("Neutrální elementární zásah nepřítele.")
                    }
                }
                if (synergyResult.mitigatedDamageAmount > 0) {
                    val activeNames = synergyResult.contributingSynergies.joinToString { it.name }
                    append(" 🛡️ Elementární synergie ($activeNames) pohltila -${synergyResult.mitigatedDamageAmount} DMG (-${(synergyResult.totalMitigationRatio * 100).toInt()}%)!")
                }
            }

            val enemyBreakdown = ElementalDamageBreakdown(
                turn = session.combatLogs.size + enemyLogs.size + 1,
                round = session.currentRound,
                attackerName = enemy.name,
                attackerElement = enemy.element,
                defenderName = target.name,
                defenderElement = target.element,
                isPlayerPartyAttacker = false,
                actionName = if (isSpecial) "Drtivý speciální útok" else "Útok nepřítele",
                basePower = baseDmg,
                affinityMultiplier = 1.0f,
                elementMatchupMultiplier = elementMult,
                matchupType = matchupType,
                isCritical = isSpecial,
                critMultiplier = if (isSpecial) 1.5f else 1.0f,
                synergyMitigatedDamage = synergyResult.mitigatedDamageAmount,
                activeSynergyName = synergyResult.contributingSynergies.firstOrNull()?.name,
                targetDefense = target.defense,
                defenseMitigation = (defMitigation).toInt(),
                rawCalculatedDamage = (baseDmg * elementMult).toInt(),
                finalDamage = rawFinalDmg,
                affinityBonusDamageGained = 0,
                matchupBonusDamageGained = (rawFinalDmg - (((baseDmg - defMitigation) * guardReduction) * 1.0f).toInt()),
                formulaDisplay = formulaStr,
                tacticalNote = tacticalNote
            )

            // Guardian Devotion: If Lord is targeted, an alive loyal guardian may intercept and mitigate damage
            val loyalGuardian = if (target.isPlayer) {
                aliveParty.filter { !it.isPlayer && it.isAlive && it.loyaltyProtectLordChancePercent > 0 }
                    .maxByOrNull { it.loyaltyProtectLordChancePercent }
            } else null

            val isProtected = loyalGuardian != null && Random.nextInt(100) < loyalGuardian.loyaltyProtectLordChancePercent

            val finalDmg: Int
            if (isProtected && loyalGuardian != null) {
                val mitigated = (rawFinalDmg * 0.65f).toInt().coerceAtLeast(4)
                val guardianShare = mitigated / 2
                val lordShare = mitigated - guardianShare
                target.hp = (target.hp - lordShare).coerceAtLeast(0)
                loyalGuardian.hp = (loyalGuardian.hp - guardianShare).coerceAtLeast(0)
                finalDmg = lordShare
                enemyLogs.add(
                    CombatLogEntry(
                        turn = session.currentRound,
                        type = "player_special",
                        message = "🛡️ [Oddaná ochrana] ${loyalGuardian.name} (Loajalita: ${loyalGuardian.loyaltyValue}%) skočila před svého Pána! Pohltila $guardianShare poškození a zmírnila zásah Pána na $lordShare!",
                        actor = loyalGuardian.name,
                        actionName = "Ochrana Pána"
                    )
                )
            } else {
                finalDmg = rawFinalDmg
                target.hp = (target.hp - finalDmg).coerceAtLeast(0)
            }

            SoundEffectManager.playCombat(if (isSpecial) CombatSound.BOSS_SPECIAL else CombatSound.ENEMY_STRIKE)

            val specialTag = if (isSpecial) " 💥 SPECIÁLNÍ ÚTOK!" else ""
            val synergyTag = if (synergyResult.mitigatedDamageAmount > 0) " 🛡️ [Synergie: -${synergyResult.mitigatedDamageAmount} DMG]" else ""
            enemyLogs.add(
                CombatLogEntry(
                    turn = session.currentRound,
                    type = if (isSpecial) "enemy_special" else "enemy_attack",
                    message = "👹 ${enemy.name} zaútočil na ${target.name} za $finalDmg poškození!$specialTag$synergyTag",
                    actor = enemy.name,
                    actionName = if (isSpecial) "Drtivý úder" else "Útok",
                    damageDealt = finalDmg,
                    damageCalculation = formulaStr,
                    elementalBreakdown = enemyBreakdown
                )
            )

            // Refresh alive party list
            aliveParty = session.party.filter { it.isAlive }
            if (aliveParty.isEmpty()) return@forEach
        }

        // Tick status effects & mana / HP regen from relationship tiers
        session.party.forEach { member ->
            member.isDefending = false
            // Mana regen per round (including loyalty bonus)
            val loyaltyBonus = com.example.haremdark.data.LoyaltyCombatData.getBonusForLoyalty(member.loyaltyValue)
            val manaRegen = 8 + session.activeSynergies.sumOf { it.manaRegenBonus } + loyaltyBonus.mpRegenPerTurn
            member.mana = (member.mana + manaRegen).coerceAtMost(member.maxMana)

            // Relationship Tier HP Regeneration
            if (member.isAlive && member.combatRegenBonus > 0 && member.hp < member.maxHp) {
                val healedHp = member.combatRegenBonus
                member.hp = (member.hp + healedHp).coerceAtMost(member.maxHp)
            }

            // Tick status effects
            member.statusEffects.removeAll { effect ->
                effect.durationTurns -= 1
                if (effect.type == "POISON" || effect.type == "BLEED" || effect.type == "BURN") {
                    member.hp = (member.hp - effect.value).coerceAtLeast(0)
                } else if (effect.type == "FREEZE") {
                    member.isDefending = false // Frozen combatants cannot defend
                } else if (effect.type == "SHOCK") {
                    member.mana = (member.mana - effect.value).coerceAtLeast(0)
                }
                effect.durationTurns <= 0
            }
        }

        session.enemies.forEach { enemy ->
            enemy.statusEffects.removeAll { effect ->
                effect.durationTurns -= 1
                if (effect.type == "POISON" || effect.type == "BLEED" || effect.type == "BURN") {
                    enemy.hp = (enemy.hp - effect.value).coerceAtLeast(0)
                    if (effect.type == "BLEED") {
                        SoundEffectManager.playCombat(CombatSound.STATUS_TRIGGER_BLEED)
                    } else if (effect.type == "POISON") {
                        SoundEffectManager.playCombat(CombatSound.STATUS_TRIGGER_POISON)
                    }
                    enemyLogs.add(
                        CombatLogEntry(
                            turn = session.currentRound,
                            type = "system",
                            message = "${effect.icon} ${enemy.name} utrpěl ${effect.value} poškození efektem ${effect.name}!",
                            actor = "Stavový efekt"
                        )
                    )
                }
                effect.durationTurns <= 0
            }
        }

        val hazardLogs = mutableListOf<CombatLogEntry>()
        var newHazardCountdown = session.hazardCountdown - 1
        var hazardTriggerMsg: String? = null

        val hazard = session.environmentalHazard
        if (hazard != null && newHazardCountdown <= 0) {
            newHazardCountdown = hazard.triggerIntervalTurns
            val livingParty = session.party.filter { it.isAlive }
            val livingEnemies = session.enemies.filter { it.isAlive }

            when (hazard.targetRule) {
                HazardTargetRule.RANDOM_ANY_COMBATANT -> {
                    val chooseParty = if (livingParty.isNotEmpty() && livingEnemies.isNotEmpty()) {
                        Random.nextBoolean()
                    } else livingParty.isNotEmpty()

                    if (chooseParty && livingParty.isNotEmpty()) {
                        val victim = livingParty.random()
                        val dmg = (hazard.baseDamage + Random.nextInt(-2, 4)).coerceAtLeast(6)
                        victim.hp = (victim.hp - dmg).coerceAtLeast(0)
                        hazard.statusEffectToApply?.let { effect ->
                            victim.statusEffects.removeAll { it.id == effect.id }
                            victim.statusEffects.add(effect.copy())
                        }
                        hazardTriggerMsg = "⚠️ ${hazard.icon} ${hazard.name} zasáhl ${victim.name} za $dmg DMG!"
                        hazardLogs.add(
                            CombatLogEntry(
                                turn = session.currentRound,
                                type = "system",
                                message = "${hazard.icon} [TERÉNNÍ HAZARD] ${hazard.title} zasáhl ${victim.name} za $dmg poškození! Aplikován stav '${hazard.statusEffectToApply?.name ?: "Zranění"}'.",
                                actor = hazard.name,
                                actionName = hazard.title,
                                damageDealt = dmg
                            )
                        )
                    } else if (livingEnemies.isNotEmpty()) {
                        val victim = livingEnemies.random()
                        val dmg = (hazard.baseDamage + Random.nextInt(-2, 4)).coerceAtLeast(6)
                        victim.hp = (victim.hp - dmg).coerceAtLeast(0)
                        hazard.statusEffectToApply?.let { effect ->
                            victim.statusEffects.removeAll { it.id == effect.id }
                            victim.statusEffects.add(effect.copy())
                        }
                        hazardTriggerMsg = "💥 ${hazard.icon} ${hazard.name} zasáhl ${victim.name} za $dmg DMG!"
                        hazardLogs.add(
                            CombatLogEntry(
                                turn = session.currentRound,
                                type = "system",
                                message = "${hazard.icon} [TERÉNNÍ HAZARD] ${hazard.title} udeřil do ${victim.name} za $dmg poškození! Utrpěn stav '${hazard.statusEffectToApply?.name ?: "Zranění"}'.",
                                actor = hazard.name,
                                actionName = hazard.title,
                                damageDealt = dmg
                            )
                        )
                    }
                }
                HazardTargetRule.RANDOM_ALLY_ONLY -> {
                    if (livingParty.isNotEmpty()) {
                        val ally = livingParty.random()
                        if (hazard.baseDamage <= 0) {
                            val healAmt = 24
                            ally.hp = (ally.hp + healAmt).coerceAtMost(ally.maxHp)
                            hazard.statusEffectToApply?.let { effect ->
                                ally.statusEffects.removeAll { it.id == effect.id }
                                ally.statusEffects.add(effect.copy())
                            }
                            hazardTriggerMsg = "✨ ${hazard.icon} ${hazard.name} požehnal ${ally.name} (+${healAmt} HP)!"
                            hazardLogs.add(
                                CombatLogEntry(
                                    turn = session.currentRound,
                                    type = "system",
                                    message = "${hazard.icon} [POSVÁTNÝ TERÉN] ${hazard.title} požehnal ${ally.name} (obnoveno +$healAmt HP a aktivována regenerace).",
                                    actor = hazard.name,
                                    actionName = hazard.title
                                )
                            )
                        } else {
                            val dmg = (hazard.baseDamage + Random.nextInt(-2, 4)).coerceAtLeast(6)
                            ally.hp = (ally.hp - dmg).coerceAtLeast(0)
                            hazard.statusEffectToApply?.let { effect ->
                                ally.statusEffects.removeAll { it.id == effect.id }
                                ally.statusEffects.add(effect.copy())
                            }
                            hazardTriggerMsg = "⚠️ ${hazard.icon} ${hazard.name} zasáhl ${ally.name} za $dmg DMG!"
                            hazardLogs.add(
                                CombatLogEntry(
                                    turn = session.currentRound,
                                    type = "system",
                                    message = "${hazard.icon} [TERÉNNÍ HAZARD] ${hazard.title} zasáhl ${ally.name} za $dmg poškození!",
                                    actor = hazard.name,
                                    actionName = hazard.title,
                                    damageDealt = dmg
                                )
                            )
                        }
                    }
                }
                HazardTargetRule.RANDOM_ENEMY_ONLY -> {
                    if (livingEnemies.isNotEmpty()) {
                        val enemy = livingEnemies.random()
                        val dmg = (hazard.baseDamage + Random.nextInt(-2, 4)).coerceAtLeast(6)
                        enemy.hp = (enemy.hp - dmg).coerceAtLeast(0)
                        hazard.statusEffectToApply?.let { effect ->
                            enemy.statusEffects.removeAll { it.id == effect.id }
                            enemy.statusEffects.add(effect.copy())
                        }
                        hazardTriggerMsg = "💥 ${hazard.icon} ${hazard.name} udeřil do ${enemy.name} za $dmg DMG!"
                        hazardLogs.add(
                            CombatLogEntry(
                                turn = session.currentRound,
                                type = "system",
                                message = "${hazard.icon} [TERÉNNÍ HAZARD] ${hazard.title} zasáhl ${enemy.name} za $dmg poškození!",
                                actor = hazard.name,
                                actionName = hazard.title,
                                damageDealt = dmg
                            )
                        )
                    }
                }
                else -> {}
            }

            // Sound cue for hazard
            when (hazard.hazardType) {
                HazardType.LAVA_ERUPTION -> SoundEffectManager.playCombat(CombatSound.ENEMY_STRIKE)
                HazardType.TOXIC_MIASMA -> SoundEffectManager.playCombat(CombatSound.STATUS_TRIGGER_POISON)
                HazardType.THUNDER_SURGE -> SoundEffectManager.playCombat(CombatSound.CRITICAL_SUPERNOVA)
                HazardType.FROSTBITE_TEMPEST -> SoundEffectManager.playCombat(CombatSound.DARK_SPELL)
                HazardType.HOLY_RADIANCE -> SoundEffectManager.playCombat(CombatSound.HEAL_RESTORE)
                HazardType.SHADOW_ABYSS -> SoundEffectManager.playCombat(CombatSound.STATUS_TRIGGER_BLEED)
            }
        }

        val allLogs = hazardLogs + enemyLogs + session.combatLogs

        // Check if party wiped
        if (session.party.none { it.isAlive }) {
            return handleDefeat(session.copy(combatLogs = allLogs))
        }

        // Check if enemies died from DoT or hazard
        if (session.enemies.none { it.isAlive }) {
            return handleVictory(session.copy(combatLogs = allLogs))
        }

        // Start new round
        val nextSession = session.copy(
            currentTurnIndex = 0,
            currentRound = session.currentRound + 1,
            isEnemyPhase = false,
            hazardCountdown = newHazardCountdown,
            lastHazardTriggerMessage = hazardTriggerMsg,
            combatLogs = allLogs
        )

        return Pair(nextSession, "Začíná Kolo ${nextSession.currentRound}!")
    }

    private fun handleVictory(session: PartyCombatSession): Pair<PartyCombatSession, String> {
        SoundEffectManager.playCombat(CombatSound.VICTORY)

        val totalGoldBase = session.enemies.sumOf { it.rewardGold }
        val totalXpBase = session.enemies.sumOf { it.rewardXp }
        val isBossFight = session.enemies.any { it.isBoss }
        val basePrestige = if (isBossFight) 20 else 8

        // Calculate performance metrics
        val roundsTaken = session.currentRound
        val totalPartyCount = session.party.size.coerceAtLeast(1)
        val aliveCount = session.aliveParty.size
        val combosUsedCount = session.comboChainCount.coerceAtLeast(if (session.haremComboGauge >= 25) 1 else 0)
        val statusCount = session.enemies.sumOf { it.statusEffects.size }

        // Combat Efficiency Score Calculation
        val efficiency = LootDistributionCatalog.calculateEfficiencyScore(
            roundsTaken = roundsTaken,
            alivePartyCount = aliveCount,
            totalPartyCount = totalPartyCount,
            combosExecuted = combosUsedCount,
            statusEffectsInflicted = statusCount,
            isBoss = isBossFight,
            hazardSurvived = true
        )

        val rank = efficiency.rank
        val rankTitle = efficiency.rankTitle
        val multiplier = efficiency.lootRollMultiplier
        val score = efficiency.totalScore
        val flawless = efficiency.flawlessVictory
        val comboUsed = combosUsedCount > 0

        // MVP Determination: Highest attack or living companion
        val mvpMember = session.aliveParty.maxByOrNull { it.attack + (if (!it.isPlayer) 10 else 0) } ?: session.party.firstOrNull()
        val mvpName = mvpMember?.name ?: "Pán"
        val mvpId = mvpMember?.id

        val companionIds = session.party.filter { !it.isPlayer }.map { it.id }

        // Generate randomized equipment fragments and crafting resources based on efficiency
        val lootDistribution = LootDistributionCatalog.rollPostBattleLoot(
            efficiency = efficiency,
            baseGold = totalGoldBase,
            baseXp = totalXpBase,
            isBoss = isBossFight,
            mvpName = mvpName,
            mvpId = mvpId,
            companionIds = companionIds
        )

        val finalGold = lootDistribution.goldRewarded
        val finalPrestige = (basePrestige * multiplier).toInt()
        val bloodRubies = lootDistribution.bloodRubiesRewarded

        // Generate dynamic rare loot items
        val droppedItemDetails = mutableListOf<InventoryItem>()
        val droppedItemIds = mutableListOf<String>()

        // 1. Guaranteed potion/gift
        val standardGifts = listOf(
            InventoryItem("gift_roses", "Kytice černých růží", "Zvyšuje náklonnost a potěší společnici.", 1, 120, "gift", "🌹", "Vzácný", "+15 Náklonnost", source = "Boj"),
            InventoryItem("hojivy_balzam", "Léčivý balzám dominance", "Okamžitě obnoví 50 HP v boji.", 1, 90, "consumable", "🧪", "Běžný", "Obnoví 50 HP", source = "Boj"),
            InventoryItem("hedvabny_korzet", "Hedvábný krajkový korzet", "Luxusní oděv zvyšující poslušnost a touhu.", 1, 200, "gift", "🎀", "Epický", "+25 Náklonnost, +10 Poslušnost", source = "Boj")
        )
        val selectedGift = standardGifts.random()
        droppedItemDetails.add(selectedGift)
        droppedItemIds.add(selectedGift.id)

        // 2. Chance for rare weapon/armor based on rank
        val roll = (0..100).random()
        if (rank == "S+" || rank == "S" || isBossFight || roll < 45) {
            val rareEquipmentPool = listOf(
                InventoryItem("krvava_cepel_arena", "Krvavá čepel arény", "Legendární meč zocelený v desítkách bitev.", 1, 350, "equipment", "🗡️", "Epický", "+16 Boj, +8% Krit", "weapon", 16, 0, 0, source = "Boj"),
                InventoryItem("roba_temnych_hvezd", "Róba temných hvězd", "Zahalená stínovou magií chránící nositelku.", 1, 320, "equipment", "🥋", "Epický", "+12 Obrana, +35 HP", "armor", 0, 12, 35, source = "Boj"),
                InventoryItem("prsten_krvaveho_rubinu", "Amulet krvavého rubínu", "Vysává životní sílu nepřátel při každém úderu.", 1, 400, "equipment", "🧿", "Legendární", "+10 Boj, +6 Obrana, +20 HP", "accessory", 10, 6, 20, source = "Boj"),
                InventoryItem("kniha_stinovych_kouzel", "Tome temného rituálu", "Starobylá kniha zvyšující dovednosti společnic.", 1, 300, "gift", "📜", "Vzácný", "+35 Náklonnost, +20 ZK všem", source = "Boj")
            )
            val drop = rareEquipmentPool.random()
            droppedItemDetails.add(drop)
            droppedItemIds.add(drop.id)
        }

        val rewards = PartyCombatRewards(
            gold = finalGold,
            bloodRubies = bloodRubies,
            playerXp = lootDistribution.playerXpRewarded,
            haremAffinityGain = (40 * multiplier).toInt(),
            haremLoyaltyGain = (8 * multiplier).toInt(),
            prestigeGain = finalPrestige,
            lootItems = droppedItemIds,
            itemDropDetails = droppedItemDetails,
            mvpName = mvpName,
            mvpCharacterId = mvpId,
            mvpBonusXp = lootDistribution.mvpBonusXp,
            rank = rank,
            rankTitle = rankTitle,
            score = score,
            roundsTaken = roundsTaken,
            flawlessVictory = flawless,
            comboExecuted = comboUsed,
            characterXpGains = lootDistribution.companionXpGains,
            bonusMultiplier = multiplier,
            lootDistribution = lootDistribution
        )

        val victoryLog = CombatLogEntry(
            turn = session.currentRound,
            type = "victory",
            message = "🏆 HODNOCENÍ EFEKTIVITY [$rank] $rankTitle (${efficiency.efficiencyPercent}%)! Získáno $finalGold zlata, ${lootDistribution.fragmentsRewarded.size} úlomků výbavy a ${lootDistribution.resourcesRewarded.size} druhů surovin!",
            actor = "Systém",
            actionName = "Triumf"
        )

        val finishedSession = session.copy(
            isFinished = true,
            isVictory = true,
            rewards = rewards,
            combatLogs = listOf(victoryLog) + session.combatLogs
        )

        return Pair(finishedSession, "Vítězství v boji! Hodnocení: $rank")
    }

    private fun handleDefeat(session: PartyCombatSession): Pair<PartyCombatSession, String> {
        SoundEffectManager.playCombat(CombatSound.DEFEAT)

        val defeatLog = CombatLogEntry(
            turn = session.currentRound,
            type = "defeat",
            message = "💀 PORÁŽKA! Všichni členové tvé družiny padli v boji. Musíte ustoupit do bezpečí.",
            actor = "Systém",
            actionName = "Porážka"
        )

        val finishedSession = session.copy(
            isFinished = true,
            isVictory = false,
            combatLogs = listOf(defeatLog) + session.combatLogs
        )

        return Pair(finishedSession, "Družina byla poražena.")
    }
}
