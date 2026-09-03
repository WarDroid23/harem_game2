import re
with open('app/src/main/java/com/example/haremdark/ui/screens/HaremScreen.kt', 'r') as f:
    text = f.read()

# Let's see if there are any remaining syntax errors
print(re.search(r'haremViewModel\.openProfile\((.*?)\}', text))
