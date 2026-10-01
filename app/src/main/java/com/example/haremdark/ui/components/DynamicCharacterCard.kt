package com.example.haremdark.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.models.CombatStatusEffect
import com.example.haremdark.models.PartyMember

/**
 * A highly dynamic and interactive character card component.
 * Displays unit's portrait, health bar, and active status effects.
 * Includes a ripple effect and subtle scale animation on tap.
 */
@Composable
fun DynamicCharacterCard(
    member: PartyMember,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isAttacking: Boolean = false,
    isHit: Boolean = false,
    showMana: Boolean = true,
    onSelectStatusEffect: (CombatStatusEffect) -> Unit = {}
) {
    val hpPercent = member.hpPercent
    val isLowHp = hpPercent < 0.3f
    
    // Scale animation on interaction
    var isPressed by remember { mutableStateOf(false) }
    val interactionScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "card_scale"
    )

    // HP Bar Color Transition
    val hpColor = when {
        hpPercent > 0.6f -> Color(0xFF4CAF50) // Green
        hpPercent > 0.3f -> Color(0xFFFFC107) // Amber
        else -> Color(0xFFF44336) // Red
    }

    // Glow effect for selected or active turn
    val borderBrush = when {
        isAttacking -> Brush.sweepGradient(listOf(Color(0xFFFF4081), Color(0xFFFFD700), Color(0xFFFF4081)))
        isSelected -> {
            val infiniteTransition = rememberInfiniteTransition(label = "border_glow")
            val glowAlpha by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glow_alpha"
            )
            Brush.sweepGradient(
                listOf(
                    Color(0xFFFFD700).copy(alpha = glowAlpha),
                    Color(0xFFFF4081).copy(alpha = glowAlpha),
                    Color(0xFFFFD700).copy(alpha = glowAlpha)
                )
            )
        }
        else -> Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.04f)
            )
        )
    }

    Card(
        modifier = modifier
            .width(150.dp) // Fixed width for formation row
            .scale(interactionScale * (if (isAttacking) 1.05f else 1f))
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isAttacking) 3.dp else if (isSelected) 2.dp else 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = member.isAlive) {
                HapticManager.vibrateClick()
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !member.isAlive -> Color(0xFF1B141E).copy(alpha = 0.6f)
                isHit -> Color(0xFF880E4F)
                isLowHp -> Color(0xFF2C1619)
                isSelected -> Color(0xFF2A1A2E)
                else -> Color(0xFF1A161E)
            },
            contentColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Header Section: Portrait & Role
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2431))
                        .border(1.2.dp, if (isSelected) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (member.isPlayer) {
                        Text("👑", fontSize = 20.sp)
                    } else {
                        val portraitRes = StaticData.getPortraitForArchetype(member.archetypeId)
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(portraitRes)
                                .crossfade(true)
                                .build(),
                            contentDescription = member.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            error = {
                                Text(member.role.icon, fontSize = 18.sp)
                            }
                        )
                    }
                    
                    if (!member.isAlive) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💀", fontSize = 16.sp)
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(member.role.icon, fontSize = 14.sp)
                    if (member.isDefending) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1565C0).copy(alpha = 0.35f),
                            border = BorderStroke(0.8.dp, Color(0xFF64B5F6))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Box(modifier = Modifier.size(10.dp), contentAlignment = Alignment.Center) {
                                    StatusLottieAura(
                                        isBuff = true,
                                        modifier = Modifier.fillMaxSize(),
                                        glowScale = 1.4f
                                    )
                                    Text("🛡️", fontSize = 7.sp)
                                }
                                Text("KRYT", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF64B5F6))
                            }
                        }
                    }
                }
            }

            // 2. Name & Level
            Column {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (member.isAlive) Color.White else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${member.loyaltyTierName} • Lvl ${member.relationshipTierLevel}",
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // 3. Stats Bars
            if (member.isAlive) {
                // HP
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    LinearProgressIndicator(
                        progress = { hpPercent },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = hpColor,
                        trackColor = hpColor.copy(alpha = 0.15f),
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("HP", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = hpColor)
                        Text("${member.hp}/${member.maxHp}", fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // MP
                if (showMana) {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        LinearProgressIndicator(
                            progress = { member.manaPercent },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFF2196F3),
                            trackColor = Color(0xFF2196F3).copy(alpha = 0.15f),
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("MP", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64B5F6))
                            Text("${member.mana}/${member.maxMana}", fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Text(
                    text = "V BEZVĚDOMÍ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFF44336),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }

            // 4. Status Effects
            if (member.statusEffects.isNotEmpty() && member.isAlive) {
                CharacterStatusEffectsRow(
                    statusEffects = member.statusEffects,
                    onSelectEffect = onSelectStatusEffect,
                    maxVisible = 3,
                    compact = true
                )
            }
            
            // 5. Special Bonus Text
            if (member.isAlive && !member.isPlayer) {
                val bonusText = when {
                    member.loyaltyAssistChancePercent > 0 -> "💖 Asist ${member.loyaltyAssistChancePercent}%"
                    member.loyaltyCombatBonusDmg > 1.0f -> "⚔️ +${((member.loyaltyCombatBonusDmg - 1f) * 100).toInt()}% DMG"
                    else -> null
                }
                if (bonusText != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = bonusText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF80AB),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
