package com.example.haremdark.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ChevronRight
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
import com.example.haremdark.models.Character

@Composable
fun CharacterComparisonDialog(
    characters: List<Character>,
    initialChar1: Character? = null,
    onDismiss: () -> Unit
) {
    var char1Id by remember { mutableStateOf(initialChar1?.id ?: characters.firstOrNull()?.id) }
    var char2Id by remember { mutableStateOf(characters.getOrNull(1)?.id ?: characters.firstOrNull()?.id) }

    var showSelector1 by remember { mutableStateOf(false) }
    var showSelector2 by remember { mutableStateOf(false) }

    val char1 = characters.firstOrNull { it.id == char1Id } ?: characters.firstOrNull()
    val char2 = characters.firstOrNull { it.id == char2Id } ?: characters.getOrNull(1) ?: char1

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF191224)),
            border = BorderStroke(1.5.dp, Color(0xFFAB47BC)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(vertical = 8.dp)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("⚖️", fontSize = 22.sp)
                        Text(
                            text = "Porovnání postav",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White.copy(alpha = 0.7f))
                    }
                }

                // Selection Header (Character A vs Character B)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Character A Selector Button
                    Button(
                        onClick = { showSelector1 = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = char1?.name ?: "Vyber postavu",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Text("VS", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700), fontSize = 14.sp)

                    // Character B Selector Button
                    Button(
                        onClick = { showSelector2 = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880E4F)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = char2?.name ?: "Vyber postavu",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                // Comparison Bar Charts Area
                if (char1 != null && char2 != null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            ComparisonBarRow(
                                label = "Síla (Strength)",
                                val1 = char1.effectiveStrength.toFloat(),
                                val2 = char2.effectiveStrength.toFloat(),
                                maxVal = 100f,
                                color1 = Color(0xFF7E57C2),
                                color2 = Color(0xFFEC407A)
                            )
                        }
                        item {
                            ComparisonBarRow(
                                label = "Loajalita (Loyalty)",
                                val1 = char1.loajalita.toFloat(),
                                val2 = char2.loajalita.toFloat(),
                                maxVal = 100f,
                                color1 = Color(0xFF7E57C2),
                                color2 = Color(0xFFEC407A)
                            )
                        }
                        item {
                            ComparisonBarRow(
                                label = "Náklonnost (Affection)",
                                val1 = char1.srdce.toFloat(),
                                val2 = char2.srdce.toFloat(),
                                maxVal = 100f,
                                color1 = Color(0xFF7E57C2),
                                color2 = Color(0xFFEC407A)
                            )
                        }
                        item {
                            ComparisonBarRow(
                                label = "Důvěra (Trust)",
                                val1 = char1.duvera.toFloat(),
                                val2 = char2.duvera.toFloat(),
                                maxVal = 100f,
                                color1 = Color(0xFF7E57C2),
                                color2 = Color(0xFFEC407A)
                            )
                        }
                        item {
                            ComparisonBarRow(
                                label = "Poslušnost (Obedience)",
                                val1 = char1.poslusnost.toFloat(),
                                val2 = char2.poslusnost.toFloat(),
                                maxVal = 100f,
                                color1 = Color(0xFF7E57C2),
                                color2 = Color(0xFFEC407A)
                            )
                        }
                        item {
                            ComparisonBarRow(
                                label = "Touha (Desire)",
                                val1 = char1.touha.toFloat(),
                                val2 = char2.touha.toFloat(),
                                maxVal = 100f,
                                color1 = Color(0xFF7E57C2),
                                color2 = Color(0xFFEC407A)
                            )
                        }
                        item {
                            ComparisonBarRow(
                                label = "Rarita (★)",
                                val1 = (char1.rarity * 20).toFloat(),
                                val2 = (char2.rarity * 20).toFloat(),
                                maxVal = 100f,
                                customText1 = "${char1.rarity}★",
                                customText2 = "${char2.rarity}★",
                                color1 = Color(0xFF7E57C2),
                                color2 = Color(0xFFEC407A)
                            )
                        }
                        item {
                            ComparisonBarRow(
                                label = "Úroveň vztahu",
                                val1 = (char1.affinityLevel * 20).toFloat(),
                                val2 = (char2.affinityLevel * 20).toFloat(),
                                maxVal = 100f,
                                customText1 = "Úr. ${char1.affinityLevel}",
                                customText2 = "Úr. ${char2.affinityLevel}",
                                color1 = Color(0xFF7E57C2),
                                color2 = Color(0xFFEC407A)
                            )
                        }
                    }
                }
            }
        }
    }

    // Character 1 Selection Dialog
    if (showSelector1) {
        CharacterSelectionListModal(
            title = "Vyber první postavu",
            characters = characters,
            onSelect = {
                char1Id = it.id
                showSelector1 = false
            },
            onDismiss = { showSelector1 = false }
        )
    }

    // Character 2 Selection Dialog
    if (showSelector2) {
        CharacterSelectionListModal(
            title = "Vyber druhou postavu",
            characters = characters,
            onSelect = {
                char2Id = it.id
                showSelector2 = false
            },
            onDismiss = { showSelector2 = false }
        )
    }
}

@Composable
fun ComparisonBarRow(
    label: String,
    val1: Float,
    val2: Float,
    maxVal: Float = 100f,
    customText1: String? = null,
    customText2: String? = null,
    color1: Color,
    color2: Color
) {
    val fraction1 = (val1 / maxVal).coerceIn(0f, 1f)
    val fraction2 = (val2 / maxVal).coerceIn(0f, 1f)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF221831),
        border = BorderStroke(0.5.dp, Color(0xFF4A355A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f)
            )

            // Bar 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { fraction1 },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = color1,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Text(
                    text = customText1 ?: "${val1.toInt()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color1,
                    modifier = Modifier.width(36.dp)
                )
            }

            // Bar 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { fraction2 },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = color2,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Text(
                    text = customText2 ?: "${val2.toInt()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color2,
                    modifier = Modifier.width(36.dp)
                )
            }
        }
    }
}

@Composable
fun CharacterSelectionListModal(
    title: String,
    characters: List<Character>,
    onSelect: (Character) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF191224)),
            border = BorderStroke(1.dp, Color(0xFFAB47BC)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(characters) { character ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF261836),
                            border = BorderStroke(0.5.dp, Color(0xFF4A355A)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(character) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(character.statusIcon, fontSize = 18.sp)
                                    Column {
                                        Text(
                                            text = character.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Rarita: ${character.rarity}★ • Síla: ${character.effectiveStrength}",
                                            fontSize = 10.sp,
                                            color = Color(0xFFFF80AB)
                                        )
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Zavřít", fontSize = 11.sp)
                }
            }
        }
    }
}
