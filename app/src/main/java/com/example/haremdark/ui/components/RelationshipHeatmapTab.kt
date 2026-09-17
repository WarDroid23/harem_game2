package com.example.haremdark.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.models.Character
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun RelationshipHeatmapTab(characters: List<Character>) {
    if (characters.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Harém je prázdný.", color = Color.Gray)
        }
        return
    }

    val entries = characters.mapIndexed { index, char ->
        FloatEntry(x = index.toFloat(), y = char.loajalita.toFloat())
    }
    val chartEntryModel = entryModelOf(entries)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📈 Mapa Oddanosti (Heatmap)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tento graf zobrazuje celkovou oddanost všech otrokyň. Vyšší sloupce znamenají vyšší loajalitu a bezpečnější investice.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Chart(
                        chart = columnChart(),
                        model = chartEntryModel,
                        startAxis = rememberStartAxis(title = "Oddanost (0-100)"),
                        bottomAxis = rememberBottomAxis(
                            valueFormatter = { value, _ ->
                                val index = value.toInt()
                                if (index in characters.indices) characters[index].name.take(4) else ""
                            },
                            title = "Otrokyně"
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                }
            }
        }
        
        item {
            val highLoyalty = characters.filter { it.loajalita >= 75 }.sortedByDescending { it.loajalita }
            val riskLoyalty = characters.filter { it.loajalita < 25 }.sortedBy { it.loajalita }
            
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (riskLoyalty.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("⚠️ Rizikové otrokyně (Nízká oddanost)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            riskLoyalty.forEach { c ->
                                Text("• ${c.name} (${c.loajalita}%) - Zvaž tresty nebo dary ke zlomení odporu.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
                
                if (highLoyalty.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🌟 Potenciál k Milníkům (Vysoká oddanost)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            highLoyalty.forEach { c ->
                                Text("• ${c.name} (${c.loajalita}%) - Blízko k dalším odměnám a klíčovým vzpomínkám.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        }
                    }
                }
            }
        }
    }
}
