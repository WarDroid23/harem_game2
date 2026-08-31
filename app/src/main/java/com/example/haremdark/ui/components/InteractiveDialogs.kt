package com.example.haremdark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.haremdark.data.GameContent
import com.example.haremdark.data.GameInteraction
import com.example.haremdark.data.StaticData
import com.example.haremdark.models.Concubine
import com.example.haremdark.models.Player

@Composable
fun InteractionDialog(
    concubine: Concubine,
    player: Player,
    onDismiss: () -> Unit,
    onExecuteInteraction: (GameInteraction) -> Unit,
    onCourtRomance: () -> Unit,
    onMarry: () -> Unit,
    onRent: (String, Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Odměny", "Tresty", "Intimita", "Vztahy", "Nájem")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Péče o ${concubine.name}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Fáze ${concubine.fazeZkazenosti} • Loajalita ${concubine.loajalita}% • Touha ${concubine.touha}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít")
                    }
                }

                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) }
                        )
                    }
                }

                // Content based on tab
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> InteractionList(GameContent.REWARDS, player, concubine, onExecuteInteraction)
                        1 -> InteractionList(GameContent.PUNISHMENTS, player, concubine, onExecuteInteraction)
                        2 -> InteractionList(GameContent.INTIMATE, player, concubine, onExecuteInteraction)
                        3 -> RelationshipsTab(concubine, player, onCourtRomance, onMarry)
                        4 -> RentalTab(concubine, onRent)
                    }
                }
            }
        }
    }
}

