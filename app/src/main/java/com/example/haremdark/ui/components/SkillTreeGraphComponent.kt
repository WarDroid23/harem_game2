package com.example.haremdark.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.CharacterSkillCatalog
import com.example.haremdark.data.CharacterSkillNode
import com.example.haremdark.data.SkillNodeType
import com.example.haremdark.models.Character
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SkillTreeGraphComponent(
    character: Character,
    onUnlock: (CharacterSkillNode) -> Unit,
    modifier: Modifier = Modifier
) {
    val skills = CharacterSkillCatalog.ALL_SKILL_NODES
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117)) // Dark background for the graph
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Drawing lines between nodes
        Canvas(modifier = Modifier.fillMaxWidth().height(1000.dp)) {
            skills.forEach { node ->
                if (node.reqNodeId != null) {
                    val parent = skills.find { it.id == node.reqNodeId }
                    if (parent != null) {
                        val start = Offset(parent.x * size.width / 100f, parent.y * 1000.dp.toPx() / 100f)
                        val end = Offset(node.x * size.width / 100f, node.y * 1000.dp.toPx() / 100f)
                        
                        val isUnlocked = character.unlockedCombatSkills.contains(node.id) || character.unlockedPassives.contains(node.id)
                        val isParentUnlocked = character.unlockedCombatSkills.contains(parent.id) || character.unlockedPassives.contains(parent.id)
                        
                        drawLine(
                            color = if (isUnlocked) Color(0xFFFFD700) else if (isParentUnlocked) Color.Gray else Color.DarkGray.copy(alpha = 0.3f),
                            start = start,
                            end = end,
                            strokeWidth = if (isUnlocked) 4.dp.toPx() else 2.dp.toPx(),
                            pathEffect = if (isUnlocked) null else PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }
                }
            }
        }

        // Placing nodes
        Box(modifier = Modifier.fillMaxWidth().height(1000.dp)) {
            skills.forEach { node ->
                val isUnlocked = character.unlockedCombatSkills.contains(node.id) || character.unlockedPassives.contains(node.id)
                val canUnlock = CharacterSkillCatalog.canUnlockNode(character, node)
                
                SkillGraphNode(
                    node = node,
                    isUnlocked = isUnlocked,
                    canUnlock = canUnlock,
                    characterLevel = character.level,
                    characterAffinity = character.affinityLevel,
                    onClick = { if (canUnlock && !isUnlocked) onUnlock(node) },
                    modifier = Modifier.offset(
                        x = (node.x * 2.8f).dp, // Adjust multipliers to match the normalized coordinates
                        y = (node.y * 8.5f).dp
                    )
                )
            }
        }
    }
}

@Composable
fun SkillGraphNode(
    node: CharacterSkillNode,
    isUnlocked: Boolean,
    canUnlock: Boolean,
    characterLevel: Int,
    characterAffinity: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NodePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Column(
        modifier = modifier
            .width(80.dp)
            .clickable(enabled = canUnlock && !isUnlocked) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .then(
                    if (isUnlocked) {
                        Modifier.background(Brush.radialGradient(listOf(Color(node.nodeType.badgeColorHex), Color(node.nodeType.badgeColorHex).copy(alpha = 0.5f))))
                    } else {
                        Modifier.background(if (canUnlock) Color.DarkGray else Color.Black.copy(alpha = 0.6f))
                    }
                )
                .border(
                    width = 2.dp,
                    color = if (isUnlocked) Color.White else if (canUnlock) Color(node.nodeType.badgeColorHex).copy(alpha = pulseAlpha) else Color.Gray.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Text(node.icon, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = node.name,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isUnlocked) Color.White else if (canUnlock) Color.LightGray else Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )

        if (!isUnlocked) {
            val reqs = mutableListOf<String>()
            if (characterLevel < node.reqLevel) reqs.add("L${node.reqLevel}")
            if (characterAffinity < node.reqAffinityLevel) reqs.add("A${node.reqAffinityLevel}")
            
            if (reqs.isNotEmpty()) {
                Text(
                    text = reqs.joinToString(", "),
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            } else if (canUnlock) {
                Text(
                    text = "${node.spCost} SP",
                    fontSize = 8.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
