package com.example.haremdark.data

import com.example.haremdark.models.CharacterArchetype
import com.example.haremdark.models.DegradationPhase
import com.example.haremdark.models.LoyaltyTier

object StaticData {

    val NAMES = listOf(
        "Cleopatra", "Valeria", "Morgana", "Elena", "Lyra", "Selena", "Astrid",
        "Lilith", "Aria", "Cassandra", "Roxana", "Diana", "Nyx", "Seraphina",
        "Kaelen", "Vespera", "Morrigan", "Sybilla", "Yvaine", "Ravenna",
        "Beatrix", "Ophelia", "Isolde", "Genevieve", "Katarina", "Nadia"
    )

    val ARCHETYPES = mapOf(
        "subka" to CharacterArchetype("subka", "Submisivní", "Poslušná a touží po pevném vedení.", 1.3f, 1.2f, 1.1f, 0.8f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.2f, 1.0f, 0.05f),
        "odvazna" to CharacterArchetype("odvazna", "Odvážná", "Vzdoruje, ale její hrdost lze zlomit.", 0.7f, 0.6f, 1.0f, 0.9f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.8f, 0.9f, 0.15f),
        "ustrasena" to CharacterArchetype("ustrasena", "Ustrašená", "Snadno se bojí, ale je velmi poslušná.", 1.1f, 1.1f, 0.8f, 1.5f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.1f, 1.2f, 0.02f),
        "vzdorna" to CharacterArchetype("vzdorna", "Vzdorná", "Aktivně vzdoruje, vyžaduje silnou ruku pána.", 0.5f, 0.5f, 0.8f, 0.7f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.7f, 0.7f, 0.20f),
        "touha" to CharacterArchetype("touha", "Toužící", "Silně vzrušivá, snadno ovlivnitelná potěšením.", 1.0f, 0.9f, 1.0f, 1.0f, 1.4f, 1.2f, 1.0f, 1.0f, 1.0f, 1.0f, 0.9f, 1.3f, 0.06f),
        "zlomena" to CharacterArchetype("zlomena", "Zlomená", "Už dříve zlomená, téměř bez vlastní vůle.", 1.5f, 1.5f, 1.0f, 0.6f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.4f, 1.1f, 0.00f),
        "manipulativni" to CharacterArchetype("manipulativni", "Manipulativní", "Snaží se pána ovlivnit, skrývá své intriky.", 0.8f, 0.7f, 0.6f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.6f, 1.4f, 0.10f),
        "chladna" to CharacterArchetype("chladna", "Chladná", "Zdánlivě bez emocí, těžko se s ní navazuje pouto.", 0.9f, 1.0f, 0.7f, 1.0f, 0.8f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.7f, 0.8f, 0.08f),
        "hysterialni" to CharacterArchetype("hysterialni", "Hysterická", "Nestabilní a výbušná, rychle střídá nálady.", 0.9f, 1.0f, 1.0f, 1.3f, 1.1f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.3f, 1.2f, 0.12f),
        "slechticna" to CharacterArchetype("slechticna", "Zlomená šlechtična", "Bývalá urozená dáma. Hrdost se láme pomalu, ale padá hluboko.", 0.6f, 0.55f, 0.7f, 0.85f, 1.0f, 1.0f, 1.4f, 1.0f, 1.0f, 1.0f, 0.75f, 1.25f, 0.12f),
        "nymfomanka" to CharacterArchetype("nymfomanka", "Nymfomanka", "Tělo ji zrazuje. Touha je silnější než pýcha.", 1.1f, 0.95f, 1.0f, 0.7f, 1.6f, 1.4f, 1.0f, 1.0f, 1.0f, 1.0f, 0.85f, 1.5f, 0.04f),
        "ticha_panenka" to CharacterArchetype("ticha_panenka", "Tichá panenka", "Mluví málo. Dokonalá, tichá hračka v rukou pána.", 1.2f, 1.15f, 0.9f, 1.1f, 1.0f, 1.0f, 1.0f, 1.0f, 1.2f, 1.0f, 1.15f, 1.1f, 0.03f),
        "krvava_subka" to CharacterArchetype("krvava_subka", "Krvavá subka", "Bolest ji vzrušuje. Čím víc ran, tím víc se otevírá.", 1.25f, 1.0f, 1.0f, 0.6f, 1.15f, 1.0f, 1.0f, 1.5f, 1.0f, 1.2f, 1.4f, 0.9f, 0.02f),
        "posedla" to CharacterArchetype("posedla", "Posedlá", "Něco v ní se zlomilo. Hledá pána, který ji dokončí.", 1.3f, 1.2f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.25f, 1.0f, 1.35f, 1.2f, 0.01f)
    )

