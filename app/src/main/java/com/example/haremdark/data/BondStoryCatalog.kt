package com.example.haremdark.data

import com.example.haremdark.ui.components.CharacterEmotionType
import kotlinx.serialization.Serializable

@Serializable
data class BondStoryChoice(
    val optionText: String,
    val responseText: String,
    val extraAffinity: Int = 5,
    val emotionTrigger: CharacterEmotionType = CharacterEmotionType.LOVE
)

@Serializable
data class BondStoryPage(
    val pageIndex: Int,
    val speakerName: String,
    val speakerEmotion: CharacterEmotionType = CharacterEmotionType.BLUSH,
    val text: String,
    val choices: List<BondStoryChoice> = emptyList()
)

@Serializable
data class BondStoryEpisode(
    val id: String,
    val archetypeId: String?,          // e.g. "subka", "valkyra", "knezka", "zlodejka", "aristo", or null for generic
    val episodeNumber: Int,            // 1, 2, 3
    val title: String,
    val requiredAffinityLevel: Int,    // 1..5 (5 is Max Affinity)
    val requiredLoyalty: Int = 0,
    val summary: String,
    val icon: String = "📖",
    val goldReward: Int = 150,
    val darkPowerReward: Int = 25,
    val affinityBonusReward: Int = 12,
    val statBonusDescription: String,
    val pages: List<BondStoryPage>
)

object BondStoryCatalog {

