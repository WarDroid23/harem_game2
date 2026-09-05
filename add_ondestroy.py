import re

with open('app/src/main/java/com/example/haremdark/MainActivity.kt', 'r') as f:
    text = f.read()

if "override fun onDestroy()" not in text:
    new_method = """
    override fun onDestroy() {
        com.example.haremdark.domain.VoiceManager.shutdown()
        super.onDestroy()
    }
}
"""
    text = text.rstrip()
    if text.endswith('}'):
        text = text[:-1] + new_method
        with open('app/src/main/java/com/example/haremdark/MainActivity.kt', 'w') as f:
            f.write(text)
