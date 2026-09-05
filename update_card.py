with open('app/src/main/java/com/example/haremdark/ui/components/CharacterCard.kt', 'r') as f:
    text = f.read()

if "getRelationship" not in text:
    text = text.replace("import com.example.haremdark.models.Character", "import com.example.haremdark.models.Character\nimport com.example.haremdark.models.getRelationship")

target = """                            if (character.jeManzelkou) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFE040FB).copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE040FB))
                                ) {
                                    Text(
                                        text = "💍 Manželka",
                                        color = Color(0xFFE040FB),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }"""

replacement = """                            if (character.jeManzelkou) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFE040FB).copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE040FB))
                                ) {
                                    Text(
                                        text = "💍 Manželka",
                                        color = Color(0xFFE040FB),
                                        fontSize = 10.sp,
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
                                    color = relColor.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, relColor)
                                ) {
                                    Text(
                                        text = rel.title,
                                        color = relColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/ui/components/CharacterCard.kt', 'w') as f:
    f.write(text)
