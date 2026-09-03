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
            val context = LocalContext.current
            
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: "home"
            
            var showRestDialog by remember { mutableStateOf(false) }

            HaremDarkTheme(themeName = currentTheme) {
                Scaffold(
                    topBar = {
                        GameTopBar(
                            player = gameState.player,
                            onRestClick = { showRestDialog = true },
                            onQuickSaveClick = { 
                                engine.quickSave()
                                Toast.makeText(context, "⚡ Rychlé uložení dokončeno!", Toast.LENGTH_SHORT).show()
                            }
                        )
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
                        }
                    }
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

data class NavigationDestination(
    val title: String,
    val icon: ImageVector,
    val route: String
)
