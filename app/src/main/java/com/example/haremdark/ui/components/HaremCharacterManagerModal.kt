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
                                onLease = { onLease(character.id) }
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
    onLease: () -> Unit
) {
    var roleDropdownOpen by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0F2E)),
        border = BorderStroke(
            1.dp,
            if (character.isFavorite) Color(0xFFFFD54F) else Color(0xFF9C27B0).copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Name, Title, Favorite & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(character.avatarIcon, fontSize = 22.sp)
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = character.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (character.isWife) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                    border = BorderStroke(0.5.dp, Color(0xFFFFD700))
                                ) {
                                    Text(
                                        text = "MANŽELKA",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD700),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (character.title.isNotBlank()) character.title else character.role.title,
                            fontSize = 11.sp,
                            color = Color(0xFFCE93D8)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (character.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Oblíbená",
                            tint = if (character.isFavorite) Color(0xFFFFD700) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Odebrat",
                            tint = Color(0xFFFF8A80),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Key Attributes Strip: Affection, Power Level, Role
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Affection Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2C1335),
                    border = BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💖 Náklonnost", fontSize = 10.sp, color = Color(0xFFFF80AB))
                            Text(
                                "${character.affection}/100",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF80AB)
                            )
                        }
                        Text(
                            text = character.affectionStage,
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { (character.affection / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = Color(0xFFFF4081),
                            trackColor = Color(0x33FF4081)
                        )
                    }
                }

                // Power Level Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF22163B),
                    border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚔️ Bojová síla", fontSize = 10.sp, color = Color(0xFFFFD54F))
                            Text(
                                "${character.powerLevel}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                        }
                        Text(
                            text = "Třída: ${character.powerTier} (Lv.${character.level})",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        LinearProgressIndicator(
                            progress = { (character.powerLevel / 500f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = Color(0xFFFFD54F),
                            trackColor = Color(0x33FFD54F)
                        )
                    }
                }
            }

            // Role Selector & Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Role Dropdown Button
                Box {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF38154D),
                        border = BorderStroke(1.dp, Color(0xFFAB47BC)),
                        modifier = Modifier.clickable { roleDropdownOpen = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(character.role.icon, fontSize = 12.sp)
                            Text(
                                text = "Role: ${character.role.title}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFFE1BEE7),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = roleDropdownOpen,
                        onDismissRequest = { roleDropdownOpen = false },
                        modifier = Modifier.background(Color(0xFF261238))
                    ) {
                        HaremRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(role.icon, fontSize = 14.sp)
                                        Column {
                                            Text(role.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(role.description, fontSize = 9.sp, color = Color(0xFFCE93D8))
                                        }
                                    }
                                },
                                onClick = {
                                    onRoleSelected(role)
                                    roleDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                // Quick Increment Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onSell,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Prodat 💰", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onLease,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF6C00)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Pronajmout 📜", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onAddAffection(5) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAD1457)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("+5 💖", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onAddPower(15) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57F17)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("+15 ⚔️", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }

                // Added interaction history
                com.example.haremdark.ui.components.InteractionHistoryView(history = character.interactionHistory)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Bonding Information
                Text("Pouto: ${character.bondingLevel.title}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                character.activeBonuses.forEach { bonus ->
                    Text("✨ ${bonus.title}: ${bonus.description}", fontSize = 11.sp, color = Color(0xFFFFD54F))
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
