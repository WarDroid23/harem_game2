package com.example.haremdark.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.StaticData
import com.example.haremdark.models.Character
import com.example.haremdark.models.getRelationship

@Composable
fun CharacterCard(
    character: Character,
    onInteractClick: () -> Unit,
    onDetailClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onPinClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val loyaltyTier = StaticData.getLoyaltyTier(character.loajalita)
    val archetype = StaticData.ARCHETYPES[character.archetypeId]
    val phase = StaticData.DEGRADATION_PHASES[character.fazeZkazenosti]
    val affinityTier = AffinityData.getTierForPoints(character.affinityPoints)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Avatar, Name, Badges, Favorite Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Avatar with Coil
                    val portraitRes = StaticData.getPortraitForArchetype(character.archetypeId)
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(portraitRes)
                            .crossfade(true)
                            .build(),
                        contentDescription = character.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = character.name.take(1),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = character.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (character.oblibena) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                                ) {
                                    Text(
                                        text = "★ Oblíbenkyně",
                                        color = Color(0xFFFFD700),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (character.jeManzelkou) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFE040FB).copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE040FB))
                                ) {
                                    Text(
                                        text = "💍 Manželka",
                                        color = Color(0xFFE040FB),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            
                            val rel = character.getRelationship()
                            if (rel != com.example.haremdark.models.RelStatus.NEUTRAL) {
                                val relColor = when(rel) {
                                    com.example.haremdark.models.RelStatus.BLOOD_SISTER -> Color(0xFFD32F2F)
                                    com.example.haremdark.models.RelStatus.DEVOTED -> Color(0xFF4CAF50)
                                    com.example.haremdark.models.RelStatus.IN_LOVE -> Color(0xFFE91E63)
                                    com.example.haremdark.models.RelStatus.BROKEN -> Color(0xFF9E9E9E)
                                    com.example.haremdark.models.RelStatus.OBEDIENT -> Color(0xFF2196F3)
                                    com.example.haremdark.models.RelStatus.REBELLIOUS -> Color(0xFFFF9800)
                                    else -> Color.Gray
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = relColor.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, relColor)
                                ) {
                                    Text(
                                        text = rel.title,
                                        color = relColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            } else if (character.partnerka) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFF4081).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "♥ Partnerka",
                                        color = Color(0xFFFF4081),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${archetype?.name ?: "Otrokyně"} • ${character.age} let • ${character.role}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                // Favorite Star Icon Button
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (character.oblibena) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Oblíbenkyně",
                        tint = if (character.oblibena) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            // Phase and Loyalty Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Degradation Phase Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Column {
                            Text(
                                text = "Fáze ${character.fazeZkazenosti}: ${phase?.name ?: "Čistá"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Loyalty Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(loyaltyTier.colorHex))
                        )
                        Text(
                            text = "${loyaltyTier.title} (${character.loajalita}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(loyaltyTier.colorHex),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Compact Stats Matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatBadge("Touha", "${character.touha}%", Color(0xFFE91E63), Modifier.weight(1f))
                StatBadge("Submisivita", "${character.submisivita}%", Color(0xFF00E5FF), Modifier.weight(1f))
                StatBadge("Důvěra", "${character.duvera}%", Color(0xFF4CAF50), Modifier.weight(1f))
                StatBadge("Strach", "${character.strach}%", Color(0xFFFF9800), Modifier.weight(1f))
                if (character.broken > 0) {
                    StatBadge("Zlomení", "${character.broken}%", Color(0xFF9C27B0), Modifier.weight(1f))
                }
            }

            // Affinity Tier & Passive Thought Snippet
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(affinityTier.colorHex).copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(affinityTier.colorHex).copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(affinityTier.icon, fontSize = 13.sp)
                    Text(
                        text = "Úr. ${affinityTier.level} ${affinityTier.title}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(affinityTier.colorHex)
                    )
                    Text(
                        text = "• „${AffinityData.getRandomActiveDialogue(character.affinityPoints, character.archetypeId)}“",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Rental Status Banner if on rental
            if (character.naNajmu) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF3E2723)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = null,
                            tint = Color(0xFFFFB74D),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Na nájmu u: ${character.klient ?: "Klient"} (Zbývá ${character.najemZbyvaDni} dní)",
                            color = Color(0xFFFFB74D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Actions Row: Detail & Interact Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDetailClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Podrobnosti", fontSize = 12.sp)
                }

                Button(
                    onClick = onInteractClick,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Interakce", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CharacterGridCard(
    character: Character,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onPinClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val loyaltyTier = StaticData.getLoyaltyTier(character.loajalita)
    val archetype = StaticData.ARCHETYPES[character.archetypeId]
    val portraitRes = StaticData.getPortraitForArchetype(character.archetypeId)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(245.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Character Portrait Image with Coil
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

            // Dynamic Gradient Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color(0x66000000),
                                Color.Transparent,
                                Color(0xD90E0514),
                                Color(0xFB14081E)
                            ),
                            startY = 0f
                        )
                    )
            )

            // Top Badges & Favorite Star
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Status Badges Column
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (character.jeManzelkou) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE040FB).copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = "💍 Manželka",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    } else if (character.partnerka) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFF4081).copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = "♥ Partnerka",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (character.tehotna) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFF80AB).copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = "🤰 Březí (${character.dnyTehotenstvi}d)",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (character.naNajmu) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFB74D).copy(alpha = 0.95f)
                        ) {
                            Text(
                                text = "💰 Nájem (${character.najemZbyvaDni}d)",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    val rel = character.getRelationship()
                    if (rel != com.example.haremdark.models.RelStatus.NEUTRAL) {
                        val relColor = when(rel) {
                            com.example.haremdark.models.RelStatus.BLOOD_SISTER -> Color(0xFFD32F2F)
                            com.example.haremdark.models.RelStatus.DEVOTED -> Color(0xFF4CAF50)
                            com.example.haremdark.models.RelStatus.IN_LOVE -> Color(0xFFE91E63)
                            com.example.haremdark.models.RelStatus.BROKEN -> Color(0xFF9E9E9E)
                            com.example.haremdark.models.RelStatus.OBEDIENT -> Color(0xFF2196F3)
                            com.example.haremdark.models.RelStatus.REBELLIOUS -> Color(0xFFFF9800)
                            else -> Color.Gray
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = relColor.copy(alpha = 0.8f)
                        ) {
                            Text(
                                text = rel.title,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Favorite Toggle Button
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0x88000000), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (character.oblibena) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Oblíbená",
                            tint = if (character.oblibena) Color(0xFFFFD700) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onPinClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0x88000000), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (character.isPinned) Icons.Default.PushPin else Icons.Default.LocationOn,
                            contentDescription = "Připnout",
                            tint = if (character.isPinned) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Bottom Profile Info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = character.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${archetype?.name ?: "Dívka"} • ${character.age} let",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE0E0E0),
                            fontSize = 10.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(loyaltyTier.colorHex).copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(loyaltyTier.colorHex))
                    ) {
                        Text(
                            text = "${character.loajalita}% loajal.",
                            color = Color(loyaltyTier.colorHex),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                // Mini Health & Desire Bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { (character.hp.toFloat() / character.maxHp.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0x44FFFFFF)
                    )
                    LinearProgressIndicator(
                        progress = { (character.touha / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFFE91E63),
                        trackColor = Color(0x44FFFFFF)
                    )
                }

                // Quick Tap Hint
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fáze ${character.fazeZkazenosti}/15",
                        fontSize = 9.sp,
                        color = Color(0xFFCE93D8),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Profil & dary ▸",
                        fontSize = 9.sp,
                        color = Color(0xFFFFB74D),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StatBadge(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
    }
}
