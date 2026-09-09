package com.example.haremdark.data

import com.example.haremdark.models.GiftCategory
import com.example.haremdark.models.GiftItemData
import com.example.haremdark.models.GiftRarity
import com.example.haremdark.models.InventoryItem

object GiftInventoryCatalog {

    val ALL_GIFTS = listOf(
        // FLOWERS & PERFUMES
        GiftItemData(
            id = "gift_roses",
            name = "Kytice nočních růží",
            description = "Voňavé temné růže, které vyvolávají příjemné chvění a něhu.",
            icon = "🌹",
            category = GiftCategory.FLOWERS_PERFUMES,
            rarity = GiftRarity.COMMON,
            goldCost = 25,
            baseAffinity = 16,
            loyaltyBonus = 8,
            desireBonus = 6,
            trustBonus = 6,
            obedienceBonus = 4,
            favoriteArchetypes = listOf("subka", "ticha_panenka", "ustrasena", "romanticka"),
            dislikedArchetypes = listOf("krvava_subka"),
            flavorQuote = "přijala kytici nočních růží. Její oči zjihly a jemně přivoněla k okvětním lístkům.",
            archetypeCustomReactions = mapOf(
                "subka" to "Tyto růže jsou překrásné... Cítím se po tvém boku v bezpečí, můj pane.",
                "ticha_panenka" to "...jejich vůně je tak sladká... Děkuji, že na mě myslíš.",
                "krvava_subka" to "Květiny? Raději bych viděla krev našich nepřátel... ale trny mají ostré."
            )
        ),
        GiftItemData(
            id = "gift_perfume",
            name = "Lahvička nočního parfému",
            description = "Omamná esence z půlnočních květů s afrodiziakálním účinkem.",
            icon = "🌸",
            category = GiftCategory.FLOWERS_PERFUMES,
            rarity = GiftRarity.RARE,
            goldCost = 70,
            baseAffinity = 24,
            loyaltyBonus = 12,
            desireBonus = 16,
            trustBonus = 10,
            obedienceBonus = 6,
            favoriteArchetypes = listOf("nymfomanka", "touha", "slechticna", "chladna"),
            flavorQuote = "nanesla pár kapek parfému na krk a zápěstí. Vzduch naplnila sladká, vzrušující vůně.",
            archetypeCustomReactions = mapOf(
                "nymfomanka" to "Mmm, ta vůně mi okamžitě rozpaluje krev... Přistup blíž a přivoň si ke mně...",
                "slechticna" to "Výtečný výběr. Máš vkus hodný vládce tohoto dominia."
            )
        ),

        // JEWELRY
        GiftItemData(
            id = "gift_pendant",
            name = "Rubínový přívěsek",
            description = "Blyštivý drahokam v temně stříbrném lůžku.",
            icon = "💎",
            category = GiftCategory.JEWELRY,
            rarity = GiftRarity.RARE,
            goldCost = 95,
            baseAffinity = 28,
            loyaltyBonus = 16,
            desireBonus = 10,
            trustBonus = 14,
            obedienceBonus = 8,
            favoriteArchetypes = listOf("slechticna", "manipulativni", "chladna"),
            flavorQuote = "se rozzářila radostí, když jsi jí zapnul rubínový přívěsek kolem krku.",
            archetypeCustomReactions = mapOf(
                "slechticna" to "Rubín barvy čerstvého vína... Přesně víš, jak si získat šlechtičnu.",
                "manipulativni" to "Takový třpyt... Snad si nemyslíš, že mě tím snadno ovládneš? Ale líbí se mi."
            )
        ),
        GiftItemData(
            id = "gift_sapphire_earrings",
            name = "Safírové náušnice luny",
            description = "Dvojice jemných safírů kovaných pod nočním měsícem.",
            icon = "✨",
            category = GiftCategory.JEWELRY,
            rarity = GiftRarity.EPIC,
            goldCost = 140,
            baseAffinity = 38,
            loyaltyBonus = 20,
            desireBonus = 14,
            trustBonus = 18,
            obedienceBonus = 10,
            favoriteArchetypes = listOf("chladna", "ticha_panenka", "slechticna"),
            flavorQuote = "si nasadila safírové náušnice, které zvýraznily lesk jejích očí."
        ),

        // APPAREL & SILK
        GiftItemData(
            id = "gift_lingerie",
            name = "Hedvábné prádlo",
            description = "Průsvitné černé hedvábí zvýrazňující křivky těla.",
            icon = "👘",
            category = GiftCategory.APPAREL_SILK,
            rarity = GiftRarity.RARE,
            goldCost = 55,
            baseAffinity = 22,
            loyaltyBonus = 12,
            desireBonus = 18,
            trustBonus = 8,
            obedienceBonus = 8,
            favoriteArchetypes = listOf("nymfomanka", "touha", "subka"),
            flavorQuote = "si oblékla hedvábné prádlo a předvedla své půvaby v tlumeném světle komnat.",
            archetypeCustomReactions = mapOf(
                "touha" to "Je tak hebké na kůži... Líbí se ti, jak v něm vypadám, pane?",
                "subka" to "Když si ho obleču, cítím se celá tvá... Ráda pro tebe budu krásná."
            )
        ),
        GiftItemData(
            id = "gift_velvet_corset",
            name = "Karmínový sametový korzet",
            description = "Pevně šněrovaný korzet z těžkého sametu s krajkovým lemem.",
            icon = "👗",
            category = GiftCategory.APPAREL_SILK,
            rarity = GiftRarity.EPIC,
            goldCost = 120,
            baseAffinity = 34,
            loyaltyBonus = 18,
            desireBonus = 22,
            trustBonus = 12,
            obedienceBonus = 15,
            favoriteArchetypes = listOf("krvava_subka", "odvazna", "slechticna"),
            flavorQuote = "si nechala utáhnout šněrování korzetu. Její dech se zrychlil a postava získala uhrančivý tvar."
        ),

        // SWEETS & WINE
        GiftItemData(
            id = "gift_royal_wine",
            name = "Archivní víno z Černé révy",
            description = "Ročníkový rudý mok se sladce kořeněným dozvukem a hřejivým teplem.",
            icon = "🍷",
            category = GiftCategory.SWEETS_WINE,
            rarity = GiftRarity.COMMON,
            goldCost = 35,
            baseAffinity = 18,
            loyaltyBonus = 10,
            desireBonus = 12,
            trustBonus = 10,
            obedienceBonus = 4,
            favoriteArchetypes = listOf("slechticna", "odvazna", "vzdorna"),
            flavorQuote = "připila s tebou křišťálovým pohárem. Alkohol jí uvolnil tváře a rozproudil krev."
        ),
        GiftItemData(
            id = "gift_chocolates",
            name = "Višně v hořké čokoládě",
            description = "Luxusní pochoutka naplněná višňovým likérem.",
            icon = "🍫",
            category = GiftCategory.SWEETS_WINE,
            rarity = GiftRarity.COMMON,
            goldCost = 20,
            baseAffinity = 14,
            loyaltyBonus = 6,
            desireBonus = 8,
            trustBonus = 8,
            obedienceBonus = 4,
            favoriteArchetypes = listOf("ticha_panenka", "ustrasena", "subka", "touha"),
            flavorQuote = "ochutnala sladkou čokoládu se slastným povzdechem."
        ),

        // POTIONS & ELIXIRS
        GiftItemData(
            id = "elixir_touhy",
            name = "Elixír touhy",
            description = "Okamžitě probouzí v těle nespoutanou vášeň a doplňuje energii.",
            icon = "🔮",
            category = GiftCategory.POTIONS_ELIXIRS,
            rarity = GiftRarity.RARE,
            goldCost = 40,
            baseAffinity = 22,
            loyaltyBonus = 10,
            desireBonus = 25,
            trustBonus = 6,
            obedienceBonus = 8,
            favoriteArchetypes = listOf("nymfomanka", "touha", "posedla"),
            flavorQuote = "vypila elixír až do dna. Její tváře zrudly a v očích se zažehla žhavá jiskra."
        ),
        GiftItemData(
            id = "serum_poslusnost",
            name = "Sérum poslušnosti",
            description = "Koncentrovaná alchymie podlamující vzdor a strach.",
            icon = "💉",
            category = GiftCategory.POTIONS_ELIXIRS,
            rarity = GiftRarity.EPIC,
            goldCost = 90,
            baseAffinity = 30,
            loyaltyBonus = 25,
            desireBonus = 8,
            trustBonus = 8,
            obedienceBonus = 30,
            favoriteArchetypes = listOf("vzdorna", "zlomena", "subka"),
            flavorQuote = "požila sérum poslušnosti. Její odpor byl zlomen a odevzdala se tvé vůli."
        ),

        // RELICS & CURIOS
        GiftItemData(
            id = "gift_lunar_mirror",
            name = "Zrcadlo pravdy a půvabu",
            description = "Starožitné leštěné zrcadlo zdobené stříbrnými ornamenty hadů.",
            icon = "🪞",
            category = GiftCategory.RELICS_CURIOS,
            rarity = GiftRarity.RARE,
            goldCost = 80,
            baseAffinity = 26,
            loyaltyBonus = 14,
            desireBonus = 12,
            trustBonus = 16,
            obedienceBonus = 6,
            favoriteArchetypes = listOf("slechticna", "chladna", "manipulativni"),
            flavorQuote = "pohladila vyřezávaný rám zrcadla a se samolibým úsměvem se v něm prohlédla."
        ),
        GiftItemData(
            id = "gift_ancient_harp",
            name = "Melodická harfa nymf",
            description = "Kouzelný nástroj, jehož struny rozechvívají srdce a tiší hněv.",
            icon = "🪕",
            category = GiftCategory.RELICS_CURIOS,
            rarity = GiftRarity.EPIC,
            goldCost = 130,
            baseAffinity = 36,
            loyaltyBonus = 18,
            desireBonus = 14,
            trustBonus = 24,
            obedienceBonus = 10,
            favoriteArchetypes = listOf("ticha_panenka", "romanticka", "ustrasena"),
            flavorQuote = "přejela prsty po strunách harfy a rozezněla komnatu tklivou harmonií."
        ),

        // SPECIAL BONDS (LEGENDARY & MYTHIC)
        GiftItemData(
            id = "drahy_obojek",
            name = "Zlatý obojek pána",
            description = "Symbol absolutního vlastnictví a věrnosti vyrytý rodovým erbem.",
            icon = "👑",
            category = GiftCategory.SPECIAL_BONDS,
            rarity = GiftRarity.LEGENDARY,
            goldCost = 160,
            baseAffinity = 50,
            loyaltyBonus = 30,
            desireBonus = 20,
            trustBonus = 15,
            obedienceBonus = 30,
            favoriteArchetypes = listOf("subka", "ustrasena", "ticha_panenka", "krvava_subka", "zlomena"),
            flavorQuote = "sklopila hlavu a dovolila ti uzamknout zlatý obojek kolem svého hrdla. Její oddanost je absolutní.",
            archetypeCustomReactions = mapOf(
                "subka" to "Obojek od tebe, můj pane... Navždy budu tvou oddanou služebnicí.",
                "krvava_subka" to "Zlaté okovy... Pro tebe budu krvácet i zabíjet.",
                "vzdorna" to "Ten chlad kovu na krku... Uznávám tě za svého jediného pána."
            )
        ),
        GiftItemData(
            id = "gift_shadow_oath_ring",
            name = "Prsten stínové přísahy",
            description = "Mýtický prsten z černého obsidiánu s pulzujícím karmínovým jádrem.",
            icon = "💍",
            category = GiftCategory.SPECIAL_BONDS,
            rarity = GiftRarity.MYTHIC,
            goldCost = 280,
            baseAffinity = 75,
            loyaltyBonus = 40,
            desireBonus = 30,
            trustBonus = 35,
            obedienceBonus = 25,
            favoriteArchetypes = listOf("slechticna", "odvazna", "krvava_subka", "chladna", "subka"),
            flavorQuote = "přijala prsten stínové přísahy a políbila tvou ruku na znamení věčného svazku duší.",
            archetypeCustomReactions = mapOf(
                "slechticna" to "Stínová přísaha... Naše moc a osudy jsou nyní neoddělitelně spjaty, můj králi.",
                "odvazna" to "Přísahám ti věrnost svou čepelí i srdcem. Nikdo tě přede mnou neohrozí."
            )
        )
    )

