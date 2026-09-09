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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.R
import com.example.haremdark.data.DomainData
import com.example.haremdark.models.DomainLocation
import com.example.haremdark.models.GameSave

@Composable
fun MinimapOverlay(
    gameState: GameSave,
    selectedDomainId: String,
    onDomainSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulsing animation for the current player location
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
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

    var showLegend by remember { mutableStateOf(false) }

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

                // 3. Grid overlay, connecting paths and scanlines drawn via Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw a subtle coordinates grid
                    val gridCols = 8
                    val gridRows = 6
                    val gridColor = Color(0x2280D8FF)
                    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)

                    for (i in 1 until gridCols) {
                        val x = w * (i.toFloat() / gridCols)
                        drawLine(
                            color = gridColor,
                            start = Offset(x, 0f),
                            end = Offset(x, h),
                            strokeWidth = 1f,
                            pathEffect = dashedEffect
                        )
                    }
                    for (j in 1 until gridRows) {
                        val y = h * (j.toFloat() / gridRows)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f,
                            pathEffect = dashedEffect
                        )
                    }

                    // Predefined connections between domains to look like trade/magic lanes
                    val connections = listOf(
                        Pair("temny_hvozd", "stoky_doupata"),
                        Pair("ruiny_chramu", "stoky_doupata"),
                        Pair("mesicni_pristav", "stoky_doupata"),
                        Pair("stoky_doupata", "slechticke_panstvi"),
                        Pair("slechticke_panstvi", "propast_behemoth")
                    )

                    connections.forEach { (srcId, destId) ->
                        val src = DomainData.DOMAINS.find { it.id == srcId }
                        val dest = DomainData.DOMAINS.find { it.id == destId }
                        if (src != null && dest != null) {
                            val startX = src.mapX * w
                            val startY = src.mapY * h
                            val endX = dest.mapX * w
                            val endY = dest.mapY * h

                            // Draw lane only if both domains are explored
                            val srcExplored = gameState.unlockedDomains.contains(srcId) || gameState.currentDomainId == srcId
                            val destExplored = gameState.unlockedDomains.contains(destId) || gameState.currentDomainId == destId

                            drawLine(
                                color = if (srcExplored && destExplored) Color(0x99FF80AB) else Color(0x11FF80AB),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 2.5f,
                                pathEffect = dashedEffect
                            )
                        }
                    }

                    // DRAW FOG OF WAR GRADIENTS
                    DomainData.DOMAINS.forEach { d ->
                        val isExplored = gameState.unlockedDomains.contains(d.id) || gameState.currentDomainId == d.id
                        if (!isExplored) {
                            val fx = d.mapX * w
                            val fy = d.mapY * h
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xF911071D), Color(0xD011071D), Color.Transparent),
                                    center = Offset(fx, fy),
                                    radius = 75f
                                ),
                                center = Offset(fx, fy),
                                radius = 75f
                            )
                        }
                    }
                }

                // 4. Floating coordinate labels along borders
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
                                    .clickable { onDomainSelect(domain.id) }
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
                        }
                    }
                }
            }
        }
    }
}
