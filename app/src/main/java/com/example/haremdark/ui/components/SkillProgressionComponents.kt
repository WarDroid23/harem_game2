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
import com.example.haremdark.data.CharacterSkillCatalog
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
    onUnlockWithSp: () -> Unit,
    onEnhanceWithResources: (() -> Unit)? = null
) {
    val rank = CharacterSkillCatalog.getSkillRank(character, node.id)
    val nextRank = if (rank <= 0) 1 else rank + 1
    val upgradeCost = CharacterSkillCatalog.getUpgradeCostForRank(node, nextRank)
    val (canEnhance, enhanceMsg) = CharacterSkillCatalog.canEnhanceNode(character, node)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(node.icon, fontSize = 26.sp)
                Column {
                    Text(node.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (rank > 0) Color(0xFFFFD700).copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (rank > 0) "⭐ RANK $rank / ${node.maxRank}" else "🔒 Uzamčeno",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (rank > 0) Color(0xFFFFD700) else Color.LightGray,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text("• Stupeň ${node.tier}", fontSize = 11.sp, color = Color(0xFFFF80AB))
                    }
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
                    val currentMultiplier = act.powerMultiplier + (rank.coerceAtLeast(1) - 1) * 0.25f
                    val nextMultiplier = currentMultiplier + 0.25f
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF220918),
                        border = BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("📐 Matematický vzorec & Škálování podle úrovně", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFFFD700))
                            Text(
                                text = "• Síla úderu: ${(currentMultiplier * 100).toInt()}% ${if (rank > 0 && nextRank <= node.maxRank) "➔ ${(nextMultiplier * 100).toInt()}% (Další rank)" else ""}\n• Spotřeba many: ${act.manaCost} MP\n• Obnova (Cooldown): ${act.cooldownTurns} kola\n• Typ cíle: ${act.targetType.name}",
                                fontSize = 11.sp,
                                color = Color(0xFFCFD8DC),
                                lineHeight = 16.sp
                            )
                            if (act.appliedStatus != null) {
                                val currentVal = act.appliedStatus.value + (rank.coerceAtLeast(1) - 1) * 4
                                Text(
                                    text = "• Efekt stavu: ${act.appliedStatus.icon} ${act.appliedStatus.name} (${act.appliedStatus.durationTurns} kola, síla $currentVal)",
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
                    val r = rank.coerceAtLeast(1)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF141C2E),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("⭐ Trvalé pasivní bonusy (Rank $r)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF00E5FF))
                            if (node.attackBonus > 0) Text("• +${node.attackBonus * r} Útok ${if (nextRank <= node.maxRank) "(➔ +${node.attackBonus * (r + 1)})" else ""}", fontSize = 11.sp, color = Color.White)
                            if (node.defenseBonus > 0) Text("• +${node.defenseBonus * r} Obrana ${if (nextRank <= node.maxRank) "(➔ +${node.defenseBonus * (r + 1)})" else ""}", fontSize = 11.sp, color = Color.White)
                            if (node.hpBonus > 0) Text("• +${node.hpBonus * r} Max HP ${if (nextRank <= node.maxRank) "(➔ +${node.hpBonus * (r + 1)})" else ""}", fontSize = 11.sp, color = Color.White)
                            if (node.critBonus > 0) Text("• +${node.critBonus * r}% Kritická šance ${if (nextRank <= node.maxRank) "(➔ +${node.critBonus * (r + 1)}%)" else ""}", fontSize = 11.sp, color = Color.White)
                            if (node.lifestealPercent > 0) Text("• +${node.lifestealPercent * r}% Vysávání HP ${if (nextRank <= node.maxRank) "(➔ +${node.lifestealPercent * (r + 1)}%)" else ""}", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }

                // Resource Upgrade Breakdown (Rank progression)
                if (nextRank <= node.maxRank) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1F102B),
                        border = BorderStroke(1.dp, Color(0xFFBA68C8).copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("💎 Suroviny na vylepšení (Rank $nextRank)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFFFD700))
                            Text("• Zlato: ${upgradeCost.goldCost}💰 • Temná E.: ${upgradeCost.darkEnergyCost}🔮 • ZK: ${upgradeCost.xpCost}", fontSize = 11.sp, color = Color.LightGray)
                            val matList = buildString {
                                if (upgradeCost.darkShards > 0) append("💎 Střepy: ${upgradeCost.darkShards}  ")
                                if (upgradeCost.manaEssence > 0) append("✨ Esence: ${upgradeCost.manaEssence}  ")
                                if (upgradeCost.moonDust > 0) append("🌙 Prach: ${upgradeCost.moonDust}  ")
                                if (upgradeCost.dragonBlood > 0) append("🩸 Krev: ${upgradeCost.dragonBlood}  ")
                                if (upgradeCost.crystals > 0) append("💎 Krystaly: ${upgradeCost.crystals}  ")
                            }
                            if (matList.isNotEmpty()) {
                                Text("• Vzácné suroviny: $matList", fontSize = 11.sp, color = Color(0xFF80D8FF), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (onEnhanceWithResources != null && nextRank <= node.maxRank) {
                    Button(
                        onClick = onEnhanceWithResources,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (rank == 0) "✨ Odemknout za suroviny" else "🚀 Vylepšit na Rank $nextRank (Suroviny)",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                if (!isUnlocked) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onUnlockWithXp,
                            enabled = canUnlock && character.xp >= node.xpCost,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Odemknout (${node.xpCost} ZK)", fontSize = 11.sp)
                        }
                        Button(
                            onClick = onUnlockWithSp,
                            enabled = canUnlock && character.skillPoints >= node.spCost,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Odemknout (${node.spCost} SP)", color = Color.Black, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zavřít", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF140810)
    )
}
