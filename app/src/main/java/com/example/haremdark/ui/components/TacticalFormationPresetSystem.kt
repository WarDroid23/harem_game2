package com.example.haremdark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.haremdark.models.FormationPosition

@Composable
fun TacticalFormationPresetSystem(
    currentFormation: Map<String, FormationPosition>,
    onSavePreset: (String, Map<String, FormationPosition>) -> Unit,
    onApplyPreset: (Map<String, FormationPosition>) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        "Balanced" to mapOf<String, FormationPosition>(), // Logic for presets would be mapped here
        "Tank Heavy" to mapOf(),
        "Glass Cannon" to mapOf()
    )

    Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Taktické šablony formací", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { (name, layout) ->
                Button(onClick = { onApplyPreset(layout) }) {
                    Text(name, fontSize = 10.sp)
                }
            }
        }
        
        // Simple visual grid for currently selected formation
        Text("Náhled:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color(0xFF2E1B3E), RoundedCornerShape(8.dp))) {
            // Visual representation of Front/Mid/Back lines
        }
    }
}
