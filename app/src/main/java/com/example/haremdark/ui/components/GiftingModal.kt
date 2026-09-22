package com.example.haremdark.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.LoyaltyCombatData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.Character
import com.example.haremdark.data.GameContent
import com.example.haremdark.models.GameSave

@Composable
fun GiftingModal(
    character: Character,
    gameState: GameSave,
    engine: GameEngine,
    onDismiss: () -> Unit
) {
    val loyaltyBonus = remember(character.loajalita) {
        LoyaltyCombatData.getBonusForLoyalty(character.loajalita)
    }
    val affinityTier = remember(character.affinityPoints) {
        AffinityData.getTierForPoints(character.affinityPoints)
    }

    // Inventory items suitable as gifts
    val inventoryGiftItems = gameState.player.items.filter {
        it.category == "gift" || it.id in listOf("drahy_obojek", "serum_poslusnost", "elixir_touhy", "hojivy_balzam")
    }

    // Direct gifts available for purchase with gold
    val directGifts = GameContent.DIRECT_GIFTS

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF191224)),
            border = BorderStroke(1.5.dp, Color(0xFFAB47BC)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🎁", fontSize = 20.sp)
                            Text(
                                text = "Obdarovat: ${character.name}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "${affinityTier.icon} ${affinityTier.title} • Vztah úr. ${affinityTier.level}",
                            fontSize = 11.sp,
                            color = Color(affinityTier.colorHex)
                        )
                    }

                    // Gold indicator
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2B2011),
                        border = BorderStroke(1.dp, Color(0xFFFFD700))
                    ) {
                        Text(
                            text = "🪙 ${gameState.player.gold}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Loyalty & Combat Performance Status Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF251633),
                    border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(loyaltyBonus.icon, fontSize = 16.sp)
                                Text(
                                    text = "Bojová úroveň: ${loyaltyBonus.title}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                            Text(
                                text = "🛡️ ${character.loajalita}/100 loajalita",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFFF80AB)
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (character.loajalita / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFFFF4081),
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )

                        Text(
                            text = "⚔️ Efekt v bitvě: ${loyaltyBonus.summaryText}",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "💡 Vyšší náklonnost a loajalita z darů přímo zvyšují útok, obranu a asistenční zásahy v boji!",
                            fontSize = 9.sp,
                            color = Color(0xFFCE93D8)
                        )
                    }
                }

                // Daily Quota indicator
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (character.canGiftToday) Color(0xFF1E281E) else Color(0xFF331616),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (character.canGiftToday) "Dnes zbývá: ${character.dailyGiftsRemaining} darů" else "Dnešní limit darů vyčerpán",
                            fontSize = 10.sp,
                            color = if (character.canGiftToday) Color(0xFF81C784) else Color(0xFFE57373)
                        )
                        Text(
                            text = "${character.dailyGiftsCount}/${character.maxDailyGifts}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                // Gift Selection List
                Text(
                    text = "Vyber dar k předání:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Inventory gifts section
                    if (inventoryGiftItems.isNotEmpty()) {
                        item {
                            Text(
                                text = "🎒 V tvém inventáři:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF81C784)
                            )
                        }
                        items(inventoryGiftItems) { item ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF221A30),
                                border = BorderStroke(1.dp, Color(0xFF4A355A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(item.icon, fontSize = 20.sp)
                                        Column {
                                            Text(
                                                text = "${item.name} (${item.count}×)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "+15 Náklonnost • +12 Loajalita",
                                                fontSize = 10.sp,
                                                color = Color(0xFFFF80AB)
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            engine.giveInventoryGift(character.id, item.id)
                                            onDismiss()
                                        },
                                        enabled = character.canGiftToday,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60))
                                    ) {
                                        Text("Darovat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Direct Gifts section (Purchase with gold)
                    item {
                        Text(
                            text = "🛍️ Zlaté dary z pokladnice:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }

                    items(directGifts) { gift ->
                        val canAfford = gameState.player.gold >= gift.goldCost
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF20182B),
                            border = BorderStroke(1.dp, if (canAfford) Color(0xFF4A355A) else Color.DarkGray.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(gift.icon, fontSize = 20.sp)
                                    Column {
                                        Text(
                                            text = gift.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "+${gift.loyaltyBoost} Loajalita • +${gift.romanceBoost} Srdce",
                                            fontSize = 10.sp,
                                            color = Color(0xFFFF80AB)
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        engine.giveDirectGift(gift.id, character.id)
                                        onDismiss()
                                    },
                                    enabled = character.canGiftToday && canAfford,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (canAfford) Color(0xFF8E24AA) else Color.DarkGray
                                    )
                                ) {
                                    Text("${gift.goldCost} 🪙", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Footer button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.8f))
                ) {
                    Text("Zavřít", fontSize = 12.sp)
                }
            }
        }
    }
}

