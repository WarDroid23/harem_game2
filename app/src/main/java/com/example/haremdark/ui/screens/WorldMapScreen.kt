package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.haremdark.R
import com.example.haremdark.data.DomainData
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.CharacterReward
import com.example.haremdark.models.DomainLocation
import com.example.haremdark.models.GameSave

@Composable
fun WorldMapScreen(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentDomain = DomainData.getDomainById(gameState.currentDomainId)
    var selectedDomainId by remember { mutableStateOf(gameState.currentDomainId) }
    val selectedDomain = DomainData.getDomainById(selectedDomainId)

    val player = gameState.player

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 95.dp)
    ) {
        // Hero Map Header Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_fantasy_map),
                        contentDescription = "Mapa svatyní a dominií",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x33000000),
                                        Color(0xAA12081C),
                                        Color(0xEE12081C)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Mapa Temných Dominií",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Aktuální základna: ${currentDomain.name} • ${currentDomain.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Domain Selection Horizontal Carousel
        item {
            Text(
                text = "Zvolit území k průzkumu:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(DomainData.DOMAINS) { domain ->
                    val isUnlocked = gameState.unlockedDomains.contains(domain.id) || player.level >= domain.minPlayerLevel
                    val isCurrent = gameState.currentDomainId == domain.id
                    val isSelected = selectedDomainId == domain.id

                    DomainChipCard(
                        domain = domain,
                        isSelected = isSelected,
                        isCurrent = isCurrent,
                        isUnlocked = isUnlocked,
                        onClick = { selectedDomainId = domain.id }
                    )
                }
            }
        }

        // Selected Domain Detailed Showcase
        item {
            val isUnlocked = gameState.unlockedDomains.contains(selectedDomain.id) || player.level >= selectedDomain.minPlayerLevel
            val isCurrent = gameState.currentDomainId == selectedDomain.id

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Domain Header & Difficulty Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedDomain.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = selectedDomain.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                            )
                            Text(
                                text = "📍 ${selectedDomain.region}",
                                fontSize = 11.sp,
                                color = Color(0xFFFFD700)
                            )
                        }

                        // Difficulty Pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when (selectedDomain.difficultyStars) {
                                1 -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                2 -> Color(0xFF2196F3).copy(alpha = 0.2f)
                                3 -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                4 -> Color(0xFFE91E63).copy(alpha = 0.2f)
                                else -> Color(0xFF9C27B0).copy(alpha = 0.2f)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when (selectedDomain.difficultyStars) {
                                    1 -> Color(0xFF4CAF50)
                                    2 -> Color(0xFF2196F3)
                                    3 -> Color(0xFFFF9800)
                                    4 -> Color(0xFFE91E63)
                                    else -> Color(0xFF9C27B0)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "★".repeat(selectedDomain.difficultyStars),
                                    color = Color(0xFFFFD700),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = selectedDomain.difficulty,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Domain Description
                    Text(
                        text = selectedDomain.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Potential Character Rewards Section
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Potenciální odměny & dívky v této oblasti:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Character Reward Cards
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedDomain.potentialRewards.forEach { reward ->
                            CharacterRewardItem(reward = reward)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // NPC Display
                    if (selectedDomain.npcTrader && selectedDomain.npcName != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF3E2723),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_sexy_trader),
                                    contentDescription = "Obchodnice",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Color(0xFFFFD700), CircleShape)
                                )
                                Column {
                                    Text(
                                        text = "Tajemná obchodnice: ${selectedDomain.npcName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFFFD700)
                                    )
                                    Text(
                                        text = "Nabízí vzácné předměty a úkoly výměnou za zlaťáky.",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    // Resource Drops
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "📦 Naleziště surovin a relikvií:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = selectedDomain.resourceDrops.joinToString(" • "),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Travel & Expedition Actions
                    if (!isUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Text(
                                    text = "Zamčeno: Vyžaduje úroveň pána ${selectedDomain.minPlayerLevel} (Tvá úroveň: ${player.level})",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isCurrent) {
                                Button(
                                    onClick = {
                                        val res = engine.travelToDomain(selectedDomain.id)
                                        Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.DirectionsWalk, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Cestovat sem (${selectedDomain.travelCostEnergy} SE)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Zde sídlíš", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    val res = engine.exploreDomain(selectedDomain.id)
                                    Toast.makeText(context, res.second, Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                            ) {
                                Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Výprava & Lov (${15 + selectedDomain.difficultyStars * 2} SE)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DomainChipCard(
    domain: DomainLocation,
    isSelected: Boolean,
    isCurrent: Boolean,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else if (isCurrent) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700))
        else null,
        modifier = Modifier.width(135.dp)
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
                Text(
                    text = "★".repeat(domain.difficultyStars),
                    color = Color(0xFFFFD700),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!isUnlocked) {
                    Icon(Icons.Default.Lock, contentDescription = "Zamčeno", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.error)
                } else if (isCurrent) {
                    Text("📍", fontSize = 11.sp)
                }
            }

            Text(
                text = domain.name,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = domain.difficulty,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CharacterRewardItem(reward: CharacterReward) {
    val portraitRes = StaticData.getPortraitForArchetype(reward.archetypeId)
    val rarityColor = when (reward.rarity) {
        "Legendární" -> Color(0xFFFFD700)
        "Epická" -> Color(0xFFE040FB)
        "Vzácná" -> Color(0xFF29B6F6)
        else -> Color(0xFF81C784)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, rarityColor, CircleShape)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(portraitRes)
                        .crossfade(true)
                        .build(),
                    contentDescription = reward.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Fallback",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = reward.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = rarityColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = reward.rarity,
                            color = rarityColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = reward.traitDescription,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "${reward.dropRatePercent}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700)
            )
        }
    }
}
