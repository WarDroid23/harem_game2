with open('app/src/main/java/com/example/haremdark/ui/screens/EmpireScreen.kt', 'r') as f:
    text = f.read()

# Add imports
imports = """import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf"""

if "import com.patrykandpatrick.vico" not in text:
    text = text.replace("import androidx.compose.ui.unit.sp", imports)

# 1. Update Tabs array
target_tabs = """    val tabs = listOf("🗡️ Mafie & Území", "🏰 Pevnost & Budovy", "💰 Nájemní registr")"""
replacement_tabs = """    val tabs = listOf("🗡️ Mafie", "🏰 Budovy", "💰 Nájem", "📈 Produkce")"""
text = text.replace(target_tabs, replacement_tabs)

# 2. Add to when block
target_when = """        when (selectedTab) {
            0 -> MafiaTab(gameState, engine)
            1 -> BuildingsTab(gameState, engine)
            2 -> RentalsHubTab(gameState)
        }"""
replacement_when = """        when (selectedTab) {
            0 -> MafiaTab(gameState, engine)
            1 -> BuildingsTab(gameState, engine)
            2 -> RentalsHubTab(gameState)
            3 -> StatisticsTab(gameState)
        }"""
text = text.replace(target_when, replacement_when)

# 3. Add StatisticsTab Composable
statistics_tab = """
@Composable
fun StatisticsTab(gameState: GameSave) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Historie produkce zlata", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    
                    if (gameState.resourceHistory.size >= 2) {
                        val goldEntries = gameState.resourceHistory.mapIndexed { index, stat ->
                            FloatEntry(x = index.toFloat(), y = stat.goldProduced.toFloat())
                        }
                        
                        Chart(
                            chart = lineChart(),
                            model = entryModelOf(goldEntries),
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    } else {
                        Text(
                            text = "Není dostatek dat pro graf (ukonči den).",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f),
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                }
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Historie produkce surovin", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    
                    if (gameState.resourceHistory.size >= 2) {
                        val woodEntries = gameState.resourceHistory.mapIndexed { index, stat ->
                            FloatEntry(x = index.toFloat(), y = stat.woodProduced.toFloat())
                        }
                        
                        Chart(
                            chart = lineChart(),
                            model = entryModelOf(woodEntries),
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                        Text("Graf ukazuje dřevo.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.7f))
                    } else {
                        Text(
                            text = "Není dostatek dat.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f)
                        )
                    }
                }
            }
        }
    }
}
"""

if "fun StatisticsTab" not in text:
    text = text + statistics_tab

with open('app/src/main/java/com/example/haremdark/ui/screens/EmpireScreen.kt', 'w') as f:
    f.write(text)