    val LOYALTY_TIERS = listOf(
        LoyaltyTier("vzpoura", 0, 14, "Vzbouřenkyně", "Otevřeně vzdoruje. Riziko útěku vysoké.", 1.8f, 0.6f, 1.3f, 0xFFE53935),
        LoyaltyTier("neduvera", 15, 29, "Nedůvěřivá", "Poslouchá jen ze strachu. Loajalita se buduje pomalu.", 1.3f, 0.8f, 1.15f, 0xFFFF9800),
        LoyaltyTier("opatrna", 30, 49, "Opatrná služka", "Plní příkazy bez nadšení. Čeká, co přijde.", 1.0f, 1.0f, 1.0f, 0xFFFFEB3B),
        LoyaltyTier("oddana", 50, 69, "Oddaná", "Začíná ti věřit. Odměny mají silnější účinek.", 0.6f, 1.15f, 0.95f, 0xFF00E5FF),
        LoyaltyTier("verna", 70, 84, "Věrná otrokyně", "Tvé jméno ji drží. Útěk je nepravděpodobný.", 0.25f, 1.25f, 0.85f, 0xFF4CAF50),
        LoyaltyTier("zasvecena", 85, 94, "Zasvěcená", "Téměř bez vlastní vůle ve tvůj prospěch. Hluboká oddanost.", 0.05f, 1.35f, 0.75f, 0xFFE040FB),
        LoyaltyTier("absolutni", 95, 100, "Absolutní majetek", "Neexistuje bez tebe. Útěk = 0. Absolutní odevzdání.", 0.0f, 1.5f, 0.7f, 0xFFFFD700)
    )

    fun getLoyaltyTier(loyalty: Int): LoyaltyTier {
        val clamped = loyalty.coerceIn(0, 100)
        return LOYALTY_TIERS.firstOrNull { clamped in it.min..it.max } ?: LOYALTY_TIERS.first()
    }

