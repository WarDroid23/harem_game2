package com.example.haremdark

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.ui.components.GameTopBar
import com.example.haremdark.ui.screens.*
import com.example.haremdark.ui.components.DailyAttendanceDialog
import kotlinx.coroutines.launch

import com.example.haremdark.ui.theme.HaremDarkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val engine = GameEngine(applicationContext)

        setContent {
            val gameState by engine.gameState.collectAsState()
            val combatSession by engine.combatState.collectAsState()
            val currentTheme by engine.currentTheme.collectAsState()
            val dailyReward by engine.dailyRewardAvailable.collectAsState()
            val context = LocalContext.current
            
            var activeNarrativeEvent by remember { mutableStateOf<com.example.haremdark.data.NarrativeEvent?>(null) }
            LaunchedEffect(Unit) {
                engine.narrativeEvents.collect { event ->
                    activeNarrativeEvent = event
                }
            }
            
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: "home"
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()
            
            var showRestDialog by remember { mutableStateOf(false) }
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            HaremDarkTheme(themeName = currentTheme) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Rychlá navigace",
                                modifier = Modifier.padding(16.dp),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            HorizontalDivider()
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                                label = { Text("Harém") },
                                selected = currentRoute == "harem",
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    navController.navigate("harem") { launchSingleTop = true }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                                label = { Text("Inventář") },
                                selected = currentRoute == "inventory",
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    navController.navigate("inventory") { launchSingleTop = true }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                                label = { Text("Úspěchy") },
                                selected = currentRoute == "achievements",
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    navController.navigate("achievements") { launchSingleTop = true }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.SportsMartialArts, contentDescription = null) },
                                label = { Text("Boj") },
                                selected = currentRoute == "arena" || currentRoute == "party_combat", // Support combat routes
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    // Default to arena or Party Combat
                                    navController.navigate("arena") { launchSingleTop = true }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }
                ) {
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                                GameTopBar(
                                    player = gameState.player,
                                    onRestClick = { showRestDialog = true },
                                    onQuickSaveClick = { 
                                        coroutineScope.launch {
                                            engine.quickSaveSuspend()
                                            snackbarHostState.showSnackbar("💾 Uloženo do DataStore (Pán, ${gameState.characters.size} dívek, výbava)")
                                            Toast.makeText(context, "⚡ DataStore: Rychlé uložení dokončeno!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            val items = listOf(
                                NavigationDestination("Dominium", Icons.Default.Castle, "home"),
                                NavigationDestination("Harém", Icons.Default.Favorite, "harem"),
                                NavigationDestination("Mapa", Icons.Default.Map, "map"),
                                NavigationDestination("Aréna", Icons.Default.Warning, "arena"),
                                NavigationDestination("Pevnost", Icons.Default.LocationCity, "empire"),
                                NavigationDestination("Aktivity", Icons.Default.Explore, "activities"),
                                NavigationDestination("Pán", Icons.Default.Person, "progression"),
                                NavigationDestination("Nastavení", Icons.Default.Settings, "settings")
                            )

                            items.forEach { dest ->
                                NavigationBarItem(
                                    selected = currentRoute == dest.route,
                                    onClick = {
                                        navController.navigate(dest.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(dest.icon, contentDescription = dest.title) },
                                    label = { Text(dest.title, fontSize = 8.sp, maxLines = 1) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            // Quick Save Action directly in the main Navigation Menu
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    coroutineScope.launch {
                                        engine.quickSaveSuspend()
                                        snackbarHostState.showSnackbar("💾 Uloženo do DataStore (Pán, ${gameState.characters.size} dívek, výbava)")
                                        Toast.makeText(context, "⚡ DataStore: Rychlé uložení dokončeno!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                icon = { 
                                    Icon(
                                        Icons.Default.Save, 
                                        contentDescription = "Rychlé uložení",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(22.dp)
                                    ) 
                                },
                                label = { 
                                    Text(
                                        "Uložit", 
                                        fontSize = 8.sp, 
                                        maxLines = 1,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50)
                                    ) 
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavHost(navController = navController, startDestination = "home") {
                            composable("home") {
                                HomeScreen(
                                    gameState = gameState,
                                    engine = engine,
                                    onNavigateToHarem = { navController.navigate("harem") },
                                    onNavigateToActivities = { navController.navigate("activities") },
                                    onNavigateToEmpire = { navController.navigate("empire") },
                                    onNavigateToProgression = { navController.navigate("progression") },
                                    onNavigateToMap = { navController.navigate("map") }
                                )
                            }
                            composable("harem") {
                                HaremScreen(
                                    gameState = gameState,
                                    engine = engine,
                                    onNavigateToHunt = { navController.navigate("activities") }
                                )
                            }
                            composable("map") {
                                WorldMapScreen(gameState = gameState, engine = engine)
                            }
                            composable("arena") {
                                ArenaScreen(gameState = gameState, engine = engine)
                            }
                            composable("empire") {
                                EmpireScreen(gameState = gameState, engine = engine)
                            }
                            composable("activities") {
                                ActivitiesScreen(gameState = gameState, combatSession = combatSession, engine = engine)
                            }
                            composable("progression") {
                                ProgressionScreen(gameState = gameState, engine = engine)
                            }
                            composable("settings") {
                                SaveSettingsScreen(gameState = gameState, currentTheme = currentTheme, engine = engine)
                            }
                            composable("achievements") {
                                com.example.haremdark.ui.screens.AchievementScreen(gameState = gameState, engine = engine, onMenuClick = { coroutineScope.launch { drawerState.open() } })
                            }
                            composable("inventory") {
                                com.example.haremdark.ui.screens.InventoryScreen(gameState = gameState, engine = engine)
                            }
                        }
                    }
                }

                                                // Daily Login Reward Dialog
                dailyReward?.let { reward ->
                    DailyAttendanceDialog(
                        reward = reward,
                        onClaim = {
                            engine.claimDailyReward()
                            Toast.makeText(context, "Denní odměna vybrána!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Narrative Scenario Dialog
                activeNarrativeEvent?.let { event ->
                    AlertDialog(
                        onDismissRequest = { activeNarrativeEvent = null },
                        title = { Text(event.title, color = MaterialTheme.colorScheme.primary) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(event.description, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Odměna:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(event.rewardText, color = Color(0xFFFFD700), fontSize = 12.sp)
                            }
                        },
                        confirmButton = {
                            Button(onClick = { activeNarrativeEvent = null }) {
                                Text("Pokračovat")
                            }
                        }
                    )
                }

                // Next Day Rest Confirmation Dialog
                if (showRestDialog) {
                    AlertDialog(
                        onDismissRequest = { showRestDialog = false },
                        title = { Text("Ukončit den a odpočinout si?") },
                        text = {
                            Text(
                                "Postoupíš do Dne ${gameState.player.day + 1}.\n" +
                                "• Sexuální a Temná energie budou plně doplněny\n" +
                                "• Vyberou se zisky z mafiánských území a budov\n" +
                                "• Posune se doba nájmů a těhotenství v harému"
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showRestDialog = false
                                    engine.restNextDay()
                                    Toast.makeText(context, "🌅 Svítá nový den v dominiu!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Ano, nový den")
                            }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { showRestDialog = false }) {
                                Text("Zrušit")
                            }
                        }
                    )
                }
                }
            }
        }
    }
}

data class NavigationDestination(
    val title: String,
    val icon: ImageVector,
    val route: String
)
