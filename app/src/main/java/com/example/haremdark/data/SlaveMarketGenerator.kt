package com.example.haremdark.data

import com.example.haremdark.R
import com.example.haremdark.models.Element
import java.util.UUID

object SlaveMarketGenerator {

    private val FIRST_NAMES = listOf(
        "Elena", "Aurelia", "Lilith", "Lyra", "Vespera", "Kaelen", "Gwyneth",
        "Serafina", "Silvia", "Nyx", "Morrigan", "Isolda", "Valeria", "Selene",
        "Astraea", "Talia", "Cassandra", "Raven", "Elowen", "Mireia", "Zephyra",
        "Freya", "Ariadne", "Caelia", "Dahlia", "Ember", "Vesper", "Scylla"
    )

    private val TITLES_BY_ARCHETYPE = mapOf(
        "draci_divka" to listOf("Barbarská Drakobijka", "Dračí Nevěsta", "Plamenná Lovkyně", "Pustinná Vládkyně", "Válečnice Žáru"),
        "posedla" to listOf("Padlá Kněžka Temnoty", "Astrální Adeptka", "Obětovaná Panna", "Stínová Šamanka", "Kněžka Propasti"),
        "slechticna" to listOf("Uvězněná Šlechtična", "Zapomenutá Princezna", "Vévodkyně v Okovech", "Aristokratka Exilu", "Císařská Dědička"),
        "sukuba" to listOf("Sukubí Společnice", "Čarodějka Touhy", "Královna Rozkoše", "Noční Pokušitelka", "Vášnivá Svůdkyně"),
        "odvazna" to listOf("Strážkyně Hvozdu", "Divoká Hraničářka", "Stínová Lovkyně", "Smaragdová Válečnice", "Pohanská Zvědka"),
        "ticha_panenka" to listOf("Svatá Panna Řádu", "Zlomená Mučednice", "Posvátná Oběť", "Němá Vězeňkyně", "Klášterní Růže"),
        "vampire_queen" to listOf("Krvavá Hraběnka", "Temná Matriarcha", "Upíří Kněžka", "Nocležnice Smrti", "Smaragdová Krvavka")
    )

    private val PERSONALITY_TEMPERAMENTS = listOf(
        "Hrdá", "Chladná", "Horlivá", "Plašivá", "Nespoutaná", "Vášnivá",
        "Submisivní", "Dominantní", "Lstivá", "Mystická", "Krvavá", "Rozmarná",
        "Srdcatá", "Nedůvěřivá", "Zádumčivá", "Pobožná", "Pohanská", "Věrná",
        "Divoká", "Kalkulující", "Mstivá", "Oddaná"
    )

    private val PERSONALITY_QUIRKS = listOf(
        "Pohrdá Slabostí", "Touží po Moci", "Křehká Duše", "Nezlomná Vůle",
        "Odevzdaná Osudu", "Smlouva v Krvi", "Zapomenutá Dědička", "Miluje Temnotu",
        "Posedlá Ctižádostí", "Hledá Spasitele", "Divoká Vášnivost", "Krvavý Fanatismus",
        "Svaté Odhodlání", "Tajemný Úsměv", "Ocelové Srdce"
    )

    private val PERSONALITY_SPECIAL_TAGS = listOf(
        "Mistr Černé Magie", "Taktická Geniálnost", "Tichý Zabiják", "Bojový Fanatismus",
        "Svaté Požehnání", "Svůdný Půvab", "Dračí Hněv", "Srdce ze Zlata", "Nespoutaný Duch"
    )

