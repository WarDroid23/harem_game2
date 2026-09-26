package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave
import com.example.haremdark.ui.components.CharacterDiscoveryDialog
import com.example.haremdark.ui.components.CharacterStatProgressionVicoChart
import com.example.haremdark.ui.components.RecruitModalDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HaremManagementScreen(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showRecruitDialog by remember { mutableStateOf(false) }
    var discoveredCharacter by remember { mutableStateOf<Character?>(null) }
    var selectedCharacterForDetail by remember { mutableStateOf<Character?>(null) }

    val characters = gameState.characters
    val player = gameState.player

    val filteredCharacters = remember(characters, searchQuery) {
        if (searchQuery.isBlank()) characters
        else characters.filter { it.name.contains(searchQuery, ignoreCase = true) || it.role.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "👑 Správa Harému",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "${characters.size} / ${player.maxPopulation} dívek • 💰 ${player.gold} zlata • 💎 ${player.darkEnergy} gemů",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showRecruitDialog = true },
                        modifier = Modifier
                            .testTag("btn_recruit_harem")
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = "Nábor",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("harem_management_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Resource & Currency Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💰 Zlato: ${player.gold}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Text(
                        text = "💎 Gemy (Temná E.): ${player.darkEnergy}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAB47BC)
                    )
                    Text(
                        text = "🧪 Mana: ${player.manaEssence}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E676)
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Hledat dívku podle jména či role...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Vymazat")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_harem")
            )

            // Character List View
            if (filteredCharacters.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📭", fontSize = 42.sp)
                        Text(
                            text = if (searchQuery.isBlank()) "Zatím žádné dívky v harému." else "Žádná dívka neodpovídá hledání.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Button(
                            onClick = { showRecruitDialog = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Provést nábor nové dívky")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(filteredCharacters, key = { it.id }) { character ->
                        HaremManagementItemCard(
                            character = character,
                            currentDay = gameState.player.day,
                            onInteract = {
                                engine.executeBondingInteraction(character.id, "bonding_chat")
                                Toast.makeText(context, "Pozdravena ${character.name} (+náklonnost)", Toast.LENGTH_SHORT).show()
                            },
                            onTrain = {
                                engine.executeBondingInteraction(character.id, "bonding_gift")
                                Toast.makeText(context, "Trénink ${character.name} proveden!", Toast.LENGTH_SHORT).show()
                            },
                            onLevelUp = {
                                val (success, msg) = engine.levelUpCharacter(character.id)
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            onOpenDetail = {
                                selectedCharacterForDetail = character
                            }
                        )
                    }
                }
            }
        }
    }

    // Recruit Modal Dialog
    if (showRecruitDialog) {
        RecruitModalDialog(
            gameState = gameState,
            engine = engine,
            onDismiss = { showRecruitDialog = false },
            onCharacterDiscovered = { newChar ->
                showRecruitDialog = false
                discoveredCharacter = newChar
            }
        )
    }

    // Discovery Reward Dialog
    discoveredCharacter?.let { char ->
        CharacterDiscoveryDialog(
            character = char,
            onDismiss = { discoveredCharacter = null },
            onRecruitAnother = {
                discoveredCharacter = null
                showRecruitDialog = true
            }
        )
    }

    // Character Detail & Stat Progression Modal
    selectedCharacterForDetail?.let { char ->
        AlertDialog(
            onDismissRequest = { selectedCharacterForDetail = null },
            title = { Text("Detail a progrese: ${char.name} (Úr. ${char.level})") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Role: ${char.role} • Věk: ${char.age} • Nálada: ${char.nalada}", style = MaterialTheme.typography.bodySmall)
                    CharacterStatProgressionVicoChart(
                        character = char,
                        currentDay = gameState.player.day,
                        isFullTab = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { selectedCharacterForDetail = null }) {
                    Text("Zavřít")
                }
            }
        )
    }
}

@Composable
fun HaremManagementItemCard(
    character: Character,
    currentDay: Int,
    onInteract: () -> Unit,
    onTrain: () -> Unit,
    onLevelUp: () -> Unit,
    onOpenDetail: () -> Unit
) {
    val portraitRes = StaticData.getPortraitForArchetype(character.archetypeId)
    val affinityTier = AffinityData.getTierForPoints(character.affinityPoints)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                com.example.haremdark.domain.SoundEffectManager.playCharacterVoice(character.voicePackId, com.example.haremdark.domain.CharacterVoiceType.TAP_GREETING)
                onOpenDetail()
            }
            .testTag("harem_item_card_${character.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(portraitRes)
                            .crossfade(true)
                            .build(),
                        contentDescription = character.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = character.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Úr. ${character.level}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "${character.role} • Nálada: ${character.nalada}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Affinity Tier Icon
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(affinityTier.colorHex).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(affinityTier.colorHex).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(affinityTier.icon, fontSize = 12.sp)
                        Text(
                            text = "St. ${affinityTier.level}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(affinityTier.colorHex)
                        )
                    }
                }
                
                // Rarity Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(character.rarityEnum.color).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(character.rarityEnum.color).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = character.rarityEnum.title,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(character.rarityEnum.color)
                    )
                }
            }

            // Stat bars row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBadgeSimple("⚔️ Síla", "${character.strength}", Color(0xFFFF7043), Modifier.weight(1f))
                StatBadgeSimple("🛡️ Obrana", "${character.attributes.defense}", Color(0xFF26C6DA), Modifier.weight(1f))
                StatBadgeSimple("💚 HP", "${character.hp}/${character.maxHp}", Color(0xFF66BB6A), Modifier.weight(1f))
                StatBadgeSimple("🤝 Loajalita", "${character.loajalita}%", Color(0xFFAB47BC), Modifier.weight(1f))
            }

            // Action Buttons (Level Up, Interact, Train, Chart)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onLevelUp,
                    modifier = Modifier.height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Vylepšit (75💰)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedButton(
                    onClick = onInteract,
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pozdravit", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = onOpenDetail,
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Grafy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatBadgeSimple(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