@Composable
fun InteractionList(
    interactions: List<GameInteraction>,
    player: Player,
    concubine: Concubine,
    onExecute: (GameInteraction) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(interactions) { interaction ->
            val canAffordEnergy = player.sexEnergy >= interaction.energyCost
            val canAffordDark = player.darkEnergy >= interaction.darkCost
            val canAffordGold = player.gold >= interaction.goldCost
            val phaseOk = concubine.fazeZkazenosti >= interaction.minPhase
            val favOk = !interaction.requiresFavorite || concubine.oblibena
            val wifeOk = !interaction.requiresWife || concubine.jeManzelkou
            val enabled = canAffordEnergy && canAffordDark && canAffordGold && phaseOk && favOk && wifeOk

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = interaction.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (interaction.energyCost > 0) {
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE91E63).copy(alpha = 0.2f)) {
                                    Text("⚡ ${interaction.energyCost}", fontSize = 10.sp, color = Color(0xFFE91E63), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                            if (interaction.darkCost > 0) {
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF9C27B0).copy(alpha = 0.2f)) {
                                    Text("🔮 ${interaction.darkCost}", fontSize = 10.sp, color = Color(0xFF9C27B0), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                            if (interaction.goldCost > 0) {
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFFD700).copy(alpha = 0.2f)) {
                                    Text("💰 ${interaction.goldCost}", fontSize = 10.sp, color = Color(0xFFFFD700), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }

                    Text(
                        text = interaction.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )

                    Text(
                        text = "Efekt: ${interaction.effectDescription}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (!phaseOk) {
                        Text("⚠️ Vyžaduje alespoň fázi zkázanosti ${interaction.minPhase}!", fontSize = 10.sp, color = Color(0xFFFF9800))
                    }
                    if (interaction.requiresFavorite && !concubine.oblibena) {
                        Text("★ Vyžaduje status Oblíbenkyně!", fontSize = 10.sp, color = Color(0xFFFFD700))
                    }
                    if (interaction.requiresWife && !concubine.jeManzelkou) {
                        Text("💍 Vyžaduje status Manželka!", fontSize = 10.sp, color = Color(0xFFE040FB))
                    }

                    Button(
                        onClick = { onExecute(interaction) },
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text("Provést akreditaci", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RelationshipsTab(
    concubine: Concubine,
    player: Player,
    onCourt: () -> Unit,
    onMarry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Romantické sbližování ♥", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Stav romance: ${concubine.romanceBody}/100 body",
                    color = Color(0xFFFF4081),
                    fontWeight = FontWeight.Bold
                )
                LinearProgressIndicator(
                    progress = { (concubine.romanceBody / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFFF4081)
                )
                Text(
                    "Dvořením, dary a soukromými večeřemi prohlubuješ její city. Při 50 bodech se stává Partnerkou.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Button(
                    onClick = onCourt,
                    enabled = player.gold >= 50 && concubine.romanceBody < 100,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dvořit se & obdarovat (50 zlatých)", fontSize = 12.sp)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Manželský svazek 💍", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    if (concubine.jeManzelkou) "Již je tvou oficiální Manželkou dominia!"
                    else "Vyžaduje: 80 Romance (máš ${concubine.romanceBody}) & 70 Loajalita (máš ${concubine.loajalita}%) & 300 Zlata.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                if (!concubine.jeManzelkou) {
                    Button(
                        onClick = onMarry,
                        enabled = concubine.romanceBody >= 80 && concubine.loajalita >= 70 && player.gold >= 300,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Uzavřít sňatek (300 zlatých)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RentalTab(
    concubine: Concubine,
    onRent: (String, Int) -> Unit
) {
    var selectedClient by remember { mutableStateOf("Šlechtický dvůr") }
    var selectedDays by remember { mutableIntStateOf(3) }
    val clients = listOf("Šlechtický dvůr", "Otrokářský syndikát", "Inkviziční legie", "Cech bohatých kupců")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (concubine.naNajmu) {
            Text(
                "Dívka je v současnosti pronajata klientovi '${concubine.klient}'. Zbývá ${concubine.najemZbyvaDni} dní.",
                color = Color(0xFFFFB74D),
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                "Pronajmi otrokyni vybranému klientovi na stanovený počet dní. Získáš okamžitou zálohu (45 zl./den) a denní pasivní příjem.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Text("Vyber klienta:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            clients.forEach { client ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedClient == client,
                        onClick = { selectedClient = client }
                    )
                    Text(client, fontSize = 13.sp)
                }
            }

            Text("Doba trvání: $selectedDays dní (Záloha: ${selectedDays * 45} zlatých)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Slider(
                value = selectedDays.toFloat(),
                onValueChange = { selectedDays = it.toInt() },
                valueRange = 1f..5f,
                steps = 3
            )

            Button(
                onClick = { onRent(selectedClient, selectedDays) },
                modifier = Modifier.fillMaxWidth(),
                enabled = concubine.hp >= 40
            ) {
                Text("Odeslat na nájem", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ConcubineDetailDialog(
    concubine: Concubine,
    onDismiss: () -> Unit
) {
    val loyalty = StaticData.getLoyaltyTier(concubine.loajalita)
    val archetype = StaticData.ARCHETYPES[concubine.archetypeId]
    val phase = StaticData.DEGRADATION_PHASES[concubine.fazeZkazenosti]

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(concubine.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("${archetype?.name ?: "Otrokyně"} • ${concubine.age} let", color = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít")
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Archetyp & Osobnost", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(archetype?.description ?: "Bez popisu", style = MaterialTheme.typography.bodySmall)
                        Text("Fáze ${concubine.fazeZkazenosti}: ${phase?.name}", fontWeight = FontWeight.Bold)
                        Text(phase?.description ?: "", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Loajalita a Oddanost", fontWeight = FontWeight.Bold, color = Color(loyalty.colorHex))
                        Text("${loyalty.title} (${concubine.loajalita}%)", fontWeight = FontWeight.Bold)
                        Text(loyalty.description, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Text("Kompletní statistiky:", fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatRow("Životy (HP)", "${concubine.hp}/${concubine.maxHp}")
                    StatRow("Touha", "${concubine.touha}%")
                    StatRow("Vlhkost", "${concubine.vlhkost}%")
                    StatRow("Submisivita", "${concubine.submisivita}%")
                    StatRow("Poslušnost", "${concubine.poslusnost}%")
                    StatRow("Důvěra", "${concubine.duvera}%")
                    StatRow("Strach", "${concubine.strach}%")
                    StatRow("Zlomení vůle (Broken)", "${concubine.broken}%")
                    StatRow("Mindbreak", "${concubine.mindbreak}%")
                    StatRow("Závislost na bolesti", "${concubine.painAddiction}%")
                    StatRow("Ponížení", "${concubine.humiliation}%")
                    StatRow("Jizvy & stopy", "${concubine.scarred}%")
                    StatRow("Cejch pána", if (concubine.ownedMark) "Vypálen na kůži" else "Žádný")
                    StatRow("Těhotenství", if (concubine.tehotna) "V jiném stavu (Den ${concubine.dnyTehotenstvi})" else "Ne")
                    StatRow("Potomstvo v dominiu", "${concubine.deti} dětí")
                }
            }
        }
    }
}

@Composable
fun StatRow(name: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
