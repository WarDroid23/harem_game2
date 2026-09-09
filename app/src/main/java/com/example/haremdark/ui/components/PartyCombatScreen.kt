package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.PartyCombatManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.CombatLogEntry
import com.example.haremdark.models.GameSave
import com.example.haremdark.models.PartyCombatSession
import com.example.haremdark.models.SkillTargetType
import kotlinx.coroutines.launch

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

    val aliveEnemies = session.enemies.filter { it.isAlive }
    val aliveParty = session.party.filter { it.isAlive }
    val activeMember = session.currentActiveMember

    val targetEnemy = aliveEnemies.getOrNull(session.selectedTargetEnemyIndex) ?: aliveEnemies.firstOrNull()

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

        // Main Combat Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                            onClick = { SoundEffectManager.toggleMute() },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "Zvuk",
                                tint = if (isMuted) Color.Red else Color(0xFFFFD700),
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
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Ústup",
                                tint = Color(0xFFFF8A80),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

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
                    onSelectTarget = { idx ->
                        onSessionUpdated(session.copy(selectedTargetEnemyIndex = idx))
                    }
                )
            }

            // --- LATEST COMBAT FEEDBACK LOG ---
            val lastLog = session.combatLogs.firstOrNull()
            if (lastLog != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xDD120818),
                    border = BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("⚡", fontSize = 14.sp)
                        Text(
                            text = lastLog.message,
                            fontSize = 11.sp,
                            color = Color(0xFFF8BBD0),
                            maxLines = 2
                        )
                    }
                }
            }

            // --- HAREM ULTIMATE COMBO GAUGE ---
            HaremUltimateComboGauge(
                gauge = session.haremComboGauge,
                maxGauge = session.maxHaremComboGauge,
                onTriggerCombo = {
                    coroutineScope.launch {
                        fxState.triggerAttackSequence(
                            attackerPartyIndex = session.currentTurnIndex,
                            targetEnemyIndex = session.selectedTargetEnemyIndex,
                            abilityType = CombatAbilityType.CHAR_SPECIAL,
                            customName = "Harémové Kombo Dominia",
                            damageText = "-${(activeMember?.attack ?: 30) * 3}",
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
                        val baseDmg = (activeMember?.attack ?: 20)
                        fxState.triggerAttackSequence(
                            attackerPartyIndex = session.currentTurnIndex,
                            targetEnemyIndex = session.selectedTargetEnemyIndex,
                            abilityType = CombatAbilityType.SLASH,
                            customName = "Bojový Výpad",
                            damageText = "-$baseDmg",
                            isCrit = false,
                            scope = coroutineScope
                        )
                        val (next, _) = PartyCombatManager.executeBasicAttack(session, session.selectedTargetEnemyIndex)
                        onSessionUpdated(next)
                    }
                },
                onSkillSelect = { skill ->
                    coroutineScope.launch {
                        val animType = when (skill.animationType) {
                            "DARK_BURST" -> CombatAbilityType.DARK_BURST
                            "SHADOW_CURSE" -> CombatAbilityType.SHADOW_CURSE
                            "BLEED_STRIKE" -> CombatAbilityType.BLEED_STRIKE
                            "HEAVY_STRIKE" -> CombatAbilityType.HEAVY_STRIKE
                            "HAREM_SUPPORT" -> CombatAbilityType.HAREM_SUPPORT
                            "DEFEND" -> CombatAbilityType.DEFEND
                            else -> CombatAbilityType.SLASH
                        }
                        val isAllyTarget = (skill.targetType == SkillTargetType.SINGLE_ALLY || skill.targetType == SkillTargetType.ALL_ALLIES)
                        val estimatedDmg = ((activeMember?.attack ?: 20) * skill.powerMultiplier).toInt()

                        if (isAllyTarget) {
                            fxState.triggerAbility(animType, skill.name, coroutineScope)
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
                                customName = skill.name,
                                damageText = "-$estimatedDmg",
                                isCrit = skill.powerMultiplier >= 1.5f,
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

    // --- FULL LOGS MODAL ---
    if (showLogsModal) {
        PartyCombatLogModal(
            logs = session.combatLogs,
            onDismiss = { showLogsModal = false }
        )
    }

    // --- VICTORY MODAL ---
    if (session.isFinished && session.isVictory) {
        PartyCombatVictoryDialog(
            session = session,
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
