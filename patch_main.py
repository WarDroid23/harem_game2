import re

with open("app/src/main/java/com/example/haremdark/MainActivity.kt", "r") as f:
    content = f.read()

# Add navigation imports
import_block = """import androidx.navigation.compose.*
import androidx.navigation.NavGraph.Companion.findStartDestination"""

content = content.replace("import com.example.haremdark.ui.theme.HaremDarkTheme", "import com.example.haremdark.ui.theme.HaremDarkTheme\n" + import_block)

# Replace state var
content = content.replace("var currentNavIndex by remember { mutableIntStateOf(0) }", """val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: "home\"""")

# Replace the NavigationDestination data class mapping and the whole Scaffold
# Wait, it's easier to just rewrite the whole Scaffold.
