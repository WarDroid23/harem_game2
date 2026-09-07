package com.example.haremdark.ui.components

import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

import androidx.compose.foundation.clickable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.DirectGiftItem
import com.example.haremdark.data.GameContent
import com.example.haremdark.data.GameInteraction
import com.example.haremdark.data.StaticData
import com.example.haremdark.models.Character
import com.example.haremdark.models.InventoryItem
import com.example.haremdark.models.Player
import com.example.haremdark.domain.GameEngine

@Composable
fun CharacterDetailDialog(
    character: Character,
    player: Player,
    onDismiss: () -> Unit,
    onGiveDirectGift: (DirectGiftItem) -> Unit,
    onUseInventoryItem: (InventoryItem) -> Unit,
    onExecuteInteraction: (GameInteraction) -> Unit,
    onCourtRomance: () -> Unit,
    onMarry: () -> Unit,
    onRent: (String, Int) -> Unit,
    onUpgradeSkill: (String) -> Unit,
    onEquipItem: (String, String) -> Unit,
    onUnequipItem: (String) -> Unit
) {
    val loyalty = StaticData.getLoyaltyTier(character.loajalita)
    val archetype = StaticData.ARCHETYPES[character.archetypeId]
    val phase = StaticData.DEGRADATION_PHASES[character.fazeZkazenosti]
    val portraitRes = StaticData.getPortraitForArchetype(character.archetypeId)

    var selectedSection by remember { mutableIntStateOf(0) }
    val sectionTabs = listOf("📊 Profil", "🛡️ Výbava", "💖 Náklonnost", "🎁 Dary", "⚡ Akce", "✨ Dovednosti")
    var activeEmote by remember { mutableStateOf<String?>(null) }
    var emoteKey by remember { mutableLongStateOf(0L) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Hero Portrait Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
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
                                    colors = listOf(
                                        Color(0x33000000),
                                        Color(0x9910061A),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                    )

                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color(0x88000000), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White)
                    }

                    // Title & Badges on Banner
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = character.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (character.oblibena) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFFD700).copy(alpha = 0.85f)
                                ) {
                                    Text(
                                        text = "★ Oblíbenkyně",
                                        color = Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (character.jeManzelkou) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFE040FB).copy(alpha = 0.85f)
                                ) {
                                    Text(
                                        text = "💍 Manželka",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${archetype?.name ?: "Otrokyně"} • ${character.age} let • Fáze ${character.fazeZkazenosti}: ${phase?.name ?: "Poddajná"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Section Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedSection,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sectionTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedSection == index,
                            onClick = { selectedSection = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedSection == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                }

                // Section Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(14.dp)
                ) {
                    when (selectedSection) {
                        0 -> ProfileAndStatsTab(character = character, loyaltyTier = loyalty, archetype = archetype, phase = phase)
                        1 -> EquipmentTab(character = character, player = player, onEquip = onEquipItem, onUnequip = onUnequipItem)
                        2 -> AffinityAndDialogueTab(character = character)
                        3 -> GiftingAndItemsTab(
                            character = character,
                            player = player,
                            onGiveDirectGift = onGiveDirectGift,
                            onUseInventoryItem = onUseInventoryItem
                        )
                        4 -> InteractionsSectionTab(
                            character = character,
                            player = player,
                            onExecuteInteraction = onExecuteInteraction,
                            onCourtRomance = onCourtRomance,
                            onMarry = onMarry,
                            onRent = onRent,
                            onUpgradeSkill = onUpgradeSkill
                        )
                        5 -> SkillTreeTab(character = character, onUpgradeSkill = onUpgradeSkill)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileAndStatsTab(
    character: Character,
    loyaltyTier: com.example.haremdark.models.LoyaltyTier,
    archetype: com.example.haremdark.models.CharacterArchetype?,
    phase: com.example.haremdark.models.DegradationPhase?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Archetype and Phase Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("🎭 Archetyp: ${archetype?.name ?: "Dívka"}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(archetype?.description ?: "Bez popisu", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Text("🔥 Fáze ${character.fazeZkazenosti}: ${phase?.name ?: ""}", fontWeight = FontWeight.Bold, color = Color(0xFFCE93D8))
                Text(phase?.description ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))
            }
        }

        // Loyalty Tier Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("👑 Stupeň oddanosti", fontWeight = FontWeight.Bold, color = Color(loyaltyTier.colorHex))
                    Text("${loyaltyTier.title} (${character.loajalita}%)", fontWeight = FontWeight.Bold, color = Color(loyaltyTier.colorHex))
                }
                LinearProgressIndicator(
                    progress = { (character.loajalita / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(loyaltyTier.colorHex)
                )
                Text(loyaltyTier.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }
        }

        // Quick Affinity Preview Card
        val affinityTier = AffinityData.getTierForPoints(character.affinityPoints)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(affinityTier.icon, fontSize = 14.sp)
                        Text("Náklonnost k pánovi", fontWeight = FontWeight.Bold, color = Color(affinityTier.colorHex))
                    }
                    Text("Úr. ${affinityTier.level} • ${affinityTier.title} (${character.affinityPoints} pts)", fontWeight = FontWeight.Bold, color = Color(affinityTier.colorHex), fontSize = 11.sp)
                }
                LinearProgressIndicator(
                    progress = { (character.affinityPoints % 100 / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(affinityTier.colorHex)
                )
                Text("💭 \"${AffinityData.getRandomActiveDialogue(character.affinityPoints, character.archetypeId)}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f), fontWeight = FontWeight.Medium)
            }
        }

        // Detailed Progress Stats
        Text("Základní vitální ukazatele:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatProgressBar("Životní síla (HP)", character.hp, character.maxHp, Color(0xFF4CAF50))
                StatProgressBar("Touha a vzrušení", character.touha, 100, Color(0xFFE91E63))
                StatProgressBar("Vlhkost a citlivost", character.vlhkost, 100, Color(0xFF00BCD4))
                StatProgressBar("Poslušnost", character.poslusnost, 100, Color(0xFF00E5FF))
                StatProgressBar("Submisivita", character.submisivita, 100, Color(0xFF9C27B0))
                StatProgressBar("Důvěra k pánovi", character.duvera, 100, Color(0xFF8BC34A))
                StatProgressBar("Strach a bázeň", character.strach, 100, Color(0xFFFF9800))
            }
        }

        // Dark Degradation Stats
        Text("Temné modifikátory poddanství:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatProgressBar("Zlomení vůle (Broken)", character.broken, 100, Color(0xFF7E57C2))
                StatProgressBar("Ztráta rozumu (Mindbreak)", character.mindbreak, 100, Color(0xFFD32F2F))
                StatProgressBar("Závislost na bolesti", character.painAddiction, 100, Color(0xFFFF5252))
                StatProgressBar("Hladina ponížení", character.humiliation, 100, Color(0xFFFFA726))
                StatProgressBar("Jizvy a stopy trestu", character.scarred, 100, Color(0xFF8D6E63))
            }
        }

        // Status Highlights
        Text("Doplňující stav:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatRow("Cejch pána na kůži", if (character.ownedMark) "🔥 Vypálen" else "Ne")
                StatRow("Stav těhotenství", if (character.tehotna) "🤰 Březí (Den ${character.dnyTehotenstvi})" else "Ne")
                StatRow("Narozené děti v dominiu", "👶 ${character.deti}")
                StatRow("Stav nájmu", if (character.naNajmu) "💰 Pronajata (${character.klient})" else "V paláci")
            }
        }
    }
}

