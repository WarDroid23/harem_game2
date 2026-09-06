import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

# Add imports for longPreferencesKey and intPreferencesKey
text = text.replace('import androidx.datastore.preferences.core.stringPreferencesKey', 'import androidx.datastore.preferences.core.stringPreferencesKey\nimport androidx.datastore.preferences.core.longPreferencesKey\nimport androidx.datastore.preferences.core.intPreferencesKey\nimport com.example.haremdark.models.InventoryItem')


# Add DailyRewardState model at top
reward_state = """
data class DailyRewardState(
    val consecutiveDays: Int,
    val rewardGold: Int,
    val rewardMana: Int,
    val itemReward: InventoryItem?
)
"""
text = text.replace('class GameEngine(private val context: Context) {', reward_state + '\nclass GameEngine(private val context: Context) {')

# Add properties and methods to GameEngine
reward_funcs = """
    private val _dailyRewardAvailable = MutableStateFlow<DailyRewardState?>(null)
    val dailyRewardAvailable: StateFlow<DailyRewardState?> = _dailyRewardAvailable.asStateFlow()

    fun checkDailyLogin() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val lastLoginKey = longPreferencesKey("last_login_epoch_day")
            val streakKey = intPreferencesKey("consecutive_login_days")
            val prefs = context.dataStore.data.first()
            
            val lastLoginEpochDay = prefs[lastLoginKey] ?: 0L
            val consecutiveDays = prefs[streakKey] ?: 0
            
            val currentEpochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            
            if (currentEpochDay > lastLoginEpochDay) {
                val isConsecutive = (currentEpochDay - lastLoginEpochDay) == 1L
                val newStreak = if (isConsecutive) consecutiveDays + 1 else 1
                
                val rewardGold = 100 + (newStreak * 20).coerceAtMost(400)
                val rewardMana = 20 + (newStreak * 5).coerceAtMost(100)
                
                val itemReward = if (newStreak % 5 == 0) {
                    InventoryItem(
                        id = "daily_gift_epic",
                        name = "Epický dar za věrnost",
                        description = "Cenný předmět pro tvůj harém. (Dárek)",
                        count = 1,
                        price = 250,
                        category = "gift",
                        icon = "🎁",
                        rarity = "Epický",
                        effectDescription = "Velmi zvyšuje náklonnost"
                    )
                } else null
                
                _dailyRewardAvailable.value = DailyRewardState(
                    consecutiveDays = newStreak,
                    rewardGold = rewardGold,
                    rewardMana = rewardMana,
                    itemReward = itemReward
                )
            }
        }
    }

    fun claimDailyReward() {
        val reward = _dailyRewardAvailable.value ?: return
        _dailyRewardAvailable.value = null
        
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val lastLoginKey = longPreferencesKey("last_login_epoch_day")
            val streakKey = intPreferencesKey("consecutive_login_days")
            val currentEpochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            
            context.dataStore.edit { prefs ->
                prefs[lastLoginKey] = currentEpochDay
                prefs[streakKey] = reward.consecutiveDays
            }
            
            updateState { state ->
                val p = state.player
                val newItems = p.items.toMutableList()
                if (reward.itemReward != null) {
                    val existingIdx = newItems.indexOfFirst { it.id == reward.itemReward.id }
                    if (existingIdx != -1) {
                        val ex = newItems[existingIdx]
                        newItems[existingIdx] = ex.copy(count = ex.count + 1)
                    } else {
                        newItems.add(reward.itemReward)
                    }
                }
                
                state.copy(
                    player = p.copy(
                        gold = p.gold + reward.rewardGold,
                        darkEnergy = (p.darkEnergy + reward.rewardMana).coerceAtMost(p.maxDarkEnergy),
                        items = newItems
                    )
                )
            }
            autoSave()
        }
    }
"""

text = text.replace('    init {', reward_funcs + '\n    init {')

# Call checkDailyLogin in init block (after loading state)
init_replacement = """        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val savedState = loadStateSuspend("save_slot_autosave") 
                ?: loadStateSuspend("save_slot_1")
            
            if (savedState != null) {
                _gameState.value = savedState
                _currentTheme.value = savedState.currentTheme
            }
            
            checkDailyLogin()
        }"""
text = re.sub(r'kotlinx\.coroutines\.CoroutineScope\(kotlinx\.coroutines\.Dispatchers\.IO\)\.launch \{\s*val savedState = loadStateSuspend\("save_slot_autosave"\)\s*\?: loadStateSuspend\("save_slot_1"\)\s*if \(savedState != null\) \{\s*_gameState\.value = savedState\s*_currentTheme\.value = savedState\.currentTheme\s*\}\s*\}', init_replacement, text)


with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)

