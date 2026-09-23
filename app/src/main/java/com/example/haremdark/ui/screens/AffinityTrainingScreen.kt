package com.example.haremdark.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.haremdark.data.ElementalCodexData
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HaremSound
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.Character
import com.example.haremdark.models.Element
import com.example.haremdark.models.GameSave
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Metadata and theming configuration for elemental sparring.
 */
data class ElementTrainingInfo(
    val element: Element,
    val displayName: String,
    val icon: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val combatRole: String,
    val resonanceEffect: String
)

val ELEMENT_CATALOG: List<ElementTrainingInfo> = listOf(
    ElementTrainingInfo(
        element = Element.FIRE,
        displayName = "Oheň",
        icon = "🔥",
        primaryColor = Color(0xFFFF5722),
        secondaryColor = Color(0xFFFFB300),
        combatRole = "Útočný žár",
        resonanceEffect = "+Kritické popáleniny a průraz zbrojí"
    ),
    ElementTrainingInfo(
        element = Element.WATER,
        displayName = "Voda",
        icon = "💧",
        primaryColor = Color(0xFF0288D1),
        secondaryColor = Color(0xFF26C6DA),
        combatRole = "Obnova & Proud",
        resonanceEffect = "+Regenerace many a odolnost proti jedům"
    ),
    ElementTrainingInfo(
        element = Element.EARTH,
        displayName = "Země",
        icon = "🪨",
        primaryColor = Color(0xFF43A047),
        secondaryColor = Color(0xFF8D6E63),
        combatRole = "Pevnostní val",
        resonanceEffect = "+Redukce plošného poškození a pevný postoj"
    ),
    ElementTrainingInfo(
        element = Element.AIR,
        displayName = "Vzduch",
        icon = "🌪️",
        primaryColor = Color(0xFF00BCD4),
        secondaryColor = Color(0xFFE0F7FA),
        combatRole = "Hbitý vítr",
        resonanceEffect = "+Vyšší šance na úskok a bleskové tempo"
    ),
    ElementTrainingInfo(
        element = Element.ICE,
        displayName = "Led",
        icon = "❄️",
        primaryColor = Color(0xFF29B6F6),
        secondaryColor = Color(0xFF80DEEA),
        combatRole = "Mrazivá kontrola",
        resonanceEffect = "+Zpomalení nepřítele a omračující kryo-efekt"
    ),
    ElementTrainingInfo(
        element = Element.LIGHTNING,
        displayName = "Blesk",
        icon = "⚡",
        primaryColor = Color(0xFFFFD600),
        secondaryColor = Color(0xFFFF9100),
        combatRole = "Elektrický blesk",
        resonanceEffect = "+Šoková rezonance a řetězové výboje"
    ),
    ElementTrainingInfo(
        element = Element.DARK,
        displayName = "Temnota",
        icon = "🔮",
        primaryColor = Color(0xFFAB47BC),
        secondaryColor = Color(0xFF311B92),
        combatRole = "Magie stínů",
        resonanceEffect = "+Vysávání životů a temné kletby"
    ),
    ElementTrainingInfo(
        element = Element.HOLY,
        displayName = "Světlo",
        icon = "✨",
        primaryColor = Color(0xFFFFD54F),
        secondaryColor = Color(0xFFFFF9C4),
        combatRole = "Svatá záře",
        resonanceEffect = "+Očista spojenců a posvátné štíty"
    ),
    ElementTrainingInfo(
        element = Element.PHYSICAL,
        displayName = "Fyzický",
        icon = "⚔️",
        primaryColor = Color(0xFFEF5350),
        secondaryColor = Color(0xFFB0BEC5),
        combatRole = "Ocel & Krev",
        resonanceEffect = "+Základní hrubá síla a hluboké krvácení"
    )
)

/**
 * State object capturing animation details for an affinity multiplier boost.
 */
