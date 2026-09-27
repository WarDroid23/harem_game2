package com.example.haremdark.domain

import com.example.haremdark.models.Character
import com.example.haremdark.models.GameSave
import com.example.haremdark.models.Player
import com.example.haremdark.models.RelStatus
import com.example.haremdark.models.getRelationship
import java.io.Serializable
import java.util.UUID
import kotlin.random.Random

enum class EncounterCategory(val title: String, val icon: String, val badgeColorHex: Long) {
    HAREM_RELATIONSHIP("Vztahy v harému", "💖", 0xFFFF4081),
    DOMAIN_PROGRESSION("Rozvoj dominia", "🏰", 0xFFFFC107),
    INQUISITION_INTRUSION("Inkvizice & Hrozby", "⚔️", 0xFFE53935),
    SUBTERRANEAN_EXPEDITION("Podzemí & Krypta", "🗝️", 0xFF8E24AA),
    MYSTICAL_EVENT("Temná mystika", "🔮", 0xFF00BCD4),
    REBELLION_RISK("Vzpoura & Kázeň", "⛓️", 0xFFFF7043)
}

data class EncounterPrerequisites(
    val minPlayerLevel: Int = 1,
    val minPlayerDay: Int = 1,
    val minDomainExpansionLevel: Int = 1,
    val minAffinityLevel: Int = 0,
    val minLoyalty: Int = 0,
    val maxLoyalty: Int = 100,
    val minFear: Int = 0,
    val minAffection: Int = 0,
    val requiredRelStatus: RelStatus? = null,
    val requiresWife: Boolean = false,
    val requiresPartner: Boolean = false,
    val requiredArchetype: String? = null,
    val minCharacterCount: Int = 1
) : Serializable

data class ProgressionEncounterChoice(
    val id: String,
    val text: String,
    val style: String, // "dominant", "romantic", "graceful", "strategic", "passionate"
    val reactionText: String,
    val affinityGain: Int = 0,
    val loyaltyGain: Int = 0,
    val affectionGain: Int = 0,
    val obedienceGain: Int = 0,
    val fearGain: Int = 0,
    val moraleGain: Int = 0,
    val resourceRewards: Map<String, Int> = emptyMap(), // "gold", "wood", "stone", "iron", "mana", "darkEnergy", "sexEnergy", "influence"
    val outcomeSummary: String
) : Serializable

data class ProgressionEncounter(
    val id: String = UUID.randomUUID().toString(),
    val category: EncounterCategory,
    val title: String,
    val prerequisites: EncounterPrerequisites,
    val prologue: String,
    val speakerName: String,
    val speakerIcon: String = "📜",
    val characterId: String? = null,
    val monologue: String,
    val choices: List<ProgressionEncounterChoice>,
    val weight: Int = 100,
    val repeatable: Boolean = false
) : Serializable

data class ProgressionEncounterResult(
    val encounter: ProgressionEncounter,
    val chosenChoice: ProgressionEncounterChoice,
    val characterName: String?,
    val reactionText: String,
    val summaryText: String
) : Serializable

object ProgressionEncounterCatalog {

