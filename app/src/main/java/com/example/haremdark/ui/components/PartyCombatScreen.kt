package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.PartyCombatManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.CombatLogEntry
import com.example.haremdark.models.CombatStatusEffect
import com.example.haremdark.models.GameSave
import com.example.haremdark.models.PartyCombatSession
import com.example.haremdark.models.SkillCategory
import com.example.haremdark.models.SkillTargetType
import com.example.haremdark.models.FormationPosition
import com.example.haremdark.models.CombatRole
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PartyCombatScreen(
    session: PartyCombatSession,
    gameState: GameSave,
    engine: GameEngine,
    onSessionUpdated: (PartyCombatSession) -> Unit,
    onExitCombat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val fxState = remember { CombatVisualFxState() }
    val isMuted by SoundEffectManager.isMuted.collectAsState()

    var showLogsModal by remember { mutableStateOf(false) }
    var showElementalLogsModal by remember { mutableStateOf(false) }
    var showElementalSynergyModal by remember { mutableStateOf(false) }
    var selectedStatusEffectForDetail by remember { mutableStateOf<CombatStatusEffect?>(null) }
    var isAutoBattle by remember { mutableStateOf(false) }

    val aliveEnemies = session.enemies.filter { it.isAlive }
    val aliveParty = session.party.filter { it.isAlive }
    val activeMember = session.currentActiveMember

    val targetEnemy = aliveEnemies.getOrNull(session.selectedTargetEnemyIndex) ?: aliveEnemies.firstOrNull()

    // Screen Shake & Camera Scale values
    val shakeX = fxState.shakeOffsetX.value
    val shakeY = fxState.shakeOffsetY.value
    val shakeRot = fxState.shakeRotation.value
    val camScale = fxState.cameraScale.value

    // Smart Tactical Auto-Battle AI loop
    LaunchedEffect(isAutoBattle, session.currentTurnIndex, session.isFinished, session.haremComboGauge) {
        if (isAutoBattle && !session.isFinished) {
            kotlinx.coroutines.delay(800)
            if (session.isFinished) return@LaunchedEffect

            // 1. Ultimate Combo priority
            if (session.isComboReady) {
                val (next, _) = PartyCombatManager.executeHaremComboUltimate(session)
                onSessionUpdated(next)
                return@LaunchedEffect
            }

            if (session.currentActiveMember != null) {
                val member = session.currentActiveMember!!

                // A. Assess teammate health (prioritize healing/shields)
                val lowestHpAlly = session.party.filter { it.isAlive }.minByOrNull { it.hp.toFloat() / it.maxHp.toFloat() }
                val lowestHpPercent = lowestHpAlly?.let { it.hp.toFloat() / it.maxHp.toFloat() } ?: 1.0f

                val healSkill = member.skills.firstOrNull { it.healAmount > 0 && member.mana >= it.manaCost }
                if (lowestHpPercent < 0.60f && healSkill != null && lowestHpAlly != null) {
                    val allyIdx = session.party.indexOf(lowestHpAlly).coerceIn(0, session.party.size - 1)
                    val targetId = if (healSkill.targetType == SkillTargetType.SINGLE_ALLY) allyIdx else session.selectedTargetAllyIndex
                    val (next, _) = PartyCombatManager.executeSkill(session, healSkill.id, targetId)
                    onSessionUpdated(next)
                    return@LaunchedEffect
                }

                // B. Active Vanguard/Tank self-defense strategy
                if (member.formationPosition == FormationPosition.FRONT_LINE && member.hp.toFloat() / member.maxHp.toFloat() < 0.35f) {
                    val shieldSkill = member.skills.firstOrNull { it.category == SkillCategory.SUPPORT_BUFF && member.mana >= it.manaCost }
                    if (shieldSkill != null) {
                        val (next, _) = PartyCombatManager.executeSkill(session, shieldSkill.id, session.selectedTargetAllyIndex)
                        onSessionUpdated(next)
                        return@LaunchedEffect
                    } else {
                        val (next, _) = PartyCombatManager.executeDefend(session)
                        onSessionUpdated(next)
                        return@LaunchedEffect
                    }
                }

                // C. Active Priestess/Buff support strategy
                if (member.role == CombatRole.HEALER_PRIESTESS || member.formationPosition == FormationPosition.BACK_LINE) {
                    val buffSkill = member.skills.firstOrNull { it.category == SkillCategory.SUPPORT_BUFF && member.mana >= it.manaCost }
                    if (buffSkill != null) {
                        val (next, _) = PartyCombatManager.executeSkill(session, buffSkill.id, session.selectedTargetAllyIndex)
                        onSessionUpdated(next)
                        return@LaunchedEffect
                    }
                }

                // D. Select optimal offensive skills
                val damageSkill = member.skills
                    .filter { it.manaCost <= member.mana && it.powerMultiplier > 1.1f && it.healAmount == 0 }
                    .maxByOrNull { it.powerMultiplier }

                // E. Element system targeting (focus on weaknesses or lowest HP)
                val aliveEnemies = session.enemies.filter { it.isAlive }
                if (aliveEnemies.isNotEmpty()) {
                    val weakEnemies = aliveEnemies.filter { enemy ->
                        PartyCombatManager.getElementMultiplier(member.element, enemy.element) > 1.1f
                    }
                    val targetEnemy = if (weakEnemies.isNotEmpty()) {
                        weakEnemies.minByOrNull { it.hp }!!
                    } else {
                        aliveEnemies.minByOrNull { it.hp }!!
                    }
                    val targetEnemyIdx = session.enemies.indexOf(targetEnemy).coerceIn(0, session.enemies.size - 1)

                    if (damageSkill != null) {
                        val targetId = if (damageSkill.targetType == SkillTargetType.ALL_ENEMIES) 0 else targetEnemyIdx
                        val updatedSession = session.copy(selectedTargetEnemyIndex = targetEnemyIdx)
                        val (next, _) = PartyCombatManager.executeSkill(updatedSession, damageSkill.id, targetId)
                        onSessionUpdated(next)
                    } else {
                        val updatedSession = session.copy(selectedTargetEnemyIndex = targetEnemyIdx)
                        val (next, _) = PartyCombatManager.executeBasicAttack(updatedSession, targetEnemyIdx)
                        onSessionUpdated(next)
                    }
                }
            }
        }
    }

    // Auto feedback for incoming enemy actions
    LaunchedEffect(session.combatLogs.firstOrNull()?.turn, session.combatLogs.firstOrNull()?.message) {
        val latest = session.combatLogs.firstOrNull() ?: return@LaunchedEffect
        if (latest.type == "enemy_special") {
            fxState.triggerAbility(
                type = CombatAbilityType.HEAVY_STRIKE,
                customName = "💥 ${latest.actor}: ${latest.actionName}",
                scope = coroutineScope,
                isCritical = true
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0814))
    ) {
        // --- 1. ATMOSPHERIC BATTLEFIELD BACKGROUND ---
        Image(
            painter = painterResource(id = session.backgroundDrawableRes),
            contentDescription = session.encounterTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark Vignette Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC0D0612),
                            Color(0x99120818),
                            Color(0xF00D0612)
                        )
                    )
                )
        )

        // Visual FX & Particles Layer
        CombatVisualFxOverlay(
            fxState = fxState,
            modifier = Modifier.fillMaxSize()
        )

        session.environmentalHazard?.let { haz ->
            EnvironmentalHazardParticles(
                hazardType = haz.hazardType,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Global Weather Subtle Particle Overlay
        CombatWeatherParticles(
            weatherId = session.weather.id,
            modifier = Modifier.fillMaxSize()
        )

        // Main Combat Layout with Screen Shake and Camera Impact Scale
        Column(
            modifier = Modifier
                .fillMaxSize()
                .scale(camScale)
                .graphicsLayer {
                    rotationZ = shakeRot
                }
                .offset { IntOffset(shakeX.roundToInt(), shakeY.roundToInt()) }
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- TOP BAR: TITLE, ROUND, SYNERGIES, AUDIO, LOGS, FLEE ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xCC1A0D22)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFC2185B)
                        ) {
                            Text(
                                "KOLO ${session.currentRound}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Column {
                            Text(
                                text = session.encounterTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFFFD700),
                                maxLines = 1
                            )
                            if (session.activeSynergies.isNotEmpty()) {
                                Text(
                                    text = session.activeSynergies.joinToString(" • ") { "${it.icon} ${it.name}" },
                                    fontSize = 9.sp,
                                    color = Color(0xFFFF80AB),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { isAutoBattle = !isAutoBattle },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = if (isAutoBattle) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = "Auto-Battle",
                                tint = if (isAutoBattle) Color(0xFF69F0AE) else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Animation Speed Toggle
                        var speedIdx by remember { mutableStateOf(0) }
                        val speeds = listOf(1.0f, 2.0f, 4.0f)
                        val speedLabels = listOf("1x", "2x", "4x")
                        IconButton(
                            onClick = {
                                speedIdx = (speedIdx + 1) % speeds.size
                                onSessionUpdated(session.copy(animationSpeedMultiplier = speeds[speedIdx]))
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Text(
                                text = speedLabels[speedIdx],
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC2185B)
                            )
                        }

                        IconButton(
                            onClick = { SoundEffectManager.toggleMute() },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Zvuk",
                                tint = if (isMuted) Color.Red else Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { showElementalSynergyModal = true },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Elementární Synergie",
                                tint = if (session.elementalSynergies.isNotEmpty()) Color(0xFF69F0AE) else Color(0xFFB388FF),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { showLogsModal = true },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HistoryEdu,
                                contentDescription = "Deník",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onExitCombat,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Ústup",
                                tint = Color(0xFFFF8A80),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // --- ELEMENTAL SYNERGY PASSIVE BUFFS BANNER ---
            if (session.elementalSynergies.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xD8200D35),
                    border = BorderStroke(1.dp, Color(0xFFAB47BC).copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showElementalSynergyModal = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🛡️", fontSize = 12.sp)
                            Text(
                                text = "Elementární Synergie: " + session.elementalSynergies.joinToString(" • ") { "${it.icon} ${it.name} (-${(it.damageResistancePercent * 100).toInt()}%)" },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE1BEE7),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "INFO ➔",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F)
                        )
                    }
                }
            }

            // --- DYNAMIC WEATHER & STATUS EFFECT BANNER ---
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x991C0E28),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(session.weather.icon, fontSize = 16.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Klima: ${session.weather.name}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                            if (session.weather.statusEffectName != null) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFC2185B).copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = session.weather.statusEffectName!!,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = session.weather.description,
                            fontSize = 9.sp,
                            color = Color(0xFFE1BEE7),
                            maxLines = 1
                        )
                    }
                }
            }

            // --- ENVIRONMENTAL HAZARDS & TERRAIN MODIFIERS BANNER ---
            EnvironmentalHazardBanner(
                hazard = session.environmentalHazard,
                countdown = session.hazardCountdown,
                lastTriggerMessage = session.lastHazardTriggerMessage
            )

            // --- ENEMY BATTLE LINE ---
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "👹 Nepřátelská linie (klepnutím vyber cíl):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFF8A80)
                )

                EnemyBattleLine(
                    enemies = session.enemies,
                    selectedTargetIndex = session.selectedTargetEnemyIndex,
                    attackerEnemyIndex = fxState.activeAttackerEnemyIndex,
                    hitTargetEnemyIndex = fxState.hitTargetEnemyIndex,
                    onSelectStatusEffect = { eff -> selectedStatusEffectForDetail = eff },
                    onSelectTarget = { idx ->
                        onSessionUpdated(session.copy(selectedTargetEnemyIndex = idx))
                    }
                )
            }

            // --- SCROLLABLE COMBAT LOG VIEW ---
            ScrollableCombatLogView(
                logs = session.combatLogs,
                onOpenFullModal = { showLogsModal = true },
                onOpenElementalLogModal = { showElementalLogsModal = true }
            )

            // --- HAREM ULTIMATE COMBO GAUGE ---
            HaremUltimateComboGauge(
                gauge = session.haremComboGauge,
                maxGauge = session.maxHaremComboGauge,
                onTriggerCombo = {
                    coroutineScope.launch {
                        val baseDmg = ((activeMember?.attack ?: 30) * 3.2f).toInt()
                        fxState.triggerAttackSequence(
                            attackerPartyIndex = session.currentTurnIndex,
                            targetEnemyIndex = session.selectedTargetEnemyIndex,
                            abilityType = CombatAbilityType.HAREM_ULTIMATE,
                            customName = "Harémové Kombo Dominia",
                            damageText = "-$baseDmg HP",
                            isCrit = true,
                            scope = coroutineScope
                        )
                        val (next, _) = PartyCombatManager.executeHaremComboUltimate(session)
                        onSessionUpdated(next)
                    }
                }
            )

            // --- PARTY FORMATION ROW ---
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "💖 Formace Harému Dominia:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFF80AB)
                )

                PartyFormationRow(
                    party = session.party,
                    activeTurnIndex = session.currentTurnIndex,
                    selectedTargetAllyIndex = session.selectedTargetAllyIndex,
                    attackerPartyIndex = fxState.activeAttackerPartyIndex,
                    hitTargetPartyIndex = fxState.hitTargetPartyIndex,
                    onSelectStatusEffect = { eff -> selectedStatusEffectForDetail = eff },
                    onSelectAlly = { idx ->
                        onSessionUpdated(session.copy(selectedTargetAllyIndex = idx))
                    }
                )
            }

            // --- COMMAND ACTION CONSOLE ---
            PartyCommandConsole(
                activeMember = activeMember,
                targetEnemyName = targetEnemy?.name ?: "Nepřítel",
                playerItems = gameState.player.items,
                onBasicAttack = {
                    coroutineScope.launch {
                        val (next, msg) = PartyCombatManager.executeBasicAttack(session, session.selectedTargetEnemyIndex)
                        val isCrit = msg.contains("KRITICKÝ ZÁSAH") || msg.contains("KRIT")
                        val damageDealt = next.combatLogs.firstOrNull()?.damageDealt ?: (activeMember?.attack ?: 20)

                        fxState.triggerAttackSequence(
                            attackerPartyIndex = session.currentTurnIndex,
                            targetEnemyIndex = session.selectedTargetEnemyIndex,
                            abilityType = if (isCrit) CombatAbilityType.CRITICAL_SUPERNOVA else CombatAbilityType.SLASH,
                            customName = if (isCrit) "Kritický výpad (${activeMember?.name ?: "Bojovnice"})" else "Bojový Výpad",
                            damageText = "-$damageDealt HP",
                            isCrit = isCrit,
                            scope = coroutineScope
                        )
                        onSessionUpdated(next)
                    }
                },
                onSkillSelect = { skill ->
                    coroutineScope.launch {
                        val isSpecial = skill.category == SkillCategory.ULTIMATE_COMBO || skill.powerMultiplier >= 1.5f
                        val isCrit = skill.powerMultiplier >= 1.4f || isSpecial
                        val animType = when {
                            skill.animationType == "DARK_BURST" -> CombatAbilityType.DARK_BURST
                            skill.animationType == "SHADOW_CURSE" -> CombatAbilityType.SHADOW_CURSE
                            skill.animationType == "BLEED_STRIKE" -> CombatAbilityType.BLEED_STRIKE
                            skill.animationType == "HEAVY_STRIKE" -> CombatAbilityType.HEAVY_STRIKE
                            skill.animationType == "HAREM_SUPPORT" -> CombatAbilityType.HAREM_SUPPORT
                            skill.animationType == "DEFEND" -> CombatAbilityType.DEFEND
                            skill.category == SkillCategory.ULTIMATE_COMBO -> CombatAbilityType.CHAR_SPECIAL
                            else -> CombatAbilityType.SLASH
                        }
                        val isAllyTarget = (skill.targetType == SkillTargetType.SINGLE_ALLY || skill.targetType == SkillTargetType.ALL_ALLIES)
                        val estimatedDmg = ((activeMember?.attack ?: 20) * skill.powerMultiplier).toInt()

                        if (isAllyTarget) {
                            fxState.triggerAbility(animType, skill.name, coroutineScope, isCritical = isCrit)
                            fxState.triggerFloatingText(
                                text = "+${estimatedDmg.coerceAtLeast(30)} HP",
                                isHeal = true,
                                isEnemyTarget = false,
                                scope = coroutineScope
                            )
                        } else {
                            fxState.triggerAttackSequence(
                                attackerPartyIndex = session.currentTurnIndex,
                                targetEnemyIndex = session.selectedTargetEnemyIndex,
                                abilityType = animType,
                                customName = "${skill.icon} ${skill.name}",
                                damageText = "-$estimatedDmg HP",
                                isCrit = isCrit,
                                scope = coroutineScope
                            )
                        }

                        val targetIdx = if (isAllyTarget) session.selectedTargetAllyIndex else session.selectedTargetEnemyIndex
                        val (next, _) = PartyCombatManager.executeSkill(session, skill.id, targetIdx)
                        onSessionUpdated(next)
                    }
                },
                onDefend = {
                    coroutineScope.launch {
                        fxState.triggerAbility(CombatAbilityType.DEFEND, "Obranný Postoj", coroutineScope)
                        fxState.triggerFloatingText(
                            text = "+50% Obrana",
                            isShield = true,
                            isEnemyTarget = false,
                            scope = coroutineScope
                        )
                        val (next, _) = PartyCombatManager.executeDefend(session)
                        onSessionUpdated(next)
                    }
                },
                onUseItem = { item ->
                    coroutineScope.launch {
                        fxState.triggerAbility(CombatAbilityType.ITEM_HEAL, item.name, coroutineScope)
                        val healAmount = if (item.hpBonus > 0) item.hpBonus else 50
                        fxState.triggerFloatingText(
                            text = "+$healAmount HP",
                            isHeal = true,
                            isEnemyTarget = false,
                            scope = coroutineScope
                        )
                        val (next, _) = PartyCombatManager.executeUseItem(session, item, session.selectedTargetAllyIndex)
                        onSessionUpdated(next)
                    }
                }
            )
        }
    }

    // --- STATUS EFFECT COUNTDOWN & DETAIL MODAL ---
    val currentSelectedStatus = selectedStatusEffectForDetail
    if (currentSelectedStatus != null) {
        CombatStatusEffectDetailDialog(
            effect = currentSelectedStatus,
            onDismiss = { selectedStatusEffectForDetail = null }
        )
    }

    // --- FULL LOGS MODAL ---
    if (showLogsModal) {
        PartyCombatLogModal(
            logs = session.combatLogs,
            comboChainCount = session.comboChainCount,
            onDismiss = { showLogsModal = false }
        )
    }

    // --- ELEMENTAL COMBAT LOG MODAL ---
    if (showElementalLogsModal) {
        ElementalCombatLogModal(
            logs = session.combatLogs,
            onDismiss = { showElementalLogsModal = false }
        )
    }

    // --- ELEMENTAL SYNERGY MODAL ---
    if (showElementalSynergyModal) {
        ElementalSynergyModal(
            activeSynergies = session.elementalSynergies,
            combatLogs = session.combatLogs,
            onDismiss = { showElementalSynergyModal = false }
        )
    }

    // --- VICTORY MODAL ---
    if (session.isFinished && session.isVictory) {
        PartyCombatVictoryDialog(
            session = session,
            onForgeFragment = { fragId ->
                engine.forgeEquipmentFromFragments(fragId)
            },
            onDismiss = {
                // Apply victory rewards to gameState via engine with rich item drops and companion XP
                val rew = session.rewards
                if (rew != null) {
                    engine.awardPartyCombatVictoryWithDetails(rew, session)
                }
                onExitCombat()
            }
        )
    }

    // --- DEFEAT MODAL ---
    if (session.isFinished && !session.isVictory) {
        PartyCombatDefeatDialog(
            session = session,
            onDismiss = onExitCombat
        )
    }
}