    private val ORIGIN_SENTENCES = listOf(
        "Pochází ze zneuctěného rodu vysoké šlechty Severních Držav, kde byla vychována v luxusu i přísné kázni.",
        "Bývala prvotřídní adeptkou v tajném astrálním chrámu Měsíční Propasti, dokud nebyly její svatyně vypáleny.",
        "Vyrůstala v pustinách Krvavého Kaňonu, kde jako divoká lovkyně čelila bestiím a nepřátelským kmenům.",
        "Dcera vyhnaného arcimága, v jejíž krvi koluje prastará runová magie neznámého původu.",
        "Bývalá velitelka paladinů Svatého Řádu Slunce, zrazená vlastními spolubojovníky z žárlivosti.",
        "Nomádská šamanka z Věčných Dun, která po staletí strážila posvátné oázy před nájezdníky.",
        "Sirotek z chudinských uliček podchrámu, ze které se postupně stala nejobávanější nájemná vražedkyně."
    )

    private val TURNING_POINT_SENTENCES = listOf(
        "Při přepadení karavany temnými elfy osobně pobila tucet útočníků, ale po zradě svého pobočníka padla do zajetí.",
        "Když její chrám znesvětila inkvizice, vzala na sebe kletbu předků, aby výkupným zachránila své mladší sestry.",
        "Během palácového převratu odmítla vydat pečeť rodu a sama čelila přesile gardistů, dokud nebyla spoutána magií.",
        "Při lovu na legendárního fenodraka vstoupila do zakázané zóny a byla chycena bezohlednými kupci s otroky.",
        "Po prohraném rituálním duelu o trůn byla uvržena do řetězů a prodána na veřejném aukčním trhu."
    )

    private val MOTIVATION_SENTENCES = listOf(
        "Nyní spoléhá na to, že najde nového pána, jehož síla předčí její temná očekávání a umožní jí dosáhnout kruté pomsty.",
        "Jeho autoritu zpočátku podrobuje chladné skepsi, avšak v hloubi duše touží po mocném ochranném náručí.",
        "Hledá někoho, komu by mohla bezvýhradně obětovat svou magickou moc a získat tak bezpečný domov v dominiu.",
        "Její srdce překypuje potlačovanou vášní a dychtí po pánovi, který ji zkrotí a vštípí jí nový smysl existence.",
        "Je chladně kalkulující a věří, že služba mocnému vládci dominia je její nejlepší šancí, jak se vrátit na vrchol."
    )

    private data class TraitTemplate(
        val name: String,
        val descPattern: String,
        val defaultElement: Element
    )

    private val TRAIT_TEMPLATES = listOf(
        TraitTemplate("Dračí Žár", "+%d%% Těžba kamene & +%d%% Fyzické poškození v boji", Element.FIRE),
        TraitTemplate("Svatokrádežná Aura", "+%d%% Produkce many ve Věži Arcana & +%d Magický útok", Element.DARK),
        TraitTemplate("Císařská Autorita", "+%d%% Zisk zlata v Mincovně & +%d Prestiže Pána", Element.HOLY),
        TraitTemplate("Sukubí Svádění", "+%d%% Generování Sexuální Energie & Šance na Okouzlení", Element.ICE),
        TraitTemplate("Lesní Moudrost", "+%d%% Těžba černého dřeva & +%d Rychlost v boji", Element.EARTH),
        TraitTemplate("Krvavý Pakt", "+%d%% Životní vysávání & +%d Obrana v boji", Element.PHYSICAL),
        TraitTemplate("Astrální Vize", "+%d%% Rychlost kouzlení & +%d Magická obrana", Element.DARK),
        TraitTemplate("Pohanská Ochrana", "+%d%% Odolnost vůči efektům & +%d HP v boji", Element.EARTH)
    )

    private val PORTRAIT_RESOURCES = listOf(
        R.drawable.img_slave_elena_portrait_1790459398016,
        R.drawable.img_slave_aurelia_portrait_1790459410403,
        R.drawable.img_slave_lilith_portrait_1790459422573,
        R.drawable.img_slave_vampire_queen_1790459434350,
        R.drawable.img_skin_valkyrie_golden_dominion_1790460252949
    )

