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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.*
import kotlin.random.Random
import com.example.haremdark.data.AffinityData
import com.airbnb.lottie.compose.*
import com.example.haremdark.data.CharacterSkillCatalog
import com.example.haremdark.data.CharacterSkillNode
import com.example.haremdark.data.SkillNodeType
import com.example.haremdark.data.DirectGiftItem
import com.example.haremdark.data.GameContent
import com.example.haremdark.data.GameInteraction
import com.example.haremdark.data.StaticData
import com.example.haremdark.models.Character
import com.example.haremdark.models.InventoryItem
import com.example.haremdark.models.InteractionLogEntry
import com.example.haremdark.models.getSafeInteractionLogs
import com.example.haremdark.models.getSafeMoraleTrend
import com.example.haremdark.models.getDefaultTrainingPresets
import com.example.haremdark.models.TrainingPreset
import com.example.haremdark.models.TrainingPresetAction
import com.example.haremdark.models.MoraleRecord
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.example.haremdark.models.KeyMemory
import com.example.haremdark.models.Player
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.VoiceManager
import com.example.haremdark.domain.VoiceTriggerType
import com.example.haremdark.models.EquipmentLoadout
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.BorderStroke
import android.widget.Toast

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
    onUnequipItem: (String) -> Unit,
    engine: GameEngine? = null,
    initialTab: Int = 0
) {
    val context = LocalContext.current
    var currentActiveCharacter by remember(character.id) { mutableStateOf(character) }

    val loyalty = StaticData.getLoyaltyTier(currentActiveCharacter.loajalita)
    val archetype = StaticData.ARCHETYPES[currentActiveCharacter.archetypeId]
    val phase = StaticData.DEGRADATION_PHASES[currentActiveCharacter.fazeZkazenosti]
    val portraitRes = StaticData.getPortraitForArchetype(currentActiveCharacter.archetypeId)

    var selectedSection by remember { mutableIntStateOf(initialTab) }
    val sectionTabs = listOf("📖 Životopis", "📊 Profil", "🛡️ Výbava", "💖 Náklonnost", "📖 Příběhy Pouta", "🎙️ Archiv & Hlasy", "🎁 Dary", "📦 Sklad & Inventář", "⚡ Akce", "✨ Dovednosti", "🎯 Výcvik", "📋 Úkoly", "🕰️ Klíčové Momenty", "🖼️ Galerie", "🏆 Milníky", "📜 Historie")
    var activeEmote by remember { mutableStateOf<String?>(null) }
    var emoteKey by remember { mutableLongStateOf(0L) }

    // Lottie Animated Emotion Reactions (Blushing, Cheering, Love, Shy, Sparkle)
    var emotionTriggerKey by remember(character.id) { mutableLongStateOf(0L) }
    var activeEmotionType by remember { mutableStateOf(CharacterEmotionType.BLUSH) }

    val triggerEmotionReaction: (CharacterEmotionType) -> Unit = { emotion ->
        activeEmotionType = emotion
        emotionTriggerKey = System.currentTimeMillis()
    }

    // Interactive Particle Burst Effect for High-Affinity Actions
    var particleBurstTrigger by remember(character.id) { mutableLongStateOf(0L) }
    var particleBurstType by remember { mutableStateOf(AffinityBurstType.LOVE_BURST) }
    var particleIntensity by remember { mutableFloatStateOf(1.0f) }
    var previousAffinityPoints by remember(character.id) { mutableIntStateOf(currentActiveCharacter.affinityPoints) }

    val triggerAffinityEffect: (AffinityBurstType, Float) -> Unit = { type, intensity ->
        particleBurstType = type
        particleIntensity = intensity
        particleBurstTrigger = System.currentTimeMillis()
        when (type) {
            AffinityBurstType.HEARTS -> triggerEmotionReaction(CharacterEmotionType.BLUSH)
            AffinityBurstType.SPARKLES -> triggerEmotionReaction(CharacterEmotionType.CHEER)
            AffinityBurstType.LOVE_BURST -> triggerEmotionReaction(CharacterEmotionType.LOVE)
            AffinityBurstType.DEVOTION_GOLD -> triggerEmotionReaction(CharacterEmotionType.SPARKLE)
        }
    }

    LaunchedEffect(currentActiveCharacter.affinityPoints, currentActiveCharacter.affinityLevel) {
        if (currentActiveCharacter.affinityPoints > previousAffinityPoints) {
            val isHighAffinity = (currentActiveCharacter.affinityPoints - previousAffinityPoints) >= 15 || currentActiveCharacter.affinityLevel >= 4
            particleBurstType = if (isHighAffinity) AffinityBurstType.LOVE_BURST else AffinityBurstType.HEARTS
            particleIntensity = if (isHighAffinity) 1.4f else 1.0f
            particleBurstTrigger = System.currentTimeMillis()
        }
        previousAffinityPoints = currentActiveCharacter.affinityPoints
    }

    val offsetY = remember { androidx.compose.animation.core.Animatable(150f) }
    val alphaVal = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(character) {
        currentActiveCharacter = character
    }

    LaunchedEffect(currentActiveCharacter.id) {
        launch {
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = 0.72f,
                    stiffness = 250f
                )
            )
        }
        launch {
            alphaVal.animateTo(
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 250
                )
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .graphicsLayer(
                    translationY = offsetY.value,
                    alpha = alphaVal.value
                ),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Hero Portrait Banner with Dynamic Mood & Affinity Background
                val moodTheme = MoodThemeResolver.resolve(currentActiveCharacter)
                val transition = rememberInfiniteTransition(label = "HeroMoodParticle")
                val particleProgress by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(4000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "HeroParticleProg"
                )
                val pulseScale by transition.animateFloat(
                    initialValue = 0.85f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "HeroPulseScale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(138.dp)
                ) {
                    // Dynamic Mood Background Asset Layer
                    Image(
                        painter = painterResource(id = moodTheme.backgroundDrawableRes),
                        contentDescription = "Atmosféra: ${moodTheme.moodTitle}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Character Portrait overlay with subtle transparency blend
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(portraitRes)
                            .crossfade(true)
                            .build(),
                        contentDescription = currentActiveCharacter.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.85f),
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = moodTheme.primaryAccent,
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

                    // Dynamic Mood Gradient Tint
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x33000000),
                                        moodTheme.ambientGlowColor.copy(alpha = 0.35f),
                                        Color(0xBB10061A),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                    )

                    // Dynamic Mood Particle Layer (Floating hearts, embers, celestial stars)
                    MoodParticleCanvas(
                        particleType = moodTheme.particleType,
                        particleColor = moodTheme.particleColor,
                        progress = particleProgress,
                        pulseScale = pulseScale,
                        modifier = Modifier.fillMaxSize()
                    )

                    // High-Affinity Interaction Particle Burst Overlay (Hearts & Sparkles)
                    AffinityParticleOverlay(
                        triggerKey = particleBurstTrigger,
                        burstType = particleBurstType,
                        intensity = particleIntensity,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Lottie Emotional Reaction Overlay (Blushing, Cheering, Love, Shy, Sparkles)
                    LottieEmotionOverlay(
                        triggerKey = emotionTriggerKey,
                        emotionType = activeEmotionType,
                        sizeDp = 180.dp,
                        modifier = Modifier.fillMaxSize()
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
                                text = currentActiveCharacter.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(currentActiveCharacter.statusIcon, fontSize = 14.sp)
                                    Text(
                                        text = currentActiveCharacter.nalada,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            if (currentActiveCharacter.breakthroughActive) {
                                val breakthroughLabel = when(currentActiveCharacter.breakthroughType) {
                                    "combat_fury" -> "🔥 Zuřivost"
                                    "iron_will" -> "🛡️ Vůle"
                                    "shadow_step" -> "💨 Úhyb"
                                    else -> "✨ Průlom"
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFFFFD700))
                                ) {
                                    Text(
                                        breakthroughLabel,
                                        color = Color(0xFFFFD700),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (currentActiveCharacter.oblibena) {
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
                            if (currentActiveCharacter.jeManzelkou) {
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
                            text = "${archetype?.name ?: "Otrokyně"} • ${currentActiveCharacter.age} let • Fáze ${currentActiveCharacter.fazeZkazenosti}: ${phase?.name ?: "Poddajná"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // --- VISUAL PROGRESS BARS FOR ALL PARTY MEMBERS (Health, Mana, Affinity) ---
                val allCharacters = engine?.gameState?.value?.characters ?: listOf(character)
                PartyMembersStatusBar(
                    characters = allCharacters,
                    player = player,
                    activeCharacter = currentActiveCharacter,
                    onSelectCharacter = { selected ->
                        currentActiveCharacter = selected
                    },
                    onTogglePartyMember = { charId ->
                        engine?.let { eng ->
                            val (success, msg) = eng.togglePartyMember(charId)
                            if (msg.isNotBlank()) {
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )

                // High-Fidelity Detailed Visual Progress Bars for the Active Party Member
                PartyMemberDetailedProgressBarsCard(
                    character = currentActiveCharacter,
                    player = player,
                    onToggleParty = {
                        engine?.let { eng ->
                            val (success, msg) = eng.togglePartyMember(currentActiveCharacter.id)
                            if (msg.isNotBlank()) {
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )

                // Dynamic Mood Resonance & Atmosphere Banner
                MoodAtmosphereBanner(
                    character = currentActiveCharacter,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                LowMoraleWarningBanner(
                    character = currentActiveCharacter,
                    onOpenTraining = { selectedSection = 8 }
                )

                // Section Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedSection,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 8.dp,
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
                        .padding(10.dp)
                ) {
                    when (selectedSection) {
                        0 -> BioTab(character = currentActiveCharacter)
                        1 -> ProfileAndStatsTab(character = currentActiveCharacter, loyaltyTier = loyalty, archetype = archetype, phase = phase)
                        2 -> EquipmentTab(character = currentActiveCharacter, player = player, onEquip = onEquipItem, onUnequip = onUnequipItem, engine = engine)
                        3 -> AffinityAndDialogueTab(character = currentActiveCharacter, engine = engine, onTriggerAffinityEffect = triggerAffinityEffect)
                        4 -> BondStoryTab(
                            character = currentActiveCharacter,
                            player = player,
                            engine = engine,
                            onTriggerEmotion = triggerEmotionReaction
                        )
                        5 -> CharacterArchiveTab(
                            character = currentActiveCharacter,
                            player = player,
                            engine = engine,
                            onTriggerEmotion = triggerEmotionReaction
                        )
                        6 -> GiftingAndItemsTab(
                            character = currentActiveCharacter,
                            player = player,
                            onGiveDirectGift = { gift ->
                                triggerAffinityEffect(AffinityBurstType.LOVE_BURST, 1.25f)
                                onGiveDirectGift(gift)
                            },
                            onUseInventoryItem = onUseInventoryItem,
                            engine = engine,
                            onTriggerAffinityEffect = triggerAffinityEffect
                        )
                        7 -> {
                            if (engine != null) {
                                InventoryManagementPanel(
                                    gameState = engine.gameState.value,
                                    engine = engine,
                                    activeCharacter = currentActiveCharacter,
                                    onUseItemOnCharacter = { item ->
                                        onUseInventoryItem(item)
                                    }
                                )
                            } else {
                                GiftingAndItemsTab(
                                    character = currentActiveCharacter,
                                    player = player,
                                    onGiveDirectGift = { gift ->
                                        triggerAffinityEffect(AffinityBurstType.LOVE_BURST, 1.25f)
                                        onGiveDirectGift(gift)
                                    },
                                    onUseInventoryItem = onUseInventoryItem,
                                    engine = engine,
                                    onTriggerAffinityEffect = triggerAffinityEffect
                                )
                            }
                        }
                        8 -> InteractionsSectionTab(
                            character = currentActiveCharacter,
                            player = player,
                            onExecuteInteraction = { inter ->
                                if (inter.type == "odmena" || inter.type == "intimni") {
                                    triggerAffinityEffect(AffinityBurstType.HEARTS, 1.2f)
                                }
                                onExecuteInteraction(inter)
                            },
                            onCourtRomance = {
                                triggerAffinityEffect(AffinityBurstType.DEVOTION_GOLD, 1.5f)
                                onCourtRomance()
                            },
                            onMarry = {
                                triggerAffinityEffect(AffinityBurstType.DEVOTION_GOLD, 2.0f)
                                onMarry()
                            },
                            onRent = onRent,
                            onUpgradeSkill = onUpgradeSkill
                        )
                        9 -> SkillTreeTab(
                            character = currentActiveCharacter,
                            engine = engine,
                            onUpgradeSkill = onUpgradeSkill
                        )
                        10 -> TrainingMiniGameComponent(
                            character = currentActiveCharacter,
                            engine = engine
                        )
                        11 -> DailyAssignmentsTab(
                            character = currentActiveCharacter,
                            engine = engine
                        )
                        12 -> KeyMomentsTab(
                            character = currentActiveCharacter
                        )
                        13 -> MemoryGalleryTab(character = currentActiveCharacter)
                        14 -> MilestonesTab(character = currentActiveCharacter)
                        15 -> SlaveInteractionLogTab(
                            character = currentActiveCharacter,
                            engine = engine
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BioTab(character: Character) {
    val bio = remember(character.archetypeId) {
        when (character.archetypeId) {
            "subka" -> CharacterBio(
                lore = "Pochází z chudého pohraničí. Byla prodána do područí dominia, aby zachránila svou rodinu před hladem. Postupem času v sobě objevila hlubokou potřebu sloužit pevné a nekompromisní ruce, která jí dává pocit bezpečí a řádu.",
                traits = listOf("Poddajná 🌸", "Hledající uznání ✨", "Citlivá ❤️", "Oddaná 🤝"),
                goals = listOf("Najít absolutní bezpečí v tvém stínu 🌌", "Stát se tvou nejvěrnější služebnicí 📜", "Získat uznání ostatních dívek v harému 👑")
            )
            "slechticna" -> CharacterBio(
                lore = "Bývalá dcera vlivného barona ze severního království. Její rod byl svržen inkvizicí a ona sama byla uvržena do otroctví. I v řetězech si zachovává svou aristokratickou hrdost, pýchu a dokonalé společenské vystupování. Pohrdá slabostí.",
                traits = listOf("Arogantní 💎", "Ambiciózní ⚡", "Inteligentní 🧠", "Hrdá 👑"),
                goals = listOf("Pomstít se zrádcům jejího rodu ⚔️", "Získat zpět svůj ztracený vliv skrz tvou moc 💍", "Dosáhnout výsadního postavení v harému 🏰")
            )
            "touha" -> CharacterBio(
                lore = "Bývalá společnice z hlavního města, která dokonale ovládá umění svádění a jemné manipulace. Její přítomnost je jako horký vítr, který rozpaluje vášně. Pod maskou sebejistoty však skrývá hluboký strach ze samoty a odmítnutí.",
                traits = listOf("Smyslná 🔥", "Provokativní 💋", "Hravá 🎭", "Vnitřně nejistá 🩹"),
                goals = listOf("Získat tvou plnou, exkluzivní pozornost 💖", "Ovládnout umění stínové magie rozkoše 🌌", "Stát se tvým osobním klenotem 💎")
            )
            "odvazna" -> CharacterBio(
                lore = "Bojovnice z divokých kmenů jihu, zajatá během pohraničních válek. Považuje tě za nepřítele, ale hluboce respektuje čest, odvahu a bojovou sílu. Pohrdá zbabělostí.",
                traits = listOf("Divoká 🦁", "Čestná 🛡️", "Fyzicky zdatná ⚔️", "Nedůvěřivá 👁️"),
                goals = listOf("Dokázat svou hodnotu v bitvách po tvém boku 🛡️", "Nalézt pána, kterého může skutečně respektovat 👑", "Ochránit své spolubojovnice v dominiu 🤝")
            )
            "sukuba" -> CharacterBio(
                lore = "Démon stínů vyvolaný z hlubin Pekla. Původně plánovala vysát tvou životní energii, ale tvá pevná vůle a temná magie ji spoutaly. Nyní ji fascinuje tvá nadvláda.",
                traits = listOf("Démonická 😈", "Manipulativní 🧶", "Nenasytná 👅", "Věrná pod tlakem ⛓️"),
                goals = listOf("Pohltit tvou temnou energii k posílení své moci ⚡", "Podmanit si mysli tvých nepřátel 🧠", "Učinit z tvého lože oltář rozkoše 🌙")
            )
            "draci_divka" -> CharacterBio(
                lore = "Poslední dědička prastarého dračího klanu, který byl vyhlazen lovci. Její krev žhne horkostí a její tělo zdobí jemné šupiny. Její loajalita je absolutní, jakmile ji získáš.",
                traits = listOf("Horkokrevná 🌋", "Povýšená 💅", "Nezlomná ✊", "Ochránitelská 🛡️"),
                goals = listOf("Obnovit slávu dračího klanu 🐉", "Najít partnera s dostatečně silným plamenem 🔥", "Spálit všechny nepřátele tvého dominia ☄️")
            )
            "nymfomanka" -> CharacterBio(
                lore = "Mladá dívka stižená kletbou neutišitelné touhy. Její vlastní tělo je jejím vězením. Hledá v tobě zachránce i přísného vůdce, který dokáže usměrnit její neovladatelné impulsy.",
                traits = listOf("Obsedantní 🌀", "Plachá mimo ložnici 😳", "Neustále vzrušená 🌡️", "Vděčná 🙏"),
                goals = listOf("Dosáhnout absolutního uspokojení pod tvým vedením 🌹", "Naučit se ovládat své tělesné touhy 🛑", "Sloužit ti bez ohledu na vlastní stud ⛓️")
            )
            "ticha_panenka" -> CharacterBio(
                lore = "Dívka, která po prožitém traumatu téměř ztratila řeč. Je jako tichý stín v tvém dominiu. Její poslušnost je naprosto bezmezná, komunikuje pouze pohledy a gesty.",
                traits = listOf("Mlčenlivá 🤫", "Dokonale poslušná 🧘", "Nenápadná 👤", "Křehká 🕊️"),
                goals = listOf("Nalézt mír a ochranu před světem 🌿", "Sloužit ti bez kladení jakýchkoli otázek 🔗", "Být ti tiše nablízku ✨")
            )
            "krvava_subka" -> CharacterBio(
                lore = "Bolest a rozkoš jsou pro ni nerozlučně spojeny. Hledá pána, který dokáže otestovat její hranice a uvolnit její vnitřní plamen skrze utrpení.",
                traits = listOf("Masochistická 🩸", "Extatická ☄️", "Dychtivá 🐾", "Nekontrolovatelná 🌪️"),
                goals = listOf("Otestovat hranice své bolesti 🩹", "Být potrestána za každou maličkost ⛓️", "Odevzdat svou krev svému vládci 🩸")
            )
            "posedla" -> CharacterBio(
                lore = "Něco hluboko v ní se zlomilo. Cítí se jako prázdná skořápka, kterou může naplnit pouze tvá mocná temná vůle. Chce být tvým nástrojem.",
                traits = listOf("Prázdná 🫙", "Vnímavá 📡", "Stínová 👤", "Fixovaná 🎯"),
                goals = listOf("Ztratit vlastní identitu ve tvé vůli 🔗", "Být dokonale naplněna tvou energií ⚡", "Sloužit jako tvé prodloužené rameno stínů 👁️")
            )
            else -> CharacterBio(
                lore = "Dívka se silným odhodláním přizpůsobit se novému životu v temném dominiu. Hledá své místo pod sluncem a snaží se pochopit záměry svého vládce.",
                traits = listOf("Přizpůsobivá 🛠️", "Opatrná 🐾", "Pozorující 👁️"),
                goals = listOf("Přežít v tomto drsném světě 🛡️", "Pochopit povahu svého pána 🧠", "Nalézt přátele mezi ostatními dívkami 🤝")
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Portrait & Relationship Status Card (Dynamic Badge & Frame)
        val tier = AffinityData.getTierForPoints(character.affinityPoints)
        val tierColor = Color(tier.colorHex)
        val portraitRes = StaticData.getPortraitForArchetype(character.archetypeId)

        // Dynamic border styling based on tier level
        val (borderWidth, borderBrush, frameGlow) = remember(tier.level) {
            when {
                tier.level >= 4 -> Triple(
                    4.dp,
                    Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFFF4081), Color(0xFFFFD700))),
                    Color(0xFFFF4081).copy(alpha = 0.3f)
                )
                tier.level >= 2 -> Triple(
                    2.5.dp,
                    Brush.linearGradient(listOf(Color(0xFFE0E0E0), Color(0xFF81C784), Color(0xFFE0E0E0))),
                    Color(0xFF81C784).copy(alpha = 0.15f)
                )
                else -> Triple(
                    1.5.dp,
                    Brush.linearGradient(listOf(Color(0xFFCD7F32), Color(0xFF9E9E9E), Color(0xFFCD7F32))),
                    Color.Transparent
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Portrait with Frame
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer {
                            shadowElevation = if (frameGlow != Color.Transparent) 12f else 0f
                            spotShadowColor = frameGlow
                            ambientShadowColor = frameGlow
                        }
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .border(borderWidth, borderBrush, CircleShape)
                        .padding(borderWidth + 1.dp)
                        .clip(CircleShape)
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(portraitRes)
                            .crossfade(true)
                            .build(),
                        contentDescription = character.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Info & Badge Column
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = character.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Relationship Tier Badge
                    Surface(
                        color = tierColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, tierColor.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(tier.icon, fontSize = 12.sp)
                            Text(
                                text = "Úroveň ${tier.level}: ${tier.title}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = tierColor
                            )
                        }
                    }

                    // Mood Indicator Badge
                    val (moodText, moodColor, moodIcon) = remember(character.nalada) {
                        when (character.nalada.lowercase()) {
                            "veselá", "happy" -> Triple("Veselá (+25% dárky)", Color(0xFF81C784), "🌟")
                            "rozmarná", "playful" -> Triple("Rozmarná (+10% dárky)", Color(0xFF64B5F6), "🍓")
                            "znuděná", "bored" -> Triple("Znuděná (-20% dárky)", Color(0xFFFFB74D), "💤")
                            "rozzlobená", "angry" -> Triple("Rozzlobená (-40% dárky)", Color(0xFFE57373), "⚡")
                            else -> Triple("Neutrální", Color(0xFF9E9E9E), "😐")
                        }
                    }

                    Surface(
                        color = moodColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, moodColor.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(moodIcon, fontSize = 12.sp)
                            Text(
                                text = "Nálada: $moodText",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = moodColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Progress Bar of points inside current level
                    val nextTier = AffinityData.TIERS.firstOrNull { it.level == tier.level + 1 }
                    val progress = if (nextTier != null) {
                        val currentSpan = (character.affinityPoints - tier.minPoints).toFloat()
                        val totalSpan = (nextTier.minPoints - tier.minPoints).toFloat()
                        (currentSpan / totalSpan).coerceIn(0f, 1f)
                    } else 1.0f

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💖 Náklonnost",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "${character.affinityPoints} / ${nextTier?.minPoints ?: "Max"}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = tierColor
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            color = tierColor,
                            trackColor = tierColor.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }

        // Unique Lore Card
        var selectedGlossaryTerm by remember { mutableStateOf<GlossaryTerm?>(null) }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📖 Osobní historie a původ",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                InteractiveCodexContent(
                    text = bio.lore,
                    onTermClick = { term ->
                        selectedGlossaryTerm = GLOSSARY_TERMS.find { it.term.equals(term, ignoreCase = true) }
                    },
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Glossary Popup
        if (selectedGlossaryTerm != null) {
            AlertDialog(
                onDismissRequest = { selectedGlossaryTerm = null },
                icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text(selectedGlossaryTerm!!.term, fontWeight = FontWeight.Bold) },
                text = { Text(selectedGlossaryTerm!!.definition, fontSize = 14.sp, lineHeight = 20.sp) },
                confirmButton = {
                    TextButton(onClick = { selectedGlossaryTerm = null }) {
                        Text("Zavřít")
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        // Personality Traits Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎭 Osobnostní rysy",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFCE93D8)
                )
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val displayTraits = if (character.traits.isNotEmpty()) {
                        character.traits.map { 
                            when(it) {
                                "Arogantní" -> "Arogantní 💎"
                                "Povýšená" -> "Povýšená 👑"
                                "Pracovitá" -> "Pracovitá ⚡"
                                "Líná" -> "Líná 💤"
                                "Týmová hráčka" -> "Týmová hráčka 🤝"
                                "Samotářka" -> "Samotářka 🧊"
                                "Mírná" -> "Mírná 🌸"
                                else -> "$it 🎭"
                            }
                        }
                    } else {
                        bio.traits
                    }
                    items(displayTraits) { trait ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(trait, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // Personal Goals Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎯 Osobní cíle v dominiu",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF81C784)
                )
                bio.goals.forEach { goal ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("•", fontWeight = FontWeight.Bold, color = Color(0xFF81C784), fontSize = 16.sp)
                        Text(
                            text = goal,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Relationship History Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "📜 Historie vztahu a rozhodnutí",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF64B5F6)
                )

                if (character.relationshipHistory.isEmpty()) {
                    Text(
                        text = "Zatím jste neučinili žádná významná rozhodnutí v rozhovorech s touto dívkou. Navštiv záložku Náklonnost a zahaj s ní interaktivní rozhovor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        lineHeight = 18.sp
                    )
                } else {
                    character.relationshipHistory.forEach { record ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Den ${record.day} • Volba Pána",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64B5F6)
                                    )
                                    if (record.outcomeEffects.isNotEmpty()) {
                                        Text(
                                            text = record.outcomeEffects,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                
                                Text(
                                    text = "„${record.prompt}“",
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("👑", fontSize = 12.sp)
                                    Column {
                                        Text(
                                            text = "Tvá odpověď:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = record.choice,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("💬", fontSize = 12.sp)
                                    Column {
                                        Text(
                                            text = "Reakce dívky:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = record.feedback,
                                            fontSize = 11.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                        )
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

data class CharacterBio(
    val lore: String,
    val traits: List<String>,
    val goals: List<String>
)

@Composable
fun CharacterRadarChart(character: Character, modifier: Modifier = Modifier) {
    val tier = AffinityData.getTierForPoints(character.affinityPoints)
    val color = Color(tier.colorHex)

    val stats = listOf(
        Pair("Loajalita", (character.loajalita / 100f).coerceIn(0f, 1f)),
        Pair("Morálka", (character.morale / 100f).coerceIn(0f, 1f)),
        Pair("Poslušnost", (character.poslusnost / 100f).coerceIn(0f, 1f)),
        Pair("Síla", (character.strength / 100f).coerceIn(0f, 1f)),
        Pair("Submise", (character.submisivita / 100f).coerceIn(0f, 1f)),
        Pair("Důvěra", (character.duvera / 100f).coerceIn(0f, 1f))
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🕸️ Radarový graf schopností & pouta", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color)
                Text("Stupeň ${tier.level} (${tier.stageName})", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val onSurfaceColor = MaterialTheme.colorScheme.onSurface
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f * 0.75f
                    val count = stats.size

                    for (i in 1..4) {
                        val ringRadius = radius * (i / 4f)
                        val path = Path()
                        for (j in 0 until count) {
                            val angle = (Math.PI * 2 / count) * j - (Math.PI / 2)
                            val x = center.x + (ringRadius * cos(angle)).toFloat()
                            val y = center.y + (ringRadius * sin(angle)).toFloat()
                            if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        path.close()
                        drawPath(
                            path = path,
                            color = onSurfaceColor.copy(alpha = 0.15f),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    for (j in 0 until count) {
                        val angle = (Math.PI * 2 / count) * j - (Math.PI / 2)
                        val x = center.x + (radius * cos(angle)).toFloat()
                        val y = center.y + (radius * sin(angle)).toFloat()
                        drawLine(
                            color = onSurfaceColor.copy(alpha = 0.2f),
                            start = center,
                            end = Offset(x, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val statPath = Path()
                    val points = mutableListOf<Offset>()
                    stats.forEachIndexed { j, (_, value) ->
                        val angle = (Math.PI * 2 / count) * j - (Math.PI / 2)
                        val scaledRadius = radius * value.coerceIn(0.15f, 1f)
                        val x = center.x + (scaledRadius * cos(angle)).toFloat()
                        val y = center.y + (scaledRadius * sin(angle)).toFloat()
                        points.add(Offset(x, y))
                        if (j == 0) statPath.moveTo(x, y) else statPath.lineTo(x, y)
                    }
                    statPath.close()

                    drawPath(
                        path = statPath,
                        color = color.copy(alpha = 0.4f)
                    )
                    drawPath(
                        path = statPath,
                        color = color,
                        style = Stroke(width = 2.5f.dp.toPx())
                    )

                    points.forEach { pt ->
                        drawCircle(color = Color.White, radius = 3.5.dp.toPx(), center = pt)
                        drawCircle(color = color, radius = 2.dp.toPx(), center = pt)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                stats.forEach { (label, value) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text("${(value * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
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
        val scaleAnim = remember { androidx.compose.animation.core.Animatable(1f) }
        LaunchedEffect(character.affinityPoints) {
            scaleAnim.animateTo(1.15f, animationSpec = androidx.compose.animation.core.tween(150))
            scaleAnim.animateTo(0.95f, animationSpec = androidx.compose.animation.core.tween(100))
            scaleAnim.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(100))
        }
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
                        .clip(RoundedCornerShape(3.dp))
                        .graphicsLayer {
                            scaleX = scaleAnim.value
                            scaleY = scaleAnim.value
                        },
                    color = Color(affinityTier.colorHex)
                )
                Text("💭 \"${AffinityData.getRandomActiveDialogue(character)}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f), fontWeight = FontWeight.Medium)
                Text("⚔️ Pasivní boj: ${affinityTier.combatBonusDescription}", fontSize = 10.sp, color = Color(0xFFFF80AB), fontWeight = FontWeight.SemiBold)
            }
        }

        // Radar Chart of Character Stats & Affinity Tier Growth
        CharacterRadarChart(character = character)

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
                // Calculation of combat stats
                val hpBonus = character.equipment.values.filterNotNull().sumOf { it.hpBonus } + ((character.skills["vitality"] ?: 0) * 10)
                val totalMaxHp = character.maxHp + hpBonus
                
                val combatSkill = character.skills["combat"] ?: 0
                val combatBonus = character.equipment.values.filterNotNull().sumOf { it.combatBonus }
                var totalCombat = combatSkill + combatBonus + (combatSkill * 5)
                if (character.archetypeId in listOf("odvazna", "vzdorna", "krvava_subka", "zlomena")) {
                    totalCombat = (totalCombat * 1.2).toInt()
                }
                
                val defSkill = character.skills["defense"] ?: 0
                val defBonus = character.equipment.values.filterNotNull().sumOf { it.defenseBonus }
                val totalDef = defSkill + defBonus + (defSkill * 2)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚔️ Útok", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Text("$totalCombat", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🛡️ Obrana", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Text("$totalDef", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                
                StatProgressBar("Životní síla (HP)", character.hp, totalMaxHp, Color(0xFF4CAF50))
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
fun AffinityAndDialogueTab(
    character: Character,
    engine: GameEngine? = null,
    onTriggerAffinityEffect: ((AffinityBurstType, Float) -> Unit)? = null
) {
    val tier = AffinityData.getTierForPoints(character.affinityPoints)
    val nextTier = AffinityData.TIERS.firstOrNull { it.level == tier.level + 1 }
    val progressInTier = if (nextTier != null) {
        val currentSpan = (character.affinityPoints - tier.minPoints).toFloat()
        val totalSpan = (nextTier.minPoints - tier.minPoints).toFloat()
        (currentSpan / totalSpan).coerceIn(0f, 1f)
    } else 1.0f

    val animatedAffinityProgress by animateFloatAsState(
        targetValue = progressInTier,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 250f),
        label = "AffinityProgressBarAnim"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dynamic Mood Atmosphere Banner
        MoodAtmosphereBanner(character = character)

        // Main Affinity Level Status Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(tier.colorHex).copy(alpha = 0.4f))
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
                    progress = { animatedAffinityProgress },
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

        // Affinity Growth Trend Chart (Vico library visualization)
        AffinityTrendChart(
            character = character,
            modifier = Modifier.fillMaxWidth()
        )

        // Active Speech Dialogue Card
        val activeLine = AffinityData.getRandomActiveDialogue(character)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(tier.colorHex).copy(alpha = 0.12f)
            ),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(tier.colorHex).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("💬", fontSize = 20.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Aktuální myšlenky k pánovi:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(tier.colorHex)
                            )
                            IconButton(
                                onClick = { com.example.haremdark.domain.VoiceManager.speak(activeLine, character.archetypeId) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Přehrát hlas",
                                    tint = Color(tier.colorHex),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
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

                // --- 1. MICROPHONE GREET FEATURE ---
                var isListening by remember(character.id) { mutableStateOf(false) }
                var micGreetingResponse by remember(character.id) { mutableStateOf<String?>(null) }
                val coroutineScope = rememberCoroutineScope()

                Divider(color = Color(tier.colorHex).copy(alpha = 0.2f), thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isListening = true
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(1200L)
                                isListening = false
                                val dialogues = AffinityData.getDialoguesForTier(character.archetypeId, tier.level)
                                val chosen = if (dialogues.isNotEmpty()) dialogues.random() else activeLine
                                micGreetingResponse = chosen
                                com.example.haremdark.domain.VoiceManager.speak(chosen, character.archetypeId)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(tier.colorHex).copy(alpha = 0.85f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isListening) "Poslouchám hlas..." else "🎤 Pozdravit hlasem (Mikrofon)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                if (micGreetingResponse != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(tier.colorHex).copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "🎙️ Reakce ${character.name} na tvůj hlas:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(tier.colorHex)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "„$micGreetingResponse“",
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // --- 2. PERK SYSTEM & PASSIVE BONUSES OVERVIEW ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("✨", fontSize = 20.sp)
                    Column {
                        Text(
                            text = "Odemčené perky & pasivní bonusy",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = "Bonusy aktivní na základě dosažených stupňů vztahu",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                // Active Combat Stat Boosts Summary
                if (tier.hpBonus > 0 || tier.attackBonusPercent > 0 || tier.defenseBonus > 0 || tier.hpRegenBonus > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E88E5).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF1E88E5).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "🛡️ Aktivní bojové staty z pouta:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF90CAF9)
                            )
                            Text(
                                text = tier.combatBonusDescription,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // List unlocked perks up to current tier
                val unlockedTiers = AffinityData.TIERS.filter { it.level <= tier.level }
                unlockedTiers.forEach { t ->
                    t.unlockedPerks.forEach { perk ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("✅", fontSize = 11.sp)
                            Text(
                                text = "[Lv.${t.level}] $perk",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // --- 3. RANDOM NARRATIVE ENCOUNTER GENERATOR ---
        var generatedEventSummary by remember(character.id) { mutableStateOf<String?>(null) }
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFFBA68C8).copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🎲", fontSize = 20.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Generátor náhodných setkání",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFBA68C8)
                        )
                        Text(
                            text = "Spusť náhodnou konverzaci či scénu odpovídající vztahovému stupni (${tier.stageName})",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Button(
                    onClick = {
                        val event = com.example.haremdark.data.HaremEventData.generateEventForCharacter(character)
                        generatedEventSummary = event.title + "\n„" + event.teaserText + "“"
                        engine?.addLog("🎲 Vygenerována náhodná událost (${event.eventType.title}) pro ${character.name} (Stupeň ${tier.level})")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA68C8)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("✨ Vygenerovat náhodnou událost setkání", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (generatedEventSummary != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFBA68C8).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFBA68C8).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "📜 Výsledek generátoru setkání:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE1BEE7)
                            )
                            Text(
                                text = generatedEventSummary!!,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // --- BRANCHING DIALOGUE MINI-GAME ---
        var activeScenario by remember(character.id) { mutableStateOf<com.example.haremdark.data.DialogueScenario?>(null) }
        var currentFeedback by remember(character.id) { mutableStateOf<String?>(null) }
        var selectedOptionIdx by remember(character.id) { mutableStateOf<Int?>(null) }
        var showOutcome by remember(character.id) { mutableStateOf(false) }

        var shakeX by remember { mutableStateOf(0f) }
        var shakeY by remember { mutableStateOf(0f) }
        var shakeTrigger by remember { mutableStateOf(0) }

        LaunchedEffect(shakeTrigger) {
            if (shakeTrigger > 0) {
                val strength = 8f
                repeat(8) { i ->
                    shakeX = if (i % 2 == 0) strength else -strength
                    shakeY = if (i % 2 == 1) strength / 1.5f else -strength / 1.5f
                    delay(40)
                }
                shakeX = 0f
                shakeY = 0f
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = shakeX.dp, y = shakeY.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // DAILY DIALOGUE QUOTA & COOLDOWN STATUS
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (character.canTalkToday) Color(0xFFFF80AB).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, if (character.canTalkToday) Color(0xFFFF80AB).copy(alpha = 0.5f) else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(if (character.canTalkToday) "💬" else "😴", fontSize = 14.sp)
                            Column {
                                Text(
                                    text = if (character.canTalkToday) "Denní rozhovory: ${character.dailyTalksRemaining}/${character.maxDailyTalks} zbývá dnes"
                                    else "Denní limit rozhovorů vyčerpán (${character.maxDailyTalks}/${character.maxDailyTalks})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (character.canTalkToday) Color(0xFFFF80AB) else MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = if (character.canTalkToday) "Rozhovory prohlubují náklonnost a mění pohled dívky."
                                    else "Dívka vstřebává dnešní slova. Nové rozhovory zítra po odpočinku.",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Status dot indicators
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(character.maxDailyTalks) { idx ->
                                val isUsed = idx < character.dailyTalksCount
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isUsed) Color.Gray.copy(alpha = 0.35f)
                                            else Color(0xFFFF80AB)
                                        )
                                        .border(
                                            1.dp,
                                            if (isUsed) Color.Gray.copy(alpha = 0.5f) else Color(0xFFFF80AB),
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🗣️ Interaktivní Rozhovor",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFFF80AB)
                    )
                    if (activeScenario == null && !showOutcome) {
                        if (character.canTalkToday) {
                            Button(
                                onClick = {
                                    activeScenario = com.example.haremdark.data.AffinityData.getScenarioForArchetype(character.archetypeId, character.name)
                                    selectedOptionIdx = null
                                    currentFeedback = null
                                    showOutcome = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF80AB)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Zahájit rozhovor 💬 (${character.dailyTalksRemaining}x)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Gray.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "😴 Dnes vyčerpáno",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                if (activeScenario != null) {
                    Text(
                        text = activeScenario!!.prompt,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vyber si svou odpověď jako Pán:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Button(
                            onClick = {
                                val idx = 0
                                val option = activeScenario!!.options[idx]
                                selectedOptionIdx = idx
                                currentFeedback = option.feedback
                                showOutcome = true
                                val finalPrompt = activeScenario!!.prompt
                                activeScenario = null
                                shakeTrigger++
                                
                                val effectsList = mutableListOf<String>()
                                if (option.affinity != 0) effectsList.add("💖 ${if (option.affinity > 0) "+" else ""}${option.affinity}")
                                if (option.loyalty != 0) effectsList.add("👑 ${if (option.loyalty > 0) "+" else ""}${option.loyalty}")
                                if (option.trust != 0) effectsList.add("🤝 ${if (option.trust > 0) "+" else ""}${option.trust}")
                                if (option.submissiveness != 0) effectsList.add("⛓️ ${if (option.submissiveness > 0) "+" else ""}${option.submissiveness}")
                                if (option.fear != 0) effectsList.add("😨 ${if (option.fear > 0) "+" else ""}${option.fear}")
                                if (option.broken != 0) effectsList.add("💀 ${if (option.broken > 0) "+" else ""}${option.broken}")
                                val outcomeEffects = effectsList.joinToString(", ")

                                val logText = "★ Rozhovor s ${character.name}: vybrána možnost „${option.text}“ -> ${option.feedback}"
                                engine?.applyDialogueChoiceOutcome(
                                    characterId = character.id,
                                    affinityGain = option.affinity,
                                    loyaltyGain = option.loyalty,
                                    trustGain = option.trust,
                                    submissivenessGain = option.submissiveness,
                                    fearGain = option.fear,
                                    brokenGain = option.broken,
                                    logText = logText,
                                    prompt = finalPrompt,
                                    optionText = option.text,
                                    feedback = option.feedback,
                                    outcomeEffects = outcomeEffects
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F).copy(alpha = 0.85f),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("⚡ Přeskočit", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    activeScenario!!.options.forEachIndexed { idx, option ->
                        Card(
                            onClick = {
                                selectedOptionIdx = idx
                                currentFeedback = option.feedback
                                showOutcome = true
                                val finalPrompt = activeScenario!!.prompt
                                activeScenario = null
                                shakeTrigger++
                                
                                val effectsList = mutableListOf<String>()
                                if (option.affinity != 0) effectsList.add("💖 ${if (option.affinity > 0) "+" else ""}${option.affinity}")
                                if (option.loyalty != 0) effectsList.add("👑 ${if (option.loyalty > 0) "+" else ""}${option.loyalty}")
                                if (option.trust != 0) effectsList.add("🤝 ${if (option.trust > 0) "+" else ""}${option.trust}")
                                if (option.submissiveness != 0) effectsList.add("⛓️ ${if (option.submissiveness > 0) "+" else ""}${option.submissiveness}")
                                if (option.fear != 0) effectsList.add("😨 ${if (option.fear > 0) "+" else ""}${option.fear}")
                                if (option.broken != 0) effectsList.add("💀 ${if (option.broken > 0) "+" else ""}${option.broken}")
                                val outcomeEffects = effectsList.joinToString(", ")

                                val logText = "★ Rozhovor s ${character.name}: vybrána možnost „${option.text}“ -> ${option.feedback}"
                                engine?.applyDialogueChoiceOutcome(
                                    characterId = character.id,
                                    affinityGain = option.affinity,
                                    loyaltyGain = option.loyalty,
                                    trustGain = option.trust,
                                    submissivenessGain = option.submissiveness,
                                    fearGain = option.fear,
                                    brokenGain = option.broken,
                                    logText = logText,
                                    prompt = finalPrompt,
                                    optionText = option.text,
                                    feedback = option.feedback,
                                    outcomeEffects = outcomeEffects
                                )
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = option.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (option.affinity != 0) Text("💖 ${if (option.affinity > 0) "+" else ""}${option.affinity}", fontSize = 10.sp, color = Color(0xFFFF80AB), fontWeight = FontWeight.Bold)
                                    if (option.loyalty != 0) Text("👑 ${if (option.loyalty > 0) "+" else ""}${option.loyalty}", fontSize = 10.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                    if (option.trust != 0) Text("🤝 ${if (option.trust > 0) "+" else ""}${option.trust}", fontSize = 10.sp, color = Color(0xFF8BC34A), fontWeight = FontWeight.Bold)
                                    if (option.submissiveness != 0) Text("⛓️ ${if (option.submissiveness > 0) "+" else ""}${option.submissiveness}", fontSize = 10.sp, color = Color(0xFF9C27B0), fontWeight = FontWeight.Bold)
                                    if (option.fear != 0) Text("😨 ${if (option.fear > 0) "+" else ""}${option.fear}", fontSize = 10.sp, color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                                    if (option.broken != 0) Text("💀 ${if (option.broken > 0) "+" else ""}${option.broken}", fontSize = 10.sp, color = Color(0xFF7E57C2), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else if (showOutcome && currentFeedback != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF81C784).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF81C784).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Důsledky tvého rozhodnutí:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF81C784)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = currentFeedback!!,
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { com.example.haremdark.domain.VoiceManager.speak(currentFeedback!!, character.archetypeId) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Přehrát hlas dívky",
                                    tint = Color(0xFF81C784),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { showOutcome = false; currentFeedback = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                            modifier = Modifier.align(Alignment.End).height(28.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Text("Rozumím", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = "Vstup do interaktivního, větveného rozhovoru se svou dívkou. Tvé odpovědi ovlivní její pocity, věrnost, strach i oddanost na základě její jedinečné povahy a archetypu.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Roadmap of Milestones & Rewards
        Text("🗺️ Cesta náklonnosti a odměny:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

        AffinityData.TIERS.forEach { t ->
            val isUnlocked = character.affinityPoints >= t.minPoints
            val isCurrent = t.level == tier.level

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header: Level, Title, Status
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
                                fontSize = 13.sp,
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

                    // Progress Bar for current tier
                    if (isCurrent && nextTier != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LinearProgressIndicator(
                                progress = { progressInTier },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(t.colorHex)
                            )
                            Text(
                                text = "${character.affinityPoints} / ${nextTier.minPoints} pts k další úrovni",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }

                    androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Rewards Section
                    if (isUnlocked || isCurrent || t.level == tier.level + 1) {
                        // Combat Passive Bonus
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(13.dp))
                            Text(
                                text = "Bojový bonus: ${t.combatBonusDescription}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) Color(0xFFFF80AB) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        // Cosmetic Reward
                        t.cosmeticReward?.let { cosmetic ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                Text(
                                    text = "Kosmetický předmět: $cosmetic",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isUnlocked) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        
                        // Perks
                        t.unlockedPerks.forEach { perk ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = if (isUnlocked) Color(t.colorHex) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(12.dp))
                                Text(
                                    text = perk,
                                    fontSize = 11.sp,
                                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        // Dialogues
                        if (isUnlocked) {
                            val lines = AffinityData.getDialoguesForTier(character.archetypeId, t.level)
                            if (lines.isNotEmpty()) {
                                Column(modifier = Modifier.padding(top = 2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("Odemčené unikátní dialogy:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    lines.take(2).forEach { line ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "• „$line“",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = { com.example.haremdark.domain.VoiceManager.speak(line, character.archetypeId) },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.VolumeUp,
                                                    contentDescription = "Přehrát",
                                                    tint = Color(t.colorHex),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "🔒 Odměny jsou skryty, dokud se dívka více nepřiblíží této úrovni.",
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
    onUseInventoryItem: (InventoryItem) -> Unit,
    engine: GameEngine? = null,
    onTriggerAffinityEffect: ((AffinityBurstType, Float) -> Unit)? = null
) {
    var giftSubTab by remember { mutableIntStateOf(0) } // 0: Collectible Gifts & Inventory, 1: Potions & Direct

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MoodAtmosphereBanner(character = character)

        // Tab Mode Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = giftSubTab == 0,
                onClick = { giftSubTab = 0 },
                label = { Text("🎁 Dary & Inventář", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
            FilterChip(
                selected = giftSubTab == 1,
                onClick = { giftSubTab = 1 },
                label = { Text("🧪 Lektvary & Alchymie", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
        }

        if (giftSubTab == 0) {
            CollectibleGiftInventoryTab(
                character = character,
                player = player,
                engine = engine,
                onDirectGiftLegacy = { giftId ->
                    val direct = GameContent.DIRECT_GIFTS.find { it.id == giftId }
                    if (direct != null) onGiveDirectGift(direct)
                }
            )
        } else {
            // Potions & Consumables View
            val consumableItems = remember(player.items) {
                player.items.filter { it.count > 0 && !it.id.startsWith("gift_") && it.category != "gift" }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Aktivní lektvary a alchymistické esence pro ${character.name}:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                if (consumableItems.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🧪", fontSize = 28.sp)
                            Text(
                                text = "V inventáři nemáš žádné lektvary.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Můžeš je uvařit v Alchymistické laboratoři (Záložka Aktivity)!",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    consumableItems.forEach { item ->
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
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(item.icon.ifBlank { "🧪" }, fontSize = 22.sp)
                                    Column {
                                        Text("${item.name} (${item.count}x)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(item.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                        if (item.effectDescription.isNotBlank()) {
                                            Text(item.effectDescription, fontSize = 10.sp, color = Color(0xFF81C784))
                                        }
                                    }
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
fun SkillTreeTab(
    character: Character,
    engine: GameEngine? = null,
    onUpgradeSkill: (String) -> Unit
) {
    var selectedBranch by remember { mutableStateOf("Bojové Schopnosti") }
    val branches = listOf("Bojové Schopnosti", "Pasivní Statistiky", "Odkaz Výcviku", "Minulost")

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with level, xp, and available points
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Úroveň ${character.level} (Bojovnice)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("ZK: ${character.xp} ZK • SP: ${character.skillPoints} bodů", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f))
                    }
                    if (engine != null && character.xp >= 100) {
                        Button(
                            onClick = { engine.convertCharacterXpToSp(character.id) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                        ) {
                            Text("100 ZK ➔ 1 SP", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (character.xp.toFloat() / (character.level * 100).coerceAtLeast(1).toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
            }
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
                    text = { Text(branch, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            when (selectedBranch) {
                "Bojové Schopnosti" -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Aktivní & Pasivní dovednosti archetypu:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        val allSkills = CharacterSkillCatalog.getSkillTreeForCharacter(character)
                        allSkills.forEach { skillDef ->
                            val isUnlocked = character.unlockedCombatSkills.contains(skillDef.id) || character.unlockedPassives.contains(skillDef.id)
                            val canUnlockXp = !isUnlocked && character.xp >= skillDef.xpCost && character.level >= skillDef.reqLevel
                            val canUnlockSp = !isUnlocked && character.skillPoints >= skillDef.spCost && character.level >= skillDef.reqLevel
                            val isPassive = skillDef.nodeType == SkillNodeType.PASSIVE_PERK || skillDef.nodeType == SkillNodeType.SYNERGY_MASTERY

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUnlocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = if (isUnlocked) BorderStroke(1.dp, Color(0xFFFF80AB).copy(alpha = 0.5f)) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(skillDef.icon, fontSize = 18.sp)
                                            Column {
                                                Text(skillDef.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isUnlocked) Color(0xFFFF80AB) else MaterialTheme.colorScheme.onSurface)
                                                Text(if (isPassive) "Pasivní dovednost" else "Aktivní bojová schopnost (Cena: ${skillDef.activeSkill?.manaCost ?: 0} many)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                            }
                                        }

                                        if (isUnlocked) {
                                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF4CAF50).copy(alpha = 0.2f)) {
                                                Text("Odemčeno ✓", color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        } else {
                                            Text("Vyžaduje Úr. ${skillDef.reqLevel}", fontSize = 10.sp, color = if (character.level >= skillDef.reqLevel) Color(0xFF81C784) else Color(0xFFE53935), fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    Text(skillDef.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))

                                    if (!isUnlocked && engine != null) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (skillDef.xpCost > 0) {
                                                Button(
                                                    onClick = { engine.unlockCharacterSkillWithXp(character.id, skillDef.id) },
                                                    enabled = canUnlockXp,
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f),
                                                    contentPadding = PaddingValues(vertical = 4.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                                                ) {
                                                    Text("Odemknout (${skillDef.xpCost} ZK)", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            if (skillDef.spCost > 0) {
                                                Button(
                                                    onClick = { engine.unlockCharacterSkillWithSp(character.id, skillDef.id) },
                                                    enabled = canUnlockSp,
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f),
                                                    contentPadding = PaddingValues(vertical = 4.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                                                ) {
                                                    Text("Odemknout (${skillDef.spCost} SP)", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "Pasivní Statistiky" -> {
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
                }
                "Odkaz Výcviku" -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Trvalé bonusy zděděné z úspěšných výcvikových programů:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        val presetBuffs = listOf(
                            Triple("preset_discipline", "Železná Kázeň", "Každý den neklesá poslušnost a morálka se lépe udržuje."),
                            Triple("preset_care", "Vroucí Srdce", "Dvojnásobný zisk náklonnosti z dárků a něžných interakcí."),
                            Triple("preset_combat", "Zocelená v boji", "+5 k maximálnímu zdraví a poškození v boji."),
                            Triple("preset_obedience", "Slepá Oddanost", "Výrazně urychluje získávání důvěry a snižuje strach."),
                            Triple("preset_stamina", "Nekonečná Výdrž", "Zvyšuje maximální manu a snižuje únavu při náročných úkolech.")
                        )
                        
                        presetBuffs.forEach { (presetId, title, desc) ->
                            val isUnlocked = character.completedPresets.contains(presetId)
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = if (isUnlocked) BorderStroke(1.dp, Color(0xFFFFD700)) else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isUnlocked) Color(0xFFFFD700) else Color.DarkGray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(if (isUnlocked) "⭐" else "🔒", fontSize = 16.sp)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 11.sp,
                                            color = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                "Minulost" -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "📜 Klíčové vzpomínky & Střepy minulosti:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        if (character.keyMemories.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🔒", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Zatím jsi neobjevil žádné hlubší vzpomínky.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                    Text("Prováděj výcvik s vysokou morálkou (70+) pro šanci na odhalení střípků její minulosti.", fontSize = 10.sp, color = Color.Gray.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                                }
                            }
                        } else {
                            character.keyMemories.forEachIndexed { idx, memory ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { VoiceManager.speak(memory.description, character.archetypeId) },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.2f))
                                ) {
                                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("${idx + 1}.", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFFFFD700).copy(alpha = 0.5f))
                                        Column {
                                            Text(memory.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                            Text(memory.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 16.sp)
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

    val scaleAnim = remember { androidx.compose.animation.core.Animatable(1f) }
    LaunchedEffect(character.affinityPoints) {
        scaleAnim.animateTo(1.15f, animationSpec = androidx.compose.animation.core.tween(150))
        scaleAnim.animateTo(0.95f, animationSpec = androidx.compose.animation.core.tween(100))
        scaleAnim.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(100))
    }

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
                    .graphicsLayer {
                        scaleX = scaleAnim.value
                        scaleY = scaleAnim.value
                    }
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
    onUnequip: (String) -> Unit,
    engine: GameEngine? = null
) {
    val context = LocalContext.current
    val slots = listOf(
        Pair("weapon", "🗡️ Zbraň"),
        Pair("armor", "🛡️ Zbroj"),
        Pair("accessory", "💍 Doplněk")
    )
    
    var expandedSlot by remember { mutableStateOf<String?>(null) }
    var showSaveLoadoutDialog by remember { mutableStateOf(false) }
    var newLoadoutName by remember { mutableStateOf("") }
    var selectedSituation by remember { mutableStateOf("DPS") }
    var selectedIcon by remember { mutableStateOf("⚔️") }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bojová Výbava & Sety", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Voice Trigger Button: Combat War Cry
                            IconButton(
                                onClick = {
                                    val line = VoiceManager.playTriggerVoice(VoiceTriggerType.COMBAT_START, character)
                                    Toast.makeText(context, "📣 ${character.name}: „$line“", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Bojový pokřik", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                            }
                            // Voice Trigger Button: Affinity Chime
                            IconButton(
                                onClick = {
                                    val line = VoiceManager.playTriggerVoice(VoiceTriggerType.AFFINITY_LEVEL_UP, character)
                                    Toast.makeText(context, "💖 ${character.name}: „$line“", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = "Hlas pouta", tint = Color(0xFFE91E63), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    Text("Přepínejte mezi specializovanými sety výbavy pro různé situace v boji nebo uložte vlastní set.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f))
                }
            }
        }

        // Quick Loadouts Carousel
        if (engine != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚔️ Bojové sety pro situace:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFFFD700))
                        TextButton(
                            onClick = {
                                newLoadoutName = "Bojový set ${character.name}"
                                showSaveLoadoutDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Uložit set", fontSize = 11.sp)
                        }
                    }

                    val allLoadouts = engine.getAllLoadouts()
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allLoadouts) { loadout ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, Color(0xFFCE93D8).copy(alpha = 0.4f)),
                                modifier = Modifier.clickable {
                                    val (success, msg) = engine.applyLoadout(loadout.id, character.id)
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(loadout.icon, fontSize = 13.sp)
                                    Column {
                                        Text(loadout.name, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text(loadout.situationTag, fontSize = 9.sp, color = Color(0xFFFF80AB))
                                    }
                                }
                            }
                        }
                    }
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
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
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

    if (showSaveLoadoutDialog && engine != null) {
        AlertDialog(
            onDismissRequest = { showSaveLoadoutDialog = false },
            title = { Text("💾 Uložit set výbavy") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Uloží aktuální výbavu dívky ${character.name} do bojového loadoutu pro rychlé přepínání před bojem.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    OutlinedTextField(
                        value = newLoadoutName,
                        onValueChange = { newLoadoutName = it },
                        label = { Text("Název setu") },
                        placeholder = { Text("např. Smrtící dýka") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Bojové zaměření:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val situations = listOf("DPS", "TANK", "DARK_MAGIC", "BLEED", "BALANCED", "CUSTOM")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(situations) { tag ->
                            FilterChip(
                                selected = selectedSituation == tag,
                                onClick = { selectedSituation = tag },
                                label = { Text(tag, fontSize = 10.sp) }
                            )
                        }
                    }
                    Text("Ikona setu:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val icons = listOf("⚔️", "🛡️", "🔮", "🩸", "⚖️", "👑", "🏹")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(icons) { ic ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (selectedIcon == ic) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedIcon = ic }.padding(2.dp)
                            ) {
                                Text(ic, fontSize = 18.sp, modifier = Modifier.padding(6.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val saved = engine.saveCurrentLoadout(
                        name = newLoadoutName.ifBlank { "Bojový set ${character.name}" },
                        situationTag = selectedSituation,
                        icon = selectedIcon,
                        characterId = character.id
                    )
                    Toast.makeText(context, "Set '${saved.name}' byl úspěšně uložen!", Toast.LENGTH_SHORT).show()
                    showSaveLoadoutDialog = false
                }) {
                    Text("Uložit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveLoadoutDialog = false }) {
                    Text("Zrušit")
                }
            }
        )
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
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(10.dp))
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

@Composable
fun LowMoraleWarningBanner(character: Character, onOpenTraining: () -> Unit) {
    if (character.morale < 35 || character.loajalita < 25) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF3E1212)),
            border = BorderStroke(1.dp, Color(0xFFFF5252)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("⚠️", fontSize = 24.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "VAROVÁNÍ: KRITICKY NÍZKÁ MORÁLKA (${character.morale}%)",
                        color = Color(0xFFFF5252),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        "Hrozí vzpoura, pokus o útěk nebo odmítnutí poslušnosti v bojích!",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp
                    )
                }
                Button(
                    onClick = onOpenTraining,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Zahájit Výcvik", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TrainingMiniGameComponent(
    character: Character,
    engine: GameEngine?
) {
    var gameMode by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var gameCompleted by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var hits by remember { mutableIntStateOf(0) }
    var totalAttempts by remember { mutableIntStateOf(0) }
    var rankResult by remember { mutableStateOf("") }
    var loyaltyGain by remember { mutableIntStateOf(0) }
    var moraleGain by remember { mutableIntStateOf(0) }

    LaunchedEffect(gameCompleted) {
        if (gameCompleted) {
            val resultText = "Výsledek výcviku: Hodnocení $rankResult. Získán násobič loajality."
            VoiceManager.speak(resultText, character.archetypeId)
        }
    }

    var activeBeat by remember { mutableIntStateOf(1) }
    var showBreakthroughEffect by remember { mutableStateOf(false) }
    var breakthroughName by remember { mutableStateOf("") }

    var showSuccessAnim by remember { mutableStateOf(false) }
    var showFailureAnim by remember { mutableStateOf(false) }

    val successComposition by rememberLottieComposition(LottieCompositionSpec.Url("https://assets10.lottiefiles.com/packages/lf20_7w86at9a.json"))
    val failureComposition by rememberLottieComposition(LottieCompositionSpec.Url("https://assets10.lottiefiles.com/packages/lf20_ghp96qlm.json"))

    LaunchedEffect(showSuccessAnim) {
        if (showSuccessAnim) {
            kotlinx.coroutines.delay(800)
            showSuccessAnim = false
        }
    }
    LaunchedEffect(showFailureAnim) {
        if (showFailureAnim) {
            kotlinx.coroutines.delay(800)
            showFailureAnim = false
        }
    }

    var reflexPrompt by remember { mutableStateOf("") }
    var reflexStartTime by remember { mutableLongStateOf(0L) }
    var reactionMs by remember { mutableLongStateOf(0L) }
    var waitingForReflex by remember { mutableStateOf(false) }

    val prompts = remember { listOf("POKLEKNI!", "POSLECHNI PÁNA!", "DÍVEJ SE DO OČÍ!", "SOUSTŘEĎ SE!", "PODROB SE!") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("🎯 Interaktivní Výcvik Otrokyně", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(
                    "Výcvikové minihry s reflexními a rytmickými úkoly zvyšují morálku a násobí růst loajality podle vašeho výkonu!",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = gameMode == 0,
                        onClick = { if (!isPlaying) gameMode = 0 },
                        label = { Text("🎵 Rytmický dril", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = gameMode == 1,
                        onClick = { if (!isPlaying) gameMode = 1 },
                        label = { Text("⚡ Reflexní test", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = gameMode == 2,
                        onClick = { if (!isPlaying) gameMode = 2 },
                        label = { Text("⚙️ Presety", fontSize = 10.sp) }
                    )
                }
            }
        }

        if (gameMode == 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Rytmické plnění rozkazů", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700), fontSize = 14.sp)
                    Text("Stiskněte správné tlačítko v přesném rytmu, když svítí zeleně!", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))

                    if (!isPlaying && !gameCompleted) {
                        Button(
                            onClick = {
                                isPlaying = true
                                score = 0
                                hits = 0
                                totalAttempts = 0
                                activeBeat = (1..4).random()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("▶️ Spustit Rytmický Výcvik", fontWeight = FontWeight.Bold)
                        }
                    } else if (isPlaying) {
                        Text("Zásahy: $hits / 8  |  Skóre: $score", fontWeight = FontWeight.Bold, color = Color.White)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..4).forEach { btnIndex ->
                                val isTarget = btnIndex == activeBeat
                                Button(
                                    onClick = {
                                        totalAttempts++
                                        if (isTarget) {
                                            hits++
                                            score += 15
                                            showSuccessAnim = true
                                        } else {
                                            score = (score - 5).coerceAtLeast(0)
                                            showFailureAnim = true
                                        }
                                        if (totalAttempts >= 8) {
                                            isPlaying = false
                                            gameCompleted = true
                                            val pct = (hits.toFloat() / 8f) * 100f
                                            val (rank, mult) = when {
                                                pct >= 85f -> "S (3.0x Násobič)" to 3.0f
                                                pct >= 65f -> "A (2.0x Násobič)" to 2.0f
                                                pct >= 45f -> "B (1.2x Násobič)" to 1.2f
                                                else -> "F (0.5x Násobič)" to 0.5f
                                            }
                                            rankResult = rank
                                            loyaltyGain = (12 * mult).toInt()
                                            moraleGain = (10 * mult).toInt()

                                            val oldMood = character.nalada

                                            // Update mood based on rank
                                            when {
                                                pct >= 85f -> { character.nalada = "Extatická"; character.statusIcon = "🥰" }
                                                pct >= 65f -> { character.nalada = "Nadšená"; character.statusIcon = "😊" }
                                                pct >= 45f -> { character.nalada = "Poddajná"; character.statusIcon = "😌" }
                                                else -> { character.nalada = "Ponížená"; character.statusIcon = "😫" }
                                            }

                                            if (character.nalada != oldMood) {
                                                engine?.triggerMoodNotification(character, oldMood, "Výsledek rytmického drilu: Hodnocení $rankResult")
                                            } else {
                                                com.example.haremdark.domain.SoundEffectManager.playMoodFeedback(character.nalada, "training")
                                            }

                                            character.loajalita = (character.loajalita + loyaltyGain).coerceIn(0, 100)
                                            character.morale = (character.morale + moraleGain).coerceIn(0, 100)
                                            character.poslusnost = (character.poslusnost + (8 * mult).toInt()).coerceIn(0, 100)
                                            character.totalTrainingSessions++

                                            // Key Memory Discovery
                                            if (mult >= 2.0f && character.morale >= 70 && Random.nextFloat() < 0.25f) {
                                                val memoryPool = listOf(
                                                    KeyMemory("garden", "Rodná zahrada", "Vybavila si jasný obraz slunečného odpoledne v zahradách své rodné vesnice, než přišli lovci lidí.", "/app/src/main/res/drawable/memory_village_garden_1789676249433.jpg"),
                                                    KeyMemory("mother", "Ukolébavka", "Vzpomněla si na tvář své matky a na ukolébavku, kterou jí zpívala v dobách, kdy svět byl ještě bezpečný.", "/app/src/main/res/drawable/memory_mother_face_1789676260967.jpg"),
                                                    KeyMemory("sword", "Výcvik s otcem", "Během tréninku se jí vybavila technika meče, kterou ji učil její otec, dříve než byl odveden do války.", "/app/src/main/res/drawable/memory_sword_training_1789676273285.jpg"),
                                                    KeyMemory("lake", "Tajné jezero", "Vzpomněla si na své oblíbené místo u jezera, kde se schovávala, když chtěla být o samotě se svými sny.", "/app/src/main/res/drawable/memory_hidden_lake_1789676284135.jpg")
                                                )
                                                val newMemory = memoryPool.random()
                                                if (!character.keyMemories.any { it.id == newMemory.id }) {
                                                    character.keyMemories.add(newMemory)
                                                    VoiceManager.speak("Objevila jsem střípek své minulosti... " + newMemory.description, character.archetypeId)
                                                }
                                            }

                                            // Breakthrough check
                                            if (character.morale >= 70 && Random.nextFloat() < (if (character.morale >= 90) 0.4f else 0.15f)) {
                                                val types = listOf(
                                                    "combat_fury" to "Bojová Zuřivost (Aura)",
                                                    "iron_will" to "Železná Vůle (Obrana)",
                                                    "shadow_step" to "Stínový Krok (Úhyb)"
                                                )
                                                val (id, name) = types.random()
                                                character.breakthroughActive = true
                                                character.breakthroughType = id
                                                character.breakthroughExpiryDay = (engine?.gameState?.value?.player?.day ?: 1) + 3
                                                breakthroughName = name
                                                showBreakthroughEffect = true
                                            }

                                            character.interactionLogs.add(
                                                com.example.haremdark.models.InteractionLogEntry(
                                                    day = engine?.gameState?.value?.player?.day ?: 1,
                                                    type = "výcvik",
                                                    title = "Rytmický výcvik poslušnosti",
                                                    description = "Splněno $hits z 8 úkonů s přesností ${(hits/8f*100).toInt()}%.",
                                                    statChanges = "+$loyaltyGain Loajalita, +$moraleGain Morálka",
                                                    rank = rank.take(1)
                                                )
                                            )
                                        } else {
                                            activeBeat = (1..4).random()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isTarget) Color(0xFF4CAF50) else Color(0xFF333344)
                                    ),
                                    modifier = Modifier.size(60.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("$btnIndex", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.height(100.dp), contentAlignment = Alignment.Center) {
                        if (showSuccessAnim) {
                            LottieAnimation(
                                composition = successComposition,
                                iterations = 1,
                                modifier = Modifier.size(120.dp)
                            )
                        }
                        if (showFailureAnim) {
                            LottieAnimation(
                                composition = failureComposition,
                                iterations = 1,
                                modifier = Modifier.size(120.dp)
                            )
                        }
                    }
                }
            }
        } else if (gameMode == 1) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1E1E)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Reflexní test okamžité poslušnosti", fontWeight = FontWeight.Bold, color = Color(0xFFFF8A80), fontSize = 14.sp)
                    Text("Reagujte na příkaz pána okamžitě po jeho zobrazení!", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))

                    if (!isPlaying && !gameCompleted) {
                        Button(
                            onClick = {
                                isPlaying = true
                                waitingForReflex = true
                                reflexPrompt = "PŘIPRAV SE..."
                                score = 0
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("⚡ Spustit Reflexní Test", fontWeight = FontWeight.Bold)
                        }
                    } else if (isPlaying) {
                        LaunchedEffect(waitingForReflex) {
                            if (waitingForReflex) {
                                val delayMs = (1500..3500).random().toLong()
                                kotlinx.coroutines.delay(delayMs)
                                reflexPrompt = prompts.random()
                                reflexStartTime = System.currentTimeMillis()
                                waitingForReflex = false
                            }
                        }

                        if (waitingForReflex) {
                            CircularProgressIndicator(color = Color(0xFFFF5252))
                            Text("Čekej na příkaz...", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            Text(
                                reflexPrompt,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700)
                            )

                            Button(
                                onClick = {
                                    reactionMs = System.currentTimeMillis() - reflexStartTime
                                    isPlaying = false
                                    gameCompleted = true
                                    val (rank, mult) = when {
                                        reactionMs < 550L -> "S (3.0x Násobič)" to 3.0f
                                        reactionMs < 900L -> "A (2.0x Násobič)" to 2.0f
                                        reactionMs < 1400L -> "B (1.2x Násobič)" to 1.2f
                                        else -> "F (0.5x Násobič)" to 0.5f
                                    }
                                    rankResult = rank
                                    loyaltyGain = (15 * mult).toInt()
                                    moraleGain = (12 * mult).toInt()

                                    val oldMood = character.nalada

                                    // Update mood based on rank
                                    when {
                                        reactionMs < 550L -> { character.nalada = "Soustředěná"; character.statusIcon = "⚡" }
                                        reactionMs < 900L -> { character.nalada = "Pozorná"; character.statusIcon = "👁️" }
                                        reactionMs < 1400L -> { character.nalada = "Váhavá"; character.statusIcon = "🤔" }
                                        else -> { character.nalada = "Zmatená"; character.statusIcon = "😵" }
                                    }

                                    if (character.nalada != oldMood) {
                                        engine?.triggerMoodNotification(character, oldMood, "Výsledek reflexního testu: Hodnocení $rankResult")
                                    } else {
                                        com.example.haremdark.domain.SoundEffectManager.playMoodFeedback(character.nalada, "training")
                                    }

                                    character.loajalita = (character.loajalita + loyaltyGain).coerceIn(0, 100)
                                    character.morale = (character.morale + moraleGain).coerceIn(0, 100)
                                    character.poslusnost = (character.poslusnost + (10 * mult).toInt()).coerceIn(0, 100)

                                    if (mult >= 2.0f) {
                                        showSuccessAnim = true
                                        // Key Memory Discovery
                                        if (character.morale >= 70 && Random.nextFloat() < 0.25f) {
                                             val memoryPool = listOf(
                                                 KeyMemory("garden", "Rodná zahrada", "Vybavila si jasný obraz slunečného odpoledne v zahradách své rodné vesnice, než přišli lovci lidí.", "/app/src/main/res/drawable/memory_village_garden_1789676249433.jpg"),
                                                 KeyMemory("mother", "Ukolébavka", "Vzpomněla si na tvář své matky a na ukolébavku, kterou jí zpívala v dobách, kdy svět byl ještě bezpečný.", "/app/src/main/res/drawable/memory_mother_face_1789676260967.jpg"),
                                                 KeyMemory("sword", "Výcvik s otcem", "Během tréninku se jí vybavila technika meče, kterou ji učil její otec, dříve než byl odveden do války.", "/app/src/main/res/drawable/memory_sword_training_1789676273285.jpg"),
                                                 KeyMemory("lake", "Tajné jezero", "Vzpomněla si na své oblíbené místo u jezera, kde se schovávala, když chtěla být o samotě se svými sny.", "/app/src/main/res/drawable/memory_hidden_lake_1789676284135.jpg")
                                             )
                                             val newMemory = memoryPool.random()
                                             if (!character.keyMemories.any { it.id == newMemory.id }) {
                                                 character.keyMemories.add(newMemory)
                                                 VoiceManager.speak("Záblesk minulosti... " + newMemory.description, character.archetypeId)
                                             }
                                        }
                                    } else if (mult <= 0.5f) {
                                        showFailureAnim = true
                                    }

                                    // Breakthrough check
                                    if (character.morale >= 70 && Random.nextFloat() < (if (character.morale >= 90) 0.45f else 0.2f)) {
                                        val types = listOf(
                                            "combat_fury" to "Bojová Zuřivost (Aura)",
                                            "iron_will" to "Železná Vůle (Obrana)",
                                            "shadow_step" to "Stínový Krok (Úhyb)"
                                        )
                                        val (id, name) = types.random()
                                        character.breakthroughActive = true
                                        character.breakthroughType = id
                                        character.breakthroughExpiryDay = (engine?.gameState?.value?.player?.day ?: 1) + 3
                                        breakthroughName = name
                                        showBreakthroughEffect = true
                                    }

                                    character.interactionLogs.add(
                                        com.example.haremdark.models.InteractionLogEntry(
                                            day = engine?.gameState?.value?.player?.day ?: 1,
                                            type = "výcvik",
                                            title = "Reflexní test okamžité poslušnosti",
                                            description = "Reakční čas: ${reactionMs} ms na rozkaz \"$reflexPrompt\".",
                                            statChanges = "+$loyaltyGain Loajalita, +$moraleGain Morálka",
                                            rank = rank.take(1)
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                modifier = Modifier.fillMaxWidth().height(55.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("🔥 POSLECHNOUT HNED!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(modifier = Modifier.height(80.dp), contentAlignment = Alignment.Center) {
                            if (showSuccessAnim) {
                                LottieAnimation(
                                    composition = successComposition,
                                    iterations = 1,
                                    modifier = Modifier.size(100.dp)
                                )
                            }
                            if (showFailureAnim) {
                                LottieAnimation(
                                    composition = failureComposition,
                                    iterations = 1,
                                    modifier = Modifier.size(100.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (gameCompleted) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3320)),
                border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🏆 Výsledek výcviku: Hodnocení $rankResult", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700), fontSize = 16.sp)
                    if (reactionMs > 0) {
                        Text("Reakční čas: ${reactionMs} ms", fontSize = 11.sp, color = Color.White)
                    }
                    Text("Bonus k loajalitě: +$loyaltyGain  |  Zvýšení morálky: +$moraleGain", fontWeight = FontWeight.Bold, color = Color(0xFF81C784), fontSize = 12.sp)

                    Button(
                        onClick = {
                            gameCompleted = false
                            isPlaying = false
                            reactionMs = 0L
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("Opakovat výcvik", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (gameMode == 2) {
            TrainingPresetsSection(character = character, engine = engine)
        }

        if (showBreakthroughEffect) {
            BreakthroughOverlay(
                breakthroughName = breakthroughName,
                onFinished = { showBreakthroughEffect = false }
            )
        }
    }
}

@Composable
fun SlaveInteractionLogTab(
    character: Character,
    engine: GameEngine?
) {
    val currentDay = engine?.gameState?.value?.player?.day ?: 1
    val logs = remember(character.interactionLogs.size, currentDay) {
        character.getSafeInteractionLogs(currentDay)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MoraleSparklineChart(character = character, currentDay = currentDay)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "📜 Deník interakcí a výcviku",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Záznamy o výsledcích výcviku, rozhovorech a fluktuaci morálky.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        "${logs.size} Záznamů",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = logs) { entry ->
                InteractionLogCard(entry, character.archetypeId)
            }
        }
    }
}

@Composable
fun InteractionLogCard(entry: com.example.haremdark.models.InteractionLogEntry, archetypeId: String? = null) {
    val (typeIcon, typeColor) = when (entry.type) {
        "výcvik" -> "🎯" to Color(0xFF4CAF50)
        "rozhovor" -> "💬" to Color(0xFF2196F3)
        "trest" -> "⚡" to Color(0xFFE91E63)
        "odměna" -> "🎁" to Color(0xFFFF9800)
        "morálka" -> "📈" to Color(0xFF9C27B0)
        else -> "📜" to Color(0xFF00BCD4)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
        border = BorderStroke(1.dp, typeColor.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { VoiceManager.speak(entry.description, archetypeId) }
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(typeIcon, fontSize = 16.sp)
                    Text(entry.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (entry.rank != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFD700).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFFFFD700))
                        ) {
                            Text("Rank ${entry.rank}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                    Text("Den ${entry.day}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }

            Text(entry.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))

            if (entry.statChanges.isNotEmpty()) {
                Text(entry.statChanges, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = typeColor)
            }
        }
    }
}

@Composable
fun MoraleSparklineChart(
    character: Character,
    currentDay: Int = 1,
    modifier: Modifier = Modifier
) {
    val moraleRecords = remember(character.morale, character.moraleHistory.size, currentDay) {
        character.getSafeMoraleTrend(currentDay)
    }

    val chartEntries = remember(moraleRecords) {
        moraleRecords.mapIndexed { index, record ->
            FloatEntry(x = (index + 1).toFloat(), y = record.morale.toFloat())
        }
    }

    val firstVal = moraleRecords.firstOrNull()?.morale ?: 50
    val lastVal = moraleRecords.lastOrNull()?.morale ?: character.morale
    val diff = lastVal - firstVal
    val diffText = if (diff >= 0) "+$diff%" else "$diff%"
    val diffColor = if (diff >= 0) Color(0xFF81C784) else Color(0xFFFF8A80)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1424)),
        border = BorderStroke(1.dp, Color(0xFFAB47BC).copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Text("📈", fontSize = 16.sp)
                    Column {
                        Text(
                            "Sparkline Morálky (Posledních 7 dní)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            "Sledování vývoje morálky a stability slave po výcviku",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = diffColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, diffColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        diffText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = diffColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF120A16))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Chart(
                    chart = lineChart(),
                    model = entryModelOf(chartEntries),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Minulý týden: ${firstVal}%",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                Text(
                    "Aktuální: ${lastVal}% (${if (lastVal < 35) "⚠️ Riziko vzpoury" else "Stabilní"})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (lastVal < 35) Color(0xFFFF5252) else Color(0xFF81C784)
                )
            }
        }
    }
}

@Composable
fun MemoryGalleryTab(character: Character) {
    var selectedMemory by remember { mutableStateOf<KeyMemory?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "🖼️ Galerie Klíčových Vzpomínek",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (character.keyMemories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Žádné vzpomínky nebyly dosud odemčeny.\nTrénuj dívku s vysokou morálkou pro šanci na záblesk minulosti.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(character.keyMemories) { memory ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable { selectedMemory = memory },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f))
                    ) {
                        Box {
                            // Memory Illustration
                            AsyncImage(
                                model = memory.illustrationUrl ?: portraitResFor(character.archetypeId), // Fallback to portrait
                                contentDescription = memory.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Overlay gradient for title
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                            startY = 100f
                                        )
                                    )
                            )
                            
                            Text(
                                memory.title,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (selectedMemory != null) {
        Dialog(onDismissRequest = { selectedMemory = null }) {
            val memory = selectedMemory!!
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
            ) {
                Column {
                    Box(modifier = Modifier.weight(0.6f).fillMaxWidth()) {
                        AsyncImage(
                            model = memory.illustrationUrl ?: portraitResFor(character.archetypeId),
                            contentDescription = memory.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedMemory = null },
                            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White)
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(0.4f)
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            memory.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            memory.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { VoiceManager.speak(memory.description, character.archetypeId) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Přehrát vzpomínku")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper to get portrait if memory image is missing
@Composable
fun portraitResFor(archetypeId: String): Int {
    return StaticData.getPortraitForArchetype(archetypeId)
}

@Composable
fun MilestonesTab(character: Character) {
    val thresholds = listOf(
        25 to "Začátek oddanosti: +5 Síla",
        50 to "Věrné srdce: +10 Max HP",
        75 to "Absolutní fanatismus: +15% poškození (Pasiv: Fanatik)"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🏆 Milníky oddanosti", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        
        thresholds.forEach { (threshold, reward) ->
            val isUnlocked = character.milestoneRewardsUnlocked.contains(threshold)
            val progress = (character.loajalita.toFloat() / threshold.toFloat()).coerceAtMost(1f)
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) Color(0xFF1B3320) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, if (isUnlocked) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(40.dp).background(if (isUnlocked) Color(0xFF4CAF50) else Color.Gray, CircleShape)
                    ) {
                        Text(if (isUnlocked) "✓" else "$threshold%", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(reward, fontWeight = FontWeight.Bold, color = if (isUnlocked) Color.White else MaterialTheme.colorScheme.onSurface)
                        if (!isUnlocked) {
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.Gray.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrainingPresetsSection(
    character: Character,
    engine: GameEngine?
) {
    var customPresets by remember(character.id, character.trainingPresets.size) {
        mutableStateOf(character.trainingPresets)
    }
    val allPresets = remember(customPresets) {
        getDefaultTrainingPresets() + customPresets
    }

    var executionResult by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF231B2E)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFAB47BC).copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "⚙️ Automatizované výcvikové presety",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE1BEE7),
                        fontSize = 13.sp
                    )
                    Text(
                        "Uložte si posloupnosti výcvikových kroků pro rychlé a opakované spouštění.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("➕ Nový Preset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (executionResult != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3320)),
                border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        executionResult ?: "",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { executionResult = null }, modifier = Modifier.size(24.dp)) {
                        Text("✕", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }

        allPresets.forEach { preset ->
            val totalEnergy = preset.actions.sumOf { it.energyCost }
            val totalLoyalty = preset.actions.sumOf { it.loyaltyGain }
            val totalMorale = preset.actions.sumOf { it.moraleGain }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1E2E)),
                border = BorderStroke(1.dp, Color(0xFF8E24AA).copy(alpha = 0.35f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var selectedPartnerId by remember { mutableStateOf<String?>(null) }
                    val characters = engine?.gameState?.value?.characters ?: emptyList()
                    val availablePartners = characters.filter { it.id != character.id && it.naNajmu == false }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(preset.icon, fontSize = 20.sp)
                            Column {
                                Text(preset.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text(preset.description, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                            }
                        }

                        if (availablePartners.isNotEmpty()) {
                            val partner = availablePartners.find { it.id == selectedPartnerId }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF4A148C).copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, Color(0xFFCE93D8).copy(alpha = 0.4f)),
                                modifier = Modifier.clickable {
                                    val currentIndex = if (selectedPartnerId == null) -1 else availablePartners.indexOfFirst { it.id == selectedPartnerId }
                                    selectedPartnerId = if (currentIndex == availablePartners.size - 1) null else availablePartners[currentIndex + 1].id
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🤝", fontSize = 12.sp)
                                    Text(partner?.name ?: "Bez partnerky", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Action sequence flow
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        preset.actions.forEachIndexed { idx, act ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF3E2723).copy(alpha = 0.8f),
                                border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.5f))
                            ) {
                                Text(
                                    "${act.icon} ${act.name}",
                                    fontSize = 9.sp,
                                    color = Color(0xFFFFE0B2),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                            if (idx < preset.actions.size - 1) {
                                Text("➔", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    // Stat projected totals & execute button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val partner = availablePartners.find { it.id == selectedPartnerId }
                        var bonusText = ""
                        var bonusLoyalty = 0
                        var bonusMorale = 0
                        
                        if (partner != null) {
                            val traits1 = StaticData.getTraitsForArchetype(character.archetypeId)
                            val traits2 = StaticData.getTraitsForArchetype(partner.archetypeId)
                            if (traits1.isNotEmpty() && traits2.isNotEmpty() && StaticData.areTraitsComplementary(traits1[0], traits2[0])) {
                                bonusLoyalty = (totalLoyalty * 0.25f).toInt()
                                bonusMorale = (totalMorale * 0.15f).toInt()
                                bonusText = " (+25% Komplementární bonus!)"
                            } else {
                                bonusLoyalty = (totalLoyalty * 0.1f).toInt()
                                bonusText = " (+10% Týmový bonus)"
                            }
                        }

                        Text(
                            "Spotřeba: ⚡$totalEnergy energy  |  Zisk: +${totalLoyalty + bonusLoyalty} L, +${totalMorale + bonusMorale} M$bonusText",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (bonusLoyalty > 0) Color(0xFF81C784) else Color(0xFFCE93D8)
                        )

                        Button(
                            onClick = {
                                val player = engine?.gameState?.value?.player
                                val finalLoyaltyGain = totalLoyalty + bonusLoyalty
                                val finalMoraleGain = totalMorale + bonusMorale

                                if (player != null && player.sexEnergy < totalEnergy) {
                                    executionResult = "⚠️ Nedostatek sexuální energie! Potřebuješ $totalEnergy energy (máš ${player.sexEnergy})."
                                } else {
                                    if (player != null) {
                                        player.sexEnergy = (player.sexEnergy - totalEnergy).coerceAtLeast(0)
                                    }
                                    character.loajalita = (character.loajalita + finalLoyaltyGain).coerceIn(0, 100)
                                    character.morale = (character.morale + finalMoraleGain).coerceIn(0, 100)
                                    character.poslusnost = (character.poslusnost + (finalLoyaltyGain * 0.8f).toInt()).coerceIn(0, 100)
                                    character.totalTrainingSessions++
                                    character.completedPresets.add(preset.id)

                                    val oldMood = character.nalada

                                    // Update mood for presets (assume success/focus)
                                    character.nalada = "Disciplinovaná"
                                    character.statusIcon = "🫡"

                                    if (character.nalada != oldMood) {
                                        engine?.triggerMoodNotification(character, oldMood, "Výcvikový preset: ${preset.title}")
                                    } else {
                                        com.example.haremdark.domain.SoundEffectManager.playMoodFeedback(character.nalada, "training")
                                    }

                                    val currentDay = engine?.gameState?.value?.player?.day ?: 1
                                    val partnerText = if (partner != null) " (S partnerkou ${partner.name})" else ""
                                    character.interactionLogs.add(
                                        com.example.haremdark.models.InteractionLogEntry(
                                            day = currentDay,
                                            type = "výcvik",
                                            title = "Automatický preset: ${preset.title}$partnerText",
                                            description = "Provedena sekvence (${preset.actions.size} kroků): ${preset.actions.joinToString { it.name }}. Partnerka: ${partner?.name ?: "Žádná"}.",
                                            statChanges = "+$finalLoyaltyGain Loajalita, +$finalMoraleGain Morálka",
                                            rank = if (bonusLoyalty > 0) "S+" else "S"
                                        )
                                    )

                                    character.moraleHistory.add(
                                        com.example.haremdark.models.MoraleRecord(
                                            day = currentDay,
                                            morale = character.morale,
                                            source = preset.title
                                        )
                                    )

                                    executionResult = "✅ Preset „${preset.title}“ úspěšně dokončen! (+$finalLoyaltyGain Loajalita, +$finalMoraleGain Morálka)$partnerText"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("⚡ Spustit preset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            CreateTrainingPresetDialog(
                onDismiss = { showCreateDialog = false },
                onSave = { newPreset ->
                    character.trainingPresets.add(newPreset)
                    customPresets = character.trainingPresets.toMutableList()
                    showCreateDialog = false
                    executionResult = "✨ Nový automatický preset „${newPreset.title}“ byl vytvořen a uložen!"
                }
            )
        }
    }
}

@Composable
fun CreateTrainingPresetDialog(
    onDismiss: () -> Unit,
    onSave: (TrainingPreset) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedActions by remember { mutableStateOf(listOf<TrainingPresetAction>()) }

    val availableActions = remember {
        listOf(
            TrainingPresetAction("reflex", "Reflexní test poslušnosti", 10, 12, 8, "⚡"),
            TrainingPresetAction("rhythm", "Rytmický dril rozkazů", 12, 10, 6, "🎵"),
            TrainingPresetAction("praise", "Pochvala & uznání pána", 8, 8, 12, "✨"),
            TrainingPresetAction("bath", "Očistná lázeň s oleji", 15, 14, 18, "uba"),
            TrainingPresetAction("whip", "Přísný kázeňský dril", 12, 10, -5, "⛓️")
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("➕ Vytvořit výcvikový preset", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Název presetu") },
                    placeholder = { Text("Např. Ranní dril poslušnosti") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Popis presetu") },
                    placeholder = { Text("Např. Rychlá kombinace reflexů a lázně") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Vybrané úkony (${selectedActions.size}/4):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.LightGray)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (selectedActions.isEmpty()) {
                        Text("Zatím nebyly vybrány žádné úkony.", fontSize = 10.sp, color = Color.Gray)
                    } else {
                        selectedActions.forEachIndexed { idx, act ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF4A148C),
                                modifier = Modifier.clickable {
                                    selectedActions = selectedActions.toMutableList().apply { removeAt(idx) }
                                }
                            ) {
                                Text("${act.icon} ${act.name} ✕", fontSize = 9.sp, color = Color.White, modifier = Modifier.padding(6.dp))
                            }
                        }
                    }
                }

                Text("Přidat úkon do sekvence:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.LightGray)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    availableActions.forEach { act ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF261C2C),
                            border = BorderStroke(1.dp, Color(0xFF7B1FA2).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = selectedActions.size < 4) {
                                    selectedActions = selectedActions + act
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${act.icon} ${act.name}", fontSize = 11.sp, color = Color.White)
                                Text("⚡${act.energyCost} energy  |  +${act.loyaltyGain} L, +${act.moraleGain} M", fontSize = 10.sp, color = Color(0xFFE1BEE7))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && selectedActions.isNotEmpty()) {
                        val newPreset = TrainingPreset(
                            id = "custom_${System.currentTimeMillis()}",
                            title = title,
                            description = if (description.isBlank()) "Vlastní automatický preset" else description,
                            icon = "⚙️",
                            actions = selectedActions
                        )
                        onSave(newPreset)
                    }
                },
                enabled = title.isNotBlank() && selectedActions.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
            ) {
                Text("Uložit preset", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušit", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1E1424)
    )
}

@Composable
fun BreakthroughOverlay(
    breakthroughName: String,
    onFinished: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1.2f else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(2500)
        visible = false
        delay(500)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Text("✨ PRŮLOM (BREAKTHROUGH) ✨", color = Color(0xFFFFD700), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(breakthroughName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Bojové schopnosti slave dočasně zvýšeny!", color = Color.LightGray, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun GlobalAffinityMilestonesDialog(player: com.example.haremdark.models.Player, characters: List<Character>, engine: com.example.haremdark.domain.GameEngine?, onDismiss: () -> Unit) {
    val totalGlobalAffinity = characters.sumOf { it.affinityPoints }
    var actionMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.96f).fillMaxHeight(0.90f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("👑", fontSize = 24.sp)
                        Column {
                            Text("Síň slávy globálních milníků", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Celková náklonnost harému: $totalGlobalAffinity pts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít")
                    }
                }

                if (actionMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFD700).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = actionMessage!!,
                            modifier = Modifier.padding(10.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }

                Text("Odemkni exkluzivní avatary a tituly dosažením celkové afinity napříč všemi dívkami:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))

                com.example.haremdark.data.GlobalAffinityMilestoneData.MILESTONES.forEach { milestone ->
                    val isUnlocked = player.unlockedGlobalMilestones.contains(milestone.id)
                    val canClaim = totalGlobalAffinity >= milestone.requiredGlobalAffinity && !isUnlocked

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUnlocked) Color(milestone.colorHex).copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(1.dp, Color(milestone.colorHex).copy(alpha = if (isUnlocked) 0.8f else 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(milestone.icon, fontSize = 22.sp)
                                    Column {
                                        Text(milestone.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(milestone.colorHex))
                                        Text("Požadavek: ${milestone.requiredGlobalAffinity} affinity pts", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    }
                                }
                                
                                if (isUnlocked) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    ) {
                                        Text("✅ Vyzvednuto", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                    }
                                } else if (canClaim) {
                                    Button(
                                        onClick = {
                                            val (success, msg) = engine?.claimGlobalAffinityMilestone(milestone.id) ?: Pair(false, "Chyba engine")
                                            if (success) {
                                                com.example.haremdark.domain.SoundEffectManager.playLevelUp()
                                            }
                                            actionMessage = msg
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(milestone.colorHex)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("🎁 Vyzvednout odměnu", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                } else {
                                    Text("🔒 ${milestone.requiredGlobalAffinity - totalGlobalAffinity} pts zbývá", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            Text(milestone.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("🎁 Odměny milníku:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(milestone.colorHex))
                                    Text("• Exkluzivní titul: „${milestone.rewardTitleTag}“", fontSize = 10.sp)
                                    Text("• Avatar rám: ${milestone.rewardAvatarFrame}", fontSize = 10.sp)
                                    Text("• Zlaťáky: +${milestone.rewardGold} zl. | Temná energie: +${milestone.rewardDarkEnergy} TE", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Zavřít síň slávy")
                }
            }
        }
    }
}
