package com.example.haremdark.data

import com.example.haremdark.R
import com.example.haremdark.models.CharacterReward
import com.example.haremdark.models.DomainLocation

object DomainData {

    val DOMAINS = listOf(
        DomainLocation(
            id = "temny_hvozd",
            name = "Temný hvozd",
            title = "Mlžné lesy stínů",
            region = "Východní pohraničí dominia",
            difficulty = "Snadná",
            difficultyStars = 1,
            minPlayerLevel = 1,
            travelCostEnergy = 5,
            travelCostGold = 0,
            description = "Husté a neproniknutelné hvozdy zahalené věčnou mlhou. Na lesních stezkách se pohybují zbloudilé lovkyně, poutnice a dezertérky hledající útočiště.",
            potentialArchetypes = listOf("subka", "ustrasena", "ticha_panenka"),
            potentialRewards = listOf(
                CharacterReward("subka", "Submisivní dívka", "Běžná", 45, "Rychle si zvyká na vedení pána, +20% zisk loajality."),
                CharacterReward("ustrasena", "Ustrašená bylinkářka", "Běžná", 35, "Vděčná za ochranu před monstry, vysoká poslušnost."),
                CharacterReward("ticha_panenka", "Tichá lesní panna", "Vzácná", 20, "Téměř nemluví, ale její tělo je dokonale tvárné.")
            ),
            resourceDrops = listOf("Léčivé byliny", "Temné dřevo", "Zlato (20-50)", "Hojivý balzám"),
            bossId = "bandita_ze_stok",
            bannerDrawableRes = R.drawable.img_dark_banner,
            accentColor = 0xFF4CAF50,
            mapX = 0.22f,
            mapY = 0.28f
        ),
        DomainLocation(
            id = "ruiny_chramu",
            name = "Ruiny starého chrámu",
            title = "Znesvěcená svatyně bohyně noci",
            region = "Severní posvátné hory",
            difficulty = "Střední",
            difficultyStars = 2,
            minPlayerLevel = 2,
            travelCostEnergy = 10,
            travelCostGold = 25,
            description = "Zřícené mramorové sloupy a krypty, kde kněžky kdysi uctívaly zakázané kulty rozkoše a temné magie. Vzduch je nasycen lákavou magickou energií.",
            potentialArchetypes = listOf("touha", "posedla", "hysterialni"),
            potentialRewards = listOf(
                CharacterReward("touha", "Toužící čarodějka", "Vzácná", 40, "Její tělo sálá magií, +25% regenerace temné energie."),
                CharacterReward("posedla", "Posedlá kultistka", "Vzácná", 35, "Rituálně poznamenaná, okamžitě podléhá hypnóze."),
                CharacterReward("hysterialni", "Vášnivá vědma", "Běžná", 25, "Divoká a nepředvídatelná, prudce reaguje na tresty i slast.")
            ),
            resourceDrops = listOf("Temná esence (+15)", "Elixír touhy", "Krystaly noci", "Zlato (40-80)"),
            bossId = "kralovna_stinu",
            bannerDrawableRes = R.drawable.img_harem_boudoir,
            accentColor = 0xFF9C27B0,
            mapX = 0.68f,
            mapY = 0.22f
        ),
        DomainLocation(
            id = "stoky_doupata",
            name = "Městské podsvětí & Stoky",
            title = "Labyrint zločinu a doupat",
            region = "Podzemí hlavního města",
            difficulty = "Střední",
            difficultyStars = 2,
            minPlayerLevel = 3,
            travelCostEnergy = 12,
            travelCostGold = 40,
            description = "Špinavé podzemní chodby, tajná kasina a cechovní doupata. Zde se ukrývají drsné bojovnice, kapsářky a dívky se zlomenou minulostí.",
            potentialArchetypes = listOf("odvazna", "vzdorna", "krvava_subka", "zlomena"),
            potentialRewards = listOf(
                CharacterReward("odvazna", "Bojovná gladiátorka", "Vzácná", 35, "Štít a meč, v boji chrání svého pána vlastním tělem."),
                CharacterReward("vzdorna", "Vzpurná zlodějka", "Běžná", 30, "Vyžaduje přísnou ruku, ale její zkrocení přináší obrovskou slast."),
                CharacterReward("krvava_subka", "Krvavá akrobatka", "Epická", 20, "Miluje bolest a rány bičem, +30% závislost na bolesti."),
                CharacterReward("zlomena", "Zlomená otrokyně", "Běžná", 15, "Ztracená vůle, naprostá odevzdanost bez odporu.")
            ),
            resourceDrops = listOf("Kradené klenoty", "Otrávené dýky", "Sérum poslušnosti", "Zlato (60-120)"),
            bossId = "otrokarska_hlidka",
            bannerDrawableRes = R.drawable.hero_dark_dominion,
            accentColor = 0xFFFF9800,
            mapX = 0.45f,
            mapY = 0.52f
        ),
        DomainLocation(
            id = "mesicni_pristav",
            name = "Měsíční přístav",
            title = "Doky pašeráků a exotických lodí",
            region = "Západní pobřeží moře sirén",
            difficulty = "Těžká",
            difficultyStars = 3,
            minPlayerLevel = 4,
            travelCostEnergy = 15,
            travelCostGold = 65,
            description = "Rušný noční přístav s tavernami a zámořskými galeonami. Pašeráci zde vykládají cizokrajné zajatkyně, luxusní hedvábí a opojné lektvary z dalekého orientu.",
            potentialArchetypes = listOf("nymfomanka", "manipulativni", "chladna"),
            potentialRewards = listOf(
                CharacterReward("nymfomanka", "Exotická kurtizána", "Epická", 40, "Nenasytná tělesná touha, zvyšuje sexuální energii harému."),
                CharacterReward("manipulativni", "Zrádná pašeráčka", "Vzácná", 35, "Vynikající intrikánka schopná spravovat mafiánská teritoria."),
                CharacterReward("chladna", "Zámořská šlechtična", "Vzácná", 25, "Pyšná a nepřístupná, její podlehnutí je symbolem moci.")
            ),
            resourceDrops = listOf("Mořské černé perly", "Orientální hedvábí", "Zlaté mince (80-160)", "Parfémy"),
            bossId = "kapitan_zeleznich_flotily",
            bannerDrawableRes = R.drawable.img_dark_banner,
            accentColor = 0xFF00BCD4,
            mapX = 0.18f,
            mapY = 0.72f
        ),
        DomainLocation(
            id = "slechticke_panstvi",
            name = "Šlechtické panství",
            title = "Zlaté sály a komnaty aristokracie",
            region = "Královský distrikt",
            difficulty = "Smrtící",
            difficultyStars = 4,
            minPlayerLevel = 5,
            travelCostEnergy = 20,
            travelCostGold = 100,
            description = "Mramorové paláce pyšných šlechtických rodů chráněné inkviziční gardou. Dobytí tohoto sídla umožní zotročit urozené dámy a princezny zvrhnutých dynastií.",
            potentialArchetypes = listOf("slechticna", "chladna", "manipulativni"),
            potentialRewards = listOf(
                CharacterReward("slechticna", "Zlomená princezna", "Legendární", 50, "Bývalá korunní dědička, +50% prestiž dominia a obrovský příjem."),
                CharacterReward("chladna", "Vysoká komtesa", "Epická", 30, "Její chladná hrdost se po nasazení obojku mění v nehynoucí oddanost."),
                CharacterReward("manipulativni", "Vévodkyně intrik", "Vzácná", 20, "Dozírá na ostatní otrokyně a zvyšuje bezpečnost pevnosti.")
            ),
            resourceDrops = listOf("Diamantové prsteny", "Královské zlato (150-300)", "Prestižní listiny", "Rubínové šperky"),
            bossId = "inkvizitor_cerne_peceti",
            bannerDrawableRes = R.drawable.img_harem_boudoir,
            accentColor = 0xFFFFD700,
            mapX = 0.76f,
            mapY = 0.65f
        ),
        DomainLocation(
            id = "propast_behemoth",
            name = "Trhlina v propasti",
            title = "Pekelná výheň prastarých démonů",
            region = "Podzemní zlom temnoty",
            difficulty = "Královská",
            difficultyStars = 5,
            minPlayerLevel = 6,
            travelCostEnergy = 25,
            travelCostGold = 150,
            description = "Trhlina mezi světy plná plamenů a síry, kde sídlí démonické bytosti a nejtemnější entity. Jen skutečný pán temnot se může odvážit vkročit a podrobit si pekelné stvůry.",
            potentialArchetypes = listOf("krvava_subka", "posedla", "nymfomanka", "touha"),
            potentialRewards = listOf(
                CharacterReward("krvava_subka", "Démonická succuba", "Legendární", 35, "Vysává životní sílu nepřátel a odevzdává ji pánovi."),
                CharacterReward("posedla", "Avatar temné bohyně", "Legendární", 35, "Ovládá nepředstavitelné temné rituály."),
                CharacterReward("touha", "Plamenná čarodějka", "Epická", 30, "Neuhasitelný oheň vášně s maximální plodností.")
            ),
            resourceDrops = listOf("Démonické srdce", "Krev Behemotha", "Legendární zlato (300-600)", "Černá pečeť moci"),
            bossId = "arcidemon_behemoth",
            bannerDrawableRes = R.drawable.img_dark_banner,
            accentColor = 0xFFE91E63,
            mapX = 0.50f,
            mapY = 0.88f
        ),
        DomainLocation(
            id = "hostinec_u_krvave_panny",
            name = "Hostinec U Krvavé Panny",
            title = "Místo setkání lůzy a potěšení",
            region = "Křižovatka obchodních cest",
            difficulty = "Snadná",
            difficultyStars = 1,
            minPlayerLevel = 1,
            travelCostEnergy = 5,
            travelCostGold = 10,
            description = "Známý hostinec, kde se schází pochybné existence i zámožní kupci. Místní prsaté barmanky občas hledají víc než jen zlaťáky, a tajemná obchodnice tu nabízí vzácné zboží.",
            potentialArchetypes = listOf("subka", "nymfomanka", "odvazna"),
            potentialRewards = listOf(
                CharacterReward("subka", "Poslušná služebná", "Běžná", 45, "Zvyklá sloužit hostům, rychle si osvojí tvé příkazy."),
                CharacterReward("nymfomanka", "Nenasytná barmanka", "Vzácná", 35, "Vnadná kráska, jejíž chtíč nezná mezí. Rychle generuje energii.")
            ),
            resourceDrops = listOf("Kvalitní víno", "Informace z podsvětí", "Zlato (10-30)", "Hojivý balzám"),
            bossId = "vyberci_dani",
            bannerDrawableRes = R.drawable.img_tavern_sexy,
            accentColor = 0xFFD84315,
            mapX = 0.35f,
            mapY = 0.35f,
            npcTrader = true,
            npcName = "Krvavá Mary"
        ),
        DomainLocation(
            id = "tabor_zoldnerek",
            name = "Tábor Černých Růží",
            title = "Divoké žoldnéřky a bojovnice",
            region = "Kamenná pustina",
            difficulty = "Těžká",
            difficultyStars = 3,
            minPlayerLevel = 4,
            travelCostEnergy = 15,
            travelCostGold = 45,
            description = "Tábor nemilosrdných žoldnéřek, které neuznávají žádného pána. Jen ten, kdo je porazí v boji, si může nárokovat jejich těla i oddanost.",
            potentialArchetypes = listOf("odvazna", "vzdorna", "chladna"),
            potentialRewards = listOf(
                CharacterReward("odvazna", "Zjizvená veteránka", "Epická", 30, "Zkušená bojovnice. Její podrobení z ní dělá nejvěrnějšího bodyguarda."),
                CharacterReward("vzdorna", "Mladá rekrutka", "Vzácná", 40, "Divoká a nezkrotná. Zlomení její vůle je extrémně vzrušující.")
            ),
            resourceDrops = listOf("Zbraně a zbroj", "Kořist z nájezdů", "Zlato (50-100)", "Sérum poslušnosti"),
            bossId = "velitelka_cernych_ruzi",
            bannerDrawableRes = R.drawable.img_mercenary_camp,
            accentColor = 0xFF424242,
            mapX = 0.85f,
            mapY = 0.45f
        )
    )

    fun getDomainById(id: String): DomainLocation {
        return DOMAINS.firstOrNull { it.id == id } ?: DOMAINS.first()
    }
}