@Composable
fun AffinityAndDialogueTab(character: Character) {
    val tier = AffinityData.getTierForPoints(character.affinityPoints)
    val nextTier = AffinityData.TIERS.firstOrNull { it.level == tier.level + 1 }
    val progressInTier = if (nextTier != null) {
        val currentSpan = (character.affinityPoints - tier.minPoints).toFloat()
        val totalSpan = (nextTier.minPoints - tier.minPoints).toFloat()
        (currentSpan / totalSpan).coerceIn(0f, 1f)
    } else 1.0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main Affinity Level Status Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(tier.icon, fontSize = 24.sp)
                        Column {
                            Text(
                                text = "Úroveň vztahu ${tier.level}: ${tier.title}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(tier.colorHex)
                            )
                            Text(
                                text = "Celkem bodů náklonnosti: ${character.affinityPoints} pts",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                LinearProgressIndicator(
                    progress = { progressInTier },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(tier.colorHex)
                )

                if (nextTier != null) {
                    val ptsNeeded = nextTier.minPoints - character.affinityPoints
                    Text(
                        text = "Do další úrovně (${nextTier.title}): zbývá $ptsNeeded bodů (daruj dary)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                } else {
                    Text(
                        text = "👑 Dosažena maximální úroveň absolutní oddanosti!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                }
            }
        }

        // Active Speech Dialogue Card
        val activeLine = AffinityData.getRandomActiveDialogue(character.affinityPoints, character.archetypeId)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(tier.colorHex).copy(alpha = 0.12f)
            ),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(tier.colorHex).copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("💬", fontSize = 20.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aktuální pasivní myšlenky k pánovi:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(tier.colorHex)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "„$activeLine“",
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Perks & Relationship Benefits
        Text("✨ Výhody a pasivní bonusy vztahu:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tier.unlockedPerks.forEach { perk ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(tier.colorHex),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = perk,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Unlocked Dialogue Tree Breakdown
        Text("📜 Rejstřík pasivních dialogů:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        AffinityData.TIERS.forEach { t ->
            val isUnlocked = character.affinityPoints >= t.minPoints
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(t.icon, fontSize = 14.sp)
                            Text(
                                text = "Úroveň ${t.level}: ${t.title}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isUnlocked) Color(t.colorHex) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                        if (isUnlocked) {
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF4CAF50).copy(alpha = 0.2f)) {
                                Text("Odemčeno", color = Color(0xFF4CAF50), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        } else {
                            Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)) {
                                Text("Zamčeno (${t.minPoints} pts)", color = MaterialTheme.colorScheme.error, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }

                    if (isUnlocked) {
                        val lines = AffinityData.PASSIVE_DIALOGUES[t.level] ?: emptyList()
                        lines.take(2).forEach { line ->
                            Text(
                                text = "• „$line“",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    } else {
                        Text(
                            text = "🔒 Vyšší intimita a oddanost odhalí hlubší promluvy a tajná přání této dívky.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GiftingAndItemsTab(
    character: Character,
    player: Player,
    onGiveDirectGift: (DirectGiftItem) -> Unit,
    onUseInventoryItem: (InventoryItem) -> Unit
) {
    val affinityTier = AffinityData.getTierForPoints(character.affinityPoints)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Player Wealth Overview Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🪙", fontSize = 18.sp)
                    Column {
                        Text("Pokladnice pána", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text("${player.gold} Zlatých", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700), fontSize = 14.sp)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(affinityTier.colorHex).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${affinityTier.icon} Úr. ${affinityTier.level} (${character.affinityPoints} pts)",
                        color = Color(affinityTier.colorHex),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Section: Direct Purchasable Gifts
        Text("Královské dary za zlato (Zvyšují náklonnost 💖):", fontWeight = FontWeight.Bold, fontSize = 13.sp)

        GameContent.DIRECT_GIFTS.forEach { gift ->
            val canAfford = player.gold >= gift.goldCost
            val affinityBoost = (gift.loyaltyBoost + gift.trustBoost + gift.romanceBoost) / 2 + 10

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(gift.icon, fontSize = 20.sp)
                            Column {
                                Text(gift.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(gift.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                        }
                    }

                    // Stat boosts row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE91E63).copy(alpha = 0.15f)) {
                            Text("+$affinityBoost 💖 Nákl.", color = Color(0xFFE91E63), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF4CAF50).copy(alpha = 0.15f)) {
                            Text("+${gift.loyaltyBoost} Loaj.", color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFF4081).copy(alpha = 0.15f)) {
                            Text("+${gift.romanceBoost} Rom.", color = Color(0xFFFF4081), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }

                    Button(
                        onClick = { onGiveDirectGift(gift) },
                        enabled = canAfford,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8E24AA)
                        )
                    ) {
                        Text("🎁 Darovat (${gift.goldCost} zlatých)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: Inventory Items Gifting
        Text("Lektvary a předměty z inventáře:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

        if (player.items.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "V inventáři nemáš žádné předměty. Můžeš je uvařit v Alchymii (Záložka Aktivity)!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            player.items.forEach { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${item.name} (${item.count}x)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(item.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }

                        Button(
                            onClick = { onUseInventoryItem(item) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Použít", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractionsSectionTab(
    character: Character,
    player: Player,
    onExecuteInteraction: (GameInteraction) -> Unit,
    onCourtRomance: () -> Unit,
    onMarry: () -> Unit,
    onRent: (String, Int) -> Unit,
    onUpgradeSkill: (String) -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("Odměny", "Tresty", "Intimita", "Romance", "Nájem")

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = subTab,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            subTabs.forEachIndexed { idx, title ->
                Tab(
                    selected = subTab == idx,
                    onClick = { subTab = idx },
                    text = { Text(title, fontSize = 11.sp, fontWeight = if (subTab == idx) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (subTab) {
                0 -> InteractionList(GameContent.REWARDS, player, character, onExecuteInteraction)
                1 -> InteractionList(GameContent.PUNISHMENTS, player, character, onExecuteInteraction)
                2 -> InteractionList(GameContent.INTIMATE, player, character, onExecuteInteraction)
                3 -> RelationshipsTab(character, player, onCourtRomance, onMarry)
                4 -> RentalTab(character, onRent)
            }
        }
    }
}

@Composable
fun StatProgressBar(label: String, value: Int, max: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            Text("$value / $max", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
        LinearProgressIndicator(
            progress = { (value.toFloat() / max.toFloat()).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(2.5.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun InteractionDialog(
    character: Character,
    player: Player,
    onDismiss: () -> Unit,
    onExecuteInteraction: (GameInteraction) -> Unit,
    onCourtRomance: () -> Unit,
    onMarry: () -> Unit,
    onRent: (String, Int) -> Unit,
    onUpgradeSkill: (String) -> Unit
) {
    CharacterDetailDialog(
        character = character,
        player = player,
        onDismiss = onDismiss,
        onGiveDirectGift = {},
        onUseInventoryItem = {},
        onExecuteInteraction = onExecuteInteraction,
        onCourtRomance = onCourtRomance,
        onMarry = onMarry,
        onRent = onRent,
        onUpgradeSkill = onUpgradeSkill,
        onEquipItem = { _, _ -> },
        onUnequipItem = { _ -> }
    )
}

@Composable
fun InteractionList(
    interactions: List<GameInteraction>,
    player: Player,
    character: Character,
    onExecute: (GameInteraction) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(interactions) { interaction ->
            val canAffordEnergy = player.sexEnergy >= interaction.energyCost
            val canAffordDark = player.darkEnergy >= interaction.darkCost
            val canAffordGold = player.gold >= interaction.goldCost
            val phaseOk = character.fazeZkazenosti >= interaction.minPhase
            val favOk = !interaction.requiresFavorite || character.oblibena
            val wifeOk = !interaction.requiresWife || character.jeManzelkou
            val enabled = canAffordEnergy && canAffordDark && canAffordGold && phaseOk && favOk && wifeOk

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = interaction.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (interaction.energyCost > 0) {
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE91E63).copy(alpha = 0.2f)) {
                                    Text("⚡ ${interaction.energyCost}", fontSize = 10.sp, color = Color(0xFFE91E63), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                            if (interaction.darkCost > 0) {
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF9C27B0).copy(alpha = 0.2f)) {
                                    Text("🔮 ${interaction.darkCost}", fontSize = 10.sp, color = Color(0xFF9C27B0), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                            if (interaction.goldCost > 0) {
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFFD700).copy(alpha = 0.2f)) {
                                    Text("🪙 ${interaction.goldCost}", fontSize = 10.sp, color = Color(0xFFFFD700), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }

                    Text(
                        text = interaction.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Účinek: ${interaction.effectDescription}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    if (interaction.minPhase > 0) {
                        Text(
                            text = "Vyžaduje Fázi ${interaction.minPhase} (dívka má ${character.fazeZkazenosti})",
                            fontSize = 10.sp,
                            color = if (phaseOk) Color(0xFF4CAF50) else Color(0xFFE53935)
                        )
                    }

                    Button(
                        onClick = { onExecute(interaction) },
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text("Provést akci", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RelationshipsTab(
    character: Character,
    player: Player,
    onCourt: () -> Unit,
    onMarry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Romantické sbližování ♥", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Stav romance: ${character.romanceBody}/100 body",
                    color = Color(0xFFFF4081),
                    fontWeight = FontWeight.Bold
                )
                LinearProgressIndicator(
                    progress = { (character.romanceBody / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFFF4081)
                )
                Text(
                    "Dvořením, dary a soukromými večeřemi prohlubuješ její city. Při 50 bodech se stává Partnerkou.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Button(
                    onClick = onCourt,
                    enabled = player.gold >= 50 && character.romanceBody < 100,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dvořit se & obdarovat (50 zlatých)", fontSize = 12.sp)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Manželský svazek 💍", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    if (character.jeManzelkou) "Již je tvou oficiální Manželkou dominia!"
                    else "Vyžaduje: 80 Romance (máš ${character.romanceBody}) & 70 Loajalita (máš ${character.loajalita}%) & 300 Zlata.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                if (!character.jeManzelkou) {
                    Button(
                        onClick = onMarry,
                        enabled = character.romanceBody >= 80 && character.loajalita >= 70 && player.gold >= 300,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Uzavřít sňatek (300 zlatých)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RentalTab(
    character: Character,
    onRent: (String, Int) -> Unit
) {
    var selectedClient by remember { mutableStateOf("Místní měšťané") }
    var selectedDays by remember { mutableIntStateOf(3) }
    
    val clients = listOf(
        "Místní měšťané" to Pair(20, 10), // (Advance, Daily)
        "Cech bohatých kupců" to Pair(45, 30),
        "Šlechtický dvůr" to Pair(70, 50),
        "Otrokářský syndikát" to Pair(120, 80),
        "Inkviziční legie" to Pair(180, 100)
    )
    val durations = listOf(3, 7, 14)
    val clientData = clients.firstOrNull { it.first == selectedClient }?.second ?: Pair(45, 50)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (character.naNajmu) {
            Text(
                "Dívka je v současnosti pronajata klientovi '${character.klient}'. Zbývá ${character.najemZbyvaDni} dní.",
                color = Color(0xFFFFB74D),
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                "Pronajmi otrokyni vybranému klientovi na stanovený počet dní. Získáš okamžitou zálohu i denní pasivní příjem.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Text("Vyber klienta:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            clients.forEach { (client, rates) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { selectedClient = client }.padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = selectedClient == client,
                        onClick = { selectedClient = client }
                    )
                    Column {
                        Text(client, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Záloha: ${rates.first} zl/den | Denní příjem: ${rates.second} zl/den", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Text("Doba trvání: $selectedDays dní", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                durations.forEach { days ->
                    FilterChip(
                        selected = selectedDays == days,
                        onClick = { selectedDays = days },
                        label = { Text(if(days==3) "Krátkodobý (3 dny)" else if(days==7) "Střednědobý (7 dní)" else "Dlouhodobý (14 dní)", fontSize = 11.sp) }
                    )
                }
            }

            val totalAdvance = selectedDays * clientData.first
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFD700).copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Okamžitá záloha: $totalAdvance zlatých", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                    Text("Každý den nájmu přinese dalších ${clientData.second} zlatých.", fontSize = 11.sp, color = Color(0xFFFFB74D))
                    if (selectedClient == "Otrokářský syndikát" || selectedClient == "Inkviziční legie") {
                        Text("⚠️ Zvýšené riziko! Dívka se může vrátit zraněná.", fontSize = 10.sp, color = Color.Red)
                    }
                }
            }

            Button(
                onClick = { onRent(selectedClient, selectedDays) },
                modifier = Modifier.fillMaxWidth(),
                enabled = character.hp >= 40,
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedClient.contains("Inkviziční") || selectedClient.contains("Syndikát")) Color(0xFFC62828) else MaterialTheme.colorScheme.primary)
            ) {
                Text("Odeslat na nájem", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatRow(name: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SkillTreeTab(character: Character, onUpgradeSkill: (String) -> Unit) {
    var selectedBranch by remember { mutableStateOf("Boj") }
    val branches = listOf("Boj", "Podpora")

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with level, xp, and available points
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Úroveň ${character.level}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("ZK: ${character.xp} / ${character.level * 100}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${character.skillPoints} SP", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Dostupné body", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
            }
            LinearProgressIndicator(
                progress = (character.xp.toFloat() / (character.level * 100).coerceAtLeast(1).toFloat()).coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )
        }

        // Branch Selection
        TabRow(
            selectedTabIndex = branches.indexOf(selectedBranch),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            branches.forEach { branch ->
                Tab(
                    selected = selectedBranch == branch,
                    onClick = { selectedBranch = branch },
                    text = { Text(branch, fontWeight = FontWeight.Bold) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            if (selectedBranch == "Boj") {
                SkillTreeLayout(
                    character = character,
                    onUpgradeSkill = onUpgradeSkill,
                    skills = listOf(
                        SkillNodeData("combat", "Útok", "🗡️", "+5 Poškození v boji", 0),
                        SkillNodeData("defense", "Obrana", "🛡️", "+2 Obrana v boji", 1),
                        SkillNodeData("vitality", "Vitalita", "❤️", "+10 Zdraví", 1),
                        SkillNodeData("bloodlust", "Krvavá žízeň", "🩸", "Šance na krvácení", 2, req = "combat", reqLvl = 3),
                        SkillNodeData("iron_skin", "Železná kůže", "🧱", "Šance blokovat útok", 2, req = "defense", reqLvl = 3)
                    )
                )
            } else {
                SkillTreeLayout(
                    character = character,
                    onUpgradeSkill = onUpgradeSkill,
                    skills = listOf(
                        SkillNodeData("production", "Produkce", "⚒️", "+2% Produkce surovin", 0),
                        SkillNodeData("rental", "Nájmy", "💰", "+15 Zlata z nájmů", 0),
                        SkillNodeData("charm", "Šarm", "✨", "+10% Zisk náklonnosti", 1, req = "rental", reqLvl = 2),
                        SkillNodeData("efficiency", "Efektivita", "⚙️", "Sníží únavu z práce", 1, req = "production", reqLvl = 2),
                        SkillNodeData("loyalty_boost", "Oddanost", "💖", "Zabraňuje ztrátě důvěry", 2, req = "charm", reqLvl = 3)
                    )
                )
            }
        }
    }
}

data class SkillNodeData(
    val id: String,
    val name: String,
    val icon: String,
    val desc: String,
    val tier: Int,
    val req: String? = null,
    val reqLvl: Int = 0
)

@Composable
fun SkillTreeLayout(character: Character, onUpgradeSkill: (String) -> Unit, skills: List<SkillNodeData>) {
    val maxTier = skills.maxOfOrNull { it.tier } ?: 0
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        for (tier in 0..maxTier) {
            val tierSkills = skills.filter { it.tier == tier }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tierSkills.forEach { skill ->
                    val currentLvl = character.skills[skill.id] ?: 0
                    val reqMet = skill.req == null || (character.skills[skill.req] ?: 0) >= skill.reqLvl
                    val canUpgrade = character.skillPoints > 0 && reqMet
                    
                    SkillNode(
                        skill = skill,
                        currentLvl = currentLvl,
                        canUpgrade = canUpgrade,
                        reqMet = reqMet,
                        onUpgrade = { onUpgradeSkill(skill.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SkillNode(
    skill: SkillNodeData,
    currentLvl: Int,
    canUpgrade: Boolean,
    reqMet: Boolean,
    onUpgrade: () -> Unit
) {
    val bgColor = if (currentLvl > 0) MaterialTheme.colorScheme.primary 
                  else if (reqMet) MaterialTheme.colorScheme.surfaceVariant 
                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                  
    val contentColor = if (currentLvl > 0) MaterialTheme.colorScheme.onPrimary 
                       else if (reqMet) MaterialTheme.colorScheme.onSurface 
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = canUpgrade) { onUpgrade() }
            .background(bgColor)
            .padding(8.dp)
    ) {
        Text(skill.icon, fontSize = 24.sp, modifier = Modifier.padding(bottom = 4.dp))
        Text(skill.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = contentColor, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text("Lvl $currentLvl", fontSize = 10.sp, color = contentColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text(skill.desc, fontSize = 9.sp, color = contentColor.copy(alpha = 0.8f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 10.sp)
        
        if (!reqMet) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Vyžaduje:", fontSize = 8.sp, color = MaterialTheme.colorScheme.error)
            Text("${skill.req} Lvl ${skill.reqLvl}", fontSize = 8.sp, color = MaterialTheme.colorScheme.error)
        } else if (canUpgrade) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Vylepšit", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}



@Composable
fun CharacterEquipmentTab(character: Character, player: Player, engine: GameEngine) {
    val equipmentSlots = listOf("weapon" to "Zbraň", "armor" to "Zbroj", "accessory" to "Doplněk")
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🛡️ Bojové statistiky", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val combatBonus = character.equipment.values.filterNotNull().sumOf { it.combatBonus }
                    val defBonus = character.equipment.values.filterNotNull().sumOf { it.defenseBonus }
                    val hpBonus = character.equipment.values.filterNotNull().sumOf { it.hpBonus }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatCounter("HP", "${character.hp}/${character.maxHp + hpBonus} ${if(hpBonus>0) "(+$hpBonus)" else ""}")
                        StatCounter("Boj", "${character.skills["combat"] ?: 0} ${if(combatBonus>0) "(+$combatBonus)" else ""}")
                        StatCounter("Obrana", "${character.skills["defense"] ?: 0} ${if(defBonus>0) "(+$defBonus)" else ""}")
                    }
                }
            }
        }
        
        items(equipmentSlots) { (slotId, slotName) ->
            val equippedItem = character.equipment[slotId]
            val availableItems = player.items.filter { it.category == "equipment" && it.equipSlot == slotId && it.count > 0 }
            
            var showInventoryMenu by remember { mutableStateOf(false) }
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(slotName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    if (equippedItem != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(equippedItem.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(equippedItem.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(equippedItem.effectDescription, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                            Button(
                                onClick = { engine.unequipItemFromCharacter(character.id, slotId) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Odebrat")
                            }
                        }
                    } else {
                        Button(
                            onClick = { showInventoryMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text("Vybavit předmět", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
            
            if (showInventoryMenu) {
                Dialog(onDismissRequest = { showInventoryMenu = false }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Vyber předmět pro: $slotName", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (availableItems.isEmpty()) {
                                Text("Nemáš žádné volné předměty pro tento slot.", modifier = Modifier.padding(16.dp))
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(availableItems) { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable { 
                                                    engine.equipItemToCharacter(character.id, item.id, slotId)
                                                    showInventoryMenu = false
                                                }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(item.icon, fontSize = 24.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text("${item.name} (x${item.count})", fontWeight = FontWeight.Bold)
                                                Text(item.effectDescription, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { showInventoryMenu = false }, modifier = Modifier.align(Alignment.End)) {
                                Text("Zavřít")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AffinityProgressBarComponent(character: Character, modifier: Modifier = Modifier) {
    val tier = AffinityData.getTierForPoints(character.affinityPoints)
    val nextTier = AffinityData.TIERS.firstOrNull { it.level == tier.level + 1 }
    
    val progressInTier = if (nextTier != null) {
        val currentSpan = (character.affinityPoints - tier.minPoints).toFloat()
        val totalSpan = (nextTier.minPoints - tier.minPoints).toFloat()
        (currentSpan / totalSpan).coerceIn(0f, 1f)
    } else 1.0f

    val pointsNeeded = if (nextTier != null) nextTier.minPoints - character.affinityPoints else 0

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(tier.icon, fontSize = 18.sp)
                    Column {
                        Text(
                            text = "Úr. ${tier.level} • ${tier.title}",
                            fontWeight = FontWeight.Bold,
                            color = Color(tier.colorHex),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${character.affinityPoints} bodů náklonnosti",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                if (nextTier != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Do Úr. ${nextTier.level}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Zbývá $pointsNeeded pts",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    Text(
                        text = "👑 Max",
                        fontSize = 12.sp,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Custom Visual Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressInTier)
                        .fillMaxHeight()
                        .background(Color(tier.colorHex))
                )
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "💭 \"${AffinityData.getRandomActiveDialogue(character.affinityPoints, character.archetypeId)}\"", 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f), 
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
fun EquipmentTab(
    character: Character,
    player: Player,
    onEquip: (String, String) -> Unit,
    onUnequip: (String) -> Unit
) {
    val slots = listOf(
        Pair("weapon", "🗡️ Zbraň"),
        Pair("armor", "🛡️ Zbroj"),
        Pair("accessory", "💍 Doplněk")
    )
    
    var expandedSlot by remember { mutableStateOf<String?>(null) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bojová Výbava", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Vybavte dívku nalezenými předměty pro zvýšení jejích šancí v aréně.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                }
            }
        }
        
        items(slots) { (slotId, slotName) ->
            val equippedItem = character.equipment[slotId]
            val isExpanded = expandedSlot == slotId
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedSlot = if (isExpanded) null else slotId }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(slotName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            if (equippedItem != null) {
                                Text(equippedItem.name, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(equippedItem.effectDescription, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            } else {
                                Text("Žádný předmět", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                        
                        if (equippedItem != null) {
                            Button(
                                onClick = { onUnequip(slotId) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Odepnout", fontSize = 11.sp)
                            }
                        } else {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Rozbalit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    if (isExpanded && equippedItem == null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val availableItems = player.items.filter { it.category == "equipment" && it.equipSlot == slotId && it.count > 0 }
                        
                        if (availableItems.isEmpty()) {
                            Text("Nemáte v inventáři žádné vhodné předměty pro tento slot.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                        } else {
                            availableItems.forEach { item ->
                                EquipmentItemRow(item = item, character = character, onEquip = { onEquip(item.id, slotId); expandedSlot = null })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EquipmentItemRow(item: InventoryItem, character: Character, onEquip: () -> Unit) {
    // Calculate stat diffs
    val baseHp = character.maxHp
    val hpBonus = character.equipment.values.filterNotNull().sumOf { it.hpBonus } + ((character.skills["vitality"] ?: 0) * 10)
    val totalHp = baseHp + hpBonus
    
    val combatSkill = character.skills["combat"] ?: 0
    val combatBonus = character.equipment.values.filterNotNull().sumOf { it.combatBonus }
    var totalCombat = combatSkill + combatBonus + (combatSkill * 5)
    if (character.archetypeId in listOf("odvazna", "vzdorna", "krvava_subka", "zlomena")) {
        totalCombat = (totalCombat * 1.2).toInt()
    }
    
    val defSkill = character.skills["defense"] ?: 0
    val defBonus = character.equipment.values.filterNotNull().sumOf { it.defenseBonus }
    val totalDef = defSkill + defBonus + (defSkill * 2)
    
    // New stats
    val newHp = totalHp + item.hpBonus
    
    var newCombat = combatSkill + combatBonus + item.combatBonus + (combatSkill * 5)
    if (character.archetypeId in listOf("odvazna", "vzdorna", "krvava_subka", "zlomena")) {
        newCombat = (newCombat * 1.2).toInt()
    }
    
    val newDef = totalDef + item.defenseBonus
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("${item.icon} ${item.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(item.effectDescription, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            
            // Stat diff tooltip
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                if (item.hpBonus > 0) StatDiff("HP", totalHp, newHp)
                if (item.combatBonus > 0) StatDiff("Boj", totalCombat, newCombat)
                if (item.defenseBonus > 0) StatDiff("Obrana", totalDef, newDef)
            }
        }
        
        Button(
            onClick = onEquip,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text("Vybavit", fontSize = 11.sp)
        }
    }
}

@Composable
fun StatDiff(label: String, oldVal: Int, newVal: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$label: $oldVal ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(10.dp))
        Text(" $newVal", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun FloatingEmoteAnimation(emote: String) {
    val offsetY = remember { androidx.compose.animation.core.Animatable(50f) }
    val alpha = remember { androidx.compose.animation.core.Animatable(0f) }
    
    LaunchedEffect(Unit) {
        launch {
            alpha.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(300))
            delay(800)
            alpha.animateTo(0f, animationSpec = androidx.compose.animation.core.tween(400))
        }
        launch {
            offsetY.animateTo(-40f, animationSpec = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.LinearOutSlowInEasing))
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emote,
            fontSize = 42.sp,
            modifier = Modifier.offset(y = offsetY.value.dp).alpha(alpha.value),
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                    blurRadius = 4f
                )
            )
        )
    }
}
