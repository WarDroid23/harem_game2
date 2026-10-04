package com.example.haremdark.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haremdark.domain.HapticManager
import com.example.haremdark.models.TimeOfDay

@Composable
fun TimeOfDayBadge(
    currentDay: Int,
    onTimeOfDayClicked: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var manualOverride by remember { mutableStateOf<TimeOfDay?>(null) }
    val timeOfDay = manualOverride ?: TimeOfDay.getForDay(currentDay)

    val animatedBgColor by animateColorAsState(
        targetValue = timeOfDay.resolvedSurface,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "timeOfDayBg"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = animatedBgColor,
        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
        modifier = modifier
            .testTag("time_of_day_badge")
            .clickable {
                HapticManager.vibrateClick()
                manualOverride = when (timeOfDay) {
                    TimeOfDay.NIGHT -> TimeOfDay.DAWN
                    TimeOfDay.DAWN -> TimeOfDay.NOON
                    TimeOfDay.NOON -> TimeOfDay.DUSK
                    TimeOfDay.DUSK -> TimeOfDay.NIGHT
                }
                onTimeOfDayClicked?.invoke()
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(timeOfDay.emoji, fontSize = 16.sp)
            Column {
                Text(
                    text = timeOfDay.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = timeOfDay.bonusDescription,
                    fontSize = 8.sp,
                    color = Color(0xFFFF80AB),
                    maxLines = 1
                )
            }
        }
    }
}
