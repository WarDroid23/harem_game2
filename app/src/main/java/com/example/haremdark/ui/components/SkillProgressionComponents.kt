package com.example.haremdark.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.CharacterSkillNode
import com.example.haremdark.data.CombatPassiveBonuses
import com.example.haremdark.data.StaticData
import com.example.haremdark.models.Character as GameCharacter

@Composable
fun CharacterProgressHeroCard(
    character: GameCharacter,
    onConvertXpToSp: () -> Unit
) {
    val reqXpForNextLevel = character.level * 100
    val progress = (character.xp.toFloat() / reqXpForNextLevel.toFloat()).coerceIn(0f, 1f)
    val loyaltyTier = StaticData.getLoyaltyTier(character.loajalita)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                Brush.horizontalGradient(listOf(Color(0xFFFF4081), Color(0xFFFFD700))),
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF220A17)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFFFFD700), CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = StaticData.getPortraitForArchetype(character.archetypeId)),
                        contentDescription = character.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = character.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${character.archetypeId} • Úroveň ${character.level}",
                        fontSize = 12.sp,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Oddanost: ${loyaltyTier.title} (${character.affinityPoints} bodů)",
                        fontSize = 11.sp,
                        color = Color(0xFFFF80AB)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bojové ZK: ${character.xp} / $reqXpForNextLevel",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .width(180.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFFF4081),
                        trackColor = Color(0xFF4A142A)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF3E1229),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("💎", fontSize = 14.sp)
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Dovednostní body", fontSize = 9.sp, color = Color(0xFFB0BEC5))
                            Text("${character.skillPoints} SP", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onConvertXpToSp,
                    enabled = character.xp >= 100,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFFD700)
                    ),
                    border = BorderStroke(1.dp, if (character.xp >= 100) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("🔄 Převést 100 ZK ➔ 1 SP", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun PassiveBonusSummaryCard(bonuses: CombatPassiveBonuses) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF190C18)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("🛡️", fontSize = 14.sp)
                Text(
                    text = "AKTIVNÍ PASIVNÍ BONUSY ZE STROMU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF),
                    letterSpacing = 0.5.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatBadgeItem(label = "Útok", value = "+${bonuses.attackBonus}", icon = "⚔️", color = Color(0xFFFF5252))
                StatBadgeItem(label = "Obrana", value = "+${bonuses.defenseBonus}", icon = "🛡️", color = Color(0xFF448AFF))
                StatBadgeItem(label = "Max HP", value = "+${bonuses.hpBonus}", icon = "❤️", color = Color(0xFF00E676))
                StatBadgeItem(label = "Krit", value = "+${bonuses.critBonus}%", icon = "💥", color = Color(0xFFFFD700))
                if (bonuses.lifestealPercent > 0) {
                    StatBadgeItem(label = "Vysávání", value = "${bonuses.lifestealPercent}%", icon = "🩸", color = Color(0xFFFF1744))
                }
            }
        }
    }
}

