package com.example.haremdark.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import com.example.haremdark.data.StaticData
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave

@Composable
fun HaremArchiveTab(gameState: GameSave, modifier: Modifier = Modifier) {
    var selectedCharacter by remember { mutableStateOf<Character?>(null) }
    
    val recruitedGirls = gameState.characters
    
    if (recruitedGirls.isEmpty()) {
        Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("Zatím nemáš v harému žádné dívky k archivaci.", color = Color.Gray)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Hero Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Osobní spisy a biografie",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Kompletní archiv portrétů ve vysokém rozlišení a psychologických profilů tvých aktuálních otrokyň.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(recruitedGirls) { character ->
                ArchiveCard(
                    character = character,
                    onClick = { selectedCharacter = character }
                )
            }
        }
    }

    selectedCharacter?.let { character ->
        BiographyDialog(
            character = character,
            onDismiss = { selectedCharacter = null }
        )
    }
}

@Composable
fun ArchiveCard(character: Character, onClick: () -> Unit) {
    val portraitRes = StaticData.getPortraitForArchetype(character.archetypeId)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(portraitRes)
                    .crossfade(true)
                    .build(),
                contentDescription = character.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Fallback",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xAA000000), Color(0xEE000000)),
                            startY = 150f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = character.role,
                    fontSize = 10.sp,
                    color = Color(0xFFB39DDB)
                )
            }
            if (character.oblibena) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Oblíbená",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(20.dp)
                )
            }
        }
    }
}

@Composable
fun BiographyDialog(character: Character, onDismiss: () -> Unit) {
    val portraitRes = StaticData.getPortraitForArchetype(character.archetypeId)
    val archetypeInfo = StaticData.ARCHETYPES[character.archetypeId]
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // High Resolution Art View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.45f)
                ) {
                    Image(
                        painter = painterResource(id = portraitRes),
                        contentDescription = "High Res Portrait",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color(0x88000000), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White)
                    }
                    
                    // Name overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000))))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = character.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${character.age} let • ${archetypeInfo?.name ?: "Neznámý archetyp"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFE1BEE7)
                            )
                        }
                    }
                }

                // Biography Text Content
                Column(
                    modifier = Modifier
                        .weight(0.55f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    BiographySection(
                        title = "Původní profil",
                        icon = "📖",
                        content = archetypeInfo?.description ?: "Záhadná dívka neznámého původu."
                    )
                    
                    val statusText = buildString {
                        append("Tato dívka u tebe našla své místo jako ${character.role}.")
                        if (character.jeManzelkou) append(" Stala se tvou právoplatnou manželkou a chotí.")
                        else if (character.partnerka) append(" Slouží jako tvá intimní partnerka.")
                        
                        if (character.oblibena) append(" Je v současnosti tvou největší oblíbenkyní v harému.")
                        
                        if (character.deti > 0) append(" Porodila ti již ${character.deti} potomků a rozšiřuje tvou dynastii.")
                    }
                    BiographySection(
                        title = "Záznam z harému",
                        icon = "🏰",
                        content = statusText
                    )
                    
                    val mindsetText = buildString {
                        append("Její oddanost k tobě je na úrovni ${character.loajalita}%. ")
                        append("Aktuální fáze zkázanosti dosáhla úrovně ${character.fazeZkazenosti}. ")
                        if (character.broken > 50) append("Její vůle byla prakticky zlomena.")
                        else if (character.poslusnost > 70) append("Je neobyčejně poslušná a plní příkazy s pokorou.")
                        else append("Stále si zachovává určitou míru vzdoru a nezávislosti.")
                    }
                    BiographySection(
                        title = "Psychologický profil",
                        icon = "🧠",
                        content = mindsetText
                    )
                }
            }
        }
    }
}

@Composable
fun BiographySection(title: String, icon: String, content: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(icon, fontSize = 16.sp)
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 16.sp
            )
        }
    }
}
