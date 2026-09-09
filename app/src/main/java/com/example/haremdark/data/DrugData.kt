package com.example.haremdark.data

import com.example.haremdark.models.InventoryItem

data class DrugDefinition(
    val id: String,
    val name: String,
    val streetName: String,
    val icon: String,
    val category: String, // "Afrodiziakum", "Bojový stimulant", "Narkotikum / Sedativum", "Halucinogen", "Detoxikace"
    val description: String,
    val lore: String,
    val goldCost: Int,
    val darkCost: Int,
    val woodCost: Int, // Byliny a organické esence
    val manaCost: Int,
    val yieldCount: Int = 2,
    val streetPrice: Int,
    val toxicityGain: Int,
    val addictionGain: Int,
    val playerEffectSummary: String,
    val concubineEffectSummary: String,
    val flavorMessagePlayer: String,
    val flavorMessageConcubine: String,
    val requiredIngredients: List<Pair<String, Int>> = emptyList()
)

data class DrugIngredient(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val sourceLocation: String,
    val buyPrice: Int,
    val harvestBatch: Int = 3
)

data class SourcingExpedition(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val energyCost: Int,
    val darkCost: Int,
    val possibleIngredients: List<String>,
    val dangerSummary: String
)

data class HaremRoleDefinition(
    val id: String,
    val title: String,
    val icon: String,
    val description: String,
    val perkSummary: String,
    val recommendedArchetype: String
)

object DrugData {

