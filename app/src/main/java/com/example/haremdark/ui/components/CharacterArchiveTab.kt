package com.example.haremdark.ui.components

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.ArchiveEmotionInfo
import com.example.haremdark.data.CharacterArchiveCatalog
import com.example.haremdark.data.CharacterVoiceLine
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.Character
import com.example.haremdark.models.Player
import java.util.Locale

/**
 * TextToSpeech engine helper state for playing Czech voice lines.
 */
@Composable
fun rememberTtsEngine(): TextToSpeechEngineState {
    val context = LocalContext.current
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var isInitialized by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val localeResult = tts?.setLanguage(Locale("cs", "CZ"))
                if (localeResult == TextToSpeech.LANG_MISSING_DATA || localeResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.getDefault())
                }
                isInitialized = true
                ttsInstance = tts
            }
        }

        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    return remember(ttsInstance, isInitialized) {
        TextToSpeechEngineState(context, ttsInstance, isInitialized)
    }
}

class TextToSpeechEngineState(
    private val context: Context,
    private val tts: TextToSpeech?,
    val isInitialized: Boolean
) {
    var currentlyPlayingId by mutableStateOf<String?>(null)

    fun speak(lineId: String, text: String, pitch: Float = 1.0f, speechRate: Float = 0.95f) {
        if (tts != null && isInitialized) {
            tts.stop()
            tts.setPitch(pitch)
            tts.setSpeechRate(speechRate)
            currentlyPlayingId = lineId
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, lineId)
        } else {
            Toast.makeText(context, "🗣️ Hlas: \"$text\"", Toast.LENGTH_SHORT).show()
        }
    }

    fun stop() {
        tts?.stop()
        currentlyPlayingId = null
    }
}

/**
 * Full Character Archives tab for viewing unlocked voice lines & Lottie emotion animations.
 */