@Composable
fun StatBadgeItem(label: String, value: String, icon: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$icon $value", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun SkillNodeDetailModal(
    node: CharacterSkillNode,
    character: GameCharacter,
    isUnlocked: Boolean,
    canUnlock: Boolean,
    onDismiss: () -> Unit,
    onUnlockWithXp: () -> Unit,
    onUnlockWithSp: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(node.icon, fontSize = 24.sp)
                Column {
                    Text(node.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    Text("Podrobná analýza schopnosti • Stupeň ${node.tier}", fontSize = 11.sp, color = Color(0xFFFF80AB))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(node.description, fontSize = 13.sp, color = Color(0xFFE0E0E0))

                if (node.activeSkill != null) {
                    val act = node.activeSkill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF220918),
                        border = BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("📐 Matematický vzorec a škálování", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFFFD700))
                            Text(
                                text = "• Základní násobič: ${act.powerMultiplier}x Útok\n• Základní bonus k poškození: ${act.baseDamageBonus}\n• Výpočet poškození: ((Útok × ${act.powerMultiplier}) + ${act.baseDamageBonus}) × Afinitní bonus × Synergie\n• Spotřeba many: ${act.manaCost} MP\n• Obnova (Cooldown): ${act.cooldownTurns} kola\n• Typ cíle: ${act.targetType.name}",
                                fontSize = 11.sp,
                                color = Color(0xFFCFD8DC),
                                lineHeight = 16.sp
                            )
                            if (act.appliedStatus != null) {
                                Text(
                                    text = "• Efekt stavu: ${act.appliedStatus.icon} ${act.appliedStatus.name} (${act.appliedStatus.durationTurns} kola, síla ${act.appliedStatus.value})",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFF80AB),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (act.voiceQuote != null) {
                                Text(
                                    text = "💬 Hláška: \"${act.voiceQuote}\"",
                                    fontSize = 11.sp,
                                    color = Color(0xFF00E5FF),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }

                if (node.attackBonus > 0 || node.defenseBonus > 0 || node.hpBonus > 0 || node.critBonus > 0 || node.lifestealPercent > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF141C2E),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("⭐ Trvalé pasivní bonusy", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF00E5FF))
                            if (node.attackBonus > 0) Text("• +${node.attackBonus} Útok", fontSize = 11.sp, color = Color.White)
                            if (node.defenseBonus > 0) Text("• +${node.defenseBonus} Obrana", fontSize = 11.sp, color = Color.White)
                            if (node.hpBonus > 0) Text("• +${node.hpBonus} Max HP", fontSize = 11.sp, color = Color.White)
                            if (node.critBonus > 0) Text("• +${node.critBonus}% Kritická šance", fontSize = 11.sp, color = Color.White)
                            if (node.lifestealPercent > 0) Text("• +${node.lifestealPercent}% Vysávání HP (Lifesteal)", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1F121C),
                    border = BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("📌 Požadavky na odemknutí", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFE1BEE7))
                        Text("• Požadovaná úroveň hrdinky: ${node.reqLevel} (Aktuálně: ${character.level})", fontSize = 11.sp, color = if (character.level >= node.reqLevel) Color(0xFF00E676) else Color(0xFFFF5252))
                        if (node.reqAffinityLevel > 0) {
                            Text("• Požadovaná Náklonnost: Úr. ${node.reqAffinityLevel} (Aktuálně: Úr. ${character.affinityLevel})", fontSize = 11.sp, color = if (character.affinityLevel >= node.reqAffinityLevel) Color(0xFF00E676) else Color(0xFFFF5252))
                        }
                        if (node.reqLoajalita > 0) {
                            Text("• Požadovaná Loajalita: ${node.reqLoajalita}% (Aktuálně: ${character.loajalita}%)", fontSize = 11.sp, color = if (character.loajalita >= node.reqLoajalita) Color(0xFF00E676) else Color(0xFFFF5252))
                        }
                        Text("• Náklady v ZK: ${node.xpCost} ZK (Aktuálně: ${character.xp})", fontSize = 11.sp, color = if (character.xp >= node.xpCost) Color(0xFF00E676) else Color(0xFFFF5252))
                        Text("• Náklady v SP: ${node.spCost} SP (Aktuálně: ${character.skillPoints})", fontSize = 11.sp, color = if (character.skillPoints >= node.spCost) Color(0xFF00E676) else Color(0xFFFF5252))
                    }
                }
            }
        },
        confirmButton = {
            if (!isUnlocked) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onUnlockWithXp,
                        enabled = canUnlock && character.xp >= node.xpCost,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081))
                    ) {
                        Text("Odemknout (${node.xpCost} ZK)")
                    }
                    Button(
                        onClick = onUnlockWithSp,
                        enabled = canUnlock && character.skillPoints >= node.spCost,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                    ) {
                        Text("Odemknout (${node.spCost} SP)", color = Color.Black)
                    }
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Zavřít", color = Color(0xFFFFD700))
                }
            }
        },
        dismissButton = {
            if (!isUnlocked) {
                TextButton(onClick = onDismiss) {
                    Text("Zpět", color = Color.Gray)
                }
            }
        },
        containerColor = Color(0xFF140810)
    )
}