    val ALL_DRUGS = listOf(
        DrugDefinition(
            id = "drug_cerny_lotos",
            name = "Černý lotos",
            streetName = "Fialový démon",
            icon = "🌺",
            category = "Afrodiziakum",
            description = "Výtažek z půlnočních květů kvetoucích pouze na hrobech démonů. Vyvolává intenzivní euforii a magický žár.",
            lore = "V podsvětí metropole platí Černý lotos za nejvyhledávanější artikl šlechty a čarodějek. Pouhá kapka rozpouští zábrany a spojuje mysl s temnotou.",
            goldCost = 60,
            darkCost = 15,
            woodCost = 20,
            manaCost = 10,
            yieldCount = 2,
            streetPrice = 110,
            toxicityGain = 10,
            addictionGain = 16,
            playerEffectSummary = "+50 Temné energie, +35 Sexuální energie, +10 Toxicita",
            concubineEffectSummary = "+35 Touha, +30 Vlhkost, +20 Loajalita, +16 Závislost, extatický stav",
            flavorMessagePlayer = "Inhaloval jsi kouř z usušených lístků Černého lotosu. Tvým tělem projel horký fialový blesk a temná energie naplnila tvé žíly.",
            flavorMessageConcubine = "ochutnala kapku esence Černého lotosu. Její zorničky se rozšířily, tváře zahořely žárem a s extatickým povzdechem se odevzdala tvé vůli.",
            requiredIngredients = listOf("ing_lotus_leaf" to 2, "ing_shadow_poppy" to 1)
        ),
        DrugDefinition(
            id = "drug_krvavy_prach",
            name = "Krvavý prach",
            streetName = "Rudožár",
            icon = "🩸",
            category = "Bojový stimulant",
            description = "Jemně mletý krystalizovaný prach z démonické krve a síry. Způsobuje záchvat zuřivosti, necitelnost k bolesti a dravost.",
            lore = "Gladiátoři v podzemních arénách si vtírají rudožár do dásní před soubojem na život a na smrt. Zvyšuje reflexy a brutální sílu na úkor rozvahy.",
            goldCost = 75,
            darkCost = 20,
            woodCost = 10,
            manaCost = 15,
            yieldCount = 2,
            streetPrice = 135,
            toxicityGain = 15,
            addictionGain = 20,
            playerEffectSummary = "+25 Útok v boji, +20% Šance na kritický zásah, +20 HP regenerace, +15 Toxicita",
            concubineEffectSummary = "+30 Touha, +25 Krvelačnost, +15 Bojové schopnosti, +20 Závislost",
            flavorMessagePlayer = "Vstříkl jsi do krve dávku Krvavého prachu. Srdce ti začalo bít jako válečný buben a svět zčervenal v bojovém amoku.",
            flavorMessageConcubine = "vdechla špetku Krvavého prachu. Z hrdla se jí vydral dravý povzdech, v očích se zažehla jiskra nespoutané vášně a tělem jí projela touha po pánově doteku.",
            requiredIngredients = listOf("ing_demon_crystal" to 2, "ing_viper_venom" to 1)
        ),
        DrugDefinition(
            id = "drug_stinove_opium",
            name = "Stínové opium",
            streetName = "Půlnoční mlha",
            icon = "💨",
            category = "Narkotikum / Sedativum",
            description = "Těžký, sladce vonící kouř z makovic pěstovaných ve stínových ruinách. Uklidňuje vzdor, maže strach a navozuje hlubokou poddajnost.",
            lore = "Používáno otrokáři a temnými lordy ke zlomení nepoddajných zajatkyň. Mysl se noří do teplého sametu, kde odpor přestává mít smysl.",
            goldCost = 50,
            darkCost = 12,
            woodCost = 25,
            manaCost = 5,
            yieldCount = 3,
            streetPrice = 90,
            toxicityGain = 8,
            addictionGain = 22,
            playerEffectSummary = "+60 HP vyléčení, -15 Stres, uklidnění mysli, +8 Toxicita",
            concubineEffectSummary = "-30 Strach, +25 Poslušnost, +25 Submisivita, potlačení rebelie, +22 Závislost",
            flavorMessagePlayer = "Vykouřil jsi dýmku Stínového opia. Bolest tvých ran odezněla v husté mlze a tvé tělo zaplavil hluboký klid.",
            flavorMessageConcubine = "vdechla hustý kouř Stínového opia. Její napjaté svaly povolily, odpor se rozplynul a její oči se v mrákotách upřely na tebe s bezbřehou poslušností.",
            requiredIngredients = listOf("ing_shadow_poppy" to 2, "ing_mountain_sage" to 1)
        ),
        DrugDefinition(
            id = "drug_krystalicka_extaze",
            name = "Krystalická extáze",
            streetName = "Hvězdný třpyt",
            icon = "💎",
            category = "Afrodiziakum",
            description = "Fialové krystaly třpytící se ve tmě, rozpustné ve víně. Okamžitě probouzí v těle erupci smyslnosti a neovladatelného chtíče.",
            lore = "V nočních čtvrtích a luxusních nevěstincích je Hvězdný třpyt tím nejcennějším artiklem. I ta nejchladnější dáma po něm ztrácí veškeré zábrany.",
            goldCost = 70,
            darkCost = 18,
            woodCost = 15,
            manaCost = 12,
            yieldCount = 2,
            streetPrice = 130,
            toxicityGain = 12,
            addictionGain = 25,
            playerEffectSummary = "+45 Sexuální energie, +10 Dominance, +12 Toxicita",
            concubineEffectSummary = "+45 Touha, +40 Vlhkost, +25 Loajalita, +10 Srdce, +25 Závislost",
            flavorMessagePlayer = "Rozpustil jsi krystal na jazyku. Příval euforie a potěšení ti dodal novou sílu a neuhasitelnou touhu.",
            flavorMessageConcubine = "vypila pohár s rozpuštěným Hvězdným třpytem. Zachvěla se od hlavy až k patě, tělo jí zalil pot rozkoše a tiskne se k tobě v horečném prosíku o tvou blízkost.",
            requiredIngredients = listOf("ing_star_dust" to 2, "ing_lotus_leaf" to 1)
        ),
        DrugDefinition(
            id = "drug_nocni_bes",
            name = "Noční běs",
            streetName = "Zmijí kapka",
            icon = "🐍",
            category = "Bojový stimulant",
            description = "Destilovaný jed stínových zmijí smíchaný s podzemními houbami. Zrychluje reflexy a činí smysly nadpřirozeně ostrými.",
            lore = "Zbraň tichých asasínů a elitních stráží. Umožňuje vnímat pohyb stínů a reagovat dříve, než nepřítel stihne tasit zbraň.",
            goldCost = 65,
            darkCost = 14,
            woodCost = 18,
            manaCost = 10,
            yieldCount = 2,
            streetPrice = 115,
            toxicityGain = 14,
            addictionGain = 15,
            playerEffectSummary = "+15 Obrana, +15 Rychlost a úskoky v boji, +14 Toxicita",
            concubineEffectSummary = "+20 Obrana, +15 Bojová dravost, +10 Odvaha, +15 Závislost",
            flavorMessagePlayer = "Aplikoval jsi kapku Zmijího jedu. Zorničky se ti zúžily do štěrbin a každý zvuk i pohyb v místnosti vnímáš s mrazivou přesností.",
            flavorMessageConcubine = "přijala dávku Nočního běsu. Její smysly se bleskově zostřily a dívá se na tebe s dravou odevzdaností osobní strážkyně.",
            requiredIngredients = listOf("ing_viper_venom" to 2, "ing_demon_crystal" to 1)
        ),
        DrugDefinition(
            id = "drug_esence_zapomneni",
            name = "Esence zapomnění",
            streetName = "Stříbrný závoj",
            icon = "🌫️",
            category = "Halucinogen",
            description = "Tajemný stříbřitý roztok ze slz přízraků a éterického mechu. Maže staré vzpomínky na minulý život a rodinu, nechává jen věrnost pánovi.",
            lore = "Když ani přísné tresty nezlomí hrdost šlechtičny, Stříbrný závoj vymaže minulost jako tabuli. Zůstává jen nová identita v pánově harému.",
            goldCost = 110,
            darkCost = 35,
            woodCost = 30,
            manaCost = 25,
            yieldCount = 1,
            streetPrice = 220,
            toxicityGain = 18,
            addictionGain = 30,
            playerEffectSummary = "+35 Reputace v podsvětí, manipulace myslí, +18 Toxicita",
            concubineEffectSummary = "-40 Strach, +35 Důvěra, +30 Loajalita, smazání rebelského odporu, +30 Závislost",
            flavorMessagePlayer = "Připravil jsi a rituálně posvětil Stříbrný závoj pro přepsání myslí svých poddaných.",
            flavorMessageConcubine = "vypila stříbřitou esenci. Její oči se zamžily, zapomněla na svůj starý život a s úsměvem položeným na tvé hrudi tě oslovila jako svého jediného pána.",
            requiredIngredients = listOf("ing_specter_tear" to 2, "ing_star_dust" to 1, "ing_shadow_poppy" to 1)
        ),
        DrugDefinition(
            id = "drug_cistici_elixir",
            name = "Čistící bylinný detox",
            streetName = "Bílý lék",
            icon = "🌿",
            category = "Detoxikace",
            description = "Silný odvar z horských bylin, pramenité vody a očistných solí. Odbourává toxiny v krvi a zmírňuje abstinenční příznaky.",
            lore = "Nezbytnost každého alchymisty pracujícího s temnými substancemi. Bez Bílého léku hrozí otrava krve a kolaps organismu.",
            goldCost = 35,
            darkCost = 5,
            woodCost = 20,
            manaCost = 5,
            yieldCount = 2,
            streetPrice = 60,
            toxicityGain = -35,
            addictionGain = -30,
            playerEffectSummary = "-40 Toxicita pána, regenerace těla, vyléčení otravy",
            concubineEffectSummary = "-35 Závislost, vyléčení abstinenčního třesu, +15 Důvěra, +20 HP",
            flavorMessagePlayer = "Vypil jsi hořký Čistící elixír. Horkost v krvi opadla a tvé tělo se pročistilo od nahromaděných toxinů.",
            flavorMessageConcubine = "vypila čistící odvar. Zhluboka vydechla, křeče a třes ustoupily a s vděčností v očích ti poděkovala za záchranu.",
            requiredIngredients = listOf("ing_mountain_sage" to 2, "ing_lotus_leaf" to 1)
        )
    )

