package com.example.haremdark.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.haremdark.data.HaremCharacterRepository
import com.example.haremdark.data.HaremCharacterSort
import com.example.haremdark.models.HaremCharacter
import com.example.haremdark.models.HaremRole

/**
 * Interactive management modal showcasing the HaremCharacter data class
 * and testing/modifying state through HaremCharacterRepository.
 */
@Composable
fun HaremCharacterManagerModal(
    repository: HaremCharacterRepository,
    onSell: (String) -> Unit,
    onLease: (String) -> Unit,
    onExecuteInteraction: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val characters by repository.characters.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf<HaremRole?>(null) }
    var selectedSort by remember { mutableStateOf(HaremCharacterSort.AFFECTION) }
    var sortAscending by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCharacter by remember { mutableStateOf<HaremCharacter?>(null) }

    val displayedCharacters = remember(characters, searchQuery, selectedRoleFilter, selectedSort, sortAscending) {
        val filtered = repository.filter(
            query = searchQuery,
            role = selectedRoleFilter
        )
        when (selectedSort) {
            HaremCharacterSort.AFFECTION -> if (sortAscending) filtered.sortedBy { it.affection } else filtered.sortedByDescending { it.affection }
            HaremCharacterSort.POWER_LEVEL -> if (sortAscending) filtered.sortedBy { it.powerLevel } else filtered.sortedByDescending { it.powerLevel }
            HaremCharacterSort.NAME -> if (sortAscending) filtered.sortedBy { it.name.lowercase() } else filtered.sortedByDescending { it.name.lowercase() }
            HaremCharacterSort.LEVEL -> if (sortAscending) filtered.sortedBy { it.level } else filtered.sortedByDescending { it.level }
            HaremCharacterSort.LOYALTY -> if (sortAscending) filtered.sortedBy { it.loyalty } else filtered.sortedByDescending { it.loyalty }
            HaremCharacterSort.ROLE -> if (sortAscending) filtered.sortedBy { it.role.title } else filtered.sortedByDescending { it.role.title }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140A1F)),
            border = BorderStroke(1.5.dp, Color(0xFF9C27B0).copy(alpha = 0.7f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF4A148C), Color(0xFF880E4F))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
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
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFBA68C8).copy(alpha = 0.25f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Správce Společnic Harému",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${characters.size} hrdinek • Správa jmen, náklonnosti, bojové síly a rolí",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE1BEE7)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = "Přidat hrdinku",
                                    tint = Color(0xFF69F0AE)
                                )
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Zavřít",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // Summary Stats Strip
                Surface(
                    color = Color(0xFF1B0F2A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val avgAffection = remember(characters) {
                            if (characters.isNotEmpty()) characters.map { it.affection }.average().toInt() else 0
                        }
                        val totalPower = remember(characters) {
                            characters.sumOf { it.powerLevel }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "💖 Průměrná náklonnost: $avgAffection/100",
                                fontSize = 11.sp,
                                color = Color(0xFFFF80AB)
                            )
                            Text(
                                text = "⚔️ Celková bojová síla: $totalPower",
                                fontSize = 11.sp,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    }
                }

                // Search & Filter Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Hledat společnici dle jména či titulu...", fontSize = 12.sp, color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Vymazat", tint = Color.Gray)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFBA68C8),
                            unfocusedBorderColor = Color(0xFF4A148C),
                            focusedContainerColor = Color(0xFF1E102E),
                            unfocusedContainerColor = Color(0xFF1E102E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Role Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedRoleFilter == null,
                                onClick = { selectedRoleFilter = null },
                                label = { Text("Všechny role (${characters.size})", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF7B1FA2),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1E102E),
                                    labelColor = Color.White.copy(alpha = 0.7f)
                                )
                            )
                        }
                        items(HaremRole.entries.toTypedArray()) { role ->
                            val count = characters.count { it.role == role }
                            FilterChip(
                                selected = selectedRoleFilter == role,
                                onClick = { selectedRoleFilter = if (selectedRoleFilter == role) null else role },
                                label = { Text("${role.icon} ${role.title} ($count)", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF7B1FA2),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1E102E),
                                    labelColor = Color.White.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }

                    // Sort Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Řadit podle:",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                HaremCharacterSort.AFFECTION to "💖 Náklonnost",
                                HaremCharacterSort.POWER_LEVEL to "⚔️ Síla",
                                HaremCharacterSort.NAME to "🔤 Jméno",
                                HaremCharacterSort.LEVEL to "⭐ Úroveň"
                            ).forEach { (sort, label) ->
                                val isSelected = selectedSort == sort
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) Color(0xFF4A148C) else Color(0xFF180A26),
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFBA68C8) else Color.White.copy(alpha = 0.1f)),
                                    modifier = Modifier.clickable {
                                        if (selectedSort == sort) {
                                            sortAscending = !sortAscending
                                        } else {
                                            selectedSort = sort
                                            sortAscending = false
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "$label ${if (isSelected) (if (sortAscending) "▲" else "▼") else ""}",
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Character Cards List
                if (displayedCharacters.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🔍", fontSize = 28.sp)
                            Text(
                                text = "Nenalezena žádná společnice odpovídající filtrům.",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(displayedCharacters, key = { it.id }) { character ->
                            HaremCharacterCard(
                                character = character,
                                onAddAffection = { delta -> repository.updateAffection(character.id, delta) },
                                onAddPower = { delta -> repository.updatePowerLevel(character.id, delta) },
                                onToggleFavorite = { repository.toggleFavorite(character.id) },
                                onRoleSelected = { newRole -> repository.updateRole(character.id, newRole) },
                                onDelete = { repository.remove(character.id) },
                                onSell = { onSell(character.id) },
                                onLease = { onLease(character.id) },
                                onExecuteInteraction = onExecuteInteraction
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddNewHaremCharacterDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newChar ->
                repository.add(newChar)
                showAddDialog = false
            }
        )
    }
}

/**
 * Individual card displaying and managing a single HaremCharacter's core attributes.
 */
@Composable
private fun HaremCharacterCard(
    character: HaremCharacter,
    onAddAffection: (Int) -> Unit,
    onAddPower: (Int) -> Unit,
    onToggleFavorite: () -> Unit,
    onRoleSelected: (HaremRole) -> Unit,
    onDelete: () -> Unit,
    onSell: () -> Unit,
    onLease: () -> Unit,
    onExecuteInteraction: (String, String) -> Unit // Added: (characterId, interactionId)
) {
    var expanded by remember { mutableStateOf(false) }
    var roleDropdownOpen by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0F2E)),
        border = BorderStroke(
            1.dp,
            if (character.isFavorite) Color(0xFFFFD54F) else Color(0xFF9C27B0).copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header (always visible)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(character.avatarIcon, fontSize = 22.sp)
                    Text(character.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Text("💖 ${character.affection} | 🛡️ ${character.morale} Morálka", fontSize = 12.sp, color = Color(0xFFFF80AB))
            }

            // Expanded Detail View
            if (expanded) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                
                // Detailed Stats Grid
                Text("Kompletní statistiky & Vlastnosti", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCE93D8))
                
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("⚔️ Síla: ${character.powerLevel}", fontSize = 11.sp, color = Color(0xFFFFB74D))
                        Text("❤️ Životy: ${character.currentHp}/${character.maxHp}", fontSize = 11.sp, color = Color(0xFFEF5350))
                        Text("🔮 Mana: ${character.currentMana}/${character.maxMana}", fontSize = 11.sp, color = Color(0xFF42A5F5))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🛡️ Oddanost: ${character.loyalty}%", fontSize = 11.sp, color = Color(0xFF81C784))
                        val elementColor = when (character.element) {
                            com.example.haremdark.models.Element.FIRE -> Color(0xFFFF5722)
                            com.example.haremdark.models.Element.WATER -> Color(0xFF2196F3)
                            com.example.haremdark.models.Element.ICE -> Color(0xFF80DEEA)
                            com.example.haremdark.models.Element.LIGHTNING -> Color(0xFFFFEB3B)
                            com.example.haremdark.models.Element.EARTH -> Color(0xFF8D6E63)
                            com.example.haremdark.models.Element.AIR -> Color(0xFF80CBC4)
                            com.example.haremdark.models.Element.DARK -> Color(0xFFBA68C8)
                            com.example.haremdark.models.Element.HOLY -> Color(0xFFFFD54F)
                            else -> Color(0xFFCFD8DC)
                        }
                        Text("🌀 Živel: ${character.element.name}", fontSize = 11.sp, color = elementColor)
                    }
                }

                // Morale & Productivity Section
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2A153E),
                    border = BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("✨ Morálka: ${character.morale}/100", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                            Text("Produkce: ${"%.1f".format(character.moraleMultiplier)}x", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF80CBC4))
                        }
                        LinearProgressIndicator(
                            progress = { (character.morale / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = when {
                                character.morale >= 70 -> Color(0xFF4CAF50)
                                character.morale >= 40 -> Color(0xFFFFC107)
                                else -> Color(0xFFE53935)
                            },
                            trackColor = Color.Black.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "Pouto: ${character.bondingLevel.title} • Nálada: ${character.mood}",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Equipped Skills
                Text("Odemčené & Vybavené schopnosti:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCE93D8))
                if (character.unlockedSkills.isEmpty()) {
                    Text("Zatím nebyly odemčeny žádné speciální schopnosti ze stromu dovedností.", fontSize = 10.sp, color = Color.Gray)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(character.unlockedSkills) { skillId ->
                            val skillNode = com.example.haremdark.models.SkillTreeData.nodes.find { it.id == skillId }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF3B1754),
                                border = BorderStroke(1.dp, Color(0xFFBA68C8).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(skillNode?.icon ?: "✨", fontSize = 12.sp)
                                    Column {
                                        Text(skillNode?.title ?: skillId, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(skillNode?.description ?: "", fontSize = 9.sp, color = Color(0xFFE1BEE7), maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }

                // One-on-One Bonding Interactions that Directly Impact Morale
                Text("💕 Osobní interakce sbližování (Vliv na morálku):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF80AB))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { onExecuteInteraction(character.id, "bonding_chat") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF512DA8)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💬 Rozhovor", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("+12 Morálka", fontSize = 9.sp, color = Color(0xFFFFD54F))
                        }
                    }
                    Button(
                        onClick = { onExecuteInteraction(character.id, "bonding_praise") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👑 Pochvala", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("+15 Morálka", fontSize = 9.sp, color = Color(0xFFFFD54F))
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { onExecuteInteraction(character.id, "bonding_stroll") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🌸 Procházka", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("+20 M • 10 SE", fontSize = 9.sp, color = Color(0xFFFF80AB))
                        }
                    }
                    Button(
                        onClick = { onExecuteInteraction(character.id, "bonding_gift") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880E4F)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎁 Osobní dar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("+25 M • 25 Zl.", fontSize = 9.sp, color = Color(0xFFFFD700))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog to add a brand new HaremCharacter to the repository state.
 */
@Composable
private fun AddNewHaremCharacterDialog(
    onDismiss: () -> Unit,
    onAdd: (HaremCharacter) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var affectionText by remember { mutableStateOf("50") }
    var powerText by remember { mutableStateOf("120") }
    var selectedRole by remember { mutableStateOf(HaremRole.WARRIOR) }
    var isWife by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E102E)),
            border = BorderStroke(1.dp, Color(0xFFBA68C8))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Přijmout novou společnici",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Jméno hrdinky", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = affectionText,
                        onValueChange = { affectionText = it.filter { char -> char.isDigit() } },
                        label = { Text("Náklonnost (0-100)", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = powerText,
                        onValueChange = { powerText = it.filter { char -> char.isDigit() } },
                        label = { Text("Bojová síla (PWR)", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Vyberte roli v harému:", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(HaremRole.entries.toTypedArray()) { role ->
                        FilterChip(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            label = { Text("${role.icon} ${role.title}", fontSize = 9.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Zrušit", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val aff = affectionText.toIntOrNull() ?: 50
                                val pwr = powerText.toIntOrNull() ?: 100
                                val newChar = HaremCharacter(
                                    name = name.trim(),
                                    affection = aff,
                                    powerLevel = pwr,
                                    role = selectedRole,
                                    isWife = isWife
                                )
                                onAdd(newChar)
                            }
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                    ) {
                        Text("Přidat do harému")
                    }
                }
            }
        }
    }
}
