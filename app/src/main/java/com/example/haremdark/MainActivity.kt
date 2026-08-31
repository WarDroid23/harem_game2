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

            var currentNavIndex by remember { mutableIntStateOf(0) }
            var showRestDialog by remember { mutableStateOf(false) }

            HaremDarkTheme(themeName = currentTheme) {
                Scaffold(
                    topBar = {
                        GameTopBar(
                            player = gameState.player,
                            onRestClick = { showRestDialog = true }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            val items = listOf(
                                NavigationDestination("Dominium", Icons.Default.Castle, 0),
                                NavigationDestination("Harém", Icons.Default.Favorite, 1),
                                NavigationDestination("Mapa", Icons.Default.Map, 2),
                                NavigationDestination("Galerie", Icons.Default.Collections, 3),
                                NavigationDestination("Pevnost", Icons.Default.LocationCity, 4),
                                NavigationDestination("Aktivity", Icons.Default.Explore, 5),
                                NavigationDestination("Pán", Icons.Default.Person, 6),
                                NavigationDestination("Nastavení", Icons.Default.Settings, 7)
                            )

                            items.forEach { dest ->
                                NavigationBarItem(
                                    selected = currentNavIndex == dest.index,
                                    onClick = { currentNavIndex = dest.index },
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
                        when (currentNavIndex) {
                            0 -> HomeScreen(
                                gameState = gameState,
                                engine = engine,
                                onNavigateToHarem = { currentNavIndex = 1 },
                                onNavigateToActivities = { currentNavIndex = 5 },
                                onNavigateToEmpire = { currentNavIndex = 4 },
                                onNavigateToProgression = { currentNavIndex = 6 },
                                onNavigateToMap = { currentNavIndex = 2 }
                            )
                            1 -> HaremScreen(
                                gameState = gameState,
                                engine = engine,
                                onNavigateToHunt = { currentNavIndex = 5 }
                            )
                            2 -> WorldMapScreen(
                                gameState = gameState,
                                engine = engine
                            )
                            3 -> GalleryScreen(
                                gameState = gameState,
                                onNavigateToHarem = { currentNavIndex = 1 }
                            )
                            4 -> EmpireScreen(
                                gameState = gameState,
                                engine = engine
                            )
                            5 -> ActivitiesScreen(
                                gameState = gameState,
                                combatSession = combatSession,
                                engine = engine
                            )
                            6 -> ProgressionScreen(
                                gameState = gameState,
                                engine = engine
                            )
                            7 -> SaveSettingsScreen(
                                gameState = gameState,
                                currentTheme = currentTheme,
                                engine = engine
                            )
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
    val index: Int
)
