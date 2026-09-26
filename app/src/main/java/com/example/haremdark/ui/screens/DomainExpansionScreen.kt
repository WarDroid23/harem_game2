package com.example.haremdark.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.haremdark.R
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.DomainData
import com.example.haremdark.data.StaticData
import com.example.haremdark.domain.DomainEconomyManager
import com.example.haremdark.domain.EconomyStructureDef
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.NavSound
import com.example.haremdark.domain.ProceduralEventScenario
import com.example.haremdark.domain.ProceduralHaremEventEngine
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.Building
import com.example.haremdark.models.Character
import com.example.haremdark.models.DomainLocation
import com.example.haremdark.models.GameSave
import com.example.haremdark.ui.components.ProceduralEventModal

data class StoryChapterDef(
    val chapterNumber: Int,
    val id: String,
    val title: String,
    val subtitle: String,
    val requiredDomainLevel: Int,
    val requiredHaremSize: Int,
    val description: String,
    val loreStory: String,
    val rewardGold: Int,
    val rewardSexEnergy: Int,
    val rewardTitle: String,
    val domainLocationId: String
)

val STORY_CHAPTERS = listOf(
    StoryChapterDef(
        chapterNumber = 1,
        id = "chapter_1",
        title = "Kapitola I: Stíny Hvozdu a Založení Pevnosti",
        subtitle = "Probuzení vládce stínů a podmanění prvních otrokyň",
        requiredDomainLevel = 1,
        requiredHaremSize = 0,
        description = "Začínáš svou vládu na troskách starobylé pevnosti. Lesy kolem šeptají o staré magii a zbloudilé dívky hledají tvou ochranu.",
        loreStory = "Tvůj meč a temná aura si získaly první následovnice. Na ruinách starého panství začíná vyrůstat nedobytné dominium. Dcery tvého harému v tobě spatřují nejen pána, ale i jedinou naději v tomto krutém světě.",
        rewardGold = 100,
        rewardSexEnergy = 10,
        rewardTitle = "Vládce Hvozdu",
        domainLocationId = "temny_hvozd"
    ),
    StoryChapterDef(
        chapterNumber = 2,
        id = "chapter_2",
        title = "Kapitola II: Krvavá Křižovatka a Tajemství Hostince",
        subtitle = "Rozšíření vlivu na obchodní stezky a podsvětí",
        requiredDomainLevel = 2,
        requiredHaremSize = 2,
        description = "Krvavá Mary nabízí spojenectví. Získej kontrolu nad hostincem a otevři obchodní stezky pro stálý přísun zlata a rozkoše.",
        loreStory = "Křižovatka obchodních karavan nyní odvádí desátky do tvé pokladnice. Barmanky a společnice z celého kraje šíří pověsti o tvé nezkrotné moci. Každá noc v dominiu se stává velkolepou oslavou tvého vítězství.",
        rewardGold = 250,
        rewardSexEnergy = 15,
        rewardTitle = "Pán Křižovatky",
        domainLocationId = "hostinec_u_krvave_panny"
    ),
    StoryChapterDef(
        chapterNumber = 3,
        id = "chapter_3",
        title = "Kapitola III: Ztracený Chrám Luny a Posvátné Rituály",
        subtitle = "Podmanění sakrálních kněžek a prastaré měsíční magie",
        requiredDomainLevel = 3,
        requiredHaremSize = 3,
        description = "V ruinách chrámu Luny se ukrývají posvátné panny. Prolom jejich magické bariéry a přijmi jejich věčnou oddanost.",
        loreStory = "Posvátný chrám padl pod tvou nadvládu. Kněžky, které dříve sloužily bohům, nyní své modlitby a těla obětují tobě. Měsíční magie proudí tvými žilami a posiluje celou tvou armádu.",
        rewardGold = 500,
        rewardSexEnergy = 25,
        rewardTitle = "Posvátný Dobyvatel",
        domainLocationId = "ruiny_chramu"
    ),
    StoryChapterDef(
        chapterNumber = 4,
        id = "chapter_4",
        title = "Kapitola IV: Krvavé Katakomby a Vzestup Temného Císaře",
        subtitle = "Poražení inkvizice a absolutní dominance nad šlechtou",
        requiredDomainLevel = 4,
        requiredHaremSize = 5,
        description = "Šlechta se chvěje strachem. Pronikni do podzemních katakomb a ukaž celému světu, kdo je skutečným pánem osudu.",
        loreStory = "Katakomby zčervenaly krví tvých nepřátel. Šlechtické dcery klečí u tvého trůnu a prosí o tvou milost. Tvé dominium je nyní nezastavitelnou říší strachu i bezmezné rozkoše.",
        rewardGold = 1000,
        rewardSexEnergy = 40,
        rewardTitle = "Krvavý Císař",
        domainLocationId = "krvave_katakomby"
    ),
    StoryChapterDef(
        chapterNumber = 5,
        id = "chapter_5",
        title = "Kapitola V: Astrální Citadela a Věčná Nadvláda",
        subtitle = "Vzestup do nebeských sfér a podmanění nejvyšších kouzelnic",
        requiredDomainLevel = 5,
        requiredHaremSize = 8,
        description = "Levitující citadela kouzelnic byla prolomena. Získej absolutní kontrolu nad časem a magií všech sfér.",
        loreStory = "Astrální sféra se sklonila před tvou vůlí. Hvězdy září tvým jménem a tvůj harém se stal legendou přesahující samotný čas. Tvá moc je věčná a tvé slovo zákonem pro všechny bytosti.",
        rewardGold = 2500,
        rewardSexEnergy = 60,
        rewardTitle = "Astrální Pán Všehomíra",
        domainLocationId = "astralni_citadela"
    )
)

