package com.example.haremdark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.data.AffinityData
import com.example.haremdark.models.Character
import com.example.haremdark.models.getSafeAffinityTrend
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun AffinityTrendChart(
    character: Character,
    currentDay: Int = 1,
    modifier: Modifier = Modifier
) {
    val tier = remember(character.affinityPoints) {
        AffinityData.getTierForPoints(character.affinityPoints)
    }
    val nextTier = remember(character.affinityPoints) {
        AffinityData.TIERS.firstOrNull { it.level == tier.level + 1 }
    }

    val trendRecords = remember(character.affinityPoints, character.affinityHistory.size, currentDay) {
        character.getSafeAffinityTrend(currentDay)
    }

    val chartEntries = remember(trendRecords) {
        trendRecords.mapIndexed { index, record ->
            FloatEntry(x = (index + 1).toFloat(), y = record.points.toFloat())
        }
    }

    val initialPoints = trendRecords.firstOrNull()?.points ?: 0
    val growthPoints = (character.affinityPoints - initialPoints).coerceAtLeast(0)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(tier.colorHex).copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Title, Icon, Growth Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color(tier.colorHex),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Křivka růstu náklonnosti",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(tier.colorHex).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(tier.colorHex).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(tier.icon, fontSize = 11.sp)
                        Text(
                            text = "+$growthPoints bodů",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(tier.colorHex)
                        )
                    }
                }
            }

            Text(
                text = "Vývoj vztahu a oddanosti k Pánu dominia v čase.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // Vico Line Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF140D1E).copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Chart(
                    chart = lineChart(),
                    model = entryModelOf(chartEntries),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Milestone History Timeline
            Text(
                text = "Zaznamenané milníky (${trendRecords.size}):",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(trendRecords) { record ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Den ${record.day}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text("•", fontSize = 10.sp, color = Color.Gray)
                                Text("${record.points} pts", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(tier.colorHex))
                            }
                            Text(
                                text = record.source,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Next Tier Target bar
            if (nextTier != null) {
                val ptsToNext = (nextTier.minPoints - character.affinityPoints).coerceAtLeast(0)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Příští cíl: ${nextTier.icon} ${nextTier.title}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(nextTier.colorHex)
                    )
                    Text(
                        text = "Zbývá $ptsToNext bodů",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
