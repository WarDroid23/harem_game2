package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.haremdark.R
import com.example.haremdark.data.DomainData
import com.example.haremdark.models.DomainLocation
import com.example.haremdark.models.GameSave

@Composable
fun KingdomMapView(
    gameState: GameSave,
    onRegionClick: (DomainLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scrollStateVertical = rememberScrollState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "map_fx")
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "pulse"
    )

    val scanlineAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "scan"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0412))
    ) {
        // Large scrollable map area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
                .verticalScroll(scrollStateVertical)
        ) {
            Box(modifier = Modifier.size(1000.dp, 1000.dp)) {
                // 1. Map Background
                Image(
                    painter = painterResource(id = R.drawable.img_fantasy_map),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
                
                // 2. Magic/Tactical Overlay
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    // Draw connections
                    DomainData.MAP_CONNECTIONS.forEach { (srcId, destId) ->
                        val src = DomainData.DOMAINS.find { it.id == srcId }
                        val dest = DomainData.DOMAINS.find { it.id == destId }
                        if (src != null && dest != null) {
                            val isUnlocked = gameState.unlockedDomains.contains(srcId) && gameState.unlockedDomains.contains(destId)
                            if (isUnlocked) {
                                drawLine(
                                    color = Color(0xFF80D8FF).copy(alpha = 0.3f),
                                    start = Offset(src.mapX * w, src.mapY * h),
                                    end = Offset(dest.mapX * w, dest.mapY * h),
                                    strokeWidth = 3f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                )
                            }
                        }
                    }
                    
                    // Moving scanline
                    val scanY = scanlineAnim * h
                    drawLine(
                        color = Color(0x2280D8FF),
                        start = Offset(0f, scanY),
                        end = Offset(w, scanY),
                        strokeWidth = 4f
                    )
                }

                // 3. Region Markers
                DomainData.DOMAINS.forEach { domain ->
                    val isUnlocked = gameState.unlockedDomains.contains(domain.id) || gameState.player.level >= domain.minPlayerLevel
                    val isCurrent = gameState.currentDomainId == domain.id
                    
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (domain.mapX * 1000).dp - 24.dp,
                                y = (domain.mapY * 1000).dp - 24.dp
                            )
                            .size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Marker Aura
                        if (isUnlocked) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(domain.accentColor).copy(alpha = 0.2f * pulseAlpha))
                                    .border(1.dp, Color(domain.accentColor).copy(alpha = 0.5f), CircleShape)
                            )
                        }

                        // The Marker itself
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { onRegionClick(domain) },
                            color = if (isUnlocked) Color(domain.accentColor) else Color.DarkGray,
                            border = BorderStroke(2.dp, if (isCurrent) Color(0xFFFFD700) else Color.White.copy(alpha = 0.7f)),
                            tonalElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                when {
                                    !isUnlocked -> Text("🔒", fontSize = 14.sp)
                                    isCurrent -> Text("🏰", fontSize = 18.sp)
                                    domain.bossId != null -> Text("💀", fontSize = 16.sp)
                                    else -> Text("📍", fontSize = 16.sp)
                                }
                            }
                        }
                        
                        // Label
                        Box(
                            modifier = Modifier
                                .offset(y = 30.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isUnlocked) domain.name else "???",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        
        // Navigation Instructions Overlay
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            color = Color.Black.copy(alpha = 0.7f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Posouvej mapu a klepni na region pro interakci",
                color = Color.White,
                modifier = Modifier.padding(12.dp, 8.dp),
                fontSize = 12.sp
            )
        }
    }
}
