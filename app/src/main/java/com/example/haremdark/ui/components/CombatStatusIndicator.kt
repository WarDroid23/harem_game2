package com.example.haremdark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.haremdark.models.CombatSession
import com.example.haremdark.models.CombatStatusEffect
import com.example.haremdark.models.Element
import com.example.haremdark.models.Player
import com.example.haremdark.models.Character

/**
 * Status Indicator Component displaying active buffs and debuffs
 * with interactive countdown timers for their expiration.
 */

@Composable
fun StatusEffectCountdownBadge(
    effect: CombatStatusEffect,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = true
) {
    val isBuff = effect.isBuff
    val isUrgent = effect.isExpiringSoon

    // Pulse animation for expiring effects
    val infiniteTransition = rememberInfiniteTransition(label = "status_pulse")
    val urgentAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "urgent_alpha"
    )

    val baseThemeColor = when {
        isBuff && effect.type == "SHIELD" -> Color(0xFF29B6F6)
        isBuff && effect.type == "REGEN" -> Color(0xFF66BB6A)
        isBuff && effect.type.contains("BLESSING") -> Color(0xFFFF80AB)
        isBuff && effect.type.contains("CRIT") -> Color(0xFFFFD700)
        isBuff -> Color(0xFF00E676)
        effect.type == "BLEED" -> Color(0xFFD50000)
        effect.type == "POISON" -> Color(0xFF76FF03)
        effect.type == "STUN" -> Color(0xFFFF9100)
        effect.type == "FREEZE" -> Color(0xFF80D8FF)
        effect.type == "BURN" -> Color(0xFFFF3D00)
        effect.type == "SHOCK" -> Color(0xFFFFEA00)
        else -> Color(0xFFAB47BC)
    }

    val containerBg = if (isBuff) {
        Color(0xEE0E241B)
    } else {
        Color(0xEE2A0E17)
    }

    val borderColor = if (isUrgent) {
        baseThemeColor.copy(alpha = urgentAlpha)
    } else {
        baseThemeColor.copy(alpha = 0.7f)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerBg,
        border = BorderStroke(1.2.dp, borderColor),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 4.dp else 6.dp,
                vertical = if (compact) 2.dp else 4.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Icon with miniature circular countdown dial
            Box(
                modifier = Modifier.size(if (compact) 18.dp else 22.dp),
                contentAlignment = Alignment.Center
            ) {
                // Circular countdown progress ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 2.dp.toPx()
                    // Track background
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        style = Stroke(width = strokeWidth)
                    )
                    // Progress sweep
                    drawArc(
                        color = baseThemeColor,
                        startAngle = -90f,
                        sweepAngle = 360f * effect.countdownProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = effect.icon,
                    fontSize = if (compact) 10.sp else 12.sp
                )
            }

            // Compact / Extended Text
            if (!compact) {
                Text(
                    text = effect.name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Countdown Timer Pill
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isUrgent) Color(0xFFD50000).copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = if (isBuff) "▲" else "▼",
                        fontSize = 7.sp,
                        color = if (isBuff) Color(0xFF69F0AE) else Color(0xFFFF5252),
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${effect.durationTurns}k",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isUrgent) Color.Yellow else Color.White
                    )
                }
            }
        }
    }
}

/**
 * Renders a row of active status badges on characters in combat.
 */
