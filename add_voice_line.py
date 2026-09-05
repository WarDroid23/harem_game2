import re

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'r') as f:
    text = f.read()

target = """        addLog(message)
        updateState { it.copy() }
        return Pair(true, message)
    }"""

replacement = """        addLog(message)
        updateState { it.copy() }
        
        if (character.affinityPoints >= 100 || character.oblibena || character.jeManzelkou) {
            val lines = listOf(
                "Můj pane, tvá vůle je mým zákonem.",
                "Cokoliv si budeš přát.",
                "Jsem jen a jen tvá, můj vládce.",
                "Miluji tě, můj temný pane.",
                "Moje tělo i duše patří jen tobě.",
                "Jsem připravena na všechno, co si žádáš.",
                "Z tvých rukou přijmu cokoliv."
            )
            com.example.haremdark.domain.VoiceManager.speak(lines.random())
        }
        
        return Pair(true, message)
    }"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/domain/GameEngine.kt', 'w') as f:
    f.write(text)
