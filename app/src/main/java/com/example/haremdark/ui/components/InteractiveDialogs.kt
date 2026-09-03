package com.example.haremdark.ui.components
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.DirectGiftItem
import com.example.haremdark.data.GameContent
import com.example.haremdark.data.GameInteraction
import com.example.haremdark.data.StaticData
import com.example.haremdark.models.Character
import com.example.haremdark.models.InventoryItem
import com.example.haremdark.models.Player

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
    onRent: (String, Int) -> Unit
) {
    val loyalty = StaticData.getLoyaltyTier(character.loajalita)
    val archetype = StaticData.ARCHETYPES[character.archetypeId]
    val phase = StaticData.DEGRADATION_PHASES[character.fazeZkazenosti]
    val portraitRes = StaticData.getPortraitForArchetype(character.archetypeId)

    var selectedSection by remember { mutableIntStateOf(0) }
    val sectionTabs = listOf("📊 Profil", "💖 Náklonnost", "🎁 Dary", "⚡ Akce")

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
                    Image(
                        painter = painterResource(id = portraitRes),
                        contentDescription = character.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
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
                        1 -> AffinityAndDialogueTab(character = character)
                        2 -> GiftingAndItemsTab(
                            character = character,
                            player = player,
                            onGiveDirectGift = onGiveDirectGift,
                            onUseInventoryItem = onUseInventoryItem
                        )
                        3 -> InteractionsSectionTab(
                            character = character,
                            player = player,
                            onExecuteInteraction = onExecuteInteraction,
                            onCourtRomance = onCourtRomance,
                            onMarry = onMarry,
                            onRent = onRent
                        )
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
    onRent: (String, Int) -> Unit
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
    onRent: (String, Int) -> Unit
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
        onRent = onRent
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
