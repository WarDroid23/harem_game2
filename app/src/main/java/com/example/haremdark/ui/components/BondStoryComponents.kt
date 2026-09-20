package com.example.haremdark.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.BondStoryCatalog
import com.example.haremdark.data.BondStoryChoice
import com.example.haremdark.data.BondStoryEpisode
import com.example.haremdark.data.BondStoryPage
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.VoiceManager
import com.example.haremdark.domain.VoiceAssetManager
import com.example.haremdark.models.Character
import com.example.haremdark.models.Player
import com.example.haremdark.models.addAffinityHistory

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.haremdark.ui.components.LoreCatalog
import com.example.haremdark.ui.components.LoreTooltip
import com.example.haremdark.ui.components.LoreEntry

/**
 * Main Bond Story tab component integrated into character detail modal.
 */
@Composable
fun BondStoryTab(
    character: Character,
    player: Player,
    engine: GameEngine?,
    onTriggerEmotion: ((CharacterEmotionType) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allEpisodes = remember(character.archetypeId) {
        BondStoryCatalog.getAllStoriesForCharacter(character)
    }

    var selectedActiveEpisode by remember { mutableStateOf<BondStoryEpisode?>(null) }
    var activeLoreEntry by remember { mutableStateOf<LoreEntry?>(null) }

    val completedIds = character.completedBondStories
    val currentAffinityLevel = character.affinityLevel

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Banner Overview Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A1B3D)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFFF4081), Color(0xFF880E4F))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📖", fontSize = 24.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Příběhy Pouta a Vzpomínky",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Odemkni intímní kapitoly příběhu zvyšováním Affinity s dívkou ${character.name}.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val unlockedCount = allEpisodes.count { it.requiredAffinityLevel <= currentAffinityLevel && character.loajalita >= it.requiredLoyalty }
                        val completedCount = allEpisodes.count { completedIds.contains(it.id) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { if (allEpisodes.isNotEmpty()) completedCount.toFloat() / allEpisodes.size else 0f },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFFFF4081),
                                trackColor = Color.White.copy(alpha = 0.15f)
                            )
                            Text(
                                text = "$completedCount/${allEpisodes.size} Dokončeno",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF80AB)
                            )
                        }
                    }
                }
            }

            // List of Episodes
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allEpisodes, key = { it.id }) { ep ->
                    val isUnlocked = ep.requiredAffinityLevel <= currentAffinityLevel && character.loajalita >= ep.requiredLoyalty
                    val isCompleted = completedIds.contains(ep.id)

                    BondStoryEpisodeCard(
                        episode = ep,
                        isUnlocked = isUnlocked,
                        isCompleted = isCompleted,
                        onClick = {
                            if (isUnlocked) {
                                selectedActiveEpisode = ep
                            } else {
                                val reqTier = AffinityData.TIERS.find { it.level == ep.requiredAffinityLevel } ?: AffinityData.TIERS.first()
                                Toast.makeText(
                                    context,
                                    "🔒 Vyžaduje Affinity Úroveň ${ep.requiredAffinityLevel} (${reqTier.title}) & Loajalita ${ep.requiredLoyalty}!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }
        }

        // Interactive Reader Modal Dialog
        selectedActiveEpisode?.let { episode ->
            BondStoryReaderModal(
                character = character,
                player = player,
                episode = episode,
                engine = engine,
                onTriggerEmotion = onTriggerEmotion,
                onShowLore = { lore -> activeLoreEntry = lore },
                onDismiss = { selectedActiveEpisode = null },
                onCompleted = {
                    character.completedBondStories.add(episode.id)
                    selectedActiveEpisode = null
                }
            )
        }

        activeLoreEntry?.let { lore ->
            LoreTooltip(lore = lore, onDismiss = { activeLoreEntry = null })
        }
    }
}

@Composable
private fun BondStoryEpisodeCard(
    episode: BondStoryEpisode,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val cardBg = when {
        isCompleted -> Color(0xFF1E281F)
        isUnlocked -> Color(0xFF281C34)
        else -> Color(0xFF1B1822)
    }

    val borderColor = when {
        isCompleted -> Color(0xFF4CAF50).copy(alpha = 0.6f)
        isUnlocked -> Color(0xFFFF4081).copy(alpha = 0.8f)
        else -> Color.White.copy(alpha = 0.12f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) Color(0xFFFF4081).copy(alpha = 0.25f)
                        else Color.White.copy(alpha = 0.05f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isUnlocked) episode.icon else "🔒",
                    fontSize = 22.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Kapitola ${episode.episodeNumber}: ${episode.title}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.5f)
                    )

                    if (isCompleted) {
                        Surface(
                            color = Color(0xFF2E7D32),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "DOKONČENO",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    } else if (isUnlocked) {
                        Surface(
                            color = Color(0xFFC2185B),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "NAPSÁNO",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = episode.summary,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💰 +${episode.goldReward} Gold",
                        fontSize = 10.sp,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "💖 +${episode.affinityBonusReward} Affinity",
                        fontSize = 10.sp,
                        color = Color(0xFFFF80AB),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "✨ ${episode.statBonusDescription}",
                        fontSize = 10.sp,
                        color = Color(0xFFB388FF),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                imageVector = if (isUnlocked) Icons.Default.ChevronRight else Icons.Default.Lock,
                contentDescription = null,
                tint = if (isUnlocked) Color(0xFFFF4081) else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Fullscreen / modal Bond Story reader with Lottie emotion triggers and narrative choices.
 */
@Composable
fun BondStoryReaderModal(
    character: Character,
    player: Player,
    episode: BondStoryEpisode,
    engine: GameEngine?,
    onTriggerEmotion: ((CharacterEmotionType) -> Unit)?,
    onShowLore: (LoreEntry) -> Unit,
    onDismiss: () -> Unit,
    onCompleted: () -> Unit
) {
    var currentPageIdx by remember { mutableIntStateOf(0) }
    var emotionKey by remember { mutableLongStateOf(0L) }
    var activeEmotion by remember { mutableStateOf(CharacterEmotionType.LOVE) }
    var choiceMadeFeedback by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val pages = episode.pages
    val currentPage = pages.getOrNull(currentPageIdx) ?: pages.last()

    // Trigger emotion overlay whenever speaker emotion changes
    LaunchedEffect(currentPageIdx) {
        activeEmotion = currentPage.speakerEmotion
        emotionKey = System.currentTimeMillis()
        onTriggerEmotion?.invoke(currentPage.speakerEmotion)
    }

    LaunchedEffect(currentPageIdx, activeEmotion) {
        // Stop current TTS or Asset when moving to next page
        VoiceAssetManager.stopPlayback()
        VoiceManager.stop()

        val currentPage = pages[currentPageIdx]
        
        // Check for High Quality Audio Asset first
        if (VoiceAssetManager.hasAssetFor(episode.id, currentPageIdx, activeEmotion)) {
            VoiceAssetManager.playVoiceAsset(
                context = context,
                episodeId = episode.id,
                pageIndex = currentPageIdx,
                mood = activeEmotion,
                customVoiceId = currentPage.voiceLineId
            )
        } else {
            // Fallback to TTS Voice Bark if it's a character speaking
            if (currentPage.speakerName != "Vypravěč" && currentPage.speakerName != "Ty (Pán)") {
                VoiceManager.speakDialogue(
                    speakerName = currentPage.speakerName,
                    dialogueText = currentPage.text.replace("{NAME}", character.name),
                    archetype = character.archetypeId
                )
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0B18)),
            color = Color(0xFF0F0B18)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar (Static)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(episode.icon, fontSize = 20.sp)
                            Column {
                                Text(
                                    text = episode.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Strana ${currentPageIdx + 1} / ${pages.size}",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFF80AB)
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Zavřít",
                                tint = Color.White
                            )
                        }
                    }

                    // Animated content for page switching
                    AnimatedContent(
                        targetState = currentPageIdx,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width / 3 } + fadeIn(animationSpec = tween(400)))
                                    .togetherWith(slideOutHorizontally { width -> -width / 3 } + fadeOut(animationSpec = tween(400)))
                            } else {
                                (slideInHorizontally { width -> -width / 3 } + fadeIn(animationSpec = tween(400)))
                                    .togetherWith(slideOutHorizontally { width -> width / 3 } + fadeOut(animationSpec = tween(400)))
                            }.using(SizeTransform(clip = false))
                        },
                        label = "PageTransition",
                        modifier = Modifier.weight(1f)
                    ) { targetIdx ->
                        val targetPage = pages.getOrNull(targetIdx) ?: pages.last()
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Character Portrait with Lottie Emotion Reaction Overlay
                            Box(
                                modifier = Modifier
                                    .weight(0.42f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(BorderStroke(1.5.dp, Brush.linearGradient(listOf(Color(0xFFFF4081), Color(0xFF7C4DFF)))))
                                    .background(Color(0xFF1E142B)),
                                contentAlignment = Alignment.Center
                            ) {
                                val avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80"

                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = character.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )

                                // Vignette Shading Overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color(0xFF0F0B18).copy(alpha = 0.85f)
                                                )
                                            )
                                        )
                                )

                                // Active Lottie Animation Overlay
                                LottieEmotionOverlay(
                                    triggerKey = emotionKey,
                                    emotionType = activeEmotion,
                                    sizeDp = 180.dp,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Speaker Badge Name Tag
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp),
                                    color = Color(0xFF7C4DFF).copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(activeEmotion.emoji, fontSize = 14.sp)
                                        Text(
                                            text = targetPage.speakerName.replace("{NAME}", character.name),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Story Narrative Dialogue Box
                            Card(
                                modifier = Modifier
                                    .weight(0.42f)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D162A)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = targetPage.text.replace("{NAME}", character.name),
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        color = Color.White.copy(alpha = 0.95f),
                                        modifier = Modifier.pointerInput(targetPage.text) {
                                            detectTapGestures(
                                                onLongPress = {
                                                    val lore = LoreCatalog.findRelevantLore(targetPage.text)
                                                    if (lore != null) {
                                                        onShowLore(lore)
                                                    } else {
                                                        onShowLore(
                                                            LoreEntry(
                                                                "Příběh",
                                                                "Ozvěny minulosti",
                                                                "Tento dialog odhaluje střípky z minulosti postavy ${character.name}. Pozorně naslouchej a sleduj její reakce.",
                                                                "🕯️"
                                                            )
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    )

                                    choiceMadeFeedback?.let { feedback ->
                                        Surface(
                                            color = Color(0xFF2E7D32).copy(alpha = 0.85f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = feedback,
                                                fontSize = 11.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }

                                    // Interactive Choices or Navigation Buttons
                                    if (targetPage.choices.isNotEmpty() && choiceMadeFeedback == null) {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            targetPage.choices.forEach { choice ->
                                                Button(
                                                    onClick = {
                                                        choiceMadeFeedback = choice.responseText
                                                        activeEmotion = choice.emotionTrigger
                                                        emotionKey = System.currentTimeMillis()
                                                        onTriggerEmotion?.invoke(choice.emotionTrigger)

                                                        // Grant Extra Affinity
                                                        character.affinityPoints += choice.extraAffinity
                                                        if (choice.extraAffinity > 0) {
                                                            character.addAffinityHistory(player.day, "Volba v příběhu: ${choice.optionText}")
                                                            com.example.haremdark.domain.VoiceManager.playTriggerVoice(
                                                                com.example.haremdark.domain.VoiceTriggerType.STORY_MILESTONE,
                                                                character
                                                            )
                                                        }
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F1D56)),
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.5f))
                                                ) {
                                                    Text(
                                                        text = choice.optionText,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (currentPageIdx > 0) {
                                                OutlinedButton(
                                                    onClick = {
                                                        choiceMadeFeedback = null
                                                        currentPageIdx--
                                                    },
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("⬅ Zpět", fontSize = 12.sp, color = Color.White)
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.width(1.dp))
                                            }

                                            if (currentPageIdx < pages.size - 1) {
                                                Button(
                                                    onClick = {
                                                        choiceMadeFeedback = null
                                                        currentPageIdx++
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Pokračovat ➔", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            } else {
                                                // Claim Final Story Completion Reward
                                                Button(
                                                    onClick = {
                                                        player.gold += episode.goldReward
                                                        player.darkEnergy += episode.darkPowerReward
                                                        character.affinityPoints += episode.affinityBonusReward
                                                        character.addAffinityHistory(player.day, "Příběh dokončen: ${episode.title}")

                                                        com.example.haremdark.domain.VoiceManager.playTriggerVoice(
                                                            com.example.haremdark.domain.VoiceTriggerType.AFFINITY_LEVEL_UP,
                                                            character
                                                        )

                                                        Toast.makeText(
                                                            context,
                                                            "🎉 Příběh dokončen! +${episode.goldReward} Gold, +${episode.affinityBonusReward} Affinity!",
                                                            Toast.LENGTH_LONG
                                                        ).show()

                                                        onCompleted()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("🎁 Vybrat Odměnu & Dokončit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
