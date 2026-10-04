package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.models.CombatEnemy
import com.example.haremdark.models.PartyMember

/**
 * Tactical Command definition for the battle overlay.
 */
data class TacticalCommandOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badgeText: String,
    val badgeColor: Color,
    val accentColor: Color,
    val tacticalDetails: String,
    val testTag: String
)

/**
 * Overlay menu providing tactical options ('Defend', 'Focus Fire', 'Retreat', etc.)
 * for the player during combat encounters.
 */
@Composable
fun TacticalOverlayMenu(
    visible: Boolean,
    enemies: List<CombatEnemy>,
    selectedTargetIndex: Int,
    activeMember: PartyMember?,
    onSelectTarget: (Int) -> Unit,
    onDefend: () -> Unit,
    onFocusFire: (targetIndex: Int) -> Unit,
    onRetreat: () -> Unit,
    onAllOutAssault: (() -> Unit)? = null,
    onRally: (() -> Unit)? = null,
    onActivateAffinityBuff: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    if (!visible) return

    var showRetreatConfirm by remember { mutableStateOf(false) }
    var currentTargetIdx by remember(selectedTargetIndex) { mutableIntStateOf(selectedTargetIndex) }
    val aliveEnemies = enemies.filter { it.isAlive }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .heightIn(max = 680.dp)
                    .clickable(enabled = false) {} // Prevent backdrop click-through
                    .testTag("tactical_overlay_menu"),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF14081E),
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.8f),
                            Color(0xFFFF4081).copy(alpha = 0.5f),
                            Color(0xFF7B1FA2).copy(alpha = 0.8f)
                        )
                    )
                ),
                shadowElevation = 24.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFFFFD700)),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.GpsFixed,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Taktické Rozkazy",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = if (activeMember != null) "Velení pro: ${activeMember.name}" else "Strategické povely Dominia",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFF80AB),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                HapticManager.vibrateClick()
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Zavřít menu",
                                tint = Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Target Selector for Focus Fire / Direct Commands
                    if (aliveEnemies.isNotEmpty()) {
                        Text(
                            text = "🎯 Cíl pro soustředění palby:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(enemies.indices.toList()) { idx ->
                                val enemy = enemies[idx]
                                if (enemy.isAlive) {
                                    val isSelected = (idx == currentTargetIdx)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) Color(0xFF38101C) else Color(0xFF1E1022),
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color(0xFFFF1744) else Color.White.copy(alpha = 0.15f)
                                        ),
                                        modifier = Modifier
                                            .clickable {
                                                currentTargetIdx = idx
                                                onSelectTarget(idx)
                                                HapticManager.vibrateClick()
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(enemy.icon, fontSize = 16.sp)
                                            Column {
                                                Text(
                                                    text = enemy.name,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color(0xFFFF8A80) else Color.White,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "${enemy.hp}/${enemy.maxHp} HP",
                                                    fontSize = 9.sp,
                                                    color = Color.LightGray
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Adjust,
                                                    contentDescription = "Vybráno",
                                                    tint = Color(0xFFFF1744),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Tactical Command Options List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. DEFEND OPTION
                        item {
                            TacticalActionCard(
                                title = "Obrana & Kryt (Defend)",
                                subtitle = "Pevná formace štítů",
                                details = "-60% utržené poškození, krytí před kritickými zásahy a okamžitá obnova +15 MP.",
                                icon = Icons.Default.Shield,
                                badgeText = "DEFENZÍVA",
                                badgeColor = Color(0xFF1565C0),
                                accentColor = Color(0xFF42A5F5),
                                testTag = "tactical_option_defend",
                                onClick = {
                                    HapticManager.vibrateClick()
                                    onDefend()
                                    onDismiss()
                                }
                            )
                        }

                        // 2. FOCUS FIRE OPTION
                        item {
                            val targetName = aliveEnemies.getOrNull(currentTargetIdx)?.name ?: "Cíl"
                            TacticalActionCard(
                                title = "Soustředěná palba (Focus Fire)",
                                subtitle = "Prioritní cíl: $targetName",
                                details = "Označí cíl (-35% obrana, +35% zranitelnost) a koordinuje okamžitý průrazný úder družiny.",
                                icon = Icons.Default.GpsFixed,
                                badgeText = "PRIORITNÍ CÍL",
                                badgeColor = Color(0xFFC62828),
                                accentColor = Color(0xFFFF5252),
                                testTag = "tactical_option_focus_fire",
                                onClick = {
                                    HapticManager.vibrateHeavy()
                                    onFocusFire(currentTargetIdx)
                                    onDismiss()
                                }
                            )
                        }

                        // 3. ALL-OUT ASSAULT OPTION
                        if (onAllOutAssault != null) {
                            item {
                                TacticalActionCard(
                                    title = "Totální zteč (All-Out Assault)",
                                    subtitle = "Maximální bojové nasazení",
                                    details = "+30% útočná síla všech bojovnic a vyšší kritické zásahy na příští 2 kola.",
                                    icon = Icons.Default.Bolt,
                                    badgeText = "OFENZÍVA",
                                    badgeColor = Color(0xFFE65100),
                                    accentColor = Color(0xFFFFB74D),
                                    testTag = "tactical_option_assault",
                                    onClick = {
                                        HapticManager.vibrateHeavy()
                                        onAllOutAssault()
                                        onDismiss()
                                    }
                                )
                            }
                        }

                        // 4. RALLY & INSPIRE OPTION
                        if (onRally != null) {
                            item {
                                TacticalActionCard(
                                    title = "Povzbuzení & Pokřik (Rally)",
                                    subtitle = "Obnova morálky a sil",
                                    details = "Očistí 1 negativní oslabení ze všech společníků, doplní +20 MP a +25 HP celé družině.",
                                    icon = Icons.Default.AutoAwesome,
                                    badgeText = "MORÁLKA",
                                    badgeColor = Color(0xFF2E7D32),
                                    accentColor = Color(0xFF69F0AE),
                                    testTag = "tactical_option_rally",
                                    onClick = {
                                        HapticManager.vibrateClick()
                                        onRally()
                                        onDismiss()
                                    }
                                )
                            }
                        }

                        // CHARACTER-SPECIFIC AFFINITY SPECIAL ABILITY BUFF
                        if (activeMember != null && !activeMember.isPlayer && activeMember.characterSpecificBuffName.isNotBlank() && onActivateAffinityBuff != null) {
                            item {
                                TacticalActionCard(
                                    title = "✨ Afinitní schopnost: ${activeMember.characterSpecificBuffName}",
                                    subtitle = "Úroveň ${activeMember.relationshipTierLevel} (${activeMember.relationshipStageName})",
                                    details = activeMember.characterSpecificBuffSummary,
                                    icon = Icons.Default.AutoFixHigh,
                                    badgeText = "AFINITNÍ BUFF",
                                    badgeColor = Color(0xFF6A1B9A),
                                    accentColor = Color(0xFFE040FB),
                                    testTag = "tactical_option_affinity_buff",
                                    onClick = {
                                        HapticManager.vibrateHeavy()
                                        onActivateAffinityBuff()
                                        onDismiss()
                                    }
                                )
                            }
                        }

                        // 5. RETREAT OPTION
                        item {
                            TacticalActionCard(
                                title = "Taktický ústup (Retreat)",
                                subtitle = "Dýmovnice & bezpečné stažení",
                                details = "Okamžitě ukončí boj za pomoci krycí dýmovnice. Zabrání padnutí družiny a zachová nasbírané suroviny.",
                                icon = Icons.AutoMirrored.Filled.ExitToApp,
                                badgeText = "ÚSTUP",
                                badgeColor = Color(0xFF455A64),
                                accentColor = Color(0xFFFF8A80),
                                testTag = "tactical_option_retreat",
                                onClick = {
                                    HapticManager.vibrateClick()
                                    showRetreatConfirm = true
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tactical tip footer
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("💡", fontSize = 12.sp)
                            Text(
                                text = "Taktické povely lze kombinovat s formací družiny a přednostním cílením.",
                                fontSize = 10.sp,
                                color = Color(0xFFCE93D8)
                            )
                        }
                    }
                }
            }

            // Retreat confirmation sub-dialog
            if (showRetreatConfirm) {
                AlertDialog(
                    onDismissRequest = { showRetreatConfirm = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("💨", fontSize = 20.sp)
                            Text("Potvrdit taktický ústup?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    },
                    text = {
                        Text(
                            text = "Opravdu chceš odpálit dýmovnici a stáhnout družinu z tohoto střetu zpět do bezpečí sídla?",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showRetreatConfirm = false
                                onRetreat()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            modifier = Modifier.testTag("confirm_retreat_button")
                        ) {
                            Text("Ano, ustoupit", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showRetreatConfirm = false }) {
                            Text("Zůstat v boji")
                        }
                    },
                    containerColor = Color(0xFF1E1022),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

/**
 * Modern tactile card for individual tactical action options.
 */
@Composable
private fun TacticalActionCard(
    title: String,
    subtitle: String,
    details: String,
    icon: ImageVector,
    badgeText: String,
    badgeColor: Color,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F1228)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = details,
                    fontSize = 10.sp,
                    color = Color.LightGray.copy(alpha = 0.85f),
                    lineHeight = 13.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = accentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