    val ALL_INGREDIENTS = listOf(
        DrugIngredient(
            id = "ing_lotus_leaf",
            name = "Půlnoční lístky lotosu",
            icon = "🌺",
            description = "Sametově černé okvětní lístky vstřebávající stíny a temnou esenci z hrobů.",
            sourceLocation = "Temný hvozd / Ruiny",
            buyPrice = 28,
            harvestBatch = 3
        ),
        DrugIngredient(
            id = "ing_demon_crystal",
            name = "Krystaly démonické krve",
            icon = "🩸",
            description = "Fosilní krůpěje démonické krve, které pulzují divokým vnitřním žárem.",
            sourceLocation = "Ruiny starého chrámu",
            buyPrice = 36,
            harvestBatch = 2
        ),
        DrugIngredient(
            id = "ing_shadow_poppy",
            name = "Makovice stínového máku",
            icon = "💨",
            description = "Narkotické makovice s černou pryskyřicí mírnící bolest a lámouci vůli.",
            sourceLocation = "Šlechtické panství / Hvozd",
            buyPrice = 24,
            harvestBatch = 3
        ),
        DrugIngredient(
            id = "ing_star_dust",
            name = "Hvězdný éterický prach",
            icon = "✨",
            description = "Jemný fosforeskující prach padající z noční oblohy během zatmění.",
            sourceLocation = "Měsíční přístav / Chrámy",
            buyPrice = 32,
            harvestBatch = 2
        ),
        DrugIngredient(
            id = "ing_viper_venom",
            name = "Váček se zmijím jedem",
            icon = "🐍",
            description = "Destilovaný jed stínových zmijí lovených v podzemních stokách.",
            sourceLocation = "Městské stoky a doupata",
            buyPrice = 30,
            harvestBatch = 2
        ),
        DrugIngredient(
            id = "ing_specter_tear",
            name = "Slzy přízraků",
            icon = "🌫️",
            description = "Kondenzovaná mlha sebraná z náhrobků a znesvěcených krypt.",
            sourceLocation = "Katakomby a staré kobky",
            buyPrice = 48,
            harvestBatch = 1
        ),
        DrugIngredient(
            id = "ing_mountain_sage",
            name = "Horská očistná šalvěj",
            icon = "🌿",
            description = "Aromatická horská bylina pro detoxikaci těla a čištění zkažené krve.",
            sourceLocation = "Pěstírna panství / Hory",
            buyPrice = 18,
            harvestBatch = 4
        )
    )

