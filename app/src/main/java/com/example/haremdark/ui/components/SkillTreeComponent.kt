package com.example.haremdark.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.haremdark.models.SkillNode
import com.example.haremdark.models.SkillTreeData

@Composable
fun SkillTreeComponent(
    unlockedSkills: Set<String>,
    onUnlock: (SkillNode) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(SkillTreeData.nodes) { node ->
            val isUnlocked = unlockedSkills.contains(node.id)
            val canUnlock = node.requiresId == null || unlockedSkills.contains(node.requiresId)
            
            Card(
                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isUnlocked) Color(0xFF4A148C) else Color(0xFF261238))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "${node.icon} ${node.title}", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(text = node.description, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { onUnlock(node) },
                        enabled = !isUnlocked && canUnlock
                    ) {
                        Text(if (isUnlocked) "Odemčeno" else "Odemknout (${node.cost} SP)")
                    }
                }
            }
        }
    }
}