    fun getGiftById(id: String): GiftItemData? = ALL_GIFTS.find { it.id == id }

    /**
     * Converts a generic InventoryItem into a GiftItemData if matching or generates default representation.
     */
    fun fromInventoryItem(item: InventoryItem): GiftItemData {
        val matched = getGiftById(item.id)
        if (matched != null) return matched

        val category = when {
            item.category.contains("gift") || item.id.startsWith("gift_") -> GiftCategory.FLOWERS_PERFUMES
            item.category.contains("potion") || item.id.contains("elixir") -> GiftCategory.POTIONS_ELIXIRS
            else -> GiftCategory.RELICS_CURIOS
        }

        return GiftItemData(
            id = item.id,
            name = item.name,
            description = item.description,
            icon = item.icon,
            category = category,
            rarity = when (item.rarity) {
                "Legendární" -> GiftRarity.LEGENDARY
                "Epický" -> GiftRarity.EPIC
                "Vzácný" -> GiftRarity.RARE
                else -> GiftRarity.COMMON
            },
            goldCost = item.price,
            baseAffinity = 15,
            loyaltyBonus = 10,
            desireBonus = 10,
            trustBonus = 8,
            obedienceBonus = 5,
            flavorQuote = "přijala ${item.name} z tvých rukou."
        )
    }
}
