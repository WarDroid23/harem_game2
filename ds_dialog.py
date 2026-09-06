import re

with open('app/src/main/java/com/example/haremdark/ui/components/InteractiveDialogs.kt', 'r') as f:
    text = f.read()

# Fix the sectionTabs bug
old_tabs = 'val sectionTabs = listOf("📊 Profil", "🛡️ Výbava", "💖 Náklonnost", "🎁 Dary", "⚡ Akce", "✨ Dovednosti")'
new_tabs = 'val sectionTabs = listOf("📊 Profil", "💖 Náklonnost", "🎁 Dary", "⚡ Akce", "✨ Dovednosti")'
text = text.replace(old_tabs, new_tabs)

# Ensure the when statement maps correctly
# 0 -> ProfileAndStatsTab
# 1 -> AffinityAndDialogueTab
# 2 -> GiftingAndItemsTab
# 3 -> InteractionsSectionTab
# 4 -> SkillTreeTab

with open('app/src/main/java/com/example/haremdark/ui/components/InteractiveDialogs.kt', 'w') as f:
    f.write(text)

