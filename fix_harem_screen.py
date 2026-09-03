with open('app/src/main/java/com/example/haremdark/ui/screens/HaremScreen.kt', 'r') as f:
    text = f.read()

import re
text = re.sub(r'haremViewModel\.openProfile\((.*?)\}', r'haremViewModel.openProfile(\1) }', text)
text = re.sub(r'haremViewModel\.openInteraction\((.*?)\}', r'haremViewModel.openInteraction(\1) }', text)

with open('app/src/main/java/com/example/haremdark/ui/screens/HaremScreen.kt', 'w') as f:
    f.write(text)
