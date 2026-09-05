with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'r') as f:
    text = f.read()

append = """

enum class RelStatus(val title: String, val buffType: String, val buffValue: Float, val description: String) {
    BLOOD_SISTER("Krvavá sestra", "COMBAT_DMG", 0.15f, "+15% k poškození v boji (aktivní v aréně)"),
    DEVOTED("Oddaná", "GLOBAL_RES", 0.05f, "+5% ke globální produkci surovin"),
    IN_LOVE("Zamilovaná", "MORALE_RES", 0.10f, "+10% šance na pozitivní noční eventy a rychlé hojení"),
    BROKEN("Zlomená otrokyně", "OBEDIENCE", 0.20f, "+20% zisk zlata ze všech pronájmů, nižší obrana"),
    OBEDIENT("Poslušná", "RESOURCE", 0.02f, "+2% ke globální produkci surovin"),
    REBELLIOUS("Rebelující", "PENALTY", -0.10f, "-10% poškození v boji"),
    NEUTRAL("Neutrální", "NONE", 0f, "Žádný zvláštní efekt")
}

fun Character.getRelationship(): RelStatus {
    if (fazeZkazenosti >= 5 && bloodlust >= 50) return RelStatus.BLOOD_SISTER
    if (loajalita >= 80 && duvera >= 80 && romanceBody >= 50) return RelStatus.DEVOTED
    if (oblibena || (romanceBody >= 30 && loajalita > 50)) return RelStatus.IN_LOVE
    if (strach >= 70 && broken >= 40) return RelStatus.BROKEN
    if (poslusnost >= 60 && strach >= 40) return RelStatus.OBEDIENT
    if (loajalita <= 30 && strach <= 30) return RelStatus.REBELLIOUS
    return RelStatus.NEUTRAL
}
"""

text = text + append

with open('app/src/main/java/com/example/haremdark/models/GameModels.kt', 'w') as f:
    f.write(text)