data class ImperialDecree(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val durationText: String,
    val effectDescription: String,
    val costGold: Int,
    val costEnergy: Int
)

val IMPERIAL_DECREES = listOf(
    ImperialDecree(
        id = "dec_festival",
        name = "Noční festival rozkoše",
        icon = "🍷",
        description = "Vyhlásit celonoční slavnost v lázních a komnatách s hudbou a drahým vínem.",
        durationText = "24 hodin",
        effectDescription = "+30% Zisk sexuální energie a +15 Loajalita všem otrokyním",
        costGold = 150,
        costEnergy = 15
    ),
    ImperialDecree(
        id = "dec_taxes",
        name = "Královské mimořádné daně",
        icon = "👑",
        description = "Uvalit tvrdé daně a cla na všechny obchodní stezky v ovládaných provinciích.",
        durationText = "Okamžitý výnos",
        effectDescription = "+400 Zlata do pokladnice, +10% pasivní produkce",
        costGold = 0,
        costEnergy = 10
    ),
    ImperialDecree(
        id = "dec_garrison",
        name = "Železná pevnostní stráž",
        icon = "🛡️",
        description = "Posílit hlídky na hradbách a vyzbrojit stráže temnou ocelí proti nájezdům.",
        durationText = "Permanentní ochrana",
        effectDescription = "+50 Obrana pevnosti a -80% šance na povstání provincií",
        costGold = 200,
        costEnergy = 20
    ),
    ImperialDecree(
        id = "dec_alchemy_transmute",
        name = "Alchymistická transmutace esence",
        icon = "⚗️",
        description = "Využít chrámové reaktory k přeměně surové many na čisté dračí zlato.",
        durationText = "Rituální transmutace",
        effectDescription = "Okamžitě přemění 30 Many na 600 Zlata",
        costGold = 0,
        costEnergy = 25
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomainExpansionScreen(
    gameState: GameSave,
    engine: GameEngine,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val player = gameState.player
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var activeChapterStory by remember { mutableStateOf<StoryChapterDef?>(null) }
    var claimedChapters by remember { mutableStateOf(setOf<String>()) }
    var activeDecrees by remember { mutableStateOf(setOf<String>()) }
    var activeProceduralScenario by remember { mutableStateOf<ProceduralEventScenario?>(null) }

    // Concubine worker assignments (buildingId -> characterId)
    var assignedWorkers by remember { mutableStateOf(mapOf(
        "throne_room" to (gameState.characters.firstOrNull { it.oblibena || it.jeManzelkou }?.id ?: gameState.characters.firstOrNull()?.id),
        "manova_vez" to (gameState.characters.firstOrNull { it.archetypeId == "touha" || it.archetypeId == "posedla" }?.id),
        "zelezny_dul" to (gameState.characters.firstOrNull { it.archetypeId == "slechticna" || it.archetypeId == "manipulativni" }?.id),
        "pila" to (gameState.characters.firstOrNull { it.archetypeId == "odvazna" }?.id),
        "kamenolom" to (gameState.characters.firstOrNull { it.archetypeId == "draci_divka" || it.archetypeId == "vzdorna" }?.id)
    )) }

    val domainLvl = gameState.domainExpansionLevel.coerceAtLeast(1)
    val economyRates = DomainEconomyManager.calculateProduction(gameState, assignedWorkers)
    val maxCapacity = 5 + (domainLvl * 5) + (gameState.buildings.find { it.type == "ubytovny" }?.level ?: 0) * 4

    val upgradeCostGold = domainLvl * 600
    val upgradeCostDark = domainLvl * 15

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "🏰 Správa Dominia & Říše",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Úroveň dominia $domainLvl • Kapacita: ${gameState.characters.size}/$maxCapacity dívek",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = {
                            SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                            HapticManager.vibrateClick()
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                        }
                    }
                },
                actions = {
                    val isMuted by SoundEffectManager.isMuted.collectAsState()
                    val isHaptics by HapticManager.isHapticsEnabledFlow.collectAsState()

                    IconButton(
                        onClick = {
                            SoundEffectManager.toggleMute()
                            HapticManager.vibrateClick()
                            Toast.makeText(context, if (isMuted) "🔊 Zvuky zapnuty" else "🔇 Zvuky ztišeny", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Zvuk",
                            tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            HapticManager.toggleHaptics()
                            HapticManager.vibrateClick()
                            Toast.makeText(context, if (isHaptics) "📳 Vibrace vypnuty" else "📳 Vibrace zapnuty", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = if (isHaptics) Icons.Default.Vibration else Icons.Default.DoNotDisturbOn,
                            contentDescription = "Haptika",
                            tint = if (isHaptics) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Live Economy Resource Header (Wood, Stone, Mana, Gold, Energy)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ResourceGaugeItem("🪵", "${player.wood}/${economyRates.maxWoodCapacity}", "+${economyRates.woodPerDay}/d", Color(0xFF8D6E63))
                    ResourceGaugeItem("🪨", "${player.stone}/${economyRates.maxStoneCapacity}", "+${economyRates.stonePerDay}/d", Color(0xFF9E9E9E))
                    ResourceGaugeItem("🔮", "${player.mana}/${economyRates.maxManaCapacity}", "+${economyRates.manaPerDay}/d", Color(0xFF00E5FF))
                    ResourceGaugeItem("💰", "${player.gold}/${economyRates.maxGoldCapacity}", "+${economyRates.goldPerDay}/d", Color(0xFFFFD700))
                    ResourceGaugeItem("⚡", "${player.sexEnergy}/${player.maxSexEnergy}", "+${economyRates.sexEnergyPerDay}/d", Color(0xFFFF4081))
                }
            }

            // Hero Banner with strategy citadel image and Procedural Event trigger
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_empire_citadel_banner_1790459445540),
                    contentDescription = "Citadela Dominia",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "👑 Pevnost Dominia (Stupeň $domainLvl)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            "Denní výnos: +${economyRates.woodPerDay}🪵 +${economyRates.stonePerDay}🪨 +${economyRates.manaPerDay}🔮 +${economyRates.goldPerDay}💰",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                val char = gameState.characters.randomOrNull() ?: return@Button
                                SoundEffectManager.playRelationshipTier(AffinityData.getTierForPoints(char.affinityPoints).level)
                                HapticManager.vibrateClick()
                                activeProceduralScenario = ProceduralHaremEventEngine.generateProceduralEvent(
                                    character = char,
                                    locationId = gameState.currentDomainId,
                                    player = player
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("🎲 Událost", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (player.gold >= upgradeCostGold && player.darkEnergy >= upgradeCostDark) {
                                    SoundEffectManager.playEventTrigger()
                                    HapticManager.vibrateHeavy()
                                    player.gold -= upgradeCostGold
                                    player.darkEnergy -= upgradeCostDark
                                    Toast.makeText(context, "🏰 Dominium úspěšně povýšeno!", Toast.LENGTH_LONG).show()
                                } else {
                                    SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                                    HapticManager.vibrateClick()
                                    Toast.makeText(context, "Nedostatek zdrojů: Potřebuješ $upgradeCostGold💰 a $upgradeCostDark⚡", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Vylepšit (${upgradeCostGold}💰)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Strategy Tab Navigation
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "🏭 Ekonomika & Stavby",
                    "👩‍🦰 Přiřazení Otrokyň",
                    "📜 Císařské Dekrety",
                    "📖 Příběhové Kapitoly",
                    "🗺️ Provincie & Teritoria"
                ).forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = {
                            SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                            HapticManager.vibrateClick()
                            selectedTab = idx
                        },
                        text = {
                            Text(
                                label,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                when (selectedTab) {
                    0 -> EconomyBuildingsTab(
                        gameState = gameState,
                        engine = engine,
                        rates = economyRates,
                        onHarvest = {
                            SoundEffectManager.playAffinityGain()
                            HapticManager.vibrateHeavy()
                            val (success, msg) = engine.harvestDomainEconomy(assignedWorkers)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    )
                    1 -> ConcubineWorkerAssignmentTab(
                        characters = gameState.characters,
                        assignedWorkers = assignedWorkers,
                        onAssign = { bId, cId ->
                            assignedWorkers = assignedWorkers.toMutableMap().apply {
                                if (cId != null) put(bId, cId) else remove(bId)
                            }
                            SoundEffectManager.playAffinityGain()
                            HapticManager.vibrateClick()
                            Toast.makeText(context, "Dívka úspěšně přiřazena k provozu budovy!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    2 -> ImperialDecreesTab(
                        player = player,
                        activeDecrees = activeDecrees,
                        onEnactDecree = { decree ->
                            if (player.gold >= decree.costGold && player.darkEnergy >= decree.costEnergy) {
                                activeDecrees = activeDecrees + decree.id
                                SoundEffectManager.playEventTrigger()
                                HapticManager.vibrateHeavy()
                                Toast.makeText(context, "📜 Dekret '${decree.name}' byl vyhlášen!", Toast.LENGTH_LONG).show()
                            } else {
                                SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                                HapticManager.vibrateClick()
                                Toast.makeText(context, "Nedostatek zdrojů pro vyhlášení dekretu!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    3 -> StoryChaptersTab(
                        domainLevel = domainLvl,
                        haremSize = gameState.characters.size,
                        claimedChapters = claimedChapters,
                        onReadLore = { chapter ->
                            SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                            activeChapterStory = chapter
                        },
                        onClaimReward = { chapter ->
                            claimedChapters = claimedChapters + chapter.id
                            SoundEffectManager.playEventTrigger()
                            HapticManager.vibrateHeavy()
                            Toast.makeText(context, "🎉 Získáno: ${chapter.rewardGold}💰, ${chapter.rewardSexEnergy}⚡ a titul '${chapter.rewardTitle}'!", Toast.LENGTH_LONG).show()
                        }
                    )
                    4 -> KingdomTerritoryTab(
                        gameState = gameState,
                        engine = engine
                    )
                }
            }
        }
    }

    // Procedural Event Encounter Modal
    activeProceduralScenario?.let { scenario ->
        val char = gameState.characters.find { it.id == scenario.characterId } ?: gameState.characters.first()
        ProceduralEventModal(
            scenario = scenario,
            character = char,
            player = player,
            onChoiceSelected = { choice ->
                ProceduralHaremEventEngine.applyChoiceOutcome(scenario, choice, char, player)
            },
            onDismiss = {
                activeProceduralScenario = null
            }
        )
    }

    // Story Dialog
    activeChapterStory?.let { chapter ->
        AlertDialog(
            onDismissRequest = { activeChapterStory = null },
            title = {
                Text(
                    chapter.title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        chapter.subtitle,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFD700),
                        fontSize = 13.sp
                    )
                    HorizontalDivider()
                    Text(
                        chapter.loreStory,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { activeChapterStory = null }) {
                    Text("Zavřít kroniku")
                }
            }
        )
    }
}

@Composable
fun ResourceGaugeItem(
    icon: String,
    current: String,
    rate: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(icon, fontSize = 11.sp)
            Text(current, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Text(rate, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun EconomyBuildingsTab(
    gameState: GameSave,
    engine: GameEngine,
    rates: com.example.haremdark.domain.ResourceProductionRate,
    onHarvest: () -> Unit
) {
    val context = LocalContext.current
    val structures = DomainEconomyManager.STRUCTURES
    val player = gameState.player

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🌾 Sklizeň hospodářství dominia", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        Text("+${rates.woodPerDay / 2}🪵  +${rates.stonePerDay / 2}🪨  +${rates.manaPerDay / 2}🔮  +${rates.goldPerDay / 2}💰  +${rates.sexEnergyPerDay / 2}⚡", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFD700))
                    }
                    Button(
                        onClick = onHarvest,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Sklidit 🌾", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        items(structures) { struct ->
            val existing = gameState.buildings.find { it.type == struct.type }
            val level = existing?.level ?: 0
            val nextLevel = level + 1
            val woodCost = struct.requiredWoodCost(nextLevel)
            val stoneCost = struct.requiredStoneCost(nextLevel)
            val goldCost = struct.requiredGoldCost(nextLevel)
            val darkCost = struct.requiredDarkCost(nextLevel)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(struct.icon, fontSize = 22.sp)
                            Column {
                                Text(struct.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Úroveň $level / 10 • ${struct.category}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Button(
                            onClick = {
                                SoundEffectManager.playAffinityGain()
                                HapticManager.vibrateClick()
                                val (success, msg) = engine.upgradeEconomyBuilding(struct.type)
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Stavět ($goldCost💰)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        struct.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Resource costs row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text("Cena: $woodCost🪵  $stoneCost🪨  $goldCost💰" + (if (darkCost > 0) "  $darkCost⚡" else ""), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (struct.workerSynergyDescription.isNotEmpty()) {
                        Text(
                            "Synergie: ${struct.workerSynergyDescription}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConcubineWorkerAssignmentTab(
    characters: List<Character>,
    assignedWorkers: Map<String, String?>,
    onAssign: (String, String?) -> Unit
) {
    val buildingSlots = listOf(
        Triple("throne_room", "👑 Trůnní sál (Správkyně harému)", "+4⚡ Sexuální energie & +15% prestiž"),
        Triple("pila", "🪵 Dřevorubecká pila (Dozor nad těžbou)", "+40% Těžba černého dřeva 🪵"),
        Triple("kamenolom", "🪨 Kamenolom dominia (Lámání mramoru)", "+45% Těžba masivního kamene 🪨"),
        Triple("manova_vez", "🔮 Věž Arcana (Mistryně rituálů)", "+50% Zisk astrální many 🔮"),
        Triple("zelezny_dul", "💰 Zlaté doly & Mincovna (Správkyně daní)", "+35% Výnos císařského zlata 💰")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("💡", fontSize = 20.sp)
                    Text(
                        "Přiřazením otrokyň k hospodářským budovám aktivuješ jejich specifické archetypální synergie pro maximální těžbu dřeva, kamene, many i zlata.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        items(buildingSlots) { (bId, bTitle, bBonus) ->
            val assignedCharId = assignedWorkers[bId]
            val assignedChar = characters.find { it.id == assignedCharId }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(bTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        Text(bBonus, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFD700))
                    }

                    if (assignedChar != null) {
                        val portraitRes = StaticData.getPortraitForArchetype(assignedChar.archetypeId)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current).data(portraitRes).crossfade(true).build(),
                                    contentDescription = assignedChar.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, Color(0xFFFFD700), CircleShape)
                                )
                                Column {
                                    Text(assignedChar.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Loajalita: ${assignedChar.loajalita}% • ${assignedChar.statusIcon}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            TextButton(onClick = { onAssign(bId, null) }) {
                                Text("Odebrat", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                            }
                        }
                    } else {
                        Text("⚠️ Žádná dívka není přiřazena.", fontSize = 11.sp, color = Color.Gray)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            characters.take(3).forEach { char ->
                                OutlinedButton(
                                    onClick = { onAssign(bId, char.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(4.dp)
                                ) {
                                    Text(char.name, fontSize = 10.sp, maxLines = 1)
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
fun ImperialDecreesTab(
    player: com.example.haremdark.models.Player,
    activeDecrees: Set<String>,
    onEnactDecree: (ImperialDecree) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(IMPERIAL_DECREES) { decree ->
            val isActive = activeDecrees.contains(decree.id)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (isActive) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(decree.icon, fontSize = 20.sp)
                            Column {
                                Text(decree.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(decree.durationText, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        if (isActive) {
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFFD700).copy(alpha = 0.2f)) {
                                Text("Aktivní ✨", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        } else {
                            Button(
                                onClick = { onEnactDecree(decree) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Vyhlásit (${decree.costGold}💰)", fontSize = 11.sp)
                            }
                        }
                    }

                    Text(decree.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Účinek: ${decree.effectDescription}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4CAF50))
                }
            }
        }
    }
}

@Composable
fun StoryChaptersTab(
    domainLevel: Int,
    haremSize: Int,
    claimedChapters: Set<String>,
    onReadLore: (StoryChapterDef) -> Unit,
    onClaimReward: (StoryChapterDef) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(STORY_CHAPTERS) { chapter ->
            val isUnlocked = domainLevel >= chapter.requiredDomainLevel && haremSize >= chapter.requiredHaremSize
            val isClaimed = claimedChapters.contains(chapter.id)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (isUnlocked) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)),
                colors = CardDefaults.cardColors(containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(chapter.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isUnlocked) MaterialTheme.colorScheme.primary else Color.Gray)
                            Text(chapter.subtitle, fontSize = 11.sp, color = Color(0xFFFFD700))
                        }

                        if (!isUnlocked) {
                            Text("🔒 Vyžaduje dominium ${chapter.requiredDomainLevel}", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Text(chapter.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (isUnlocked) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onReadLore(chapter) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("📖 Číst kroniku", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { onClaimReward(chapter) },
                                enabled = !isClaimed,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isClaimed) "Vybráno ✓" else "Odměna 🎁", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KingdomTerritoryTab(
    gameState: GameSave,
    engine: GameEngine
) {
    val context = LocalContext.current
    val locations = DomainData.DOMAINS

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(locations) { loc ->
            val isCurrent = gameState.currentDomainId == loc.id

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (isCurrent) Color(0xFFFFD700) else Color.Transparent),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🌲", fontSize = 18.sp)
                            Text(loc.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        if (isCurrent) {
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFFD700).copy(alpha = 0.2f)) {
                                Text("Aktuální základna ★", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    SoundEffectManager.playNavigation(NavSound.MENU_CLICK)
                                    HapticManager.vibrateClick()
                                    val (success, msg) = engine.travelToDomain(loc.id)
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Přesunout sídlo", fontSize = 10.sp)
                            }
                        }
                    }

                    Text(loc.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