    /**
     * Procedurally generates a new SlaveMarketCandidate with unique personality tags,
     * backstory history, dynamic traits, stats, and balanced recruitment costs.
     */
    fun generateProceduralCandidate(forcedRarity: String? = null): SlaveMarketCandidate {
        val archetypes = TITLES_BY_ARCHETYPE.keys.toList()
        val archetypeId = archetypes.random()

        // 1. Determine Rarity
        val (rarityName, rarityHex, statMult, costMult) = when (forcedRarity ?: rollRarity()) {
            "Mýtická" -> Quad("Mýtická", 0xFFFFD700, 1.9f, 3.2f)
            "Královská" -> Quad("Královská", 0xFFFF7043, 1.5f, 2.0f)
            "Vznešená" -> Quad("Vznešená", 0xFFAB47BC, 1.25f, 1.4f)
            else -> Quad("Běžná", 0xFF66BB6A, 1.0f, 1.0f)
        }

        // 2. Name & Title
        val firstName = FIRST_NAMES.random()
        val titles = TITLES_BY_ARCHETYPE[archetypeId] ?: listOf("Společnice Dominia")
        val title = titles.random()
        val fullName = "$firstName ze Stínových Držav"

        // 3. Personality Tags (Assign 3 unique dynamic personality tags)
        val tags = mutableSetOf<String>()
        tags.add(PERSONALITY_TEMPERAMENTS.random())
        tags.add(PERSONALITY_QUIRKS.random())
        tags.add(PERSONALITY_SPECIAL_TAGS.random())
        val personalityTags = tags.toList()

        // 4. Procedural History & Backstory
        val backstory = "${ORIGIN_SENTENCES.random()} ${TURNING_POINT_SENTENCES.random()} ${MOTIVATION_SENTENCES.random()}"

        // 5. Dynamic Background Trait
        val traitTpl = TRAIT_TEMPLATES.random()
        val param1 = (20..50).random() * statMult.toInt().coerceAtLeast(1)
        val param2 = (10..30).random() * statMult.toInt().coerceAtLeast(1)
        val traitDesc = String.format(traitTpl.descPattern, param1, param2)

        // 6. Base Stats scaled by Rarity
        val initialHp = (100..150).random().times(statMult).toInt()
        val initialMana = (40..100).random().times(statMult).toInt()
        val initialAttack = (18..32).random().times(statMult).toInt()
        val initialDefense = (10..22).random().times(statMult).toInt()
        val initialSpeed = (12..25).random().times(statMult).toInt()

        // 7. Costs calculated from stats and rarity
        val costGold = (250..600).random().times(costMult).toInt()
        val costWood = if ((0..100).random() > 40) (50..180).random().times(costMult).toInt() else 0
        val costStone = if ((0..100).random() > 50) (50..200).random().times(costMult).toInt() else 0
        val costMana = if (initialMana > 70) (40..120).random().times(costMult).toInt() else 0
        val costSexEnergy = if (archetypeId == "sukuba" || rarityName == "Mýtická") (10..25).random() else 0

        val portraitRes = PORTRAIT_RESOURCES.random()

        return SlaveMarketCandidate(
            id = UUID.randomUUID().toString(),
            name = fullName,
            title = title,
            archetypeId = archetypeId,
            rarityName = rarityName,
            rarityColorHex = rarityHex,
            backgroundTraitName = traitTpl.name,
            backgroundTraitDesc = traitDesc,
            backstory = backstory,
            element = traitTpl.defaultElement,
            initialHp = initialHp,
            initialMana = initialMana,
            initialAttack = initialAttack,
            initialDefense = initialDefense,
            initialSpeed = initialSpeed,
            costWood = costWood,
            costStone = costStone,
            costGold = costGold,
            costMana = costMana,
            costSexEnergy = costSexEnergy,
            portraitRes = portraitRes,
            personalityTags = personalityTags,
            isRecruited = false
        )
    }

    private fun rollRarity(): String {
        val roll = (1..100).random()
        return when {
            roll <= 5 -> "Mýtická"
            roll <= 20 -> "Královská"
            roll <= 50 -> "Vznešená"
            else -> "Běžná"
        }
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
