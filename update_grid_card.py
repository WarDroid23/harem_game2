with open('app/src/main/java/com/example/haremdark/ui/components/CharacterCard.kt', 'r') as f:
    text = f.read()

target = """                    if (character.naNajmu) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFB74D).copy(alpha = 0.95f)
                        ) {
                            Text(
                                text = "💰 Nájem (${character.najemZbyvaDni}d)",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }"""

replacement = """                    if (character.naNajmu) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFB74D).copy(alpha = 0.95f)
                        ) {
                            Text(
                                text = "💰 Nájem (${character.najemZbyvaDni}d)",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    val rel = character.getRelationship()
                    if (rel != com.example.haremdark.models.RelStatus.NEUTRAL) {
                        val relColor = when(rel) {
                            com.example.haremdark.models.RelStatus.BLOOD_SISTER -> Color(0xFFD32F2F)
                            com.example.haremdark.models.RelStatus.DEVOTED -> Color(0xFF4CAF50)
                            com.example.haremdark.models.RelStatus.IN_LOVE -> Color(0xFFE91E63)
                            com.example.haremdark.models.RelStatus.BROKEN -> Color(0xFF9E9E9E)
                            com.example.haremdark.models.RelStatus.OBEDIENT -> Color(0xFF2196F3)
                            com.example.haremdark.models.RelStatus.REBELLIOUS -> Color(0xFFFF9800)
                            else -> Color.Gray
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = relColor.copy(alpha = 0.8f)
                        ) {
                            Text(
                                text = rel.title,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/ui/components/CharacterCard.kt', 'w') as f:
    f.write(text)
