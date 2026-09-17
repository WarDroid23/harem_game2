package com.example.haremdark.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.haremdark.R
import com.example.haremdark.data.DomainData
import com.example.haremdark.data.GameContent
import com.example.haremdark.models.DomainLocation
import com.example.haremdark.models.GameSave

import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun MinimapOverlay(
    gameState: GameSave,
    selectedDomainId: String,
    onDomainSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    isFogEnabled: Boolean = true
) {
    // Pulsing animation for the current player location
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    // New discovery pulse for markers
    val discoveryPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "discoveryPulse"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Bouncing animation for active quest markers
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceY"
    )

    // Fog-of-war reveal animation pulse for unlocked domains
    val fogPulseRadius by infiniteTransition.animateFloat(
        initialValue = 25f,
        targetValue = 70f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fogPulseRadius"
    )
    val fogPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fogPulseAlpha"
    )

    val scanlineAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "scanline"
    )

    val scanRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "rotation"
    )

    val leyLinePulse by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "leyLinePulse"
    )

    val activeQuestDomains = remember(gameState) {
        GameContent.QUESTS.filter { quest ->
            !gameState.completedQuests.contains(quest.id) && gameState.player.level >= quest.reqLevel
        }.mapNotNull { quest ->
            val domainId = when (quest.id) {
                "quest_1" -> "temny_hvozd"
                "quest_2" -> "stoky_doupata"
                "quest_3" -> "slechticke_panstvi"
                "quest_4" -> "ruiny_chramu"
                else -> null
            }
            if (domainId != null) domainId to quest else null
        }.toMap()
    }

    var showLegend by remember { mutableStateOf(false) }
    var domainPopupId by remember { mutableStateOf<String?>(null) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            // Title & Map controls header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Minimapa",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Taktická Minimapa Dominií",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                TextButton(
                    onClick = { showLegend = !showLegend },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(
                        imageVector = if (showLegend) Icons.Default.Info else Icons.Default.Info,
                        contentDescription = "Vysvětlivky",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showLegend) "Skrýt legendu" else "Zobrazit legendu",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                // 1. Fantasy Map Background Image
                Image(
                    painter = painterResource(id = R.drawable.img_fantasy_map),
                    contentDescription = "Pozadí mapy",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // 2. Dark fantasy magic radar overlay vignette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x33000000),
                                    Color(0xBB12081C)
                                )
                            )
                        )
                )

                // 2.5. Tactical Scanlines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val h = size.height
                    val w = size.width
                    val y = scanlineAnim * h
                    drawLine(
                        color = Color(0x3380D8FF),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 2f
                    )
                    
                    // Draw some floating motes (particles)
                    val time = (System.currentTimeMillis() % 10000) / 10000f
                    for (i in 0..15) {
                        val px = (i * 12345.67f % 1f) * w
                        val py = ((i * 9876.54f % 1f) + time) % 1f * h
                        drawCircle(
                            color = Color(0x44FF4081),
                            radius = 2f,
                            center = Offset(px, py)
                        )
                    }
                }

                // 3. Grid overlay, connecting paths and scanlines drawn via Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Connections between domains
                    DomainData.MAP_CONNECTIONS.forEach { (srcId, destId) ->
                        val src = DomainData.DOMAINS.find { it.id == srcId }
                        val dest = DomainData.DOMAINS.find { it.id == destId }
                        if (src != null && dest != null) {
                            val startX = src.mapX * w
                            val startY = src.mapY * h
                            val endX = dest.mapX * w
                            val endY = dest.mapY * h

                            val srcExplored = gameState.unlockedDomains.contains(srcId) || gameState.currentDomainId == srcId
                            val destExplored = gameState.unlockedDomains.contains(destId) || gameState.currentDomainId == destId

                            if (srcExplored && destExplored) {
                                drawLine(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFFFF4081).copy(alpha = 0.2f * leyLinePulse), Color(0xFF80D8FF).copy(alpha = 0.4f * leyLinePulse), Color(0xFFFF4081).copy(alpha = 0.2f * leyLinePulse)),
                                        start = Offset(startX, startY),
                                        end = Offset(endX, endY)
                                    ),
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = 6f * leyLinePulse
                                )
                            }
                        }
                    }
                }

                // 3.5 ADVANCED SHADER-BASED FOG OF WAR
                if (isFogEnabled) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    ) {
                        val w = size.width
                        val h = size.height
                        
                        // First draw a solid "Fog" layer over the entire map
                        drawRect(color = Color(0xF90A0412))
                        
                        // Reveal areas around explored nodes using DstOut
                        DomainData.DOMAINS.forEach { d ->
                            val isExplored = gameState.unlockedDomains.contains(d.id) || gameState.currentDomainId == d.id
                            if (isExplored) {
                                val fx = d.mapX * w
                                val fy = d.mapY * h
                                
                                // Radial "hole" in the fog
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.1f), Color.Black),
                                        center = Offset(fx, fy),
                                        radius = 120f
                                    ),
                                    radius = 120f,
                                    center = Offset(fx, fy),
                                    blendMode = BlendMode.DstOut
                                )
                                
                                // Secondary glow reveal
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color.Transparent, Color.Black),
                                        center = Offset(fx, fy),
                                        radius = 60f
                                    ),
                                    radius = 60f,
                                    center = Offset(fx, fy),
                                    blendMode = BlendMode.DstOut
                                )
                            }
                        }
                    }
                }

                // 4. Domain Markers & Floating Labels
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("A1", "B2", "C3", "D4", "E5", "F6").forEach {
                            Text(it, color = Color(0x6680D8FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("X-S1", "X-S2", "X-S3", "X-S4", "X-S5", "X-S6").forEach {
                            Text(it, color = Color(0x6680D8FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 5. Points of Interest (POIs) drawn dynamically as styled nodes
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val mapWidth = maxWidth
                    val mapHeight = maxHeight

                    DomainData.DOMAINS.forEach { domain ->
                        val isCurrent = gameState.currentDomainId == domain.id
                        val isSelected = selectedDomainId == domain.id
                        val isExplored = gameState.unlockedDomains.contains(domain.id) || isCurrent
                        val isUnlockedByLevel = gameState.player.level >= domain.minPlayerLevel

                        val xOffset = mapWidth * domain.mapX - 16.dp
                        val yOffset = mapHeight * domain.mapY - 16.dp

                        Box(
                            modifier = Modifier
                                .offset(x = xOffset, y = yOffset)
                                .size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Discovery Pulse Animation for newly unlocked/highlighted nodes
                            if (isExplored) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp * discoveryPulse)
                                        .clip(CircleShape)
                                        .background(Color(domain.accentColor).copy(alpha = 0.2f * (2f - discoveryPulse)))
                                        .border(1.dp, Color(domain.accentColor).copy(alpha = 0.4f * (2f - discoveryPulse)), CircleShape)
                                )
                            }

                            // Pulsing glowing ring for current player location
                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp * pulseScale)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFD700).copy(alpha = 0.25f * pulseAlpha))
                                        .border(1.5.dp, Color(0xFFFFD700).copy(alpha = pulseAlpha), CircleShape)
                                )
                            }

                            // Selection halo border (only visible if explored)
                            if (isSelected && isExplored) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                // Rotating tactical scanner ring
                                Canvas(modifier = Modifier.size(44.dp)) {
                                    drawArc(
                                        color = Color(0xFF80D8FF),
                                        startAngle = scanRotation,
                                        sweepAngle = 90f,
                                        useCenter = false,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                    )
                                    drawArc(
                                        color = Color(0xFF80D8FF),
                                        startAngle = scanRotation + 180f,
                                        sweepAngle = 90f,
                                        useCenter = false,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                    )
                                }
                            }

                            // Interactive domain core marker
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCurrent -> Color(0xFFFFD700)
                                            !isExplored -> Color(0xFF1B1B1E) // Mist charcoal core
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            else -> Color(domain.accentColor)
                                        }
                                    )
                                    .clickable { 
                                        if (isExplored) {
                                            domainPopupId = domain.id
                                        }
                                        onDomainSelect(domain.id) 
                                    }
                                    .border(
                                        width = 1.dp,
                                        color = when {
                                            isCurrent -> Color.Black
                                            !isExplored -> Color(0x33FFFFFF)
                                            else -> Color.White.copy(alpha = 0.6f)
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Mini icon on map (hidden under fog of war if unexplored)
                                when {
                                    isCurrent -> {
                                        Text(
                                            text = "👑",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    !isExplored -> {
                                        Text(
                                            text = "?",
                                            color = Color(0xFF757575),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    domain.npcTrader -> {
                                        Text(
                                            text = "🛒",
                                            fontSize = 10.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    domain.bossId != null -> {
                                        Text(
                                            text = "💀",
                                            fontSize = 10.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    !isUnlockedByLevel -> {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            modifier = Modifier.size(10.dp),
                                            tint = Color.LightGray
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = "⚡",
                                            color = Color(0xFFFFD700),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // Quest Exclamation Mark above the node
                            val activeQuest = activeQuestDomains[domain.id]
                            if (activeQuest != null && isExplored) {
                                Box(
                                    modifier = Modifier
                                        .offset(y = -22.dp + bounceY.dp)
                                        .background(Color(0xFFFFC107), RoundedCornerShape(4.dp))
                                        .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "❗",
                                        color = Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Minimal tooltip / label below/above active markers
                            Box(
                                modifier = Modifier
                                    .offset(y = 18.dp)
                                    .background(Color(0xCC0E0318), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                             ) {
                                Text(
                                    text = if (isExplored) domain.name else "???",
                                    color = if (isCurrent) Color(0xFFFFD700) else if (isExplored) Color.White else Color(0xFF616161),
                                    fontSize = 8.sp,
                                    fontWeight = if (isCurrent || (isSelected && isExplored)) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }

                            // Quick Summary Popup
                            if (domainPopupId == domain.id) {
                                Popup(
                                    alignment = Alignment.TopCenter,
                                    offset = IntOffset(0, -60),
                                    onDismissRequest = { domainPopupId = null }
                                ) {
                                    Surface(
                                        color = Color(0xEE12081C),
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(domain.accentColor)),
                                        modifier = Modifier.width(150.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(domain.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                                            Text("Obtížnost: ${domain.difficulty}", color = Color.LightGray, fontSize = 9.sp)
                                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                            Text("Zdroje:", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700), fontSize = 8.sp)
                                            domain.resourceDrops.take(3).forEach { res ->
                                                Text("• $res", color = Color.White.copy(alpha = 0.8f), fontSize = 8.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Legend Overlay Panel
                if (showLegend) {
                    Surface(
                        color = Color(0xEE12081C),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE91E63).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .width(160.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "LEGENDA MINIMAPY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(12.dp).background(Color(0xFFFFD700), CircleShape))
                                Text("Pán (Tvá poloha)", color = Color.White, fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("⚡", fontSize = 10.sp, color = Color(0xFFFFD700))
                                Text("Rychlé cestování (Navštíveno)", color = Color.White, fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("🛒", fontSize = 10.sp)
                                Text("Tajemná obchodnice", color = Color.White, fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("💀", fontSize = 10.sp)
                                Text("Hlídka / Boss oblasti", color = Color.White, fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(12.dp).background(Color(0xFF1B1B1E), CircleShape), contentAlignment = Alignment.Center) {
                                    Text("?", color = Color(0xFF757575), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("Mlha (Neobjeveno)", color = Color.Gray, fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(12.dp).background(Color(0xFF37474F), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(8.dp), tint = Color.LightGray)
                                }
                                Text("Zamčená zóna", color = Color.LightGray, fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(12.dp).border(1.dp, MaterialTheme.colorScheme.primary, CircleShape))
                                Text("Vybrané území", color = Color.White, fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(12.dp).background(
                                    Brush.radialGradient(colors = listOf(Color(0xFF9C27B0).copy(alpha = 0.6f), Color.Transparent))
                                ))
                                Text("Vliv Dominia (Nadvláda)", color = Color(0xFFE1BEE7), fontSize = 9.sp)
                            }
                            if (activeQuestDomains.isNotEmpty()) {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("❗", fontSize = 10.sp)
                                    Text("Aktivní příběh / úkol", color = Color(0xFFFFC107), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                activeQuestDomains.values.forEach { q ->
                                    val domainName = DomainData.DOMAINS.find {
                                        when (q.id) {
                                            "quest_1" -> it.id == "temny_hvozd"
                                            "quest_2" -> it.id == "stoky_doupata"
                                            "quest_3" -> it.id == "slechticke_panstvi"
                                            "quest_4" -> it.id == "ruiny_chramu"
                                            else -> false
                                        }
                                    }?.name ?: "???"
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("📜", fontSize = 9.sp)
                                        Text(
                                            text = "${q.title} ($domainName)",
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 8.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
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
