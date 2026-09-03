with open('app/src/main/java/com/example/haremdark/ui/screens/EmpireScreen.kt', 'r') as f:
    text = f.read()

imports_to_add = """import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
"""

if "import androidx.compose.animation.core.Animatable" not in text:
    text = text.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\n" + imports_to_add)

with open('app/src/main/java/com/example/haremdark/ui/screens/EmpireScreen.kt', 'w') as f:
    f.write(text)