@Composable
fun CharacterStatusEffectsRow(
    statusEffects: List<CombatStatusEffect>,
    onSelectEffect: (CombatStatusEffect) -> Unit,
    modifier: Modifier = Modifier,
    maxVisible: Int = 3,
    compact: Boolean = true
) {
    if (statusEffects.isEmpty()) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val visibleEffects = statusEffects.take(maxVisible)
        val overflowCount = statusEffects.size - maxVisible

        visibleEffects.forEach { effect ->
            StatusEffectCountdownBadge(
                effect = effect,
                onClick = { onSelectEffect(effect) },
                compact = compact
            )
        }

        if (overflowCount > 0) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.Black.copy(alpha = 0.65f),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.3f)),
                modifier = Modifier.clickable { onSelectEffect(statusEffects[maxVisible]) }
            ) {
                Text(
                    text = "+$overflowCount",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Detailed modal dialog displaying complete information and countdown timer for a status effect.
 */
@Composable
fun CombatStatusEffectDetailDialog(
    effect: CombatStatusEffect,
    onDismiss: () -> Unit
) {
    val isBuff = effect.isBuff
    val baseColor = when {
        isBuff && effect.type == "SHIELD" -> Color(0xFF29B6F6)
        isBuff && effect.type == "REGEN" -> Color(0xFF66BB6A)
        isBuff && effect.type.contains("BLESSING") -> Color(0xFFFF80AB)
        isBuff -> Color(0xFF00E676)
        effect.type == "BLEED" -> Color(0xFFD50000)
        effect.type == "POISON" -> Color(0xFF76FF03)
        effect.type == "STUN" -> Color(0xFFFF9100)
        effect.type == "FREEZE" -> Color(0xFF80D8FF)
        effect.type == "BURN" -> Color(0xFFFF3D00)
        else -> Color(0xFFAB47BC)
    }

    val elementTag = when (effect.element) {
        Element.FIRE -> "🔥 Ohnivý žár"
        Element.ICE -> "❄️ Ledový chlad"
        Element.WATER -> "💧 Vodní živel"
        Element.EARTH -> "🪨 Zemní živel"
        Element.AIR -> "🌪️ Vzdušný živel"
        Element.LIGHTNING -> "⚡ Blesková bouře"
        Element.DARK -> "🔮 Temná magie"
        Element.HOLY -> "✨ Svaté světlo"
        Element.PHYSICAL -> "⚔️ Fyzický účinek"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF160D1E)
            ),
            border = BorderStroke(1.5.dp, baseColor.copy(alpha = 0.8f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with big animated icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = baseColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, baseColor),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(effect.icon, fontSize = 24.sp)
                            }
                        }

                        Column {
                            Text(
                                text = effect.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isBuff) Color(0xFF2E7D32) else Color(0xFFC62828)
                                ) {
                                    Text(
                                        text = if (isBuff) "▲ POZITIVNÍ BUFF" else "▼ ŠKODLIVÝ DEBUFF",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = elementTag,
                                    fontSize = 10.sp,
                                    color = Color(0xFFCE93D8)
                                )
                            }
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.Gray)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

                // COUNTDOWN TIMER BOX
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x66000000),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("⏳", fontSize = 14.sp)
                                Text(
                                    text = "Časomíra vypršení efektu:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                            }

                            Text(
                                text = "Zbývá ${effect.durationTurns} ${if (effect.durationTurns == 1) "kolo" else if (effect.durationTurns in 2..4) "kola" else "kol"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (effect.isExpiringSoon) Color(0xFFFF5252) else Color(0xFF69F0AE)
                            )
                        }

                        // Countdown linear meter
                        LinearProgressIndicator(
                            progress = { effect.countdownProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = baseColor,
                            trackColor = Color(0xFF2C1930)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Aktivní trvání: ${effect.durationTurns} / ${effect.maxDuration} kol", fontSize = 9.sp, color = Color.Gray)
                            if (effect.isExpiringSoon) {
                                Text("⚠️ Brzy vyprší!", fontSize = 9.sp, color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                            } else {
                                Text("✓ Stabilní účinek", fontSize = 9.sp, color = Color(0xFF81C784))
                            }
                        }
                    }
                }

                // Description and Stat Modifiers
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x33FFFFFF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Popis a dopad na boj:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF48FB1)
                        )
                        Text(
                            text = if (effect.description.isNotBlank()) effect.description else getFallbackDescription(effect),
                            fontSize = 12.sp,
                            color = Color(0xFFEEEEEE),
                            lineHeight = 16.sp
                        )

                        if (effect.value > 0) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = baseColor.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "Hodnota účinku: ${effect.value} bodů (${if (isBuff) "+" else "-"}${effect.value} za kolo)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Tactical Countermeasure Tip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1F1528)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("💡", fontSize = 12.sp)
                        Text(
                            text = getTacticalTip(effect),
                            fontSize = 10.sp,
                            color = Color(0xFFB0BEC5),
                            lineHeight = 14.sp
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBuff) Color(0xFF2E7D32) else Color(0xFF7B1FA2)
                    )
                ) {
                    Text("Rozumím", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun getFallbackDescription(effect: CombatStatusEffect): String {
    return when (effect.type) {
        "BLEED" -> "Utrpěl hluboké krvácející rány. Na začátku každého kola ztrácí ${effect.value} HP."
        "POISON" -> "Toxický jed proudí v žilách. Udílí poškození v čase a snižuje regeneraci."
        "STUN" -> "Omráčen těžkým úderem nebo bleskem! Vynechává své následující kolo akce."
        "FREEZE" -> "Zmrazen ledovou magií. Nelze zaujmout obranný postoj a rychlost je snížena."
        "BURN" -> "V plamenech! Udílí ohnivé poškození každé kolo a narušuje koncentraci na kouzla."
        "SHOCK" -> "Elektrický výboj narušuje manový tok a odčerpává manu/energii."
        "ATK_BUFF" -> "Bojová zuřivost zvyšuje sílu všech prováděných útoků a schopností."
        "DEF_BUFF", "SHIELD" -> "Ochranná bariéra absorbuje příchozí poškození a snižuje utržená zranění."
        "REGEN" -> "Léčivá aura obnovuje ztracené životy na začátku každého kola."
        "HAREM_BLESSING" -> "Pouto lásky a oddanosti z harému posiluje vůli, morálku a regeneraci."
        "CRIT_BUFF" -> "Zvýšené soustředění dává vysokou šanci na drtivý kritický zásah."
        "RESONANCE" -> "Temná esence rezonuje v těle a umožňuje okamžité sesílání mocných kouzel."
        else -> "Aktivní bojový status ovlivňující průběh střetnutí."
    }
}

private fun getTacticalTip(effect: CombatStatusEffect): String {
    return if (effect.isBuff) {
        "Tip: Využij aktivní posílení k maximálnímu útoku nebo doplnění zdrojů dříve, než vyprší časomíra!"
    } else {
        "Tip: Proti debuffům použij schopnost Svaté kněžky (Očista) nebo vyčkej na vypršení časovače v defenzívě."
    }
}

/**
 * Utility to convert 1v1 Boss combat session states to CombatStatusEffect list with countdowns.
 */
fun buildStatusEffectsFor1v1Boss(session: CombatSession): List<CombatStatusEffect> {
    val list = mutableListOf<CombatStatusEffect>()
    if (session.enemyBleedTurns > 0) {
        list.add(
            CombatStatusEffect(
                id = "boss_bleed",
                name = "Krvácení",
                icon = "🩸",
                type = "BLEED",
                value = 14,
                durationTurns = session.enemyBleedTurns,
                maxDuration = 3,
                description = "Nepřítel krvácí z hlubokých ran. Na začátku každého kola ztrácí 14 HP.",
                element = Element.PHYSICAL
            )
        )
    }
    if (session.enemyStunned) {
        list.add(
            CombatStatusEffect(
                id = "boss_stun",
                name = "Omráčení",
                icon = "💫",
                type = "STUN",
                value = 0,
                durationTurns = 1,
                maxDuration = 1,
                description = "Boss je omráčen! Nemůže v tomto kole provést útok.",
                element = Element.LIGHTNING
            )
        )
    }
    if (session.activeBuff?.contains("Prokletí") == true) {
        list.add(
            CombatStatusEffect(
                id = "boss_curse",
                name = "Prokletí stínů",
                icon = "👁️",
                type = "CURSE",
                value = 25,
                durationTurns = 2,
                maxDuration = 2,
                description = "Kletba temnoty oslabuje nepřítele. Uděluje o 25% menší poškození.",
                element = Element.DARK
            )
        )
    }
    if (session.turnCount % 3 == 0) {
        list.add(
            CombatStatusEffect(
                id = "boss_fury",
                name = "Fázová zuřivost",
                icon = "⚡",
                type = "ATK_BUFF",
                value = 40,
                durationTurns = 1,
                maxDuration = 1,
                description = "Boss soustředí ničivou sílu pro okamžitý fázový útok!",
                element = Element.FIRE
            )
        )
    }
    return list
}

/**
 * Utility to convert 1v1 Player combat session states to CombatStatusEffect list with countdowns.
 */
fun buildStatusEffectsFor1v1Player(
    session: CombatSession,
    player: Player,
    deployedChar: Character?
): List<CombatStatusEffect> {
    val list = mutableListOf<CombatStatusEffect>()
    if (session.isDefending) {
        list.add(
            CombatStatusEffect(
                id = "player_defend",
                name = "Obranný postoj",
                icon = "🛡️",
                type = "DEF_BUFF",
                value = 65,
                durationTurns = 1,
                maxDuration = 1,
                description = "Kryt se štítem: Utržené poškození v tomto kole je sníženo o 65% a regeneruje se TE.",
                element = Element.PHYSICAL
            )
        )
    }
    if (session.activeBuff?.contains("Požehnání") == true) {
        list.add(
            CombatStatusEffect(
                id = "player_blessing",
                name = "Požehnání harému",
                icon = "💖",
                type = "HAREM_BLESSING",
                value = 20,
                durationTurns = 2,
                maxDuration = 2,
                description = "Oblíbenkyně z harému ti dodává psychickou sílu (+28 HP, regenerace a +20% k poškození).",
                element = Element.HOLY
            )
        )
    }
    if (player.darkEnergy >= 20) {
        list.add(
            CombatStatusEffect(
                id = "player_resonance",
                name = "Temná rezonance",
                icon = "🔮",
                type = "RESONANCE",
                value = 15,
                durationTurns = 3,
                maxDuration = 3,
                description = "Tvá stínová magie je plně nabitá pro sesílání zničujících stínových kleteb a vysávání duší.",
                element = Element.DARK
            )
        )
    }
    if (session.playerHp <= (session.playerMaxHp * 0.3f)) {
        list.add(
            CombatStatusEffect(
                id = "player_critical",
                name = "Kritický stav HP",
                icon = "⚠️",
                type = "CRITICAL_HP",
                value = 0,
                durationTurns = 1,
                maxDuration = 1,
                description = "Životy klesly pod 30%! Použij balzám nebo vysátí duše dříve, než padneš.",
                element = Element.PHYSICAL
            )
        )
    }
    return list
}