    fun getCatalog(): List<ProgressionEncounter> {
        return listOf(
            // 1. Rebellious Spark
            ProgressionEncounter(
                id = "enc_rebellious_spark",
                category = EncounterCategory.REBELLION_RISK,
                title = "⛓️ Jiskra vzdoru v temných chodbičkách",
                prerequisites = EncounterPrerequisites(
                    minPlayerLevel = 1,
                    minPlayerDay = 2,
                    maxLoyalty = 35,
                    minCharacterCount = 1
                ),
                prologue = "Noční ticho chodby protne tichý zvuk kovu. Zastihneš jednu z dívek, jak v šeru svírá skrytý klíč od brány dominia.",
                speakerName = "Služebná v šeru",
                speakerIcon = "⛓️",
                monologue = "„Můj pane... nemohu zde dál dýchat pod tvým těžkým pohledem! Dovol mi odejít, nebo mě svaž v řetězech, ale můj duch se neskloní jen tak bez boje!“",
                choices = listOf(
                    ProgressionEncounterChoice(
                        id = "reb_dominant",
                        text = "Ukaž jí železnou autoritu: „Tvé tělo i osud patří tomuto panství. Přivaž ji k pilíři kázně.“",
                        style = "dominant",
                        reactionText = "„Tvá síla mě mrazí v kostech... Znovu jsem pochopila, že utéct z tvého stínu je nemožné.“",
                        loyaltyGain = 18,
                        obedienceGain = 25,
                        fearGain = 10,
                        resourceRewards = mapOf("influence" to 10),
                        outcomeSummary = "+18 Loajalita, +25 Poslušnost, +10 Strach, +10 Vliv"
                    ),
                    ProgressionEncounterChoice(
                        id = "reb_romantic",
                        text = "Přistup k ní s něhou: „Nejsi vězeňkyně bez ceny. Pojď se mnou a já ti dokáži svou přízeň.“",
                        style = "romantic",
                        reactionText = "„Tvá slova... tiší můj hněv. Možná jsem se v tobě mýlila, můj pane.“",
                        affinityGain = 15,
                        loyaltyGain = 20,
                        affectionGain = 15,
                        outcomeSummary = "+15 Náklonnost, +20 Loajalita, +15 Srdce"
                    )
                )
            ),

            // 2. Devoted Confession
            ProgressionEncounter(
                id = "enc_devoted_confession",
                category = EncounterCategory.HAREM_RELATIONSHIP,
                title = "💖 Šepot vnímavého srdce",
                prerequisites = EncounterPrerequisites(
                    minPlayerLevel = 2,
                    minAffinityLevel = 3,
                    minLoyalty = 60,
                    minCharacterCount = 1
                ),
                prologue = "Dívka tě očekává v zahradě pod svitem stříbrného měsíce s pohárem vzácného vína.",
                speakerName = "Oddaná družka",
                speakerIcon = "💖",
                monologue = "„Můj věčný pane... s každým dnem, který strávím v tvé blízkosti, cítím, jak se má duše propojuje s tvou moci. Přijmi tento pohár a mou slibovanou věrnost.“",
                choices = listOf(
                    ProgressionEncounterChoice(
                        id = "dev_romantic",
                        text = "Vypij s ní pohár a stáhni ji do vřelého objetí.",
                        style = "romantic",
                        reactionText = "„Tvé objetí je mým jediným utočištěm! Jsem tvá celým svým srdcem.“",
                        affinityGain = 25,
                        affectionGain = 20,
                        loyaltyGain = 15,
                        resourceRewards = mapOf("sexEnergy" to 20, "gold" to 30),
                        outcomeSummary = "+25 Náklonnost, +20 Srdce, +15 Loajalita, +20⚡ SE, +30💰 Zlato"
                    ),
                    ProgressionEncounterChoice(
                        id = "dev_strategic",
                        text = "Pochval její příkladnou oddanost a svěř jí klíče od zámecké pokladnice.",
                        style = "strategic",
                        reactionText = "„Tato důvěra mě naplňuje hrdostí! Budu střežit tvé bohatství jako vlastní život.“",
                        affinityGain = 20,
                        loyaltyGain = 25,
                        resourceRewards = mapOf("gold" to 80, "influence" to 15),
                        outcomeSummary = "+20 Náklonnost, +25 Loajalita, +80💰 Zlato, +15 Vliv"
                    )
                )
            ),

            // 3. Inquisition Spy
            ProgressionEncounter(
                id = "enc_inquisition_spy",
                category = EncounterCategory.INQUISITION_INTRUSION,
                title = "⚔️ Stín inkvizičního špiona",
                prerequisites = EncounterPrerequisites(
                    minPlayerLevel = 2,
                    minPlayerDay = 3,
                    minDomainExpansionLevel = 1
                ),
                prologue = "Stráže dominia zadržely podezřelého zvěda v plášti se psem a pečetí Svaté inkvizice.",
                speakerName = "Velitel stráže",
                speakerIcon = "⚔️",
                monologue = "„Můj pane, tento muž slídil kolem západní věže. Našli jsme u něj šifrované dopisy určené inkvizičnímu kardinálovi.“",
                choices = listOf(
                    ProgressionEncounterChoice(
                        id = "inq_dominant",
                        text = "Podrob špiona temnému výslechu v katakombách a vytěž informace.",
                        style = "dominant",
                        reactionText = "„Zvěd pod tlakem temné magie odhalil pozice inkvizičních těl i tajné poklady!“",
                        resourceRewards = mapOf("gold" to 120, "darkEnergy" to 25, "influence" to 15),
                        outcomeSummary = "+120💰 Zlato, +25 Temná energie, +15 Vliv"
                    ),
                    ProgressionEncounterChoice(
                        id = "inq_strategic",
                        text = "Přequij ho falešnými zprávami a pošli ho zpět jako dvojitého agenta.",
                        style = "strategic",
                        reactionText = "„Inkvizice byla oklamána! Tvé dominium získalo strategickou výhodu.“",
                        resourceRewards = mapOf("gold" to 60, "influence" to 25, "mana" to 30),
                        outcomeSummary = "+60💰 Zlato, +25 Vliv, +30🔮 Mana"
                    )
                )
            ),

            // 4. Subterranean Vault
            ProgressionEncounter(
                id = "enc_subterranean_vault",
                category = EncounterCategory.SUBTERRANEAN_EXPEDITION,
                title = "🗝️ Objev prastaré chrámu katakomb",
                prerequisites = EncounterPrerequisites(
                    minPlayerLevel = 3,
                    minPlayerDay = 4,
                    minDomainExpansionLevel = 2
                ),
                prologue = "Při rozšiřování hradeb dělníci prorazili stěnu do zapomenutého chrámu Luny.",
                speakerName = "Hlavní stavitel",
                speakerIcon = "⛏️",
                monologue = "„Pane! Za kamennou zdí jsme našli oltář z černého mramoru obklopený truhlami se starobylými krystaly!“",
                choices = listOf(
                    ProgressionEncounterChoice(
                        id = "vault_mystical",
                        text = "Absorbuj energii krystalů rituálem temnoty.",
                        style = "graceful",
                        reactionText = "„Magická energie krystalů proudí tvými žilami! Tvá moc vzrostla.“",
                        resourceRewards = mapOf("darkEnergy" to 40, "mana" to 50, "stone" to 50),
                        outcomeSummary = "+40 Temná energie, +50🔮 Mana, +50🪨 Kámen"
                    ),
                    ProgressionEncounterChoice(
                        id = "vault_economic",
                        text = "Odvezeš starožitnosti na trh a zpepeněžíš je u černého syndikátu.",
                        style = "strategic",
                        reactionText = "„Poklad ze starého chrámu přinesl do pokladnice obrovské jmění!“",
                        resourceRewards = mapOf("gold" to 250, "wood" to 40, "stone" to 40),
                        outcomeSummary = "+250💰 Zlato, +40🪵 Dřevo, +40🪨 Kámen"
                    )
                )
            ),

            // 5. Blood Sister Initiation
            ProgressionEncounter(
                id = "enc_blood_sister_initiation",
                category = EncounterCategory.HAREM_RELATIONSHIP,
                title = "🩸 Rituál Krvavé sestra",
                prerequisites = EncounterPrerequisites(
                    minPlayerLevel = 3,
                    minAffinityLevel = 4,
                    minLoyalty = 75,
                    minCharacterCount = 1
                ),
                prologue = "Dívka s bojovým výrazem přichází do trůuního sálu s dýkou vyrytou runami krevní přísahy.",
                speakerName = "Krvavá válečnice",
                speakerIcon = "🩸",
                monologue = "„Můj vládce... má čepel i má krev jsou připraveny k posvátnému spojení. Dovol mi složit přísahu Krvavé sestry!“",
                choices = listOf(
                    ProgressionEncounterChoice(
                        id = "blood_accept",
                        text = "Proveď krevní řez na dlani a přijmi její přísahu bojovnice.",
                        style = "dominant",
                        reactionText = "„Krev byla prolita! Dívka se stala tvojí neohroženou Krvavou sestrou s bojovým bonusem +15%!“",
                        affinityGain = 30,
                        loyaltyGain = 30,
                        affectionGain = 20,
                        resourceRewards = mapOf("darkEnergy" to 30, "sexEnergy" to 25),
                        outcomeSummary = "+30 Náklonnost, +30 Loajalita, Rituál Krvavé sestry dokončen!"
                    )
                )
            ),

            // 6. Jealousy in Chambers
            ProgressionEncounter(
                id = "enc_jealousy_chambers",
                category = EncounterCategory.HAREM_RELATIONSHIP,
                title = "🔥 Rivalita a žárlivost v komnatách",
                prerequisites = EncounterPrerequisites(
                    minPlayerLevel = 2,
                    minCharacterCount = 2,
                    minAffection = 40
                ),
                prologue = "Při příchodu do komnat zastihneš dvě dívky uprostřed plamenné výměny názorů o tom, komu z nich věnuješ více přízně.",
                speakerName = "Zamilované dívky",
                speakerIcon = "🔥",
                monologue = "„Můj pane! Rozhodni mezi námi! Které z nás patří tvé srdce a komu dnes v noci věnuješ svou královskou pozornost?“",
                choices = listOf(
                    ProgressionEncounterChoice(
                        id = "jeal_both",
                        text = "Obejmi obě současně: „Můj harém nezná dělení. Dnes v noci patříte obě mně.“",
                        style = "passionate",
                        reactionText = "„Obě dívky sčervenaly a s vášní se podrobily tvé společné náruči!“",
                        affinityGain = 20,
                        affectionGain = 20,
                        loyaltyGain = 15,
                        resourceRewards = mapOf("sexEnergy" to 35),
                        outcomeSummary = "+20 Náklonnost pro obě, +20 Srdce, +35⚡ SE"
                    ),
                    ProgressionEncounterChoice(
                        id = "jeal_discipline",
                        text = "Pevně zjednej kázeň: „V tomto sídle vládne řád, nikoliv malicherná žárlivost.“",
                        style = "dominant",
                        reactionText = "„Tvá autorita uklidnila komnaty a upevnila respekt v harému.“",
                        loyaltyGain = 20,
                        obedienceGain = 25,
                        resourceRewards = mapOf("influence" to 15),
                        outcomeSummary = "+20 Loajalita, +25 Poslušnost, +15 Vliv"
                    )
                )
            )
        )
    }

