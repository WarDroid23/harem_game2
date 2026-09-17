package com.example.haremdark.data

import com.example.haremdark.R
import com.example.haremdark.models.CharacterReward
import com.example.haremdark.models.DomainLocation
import com.example.haremdark.models.PointOfInterestType
import com.example.haremdark.models.RegionPointOfInterest

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
            storyChapter = 1,
            requiredMilestoneId = "milestone_chapter_1",
            requiredMilestoneTitle = "Kapitola I: Stíny Hvozdu a Založení",
            subjugationRequirement = "Výpravy a založení pevnosti",
            environmentWeather = "Věčná mlha a šepot větví",
            environmentBonus = "+15% zisk bylin a submisivních dívek",
            description = "Husté a neproniknutelné hvozdy zahalené věčnou mlhou. Na lesních stezkách se pohybují zbloudilé lovkyně, poutnice a dezertérky hledající útočiště.",
            loreChronicle = "Starobylý les z dob před pádem Starého království. Stromy zde pohlcují sluneční svit a půda je prosáklá pradávnou magií. Ti, kdo zde zabloudí, často podlehnou omamnému vábení temných sil.",
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
            mapY = 0.28f,
            pointsOfInterest = listOf(
                RegionPointOfInterest(
                    id = "poi_hvozd_1",
                    domainId = "temny_hvozd",
                    name = "Mýtina nočních víl",
                    type = PointOfInterestType.SECRET_SANCTUARY,
                    description = "Skrytá lesní tůň obklopená světélkujícími květy. Dívky zde nacházejí klid a probouzejí svou smyslnost.",
                    relativeX = 0.3f,
                    relativeY = 0.4f,
                    interactionLabel = "Navštívit mýtinu",
                    rewardSummary = "+15 Touha a zotavení energie"
                ),
                RegionPointOfInterest(
                    id = "poi_hvozd_2",
                    domainId = "temny_hvozd",
                    name = "Prastarý dubový oltář",
                    type = PointOfInterestType.ALTAR,
                    description = "Kamenný menhir obtočený kořeny. Rituální oběť zde posiluje temnou auru pána.",
                    relativeX = 0.7f,
                    relativeY = 0.6f,
                    interactionLabel = "Provést rituál",
                    rewardSummary = "+20 Temná energie, +30 Zkušeností"
                )
            )
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
            storyChapter = 1,
            requiredMilestoneId = "milestone_chapter_1",
            requiredMilestoneTitle = "Kapitola I: Stíny Hvozdu a Založení",
            subjugationRequirement = "Získat alespoň 1 dívku do harému",
            environmentWeather = "Teplo krbu a kouř z opiových dýmek",
            environmentBonus = "+20% levnější nákupy a drby",
            description = "Známý hostinec, kde se schází pochybné existence i zámožní kupci. Místní prsaté barmanky občas hledají víc než jen zlaťáky, a tajemná obchodnice tu nabízí vzácné zboží.",
            loreChronicle = "Křižovatka všech obchodních karavan a pašeráckých stezek. Šenkýřka Mary ví o každém tajemství v království a za správnou cenu ráda přihraje cenné informace i unesené krasavice.",
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
            mapY = 0.38f,
            npcTrader = true,
            npcName = "Krvavá Mary",
            pointsOfInterest = listOf(
                RegionPointOfInterest(
                    id = "poi_hostinec_1",
                    domainId = "hostinec_u_krvave_panny",
                    name = "Salonek tajemné obchodnice",
                    type = PointOfInterestType.TRADER,
                    description = "Soukromý budoár Krvavé Mary plný hedvábí, afrodisiak a luxusních šperků.",
                    relativeX = 0.5f,
                    relativeY = 0.3f,
                    interactionLabel = "Obchodovat s Mary",
                    rewardSummary = "Exkluzivní afrodiziaka a dárky"
                )
            )
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
            storyChapter = 2,
            requiredMilestoneId = "milestone_chapter_2",
            requiredMilestoneTitle = "Kapitola II: Pád podsvětí a chrámové kulty",
            subjugationRequirement = "Splnit úkol: Založení harémového dominia",
            environmentWeather = "Hvězdný svit a fialová magická záře",
            environmentBonus = "+25% regenerace temné magie",
            description = "Zřícené mramorové sloupy a krypty, kde kněžky kdysi uctívaly zakázané kulty rozkoše a temné magie. Vzduch je nasycen lákavou magickou energií.",
            loreChronicle = "Kdysi nejposvátnější chrám Lunární bohyně. Po inkvizičním pogromu zůstaly jen rozvaliny, kde se ukrývají kněžky hledající nového boha, kterému by zasvětily svá těla.",
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
            mapY = 0.22f,
            pointsOfInterest = listOf(
                RegionPointOfInterest(
                    id = "poi_chram_1",
                    domainId = "ruiny_chramu",
                    name = "Lunární rituální bazén",
                    type = PointOfInterestType.SECRET_SANCTUARY,
                    description = "Bazén naplněný posvátnou vodou odrážející fialový měsíc. Dívky koupající se zde získávají mystické kouzlo.",
                    relativeX = 0.4f,
                    relativeY = 0.5f,
                    interactionLabel = "Posvětit bazén",
                    rewardSummary = "+35 Mana, +20 Důvěra a Krása"
                ),
                RegionPointOfInterest(
                    id = "poi_chram_2",
                    domainId = "ruiny_chramu",
                    name = "Krypta stínové kněžky",
                    type = PointOfInterestType.RUINS,
                    description = "Zapečetěná hrobka s prastarými magickými svitky a relikviemi zakázané rozkoše.",
                    relativeX = 0.8f,
                    relativeY = 0.3f,
                    interactionLabel = "Otevřít hrobku",
                    rewardSummary = "+Vzácný magický svitek a temné krystaly"
                )
            )
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
            storyChapter = 2,
            requiredMilestoneId = "milestone_chapter_2",
            requiredMilestoneTitle = "Kapitola II: Pád podsvětí a chrámové kulty",
            subjugationRequirement = "Dosáhnout úrovně 3 a ovládnout území",
            environmentWeather = "Vlhké šero, stékající voda a krysy",
            environmentBonus = "+20% zisk kořisti ze zlodějek",
            description = "Špinavé podzemní chodby, tajná kasina a cechovní doupata. Zde se ukrývají drsné bojovnice, kapsářky a dívky se zlomenou minulostí.",
            loreChronicle = "Pod mramorem hlavního města teče řeka intrik a špíny. Cech zlodějů a pašeráků zde vládne železnou pěstí, dokud do jejich temnoty nevstoupí mocnější pán.",
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
            mapY = 0.52f,
            pointsOfInterest = listOf(
                RegionPointOfInterest(
                    id = "poi_stoky_1",
                    domainId = "stoky_doupata",
                    name = "Tajné herní doupě 'Černá kočka'",
                    type = PointOfInterestType.TRADER,
                    description = "Hazardní doupě, kde zchudlí šlechtici prohrávají své konkubíny v kostkách.",
                    relativeX = 0.3f,
                    relativeY = 0.6f,
                    interactionLabel = "Vsadit do hry",
                    rewardSummary = "+Zlato a šance na zisk dívky"
                )
            )
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
            storyChapter = 3,
            requiredMilestoneId = "milestone_chapter_3",
            requiredMilestoneTitle = "Kapitola III: Krvavé moře a pakt žoldnéřek",
            subjugationRequirement = "Porazit Banditu ze stok a dosáhnout úrovně 4",
            environmentWeather = "Slaný vítr, příboj a záře majáku",
            environmentBonus = "+15% hodnota obchodu a exotických darů",
            description = "Rušný noční přístav s tavernami a zámořskými galeonami. Pašeráci zde vykládají cizokrajné zajatkyně, luxusní hedvábí a opojné lektvary z dalekého orientu.",
            loreChronicle = "Pobřeží lemované vracivými vlnami a zvuky přístavních krčem. Pod rouškou tmy sem připlouvají lodě z cizích kontinentů přivážející zakázané otrokyně a vzácné parfémy.",
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
            mapY = 0.72f,
            pointsOfInterest = listOf(
                RegionPointOfInterest(
                    id = "poi_pristav_1",
                    domainId = "mesicni_pristav",
                    name = "Zátoka sirén",
                    type = PointOfInterestType.SECRET_SANCTUARY,
                    description = "Útesy, kde za odlivu zpívají podmanivé sirény. Jejich hlas podmaňuje mysl a probouzí nenasytnou vášeň.",
                    relativeX = 0.2f,
                    relativeY = 0.7f,
                    interactionLabel = "Naslouchat zpěvu",
                    rewardSummary = "+40 Sexuální energie, +Exotická mušle"
                )
            )
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
            storyChapter = 3,
            requiredMilestoneId = "milestone_chapter_3",
            requiredMilestoneTitle = "Kapitola III: Krvavé moře a pakt žoldnéřek",
            subjugationRequirement = "Splnit úkol: Vliv v podsvětí",
            environmentWeather = "Suchý prach, řinčení čepelí a válečné ohně",
            environmentBonus = "+20% bojová zdatnost a zisk zbraní",
            description = "Tábor nemilosrdných žoldnéřek, které neuznávají žádného pána. Jen ten, kdo je porazí v boji, si může nárokovat jejich těla i oddanost.",
            loreChronicle = "Klan nezávislých válečnic oděných v těžké plátové zbroji a kůži. Bojují za zlato i čest, ale jejich velitelka v skrytu duše touží po vládci, který by ji zkrotil.",
            potentialArchetypes = listOf("odvazna", "vzdorna", "chladna"),
            potentialRewards = listOf(
                CharacterReward("odvazna", "Zjizvená veteránka", "Epická", 30, "Zkušená bojovnice. Její podrobení z ní dělá nejvěrnějšího bodyguarda."),
                CharacterReward("vzdorna", "Mladá rekrutka", "Vzácná", 40, "Divoká a nezkrotná. Zlomení její vůle je extrémně vzrušující.")
            ),
            resourceDrops = listOf("Zbraně a zbroj", "Kořist z nájezdů", "Zlato (50-100)", "Sérum poslušnosti"),
            bossId = "velitelka_cernych_ruzi",
            bannerDrawableRes = R.drawable.img_mercenary_camp,
            accentColor = 0xFF78909C,
            mapX = 0.84f,
            mapY = 0.44f,
            pointsOfInterest = listOf(
                RegionPointOfInterest(
                    id = "poi_zoldnerky_1",
                    domainId = "tabor_zoldnerek",
                    name = "Válečná aréna klanu",
                    type = PointOfInterestType.MONSTER_LAIR,
                    description = "Kruh z naostřených kůlů pro souboje na život a na smrt. Vítěz si odnáší respekt a poražené otrokyně.",
                    relativeX = 0.6f,
                    relativeY = 0.5f,
                    interactionLabel = "Vyzvat šampionku",
                    rewardSummary = "+Zkušenosti, Válečná trofej, Bojovnice"
                )
            )
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
            storyChapter = 4,
            requiredMilestoneId = "milestone_chapter_4",
            requiredMilestoneTitle = "Kapitola IV: Rozvrat Inkvizice a pád aristokracie",
            subjugationRequirement = "Porazit Otrokářskou hlídku a úroveň 5",
            environmentWeather = "Pozlacené lustry a chladný mramor",
            environmentBonus = "+50% prestiž a příjem z urozených dívek",
            description = "Mramorové paláce pyšných šlechtických rodů chráněné inkviziční gardou. Dobytí tohoto sídla umožní zotročit urozené dámy a princezny zvrhnutých dynastií.",
            loreChronicle = "Sídlo zkažené šlechty, která léta terorizovala poddané. Za vysokými zdmi se odehrávají opulentní plesy plné zrady, přetvářky a skrytého zhýralství.",
            potentialArchetypes = listOf("slechticna", "chladna", "manipulativni"),
            potentialRewards = listOf(
                CharacterReward("slechticna", "Zlomená princezna", "Legendární", 50, "Bývalá korunní dědička, +50% prestiž dominia a obrovský příjem."),
                CharacterReward("chladna", "Vysoká komtesa", "Epická", 30, "Její chladná hrdost se po nasazení obojku mění v nehynoucí oddanost."),
                CharacterReward("manipulativni", "Vévodkyně intrik", "Vzácná", 20, "Dozírá na ostatní otrokyně a zvyšuje bezpečnost pevnosti.")
            ),
            resourceDrops = listOf("Diamantové prsteny", "Královské zlato (150-300)", "Prestižní listiny", "Rubínové šperky"),
            bossId = "inkvizitor_cerne_peceti",
            bannerDrawableRes = R.drawable.portrait_noble,
            accentColor = 0xFFFFD700,
            mapX = 0.74f,
            mapY = 0.66f,
            pointsOfInterest = listOf(
                RegionPointOfInterest(
                    id = "poi_panstvi_1",
                    domainId = "slechticke_panstvi",
                    name = "Zrcadlový salón princezny",
                    type = PointOfInterestType.SECRET_SANCTUARY,
                    description = "Nádherná komnata vyložená benátskými zrcadly a polstrovaná hedvábím, kde urozené dámy skládají slib poddanství.",
                    relativeX = 0.5f,
                    relativeY = 0.4f,
                    interactionLabel = "Obsadit salón",
                    rewardSummary = "+Královské šperky, +25 Prestiž"
                )
            )
        ),
        DomainLocation(
            id = "krvave_katakomby",
            name = "Krvavé katakomby vampírů",
            title = "Krypty prastarých krvesajných paní",
            region = "Podzemí zapomenutého opatství",
            difficulty = "Smrtící",
            difficultyStars = 4,
            minPlayerLevel = 5,
            travelCostEnergy = 20,
            travelCostGold = 110,
            storyChapter = 4,
            requiredMilestoneId = "milestone_chapter_4",
            requiredMilestoneTitle = "Kapitola IV: Rozvrat Inkvizice a pád aristokracie",
            subjugationRequirement = "Splnit úkol: Zlomení urozené krve",
            environmentWeather = "Karmínová mlha, chlad krypt a vůně krve",
            environmentBonus = "+30% posílení schopnosti Krvavé pouto",
            description = "Hluboké labyrinty plné kamenných sarkofágů a rudých fontán. Vampírské hraběnky zde spí v tisíciletém spánku a čekají na pána s dostatečnou silou.",
            loreChronicle = "Místo opředené děsem. Tyto katakomby byly vytesány v éře první krve. Vampírské šlechtičny zde uchovávají receptury na elixíry nesmrtelnosti a nekonečné touhy.",
            potentialArchetypes = listOf("krvava_subka", "chladna", "posedla"),
            potentialRewards = listOf(
                CharacterReward("krvava_subka", "Vampírská hraběnka", "Legendární", 40, "Karmínová krev zvyšuje útok v aréně o 35%."),
                CharacterReward("chladna", "Kněžka krve", "Epická", 35, "Vládne zakázané nekromancii a zotavení životní síly.")
            ),
            resourceDrops = listOf("Karmínový rubín", "Lahvička vampírské krve", "Zlato (120-250)", "Černý samet"),
            bossId = "kralovna_stinu",
            bannerDrawableRes = R.drawable.img_dark_banner,
            accentColor = 0xFFD50000,
            mapX = 0.62f,
            mapY = 0.82f,
            pointsOfInterest = listOf(
                RegionPointOfInterest(
                    id = "poi_katakomby_1",
                    domainId = "krvave_katakomby",
                    name = "Krvavá fontána věčného mládí",
                    type = PointOfInterestType.BLOOD_FOUNTAIN,
                    description = "Fontána z černého obsidiánu chrlící posvěcenou krev bohů. Obnovuje zdraví a prohlubuje tělesnou posedlost.",
                    relativeX = 0.5f,
                    relativeY = 0.5f,
                    interactionLabel = "Napít se z pramene",
                    rewardSummary = "+Plné uzdravení HP, +25 Touha harému"
                )
            )
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
            storyChapter = 5,
            requiredMilestoneId = "milestone_chapter_5",
            requiredMilestoneTitle = "Kapitola V: Plameny propasti a Astrální citadela",
            subjugationRequirement = "Porazit Inkvizitora Černé pečeti a dosáhnout úrovně 6",
            environmentWeather = "Soptící láva, síra a pekelný žár",
            environmentBonus = "+40% zisk démonických esencí a zlata",
            description = "Trhlina mezi světy plná plamenů a síry, kde sídlí démonické bytosti a nejtemnější entity. Jen skutečný pán temnot se může odvážit vkročit a podrobit si pekelné stvůry.",
            loreChronicle = "Trhlina vznikla při roztržení magické pečetě před tisíci lety. Z plamenů povstávají divoké succuby a ohnivé čarodějky lačnící po pánovi, který zkrotí jejich pekelný oheň.",
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
            mapX = 0.40f,
            mapY = 0.88f,
            pointsOfInterest = listOf(
                RegionPointOfInterest(
                    id = "poi_propast_1",
                    domainId = "propast_behemoth",
                    name = "Pekelný oltář succub",
                    type = PointOfInterestType.ALTAR,
                    description = "Žhnoucí oltář z pekelného kamene, kde succuby skládají přísahu věčné poslušnosti.",
                    relativeX = 0.5f,
                    relativeY = 0.4f,
                    interactionLabel = "Ovládnout oltář",
                    rewardSummary = "+50 Temná magie, +Succuba artefakt"
                )
            )
        ),
        DomainLocation(
            id = "astralni_citadela",
            name = "Astrální citadela kouzelnic",
            title = "Vznášející se věž nad mraky",
            region = "Nebeská astrální sféra",
            difficulty = "Královská",
            difficultyStars = 5,
            minPlayerLevel = 6,
            travelCostEnergy = 25,
            travelCostGold = 180,
            storyChapter = 5,
            requiredMilestoneId = "milestone_chapter_5",
            requiredMilestoneTitle = "Kapitola V: Plameny propasti a Astrální citadela",
            subjugationRequirement = "Splnit úkol: Úplatek inkvizičního tribunálu",
            environmentWeather = "Polární záře, astrální víry a křišťálový prach",
            environmentBonus = "+50% zisk many a alchymistických receptů",
            description = "Levitující pevnost z bílého křemene a hvězdného skla. Nejvyšší kruh kouzelnic a astroložek zde manipuluje s osudem království.",
            loreChronicle = "Tato citadela se vznáší v beztížném prostoru nad temným světem. Kouzelnice zde staletí žily v izolaci, dokud tvá rostoucí moc neprolomila jejich magické bariéry.",
            potentialArchetypes = listOf("touha", "chladna", "manipulativni"),
            potentialRewards = listOf(
                CharacterReward("touha", "Vysoká astroložka", "Legendární", 45, "Čte osud z hvězd, +50% úspěšnost všech výprav."),
                CharacterReward("chladna", "Arcimágyně času", "Legendární", 35, "Zpomaluje čas a zdvojnásobuje regeneraci energie.")
            ),
            resourceDrops = listOf("Hvězdný prach", "Křišťál času", "Astrální esence", "Zlato (250-500)"),
            bossId = "kralovna_stinu",
            bannerDrawableRes = R.drawable.portrait_sorceress,
            accentColor = 0xFF3F51B5,
            mapX = 0.88f,
            mapY = 0.20f,
            pointsOfInterest = listOf(
                RegionPointOfInterest(
                    id = "poi_citadela_1",
                    domainId = "astralni_citadela",
                    name = "Observatoř osudu",
                    type = PointOfInterestType.WATCHTOWER,
                    description = "Obří dalekohled z křišťálu zaměřený na souhvězdí rozkoše a války.",
                    relativeX = 0.5f,
                    relativeY = 0.3f,
                    interactionLabel = "Pohlédnout do hvězd",
                    rewardSummary = "+Hvězdná mapa, +40 Zkušeností"
                )
            )
        ),
        DomainLocation(
            id = "zakazane_udoli_nymf",
            name = "Zakázané údolí nymf",
            title = "Posvátný Eden věčného potěšení",
            region = "Mýtické srdce světa",
            difficulty = "Královská",
            difficultyStars = 5,
            minPlayerLevel = 7,
            travelCostEnergy = 30,
            travelCostGold = 250,
            storyChapter = 6,
            requiredMilestoneId = "milestone_chapter_6",
            requiredMilestoneTitle = "Kapitola VI: Zakázané údolí věčné rozkoše",
            subjugationRequirement = "Porazit Arcidémona Behemotha a úroveň 7",
            environmentWeather = "Nehasnoucí jarní slunce, vůně orchidejí a zpěv",
            environmentBonus = "Absolutní harmonie harému a maximální plodnost",
            description = "Bájné údolí ukryté za neprostupnými pohořími. Zde žijí nesmrtelné nymfy a bohyně tělesné krásy, které se odevzdávají jen jedinému opravdovému císaři.",
            loreChronicle = "Konečný cíl každého vládce temnoty. V tomto údolí čas neplyne a každá dívka zde dosahuje absolutní oddanosti a věčné mladosti.",
            potentialArchetypes = listOf("nymfomanka", "touha", "slechticna", "subka"),
            potentialRewards = listOf(
                CharacterReward("nymfomanka", "Bohyně rozkoše", "Legendární", 50, "Maximální touha a nekonečná plodnost pro královský rod."),
                CharacterReward("slechticna", "Císařovna nymf", "Legendární", 40, "Nejvyšší majestát, trvale zvyšuje veškeré zisky dominia o 50%.")
            ),
            resourceDrops = listOf("Nektar nesmrtelnosti", "Zlaté jablko touhy", "Císařské zlato (500-1000)", "Božská perla"),
            bossId = null,
            bannerDrawableRes = R.drawable.harem_bg_day,
            accentColor = 0xFFFF4081,
            mapX = 0.50f,
            mapY = 0.08f,
            pointsOfInterest = listOf(
                RegionPointOfInterest(
                    id = "poi_udoli_1",
                    domainId = "zakazane_udoli_nymf",
                    name = "Pramen věčného mládí",
                    type = PointOfInterestType.SECRET_SANCTUARY,
                    description = "Posvátné vřídlo nymf obnovující veškeré síly těla i duše a pečetící věčnou lásku.",
                    relativeX = 0.5f,
                    relativeY = 0.5f,
                    interactionLabel = "Vstoupit do posvátného pramene",
                    rewardSummary = "+100% Harmonie, +Věčné pouto se všemi dívkami"
                )
            )
        )
    )

    // Map connections representing trade lanes, mountain passes, and magical ley lines
    val MAP_CONNECTIONS = listOf(
        Pair("temny_hvozd", "hostinec_u_krvave_panny"),
        Pair("hostinec_u_krvave_panny", "stoky_doupata"),
        Pair("temny_hvozd", "ruiny_chramu"),
        Pair("ruiny_chramu", "astralni_citadela"),
        Pair("ruiny_chramu", "zakazane_udoli_nymf"),
        Pair("stoky_doupata", "mesicni_pristav"),
        Pair("stoky_doupata", "tabor_zoldnerek"),
        Pair("stoky_doupata", "slechticke_panstvi"),
        Pair("slechticke_panstvi", "krvave_katakomby"),
        Pair("krvave_katakomby", "propast_behemoth"),
        Pair("propast_behemoth", "zakazane_udoli_nymf")
    )

    fun getDomainById(id: String): DomainLocation {
        return DOMAINS.firstOrNull { it.id == id } ?: DOMAINS.first()
    }
}
