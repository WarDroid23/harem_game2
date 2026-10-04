package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.haremdark.domain.GameEngine
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.domain.SoundEffectManager
import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave
import com.example.haremdark.ui.sound.LocalSoundManager
import kotlin.math.cos
import kotlin.math.sin

/**
 * Radial menu for quick character swapping during combat encounters.
 */
@Composable
fun CombatRadialCharacterSwapDialog(
    gameState: GameSave,
    engine: GameEngine,
    currentDeployedId: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = LocalSoundManager.current
    val characters = gameState.characters

    var hoveredCharacter by remember { mutableStateOf<Character?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🔄 Kruhová výměna bojovnice",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = "Vyber postavu pro okamžité nasazení do první linie",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            soundManager.playMenuClick()
                            onDismiss()
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Radial Orbital Menu Canvas & Nodes Container
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val radius = 105.dp

                    // Background Orbital Canvas Rings
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f

                        // Outer orbit ring
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = 0.25f),
                            radius = radius.toPx(),
                            center = Offset(centerX, centerY),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                        )
                        // Inner core ring
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = 0.4f),
                            radius = 45.dp.toPx(),
                            center = Offset(centerX, centerY),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    // Center Hub (Active Deployed Character)
                    val activeChar = characters.firstOrNull { it.id == currentDeployedId }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(2.dp, Color(0xFFFFD700)),
                        modifier = Modifier
                            .size(76.dp)
                            .zIndex(10f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = activeChar?.statusIcon ?: "👑",
                                fontSize = 32.sp
                            )
                        }
                    }

                    // Orbital Character Nodes
                    characters.forEachIndexed { index, char ->
                        val angle = index * (2.5 * Math.PI / characters.size) - Math.PI / 2
                        val xOffset = (radius.value * cos(angle)).dp
                        val yOffset = (radius.value * sin(angle)).dp
                        val isSelected = char.id == currentDeployedId

                        Box(
                            modifier = Modifier
                                .offset(x = xOffset, y = yOffset)
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = if (isSelected) 3.dp else 1.5.dp,
                                    color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    hoveredCharacter = char
                                    soundManager.playCharacterSelection(char.name, char.voicePackId)
                                    HapticManager.vibrateClick()
                                    engine.switchCombatCharacter(char.id)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char.statusIcon,
                                fontSize = 24.sp
                            )
                        }
                    }
                }

                // Selected Character Preview Info Card
                val previewChar = hoveredCharacter ?: characters.firstOrNull { it.id == currentDeployedId }
                if (previewChar != null) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(previewChar.statusIcon, fontSize = 28.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${previewChar.name} (${previewChar.role})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "HP: ${previewChar.hp}/${previewChar.maxHp} • Náklonnost: ${previewChar.affinityPoints} • Úroveň: ${previewChar.level}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFD700)
                                )
                            }

                            Button(
                                onClick = {
                                    soundManager.playCharacterSelection(previewChar.name, previewChar.voicePackId)
                                    HapticManager.vibrateClick()
                                    engine.switchCombatCharacter(previewChar.id)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Nasadit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
