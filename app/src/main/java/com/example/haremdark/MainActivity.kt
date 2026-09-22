package com.example.haremdark

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.MoodNotification
import com.example.haremdark.ui.components.GameTopBar
import com.example.haremdark.ui.screens.*
import com.example.haremdark.ui.components.DailyAttendanceDialog
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.domain.NavSound
import kotlinx.coroutines.launch

import com.example.haremdark.ui.theme.HaremDarkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.haremdark.domain.VoiceManager.init(applicationContext)
        com.example.haremdark.domain.HapticManager.init(applicationContext)
        val engine = GameEngine(applicationContext)

        setContent {
            val gameState by engine.gameState.collectAsState()
            val combatSession by engine.combatState.collectAsState()
            val currentTheme by engine.currentTheme.collectAsState()
            val isLightMode by engine.isLightMode.collectAsState()
            val dailyReward by engine.dailyRewardAvailable.collectAsState()
            val context = LocalContext.current
            
            var activeMoodNotification by remember { mutableStateOf<MoodNotification?>(null) }
            LaunchedEffect(Unit) {
                engine.moodNotifications.collect { notification ->
                    activeMoodNotification = notification
                }
            }

            var activeNarrativeEvent by remember { mutableStateOf<com.example.haremdark.data.NarrativeEvent?>(null) }
            LaunchedEffect(Unit) {
                engine.narrativeEvents.collect { event ->
                    activeNarrativeEvent = event
                }
            }

            val snackbarHostState = remember { SnackbarHostState() }
            LaunchedEffect(Unit) {
                var isFirst = true
                engine.lastAutoSaveEvent.collect { event ->
                    if (event != null) {
                        if (isFirst) {
                            isFirst = false
                        } else {
                            snackbarHostState.showSnackbar(
                                message = "💾 Automaticky uloženo: ${event.reason}",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            }
            
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: "home"
            val coroutineScope = rememberCoroutineScope()
            
            var showRestDialog by remember { mutableStateOf(false) }
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
 
            HaremDarkTheme(themeName = currentTheme, isLightMode = isLightMode) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier
                                .width(310.dp)
                                .fillMaxHeight()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(vertical = 12.dp)
                            ) {
                                // Drawer Header
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            "Rychlá navigace",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "${gameState.player.name} • Den ${gameState.player.day} (Lvl ${gameState.player.level})",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Quick Status Banner
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "💰 ${gameState.player.gold} zlata",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFFFD700)
                                        )
                                        Text(
                                            "⚡ ${gameState.player.sexEnergy}/${gameState.player.maxSexEnergy} SE",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFFF80AB)
                                        )
                                        Text(
                                            "🔮 ${gameState.player.mana}/${gameState.player.maxMana} MP",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF80D8FF)
                                        )
                                        Text(
                                            "🧪 ${gameState.player.manaEssence} ME",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF00E676)
                                        )
                                        Text(
                                            "🤝 ${gameState.player.influence} INF",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF03A9F4)
                                        )
                                    }
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )

                                // --- RYCHLÉ AKCE (QUICK ACTIONS) ---
                                Text(
                                    "⚡ RYCHLÉ AKCE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )

                                // 1. Další den (Nový den & Odpočinek)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF332612).copy(alpha = 0.75f)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.45f)),
                                    onClick = {
                                        coroutineScope.launch { drawerState.close() }
                                        showRestDialog = true
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFFFB300).copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Bedtime,
                                                contentDescription = "Další den",
                                                tint = Color(0xFFFFC107),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    "Další den",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFFFFE082)
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFFFFC107).copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        "Den ${gameState.player.day + 1}",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFFFD54F),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                "Odpočinek, plná energie & zisky z mafie",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                            )
                                        }
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD54F),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // 2. Rychlé uložení (Uložit hru)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF132B18).copy(alpha = 0.75f)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.45f)),
                                    onClick = {
                                        coroutineScope.launch {
                                            drawerState.close()
                                            engine.quickSaveSuspend()
                                            snackbarHostState.showSnackbar("💾 Uloženo do DataStore: Den ${gameState.player.day}, ${gameState.characters.size} dívek, výbava")
                                            Toast.makeText(context, "⚡ DataStore: Rychlé uložení dokončeno!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Save,
                                                contentDescription = "Rychlé uložení",
                                                tint = Color(0xFF66BB6A),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "Uložit hru (DataStore)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFFA5D6A7)
                                            )
                                            Text(
                                                "Uložit stav pána, dívek, skladu a výbavy",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                            )
                                        }
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = Color(0xFFA5D6A7),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )

                                // --- SEKCE 1: HLAVNÍ SPRÁVA ---
                                Text(
                                    "🏰 HLAVNÍ SPRÁVA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                                )

                                QuickNavDrawerItem(
                                    icon = Icons.Default.Castle,
                                    title = "Dominium",
                                    subtitle = "Hlavní sídlo & rychlý přehled",
                                    isSelected = currentRoute == "home",
                                    onClick = {
                                        coroutineScope.launch { drawerState.close() }
                                        SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                                        navController.navigate("home") { launchSingleTop = true }
                                    }
                                )
                                QuickNavDrawerItem(
                                    icon = Icons.Default.Favorite,
                                    title = "Harém",
                                    subtitle = "Dívky, komnaty & vztahy",
                                    badgeText = "${gameState.characters.size} dívek",
                                    isSelected = currentRoute == "harem",
                                    onClick = {
                                        coroutineScope.launch { drawerState.close() }
                                        SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                                        navController.navigate("harem") { launchSingleTop = true }
                                    }
                                )
                                QuickNavDrawerItem(
                                    icon = Icons.Default.Groups,
                                    title = "Roster Postav",
                                    subtitle = "Detailní statistiky & náklonnost",
                                    isSelected = currentRoute == "roster",
                                    onClick = {
                                        coroutineScope.launch {
                                            runCatching { drawerState.close() }
                                        }
                                        SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                                        if (currentRoute != "roster") {
                                            runCatching {
                                                navController.navigate("roster") { launchSingleTop = true }
                                            }
                                        }
                                    }
                                )
                                QuickNavDrawerItem(
                                    icon = Icons.Default.Inventory2,
                                    title = "Inventář & Sklad",
                                    subtitle = "Batoh, zámecký sklad, dary & kořist",
                                    isSelected = currentRoute == "inventory",
                                    onClick = {
                                        coroutineScope.launch { drawerState.close() }
                                        SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                                        navController.navigate("inventory") { launchSingleTop = true }
                                    }
                                )
                                QuickNavDrawerItem(
                                    icon = Icons.Default.LocationCity,
                                    title = "Pevnost & Impérium",
                                    subtitle = "Mafiánská teritoria & budovy",
                                    isSelected = currentRoute == "empire",
                                    onClick = {
                                        coroutineScope.launch { drawerState.close() }
                                        SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                                        navController.navigate("empire") { launchSingleTop = true }
                                    }
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )

                                // --- SEKCE 2: PRŮZKUM & BOJ ---
                                Text(
                                    "⚔️ PRŮZKUM & BOJ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                                )

                                QuickNavDrawerItem(
                                    icon = Icons.Default.Map,
                                    title = "Mapa světa",
                                    subtitle = "Provincie, cesty & expedice",
                                    isSelected = currentRoute == "map",
                                    onClick = {
                                        coroutineScope.launch { drawerState.close() }
                                        SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                                        navController.navigate("map") { launchSingleTop = true }
                                    }
                                )
                                QuickNavDrawerItem(
                                    icon = Icons.Default.Explore,
                                    title = "Aktivity & Lov",
                                    subtitle = "Dražby, alchymie, lov dívek & úkoly",
                                    isSelected = currentRoute == "activities",
                                    onClick = {
                                        coroutineScope.launch { drawerState.close() }
                                        SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                                        navController.navigate("activities") { launchSingleTop = true }
                                    }
                                )
                                QuickNavDrawerItem(
                                    icon = Icons.Default.SportsMartialArts,
                                    title = "Aréna & Souboje",
                                    subtitle = "Gladiátorské zápasy & turnaje",
                                    isSelected = currentRoute == "arena",
                                    onClick = {
                                        coroutineScope.launch { drawerState.close() }
                                        SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                                        navController.navigate("arena") { launchSingleTop = true }
                                    }
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )

                                // --- SEKCE 3: POSTAVA & SYSTÉM ---
                                Text(
                                    "👤 POSTAVA & SYSTÉM",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                                )

                                QuickNavDrawerItem(
                                    icon = Icons.Default.Person,
                                    title = "Pán Dominia",
                                    subtitle = "Statistiky, perky & trénink",
                                    isSelected = currentRoute == "progression",
                                    onClick = {
                                        coroutineScope.launch { drawerState.close() }
                                        SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                                        navController.navigate("progression") { launchSingleTop = true }
                                    }
                                )
                                 QuickNavDrawerItem(
                                     icon = Icons.Default.EmojiEvents,
                                     title = "Úspěchy & Trofeje",
                                     subtitle = "Tvé dosažené milníky",
                                     isSelected = currentRoute == "achievements",
                                     onClick = {
                                         coroutineScope.launch { drawerState.close() }
                                         SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                                         navController.navigate("achievements") { launchSingleTop = true }
                                     }
                                 )
                                 QuickNavDrawerItem(
                                     icon = Icons.Default.MenuBook,
                                     title = "Kodex Dominia",
                                     subtitle = "Lore, bestiář & historie",
                                     isSelected = currentRoute == "codex",
                                     onClick = {
                                         coroutineScope.launch { drawerState.close() }
                                         SoundEffectManager.playNavigation(NavSound.CODEX_OPEN)
                                         navController.navigate("codex") { launchSingleTop = true }
                                     }
                                 )
                                QuickNavDrawerItem(
                                    icon = Icons.Default.Settings,
                                    title = "Nastavení & Uložení",
                                    subtitle = "Ukládání / Načítání, vzhled & zvuk",
                                    isSelected = currentRoute == "settings",
                                    onClick = {
                                        coroutineScope.launch { drawerState.close() }
                                        SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                                        navController.navigate("settings") { launchSingleTop = true }
                                    }
                                )
                                QuickNavDrawerItem(
                                    icon = Icons.Default.Hub,
                                    title = "Síť Vztahů (Graph)",
                                    subtitle = "Konstelace harému & synergie",
                                    isSelected = currentRoute == "network_graph",
                                    onClick = {
                                        coroutineScope.launch { drawerState.close() }
                                        SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                                        navController.navigate("network_graph") { launchSingleTop = true }
                                    }
                                )

                                Spacer(Modifier.height(16.dp))
                            }
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
                                NavigationDestination("Pevnost", Icons.Default.LocationCity, "empire"),
                                NavigationDestination("Aktivity", Icons.Default.Explore, "activities"),
                                NavigationDestination("Pán", Icons.Default.Person, "progression")
                            )

                            items.forEach { dest ->
                                val isSelected = currentRoute == dest.route
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                                        navController.navigate(dest.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { 
                                        Icon(
                                            dest.icon, 
                                            contentDescription = dest.title,
                                            modifier = Modifier.size(22.dp)
                                        ) 
                                    },
                                    label = { 
                                        Text(
                                            dest.title, 
                                            fontSize = 10.sp, 
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1
                                        ) 
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
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
                        NavHost(
                            navController = navController,
                            startDestination = "home",
                            enterTransition = {
                                fadeIn(animationSpec = tween(500)) + slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(500, easing = EaseOutQuart)
                                )
                            },
                            exitTransition = {
                                fadeOut(animationSpec = tween(500)) + slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(500, easing = EaseOutQuart)
                                )
                            },
                            popEnterTransition = {
                                fadeIn(animationSpec = tween(500)) + slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(500, easing = EaseOutQuart)
                                )
                            },
                            popExitTransition = {
                                fadeOut(animationSpec = tween(500)) + slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(500, easing = EaseOutQuart)
                                )
                            }
                        ) {
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
                            composable("roster") {
                                RosterScreen(
                                    gameState = gameState,
                                    engine = engine,
                                    onBack = {
                                        if (!navController.popBackStack()) {
                                            navController.navigate("home") { launchSingleTop = true }
                                        }
                                    }
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
                                SaveSettingsScreen(gameState = gameState, currentTheme = currentTheme, isLightMode = isLightMode, engine = engine)
                            }
                            composable("achievements") {
                                com.example.haremdark.ui.screens.AchievementScreen(gameState = gameState, engine = engine, onMenuClick = { coroutineScope.launch { drawerState.open() } })
                            }
                            composable("codex") {
                                CodexScreen(player = gameState.player, onBack = { navController.popBackStack() })
                            }
                            composable("inventory") {
                                com.example.haremdark.ui.screens.InventoryScreen(gameState = gameState, engine = engine)
                            }
                            composable("network_graph") {
                                com.example.haremdark.ui.screens.CharacterNetworkGraphScreen(
                                    gameState = gameState,
                                    engine = engine,
                                    onMenuClick = { coroutineScope.launch { drawerState.open() } }
                                )
                            }
                        }

                        // Mood Notification Toast Overlay
                        activeMoodNotification?.let { notification ->
                            com.example.haremdark.ui.components.MoodNotificationToast(
                                notification = notification,
                                onDismiss = { activeMoodNotification = null }
                            )
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

    override fun onDestroy() {
        super.onDestroy()
        com.example.haremdark.domain.VoiceManager.shutdown()
    }
}

data class NavigationDestination(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@Composable
private fun QuickNavDrawerItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(22.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        label = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    if (badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
        },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            unselectedContainerColor = Color.Transparent
        ),
        modifier = modifier.padding(horizontal = 10.dp, vertical = 2.dp)
    )
}