    val ALL_EXPEDITIONS = listOf(
        SourcingExpedition(
            id = "exp_temny_hvozd",
            name = "Botanická výprava do Temného hvozdu",
            icon = "🌲",
            description = "Hledání půlnočních lotosů a divoce rostoucí léčivé šalvěje v hlubinách lesa.",
            energyCost = 12,
            darkCost = 8,
            possibleIngredients = listOf("ing_lotus_leaf", "ing_mountain_sage", "ing_shadow_poppy"),
            dangerSummary = "Nízké riziko • Vhodné pro noční sběr"
        ),
        SourcingExpedition(
            id = "exp_ruiny_chramu",
            name = "Průzkum krypt Starého chrámu",
            icon = "🏛️",
            description = "Těžba zkrystalizované démonické krve a odchyt ektoplazmatických slz přízraků.",
            energyCost = 16,
            darkCost = 18,
            possibleIngredients = listOf("ing_demon_crystal", "ing_specter_tear", "ing_star_dust"),
            dangerSummary = "Střední riziko • Přítomnost kultistů"
        ),
        SourcingExpedition(
            id = "exp_stoky",
            name = "Lov v městských stokách a podzemí",
            icon = "🐀",
            description = "Kladení pastí na stínové zmije a sběr podzemních toxických plísní.",
            energyCost = 14,
            darkCost = 5,
            possibleIngredients = listOf("ing_viper_venom", "ing_shadow_poppy"),
            dangerSummary = "Nízké riziko • Nepříjemný zápach a krysy"
        ),
        SourcingExpedition(
            id = "exp_pristav",
            name = "Noční zátah v Měsíčním přístavu",
            icon = "⚓",
            description = "Zabavování pašovaných beden hvězdného prachu z exotických zámořských lodí.",
            energyCost = 18,
            darkCost = 12,
            possibleIngredients = listOf("ing_star_dust", "ing_specter_tear", "ing_lotus_leaf"),
            dangerSummary = "Vysoké riziko • Hlídky městské stráže"
        )
    )

    fun getIngredientById(id: String): DrugIngredient? = ALL_INGREDIENTS.firstOrNull { it.id == id }

    fun getExpeditionById(id: String): SourcingExpedition? = ALL_EXPEDITIONS.firstOrNull { it.id == id }

