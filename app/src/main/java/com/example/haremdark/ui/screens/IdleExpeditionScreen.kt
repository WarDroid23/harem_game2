package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.domain.HaremSound
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave
import com.example.haremdark.models.IdleExpeditionCatalog
import com.example.haremdark.models.IdleExpeditionReport
import com.example.haremdark.models.IdleExpeditionZone
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdleExpeditionScreen(
    gameState: GameSave,
    engine: GameEngine,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeExpeditions = gameState.activeIdleExpeditions
    val reports = gameState.completedExpeditionReports
    val characters = gameState.characters

    var selectedZoneForDispatch by remember { mutableStateOf<IdleExpeditionZone?>(null) }
    var selectedCharacterIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var activeReportClaimModal by remember { mutableStateOf<IdleExpeditionReport?>(reports.firstOrNull()) }
    var selectedZoneForWeatherIntel by remember { mutableStateOf<Pair<IdleExpeditionZone, com.example.haremdark.models.ZoneWeatherCondition>?>(null) }

    // Ticking timer state for live countdowns
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
            // Auto check completions
            engine.checkAndCompleteIdleExpeditions()
        }
    }

    val materialNames = mapOf(
        "temny_strep" to "💎 Temné střepy",
        "mana_esence" to "✨ Mana esence",
        "zelezna_ruda" to "⚙️ Železná ruda",
        "drevohorec" to "🪵 Dřevohorec",
        "mesicni_prach" to "🌙 Měsíční prach",
        "draci_krev" to "🩸 Dračí krev",
        "krystal" to "💎 Krystaly"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "🗺️ Idle Expedice Harému",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Vysílej dívky na výpravy pro suroviny i při vypnuté hře",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF160A24)
                )
            )
        },
        containerColor = Color(0xFF0F0618)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- ACTIVE EXPEDITIONS BAR ---
            if (activeExpeditions.isNotEmpty() || reports.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1E0E32),
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "⏳ Probíhající & Dokončené Výpravy:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )

                        // Completed Reports Banner
                        reports.forEach { report ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { activeReportClaimModal = report },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)),
                                border = BorderStroke(1.dp, Color(0xFFFFD700))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(report.zoneIcon, fontSize = 20.sp)
                                        Column {
                                            Text("🏆 ${report.zoneName} (Dokončeno!)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                            Text("Přinesly: +${report.goldGained} Zlata, +${report.darkEnergyGained} TE", fontSize = 11.sp, color = Color(0xFFFFD54F))
                                        }
                                    }
                                    Button(
                                        onClick = { activeReportClaimModal = report },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                                    ) {
                                        Text("Vyzvednout", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Active Expeditions Countdown List
                        activeExpeditions.forEach { active ->
                            val zone = IdleExpeditionCatalog.getZoneById(active.zoneId)
                            val remainingMillis = (active.startTimeMillis + active.durationMillis - currentTimeMillis).coerceAtLeast(0L)
                            val remainingSec = remainingMillis / 1000
                            val hours = remainingSec / 3600
                            val minutes = (remainingSec % 3600) / 60
                            val seconds = remainingSec % 60
                            val timeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                            val progress = ((currentTimeMillis - active.startTimeMillis).toFloat() / active.durationMillis.toFloat()).coerceIn(0f, 1f)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF25103A)),
                                border = BorderStroke(1.dp, Color(0xFF6A1B9A))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(zone?.icon ?: "🗺️", fontSize = 18.sp)
                                            Text(zone?.name ?: "Expedice", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        }
                                        Text("⏳ $timeStr", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81D4FA))
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = Color(0xFFAB47BC),
                                        trackColor = Color(0xFF381E52)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- ZONES CATALOG LIST ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "📍 Nebezpečné Zóny Dominia:",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        fontSize = 14.sp
                    )
                }

                items(IdleExpeditionCatalog.ZONES) { zone ->
                    val isCurrentlyExploring = activeExpeditions.any { it.zoneId == zone.id }
                    val weather = com.example.haremdark.models.ZoneWeatherSystem.getWeatherForZone(zone.id, currentTimeMillis)
                    val estGold = (zone.baseGoldReward * weather.goldMultiplier).toInt()
                    val estDark = (zone.baseDarkEnergyReward * weather.darkEnergyMultiplier).toInt()

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0E32)),
                        border = BorderStroke(1.dp, if (isCurrentlyExploring) Color(0xFFAB47BC) else Color(weather.primaryColorHex).copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF2A153E),
                                    border = BorderStroke(1.dp, Color(0xFF6A1B9A)),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(zone.icon, fontSize = 24.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(zone.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Obtížnost: ${"⭐".repeat(zone.dangerLevel)}", fontSize = 11.sp, color = Color(0xFFFFC107))
                                        Text("• ${zone.durationMinutes} minut", fontSize = 11.sp, color = Color.LightGray)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(zone.description, fontSize = 12.sp, color = Color.LightGray)

                            Spacer(modifier = Modifier.height(8.dp))

                            // Weather Pill Banner with Intel clickable
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(weather.primaryColorHex).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(weather.secondaryColorHex).copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedZoneForWeatherIntel = Pair(zone, weather)
                                        HapticManager.vibrateClick()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(weather.icon, fontSize = 16.sp)
                                        Text(
                                            text = "${weather.name} (${weather.dominantElement})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(weather.secondaryColorHex)
                                        )
                                    }

                                    Text(
                                        text = "🔍 Rozbor počasí",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF80D8FF)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Drops preview with active weather multipliers
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF140822),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🎁 Očekávaná kořist:", fontSize = 11.sp, color = Color.Gray)
                                        Text("💰 +$estGold Zlata, 🔮 +$estDark TE", fontSize = 11.sp, color = Color(0xFF81D4FA), fontWeight = FontWeight.SemiBold)
                                    }
                                    if (weather.boostedMaterials.isNotEmpty()) {
                                        Text(
                                            text = "💎 Bonus surovin: +${weather.materialsBonusPercent}% (${weather.boostedMaterials.joinToString(", ")})",
                                            fontSize = 10.sp,
                                            color = Color(0xFFFFD54F)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    selectedZoneForDispatch = zone
                                    selectedCharacterIds = emptySet()
                                    HapticManager.vibrateClick()
                                },
                                enabled = !isCurrentlyExploring,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isCurrentlyExploring) "⚙️ Výprava probíhá..." else "🚀 Vyslat výpravu (${zone.requiredMembersCount} dívky)", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- MODAL: SELECT SQUAD FOR EXPEDITION ---
    selectedZoneForDispatch?.let { zone ->
        AlertDialog(
            onDismissRequest = { selectedZoneForDispatch = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(zone.icon, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vyslat výpravu do: ${zone.name}", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Vyber ${zone.requiredMembersCount} dívky, které se vydají na výpravu trvající ${zone.durationMinutes} minut. Suroviny obdržíš i po zavření aplikace.",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )

                    val availableCharacters = characters.filter { !it.status.contains("expedici") }

                    if (availableCharacters.isEmpty()) {
                        Text("Všechny tvoje dívky jsou již na výpravách!", color = Color(0xFFE57373), fontSize = 12.sp)
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(220.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(availableCharacters) { char ->
                                val isSelected = selectedCharacterIds.contains(char.id)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val newSet = selectedCharacterIds.toMutableSet()
                                            if (isSelected) {
                                                newSet.remove(char.id)
                                            } else {
                                                if (newSet.size < zone.requiredMembersCount) {
                                                    newSet.add(char.id)
                                                }
                                            }
                                            selectedCharacterIds = newSet
                                            HapticManager.vibrateClick()
                                        },
                                    colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF4A148C) else Color(0xFF231038)),
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFFFD700) else Color(0xFF381E52))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("💃", fontSize = 20.sp)
                                            Column {
                                                Text(char.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                                Text("Úroveň: ${char.level}", fontSize = 11.sp, color = Color.Gray)
                                            }
                                        }
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = null,
                                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFFD700))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val (success, msg) = engine.startIdleExpedition(zone.id, selectedCharacterIds.toList())
                        if (success) {
                            SoundEffectManager.playHarem(HaremSound.CRAFTING_SUCCESS)
                            HapticManager.vibrateClick()
                            selectedZoneForDispatch = null
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    enabled = selectedCharacterIds.size >= zone.requiredMembersCount,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
                ) {
                    Text("Zahájit výpravu (${selectedCharacterIds.size}/${zone.requiredMembersCount})")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedZoneForDispatch = null }) {
                    Text("Zrušit", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1B0B2E)
        )
    }

    // --- MODAL: CLAIM COMPLETED REPORT ---
    activeReportClaimModal?.let { report ->
        AlertDialog(
            onDismissRequest = { activeReportClaimModal = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(report.zoneIcon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Výprava Dokončena!", color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(report.flavorLog, fontSize = 12.sp, color = Color.LightGray)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF231038),
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🎁 Získaná kořist:", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700), fontSize = 12.sp)
                            Text("💰 Zlato: +${report.goldGained}", fontSize = 12.sp, color = Color.White)
                            Text("🔮 Temná energie: +${report.darkEnergyGained}", fontSize = 12.sp, color = Color.White)
                            Text("✨ Zkušenosti dívek: +${report.xpGainedPerMember} XP", fontSize = 12.sp, color = Color(0xFF81C784))

                            if (report.materialsGained.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("⚒️ Nalezené suroviny:", fontWeight = FontWeight.Bold, color = Color(0xFF81D4FA), fontSize = 12.sp)
                                report.materialsGained.forEach { (matId, count) ->
                                    val name = materialNames[matId] ?: matId
                                    Text("• $name: +$count", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        engine.dismissExpeditionReport(report.id)
                        SoundEffectManager.playLevelUp()
                        HapticManager.vibrateHeavy()
                        activeReportClaimModal = null
                        Toast.makeText(context, "Kořist byla přidána do skladu!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("Vyzvednout kořist", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1B0B2E)
        )
    }

    // --- MODAL: WEATHER INTEL & FORECAST ---
    selectedZoneForWeatherIntel?.let { (zone, weather) ->
        com.example.haremdark.ui.components.ZoneWeatherIntelModal(
            zone = zone,
            weather = weather,
            gameState = gameState,
            engine = engine,
            onDismiss = { selectedZoneForWeatherIntel = null },
            onLaunchExpedition = {
                selectedZoneForWeatherIntel = null
                selectedZoneForDispatch = zone
                selectedCharacterIds = emptySet()
            }
        )
    }
}
