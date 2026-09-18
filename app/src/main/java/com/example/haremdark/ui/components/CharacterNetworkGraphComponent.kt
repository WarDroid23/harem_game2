package com.example.haremdark.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.haremdark.data.AffinityData
import com.example.haremdark.data.CharacterLoreCatalog
import com.example.haremdark.data.CharacterLoreConnection
import com.example.haremdark.data.RelationshipConnectionType
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.models.Character
import com.example.haremdark.models.Player
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class GraphNodePos(
    val character: Character?,
    val isLord: Boolean = false,
    val x: Float,
    val y: Float,
    val radius: Float
)

data class ActiveLoreLink(
    val connection: CharacterLoreConnection,
    val char1: Character,
    val char2: Character,
    val isUnlocked: Boolean
)

/**
 * Visual Network Graph rendered in Compose Canvas.
 * Displays all unlocked characters, their relative affinity orbits to Pán Dominia,
 * and highlighted lore connections & synergy links.
 */
@Composable
fun CharacterNetworkGraphComponent(
    characters: List<Character>,
    player: Player,
    engine: GameEngine? = null,
    onSelectCharacter: ((Character) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val textMeasurer = rememberTextMeasurer()

    var selectedFilter by remember { mutableStateOf<RelationshipConnectionType?>(null) }
    var selectedCharacterNode by remember { mutableStateOf<Character?>(null) }
    var selectedLink by remember { mutableStateOf<ActiveLoreLink?>(null) }
    var isOrbitalView by remember { mutableStateOf(true) }

    // Orbital Animation
    val infiniteTransition = rememberInfiniteTransition(label = "NetworkOrbit")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbitPhase"
    )
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseProgress"
    )

    // Compute Lore Connections
    val allLoreLinks = remember(characters) {
        CharacterLoreCatalog.getActiveConnectionsForParty(characters).map { (conn, pair) ->
            ActiveLoreLink(
                connection = conn,
                char1 = pair.first,
                char2 = pair.second,
                isUnlocked = conn.isUnlocked(pair.first, pair.second)
            )
        }
    }

    val activeBuffsCount = allLoreLinks.count { it.isUnlocked }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF140D1F))
    ) {
        // Header Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF221633)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0xFFFF4081), Color(0xFF7C4DFF))))
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
                        Icon(Icons.Default.Hub, contentDescription = null, tint = Color(0xFFFF4081), modifier = Modifier.size(22.dp))
                        Text(
                            text = "🕸️ Konstelace & Síť Vztahů Harému",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Surface(
                        color = Color(0xFF7C4DFF).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF7C4DFF))
                    ) {
                        Text(
                            text = "⚡ $activeBuffsCount Aktivních Synerií",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE040FB),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = "Vizuální graf zobrazuje vzdálenost dívek od Pána dle Affinity (orby) a skryté lore vazby.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )

                // Filter Buttons Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == null,
                            onClick = { selectedFilter = null },
                            label = { Text("Vše (${allLoreLinks.size})", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFF4081),
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    items(RelationshipConnectionType.values()) { connType ->
                        val count = allLoreLinks.count { it.connection.type == connType }
                        FilterChip(
                            selected = selectedFilter == connType,
                            onClick = { selectedFilter = if (selectedFilter == connType) null else connType },
                            label = { Text("${connType.icon} ${connType.title} ($count)", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(connType.defaultColorHex),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Main Visual Canvas Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF28183D), Color(0xFF0F0A18)),
                        center = Offset.Unspecified
                    )
                )
        ) {
            val nodesState = remember { mutableStateListOf<GraphNodePos>() }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(characters, allLoreLinks, selectedFilter) {
                        detectTapGestures { tapOffset ->
                            // Check node hit
                            val hitNode = nodesState.find { node ->
                                val dx = tapOffset.x - node.x
                                val dy = tapOffset.y - node.y
                                sqrt(dx * dx + dy * dy) <= node.radius + 20f
                            }

                            if (hitNode != null) {
                                if (hitNode.isLord) {
                                    Toast
                                        .makeText(context, "👑 Pán Dominia - Centrum konstelace harému", Toast.LENGTH_SHORT)
                                        .show()
                                } else if (hitNode.character != null) {
                                    selectedCharacterNode = hitNode.character
                                    selectedLink = null
                                }
                                return@detectTapGestures
                            }

                            // Check edge hit
                            var hitLink: ActiveLoreLink? = null
                            var minDistance = 35f

                            allLoreLinks.forEach { link ->
                                if (selectedFilter == null || link.connection.type == selectedFilter) {
                                    val n1 = nodesState.find { it.character?.id == link.char1.id }
                                    val n2 = nodesState.find { it.character?.id == link.char2.id }
                                    if (n1 != null && n2 != null) {
                                        val dist = distToSegment(tapOffset, Offset(n1.x, n1.y), Offset(n2.x, n2.y))
                                        if (dist < minDistance) {
                                            minDistance = dist
                                            hitLink = link
                                        }
                                    }
                                }
                            }

                            if (hitLink != null) {
                                selectedLink = hitLink
                                selectedCharacterNode = null
                            } else {
                                selectedCharacterNode = null
                                selectedLink = null
                            }
                        }
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = (minOf(size.width, size.height) / 2f) * 0.85f
                val innerRadius = 70f

                nodesState.clear()

                // Draw Ambient Orbit Concentric Rings
                val orbits = listOf(1, 2, 3, 4, 5, 6)
                orbits.forEach { level ->
                    val r = innerRadius + (6 - level) * (maxRadius - innerRadius) / 5f
                    drawCircle(
                        color = Color(0xFF7C4DFF).copy(alpha = 0.15f),
                        radius = r,
                        center = center,
                        style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
                    )
                }

                // Calculate Character Node Positions
                val totalChars = characters.size
                val charNodes = characters.mapIndexed { index, char ->
                    val normLevel = char.affinityLevel.coerceIn(1, 6)
                    val r = innerRadius + (6 - normLevel) * (maxRadius - innerRadius) / 5f
                    val angleDeg = (index.toFloat() / totalChars.toFloat()) * 360f + orbitAngle
                    val rad = Math.toRadians(angleDeg.toDouble())
                    val nx = center.x + (r * cos(rad)).toFloat()
                    val ny = center.y + (r * sin(rad)).toFloat()

                    GraphNodePos(
                        character = char,
                        isLord = false,
                        x = nx,
                        y = ny,
                        radius = 28.dp.toPx()
                    )
                }

                val lordNode = GraphNodePos(
                    character = null,
                    isLord = true,
                    x = center.x,
                    y = center.y,
                    radius = 34.dp.toPx()
                )

                nodesState.add(lordNode)
                nodesState.addAll(charNodes)

                // 1. Draw Lord Bond Lines (Pán -> Character)
                charNodes.forEach { node ->
                    val char = node.character ?: return@forEach
                    val tierInfo = AffinityData.getTierForPoints(char.affinityPoints)
                    val lineColor = Color(tierInfo.colorHex).copy(alpha = 0.6f)

                    drawLine(
                        color = lineColor,
                        start = center,
                        end = Offset(node.x, node.y),
                        strokeWidth = (char.affinityLevel * 0.8f).dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Pulse particle along line
                    val px = center.x + (node.x - center.x) * pulseProgress
                    val py = center.y + (node.y - center.y) * pulseProgress
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = 3.dp.toPx(),
                        center = Offset(px, py)
                    )
                }

                // 2. Draw Inter-Character Lore Links
                allLoreLinks.forEach { link ->
                    if (selectedFilter == null || link.connection.type == selectedFilter) {
                        val n1 = charNodes.find { it.character?.id == link.char1.id }
                        val n2 = charNodes.find { it.character?.id == link.char2.id }
                        if (n1 != null && n2 != null) {
                            val colorHex = link.connection.customColorHex ?: link.connection.type.defaultColorHex
                            val linkColor = if (link.isUnlocked) Color(colorHex) else Color.Gray.copy(alpha = 0.35f)
                            val isSelected = selectedLink?.connection?.id == link.connection.id

                            if (link.isUnlocked) {
                                drawLine(
                                    color = linkColor,
                                    start = Offset(n1.x, n1.y),
                                    end = Offset(n2.x, n2.y),
                                    strokeWidth = if (isSelected) 4.dp.toPx() else 2.5.dp.toPx(),
                                    pathEffect = if (isSelected) null else PathEffect.dashPathEffect(floatArrayOf(12f, 6f), pulseProgress * 20f)
                                )

                                // Edge Label / Icon in middle
                                val mx = (n1.x + n2.x) / 2f
                                val my = (n1.y + n2.y) / 2f
                                drawCircle(
                                    color = Color(0xFF1F152B),
                                    radius = 10.dp.toPx(),
                                    center = Offset(mx, my)
                                )
                                drawCircle(
                                    color = linkColor,
                                    radius = 10.dp.toPx(),
                                    center = Offset(mx, my),
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            } else {
                                drawLine(
                                    color = linkColor,
                                    start = Offset(n1.x, n1.y),
                                    end = Offset(n2.x, n2.y),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                                )
                            }
                        }
                    }
                }

                // 3. Draw Central Lord Node
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFFF9100).copy(alpha = 0.3f), Color.Transparent),
                        center = center,
                        radius = lordNode.radius * 1.8f
                    ),
                    radius = lordNode.radius * 1.8f,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF3E2723),
                    radius = lordNode.radius,
                    center = center
                )
                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = lordNode.radius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
                // Lord Text/Icon
                val lordTextResult = textMeasurer.measure("👑 PÁN", TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700)))
                drawText(
                    textLayoutResult = lordTextResult,
                    topLeft = Offset(center.x - lordTextResult.size.width / 2f, center.y - lordTextResult.size.height / 2f)
                )

                // 4. Draw Character Nodes
                charNodes.forEach { node ->
                    val char = node.character ?: return@forEach
                    val tierInfo = AffinityData.getTierForPoints(char.affinityPoints)
                    val nodeColor = Color(tierInfo.colorHex)
                    val isSelected = selectedCharacterNode?.id == char.id

                    val nOffset = Offset(node.x, node.y)

                    if (isSelected) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.4f),
                            radius = node.radius + 8.dp.toPx(),
                            center = nOffset,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    // Node outer ring
                    drawCircle(
                        color = nodeColor.copy(alpha = 0.3f),
                        radius = node.radius + 4.dp.toPx(),
                        center = nOffset
                    )
                    drawCircle(
                        color = Color(0xFF231735),
                        radius = node.radius,
                        center = nOffset
                    )
                    drawCircle(
                        color = nodeColor,
                        radius = node.radius,
                        center = nOffset,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Node Archetype & Level Label
                    val nameText = textMeasurer.measure(
                        char.name,
                        TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    drawText(
                        textLayoutResult = nameText,
                        topLeft = Offset(nOffset.x - nameText.size.width / 2f, nOffset.y - nameText.size.height / 2f - 4.dp.toPx())
                    )

                    val levelText = textMeasurer.measure(
                        "💖 Lvl ${char.affinityLevel}",
                        TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = nodeColor)
                    )
                    drawText(
                        textLayoutResult = levelText,
                        topLeft = Offset(nOffset.x - levelText.size.width / 2f, nOffset.y + 4.dp.toPx())
                    )
                }
            }
        }

        // Bottom Details Card (Node or Edge Details)
        AnimatedVisibility(
            visible = selectedCharacterNode != null || selectedLink != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                if (selectedCharacterNode != null) {
                    val char = selectedCharacterNode!!
                    val tier = AffinityData.getTierForPoints(char.affinityPoints)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF281C3D)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(tier.colorHex))
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
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color(tier.colorHex), CircleShape)
                                    .background(Color(0xFF382454)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
                                    contentDescription = char.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(char.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(tier.icon, fontSize = 14.sp)
                                    Surface(color = Color(tier.colorHex).copy(alpha = 0.25f), shape = RoundedCornerShape(4.dp)) {
                                        Text(tier.title, fontSize = 9.sp, color = Color(tier.colorHex), modifier = Modifier.padding(4.dp))
                                    }
                                }

                                Text(
                                    text = "Affinity: ${char.affinityPoints} pt (Úroveň ${char.affinityLevel}) • Loajalita: ${char.loajalita}%",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )

                                Text(
                                    text = tier.perkDescription,
                                    fontSize = 10.sp,
                                    color = Color(0xFFFF80AB)
                                )
                            }

                            Button(
                                onClick = {
                                    onSelectCharacter?.invoke(char)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Detail ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                } else if (selectedLink != null) {
                    val link = selectedLink!!
                    val conn = link.connection
                    val colorHex = conn.customColorHex ?: conn.type.defaultColorHex

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF241635)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(colorHex))
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
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(conn.type.icon, fontSize = 16.sp)
                                    Text(conn.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Surface(
                                    color = if (link.isUnlocked) Color(0xFF4CAF50).copy(alpha = 0.25f) else Color.Red.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (link.isUnlocked) "✅ ODEMČENO" else "🔒 UZAMČENO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (link.isUnlocked) Color(0xFF81C784) else Color(0xFFFF8A80),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Propojení: ${link.char1.name} & ${link.char2.name}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE040FB)
                            )

                            Text(
                                text = conn.loreDescription,
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            Surface(
                                color = Color(colorHex).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(colorHex).copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Stars, contentDescription = null, tint = Color(colorHex), modifier = Modifier.size(16.dp))
                                    Text(
                                        text = conn.synergyBonusText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
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

private fun distToSegment(p: Offset, v: Offset, w: Offset): Float {
    val l2 = (v.x - w.x) * (v.x - w.x) + (v.y - w.y) * (v.y - w.y)
    if (l2 == 0f) return sqrt((p.x - v.x) * (p.x - v.x) + (p.y - v.y) * (p.y - v.y))
    var t = ((p.x - v.x) * (w.x - v.x) + (p.y - v.y) * (w.y - v.y)) / l2
    t = t.coerceIn(0f, 1f)
    val proj = Offset(v.x + t * (w.x - v.x), v.y + t * (w.y - v.y))
    val dx = p.x - proj.x
    val dy = p.y - proj.y
    return sqrt(dx * dx + dy * dy)
}
