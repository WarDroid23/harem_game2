package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.haremdark.data.DomainData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

/**
 * Interactive World Map Weather Overlay displaying current atmospheric conditions,
 * elemental bonuses, and resource yield forecasts for planning expeditions.
 */
@Composable
fun WorldMapWeatherOverlay(
    gameState: GameSave,
    engine: GameEngine,
    onNavigateToExpeditions: ((String?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedZoneForModal by remember { mutableStateOf<Pair<IdleExpeditionZone, ZoneWeatherCondition>?>(null) }
    var showOverlayExpanded by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(0) } // 0: Expediční zóny, 1: Regiony dominia

    // Periodic timer to keep weather countdown live
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val timeLeft = ZoneWeatherSystem.getTimeUntilWeatherChange(currentTimeMillis)
    val formattedTimeLeft = ZoneWeatherSystem.formatRemainingTime(timeLeft)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- TOP WEATHER RADAR BANNER ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0C2E).copy(alpha = 0.95f)),
            border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFAB47BC), Color(0xFF00E5FF)))),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showOverlayExpanded = !showOverlayExpanded
                    HapticManager.vibrateClick()
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pulsing Radar Icon
                    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.9f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse"
                    )

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF7C4DFF).copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, Color(0xFFFFD700)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "🌦️",
                                fontSize = 20.sp,
                                modifier = Modifier
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Meteorologický radar zón",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFD700)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF4A148C)
                            ) {
                                Text(
                                    text = "LIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF69F0AE),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Změna klimatu za: $formattedTimeLeft ⏱️",
                            fontSize = 11.sp,
                            color = Color(0xFFE1BEE7)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (showOverlayExpanded) "Skrýt radar" else "Otevřít radar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF80D8FF)
                    )
                    Icon(
                        imageVector = if (showOverlayExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF80D8FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // --- EXPANDABLE WEATHER OVERLAY CAROUSEL / GRID ---
        AnimatedVisibility(
            visible = showOverlayExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF130722).copy(alpha = 0.98f)),
                border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category Tab Switcher
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = Color(0xFF1F0C38),
                        contentColor = Color(0xFFFFD700),
                        indicator = { tabPositions ->
                            if (activeTab < tabPositions.size) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp),
                                    color = Color(0xFFFFD700)
                                ) {}
                            }
                        }
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0; HapticManager.vibrateClick() },
                            text = { Text("🗺️ Expediční zóny (5)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1; HapticManager.vibrateClick() },
                            text = { Text("🏰 Teritoria dominia", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    if (activeTab == 0) {
                        // Expedition Zones Weather List
                        Text(
                            text = "Naplánuj výpravy podle počasí pro maximální zisk surovin a XP:",
                            fontSize = 11.sp,
                            color = Color(0xFFB39DDB)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(IdleExpeditionCatalog.ZONES) { zone ->
                                val weather = ZoneWeatherSystem.getWeatherForZone(zone.id, currentTimeMillis)
                                val efficiency = ZoneWeatherSystem.calculateExpeditionEfficiency(zone, weather, gameState.characters)

                                ExpeditionZoneWeatherCard(
                                    zone = zone,
                                    weather = weather,
                                    efficiency = efficiency,
                                    onViewIntel = {
                                        selectedZoneForModal = Pair(zone, weather)
                                        SoundEffectManager.playNavigation(com.example.haremdark.domain.NavSound.MENU_CLICK)
                                        HapticManager.vibrateClick()
                                    },
                                    onLaunchExpedition = {
                                        onNavigateToExpeditions?.invoke(zone.id)
                                    }
                                )
                            }
                        }
                    } else {
                        // World Domain Regions Weather List
                        Text(
                            text = "Aktuální atmosférické anomálie v provinciích dominia:",
                            fontSize = 11.sp,
                            color = Color(0xFFB39DDB)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(DomainData.DOMAINS) { domain ->
                                val weather = ZoneWeatherSystem.getWeatherForDomain(domain.id, currentTimeMillis)

                                DomainRegionWeatherCard(
                                    domain = domain,
                                    weather = weather
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- TACTICAL WEATHER INTEL & FORECAST MODAL ---
    selectedZoneForModal?.let { (zone, weather) ->
        ZoneWeatherIntelModal(
            zone = zone,
            weather = weather,
            gameState = gameState,
            engine = engine,
            onDismiss = { selectedZoneForModal = null },
            onLaunchExpedition = {
                selectedZoneForModal = null
                onNavigateToExpeditions?.invoke(zone.id)
            }
        )
    }
}

@Composable
fun ExpeditionZoneWeatherCard(
    zone: IdleExpeditionZone,
    weather: ZoneWeatherCondition,
    efficiency: ExpeditionEfficiencyResult,
    onViewIntel: () -> Unit,
    onLaunchExpedition: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F0D36)),
        border = BorderStroke(1.5.dp, Color(weather.primaryColorHex)),
        modifier = modifier
            .width(220.dp)
            .clickable { onViewIntel() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Zone Icon, Name & Danger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(zone.icon, fontSize = 20.sp)
                    Text(
                        text = zone.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 120.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFD50000).copy(alpha = 0.25f)
                ) {
                    Text(
                        text = "💀 ${zone.dangerLevel}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF5252),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            // Weather Pill Banner
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(weather.primaryColorHex).copy(alpha = 0.25f),
                border = BorderStroke(1.dp, Color(weather.secondaryColorHex).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(weather.icon, fontSize = 16.sp)
                    Column {
                        Text(
                            text = weather.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(weather.secondaryColorHex)
                        )
                        Text(
                            text = weather.dominantElement,
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Yield Multiplier Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (weather.goldMultiplier > 1.0f) {
                    YieldBadge("💰 +${((weather.goldMultiplier - 1.0f) * 100).toInt()}%", Color(0xFFFFD700))
                }
                if (weather.darkEnergyMultiplier > 1.0f) {
                    YieldBadge("🔮 +${((weather.darkEnergyMultiplier - 1.0f) * 100).toInt()}%", Color(0xFFE040FB))
                }
                if (weather.materialsBonusPercent > 0) {
                    YieldBadge("💎 +${weather.materialsBonusPercent}%", Color(0xFF80D8FF))
                }
            }

            // Efficiency Tier
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Efektivita:",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
                Text(
                    text = efficiency.efficiencyTier,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = when {
                        efficiency.efficiencyScore >= 140 -> Color(0xFF69F0AE)
                        efficiency.efficiencyScore >= 115 -> Color(0xFFFFD700)
                        else -> Color(0xFFFF8A80)
                    }
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onViewIntel,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF80D8FF)),
                    border = BorderStroke(1.dp, Color(0xFF80D8FF).copy(alpha = 0.5f))
                ) {
                    Text("🔍 Intel", fontSize = 10.sp)
                }

                Button(
                    onClick = onLaunchExpedition,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                ) {
                    Text("🚀 Vyslat", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DomainRegionWeatherCard(
    domain: DomainLocation,
    weather: ZoneWeatherCondition,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0B2E)),
        border = BorderStroke(1.dp, Color(weather.primaryColorHex).copy(alpha = 0.6f)),
        modifier = modifier.width(200.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = domain.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(weather.icon, fontSize = 18.sp)
            }

            Text(
                text = "Klima: ${weather.name}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(weather.secondaryColorHex)
            )

            Text(
                text = weather.description,
                fontSize = 10.sp,
                color = Color.LightGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0x33000000),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚡ ${weather.combatStatBuff}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F),
                    modifier = Modifier.padding(6.dp)
                )
            }
        }
    }
}

@Composable
private fun YieldBadge(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.2f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.6f))
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

/**
 * Detailed tactical weather analysis modal showing forecast, catalysts,
 * and direct squad recommendations.
 */
@Composable
fun ZoneWeatherIntelModal(
    zone: IdleExpeditionZone,
    weather: ZoneWeatherCondition,
    gameState: GameSave,
    engine: GameEngine,
    onDismiss: () -> Unit,
    onLaunchExpedition: () -> Unit
) {
    val efficiency = remember(zone, weather, gameState.characters) {
        ZoneWeatherSystem.calculateExpeditionEfficiency(zone, weather, gameState.characters)
    }
    val forecasts = remember(zone) {
        ZoneWeatherSystem.getForecastForZone(zone.id, 3)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF130623)),
            border = BorderStroke(2.dp, Brush.linearGradient(listOf(Color(weather.primaryColorHex), Color(weather.secondaryColorHex)))),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Background Atmospheric Particle Canvas
                ZoneAtmosphericParticles(particleType = weather.particleType)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
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
                                color = Color(weather.primaryColorHex).copy(alpha = 0.3f),
                                border = BorderStroke(1.5.dp, Color(weather.secondaryColorHex)),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(weather.icon, fontSize = 24.sp)
                                }
                            }

                            Column {
                                Text(
                                    text = weather.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(weather.secondaryColorHex)
                                )
                                Text(
                                    text = "Zóna: ${zone.name}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE1BEE7)
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White)
                        }
                    }

                    // Weather Flavor & Elemental Lore
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF24103B).copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, Color(weather.primaryColorHex).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "🌀 Atmosférický katalyzátor:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                            Text(
                                text = weather.description,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 16.sp
                            )
                            Text(
                                text = "⚔️ Efekt v boji: ${weather.combatStatBuff}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF80D8FF)
                            )
                        }
                    }

                    // Resource Multipliers & Forecasted Loot
                    Text(
                        text = "📊 Očekávaný výnos expedice:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ResourceYieldCard("💰 Zlato", "+${efficiency.goldYield}", Color(0xFFFFD700), Modifier.weight(1f))
                        ResourceYieldCard("🔮 Temná energie", "+${efficiency.darkEnergyYield}", Color(0xFFE040FB), Modifier.weight(1f))
                        ResourceYieldCard("⭐ Index", efficiency.efficiencyTier, Color(0xFF69F0AE), Modifier.weight(1.2f))
                    }

                    // Boosted Crafting Materials Badge
                    if (weather.boostedMaterials.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E0A30),
                            border = BorderStroke(1.dp, Color(0xFF64B5F6).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("💎", fontSize = 16.sp)
                                Text(
                                    text = "Zvýšený výskyt: ${weather.boostedMaterials.joinToString(", ")} (+${weather.materialsBonusPercent}%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF81D4FA)
                                )
                            }
                        }
                    }

                    // Upcoming Forecast Timeline (Next 3 cycles)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "⏳ Předpověď počasí (Další cykly):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )

                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            forecasts.forEachIndexed { idx, (futureTime, nextWeather) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF26123D),
                                    border = BorderStroke(1.dp, Color(nextWeather.primaryColorHex).copy(alpha = 0.4f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(timeFormat.format(Date(futureTime)), fontSize = 9.sp, color = Color.Gray)
                                        Text(nextWeather.icon, fontSize = 16.sp)
                                        Text(nextWeather.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Zavřít", color = Color.Gray)
                        }

                        Button(
                            onClick = onLaunchExpedition,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Vyslat výpravu", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourceYieldCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF220D38),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = Color.LightGray)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

/**
 * Animated Ambient Particle Canvas rendering light atmospheric weather effects.
 */
@Composable
fun ZoneAtmosphericParticles(
    particleType: String,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AtmosphereAnim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val count = 25
        val width = size.width
        val height = size.height

        for (i in 0 until count) {
            val randomX = ((i * 37 + 13) % 100) / 100f * width
            val speed = 0.5f + ((i * 17) % 10) / 10f
            val currentY = ((phase * speed + (i * 0.1f)) % 1f) * height

            when (particleType) {
                "lightning", "astral_sparks" -> {
                    drawCircle(
                        color = Color(0xFFE040FB).copy(alpha = 0.35f + (phase * 0.3f)),
                        radius = 3.dp.toPx(),
                        center = Offset(randomX, currentY)
                    )
                }
                "blood_rain" -> {
                    drawLine(
                        color = Color(0xFFFF1744).copy(alpha = 0.4f),
                        start = Offset(randomX, currentY),
                        end = Offset(randomX - 4.dp.toPx(), currentY + 12.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                "snow" -> {
                    drawCircle(
                        color = Color(0xFF80D8FF).copy(alpha = 0.45f),
                        radius = 2.5.dp.toPx(),
                        center = Offset(randomX, currentY)
                    )
                }
                "fog" -> {
                    drawCircle(
                        color = Color(0xFFB0BEC5).copy(alpha = 0.15f),
                        radius = 18.dp.toPx(),
                        center = Offset(randomX, currentY)
                    )
                }
                "sunbeams", "moon_glow" -> {
                    drawCircle(
                        color = Color(0xFFFFD54F).copy(alpha = 0.25f),
                        radius = 4.dp.toPx(),
                        center = Offset(randomX, currentY)
                    )
                }
            }
        }
    }
}
