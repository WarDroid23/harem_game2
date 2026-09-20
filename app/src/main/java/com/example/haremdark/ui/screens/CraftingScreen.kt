package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.GameContent
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.AlchemyRecipe
import com.example.haremdark.models.GameSave
import com.example.haremdark.ui.components.CraftingResourcePill
import com.example.haremdark.ui.components.CraftingResourceDetailModal
import com.example.haremdark.ui.components.EquipmentFragmentCard

@Composable
fun CraftingScreen(
    gameState: GameSave,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedResourceDetail by remember { mutableStateOf<com.example.haremdark.models.CraftingResource?>(null) }

    val fragments = remember(gameState.player.equipmentFragments) {
        engine.getAvailableFragmentsWithProgress()
    }
    val resources = remember(gameState.player.craftingResources) {
        engine.getPlayerCraftingResourcesList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0615))
            .padding(16.dp)
            .testTag("crafting_screen")
    ) {
        // Header
        Text(
            text = "Řemeslná dílna & Kovárna",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFFD700)
        )
        Text(
            text = "Syntetizuj alchymistické elixíry nebo kovej zbraně z úlomků získaných z efektivních bojů.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFCE93D8)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF1D0C2A),
            contentColor = Color(0xFFFFD700),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("🧪 Alchymie", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    val readyCount = fragments.count { it.isReadyToForge }
                    Text(
                        if (readyCount > 0) "🔨 Kovárna ($readyCount)" else "🔨 Kovárna",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("💎 Suroviny (${resources.size})", fontWeight = FontWeight.Bold) }
            )
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(GameContent.ALCHEMY_RECIPES) { recipe ->
                        CraftingRecipeCard(recipe = recipe, gameState = gameState, engine = engine)
                    }
                }
            }
            1 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF220E2F),
                            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚡ Kování z úlomků výbavy",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                                Text(
                                    text = "Úlomky získáváš z post-battle distribuce kořisti za vysoké hodnocení efektivity (S/S+). Jakmile nasbíráš plný počet, můžeš předmět okamžitě ukovat!",
                                    fontSize = 10.sp,
                                    color = Color(0xFFE1BEE7)
                                )
                            }
                        }
                    }

                    items(fragments) { frag ->
                        EquipmentFragmentCard(
                            fragment = frag,
                            isForged = false,
                            onForge = {
                                val (success, msg) = engine.forgeEquipmentFromFragments(frag.id)
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
            2 -> {
                if (resources.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Zatím jsi nenasbíral žádné speciální suroviny z bojů.\nVyhraj bitvy s vysokou efektivitou!",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(resources) { res ->
                            CraftingResourcePill(
                                resource = res,
                                onClick = { selectedResourceDetail = res }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedResourceDetail?.let { res ->
        CraftingResourceDetailModal(
            resource = res,
            onDismiss = { selectedResourceDetail = null }
        )
    }
}

@Composable
fun CraftingRecipeCard(
    recipe: AlchemyRecipe,
    gameState: GameSave,
    engine: GameEngine
) {
    val context = LocalContext.current
    val player = gameState.player

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1B0B26),
        border = BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(recipe.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
            Text(recipe.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFFE1BEE7))
            Spacer(modifier = Modifier.height(6.dp))
            Text("Náklady: ${recipe.goldCost} zlata, ${recipe.darkCost} temné energie", fontSize = 10.sp, color = Color(0xFF80D8FF))

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val (success, msg) = engine.craftItem(recipe.id)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(36.dp)
            ) {
                Text("Syntetizovat", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
