package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

data class LoreEntry(
    val keyword: String,
    val title: String,
    val description: String,
    val icon: String = "📖"
)

object LoreCatalog {
    val entries = listOf(
        LoreEntry(
            "Dominium",
            "Město Dominium",
            "Hlavní metropole říše, kde se kříží osudy všech ras a kde vládne zákon silnějšího.",
            "🏰"
        ),
        LoreEntry(
            "Temná Energie",
            "Esence Temnoty",
            "Mystická síla pohánějící magii v tomto světě. Lze ji čerpat z rituálů a emocí.",
            "🌑"
        ),
        LoreEntry(
            "Harém",
            "Pánovy komnaty",
            "Bezpečné útočiště pro tvé společnice, kde se buduje důvěra a loajalita.",
            "👑"
        ),
        LoreEntry(
            "Affinity",
            "Náklonnost",
            "Míra pouta mezi pánem a jeho společnicí. Otevírá nové dialogy a schopnosti.",
            "💖"
        ),
        LoreEntry(
            "Subka",
            "Archetyp: Submisivní",
            "Postavy s tímto archetypem preferují odevzdanost a plnění přání svého pána.",
            "⛓️"
        )
    )

    fun findRelevantLore(text: String): LoreEntry? {
        return entries.firstOrNull { text.contains(it.keyword, ignoreCase = true) }
    }
}

@Composable
fun LoreTooltip(
    lore: LoreEntry,
    onDismiss: () -> Unit
) {
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.02f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(Color(0xFFBB86FC), Color(0xFF03DAC6))
                        ),
                        RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1A1A2E),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(lore.icon, fontSize = 24.sp)
                        Text(
                            text = lore.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFBB86FC)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.2f))
                    )

                    Text(
                        text = lore.description,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF03DAC6).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF03DAC6).copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "💡 Klepni kamkoliv pro zavření",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF03DAC6)
                        )
                    }
                }
            }
        }
    }
}
