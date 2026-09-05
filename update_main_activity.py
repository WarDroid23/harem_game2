import re

with open('app/src/main/java/com/example/haremdark/MainActivity.kt', 'r') as f:
    text = f.read()

if "VoiceManager.init(this)" not in text:
    target = """        setContent {
            HaremDarkTheme {"""
    replacement = """        com.example.haremdark.domain.VoiceManager.init(this)
        
        setContent {
            HaremDarkTheme {"""
    text = text.replace(target, replacement)
    
    target_destroy = """}"""
    # Wait, need to add onDestroy
    
    with open('app/src/main/java/com/example/haremdark/MainActivity.kt', 'w') as f:
        f.write(text)
