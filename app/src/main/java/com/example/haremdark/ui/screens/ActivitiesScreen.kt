package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import com.example.haremdark.data.GameContent
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.CombatSession
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.GameSave

@Composable
fun ActivitiesScreen(
    gameState: GameSave,
    combatSession: CombatSession?,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("🏹 Lov", "🏛️ Dražba", "⚔️ Souboje", "🧪 Alchymie", "📜 Úkoly")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) }
                )
            }
        }

        when (selectedTab) {
            0 -> HuntingTab(gameState, engine)
            1 -> AuctionTab(gameState, engine)
            2 -> CombatTab(gameState, combatSession, engine)
            3 -> AlchemyTab(gameState, engine)
            4 -> QuestsTab(gameState, engine)
        }
    }
}

@Composable
fun HuntingTab(gameState: GameSave, engine: GameEngine) {
    val context = LocalContext.current
    val locations = listOf(
        "Temný hvozd" to "Hluboké lesy plné poutnic, zbloudilých lovkyň a dezertérek.",
        "Ruiny starého chrámu" to "Znesvěcené svatyně ukrývající kněžky a tiché vyznavačky.",
        "Městské stoky a doupata" to "Špinavé podsvětí s kapsářkami a uprchlými otrokyněmi.",
        "Měsíční přístav" to "Doky plné pašeráckých lodí a cizinek z dalekých zemí.",
        "Šlechtické panství" to "Opuštěná panství s urozenými dámami prchajícími před inkvizicí."
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Lov a pátrání po nových otrokyních", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Každá výprava stojí 15 Sexuální energie. Získáš novou dívku s unikátním archetypem.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                }
            }
        }

        items(locations) { (locName, desc) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(locName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE91E63).copy(alpha = 0.2f)) {
                            Text("⚡ 15 energie", fontSize = 11.sp, color = Color(0xFFE91E63), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))

                    Button(
                        onClick = {
                            val (concubine, msg) = engine.hunt(locName)
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = gameState.player.sexEnergy >= 15,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.TrackChanges, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Vydat se na lov", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AuctionTab(gameState: GameSave, engine: GameEngine) {
    val context = LocalContext.current
    val auctionOffers = listOf(
        Triple("slechticna", "Bývalá hraběnka z rodu Valerius", 280),
        Triple("nymfomanka", "Nespoutaná kurtizána z přístavní čtvrti", 190),
        Triple("krvava_subka", "Krvavá gladiátorka z arény", 240),
        Triple("ticha_panenka", "Tajemná dívka bez minulosti", 210),
        Triple("posedla", "Posedlá vyznavačka temného rituálu", 320)
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Aukční síň otrokářského syndikátu", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Dražba vzácných a specializovaných archetypů bez nutnosti pátrání v divočině.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                }
            }
        }

        items(auctionOffers) { (archId, desc, price) ->
            val archetype = StaticData.ARCHETYPES[archId]
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(archetype?.name ?: archId, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFFD700).copy(alpha = 0.2f)) {
                            Text("💰 $price zl.", fontSize = 11.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                    Text("Popis archetypu: ${archetype?.description ?: ""}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)

                    Button(
                        onClick = {
                            val (success, msg) = engine.buyAuction(archId, price)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = gameState.player.gold >= price,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Vydražit a koupit", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CombatTab(gameState: GameSave, session: CombatSession?, engine: GameEngine) {
    if (session != null) {
        // Active Combat View
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Boss status card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(session.boss.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("${session.bossHp}/${session.bossMaxHp} HP", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                    }
                    val bossProgress = (session.bossHp.toFloat() / session.bossMaxHp.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { bossProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFE53935)
                    )
                    Text("Fáze: ${session.boss.phaseName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Player HP in combat
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pán dominia (${gameState.player.name})", fontWeight = FontWeight.Bold)
                        Text("${session.playerHp}/${session.playerMaxHp} HP", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    }
                    val playerProgress = (session.playerHp.toFloat() / session.playerMaxHp.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { playerProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            // Combat Actions
            if (!session.isOver) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { engine.executeCombatTurn("attack") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("🗡️ Útok", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { engine.executeCombatTurn("dark_burst") },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                        enabled = gameState.player.darkEnergy >= 10
                    ) {
                        Text("🔮 Temný výboj", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { engine.executeCombatTurn("heal") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🧪 Hojivý balzám", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { engine.executeCombatTurn("flee") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🏃 Útěk", fontSize = 11.sp)
                    }
                }
            } else {
                Button(
                    onClick = { engine.endCombat() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ukončit souboj & Zpět do arény", fontWeight = FontWeight.Bold)
                }
            }

            // Combat Log
            Text("Průběh souboje:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(session.log) { line ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(line, fontSize = 11.sp, modifier = Modifier.padding(6.dp))
                    }
                }
            }
        }
    } else {
        // Boss Selection List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Aréna a Bossové dominia", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Vyzvi na souboj mocné nepřátele pro zisk obrovského množství zlata, zkušeností a reputace.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                    }
                }
            }

            items(GameContent.BOSSES) { boss ->
                val defeated = gameState.defeatedBosses.contains(boss.id)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (defeated) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(boss.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            if (defeated) {
                                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF4CAF50).copy(alpha = 0.2f)) {
                                    Text("✓ Poražen", fontSize = 11.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            } else {
                                Text("${boss.hp} HP", color = Color(0xFFE53935), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        Text(boss.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                        Text("Odměna: ${boss.rewardGold} zlata • ${boss.rewardXp} XP", fontSize = 11.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.SemiBold)

                        Button(
                            onClick = { engine.startBossCombat(boss) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !defeated
                        ) {
                            Text(if (defeated) "Již poražen" else "Vyzvat na souboj", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlchemyTab(gameState: GameSave, engine: GameEngine) {
    val context = LocalContext.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Alchymistická laboratoř", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Míchej esence a temnou energii pro výrobu lektvarů pro harém a souboje.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                }
            }
        }

        items(GameContent.ALCHEMY_RECIPES) { recipe ->
            val canAffordGold = gameState.player.gold >= recipe.goldCost
            val canAffordDark = gameState.player.darkEnergy >= recipe.darkCost

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(recipe.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFFD700).copy(alpha = 0.2f)) {
                                Text("💰 ${recipe.goldCost}", fontSize = 11.sp, color = Color(0xFFFFD700), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF9C27B0).copy(alpha = 0.2f)) {
                                Text("🔮 ${recipe.darkCost}", fontSize = 11.sp, color = Color(0xFF9C27B0), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Text(recipe.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))

                    Button(
                        onClick = {
                            val (success, msg) = engine.brewAlchemy(recipe)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canAffordGold && canAffordDark,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Uvařit elixír", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun QuestsTab(gameState: GameSave, engine: GameEngine) {
    val context = LocalContext.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        items(GameContent.QUESTS) { quest ->
            val completed = gameState.completedQuests.contains(quest.id)
            val levelOk = gameState.player.level >= quest.reqLevel
            val concubinesOk = quest.reqConcubines == 0 || gameState.concubines.size >= quest.reqConcubines

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (completed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(quest.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (completed) Color(0xFF4CAF50).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (completed) "Splněno" else quest.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (completed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(quest.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                    Text("Odměna: +${quest.rewardGold} zlata • +${quest.rewardXp} XP ${if (quest.rewardDarkEnergy > 0) "• +${quest.rewardDarkEnergy} Temnoty" else ""}", fontSize = 11.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.SemiBold)

                    if (!completed) {
                        Button(
                            onClick = {
                                val (success, msg) = engine.claimQuest(quest.id)
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = levelOk && concubinesOk,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Splnit & Vyzvednout odměnu", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
