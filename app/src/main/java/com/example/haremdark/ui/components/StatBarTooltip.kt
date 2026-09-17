package com.example.haremdark.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.haremdark.data.AffinityData
import com.example.haremdark.models.Character

/**
 * Types of vital stats bars in character interaction.
 */
enum class StatBarTooltipType(
    val title: String,
    val icon: String,
    val subtitle: String,
    val accentColor: Color,
    val shortSummary: String,
    val whatItRepresents: String,
    val howToManage: String,
    val combatAndHaremImpact: String
) {
    HEALTH(
        title = "Zdraví (HP - Životy)",
        icon = "❤️",
        subtitle = "Tělesná vitalita & výdrž v boji",
        accentColor = Color(0xFFFF5252),
        shortSummary = "Ukazatel fyzické životaschopnosti dívky",
        whatItRepresents = "Zdraví (HP) představuje aktuální tělesnou odolnost a kondici konkubíny. V průzkumech teritorií, aréně a tahových soubojích nepřátelé dívce udělují zranění, která snižují tuto hodnotu. Klesne-li HP na nulu, dívka je v boji vyřazena (bezvědomí) a nemůže dál asistovat.",
        howToManage = "Jak doplnit HP: Léčivými lektvary (např. Elixír života v záložce Předměty), přespáním na nový den v dominiu, nebo regenerací v zámeckých lázních.",
        combatAndHaremImpact = "Herní dopad: Dívka s plným zdravím podává maximální výkon a netrpí postihy k náladě ani k loajalitě."
    ),
    MANA(
        title = "Mana (MP - Magická esence)",
        icon = "🔮",
        subtitle = "Zásoba stínové magie & koncentrace",
        accentColor = Color(0xFF00E5FF),
        shortSummary = "Zásoba mystické energie pro kouzla",
        whatItRepresents = "Mana (MP) měří magickou sílu, duševní koncentraci a zásobu esence potřebnou k sesílání mocných bojových dovedností, kouzel a rituálů. Každá speciální technika (např. léčení, stínový blesk, štít) spotřebovává body many.",
        howToManage = "Jak doplnit MP: Magickými lektvary (Lektvar many), meditací v soukromých komnatách, nebo přirozenou regenerací při přechodu do nového dne.",
        combatAndHaremImpact = "Herní dopad: Bez dostatečné many nemůže dívka sesílat své zničující dovednosti a je omezena pouze na základní útoky."
    ),
    AFFINITY(
        title = "Náklonnost (Affinity - Pouto)",
        icon = "💖",
        subtitle = "Úroveň lásky, důvěry a oddanosti k Pánovi",
        accentColor = Color(0xFFFF4081),
        shortSummary = "Hloubka emočního propojení s vládcem",
        whatItRepresents = "Náklonnost (Affinity) je klíčový ukazatel vztahu. Vyjadřuje, nakolik si dívka tvého Pána oblíbila, jak hluboce mu důvěřuje a jak poddajná je její duše. Náklonnost je rozdělena do 5 úrovní (od Cizinky po Božské pouto), přičemž každá úroveň poskytuje trvalou pasivní auru pro celou družinu.",
        howToManage = "Jak zvýšit Náklonnost: Darováním dárků podle jejího archetypu (květiny, šperky, knihy v záložce Dary), lichocením, volbou empatických odpovědí v interaktivních dialozích a plněním jejích tužeb.",
        combatAndHaremImpact = "Herní dopad: Vyšší úroveň odemyká intimní scény, nové dialogové linie, bojová posílení pro Pána, možnost požádat dívku o ruku a zplodit dědice dominia."
    )
}

/**
 * Detailed informational dialog explaining a stat bar to new and veteran players.
 * Triggered by long-pressing on Health, Mana, or Affinity bars on the character screen.
 */
@Composable
fun StatBarExplanationDialog(
    tooltipType: StatBarTooltipType,
    character: Character,
    onDismiss: () -> Unit
) {
    val tier = remember(character.affinityPoints) {
        AffinityData.getTierForPoints(character.affinityPoints)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF191122)),
            border = BorderStroke(1.5.dp, tooltipType.accentColor.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with glowing icon and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(tooltipType.accentColor.copy(alpha = 0.15f))
                                .border(1.dp, tooltipType.accentColor.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(tooltipType.icon, fontSize = 24.sp)
                        }
                        Column {
                            Text(
                                text = tooltipType.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = tooltipType.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = tooltipType.accentColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("✕", color = Color.White.copy(alpha = 0.6f), fontSize = 16.sp)
                    }
                }

                // Current Character Stat Preview Row
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, tooltipType.accentColor.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Aktuální stav (${character.name}):",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        val statValueText = when (tooltipType) {
                            StatBarTooltipType.HEALTH -> "${character.hp} / ${character.maxHp} HP"
                            StatBarTooltipType.MANA -> "${character.mana} / ${character.maxMana} MP"
                            StatBarTooltipType.AFFINITY -> "Úr. ${tier.level} • ${character.affinityPoints} bodů"
                        }
                        Text(
                            text = statValueText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = tooltipType.accentColor
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // 1. What it represents
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "📖 Co tento ukazatel znamená:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = tooltipType.whatItRepresents,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }

                // 2. How to manage / restore
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = tooltipType.accentColor.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, tooltipType.accentColor.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "💡 Jak doplňovat a zvyšovat:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = tooltipType.accentColor
                        )
                        Text(
                            text = tooltipType.howToManage,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )
                    }
                }

                // 3. Combat & Harem impact
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("⚔️", fontSize = 16.sp)
                        Column {
                            Text(
                                text = "Herní přínos v dominiu:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFFFD54F)
                            )
                            Text(
                                text = tooltipType.combatAndHaremImpact,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Dismiss Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tooltipType.accentColor
                    )
                ) {
                    Text(
                        text = "Rozumím (Zavřít)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
