package com.example.haremdark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.haremdark.domain.DailyRewardState

@Composable
fun DailyAttendanceDialog(
    reward: DailyRewardState,
    onClaim: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* Forced claim */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = "🎁 Týdenní Odměny za Věrnost",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Tvé temné dominium prosperuje každým dnem, kdy se ukážeš. Tvá věrnost je odměněna!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                // The 7-Day Track
                // We use newStreak % 7 to see where we are in the cycle.
                val cycleDay = if (reward.consecutiveDays % 7 == 0) 7 else reward.consecutiveDays % 7
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (day in 1..7) {
                        val isToday = day == cycleDay
                        val isPast = day < cycleDay
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Den $day", fontSize = 9.sp, color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal)
                            
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isPast -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                        }
                                    )
                                    .border(
                                        width = if (isToday) 2.dp else 1.dp,
                                        color = when {
                                            isPast -> Color(0xFF4CAF50)
                                            isToday -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    isPast -> Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                                    day == 7 -> Text("🎁", fontSize = 16.sp)
                                    else -> Text("💰", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Today's Reward Details
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Dnešní kořist (Den ${reward.consecutiveDays}):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFFD700).copy(alpha = 0.1f)) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("💰", fontSize = 20.sp)
                                Text("+${reward.rewardGold} Zlatých", fontWeight = FontWeight.Bold, color = Color(0xFFB8860B))
                            }
                        }
                        
                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF9C27B0).copy(alpha = 0.1f)) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("🔮", fontSize = 20.sp)
                                Text("+${reward.rewardMana} Temné energie", fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
                            }
                        }
                    }
                    
                    if (reward.itemReward != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surface, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(reward.itemReward.icon, fontSize = 28.sp)
                                }
                                Column {
                                    Text("Speciální odměna za věrnost!", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    Text(reward.itemReward.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(reward.itemReward.effectDescription, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Claim Button
                Button(
                    onClick = onClaim,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Převzít odměnu", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