data class AffinityMultiplierFeedbackState(
    val triggerId: Long,
    val characterName: String,
    val elementInfo: ElementTrainingInfo,
    val oldMultiplier: Float,
    val newMultiplier: Float,
    val delta: Float = 0.05f,
    val newlyUnlockedSkin: String? = null,
    val totalMultiplierSum: Float = 0f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AffinityTrainingScreen(
    gameState: GameSave,
    engine: GameEngine,
    onBack: (() -> Unit)? = null,
    onOpenElementalCodex: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val characters = gameState.characters

    // Track currently selected character
    var selectedCharacterId by remember {
        mutableStateOf(characters.firstOrNull()?.id ?: "")
    }

    // Keep active character refreshed from gameState
    val activeCharacter = remember(gameState, selectedCharacterId) {
        characters.firstOrNull { it.id == selectedCharacterId } ?: characters.firstOrNull()
    }

    // State for visual feedback animation
    var feedbackState by remember { mutableStateOf<AffinityMultiplierFeedbackState?>(null) }
    var lastUpgradedElement by remember { mutableStateOf<Element?>(null) }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Top Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("affinity_training_back_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Zpět",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Trénink Afinity & Živlů",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Zvyšuj elementární násobiče dívek a odemykej aury v Kodexu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "+0.05x",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (onOpenElementalCodex != null) {
                        Button(
                            onClick = onOpenElementalCodex,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .height(34.dp)
                                .testTag("affinity_open_elemental_codex_button")
                        ) {
                            Icon(
                                Icons.Default.AutoStories,
                                contentDescription = "Elementární Kodex",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Kodex (${ElementalCodexData.getDiscoveredCount(gameState)}/9)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (characters.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nemáš v harému žádné hrdinky k tréninku.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
                ) {
                    // Character selection carousel
                    item {
                        Text(
                            text = "VYBER HRDINKU K TRÉNINKU",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(characters) { char ->
                                val isSelected = char.id == activeCharacter?.id
                                val totalMult = remember(char.elementalMultipliers) {
                                    char.elementalMultipliers.values.sum()
                                }
                                val archetype = remember(char.archetypeId) {
                                    StaticData.ARCHETYPES[char.archetypeId]
                                }

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    },
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                        }
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            selectedCharacterId = char.id
                                            SoundEffectManager.playNavigation(com.example.haremdark.domain.NavSound.MENU_CLICK)
                                        }
                                        .testTag("character_chip_${char.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.surface
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = when (char.archetypeId) {
                                                    "subka" -> "🌸"
                                                    "odvazna" -> "⚔️"
                                                    "touha" -> "💋"
                                                    "zlomena" -> "⛓️"
                                                    "chladna" -> "❄️"
                                                    "sukuba" -> "🦇"
                                                    "draci_divka" -> "🐉"
                                                    else -> "✨"
                                                },
                                                fontSize = 18.sp
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = char.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = archetype?.name ?: "Dívka",
                                                    fontSize = 10.sp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                                Text(
                                                    text = "Σ ${"%.2f".format(totalMult)}x",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFFB300)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Active Character Mastery Dashboard
                    if (activeCharacter != null) {
                        item {
                            CharacterMasterySummaryCard(
                                character = activeCharacter,
                                onNavigateToCodex = {
                                    // Navigating or previewing
                                }
                            )
                        }

                        // Sparring Elements Grid Header
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "ELEMENTÁRNÍ SPARING",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.1.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Zvol živel pro posílení násobiče poškození (+0.05x)",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = "Seance: ${activeCharacter.totalTrainingSessions}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Elemental cards grid
                        items(ELEMENT_CATALOG) { elemInfo ->
                            val currentMult = activeCharacter.elementalMultipliers.getOrDefault(elemInfo.element.name, 1.0f)
                            val isJustUpgraded = lastUpgradedElement == elemInfo.element

                            ElementalSparringCard(
                                elementInfo = elemInfo,
                                currentMultiplier = currentMult,
                                isJustUpgraded = isJustUpgraded,
                                onTrain = {
                                    val oldMult = currentMult
                                    val oldSkins = activeCharacter.unlockedSkins.toList()

                                    // Run training in engine
                                    engine.performAffinityTraining(activeCharacter, elemInfo.element)

                                    val newMult = activeCharacter.elementalMultipliers.getOrDefault(elemInfo.element.name, 1.0f)
                                    val newSkins = activeCharacter.unlockedSkins.toList()
                                    val unlockedSkin = newSkins.firstOrNull { it !in oldSkins }
                                    val totalSum = activeCharacter.elementalMultipliers.values.sum()

                                    lastUpgradedElement = elemInfo.element

                                    // Play audio feedback
                                    SoundEffectManager.playHarem(HaremSound.AFFINITY_UP)
                                    if (unlockedSkin != null) {
                                        SoundEffectManager.playHarem(HaremSound.SKILL_UNLOCK)
                                    }

                                    // Trigger feedback animation overlay
                                    feedbackState = AffinityMultiplierFeedbackState(
                                        triggerId = System.currentTimeMillis(),
                                        characterName = activeCharacter.name,
                                        elementInfo = elemInfo,
                                        oldMultiplier = oldMult,
                                        newMultiplier = newMult,
                                        newlyUnlockedSkin = unlockedSkin,
                                        totalMultiplierSum = totalSum
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Overlay celebration animation with Lottie and Compose transitions
        feedbackState?.let { state ->
            AffinityMultiplierCelebrationOverlay(
                feedback = state,
                onDismiss = { feedbackState = null },
                onOpenElementalCodex = onOpenElementalCodex
            )
        }
    }
}

/**
 * Character Mastery card displaying total elemental sum, next milestone, and unlocked Codex skins.
 */
@Composable
fun CharacterMasterySummaryCard(
    character: Character,
    onNavigateToCodex: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMultiplier = remember(character.elementalMultipliers) {
        character.elementalMultipliers.values.sum()
    }

    // Milestones: 5.0x (Aura I), 10.0x (Aura II), 15.0x (Aura III)
    val nextMilestone = when {
        totalMultiplier < 5.0f -> 5.0f to "Elemental_Aura_I"
        totalMultiplier < 10.0f -> 10.0f to "Elemental_Aura_II"
        totalMultiplier < 15.0f -> 15.0f to "Elemental_Aura_III"
        else -> 15.0f to null
    }

    val prevTarget = when {
        totalMultiplier < 5.0f -> 0.0f
        totalMultiplier < 10.0f -> 5.0f
        totalMultiplier < 15.0f -> 10.0f
        else -> 15.0f
    }

    val progressValue = if (nextMilestone.second != null) {
        val range = nextMilestone.first - prevTarget
        ((totalMultiplier - prevTarget) / range).coerceIn(0f, 1f)
    } else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = progressValue,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "mastery_progress"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(MaterialTheme.colorScheme.primary, Color(0xFFFFB300))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 24.sp)
                    }

                    Column {
                        Text(
                            text = "${character.name} • Mistrovství Afinity",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Celková síla elementů: ${"%.2f".format(totalMultiplier)}x",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color(0xFFFFB300)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Tréninky: ${character.totalTrainingSessions}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Progress bar to next Codex cosmetic aura milestone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (nextMilestone.second != null) {
                        "Cíl: ${nextMilestone.second} (${"%.1f".format(nextMilestone.first)}x)"
                    } else {
                        "👑 Maximální aury odemčeny!"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(6.dp))
            ClipLinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Spacer(Modifier.height(14.dp))

            // Unlocked skins badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aury:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val auraMilestones = listOf(
                    "Elemental_Aura_I" to "Aura I (5x)",
                    "Elemental_Aura_II" to "Aura II (10x)",
                    "Elemental_Aura_III" to "Aura III (15x)"
                )

                auraMilestones.forEach { (skinId, label) ->
                    val isUnlocked = character.unlockedSkins.contains(skinId)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isUnlocked) {
                            Color(0xFFFFB300).copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isUnlocked) Color(0xFFFFB300) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = if (isUnlocked) "⭐" else "🔒",
                                fontSize = 10.sp
                            )
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Normal,
                                color = if (isUnlocked) Color(0xFFFFD54F) else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClipLinearProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(
                    Brush.horizontalGradient(
                        listOf(MaterialTheme.colorScheme.primary, Color(0xFFFFB300))
                    )
                )
        )
    }
}

/**
 * Individual Elemental Sparring Card.
 */
@Composable
fun ElementalSparringCard(
    elementInfo: ElementTrainingInfo,
    currentMultiplier: Float,
    isJustUpgraded: Boolean,
    onTrain: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamic scale pop when upgraded
    val bounceScale by animateFloatAsState(
        targetValue = if (isJustUpgraded) 1.03f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "card_scale"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isJustUpgraded) 2.dp else 1.dp,
            color = if (isJustUpgraded) elementInfo.primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .scale(bounceScale)
            .testTag("element_card_${elementInfo.element.name}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Element Icon Box
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    elementInfo.primaryColor.copy(alpha = 0.35f),
                                    elementInfo.secondaryColor.copy(alpha = 0.15f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            elementInfo.primaryColor.copy(alpha = 0.6f),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(elementInfo.icon, fontSize = 24.sp)
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = elementInfo.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = elementInfo.primaryColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${"%.2f".format(currentMultiplier)}x",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = elementInfo.primaryColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = elementInfo.combatRole,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = elementInfo.primaryColor
                    )
                    Text(
                        text = elementInfo.resonanceEffect,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Action Sparring Button
            Button(
                onClick = onTrain,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = elementInfo.primaryColor),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .height(42.dp)
                    .testTag("train_button_${elementInfo.element.name}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "+0.05x",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Visual Feedback Animation Overlay that triggers upon affinity multiplier increase.
 * Combines Lottie Animation, spring punch scaling, glowing radial aura, floating delta badge,
 * particle sparkles, and milestone celebration transitions.
 */
@Composable
fun AffinityMultiplierCelebrationOverlay(
    feedback: AffinityMultiplierFeedbackState,
    onDismiss: () -> Unit,
    onOpenElementalCodex: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Compose transitions
    val fadeAnim = remember(feedback.triggerId) { Animatable(0f) }
    val scaleAnim = remember(feedback.triggerId) { Animatable(0.4f) }
    val floatingBadgeY = remember(feedback.triggerId) { Animatable(28f) }
    val particleRadius = remember(feedback.triggerId) { Animatable(0f) }
    val particleAlpha = remember(feedback.triggerId) { Animatable(1f) }

    // Animated counter from old to new multiplier
    val counterAnim = remember(feedback.triggerId) { Animatable(feedback.oldMultiplier) }

    // Lottie Composition
    val lottieJson = remember(feedback.elementInfo) {
        AFFINITY_SURGE_LOTTIE_JSON
    }
    val composition by rememberLottieComposition(LottieCompositionSpec.JsonString(lottieJson))
    val lottieProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = 1.25f
    )

    // Infinite gentle glow pulse
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_float"
    )

    LaunchedEffect(feedback.triggerId) {
        // Reset states
        fadeAnim.snapTo(0f)
        scaleAnim.snapTo(0.4f)
        floatingBadgeY.snapTo(28f)
        particleRadius.snapTo(0f)
        particleAlpha.snapTo(1f)

        // Launch concurrent animations
        launch {
            fadeAnim.animateTo(1f, tween(150, easing = LinearEasing))
        }
        launch {
            scaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            delay(120)
            floatingBadgeY.animateTo(
                targetValue = -12f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            particleRadius.animateTo(110f, tween(900, easing = EaseOutQuad))
        }
        launch {
            delay(350)
            particleAlpha.animateTo(0f, tween(550, easing = LinearEasing))
        }
        launch {
            delay(100)
            counterAnim.animateTo(feedback.newMultiplier, tween(500, easing = EaseOutQuad))
        }

        // Keep active for feedback and celebratory feel, auto-close after 2.8s
        delay(2800)
        fadeAnim.animateTo(0f, tween(250, easing = LinearEasing))
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(fadeAnim.value)
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                coroutineScope.launch {
                    fadeAnim.animateTo(0f, tween(150))
                    onDismiss()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Radial Background Glow
        Box(
            modifier = Modifier
                .size(340.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            feedback.elementInfo.primaryColor.copy(alpha = 0.45f * pulseGlow),
                            feedback.elementInfo.secondaryColor.copy(alpha = 0.20f * pulseGlow),
                            Color.Transparent
                        )
                    )
                )
        )

        // Canvas Particle Burst radiating outwards
        Canvas(modifier = Modifier.size(240.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val particleCount = 14
            for (i in 0 until particleCount) {
                val angle = (i * (2 * Math.PI / particleCount)).toFloat()
                val dist = particleRadius.value
                val px = center.x + cos(angle.toDouble()).toFloat() * dist
                val py = center.y + sin(angle.toDouble()).toFloat() * dist
                drawCircle(
                    color = if (i % 2 == 0) feedback.elementInfo.primaryColor.copy(alpha = particleAlpha.value)
                    else feedback.elementInfo.secondaryColor.copy(alpha = particleAlpha.value),
                    radius = (4f + (i % 3) * 2f),
                    center = Offset(px, py)
                )
            }
        }

        // Main Content Card with Scale Punch
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scaleAnim.value)
                .padding(horizontal = 24.dp)
        ) {
            // Lottie Animation Container
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                if (composition != null) {
                    LottieAnimation(
                        composition = composition,
                        progress = { lottieProgress },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback visual icon
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(feedback.elementInfo.primaryColor.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(feedback.elementInfo.icon, fontSize = 48.sp)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Floating "+0.05x Multiplier" Badge
            Box(
                modifier = Modifier
                    .offset(y = floatingBadgeY.value.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(feedback.elementInfo.primaryColor, feedback.elementInfo.secondaryColor)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "+0.05x NÁSOBIČ ZVÝŠEN!",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color.Black,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Central Info Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF140C1A).copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            feedback.elementInfo.primaryColor,
                            feedback.elementInfo.secondaryColor,
                            feedback.elementInfo.primaryColor
                        )
                    )
                ),
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "AFINITA POSÍLENA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.4.sp,
                        color = feedback.elementInfo.primaryColor
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${feedback.characterName} • ${feedback.elementInfo.displayName}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(Modifier.height(14.dp))

                    // Counter Transition: Old -> New
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "${"%.2f".format(feedback.oldMultiplier)}x",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f)
                        )

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = feedback.elementInfo.primaryColor,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(20.dp)
                        )

                        Text(
                            text = "${"%.2f".format(counterAnim.value)}x",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD54F)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Celková suma mistrovství: ${"%.2f".format(feedback.totalMultiplierSum)}x",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    // Milestone celebration banner if a new skin was unlocked
                    AnimatedVisibility(
                        visible = feedback.newlyUnlockedSkin != null,
                        enter = fadeIn() + expandVertically()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFFD54F).copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("👑", fontSize = 16.sp)
                                    Text(
                                        text = "NOVÝ VZHLED V KODEXU!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFFD54F)
                                    )
                                }
                                Text(
                                    text = feedback.newlyUnlockedSkin ?: "",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Dismiss button
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = feedback.elementInfo.primaryColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("affinity_celebration_dismiss_button")
                    ) {
                        Text(
                            text = "Pokračovat v tréninku",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    if (onOpenElementalCodex != null) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onOpenElementalCodex()
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                feedback.elementInfo.primaryColor.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("affinity_celebration_open_codex_button")
                        ) {
                            Icon(
                                Icons.Default.AutoStories,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = feedback.elementInfo.primaryColor
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Číst původ v Elementárním Kodexu",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Self-contained vector Lottie JSON animation for the Affinity Multiplier surge.
 * Includes radiant expanding rings, flashing starburst core, and orbiting energy orbs.
 */
private const val AFFINITY_SURGE_LOTTIE_JSON = """
{
  "v": "5.7.4", "fr": 30, "ip": 0, "op": 45, "w": 200, "h": 200, "nm": "Affinity Multiplier Surge", "ddd": 0, "assets": [],
  "layers": [
    {
      "ddd": 0, "ind": 1, "ty": 4, "nm": "Outward Pulse Ring", "sr": 1,
      "ks": {
        "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 8, "s": [100] }, { "t": 32, "s": [80] }, { "t": 45, "s": [0] }] },
        "p": { "a": 0, "k": [100, 100, 0] },
        "s": { "a": 1, "k": [{ "t": 0, "s": [20, 20] }, { "t": 45, "s": [160, 160] }] }
      },
      "shapes": [
        {
          "ty": "gr",
          "it": [
            { "d": 1, "ty": "el", "s": { "a": 0, "k": [90, 90] }, "p": { "a": 0, "k": [0, 0] } },
            { "ty": "st", "c": { "a": 0, "k": [1, 0.84, 0, 1] }, "w": { "a": 0, "k": 6 }, "o": { "a": 0, "k": 90 } }
          ]
        }
      ]
    },
    {
      "ddd": 0, "ind": 2, "ty": 4, "nm": "Rotating Starburst", "sr": 1,
      "ks": {
        "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 6, "s": [100] }, { "t": 30, "s": [100] }, { "t": 45, "s": [0] }] },
        "r": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 45, "s": [120] }] },
        "p": { "a": 0, "k": [100, 100, 0] },
        "s": { "a": 1, "k": [{ "t": 0, "s": [25, 25] }, { "t": 16, "s": [130, 130] }, { "t": 45, "s": [145, 145] }] }
      },
      "shapes": [
        {
          "ty": "gr",
          "it": [
            { "d": 1, "ty": "rc", "s": { "a": 0, "k": [8, 90] }, "p": { "a": 0, "k": [0, 0] }, "r": { "a": 0, "k": 2 } },
            { "d": 1, "ty": "rc", "s": { "a": 0, "k": [90, 8] }, "p": { "a": 0, "k": [0, 0] }, "r": { "a": 0, "k": 2 } },
            { "d": 1, "ty": "rc", "s": { "a": 0, "k": [8, 90] }, "p": { "a": 0, "k": [0, 0] }, "r": { "a": 0, "k": 45 } },
            { "ty": "fl", "c": { "a": 0, "k": [1, 0.5, 0, 1] }, "o": { "a": 0, "k": 95 } }
          ]
        }
      ]
    },
    {
      "ddd": 0, "ind": 3, "ty": 4, "nm": "Radiant Center Core", "sr": 1,
      "ks": {
        "o": { "a": 1, "k": [{ "t": 0, "s": [0] }, { "t": 10, "s": [100] }, { "t": 35, "s": [70] }, { "t": 45, "s": [0] }] },
        "p": { "a": 0, "k": [100, 100, 0] },
        "s": { "a": 1, "k": [{ "t": 0, "s": [15, 15] }, { "t": 18, "s": [115, 115] }, { "t": 45, "s": [130, 130] }] }
      },
      "shapes": [
        {
          "ty": "gr",
          "it": [
            { "d": 1, "ty": "el", "s": { "a": 0, "k": [50, 50] }, "p": { "a": 0, "k": [0, 0] } },
            { "ty": "fl", "c": { "a": 0, "k": [1, 1, 0.7, 1] }, "o": { "a": 0, "k": 95 } }
          ]
        }
      ]
    }
  ]
}
"""
