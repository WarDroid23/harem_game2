package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.GameContent
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave
import com.example.haremdark.ui.components.LottieTacticalJsonCatalog
import com.example.haremdark.viewmodels.HaremViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterScreen(
    gameState: GameSave,
    engine: GameEngine,
    onBack: () -> Unit
) {
    val haremViewModel: HaremViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HaremViewModel(engine) as T
            }
        }
    )

    val filteredList by haremViewModel.filteredList.collectAsState()
    val searchQuery by haremViewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Roster Postav", style = MaterialTheme.typography.titleLarge)
                        Text("${filteredList.size} aktivních dcer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zpět")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Refresh or Filter logic if needed */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtr")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { haremViewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                placeholder = { Text("Hledat dceru podle jména nebo archetypu...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PersonSearch, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Žádná dcera nenalezena", color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 340.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList, key = { it.id }) { character ->
                        CharacterRosterCard(character = character)
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterRosterCard(character: Character) {
    val affinityTier = AffinityData.getTierForPoints(character.affinityPoints)
    val archetype = StaticData.ARCHETYPES[character.archetypeId]
    val context = LocalContext.current

    // Lottie Animation for Devotion/Affection Loop
    val composition by rememberLottieComposition(LottieCompositionSpec.JsonString(LottieTacticalJsonCatalog.DEVOTION_JSON))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 0.6f
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(affinityTier.colorHex).copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background Lottie for High Affinity
            if (character.affinityLevel >= 4) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0.15f)
                        .scale(1.5f)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Portrait
                    Box(modifier = Modifier.size(80.dp)) {
                        AsyncImage(
                            model = "https://api.aistudio.google.com/v1/assets/${character.id}_portrait", // Placeholder for actual logic
                            contentDescription = character.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, Color(affinityTier.colorHex), RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop,
                            error = androidx.compose.ui.res.painterResource(id = com.example.haremdark.R.drawable.ic_launcher_background) // fallback
                        )
                        
                        // Level Badge
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 4.dp, y = 4.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            tonalElevation = 4.dp
                        ) {
                            Text(
                                "Lvl ${character.level}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = character.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (character.oblibena) {
                                Icon(Icons.Default.Star, contentDescription = "Oblíbená", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            }
                        }
                        
                        Text(
                            text = "${archetype?.name ?: "Neznámá"} • ${character.age} let",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Affinity Progress Bar
                        val (currentInTier, tierSpan) = AffinityData.getProgressInTier(character.affinityPoints)
                        val progressValue = (currentInTier.toFloat() / tierSpan.toFloat()).coerceIn(0f, 1f)
                        
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    "${affinityTier.icon} ${affinityTier.title}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(affinityTier.colorHex)
                                )
                                Text(
                                    "$currentInTier / $tierSpan",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progressValue },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = Color(affinityTier.colorHex),
                                trackColor = Color(affinityTier.colorHex).copy(alpha = 0.2f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RosterStatItem(label = "Loajalita", value = "${character.loajalita}%", icon = Icons.Default.VerifiedUser, color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
                    RosterStatItem(label = "Morálka", value = "${character.morale}%", icon = Icons.Default.Mood, color = Color(0xFF2196F3), modifier = Modifier.weight(1f))
                    RosterStatItem(label = "Poslušnost", value = "${character.poslusnost}%", icon = Icons.Default.Handshake, color = Color(0xFF9C27B0), modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RosterStatItem(label = "Touha", value = "${character.touha}%", icon = Icons.Default.Favorite, color = Color(0xFFE91E63), modifier = Modifier.weight(1f))
                    RosterStatItem(label = "Důvěra", value = "${character.duvera}%", icon = Icons.Default.Diversity3, color = Color(0xFF00BCD4), modifier = Modifier.weight(1f))
                    RosterStatItem(label = "Strach", value = "${character.strach}%", icon = Icons.Default.Warning, color = Color(0xFFFF9800), modifier = Modifier.weight(1f))
                }

                if (character.broken > 0 || character.mindbreak > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (character.broken > 0) {
                            RosterStatItem(label = "Zlomení", value = "${character.broken}%", icon = Icons.Default.LinkOff, color = Color(0xFF795548), modifier = Modifier.weight(1f))
                        }
                        if (character.mindbreak > 0) {
                            RosterStatItem(label = "Mindbreak", value = "${character.mindbreak}%", icon = Icons.Default.Psychology, color = Color(0xFF673AB7), modifier = Modifier.weight(1f))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Status Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(character.statusIcon, fontSize = 16.sp)
                            Text(
                                text = character.nalada.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Text(
                            text = character.role,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RosterStatItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = color.copy(alpha = 0.7f))
        }
    }
}
