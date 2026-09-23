package com.example.haremdark.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.haremdark.models.CombatLogEntry
import com.example.haremdark.models.Element
import com.example.haremdark.models.ElementalSynergyBuff
import com.example.haremdark.models.ElementalSynergyManager

/**
 * Modal dialog presenting active Elemental Synergies and the catalog of complementary
 * elemental pairings that trigger passive damage resistance buffs.
 */
@Composable
fun ElementalSynergyModal(
    activeSynergies: List<ElementalSynergyBuff>,
    combatLogs: List<CombatLogEntry>,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Aktivní Synergie, 1: Kodex Rezonancí

    // Total damage prevented by synergies in this combat session
    val totalDamageMitigated = remember(combatLogs) {
        combatLogs.mapNotNull { it.elementalBreakdown?.synergyMitigatedDamage }.sum()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140B1E)),
            border = BorderStroke(1.5.dp, Color(0xFF9C27B0).copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF4A148C), Color(0xFF880E4F))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFBA68C8).copy(alpha = 0.25f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Elementární Synergie Týmu",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Pasivní redukce poškození komplementárními živly",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE1BEE7)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Zavřít",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Stats Bar (Active count & Total mitigated)
                Surface(
                    color = Color(0xFF1E122A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "⚡ Aktivní synergie:",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (activeSynergies.isNotEmpty()) Color(0xFF4CAF50).copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, if (activeSynergies.isNotEmpty()) Color(0xFF4CAF50) else Color.Gray)
                            ) {
                                Text(
                                    "${activeSynergies.size} aktivní",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeSynergies.isNotEmpty()) Color(0xFF69F0AE) else Color.Gray,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "🛡️ Pohlcené poškození:",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                "-$totalDamageMitigated DMG",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    }
                }

                // Tab Selector
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF190F25),
                    contentColor = Color(0xFFFF4081)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "🛡️ Aktivní (${activeSynergies.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "📜 Kodex Rezonancí (${ElementalSynergyManager.DEFINED_SYNERGIES.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                // Tab Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    if (selectedTab == 0) {
                        ActiveSynergiesTab(activeSynergies = activeSynergies, combatLogs = combatLogs)
                    } else {
                        SynergyCatalogTab(activeSynergies = activeSynergies)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveSynergiesTab(
    activeSynergies: List<ElementalSynergyBuff>,
    combatLogs: List<CombatLogEntry>
) {
    if (activeSynergies.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "🌫️ Žádná aktivní synergie",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "V družině momentálně nejsou dívky s komplementárními elementy.\n\n" +
                            "Kombinujte např. Oheň + Zemi, Vodu + Led, Blesk + Vzduch nebo Temnotu + Světlo pro odemčení mohutných pasivních štítů!",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF261238),
                    border = BorderStroke(1.dp, Color(0xFFAB47BC).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFCE93D8),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Aktivní synergie pasivně tlumí přicházející útoky nepřátel odpovídajících živlů během celého boje.",
                            fontSize = 11.sp,
                            color = Color(0xFFE1BEE7)
                        )
                    }
                }
            }

            items(activeSynergies) { synergy ->
                ActiveSynergyCard(synergy = synergy, combatLogs = combatLogs)
            }
        }
    }
}

@Composable
private fun ActiveSynergyCard(
    synergy: ElementalSynergyBuff,
    combatLogs: List<CombatLogEntry>
) {
    val damageMitigatedByThisSynergy = remember(combatLogs, synergy.id) {
        combatLogs.filter { it.elementalBreakdown?.activeSynergyName == synergy.name }
            .sumOf { it.elementalBreakdown?.synergyMitigatedDamage ?: 0 }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E102E)),
        border = BorderStroke(1.dp, Color(0xFFAB47BC).copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Text(
                        text = synergy.icon,
                        fontSize = 20.sp
                    )
                    Column {
                        Text(
                            text = synergy.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F)
                        )
                        Text(
                            text = synergy.titleBadge,
                            fontSize = 11.sp,
                            color = Color(0xFF69F0AE)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(0xFF4CAF50))
                ) {
                    Text(
                        text = "AKTIVNÍ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF69F0AE),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = synergy.description,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.85f)
            )

            // Participating members
            if (synergy.participatingMemberNames.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "🔗 Rezonující hrdinky:",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = synergy.participatingMemberNames.joinToString(", "),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF80AB)
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // Resistance breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tlumí útoky živlů:",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = synergy.resistedElements.joinToString(" • ") { it.name },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF80D8FF)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF311B92).copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, Color(0xFF7C4DFF))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "-${(synergy.damageResistancePercent * 100).toInt()}% DMG",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F)
                        )
                    }
                }
            }

            if (damageMitigatedByThisSynergy > 0) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1B5E20).copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, Color(0xFF66BB6A).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✨ V tomto boji ušetřeno -$damageMitigatedByThisSynergy HP poškození!",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA5D6A7),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SynergyCatalogTab(
    activeSynergies: List<ElementalSynergyBuff>
) {
    val activeIds = remember(activeSynergies) { activeSynergies.map { it.id }.toSet() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Komplementární živly se vzájemně doplňují a tvoří přirozené energetické bariéry. Sestavením odpovídajících dívk v partě získáte následující výhody:",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(ElementalSynergyManager.DEFINED_SYNERGIES) { catalogBuff ->
            val isActive = activeIds.contains(catalogBuff.id)

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) Color(0xFF231238) else Color(0xFF180E24)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isActive) Color(0xFFBA68C8) else Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
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
                            Text(catalogBuff.icon, fontSize = 18.sp)
                            Column {
                                Text(
                                    catalogBuff.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) Color(0xFFFFD54F) else Color.White
                                )
                                Text(
                                    catalogBuff.titleBadge,
                                    fontSize = 10.sp,
                                    color = if (isActive) Color(0xFF69F0AE) else Color.Gray
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isActive) Color(0xFF4CAF50).copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, if (isActive) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = if (isActive) "AKTIVNÍ" else "NEAKTIVNÍ",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) Color(0xFF69F0AE) else Color.Gray,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = catalogBuff.description,
                        fontSize = 10.5.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0F0718),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🛡️ ${catalogBuff.bonusPerkDescription}",
                                fontSize = 10.sp,
                                color = Color(0xFFFF80AB)
                            )
                        }
                    }
                }
            }
        }
    }
}
