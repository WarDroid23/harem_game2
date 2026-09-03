package com.example.haremdark.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.R
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.GameSave

@Composable
fun HomeScreen(
    gameState: GameSave,
    engine: GameEngine,
    onNavigateToHarem: () -> Unit,
    onNavigateToActivities: () -> Unit,
    onNavigateToEmpire: () -> Unit,
    onNavigateToProgression: () -> Unit,
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val player = gameState.player
    val favorite = gameState.characters.firstOrNull { it.oblibena }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        // Hero Image Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(18.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_dark_banner),
                    contentDescription = "Pevnost dominia",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xDD0F0B0E))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Temné Dominium Páně",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Den ${player.day} • ${gameState.characters.size} otrokyň v komnatách",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFD700)
                    )
                }
            }
        }

        // Summary card for current resources
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Přehled Zdrojů",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ResourceItem(
                            icon = Icons.Default.MonetizationOn, 
                            color = Color(0xFFFFD700),
                            label = "Zlato",
                            value = "${player.gold}",
                            progress = null
                        )
                        ResourceItem(
                            icon = Icons.Default.Bolt, 
                            color = Color(0xFFE91E63),
                            label = "Sex Energie",
                            value = "${player.sexEnergy} / ${player.maxSexEnergy}",
                            progress = player.sexEnergy.toFloat() / player.maxSexEnergy.toFloat().coerceAtLeast(1f)
                        )
                        ResourceItem(
                            icon = Icons.Default.DarkMode, 
                            color = Color(0xFF9C27B0),
                            label = "Temná Síla",
                            value = "${player.darkEnergy} / ${player.maxDarkEnergy}",
                            progress = player.darkEnergy.toFloat() / player.maxDarkEnergy.toFloat().coerceAtLeast(1f)
                        )
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ResourceItem(
                            icon = Icons.Default.Groups, 
                            color = Color(0xFF2196F3),
                            label = "Harém",
                            value = "${gameState.characters.size} Dívek",
                            progress = null
                        )
                        ResourceItem(
                            icon = Icons.Default.LocationCity, 
                            color = Color(0xFF00E5FF),
                            label = "Území",
                            value = "${gameState.territories.count { it.level > 0 }}/5 Zón",
                            progress = gameState.territories.count { it.level > 0 }.toFloat() / 5f
                        )
                    }
                }
            }
        }


        // Active Buffs
        if (gameState.activeBuffs.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "✨ Aktivní pouta a požehnání",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        gameState.activeBuffs.forEach { buff ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = buff.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = buff.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "${buff.durationDays} dny",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Favorite Character Spotlight if exists
        if (favorite != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A1C12)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "★ Vyvolená Oblíbenkyně: ${favorite.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                            Text(
                                text = "Fáze ${favorite.fazeZkazenosti} • Loajalita: ${favorite.loajalita}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // --- DAILY MISSIONS ---
        if (gameState.dailyMissions.isNotEmpty()) {
            item {
                Text(
                    text = "Denní úkoly",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                )
            }
            items(gameState.dailyMissions, key = { it.id }) { mission ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(mission.description, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${mission.currentProgress} / ${mission.targetCount}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        
                        LinearProgressIndicator(
                            progress = { (mission.currentProgress.toFloat() / mission.targetCount.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            drawStopIndicator = {}
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (mission.rewardGold > 0) {
                                    Text("💰 ${mission.rewardGold}", fontSize = 12.sp, color = Color(0xFFFFD700))
                                }
                                if (mission.rewardDarkEnergy > 0) {
                                    Text("🌙 ${mission.rewardDarkEnergy}", fontSize = 12.sp, color = Color(0xFF9C27B0))
                                }
                                if (mission.rewardSexEnergy > 0) {
                                    Text("⚡ ${mission.rewardSexEnergy}", fontSize = 12.sp, color = Color(0xFFE91E63))
                                }
                            }
                            
                            if (mission.isCompleted) {
                                if (mission.isClaimed) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Hotovo", tint = Color.Green, modifier = Modifier.size(24.dp))
                                } else {
                                    Button(
                                        onClick = { 
                                            val (success, msg) = engine.claimMissionReward(mission.id)
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        modifier = Modifier.height(30.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black)
                                    ) {
                                        Text("Vyzvednout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Grid view for quick access to core game features
        item {
            Text(
                text = "Rychlý Přístup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        item {
            val gridItems = listOf(
                GridActionItem("Harém", "Správa dívek", Icons.Default.Favorite, onNavigateToHarem),
                GridActionItem("Lov", "Zajat nové", Icons.Default.TrackChanges, onNavigateToActivities),
                GridActionItem("Pevnost", "Příjem a mafie", Icons.Default.AccountBalance, onNavigateToEmpire),
                GridActionItem("Mapa", "Cestování", Icons.Default.Map, onNavigateToMap),
                GridActionItem("Pán", "Trénink výdrže", Icons.Default.Bolt, onNavigateToProgression)
            )

            // Using standard LazyVerticalGrid but we can't nest it in LazyColumn unless bounded height.
            // So we use a custom fixed height based on item count, or we can just build columns/rows.
            // Since it's fixed 5 items, building it with Columns and Rows is safer and cleaner in LazyColumn.
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionCard(gridItems[0], Modifier.weight(1f))
                    ActionCard(gridItems[1], Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionCard(gridItems[2], Modifier.weight(1f))
                    ActionCard(gridItems[3], Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionCard(gridItems[4], Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Activity Log
        item {
            Text(
                text = "Kronika dominia (Záznamy)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(gameState.gameLog.take(8)) { log ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HistoryEdu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = log,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
fun ResourceItem(icon: ImageVector, color: Color, label: String, value: String, progress: Float? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(90.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        if (progress != null) {
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f),
                drawStopIndicator = {}
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

data class GridActionItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun ActionCard(
    item: GridActionItem,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = item.onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(verticalArrangement = Arrangement.Center) {
                Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = item.subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}
