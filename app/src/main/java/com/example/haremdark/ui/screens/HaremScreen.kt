package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.GameInteraction
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.Concubine
import com.example.haremdark.models.GameSave
import com.example.haremdark.ui.components.ConcubineCard
import com.example.haremdark.ui.components.ConcubineDetailDialog
import com.example.haremdark.ui.components.InteractionDialog

@Composable
fun HaremScreen(
    gameState: GameSave,
    engine: GameEngine,
    onNavigateToHunt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableIntStateOf(0) }
    val filters = listOf("Všechny", "★ Oblíbená", "💍 Vztahy", "💰 Na nájmu")

    var interactingConcubine by remember { mutableStateOf<Concubine?>(null) }
    var detailConcubine by remember { mutableStateOf<Concubine?>(null) }

    val filteredList = remember(gameState.concubines, selectedFilter) {
        when (selectedFilter) {
            1 -> gameState.concubines.filter { it.oblibena }
            2 -> gameState.concubines.filter { it.jeManzelkou || it.partnerka }
            3 -> gameState.concubines.filter { it.naNajmu }
            else -> gameState.concubines
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
        ) {
            // Harem Progress Header
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Úroveň harému ${gameState.haremLevel}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Kapacita: ${gameState.concubines.size} dívek",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val progress = (gameState.haremExp.toFloat() / gameState.haremMaxExp.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "EXP: ${gameState.haremExp}/${gameState.haremMaxExp}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Pasivní příjem: +${gameState.haremLevel * 10} zl./den",
                                fontSize = 10.sp,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filters.forEachIndexed { index, filter ->
                        FilterChip(
                            selected = selectedFilter == index,
                            onClick = { selectedFilter = index },
                            label = { Text(filter, fontSize = 11.sp, fontWeight = if (selectedFilter == index) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // Concubines List
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SentimentDissatisfied,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Žádná otrokyně neodpovídá zvolenému filtru.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { concubine ->
                    ConcubineCard(
                        concubine = concubine,
                        onInteractClick = { interactingConcubine = concubine },
                        onDetailClick = { detailConcubine = concubine },
                        onFavoriteClick = {
                            val res = engine.setFavorite(concubine.id)
                            Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Floating Action Button for Hunt
        FloatingActionButton(
            onClick = onNavigateToHunt,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ulovit dívku")
                Text("Ulovit", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Interaction Sheet
    interactingConcubine?.let { concubine ->
        InteractionDialog(
            concubine = concubine,
            player = gameState.player,
            onDismiss = { interactingConcubine = null },
            onExecuteInteraction = { interaction ->
                val (success, msg) = engine.executeInteraction(concubine.id, interaction)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                if (success) {
                    interactingConcubine = gameState.concubines.firstOrNull { it.id == concubine.id }
                }
            },
            onCourtRomance = {
                val (success, msg) = engine.courtRomance(concubine.id)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            onMarry = {
                val (success, msg) = engine.marryConcubine(concubine.id)
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            },
            onRent = { client, days ->
                val (success, msg) = engine.rentSlave(concubine.id, client, days)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                if (success) interactingConcubine = null
            }
        )
    }

    // Detail Dialog
    detailConcubine?.let { concubine ->
        ConcubineDetailDialog(
            concubine = concubine,
            onDismiss = { detailConcubine = null }
        )
    }
}