    fun ingredientToInventoryItem(ing: DrugIngredient, count: Int = 1): InventoryItem {
        return InventoryItem(
            id = ing.id,
            name = ing.name,
            description = ing.description,
            count = count,
            price = ing.buyPrice,
            category = "ingredient",
            icon = ing.icon,
            rarity = when {
                ing.buyPrice >= 40 -> "Epický"
                ing.buyPrice >= 28 -> "Vzácný"
                else -> "Běžný"
            },
            effectDescription = "Alchymistická surovina ze zdroje: ${ing.sourceLocation}"
        )
    }

    val ALL_ROLES = listOf(
        HaremRoleDefinition(
            id = "role_strazkyne",
            title = "Strážkyně ložnice",
            icon = "🛡️",
            description = "Osobní tělesná strážkyně střežící pánovu ložnici ve dne v noci.",
            perkSummary = "+15% k obraně pána v boji, chrání před nočními přepadeními",
            recommendedArchetype = "odvazna, krvava_subka, vzdorna"
        ),
        HaremRoleDefinition(
            id = "role_intrikanka",
            title = "Vrchní intrikánka",
            icon = "📜",
            description = "Špionka a manipulátorka shromažďující tajemství šlechty a inkvizice.",
            perkSummary = "-3 vliv inkvizice denně, +10% šance na úspěch v podsvětí",
            recommendedArchetype = "slechticna, manipulativni, chladna"
        ),
        HaremRoleDefinition(
            id = "role_spravkyne_laboratore",
            title = "Správkyně laboratoře",
            icon = "⚗️",
            description = "Mistryně alchymie dohlížející na destilaci drog a lektvarů.",
            perkSummary = "+50% výtěžek při vaření drog a alchymie (více dávek z jedné várky)",
            recommendedArchetype = "touha, posedla, chladna"
        ),
        HaremRoleDefinition(
            id = "role_pani_haremu",
            title = "Paní harému (První dáma)",
            icon = "👑",
            description = "Vrchní konkubína udržující pořádek, disciplínu a harmonii mezi ostatními dívkami.",
            perkSummary = "+15 Harmonie harému denně, brání žárlivosti a sporům",
            recommendedArchetype = "slechticna, subka, oblibena"
        ),
        HaremRoleDefinition(
            id = "role_kuryrka",
            title = "Mafiánská kurýrka",
            icon = "💰",
            description = "Dívka dohlížející na distribuci a pouliční prodej narkotik na černém trhu.",
            perkSummary = "+35% zisk zlata z distribuce drog na ovládaných územích",
            recommendedArchetype = "odvazna, nymfomanka, touha"
        ),
        HaremRoleDefinition(
            id = "role_spolecnice",
            title = "Důvěrná společnice",
            icon = "💖",
            description = "Nejbližší důvěrnice vyhrazená pro soukromé rozmluvy, masáže a noční potěšení.",
            perkSummary = "+15 Sexuální energie každé ráno, rychlejší růst náklonnosti",
            recommendedArchetype = "subka, nymfomanka, ticha_panenka"
        ),
        HaremRoleDefinition(
            id = "role_dozorkyne",
            title = "Dozorkyně kobek",
            icon = "⛓️",
            description = "Přísná dozorkyně dohlížející na vězně, tresty a mučírnu dominia.",
            perkSummary = "+25% efektivita trestů a lámání vůle nepoddajných dívek",
            recommendedArchetype = "krvava_subka, chladna, posedla"
        )
    )

    fun getDrugById(id: String): DrugDefinition? = ALL_DRUGS.firstOrNull { it.id == id }

    fun toInventoryItem(drug: DrugDefinition, count: Int = drug.yieldCount): InventoryItem {
        return InventoryItem(
            id = drug.id,
            name = drug.name,
            description = drug.description,
            count = count,
            price = drug.streetPrice,
            category = "drug",
            icon = drug.icon,
            rarity = when (drug.category) {
                "Detoxikace" -> "Běžný"
                "Narkotikum / Sedativum" -> "Vzácný"
                "Afrodiziakum", "Bojový stimulant" -> "Epický"
                else -> "Legendární"
            },
            effectDescription = drug.playerEffectSummary
        )
    }
}
