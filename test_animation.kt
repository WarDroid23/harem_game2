import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnimatedResourceItem(icon: String, name: String, value: Int) {
    var previousValue by remember { mutableStateOf(value) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(value) {
        if (value > previousValue) {
            scale.animateTo(1.2f, animationSpec = tween(150))
            scale.animateTo(1f, animationSpec = tween(300))
        }
        previousValue = value
    }

    Row(
        modifier = Modifier
            .scale(scale.value)
            .background(Color.DarkGray.copy(alpha=0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(icon, fontSize = 14.sp)
        Text("$value", color = Color.White, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    }
}
