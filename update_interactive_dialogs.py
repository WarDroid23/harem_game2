import re

with open('app/src/main/java/com/example/haremdark/ui/components/InteractiveDialogs.kt', 'r') as f:
    text = f.read()

target1 = """    val sectionTabs = listOf("📊 Profil", "💖 Náklonnost", "🎁 Dary", "⚡ Akce", "✨ Dovednosti")"""
replacement1 = """    val sectionTabs = listOf("📊 Profil", "🛡️ Výbava", "💖 Náklonnost", "🎁 Dary", "⚡ Akce", "✨ Dovednosti")"""
text = text.replace(target1, replacement1)

target2 = """                        when (selectedSection) {
                            0 -> CharacterProfileTab(character, loyalty, phase)
                            1 -> CharacterAffinityTab(character)
                            2 -> CharacterGiftsTab(character, gameState, onGiveGift)
                            3 -> CharacterInteractionsTab(character, player, gameState.unlockedDomains, engine, onDismiss)
                            4 -> CharacterSkillsTab(character, player, onUpgradeSkill)
                        }"""
replacement2 = """                        when (selectedSection) {
                            0 -> CharacterProfileTab(character, loyalty, phase)
                            1 -> CharacterEquipmentTab(character, player, engine)
                            2 -> CharacterAffinityTab(character)
                            3 -> CharacterGiftsTab(character, gameState, onGiveGift)
                            4 -> CharacterInteractionsTab(character, player, gameState.unlockedDomains, engine, onDismiss)
                            5 -> CharacterSkillsTab(character, player, onUpgradeSkill)
                        }"""
text = text.replace(target2, replacement2)

with open('app/src/main/java/com/example/haremdark/ui/components/InteractiveDialogs.kt', 'w') as f:
    f.write(text)