    val ALL_STORIES: List<BondStoryEpisode> = listOf(
        // ================= SUBMISIVNÍ DÍVKA (SUBKA) =================
        BondStoryEpisode(
            id = "subka_ep1",
            archetypeId = "subka",
            episodeNumber = 1,
            title = "První Svitání pod Oboatkem",
            requiredAffinityLevel = 2,
            requiredLoyalty = 20,
            summary = "Její tělo se ještě chvěje strachem z minulosti, ale v tvých očích poprvé hledá bezpečí a útočiště.",
            icon = "🌸",
            goldReward = 120,
            darkPowerReward = 15,
            affinityBonusReward = 10,
            statBonusDescription = "+10 Max HP & +5 Loajalita pro dívku",
            pages = listOf(
                BondStoryPage(
                    pageIndex = 0,
                    speakerName = "Vypravěč",
                    speakerEmotion = CharacterEmotionType.SPARKLE,
                    text = "Měsíční světlo prosvítá okenními mřížemi do tvé komnaty. Dívka klečí u tvého křesla, hlavičku má skloněnou a ruce se jí jemně chvějí."
                ),
                BondStoryPage(
                    pageIndex = 1,
                    speakerName = "{NAME}",
                    speakerEmotion = CharacterEmotionType.BLUSH,
                    text = "Můj Pane... dříve jsem znala jen chlad a přísné tresty. Kdykoliv ke mně někdo přistoupil, očekávala jsem bolest. Ale když se mě dotkneš ty..."
                ),
                BondStoryPage(
                    pageIndex = 2,
                    speakerName = "Ty (Pán)",
                    speakerEmotion = CharacterEmotionType.LOVE,
                    text = "Pohladíš ji po tváři a usměješ se na ni. Cítíš, jak se napětí v jejím těle pomalu rozpouští.",
                    choices = listOf(
                        BondStoryChoice(
                            optionText = "Jsi má a u mě ti nikdo nepřivodí zbytečnou bolest.",
                            responseText = "Dívka ti vtiskne jemný polibek na dlaň a tváře jí vzplanou horkým červenáním.",
                            extraAffinity = 8,
                            emotionTrigger = CharacterEmotionType.BLUSH
                        ),
                        BondStoryChoice(
                            optionText = "Tvá poslušnost je mou největší radostí.",
                            responseText = "Její oči září hlubokým odevzdáním a tichým štěstím.",
                            extraAffinity = 6,
                            emotionTrigger = CharacterEmotionType.CHEER
                        )
                    )
                ),
                BondStoryPage(
                    pageIndex = 3,
                    speakerName = "{NAME}",
                    speakerEmotion = CharacterEmotionType.LOVE,
                    text = "Slibuji, že ti odevzdám celé své srdce i duši. Stanu se tvojí nejoddanější služebnicí..."
                )
            )
        ),
        BondStoryEpisode(
            id = "subka_ep2",
            archetypeId = "subka",
            episodeNumber = 2,
            title = "Tajemství Stříbrného Medailonu",
            requiredAffinityLevel = 4,
            requiredLoyalty = 50,
            summary = "Dívka ti svěří jediný předmět, který jí zbyhl po rodině, na znamení absolutní důvěry.",
            icon = "💍",
            goldReward = 250,
            darkPowerReward = 35,
            affinityBonusReward = 18,
            statBonusDescription = "+15 Důvěra & +10 Vlhkost",
            pages = listOf(
                BondStoryPage(
                    pageIndex = 0,
                    speakerName = "{NAME}",
                    speakerEmotion = CharacterEmotionType.SHY,
                    text = "Pane... tento medailon jsem schovávala pod šaty celá léta. Byl to jediný odkaz mé matky, než mě odvedli otrokáři."
                ),
                BondStoryPage(
                    pageIndex = 1,
                    speakerName = "Ty (Pán)",
                    speakerEmotion = CharacterEmotionType.SPARKLE,
                    text = "Rozepne stříbrný řetízek a vkládá ti starobylý šperk do dlaně. Ještě je teplý od jejího těla."
                ),
                BondStoryPage(
                    pageIndex = 2,
                    speakerName = "{NAME}",
                    speakerEmotion = CharacterEmotionType.BLUSH,
                    text = "Už nepotřebuji žádné vzpomínky na minulost. Můj život a můj osud začaly až v den, kdy jsem vstoupila do tvého harému.",
                    choices = listOf(
                        BondStoryChoice(
                            optionText = "Schovám ho u sebe jako symbol tvé neotřesitelné věrnosti.",
                            responseText = "Dívka radostně vypískne a obejme tvá kolena s očima plnýma slz dojjetí.",
                            extraAffinity = 10,
                            emotionTrigger = CharacterEmotionType.CHEER
                        ),
                        BondStoryChoice(
                            optionText = "Vrátím ti ho – nos ho dál jako důkaz mé přízně.",
                            responseText = "Její tvář zčervená studem i dojetím. Políbí tě na rty s planoucí vášní.",
                            extraAffinity = 12,
                            emotionTrigger = CharacterEmotionType.LOVE
                        )
                    )
                )
            )
        ),
        BondStoryEpisode(
            id = "subka_ep3",
            archetypeId = "subka",
            episodeNumber = 3,
            title = "Nekonečné Pouto Oddanosti (Max Affinity)",
            requiredAffinityLevel = 5,
            requiredLoyalty = 80,
            summary = "Dosáhl jsi absolutní spřízněnosti. Dívka ti přísahá věčné pouto duší, které nepřebije ani smrt.",
            icon = "💖",
            goldReward = 500,
            darkPowerReward = 80,
            affinityBonusReward = 25,
            statBonusDescription = "Absolutní Pouto: +25% k příjmu zlata & Trvalá Aura Vášeň",
            pages = listOf(
                BondStoryPage(
                    pageIndex = 0,
                    speakerName = "Vypravěč",
                    speakerEmotion = CharacterEmotionType.LOVE,
                    text = "Noc je tichá, ale mezi vámi koluje neviditelná energie neotřesitelné lásky a nadvlády."
                ),
                BondStoryPage(
                    pageIndex = 1,
                    speakerName = "{NAME}",
                    speakerEmotion = CharacterEmotionType.LOVE,
                    text = "Ty nejsi jen můj Pán... jsi můj smysl existence, můj vzduch i můj jediný Bůh. Bez tebe je svět jen prázdnotou."
                ),
                BondStoryPage(
                    pageIndex = 2,
                    speakerName = "{NAME}",
                    speakerEmotion = CharacterEmotionType.BLUSH,
                    text = "Vezmi si mou vůli, mé tělo i mou duši. Budu ti sloužit dnes, zítra i navěky!",
                    choices = listOf(
                        BondStoryChoice(
                            optionText = "Jsi má věčná královna mého srdce v temnotě.",
                            responseText = "Dívka vás divoce políbí, zatímco nad jejím portrétem planou zářivá srdce věčného pouta!",
                            extraAffinity = 20,
                            emotionTrigger = CharacterEmotionType.LOVE
                        )
                    )
                )
            )
        ),

        // ================= HRDÁ BOJOVNICE (VALKYRA) =================
        BondStoryEpisode(
            id = "valkyra_ep1",
            archetypeId = "valkyra",
            episodeNumber = 1,
            title = "Jizva z Bitevního Pole",
            requiredAffinityLevel = 2,
            requiredLoyalty = 25,
            summary = "Hrdá bojovnice ti ukáže jizvu na svém rameni a poprvé ti přizná svou zranitelnost.",
            icon = "⚔️",
            goldReward = 180,
            darkPowerReward = 25,
            affinityBonusReward = 12,
            statBonusDescription = "+15 Síla & +10 Bojové Dovednosti",
            pages = listOf(
                BondStoryPage(
                    pageIndex = 0,
                    speakerName = "{NAME}",
                    speakerEmotion = CharacterEmotionType.SPARKLE,
                    text = "Vidíš tuto jizvu u mého klíčku? Získala jsem ji v bitvě u Krvavého Průsmyku. Tehdy jsem si myslela, že zemřu jako osamělá lvice."
                ),
                BondStoryPage(
                    pageIndex = 1,
                    speakerName = "Ty (Pán)",
                    speakerEmotion = CharacterEmotionType.LOVE,
                    text = "Jemně přejedeš prstem po staré jizvě. Bojovnice lehce znejistí a na okamžik ztratí svůj přísný výraz."
                ),
                BondStoryPage(
                    pageIndex = 2,
                    speakerName = "{NAME}",
                    speakerEmotion = CharacterEmotionType.BLUSH,
                    text = "Nikomu jinému bych nedovolila se jí dotknout... Ale tvé ruce mají sílu krále i jemnost ochráníce.",
                    choices = listOf(
                        BondStoryChoice(
                            optionText = "Budeš bojovat po mém boku, ne jako otrok, ale jako má nejobávanější bojovnice.",
                            responseText = "Bojovnice se hrdě usměje, oči jí planou nadšením a válečným jásotem!",
                            extraAffinity = 10,
                            emotionTrigger = CharacterEmotionType.CHEER
                        )
                    )
                )
            )
        ),
        BondStoryEpisode(
            id = "valkyra_ep3",
            archetypeId = "valkyra",
            episodeNumber = 3,
            title = "Královna Bitevního Harému (Max Affinity)",
            requiredAffinityLevel = 5,
            requiredLoyalty = 85,
            summary = "Nezlomná bojovnice před tebou poklekne v plné zbroji a nabídne ti svůj meč i své váchnivé srdce.",
            icon = "👑",
            goldReward = 550,
            darkPowerReward = 100,
            affinityBonusReward = 30,
            statBonusDescription = "Krvavá Královna: +30 Síla & Bitevní Aura v Soubojích",
            pages = listOf(
                BondStoryPage(
                    pageIndex = 0,
                    speakerName = "{NAME}",
                    speakerEmotion = CharacterEmotionType.CHEER,
                    text = "Přísahala jsem, že se nikdy nikomu nepodrobím. Ale ty jsi mě porazil nejen sílou, ale i svou velikostí duše!"
                ),
                BondStoryPage(
                    pageIndex = 1,
                    speakerName = "{NAME}",
                    speakerEmotion = CharacterEmotionType.LOVE,
                    text = "Můj meč je tvým mečem. Můj život patří tobě! Veď mě do jakékoliv bitvy!",
                    choices = listOf(
                        BondStoryChoice(
                            optionText = "Společně dobudeme celý svět!",
                            responseText = "Bojovnice zvolá vítězný pokřik a vášeň naplní celou komnatu!",
                            extraAffinity = 25,
                            emotionTrigger = CharacterEmotionType.CHEER
                        )
                    )
                )
            )
        ),

        // ================= TEMNÁ KNĚŽKA (KNEZKA) =================
        BondStoryEpisode(
            id = "knezka_ep1",
            archetypeId = "knezka",
            episodeNumber = 1,
            title = "Šepot v Chrámu Stínů",
            requiredAffinityLevel = 2,
            requiredLoyalty = 20,
            summary = "Temná kněžka ti odhalí tajná zakletí svatyně a poprvé pocítí hříšnou touhu.",
            icon = "🔮",
            goldReward = 160,
            darkPowerReward = 40,
            affinityBonusReward = 12,
            statBonusDescription = "+20 Temná Moc & +10 Magický Odpor",
            pages = listOf(
                BondStoryPage(
                    pageIndex = 0,
                    speakerName = "{NAME}",
                    speakerEmotion = CharacterEmotionType.SPARKLE,
                    text = "Temné rituály vyžadují čistou mysl... avšak od chvíle, co jsem s tebou, se mé modlitby mění v myšlenky na tvé doteky."
                ),
                BondStoryPage(
                    pageIndex = 1,
                    speakerName = "{NAME}",
                    speakerEmotion = CharacterEmotionType.SHY,
                    text = "Je to hřích vůči mým starým bohům... ale ty jsi temnější i sladší než všichni božští pánové dohromady.",
                    choices = listOf(
                        BondStoryChoice(
                            optionText = "Staň se kněžkou mého vlastního kultu.",
                            responseText = "Kněžka se potichu usměje a oči se jí rozhoří fialovým mystickým svitem.",
                            extraAffinity = 10,
                            emotionTrigger = CharacterEmotionType.SPARKLE
                        )
                    )
                )
            )
        ),

        // ================= GENERAL / UNIVERSAL EPISODE =================
        BondStoryEpisode(
            id = "general_ep_max",
            archetypeId = null,
            episodeNumber = 3,
            title = "Noc Nejsladšího Triumfu (Univerzální Vzpomínka)",
            requiredAffinityLevel = 5,
            requiredLoyalty = 75,
            summary = "Sváteční noc oslavy vaší absolutní blízkosti a neotřesitelného spojenectví.",
            icon = "🌟",
            goldReward = 400,
            darkPowerReward = 60,
            affinityBonusReward = 20,
            statBonusDescription = "+200 Zlata & Trvalý Bonus Všech Atributů +5",
            pages = listOf(
                BondStoryPage(
                    pageIndex = 0,
                    speakerName = "{NAME}",
                    speakerEmotion = CharacterEmotionType.LOVE,
                    text = "Pohled na tebe mi dává sílu překonat jakoukoliv překážku. Děkuji ti za všechno, co jsi pro mě udělal..."
                ),
                BondStoryPage(
                    pageIndex = 1,
                    speakerName = "{NAME}",
                    speakerEmotion = CharacterEmotionType.CHEER,
                    text = "Naše příběhy teprve začínají, můj drahý Pánové!",
                    choices = listOf(
                        BondStoryChoice(
                            optionText = "Připijme na naši věčnou nadvládu a lásku!",
                            responseText = "Portrét zazáří třpytivým jásotem a romantickou auru!",
                            extraAffinity = 15,
                            emotionTrigger = CharacterEmotionType.CHEER
                        )
                    )
                )
            )
        )
    )

    fun getAvailableStoriesForCharacter(
        character: com.example.haremdark.models.Character,
        affinityLevel: Int
    ): List<BondStoryEpisode> {
        return ALL_STORIES.filter { story ->
            (story.archetypeId == null || story.archetypeId.equals(character.archetypeId, ignoreCase = true)) &&
            story.requiredAffinityLevel <= affinityLevel &&
            character.loajalita >= story.requiredLoyalty
        }
    }

    fun getAllStoriesForCharacter(character: com.example.haremdark.models.Character): List<BondStoryEpisode> {
        return ALL_STORIES.filter { story ->
            story.archetypeId == null || story.archetypeId.equals(character.archetypeId, ignoreCase = true)
        }
    }
}
