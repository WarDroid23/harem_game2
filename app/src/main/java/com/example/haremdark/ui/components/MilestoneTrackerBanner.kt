package com.example.haremdark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.models.Character

@Composable
fun MilestoneTrackerBanner(characters: List<Character>) {
    if (characters.isEmpty()) return

    // Find the character closest to the next milestone (25, 50, 75, 100)
    val thresholds = listOf(25, 50, 75, 100)
    var closestChar: Character? = null
    var nextMilestone = 0
    var closestDistance = 100

    for (c in characters) {
        val currentLoyalty = c.loajalita
        if (currentLoyalty >= 100) continue
        
        val target = thresholds.firstOrNull { it > currentLoyalty } ?: 100
        val distance = target - currentLoyalty
        
        if (distance < closestDistance) {
            closestDistance = distance
            nextMilestone = target
            closestChar = c
        }
    }

    if (closestChar == null) return

    val progress = closestChar.loajalita.toFloat() / nextMilestone.toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f)),
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🌟 Blížící se milník:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    Text(closestChar.name, fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
                Text("${closestChar.loajalita} / $nextMilestone", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f)
            )
            Text(
                text = "Dosáhni oddanosti $nextMilestone k odemčení exkluzivní vzpomínky a odměn!",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
            )
        }
    }
}
