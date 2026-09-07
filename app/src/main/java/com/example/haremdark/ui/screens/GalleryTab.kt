package com.example.haremdark.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.haremdark.R
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.StaticData
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave

data class GalleryItem(
    val title: String,
    val subtitle: String,
    val imageRes: Int,
    val isUnlocked: Boolean,
    val lockReason: String = ""
)

@Composable
fun HaremGalleryTab(gameState: GameSave, modifier: Modifier = Modifier) {
    var selectedImage by remember { mutableStateOf<GalleryItem?>(null) }
    
    // Generate Gallery Items dynamically based on unlocked characters
    val galleryItems = remember(gameState.characters) {
        val items = mutableListOf<GalleryItem>()
        
        // 1. Portraits
        gameState.characters.forEach { char ->
            val portraitRes = StaticData.getPortraitForArchetype(char.archetypeId)
            items.add(
                GalleryItem(
                    title = "Portrét: ${char.name}",
                    subtitle = "Získáno: Den ${char.lastInteractionDay}",
                    imageRes = portraitRes,
                    isUnlocked = true
                )
            )
            
            // 2. Affinity Events Artwork
            // Tier 3
            val tier3Unlocked = char.affinityPoints >= AffinityData.TIERS[2].minPoints
            items.add(
                GalleryItem(
                    title = "${char.name} - Noční ticho",
                    subtitle = "Odemčeno na 3. úrovni náklonnosti (Důvěrnice)",
                    imageRes = R.drawable.img_tavern_sexy,
                    isUnlocked = tier3Unlocked,
                    lockReason = "Vyžaduje 3. úroveň náklonnosti s ${char.name}"
                )
            )
            
            // Tier 5
            val tier5Unlocked = char.affinityPoints >= AffinityData.TIERS[4].minPoints
            items.add(
                GalleryItem(
                    title = "${char.name} - Oddanost duše",
                    subtitle = "Odemčeno na 5. úrovni náklonnosti (Spřízněná duše)",
                    imageRes = R.drawable.img_harem_boudoir,
                    isUnlocked = tier5Unlocked,
                    lockReason = "Vyžaduje 5. úroveň náklonnosti s ${char.name}"
                )
            )
        }
        
        // Add some generic high-tier artworks that require total harem size or total affinity
        val totalAffinity = gameState.characters.sumOf { it.affinityPoints }
        items.add(
            GalleryItem(
                title = "Slavnost Hříšníků",
                subtitle = "Epické shromáždění tvého harému",
                imageRes = R.drawable.img_dark_banner,
                isUnlocked = totalAffinity >= 500,
                lockReason = "Vyžaduje celkovou náklonnost harému 500 bodů"
            )
        )
        
        items
    }

    if (galleryItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                Text("Galerie je prázdná.", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Text("Získejte dívky a budujte s nimi náklonnost k odhalení vzpomínek.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        modifier = modifier.fillMaxSize().padding(horizontal = 4.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text("🖼️ Osobní Galerie Vzpomínek", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
        }
        
        items(galleryItems) { item ->
            GalleryCard(item = item, onClick = { if (item.isUnlocked) selectedImage = item })
        }
    }

    // Full Screen Image Viewer
    selectedImage?.let { item ->
        Dialog(
            onDismissRequest = { selectedImage = null },
            properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.imageRes)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Overlay text
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Text(item.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.subtitle, fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                }

                // Close Button
                IconButton(
                    onClick = { selectedImage = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun GalleryCard(item: GalleryItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(enabled = item.isUnlocked, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.isUnlocked) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.imageRes)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }
                )
                
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                        .padding(8.dp)
                ) {
                    Text(item.title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1)
                }
            } else {
                // Locked state
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Lock, contentDescription = "Zamčeno", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(item.lockReason, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 14.sp)
                    }
                }
            }
        }
    }
}
