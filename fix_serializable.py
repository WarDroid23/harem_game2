with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'r') as f:
    text = f.read()

text = text.replace("@Serializable\n\n@Serializable\ndata class PartyBuff", "@Serializable\ndata class PartyBuff")

with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'w') as f:
    f.write(text)