    fun evaluateEligibleEncounters(save: GameSave): List<ProgressionEncounter> {
        val player = save.player
        val characters = save.characters
        val catalog = getCatalog()

        return catalog.filter { enc ->
            val req = enc.prerequisites
            val levelOk = player.level >= req.minPlayerLevel
            val dayOk = player.day >= req.minPlayerDay
            val domainOk = save.domainExpansionLevel >= req.minDomainExpansionLevel
            val countOk = characters.size >= req.minCharacterCount

            var charMatchOk = true
            if (enc.prerequisites.minAffinityLevel > 0 ||
                enc.prerequisites.minLoyalty > 0 ||
                enc.prerequisites.maxLoyalty < 100 ||
                enc.prerequisites.minAffection > 0 ||
                enc.prerequisites.requiredRelStatus != null ||
                enc.prerequisites.requiresWife ||
                enc.prerequisites.requiresPartner ||
                enc.prerequisites.requiredArchetype != null) {

                charMatchOk = characters.any { char ->
                    val rel = char.getRelationship()
                    char.affinityLevel >= req.minAffinityLevel &&
                            char.loajalita >= req.minLoyalty &&
                            char.loajalita <= req.maxLoyalty &&
                            char.srdce >= req.minAffection &&
                            (req.requiredRelStatus == null || rel == req.requiredRelStatus) &&
                            (!req.requiresWife || char.jeManzelkou) &&
                            (!req.requiresPartner || char.partnerka) &&
                            (req.requiredArchetype == null || char.archetypeId == req.requiredArchetype)
                }
            }

            levelOk && dayOk && domainOk && countOk && charMatchOk
        }
    }

    fun selectRandomEncounter(save: GameSave): ProgressionEncounter? {
        val eligible = evaluateEligibleEncounters(save)
        if (eligible.isEmpty()) return null

        val totalWeight = eligible.sumOf { it.weight }
        if (totalWeight <= 0) return eligible.random()

        var randomVal = Random.nextInt(totalWeight)
        for (enc in eligible) {
            randomVal -= enc.weight
            if (randomVal < 0) return enc
        }
        return eligible.last()
    }
}
