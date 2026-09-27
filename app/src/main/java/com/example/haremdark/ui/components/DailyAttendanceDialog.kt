package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.haremdark.domain.DailyRewardState
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import kotlin.random.Random

/**
 * Interactive Daily Attendance and Login Reward Dialog.
 *
 * Features a 7-day calendar track, streak bonus multiplier,
 * multi-resource reward breakdown (Gold, Mana, Gems, Skill Points, Items),
 * and celebratory claim animation to maximize retention.
 */
@Composable
fun DailyAttendanceDialog(
    reward: DailyRewardState,
    onClaim: () -> Unit,
    onDismiss: (() -> Unit)? = null
) {
    var isClaimedAnim by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { onDismiss?.invoke() },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140822)),
            border = BorderStroke(2.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFAB47BC)))),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Background Glow & Confetti Effects
                if (isClaimedAnim) {
                    ConfettiOverlay()
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Title
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
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🎁", fontSize = 22.sp)
                                }
                            }

                            Column {
                                Text(
                                    text = "Denní Odměny Věrnosti",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFD700)
                                )
                                Text(
                                    text = "Série přihlášení: ${reward.consecutiveDays} dny v řadě 🔥",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE1BEE7)
                                )
                            }
                        }

                        if (onDismiss != null) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White)
                            }
                        }
                    }

                    // Streak Multiplier Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2A0F3D),
                        border = BorderStroke(1.dp, Color(0xFFBA68C8).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Bonus za věrnost: +${reward.streakBonusPercent}% k odměnám",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF4A148C)
                            ) {
                                Text(
                                    text = "Den ${reward.consecutiveDays}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD54F),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // 7-Day Attendance Track
                    val cycleDay = if (reward.consecutiveDays % 7 == 0) 7 else reward.consecutiveDays % 7

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (day in 1..7) {
                            val isToday = day == cycleDay
                            val isPast = day < cycleDay

                            val scale by animateFloatAsState(
                                targetValue = if (isToday) 1.15f else 1.0f,
                                animationSpec = spring(stiffness = Spring.StiffnessLow),
                                label = "DayScale"
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                            ) {
                                Text(
                                    text = "Den $day",
                                    fontSize = 9.sp,
                                    color = if (isToday) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f),
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                )

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isPast -> Color(0xFF4CAF50).copy(alpha = 0.25f)
                                                isToday -> Color(0xFFFFD700).copy(alpha = 0.25f)
                                                else -> Color.White.copy(alpha = 0.05f)
                                            }
                                        )
                                        .border(
                                            width = if (isToday) 2.5.dp else 1.dp,
                                            color = when {
                                                isPast -> Color(0xFF4CAF50)
                                                isToday -> Color(0xFFFFD700)
                                                else -> Color.White.copy(alpha = 0.15f)
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        isPast -> Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                        day == 7 -> Text("👑", fontSize = 18.sp)
                                        day == 5 -> Text("🎁", fontSize = 16.sp)
                                        else -> Text("💰", fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    // Today's Multi-Resource Rewards Cards
                    Text(
                        text = "Dnešní vybraná kořist (Den ${reward.consecutiveDays}):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RewardResourceChip("💰 Zlato", "+${reward.rewardGold}", Color(0xFFFFD700), Modifier.weight(1f))
                            RewardResourceChip("🔮 Temná energie", "+${reward.rewardMana}", Color(0xFFAB47BC), Modifier.weight(1f))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RewardResourceChip("💎 Drahokamy", "+${reward.rewardGems}", Color(0xFF00E5FF), Modifier.weight(1f))
                            if (reward.rewardSkillPoints > 0) {
                                RewardResourceChip("⭐ Body dovedností", "+${reward.rewardSkillPoints}", Color(0xFFFF9800), Modifier.weight(1f))
                            } else {
                                RewardResourceChip("✨ Zkušenosti", "+${reward.rewardXp} XP", Color(0xFF69F0AE), Modifier.weight(1f))
                            }
                        }

                        // Special Item / Gift Reward if present
                        if (reward.itemReward != null) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF330B48),
                                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4A148C)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(reward.itemReward.icon, fontSize = 26.sp)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "⭐ Speciální bonus za milník!",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFD700)
                                        )
                                        Text(
                                            text = reward.itemReward.name,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = reward.itemReward.effectDescription,
                                            fontSize = 11.sp,
                                            color = Color(0xFFE1BEE7)
                                        )
                                    }
                                }
                            }
                        }

                        // Bonus Rare Crafting Materials for Consecutive Streak
                        if (reward.craftingMaterialsReward.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF26123D),
                                border = BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "⚒️ Vzácné řemeslné suroviny za sérií:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD700)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val materialNames = mapOf(
                                            "temny_strep" to "💎 Střepy",
                                            "mana_esence" to "✨ Esence",
                                            "zelezna_ruda" to "⚙️ Ruda",
                                            "drevohorec" to "🪵 Dřevo",
                                            "mesicni_prach" to "🌙 Prach",
                                            "draci_krev" to "🩸 Krev",
                                            "krystal" to "💎 Krystaly"
                                        )
                                        reward.craftingMaterialsReward.forEach { (matId, count) ->
                                            val label = materialNames[matId] ?: matId
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF381E52)
                                            ) {
                                                Text(
                                                    text = "$label +$count",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF81D4FA),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Claim Button
                    Button(
                        onClick = {
                            isClaimedAnim = true
                            SoundEffectManager.playLevelUp()
                            HapticManager.vibrateHeavy()
                            onClaim()
                        },
                        enabled = !reward.isAlreadyClaimedToday,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFF332244),
                            disabledContentColor = Color.Gray
                        )
                    ) {
                        Text(
                            text = if (reward.isAlreadyClaimedToday) "Dnes již vybráno ✓" else "🎁 Převzít dnešní odměnu",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardResourceChip(
    title: String,
    valueStr: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
            Text(valueStr, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
private fun ConfettiOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val particles = 30
        for (i in 0 until particles) {
            val x = (i * 37) % size.width.toInt()
            val y = (i * 47) % size.height.toInt()
            val particleColor = when (i % 4) {
                0 -> Color(0xFFFFD700)
                1 -> Color(0xFFE91E63)
                2 -> Color(0xFF00E5FF)
                else -> Color(0xFF69F0AE)
            }
            drawCircle(
                color = particleColor.copy(alpha = 0.7f),
                radius = (4..8).random().dp.toPx(),
                center = Offset(x.toFloat(), y.toFloat())
            )
        }
    }
}