@Composable
fun CharacterArchiveTab(
    character: Character,
    player: Player,
    engine: GameEngine?,
    onTriggerEmotion: ((CharacterEmotionType) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ttsState = rememberTtsEngine()

    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = Voice Lines, 1 = Lottie Emotions
    var activeArchiveEmotionKey by remember { mutableLongStateOf(0L) }
    var activeArchiveEmotion by remember { mutableStateOf(CharacterEmotionType.BLUSH) }

    // Search & Filter State
    var searchQuery by remember { mutableStateOf("") }
    var selectedEmotionFilter by remember { mutableStateOf<CharacterEmotionType?>(null) }
    var selectedArchetypeFilter by remember { mutableStateOf<String?>(null) } // null = All
    var showAllCatalogLines by remember { mutableStateOf(false) }

    val allVoiceLines = remember(showAllCatalogLines, character.archetypeId) {
        if (showAllCatalogLines) {
            CharacterArchiveCatalog.ALL_VOICE_LINES
        } else {
            CharacterArchiveCatalog.getVoiceLinesForCharacter(character)
        }
    }
    val allEmotions = CharacterArchiveCatalog.EMOTIONS_ARCHIVE

    // Filter voice lines based on searchQuery, selectedEmotionFilter, selectedArchetypeFilter
    val filteredVoiceLines = remember(
        allVoiceLines,
        searchQuery,
        selectedEmotionFilter,
        selectedArchetypeFilter,
        character
    ) {
        val query = searchQuery.trim().lowercase()

        allVoiceLines.filter { line ->
            // 1. Emotion filter chip
            val matchesEmotionChip = selectedEmotionFilter == null || line.emotionTrigger == selectedEmotionFilter

            // 2. Archetype filter chip
            val matchesArchetypeChip = selectedArchetypeFilter == null ||
                    line.archetypeId == null ||
                    line.archetypeId.equals(selectedArchetypeFilter, ignoreCase = true)

            // 3. Search query matching keyword, emotion, or character name
            val matchesQuery = if (query.isEmpty()) {
                true
            } else {
                // Keyword matches in title, text, or category
                val titleMatch = line.title.lowercase().contains(query)
                val textMatch = line.czechText.lowercase().contains(query)
                val categoryMatch = line.category.lowercase().contains(query)

                // Emotional state matches
                val emotionDisplayNameMatch = line.emotionTrigger.displayName.lowercase().contains(query)
                val emotionEnumMatch = line.emotionTrigger.name.lowercase().contains(query)
                val emotionEmojiMatch = line.emotionTrigger.emoji.contains(query)

                // Character name / Archetype matches
                val archetypeNames = when (line.archetypeId?.lowercase()) {
                    "subka" -> listOf("subka", "submisivní", "submisivni", "aria", "dívka")
                    "valkyra" -> listOf("valkyra", "valkýra", "bojovnice", "freya", "hrdá")
                    "knezka" -> listOf("knezka", "kněžka", "temná", "astarte")
                    else -> listOf("všechny", "vsechny", "univerzální", "univerzalni", "general")
                }
                val archetypeMatch = archetypeNames.any { it.contains(query) || query.contains(it) }

                val currentCharacterNameMatch = character.name.lowercase().contains(query) &&
                        (line.archetypeId == null || line.archetypeId.equals(character.archetypeId, ignoreCase = true))

                titleMatch || textMatch || categoryMatch || emotionDisplayNameMatch || emotionEnumMatch || emotionEmojiMatch || archetypeMatch || currentCharacterNameMatch
            }

            matchesEmotionChip && matchesArchetypeChip && matchesQuery
        }
    }

    val unlockedVoiceLinesCount = allVoiceLines.count { line ->
        line.requiredAffinityLevel <= character.affinityLevel && character.loajalita >= line.requiredLoyalty
    }
    val unlockedEmotionsCount = allEmotions.count { emotion ->
        emotion.requiredAffinityLevel <= character.affinityLevel
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Character Portrait & Active Emotion Showcase Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F152B)),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0xFFFF4081), Color(0xFF7C4DFF))))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Interactive Preview Avatar with Lottie Emotion Overlay
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFFF4081).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .background(Color(0xFF2D1E40)),
                    contentAlignment = Alignment.Center
                ) {
                    val avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80"
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = character.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )

                    // Active Lottie Overlay
                    LottieEmotionOverlay(
                        triggerKey = activeArchiveEmotionKey,
                        emotionType = activeArchiveEmotion,
                        sizeDp = 100.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📜 Archiv Dívky ${character.name}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "Poslouchej odemčené hlasové projevy a zkoumej emoční Lottie reakce.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = Color(0xFF7C4DFF).copy(alpha = 0.25f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFF7C4DFF))
                        ) {
                            Text(
                                text = "🎙️ $unlockedVoiceLinesCount/${allVoiceLines.size} Hlasů",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE040FB),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            color = Color(0xFFFF4081).copy(alpha = 0.25f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF4081))
                        ) {
                            Text(
                                text = "✨ $unlockedEmotionsCount/${allEmotions.size} Emočních Lottie",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF80AB),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Sub-Tab Switcher
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = Color(0xFF231934),
            contentColor = Color(0xFFFF4081),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = {
                    Text(
                        "🎙️ Hlasové Projevy (${filteredVoiceLines.size}/${allVoiceLines.size})",
                        fontSize = 11.sp,
                        fontWeight = if (activeSubTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = {
                    Text(
                        "✨ Lottie Reakce (${allEmotions.size})",
                        fontSize = 11.sp,
                        fontWeight = if (activeSubTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

        // Sub-Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                0 -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 🔍 SEARCH BAR & FILTER SECTION
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E152A)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Search Bar
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            "🔍 Hledat (klíčové slovo, emoce, jméno...)",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.5f)
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Hledat",
                                            tint = Color(0xFFFF4081),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Vymazat",
                                                    tint = Color.White.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF140D1F),
                                        unfocusedContainerColor = Color(0xFF140D1F),
                                        focusedBorderColor = Color(0xFFFF4081),
                                        unfocusedBorderColor = Color(0xFF7C4DFF).copy(alpha = 0.5f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                // Filter Chips Row 1: Emotional State
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Emoce:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF80AB)
                                    )
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        item {
                                            FilterChip(
                                                selected = selectedEmotionFilter == null,
                                                onClick = { selectedEmotionFilter = null },
                                                label = { Text("Všechny emoce", fontSize = 9.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFFFF4081),
                                                    selectedLabelColor = Color.White,
                                                    containerColor = Color(0xFF2B1C3F),
                                                    labelColor = Color.White.copy(alpha = 0.7f)
                                                ),
                                                modifier = Modifier.height(26.dp)
                                            )
                                        }
                                        items(CharacterEmotionType.values()) { emotion ->
                                            FilterChip(
                                                selected = selectedEmotionFilter == emotion,
                                                onClick = {
                                                    selectedEmotionFilter = if (selectedEmotionFilter == emotion) null else emotion
                                                },
                                                label = { Text("${emotion.emoji} ${emotion.displayName}", fontSize = 9.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFFE040FB),
                                                    selectedLabelColor = Color.White,
                                                    containerColor = Color(0xFF2B1C3F),
                                                    labelColor = Color.White.copy(alpha = 0.7f)
                                                ),
                                                modifier = Modifier.height(26.dp)
                                            )
                                        }
                                    }
                                }

                                // Filter Chips Row 2: Character Archetype / Catalog Toggle
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Postava:",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD1C4E9)
                                        )
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            item {
                                                FilterChip(
                                                    selected = selectedArchetypeFilter == null,
                                                    onClick = { selectedArchetypeFilter = null },
                                                    label = { Text("Všechny", fontSize = 9.sp) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = Color(0xFF7C4DFF),
                                                        selectedLabelColor = Color.White,
                                                        containerColor = Color(0xFF2B1C3F),
                                                        labelColor = Color.White.copy(alpha = 0.7f)
                                                    ),
                                                    modifier = Modifier.height(26.dp)
                                                )
                                            }
                                            item {
                                                FilterChip(
                                                    selected = selectedArchetypeFilter == "subka",
                                                    onClick = { selectedArchetypeFilter = if (selectedArchetypeFilter == "subka") null else "subka" },
                                                    label = { Text("⛓️ Subka", fontSize = 9.sp) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = Color(0xFF7C4DFF),
                                                        selectedLabelColor = Color.White,
                                                        containerColor = Color(0xFF2B1C3F),
                                                        labelColor = Color.White.copy(alpha = 0.7f)
                                                    ),
                                                    modifier = Modifier.height(26.dp)
                                                )
                                            }
                                            item {
                                                FilterChip(
                                                    selected = selectedArchetypeFilter == "valkyra",
                                                    onClick = { selectedArchetypeFilter = if (selectedArchetypeFilter == "valkyra") null else "valkyra" },
                                                    label = { Text("⚔️ Valkýra", fontSize = 9.sp) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = Color(0xFF7C4DFF),
                                                        selectedLabelColor = Color.White,
                                                        containerColor = Color(0xFF2B1C3F),
                                                        labelColor = Color.White.copy(alpha = 0.7f)
                                                    ),
                                                    modifier = Modifier.height(26.dp)
                                                )
                                            }
                                            item {
                                                FilterChip(
                                                    selected = selectedArchetypeFilter == "knezka",
                                                    onClick = { selectedArchetypeFilter = if (selectedArchetypeFilter == "knezka") null else "knezka" },
                                                    label = { Text("🔮 Kněžka", fontSize = 9.sp) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = Color(0xFF7C4DFF),
                                                        selectedLabelColor = Color.White,
                                                        containerColor = Color(0xFF2B1C3F),
                                                        labelColor = Color.White.copy(alpha = 0.7f)
                                                    ),
                                                    modifier = Modifier.height(26.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Toggle catalog view
                                    TextButton(
                                        onClick = { showAllCatalogLines = !showAllCatalogLines },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) {
                                        Text(
                                            text = if (showAllCatalogLines) "👤 Jen ${character.name}" else "🌐 Celý Archiv",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF80AB)
                                        )
                                    }
                                }

                                // Active Filter Status & Clear Button
                                if (searchQuery.isNotEmpty() || selectedEmotionFilter != null || selectedArchetypeFilter != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🔍 Nalezeno ${filteredVoiceLines.size} z ${allVoiceLines.size} hlasů",
                                            fontSize = 10.sp,
                                            color = Color(0xFFFF80AB),
                                            fontWeight = FontWeight.Bold
                                        )

                                        TextButton(
                                            onClick = {
                                                searchQuery = ""
                                                selectedEmotionFilter = null
                                                selectedArchetypeFilter = null
                                            },
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier.height(20.dp)
                                        ) {
                                            Text(
                                                text = "✕ Vyčistit filtr",
                                                fontSize = 9.sp,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Voice Lines List
                        if (filteredVoiceLines.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("🔎 Žádné hlasové projevy neodpovídají tvému hledání.", fontSize = 12.sp, color = Color.Gray)
                                    Text("Zkus změnit klíčové slovo, vybranou emoci nebo jméno postavy.", fontSize = 10.sp, color = Color.Gray.copy(alpha = 0.7f))
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredVoiceLines, key = { it.id }) { line ->
                                    val isUnlocked = line.requiredAffinityLevel <= character.affinityLevel && character.loajalita >= line.requiredLoyalty
                                    val isPlaying = ttsState.currentlyPlayingId == line.id

                                    ArchiveVoiceLineCard(
                                        voiceLine = line,
                                        currentCharacter = character,
                                        isUnlocked = isUnlocked,
                                        isPlaying = isPlaying,
                                        onPlayClick = {
                                            if (isUnlocked) {
                                                if (isPlaying) {
                                                    ttsState.stop()
                                                } else {
                                                    ttsState.speak(line.id, line.czechText, line.pitch, line.speechRate)
                                                    activeArchiveEmotion = line.emotionTrigger
                                                    activeArchiveEmotionKey = System.currentTimeMillis()
                                                    onTriggerEmotion?.invoke(line.emotionTrigger)
                                                }
                                            } else {
                                                val reqTier = AffinityData.TIERS.find { it.level == line.requiredAffinityLevel } ?: AffinityData.TIERS.first()
                                                Toast.makeText(
                                                    context,
                                                    "🔒 Hlas uzamčen! Vyžaduje Affinity Úroveň ${line.requiredAffinityLevel} (${reqTier.title}) & Loajalita ${line.requiredLoyalty}.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        onTriggerLottie = {
                                            if (isUnlocked) {
                                                activeArchiveEmotion = line.emotionTrigger
                                                activeArchiveEmotionKey = System.currentTimeMillis()
                                                onTriggerEmotion?.invoke(line.emotionTrigger)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Lottie Emotion Showcase Gallery
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allEmotions, key = { it.emotionType.name }) { emotionInfo ->
                            val isUnlocked = emotionInfo.requiredAffinityLevel <= character.affinityLevel

                            ArchiveEmotionCard(
                                emotionInfo = emotionInfo,
                                isUnlocked = isUnlocked,
                                onClick = {
                                    if (isUnlocked) {
                                        activeArchiveEmotion = emotionInfo.emotionType
                                        activeArchiveEmotionKey = System.currentTimeMillis()
                                        onTriggerEmotion?.invoke(emotionInfo.emotionType)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "🔒 Animace vyžaduje Affinity Úroveň ${emotionInfo.requiredAffinityLevel}!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchiveVoiceLineCard(
    voiceLine: CharacterVoiceLine,
    currentCharacter: Character? = null,
    isUnlocked: Boolean,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onTriggerLottie: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) Color(0xFF341E47) else if (isUnlocked) Color(0xFF251A33) else Color(0xFF191422)
        ),
        border = BorderStroke(
            1.dp,
            if (isPlaying) Color(0xFFE040FB) else if (isUnlocked) Color(0xFFFF4081).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)
        )
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(voiceLine.emotionTrigger.emoji, fontSize = 16.sp)
                    Text(
                        text = voiceLine.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Emotion Badge
                    Surface(
                        color = Color(0xFFFF4081).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${voiceLine.emotionTrigger.emoji} ${voiceLine.emotionTrigger.displayName}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF80AB),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    // Character / Archetype Badge
                    val archLabel = when (voiceLine.archetypeId?.lowercase()) {
                        "subka" -> "⛓️ Subka"
                        "valkyra" -> "⚔️ Valkýra"
                        "knezka" -> "🔮 Kněžka"
                        else -> "🌟 Všechny"
                    }
                    Surface(
                        color = Color(0xFF7C4DFF).copy(alpha = 0.25f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = archLabel,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE040FB),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    // Category Tag
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = voiceLine.category,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD1C4E9),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = if (isUnlocked) "\"${voiceLine.czechText}\"" else "🔒 Tento hlasový projev je dosud uzamčen v paměti.",
                fontSize = 11.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = if (isUnlocked) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.4f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isUnlocked) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onPlayClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlaying) Color(0xFFE040FB) else Color(0xFFFF4081)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPlaying) "Zastavit" else "▶ Přehrát Hlas",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = onTriggerLottie,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp),
                            border = BorderStroke(1.dp, Color(0xFF7C4DFF))
                        ) {
                            Text(
                                text = "🎬 Animace Lottie",
                                fontSize = 10.sp,
                                color = Color(0xFFD1C4E9)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Vyžaduje Affinity Úroveň ${voiceLine.requiredAffinityLevel} & Loajalitu ${voiceLine.requiredLoyalty}",
                        fontSize = 10.sp,
                        color = Color(0xFFFF80AB)
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveEmotionCard(
    emotionInfo: ArchiveEmotionInfo,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFF281C38) else Color(0xFF171320)
        ),
        border = BorderStroke(
            1.dp,
            if (isUnlocked) Color(0xFFFF4081).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) Color(0xFFFF4081).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isUnlocked) emotionInfo.emotionType.emoji else "🔒",
                    fontSize = 22.sp
                )
            }

            Text(
                text = emotionInfo.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Text(
                text = emotionInfo.description,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Surface(
                color = if (isUnlocked) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (isUnlocked) "▶ PŘEHRÁT" else "🔒 Úroveň ${emotionInfo.requiredAffinityLevel}",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) Color(0xFF81C784) else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
