package com.example.haremdark.domain

import com.example.haremdark.data.PartyCombatCatalog
import com.example.haremdark.models.*
import kotlin.random.Random

object PartyCombatManager {

    /**
     * Create a new party combat session with selected harem girls and optional player.
     */
    fun createSession(
        selectedCharacterIds: List<String>,
        allCharacters: List<Character>,
        player: Player,
        includePlayerAsLeader: Boolean,
        encounterDef: PartyCombatCatalog.PartyEncounterDefinition
    ): PartyCombatSession {
        val partyList = mutableListOf<PartyMember>()

        if (includePlayerAsLeader) {
            val playerCombat = player.skills["boj"] ?: 1
            val playerDark = player.skills["temnota"] ?: 1
            val equippedWeapon = player.weapons.getOrNull(player.equippedWeaponIndex) ?: player.weapons.firstOrNull()
            val weaponDmg = equippedWeapon?.damage ?: 15

            val playerMember = PartyMember(
                id = "player",
                name = "Pán Dominia",
                isPlayer = true,
                archetypeId = "player",
                role = CombatRole.PHYSICAL_DPS,
                hp = player.hp,
                maxHp = player.maxHp,
                mana = (player.darkEnergy + player.sexEnergy).coerceAtMost(100),
                maxMana = 100,
                attack = 22 + playerCombat * 3 + weaponDmg / 2,
                defense = 14 + (player.skills["obrana"] ?: 0) * 2,
                speed = 14,
                critRatePercent = 15 + playerCombat * 2,
                skills = PartyCombatCatalog.getPlayerSkills(playerCombat, playerDark),
                loyaltyTierName = "Vládce",
                affinityBonusDmg = 1.25f,
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
                val affinityDmgMult = 1.0f + (affinityTier.level * 0.05f) + (if (char.oblibena) 0.10f else 0f)
                val passiveBonuses = com.example.haremdark.data.CharacterSkillCatalog.calculatePassiveBonuses(char)
                val unlockedActiveSkills = com.example.haremdark.data.CharacterSkillCatalog.getUnlockedActiveSkills(char)
                val baseSkills = PartyCombatCatalog.getSkillsForCharacter(
                    archetypeId = char.archetype,
                    corruptionPhase = char.fazeZkazenosti,
                    affinityLevel = affinityTier.level,
                    characterName = char.name
                )
                val allSkills = (baseSkills + unlockedActiveSkills).distinctBy { it.id }

                val member = PartyMember(
                    id = char.id,
                    name = char.name,
                    isPlayer = false,
                    archetypeId = char.archetype,
                    role = role,
                    hp = char.hp + eqBonusHp + passiveBonuses.hpBonus,
                    maxHp = char.maxHp + eqBonusHp + passiveBonuses.hpBonus,
                    mana = 50 + char.fazeZkazenosti * 5,
                    maxMana = 50 + char.fazeZkazenosti * 5,
                    attack = 18 + combatSkill * 2 + eqBonusAtk + char.fazeZkazenosti * 3 + passiveBonuses.attackBonus,
                    defense = 10 + (char.skills["defense"] ?: 3) + (if (role == CombatRole.TANK_GUARDIAN) 8 else 0) + passiveBonuses.defenseBonus,
                    speed = 12 + (if (role == CombatRole.ASSASSIN_BLADE) 6 else 0) + passiveBonuses.speedBonus,
                    critRatePercent = 10 + (if (role == CombatRole.PHYSICAL_DPS || role == CombatRole.ASSASSIN_BLADE) 15 else 0) + passiveBonuses.critBonus,
                    skills = allSkills,
                    loyaltyTierName = affinityTier.title,
                    affinityBonusDmg = affinityDmgMult,
                    favoriteWeaponIcon = if (role == CombatRole.TANK_GUARDIAN) "🛡️" else if (role == CombatRole.DARK_SORCERESS) "🔮" else "🗡️"
                )
                partyList.add(member)
            }
        }

        // Deep copy enemies
        val clonedEnemies = encounterDef.enemies.map { it.copy(statusEffects = mutableListOf()) }
        val synergies = PartyCombatCatalog.calculateSynergies(partyList)

        SoundEffectManager.playCombat(CombatSound.COMBAT_START)

        return PartyCombatSession(
            id = "combat_${System.currentTimeMillis()}",
            encounterTitle = encounterDef.title,
            encounterLocation = encounterDef.location,
            backgroundDrawableRes = encounterDef.backgroundRes,
            party = partyList,
            enemies = clonedEnemies,
            currentTurnIndex = 0,
            currentRound = 1,
            isEnemyPhase = false,
            selectedTargetEnemyIndex = 0,
            selectedTargetAllyIndex = 0,
            haremComboGauge = 20,
            activeSynergies = synergies,
            combatLogs = listOf(
                CombatLogEntry(
                    turn = 1,
                    type = "system",
                    message = "⚔️ Střet začíná! Tvá družina (${partyList.size} členů) čelí nepřátelům: ${encounterDef.title}.",
                    actor = "Aréna Dominia",
                    actionName = "Zahájení boje"
                )
            ),
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

        // Calculate damage
        val isCrit = Random.nextInt(100) < (activeMember.critRatePercent + (session.activeSynergies.sumOf { it.critBonusPercent }))
        val critMult = if (isCrit) 1.65f else 1.0f
        val synergyAtkBonus = 1.0f + session.activeSynergies.map { it.attackBonusPercent }.sum()
        val buffAtk = activeMember.statusEffects.filter { it.type == "ATK_BUFF" }.sumOf { it.value }

        val rawDmg = (activeMember.attack + buffAtk + Random.nextInt(-2, 4)) * activeMember.affinityBonusDmg * synergyAtkBonus
        val finalDmg = ((rawDmg - (target.defense * 0.4f)) * critMult).toInt().coerceAtLeast(8)

        // Apply damage to enemy
        target.hp = (target.hp - finalDmg).coerceAtLeast(0)

        // Charge Harem Combo
        val newCombo = (session.haremComboGauge + (if (isCrit) 18 else 10)).coerceAtMost(session.maxHaremComboGauge)

        // Sound effect
        if (isCrit) {
            SoundEffectManager.playCombat(CombatSound.CRITICAL_HIT)
        } else {
            SoundEffectManager.playCombat(CombatSound.PLAYER_SLASH)
        }

        val critTag = if (isCrit) " 💥 KRITICKÝ ZÁSAH!" else ""
        val logMsg = "🗡️ ${activeMember.name} zaútočila na ${target.name} a udělila $finalDmg poškození!$critTag"

        val newLog = CombatLogEntry(
            turn = session.currentRound,
            type = if (isCrit) "player_special" else "player_attack",
            message = logMsg,
            actor = activeMember.name,
            actionName = "Základní útok",
            damageDealt = finalDmg,
            damageCalculation = "[Síla: ${activeMember.attack}] * [Krit: x${"%.2f".format(critMult)}] * [Synergie: x${"%.2f".format(synergyAtkBonus)}] - [Obrana: ${(target.defense * 0.4f).toInt()}] = $finalDmg DMG"
        )

        val updatedLogs = listOf(newLog) + session.combatLogs

        var nextSession = session.copy(
            haremComboGauge = newCombo,
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

        val synergyAtk = 1.0f + session.activeSynergies.map { it.attackBonusPercent }.sum()

        when (skill.targetType) {
            SkillTargetType.SINGLE_ENEMY -> {
                val aliveEnemies = session.enemies.filter { it.isAlive }
                val target = aliveEnemies.getOrNull(targetIndex) ?: aliveEnemies.firstOrNull()
                if (target != null) {
                    val rawDmg = ((activeMember.attack * skill.powerMultiplier) + skill.baseDamageBonus) * activeMember.affinityBonusDmg * synergyAtk
                    val finalDmg = (rawDmg - (target.defense * 0.3f)).toInt().coerceAtLeast(10)
                    target.hp = (target.hp - finalDmg).coerceAtLeast(0)

                    if (skill.appliedStatus != null) {
                        target.statusEffects.add(skill.appliedStatus.copy())
                    }

                    if (skill.healAmount > 0) {
                        activeMember.hp = (activeMember.hp + skill.healAmount).coerceAtMost(activeMember.maxHp)
                    }

                    SoundEffectManager.playCombat(if (skill.category == SkillCategory.DARK_MAGIC) CombatSound.DARK_SPELL else CombatSound.PLAYER_SLASH)

                    newLogs.add(
                        CombatLogEntry(
                            turn = session.currentRound,
                            type = if (skill.category == SkillCategory.DARK_MAGIC) "player_spell" else "player_special",
                            message = "${skill.icon} ${activeMember.name} použila '${skill.name}' na ${target.name} za $finalDmg poškození!",
                            actor = activeMember.name,
                            actionName = skill.name,
                            damageDealt = finalDmg,
                            narrativeText = skill.voiceQuote ?: "${activeMember.name} soustředila svou sílu do zničujícího úderu."
                        )
                    )
                }
            }
            SkillTargetType.ALL_ENEMIES -> {
                val aliveEnemies = session.enemies.filter { it.isAlive }
                var totalDmg = 0
                aliveEnemies.forEach { enemy ->
                    val rawDmg = ((activeMember.attack * skill.powerMultiplier) + skill.baseDamageBonus) * activeMember.affinityBonusDmg * synergyAtk
                    val finalDmg = (rawDmg - (enemy.defense * 0.25f)).toInt().coerceAtLeast(8)
                    enemy.hp = (enemy.hp - finalDmg).coerceAtLeast(0)
                    totalDmg += finalDmg

                    if (skill.appliedStatus != null) {
                        enemy.statusEffects.add(skill.appliedStatus.copy())
                    }
                }

                if (skill.healAmount > 0) {
                    session.party.filter { it.isAlive }.forEach { ally ->
                        ally.hp = (ally.hp + skill.healAmount).coerceAtMost(ally.maxHp)
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
                    val prevHp = targetAlly.hp
                    targetAlly.hp = (targetAlly.hp + skill.healAmount).coerceAtMost(targetAlly.maxHp)
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
                session.aliveParty.forEach { ally ->
                    if (skill.healAmount > 0) {
                        ally.hp = (ally.hp + skill.healAmount).coerceAtMost(ally.maxHp)
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

        SoundEffectManager.playCombat(CombatSound.SHIELD_BLOCK)

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
        val aliveParty = session.aliveParty
        if (aliveParty.isEmpty()) {
            return handleDefeat(session)
        }

        val nextIndex = session.currentTurnIndex + 1

        if (nextIndex < aliveParty.size) {
            // Next party member's turn
            val updatedSession = session.copy(currentTurnIndex = nextIndex)
            return Pair(updatedSession, "Tah: ${updatedSession.currentActiveMember?.name}")
        } else {
            // Party phase complete -> Run Enemy Phase!
            return executeEnemyPhase(session)
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

            // Find target (Prefer taunting tanks, or lowest HP)
            val taunters = aliveParty.filter { p -> p.statusEffects.any { it.type == "TAUNT" } }
            val target = if (taunters.isNotEmpty()) {
                taunters.random()
            } else {
                aliveParty.minByOrNull { it.hp } ?: aliveParty.random()
            }

            // Calculate enemy damage
            val isSpecial = enemy.isBoss && Random.nextInt(100) < enemy.specialAttackChance
            val baseDmg = if (isSpecial) (enemy.attack * 1.5f).toInt() else enemy.attack + Random.nextInt(-2, 4)
            val defMitigation = target.defense * 0.4f
            val guardReduction = if (target.isDefending) 0.40f else 1.0f

            val finalDmg = ((baseDmg - defMitigation) * guardReduction).toInt().coerceAtLeast(6)
            target.hp = (target.hp - finalDmg).coerceAtLeast(0)

            SoundEffectManager.playCombat(if (isSpecial) CombatSound.BOSS_SPECIAL else CombatSound.ENEMY_STRIKE)

            val specialTag = if (isSpecial) " 💥 SPECIÁLNÍ ÚTOK!" else ""
            enemyLogs.add(
                CombatLogEntry(
                    turn = session.currentRound,
                    type = if (isSpecial) "enemy_special" else "enemy_attack",
                    message = "👹 ${enemy.name} zaútočil na ${target.name} za $finalDmg poškození!$specialTag",
                    actor = enemy.name,
                    actionName = if (isSpecial) "Drtivý úder" else "Útok",
                    damageDealt = finalDmg
                )
            )

            // Refresh alive party list
            aliveParty = session.party.filter { it.isAlive }
            if (aliveParty.isEmpty()) return@forEach
        }

        // Tick status effects & mana regen
        session.party.forEach { member ->
            member.isDefending = false
            // Mana regen per round
            val manaRegen = 8 + session.activeSynergies.sumOf { it.manaRegenBonus }
            member.mana = (member.mana + manaRegen).coerceAtMost(member.maxMana)

            // Tick status effects
            member.statusEffects.removeAll { effect ->
                effect.durationTurns -= 1
                if (effect.type == "POISON" || effect.type == "BLEED") {
                    member.hp = (member.hp - effect.value).coerceAtLeast(0)
                }
                effect.durationTurns <= 0
            }
        }

        session.enemies.forEach { enemy ->
            enemy.statusEffects.removeAll { effect ->
                effect.durationTurns -= 1
                if (effect.type == "POISON" || effect.type == "BLEED") {
                    enemy.hp = (enemy.hp - effect.value).coerceAtLeast(0)
                    enemyLogs.add(
                        CombatLogEntry(
                            turn = session.currentRound,
                            type = "system",
                            message = "🩸 ${enemy.name} utrpěl ${effect.value} poškození krvácením / jedem!",
                            actor = "Stavový efekt"
                        )
                    )
                }
                effect.durationTurns <= 0
            }
        }

        val allLogs = enemyLogs + session.combatLogs

        // Check if party wiped
        if (session.party.none { it.isAlive }) {
            return handleDefeat(session.copy(combatLogs = allLogs))
        }

        // Check if enemies died from DoT
        if (session.enemies.none { it.isAlive }) {
            return handleVictory(session.copy(combatLogs = allLogs))
        }

        // Start new round
        val nextSession = session.copy(
            currentTurnIndex = 0,
            currentRound = session.currentRound + 1,
            isEnemyPhase = false,
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
        val flawless = (aliveCount == totalPartyCount)
        val comboUsed = (session.haremComboGauge >= 25)

        // Performance Score
        var score = 500
        score += ((10 - roundsTaken) * 70).coerceAtLeast(0)
        score += ((aliveCount.toFloat() / totalPartyCount) * 250).toInt()
        if (flawless) score += 200
        if (isBossFight) score += 150

        // Rank determination
        val (rank, rankTitle, multiplier) = when {
            score >= 1050 -> Triple("S+", "Absolutní dominance", 1.8f)
            score >= 900 -> Triple("S", "Dominantní triumf", 1.5f)
            score >= 700 -> Triple("A", "Slavné vítězství", 1.25f)
            score >= 500 -> Triple("B", "Taktické vítězství", 1.1f)
            else -> Triple("C", "Těsný únik", 1.0f)
        }

        val finalGold = (totalGoldBase * multiplier).toInt()
        val finalPrestige = (basePrestige * multiplier).toInt()
        val bloodRubies = if (isBossFight) (15 * multiplier).toInt() else (5 * multiplier).toInt()

        // MVP Determination: Highest attack or living companion
        val mvpMember = session.aliveParty.maxByOrNull { it.attack + (if (!it.isPlayer) 10 else 0) } ?: session.party.firstOrNull()
        val mvpName = mvpMember?.name ?: "Pán"
        val mvpId = mvpMember?.id

        // Distribute Combat XP per character
        val charXpMap = mutableMapOf<String, Int>()
        val baseCharXp = (totalXpBase * multiplier).toInt().coerceAtLeast(25)
        session.party.filter { !it.isPlayer }.forEach { member ->
            val isMvp = (member.id == mvpId)
            val xpAward = if (isMvp) (baseCharXp * 1.5f).toInt() else baseCharXp
            charXpMap[member.id] = xpAward
        }

        // Generate dynamic rare loot items based on rank and boss
        val droppedItemDetails = mutableListOf<InventoryItem>()
        val droppedItemIds = mutableListOf<String>()

        // 1. Guaranteed potion/gift
        val standardGifts = listOf(
            InventoryItem("gift_roses", "Kytice černých růží", "Zvyšuje náklonnost a potěší společnici.", 1, 120, "gift", "🌹", "Vzácný", "+15 Náklonnost"),
            InventoryItem("hojivy_balzam", "Léčivý balzám dominance", "Okamžitě obnoví 50 HP v boji.", 1, 90, "consumable", "🧪", "Běžný", "Obnoví 50 HP"),
            InventoryItem("hedvabny_korzet", "Hedvábný krajkový korzet", "Luxusní oděv zvyšující poslušnost a touhu.", 1, 200, "gift", "🎀", "Epický", "+25 Náklonnost, +10 Poslušnost")
        )
        val selectedGift = standardGifts.random()
        droppedItemDetails.add(selectedGift)
        droppedItemIds.add(selectedGift.id)

        // 2. Chance for rare weapon/armor based on rank
        val roll = (0..100).random()
        if (rank == "S+" || rank == "S" || isBossFight || roll < 45) {
            val rareEquipmentPool = listOf(
                InventoryItem("krvava_cepel_arena", "Krvavá čepel arény", "Legendární meč zocelený v desítkách bitev.", 1, 350, "equipment", "🗡️", "Epický", "+16 Boj, +8% Krit", "weapon", 16, 0, 0),
                InventoryItem("roba_temnych_hvezd", "Róba temných hvězd", "Zahalená stínovou magií chránící nositelku.", 1, 320, "equipment", "🥋", "Epický", "+12 Obrana, +35 HP", "armor", 0, 12, 35),
                InventoryItem("prsten_krvaveho_rubinu", "Amulet krvavého rubínu", "Vysává životní sílu nepřátel při každém úderu.", 1, 400, "equipment", "🧿", "Legendární", "+10 Boj, +6 Obrana, +20 HP", "accessory", 10, 6, 20),
                InventoryItem("kniha_stinovych_kouzel", "Tome temného rituálu", "Starobylá kniha zvyšující dovednosti společnic.", 1, 300, "gift", "📜", "Vzácný", "+35 Náklonnost, +20 ZK všem")
            )
            val drop = rareEquipmentPool.random()
            droppedItemDetails.add(drop)
            droppedItemIds.add(drop.id)
        }

        val rewards = PartyCombatRewards(
            gold = finalGold,
            bloodRubies = bloodRubies,
            playerXp = (totalXpBase * multiplier).toInt(),
            haremAffinityGain = (40 * multiplier).toInt(),
            haremLoyaltyGain = (8 * multiplier).toInt(),
            prestigeGain = finalPrestige,
            lootItems = droppedItemIds,
            itemDropDetails = droppedItemDetails,
            mvpName = mvpName,
            mvpCharacterId = mvpId,
            mvpBonusXp = if (mvpId != null) (baseCharXp * 0.5f).toInt() else 0,
            rank = rank,
            rankTitle = rankTitle,
            score = score,
            roundsTaken = roundsTaken,
            flawlessVictory = flawless,
            comboExecuted = comboUsed,
            characterXpGains = charXpMap,
            bonusMultiplier = multiplier
        )

        val victoryLog = CombatLogEntry(
            turn = session.currentRound,
            type = "victory",
            message = "🏆 HODNOCENÍ [$rank] $rankTitle! Družina získala $finalGold zlata, $bloodRubies rubínů a MVP $mvpName získává bonusové ZK!",
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