    val DEGRADATION_PHASES = mapOf(
        0 to DegradationPhase(0, "Čistá", "Otrokyně je zatím nedotčená."),
        1 to DegradationPhase(1, "Submisivní služka", "Mírně poddajná, začíná být poslušná.", mapOf("poslusnost" to 5, "submisivita" to 5), reqZavislost = 15, reqBroken = 15),
        2 to DegradationPhase(2, "Sub slut", "Sexuálně oddaná, touží po pánově pozornosti.", mapOf("touha" to 10, "submisivita" to 10, "loajalita" to 3), reqZavislost = 30, reqBroken = 30, reqTouha = 50),
        3 to DegradationPhase(3, "Sub slutty whore", "Zcela oddaná rozkoši, poslouchá bez odporu.", mapOf("touha" to 15, "submisivita" to 15, "poslusnost" to 10, "loajalita" to 5), reqZavislost = 50, reqBroken = 50, reqTouha = 70),
        4 to DegradationPhase(4, "Trash slut", "Zcela zlomená pýchou, dělá cokoliv pro přízeň pána.", mapOf("touha" to 20, "submisivita" to 20, "poslusnost" to 15, "loajalita" to 8, "humiliation" to 10), reqZavislost = 70, reqBroken = 70, reqTouha = 85, reqHumiliation = 50),
        5 to DegradationPhase(5, "Stepmom", "Starší otrokyně s mateřským instinktem, pro domácí službu.", mapOf("poslusnost" to 15, "loajalita" to 12, "duvera" to 10, "plodnost" to 10), reqAge = 30, reqBroken = 40, reqLoajalita = 40),
        6 to DegradationPhase(6, "Stepsister", "Mladá, hravá, ale zkažená otrokyně.", mapOf("touha" to 15, "vlhkost" to 15, "submisivita" to 10, "duvera" to 5), reqAge = 18, reqBroken = 30, reqZavislost = 30),
        7 to DegradationPhase(7, "Pregnant teen", "Těhotná mladá otrokyně, zranitelná a oddaná.", mapOf("plodnost" to 20, "submisivita" to 10, "touha" to 10), reqAge = 18, reqBroken = 50, reqPregnant = true),
        8 to DegradationPhase(8, "Prego mom", "Těhotná matka, ideální chovná otrokyně.", mapOf("plodnost" to 25, "submisivita" to 20, "poslusnost" to 15, "loajalita" to 10), reqAge = 25, reqBroken = 60, reqPregnant = true),
        9 to DegradationPhase(9, "Granny", "Starší zkušená otrokyně, loajální a oddaná dominiu.", mapOf("poslusnost" to 20, "loajalita" to 15, "duvera" to 10), reqAge = 45, reqBroken = 50, reqLoajalita = 60),
        10 to DegradationPhase(10, "Chovná matka rozkoše", "Naprosto odevzdaná těhotná matka, zcela oddaná pánovi.", mapOf("touha" to 30, "submisivita" to 30, "poslusnost" to 25, "loajalita" to 20, "plodnost" to 30), reqZavislost = 90, reqBroken = 90, reqTouha = 95, reqPregnant = true, reqAge = 20),
        11 to DegradationPhase(11, "Živá panenka", "Tělo reaguje, ale vůle je pryč. Pohybuje se jen na příkaz.", mapOf("poslusnost" to 25, "submisivita" to 25, "broken" to 5, "mindbreak" to 8, "touha" to 10), reqBroken = 85, reqMindbreak = 60, reqPoslusnost = 80),
        12 to DegradationPhase(12, "Bezmyšlenková subka", "Mysl je vymazaná. Umí jen šeptat „ano, můj pane“.", mapOf("poslusnost" to 30, "submisivita" to 30, "loajalita" to 20, "mindbreak" to 15), reqMindbreak = 80, reqBroken = 90, reqPoslusnost = 90),
        13 to DegradationPhase(13, "Krvavá oddaná", "Bolest se stala její jedinou řečí lásky a uctívání.", mapOf("pain_addiction" to 25, "submisivita" to 20, "broken" to 10, "scarred" to 10, "touha" to 15), reqPainAddiction = 70, reqScarred = 40, reqBroken = 75),
        14 to DegradationPhase(14, "Věčná otrokyně", "Už neexistuje jako osoba. Je absolutním majetkem pána.", mapOf("loajalita" to 30, "poslusnost" to 35, "submisivita" to 30, "duvera" to 20, "mindbreak" to 10), reqLoajalita = 95, reqBroken = 95, reqMindbreak = 70, reqPoslusnost = 95),
        15 to DegradationPhase(15, "Zlomová matka", "Těhotná, zlomená a hluboce oddaná. Nosí tvé potomstvo.", mapOf("plodnost" to 30, "poslusnost" to 25, "submisivita" to 25, "loajalita" to 25, "broken" to 10), reqPregnant = true, reqBroken = 85, reqLoajalita = 80, reqPoslusnost = 85)
    )

    fun calculatePhase(
        broken: Int,
        mindbreak: Int,
        poslusnost: Int,
        loajalita: Int,
        painAddiction: Int,
        scarred: Int,
        touha: Int,
        humiliation: Int,
        zavislost: Int,
        age: Int,
        pregnant: Boolean
    ): Int {
        var highest = 0
        for ((lvl, phase) in DEGRADATION_PHASES) {
            var ok = true
            if (phase.reqBroken > 0 && broken < phase.reqBroken) ok = false
            if (phase.reqMindbreak > 0 && mindbreak < phase.reqMindbreak) ok = false
            if (phase.reqPoslusnost > 0 && poslusnost < phase.reqPoslusnost) ok = false
            if (phase.reqLoajalita > 0 && loajalita < phase.reqLoajalita) ok = false
            if (phase.reqPainAddiction > 0 && painAddiction < phase.reqPainAddiction) ok = false
            if (phase.reqScarred > 0 && scarred < phase.reqScarred) ok = false
            if (phase.reqTouha > 0 && touha < phase.reqTouha) ok = false
            if (phase.reqHumiliation > 0 && humiliation < phase.reqHumiliation) ok = false
            if (phase.reqZavislost > 0 && zavislost < phase.reqZavislost) ok = false
            if (phase.reqAge > 0 && age < phase.reqAge) ok = false
            if (phase.reqPregnant && !pregnant) ok = false
            if (ok && lvl > highest) highest = lvl
        }
        return highest
    }
}
