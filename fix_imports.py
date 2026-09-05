import re
import os

files = [
    'app/src/main/java/com/example/haremdark/ui/components/InteractiveDialogs.kt',
    'app/src/main/java/com/example/haremdark/ui/screens/ArchiveTab.kt',
    'app/src/main/java/com/example/haremdark/ui/screens/WorldMapScreen.kt'
]

for file in files:
    with open(file, 'r') as f:
        text = f.read()
    
    # ensure exactly one ContentScale import
    text = re.sub(r'import androidx\.compose\.ui\.layout\.ContentScale\n?', '', text)
    text = text.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport androidx.compose.ui.layout.ContentScale")
    
    with open(file, 'w') as f:
        f.write(text)
