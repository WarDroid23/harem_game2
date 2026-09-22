package com.example.haremdark.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.*
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave
import com.example.haremdark.ui.screens.AttributeDisplayCard

@Composable
fun RecruitModalDialog(
    gameState: GameSave,
    engine: GameEngine,
    onDismiss: () -> Unit,
    onCharacterDiscovered: (Character) -> Unit
) {
    val context = LocalContext.current
    val player = gameState.player
    val currentPopulation = gameState.characters.size
    val maxPopulation = player.maxPopulation
    val isHaremFull = currentPopulation >= maxPopulation

    var selectedTierId by remember { mutableStateOf("basic") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .widthIn(max = 600.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("⛓️", fontSize = 20.sp)
                            }
                        }
                        Column {
                            Text(
                                "Nábor & Objevování",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Rozšiř své dominium o nové dcery",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Resource & Capacity Status Bar
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Gold
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("💰", fontSize = 14.sp)
                                Text(
                                    "${player.gold}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFFFD700)
                                )
                            }
                            // Mana
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("🔮", fontSize = 14.sp)
                                Text(
                                    "${player.mana}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFCE93D8)
                                )
                            }
                        }

                        // Population
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("👥", fontSize = 14.sp)
                            Text(
                                "$currentPopulation / $maxPopulation",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isHaremFull) Color.Red else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (isHaremFull) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Text(
                                "Kapacita harému je naplněna! Vylepši sídlo pro další místa.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Recruitment Tiers Scrollable List
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CharacterGenerator.RECRUIT_TIERS.forEach { tier ->
                        val isSelected = selectedTierId == tier.id
                        val canAfford = player.gold >= tier.goldCost && player.mana >= tier.manaCost
                        val tierColor = Color(tier.colorHex)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    HapticManager.vibrateClick()
                                    selectedTierId = tier.id
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) tierColor.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) tierColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(tier.icon, fontSize = 26.sp)
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    tier.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = tierColor.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        tier.badge,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = tierColor
                                                    )
                                                }
                                            }
                                            Text(
                                                tier.subtitle,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Cost display
                                    Column(horizontalAlignment = Alignment.End) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("💰 ${tier.goldCost}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (player.gold >= tier.goldCost) Color(0xFFFFD700) else Color.Red)
                                        }
                                        if (tier.manaCost > 0) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("🔮 ${tier.manaCost}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (player.mana >= tier.manaCost) Color(0xFFCE93D8) else Color.Red)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    tier.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    lineHeight = 15.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Rarity expectations & odds badge
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    border = BorderStroke(0.5.dp, tierColor.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Šance na kvalitu:",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            tier.rarityDescription,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = tierColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Button to trigger recruitment
                val currentSelectedTier = CharacterGenerator.RECRUIT_TIERS.find { it.id == selectedTierId }
                    ?: CharacterGenerator.RECRUIT_TIERS.first()
                val canAffordSelected = player.gold >= currentSelectedTier.goldCost && player.mana >= currentSelectedTier.manaCost
                val isButtonEnabled = canAffordSelected && !isHaremFull

                Button(
                    onClick = {
                        HapticManager.vibrateHeavy()
                        SoundEffectManager.playEvent(EventSound.EVENT_SUMMON)
                        val result = engine.recruitWithRng(currentSelectedTier.id)
                        if (result.success && result.character != null) {
                            onCharacterDiscovered(result.character)
                        } else {
                            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isButtonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Text(
                            text = if (isHaremFull) "Harém je plný"
                            else if (!canAffordSelected) "Nedostatek surovin"
                            else "Objevit dívku (${currentSelectedTier.title})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterDiscoveryDialog(
    character: Character,
    onDismiss: () -> Unit,
    onRecruitAnother: () -> Unit
) {
    val archetype = remember(character.archetypeId) {
        StaticData.ARCHETYPES[character.archetypeId]
    }
    val affinityTier = remember(character.affinityPoints) {
        AffinityData.getTierForPoints(character.affinityPoints)
    }
    val portraitRes = remember(character.archetypeId) {
        StaticData.getPortraitForArchetype(character.archetypeId)
    }

    val rarityColor = when (character.rarity) {
        5 -> Color(0xFFFF1744) // Mythic Red
        4 -> Color(0xFFFFD700) // Legendary Gold
        3 -> Color(0xFFBA68C8) // Epic Purple
        2 -> Color(0xFF4FC3F7) // Rare Cyan
        else -> Color(0xFF81C784) // Common Green
    }

    val rarityTitle = when (character.rarity) {
        5 -> "MYTICKÁ POSTAVA (5★)"
        4 -> "LEGENDÁRNÍ POSTAVA (4★)"
        3 -> "EPICKÁ POSTAVA (3★)"
        2 -> "VZÁCNÁ POSTAVA (2★)"
        else -> "BĚŽNÁ POSTAVA (1★)"
    }

    // Animation scale for reveal impact
    val scaleAnim = remember { Animatable(0.75f) }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 520.dp)
                .scale(scaleAnim.value),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(2.dp, rarityColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Banner: Discovery Title & Stars
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = rarityColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "✨ $rarityTitle ✨",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = rarityColor,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Portrait with glowing border
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(12.dp, CircleShape, spotColor = rarityColor)
                ) {
                    Image(
                        painter = painterResource(id = portraitRes),
                        contentDescription = character.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(3.dp, rarityColor, CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-4).dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.5.dp, rarityColor)
                    ) {
                        Text(
                            text = character.statusIcon,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Name, Role & Archetype
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${archetype?.name ?: "Neznámý archetyp"} • ${character.age} let • ${character.role}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                if (archetype?.description?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${archetype.description}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Attributes Card (Rolled with RNG)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "ZÍSKANÉ ZÁKLADNÍ ATRIBUTY (RNG)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Strength
                            AttributeDisplayCard(
                                label = "Síla",
                                value = "${character.effectiveStrength}",
                                subtext = "Boj",
                                icon = Icons.Default.Bolt,
                                color = Color(0xFFFF5722),
                                progress = (character.effectiveStrength.toFloat() / 100f).coerceIn(0f, 1f),
                                modifier = Modifier.weight(1f)
                            )
                            // Loyalty
                            AttributeDisplayCard(
                                label = "Loajalita",
                                value = "${character.effectiveLoyalty}%",
                                subtext = "Věrnost",
                                icon = Icons.Default.Shield,
                                color = Color(0xFF4CAF50),
                                progress = (character.effectiveLoyalty.toFloat() / 100f).coerceIn(0f, 1f),
                                modifier = Modifier.weight(1f)
                            )
                            // Affection
                            AttributeDisplayCard(
                                label = "Náklonnost",
                                value = "${character.effectiveAffection}%",
                                subtext = "Srdce",
                                icon = Icons.Default.Favorite,
                                color = Color(0xFFE91E63),
                                progress = (character.effectiveAffection.toFloat() / 100f).coerceIn(0f, 1f),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Traits & Personality Tags
                if (character.traits.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        character.traits.take(3).forEach { trait ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = trait,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onRecruitAnother,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Další nábor", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Přidat do rosteru", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
