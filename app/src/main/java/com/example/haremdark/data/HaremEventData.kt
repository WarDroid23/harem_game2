package com.example.haremdark.data

import com.example.haremdark.models.Character
import java.util.UUID

enum class HaremEventType(val title: String, val icon: String, val badgeColorHex: Long) {
    CHAMBER_SUMMONS("Pozvání do komnaty", "🕯️", 0xFFBA68C8),
    DESIRE_SURGE("Příval divoké touhy", "🔥", 0xFFFF4081),
    SECRET_CONFESSION("Důvěrné přiznání", "🗝️", 0xFF00BCD4),
    DEVOTION_PLEDGE("Přísaha oddanosti", "💖", 0xFFE040FB),
    FEAR_PLEA("Tichá bázlivá prosba", "⛓️", 0xFF9E9E9E),
    ROYAL_COUNCIL("Královská audience", "👑", 0xFFFFD700)
}

data class DialogueChoice(
    val id: String,
    val text: String,
    val style: String, // "dominant", "romantic", "graceful"
    val reactionText: String,
    val affinityGain: Int = 15,
    val statChanges: Map<String, Int> = emptyMap(),
    val outcomeSummary: String
)

data class SpecialDialogueSequence(
    val prologue: String,
    val characterMonologue: String,
    val options: List<DialogueChoice>
)

data class TimeLimitedHaremEvent(
    val id: String = UUID.randomUUID().toString(),
    val characterId: String,
    val characterName: String,
    val characterArchetype: String,
    val affinityTier: Int,
    val eventType: HaremEventType,
    val title: String,
    val teaserText: String,
    val durationSeconds: Int = 60,
    var remainingSeconds: Int = 60,
    val dialogueSequence: SpecialDialogueSequence,
    val createdAtMillis: Long = System.currentTimeMillis()
)

object HaremEventData {

    fun generateEventForCharacter(character: Character): TimeLimitedHaremEvent {
        val tier = AffinityData.getLevelForPoints(character.affinityPoints)
        val archetype = character.archetypeId

        val (type, title, teaser, sequence) = when (tier) {
            1 -> generateTier1Event(character)
            2 -> generateTier2Event(character)
            3 -> generateTier3Event(character)
            4 -> generateTier4Event(character)
            5 -> generateTier5Event(character)
            else -> generateTier6Event(character)
        }

        return TimeLimitedHaremEvent(
            characterId = character.id,
            characterName = character.name,
            characterArchetype = character.archetypeId,
            affinityTier = tier,
            eventType = type,
            title = title,
            teaserText = teaser,
            durationSeconds = 65,
            remainingSeconds = 65,
            dialogueSequence = sequence
        )
    }

    private fun generateTier1Event(character: Character): EventTemplate {
        return EventTemplate(
            type = HaremEventType.FEAR_PLEA,
            title = "⛓️ Noční chvění v řetězech",
            teaser = "„Pane... prosím, neodcházej ještě. Kolem komnaty obchází stíny a mé tělo se třese bázní.“",
            sequence = SpecialDialogueSequence(
                prologue = "Vstoupil jsi do její skromné komnaty. ${character.name} sedí v koutě lože, s rukama sepjatýma na klíně. Její dech je zrychlený a v jejích očích se zračí směsice strachu a opatrné zvědavosti.",
                characterMonologue = "„Můj pane... slyšela jsem tvé kroky na chodbě. Vím, že jsem zde tvou zajatkyní a můj život visí na tvé vůli. Ale prosím... pověz mi, co se mnou zamýšlíš? Pokud budu plnit každý tvůj rozkaz bez odmluvy, ušetříš mě utrpení?“",
                options = listOf(
                    DialogueChoice(
                        id = "t1_dominant",
                        text = "„Tvůj život i tvé tělo patří mně. Nauč se poslouchat a zjistíš, že má nadvláda je tvou jedinou jistotou.“",
                        style = "dominant",
                        reactionText = "„Ano, pane... skláním před tebou hlavu. Tvůj hlas ve mně probouzí posvátnou hrůzu, ale vím, kde je mé místo.“",
                        affinityGain = 12,
                        statChanges = mapOf("poslusnost" to 15, "submisivita" to 12, "strach" to 5, "darkEnergy" to 5),
                        outcomeSummary = "+12 Náklonnost, +15 Poslušnost, +12 Submisivita, +5 Temná energie"
                    ),
                    DialogueChoice(
                        id = "t1_romantic",
                        text = "„Nejsem netvor bez citu. Když se mi oddáš, staneš se klenotem mého sídla, ne poníženou obětí.“",
                        style = "romantic",
                        reactionText = "„Tvá slova... jsou nečekaně něžná. Snad je pravda, že pod temnou maskou dřímá muž, kterému mohu věřit.“",
                        affinityGain = 20,
                        statChanges = mapOf("duvera" to 15, "loajalita" to 12, "touha" to 10),
                        outcomeSummary = "+20 Náklonnost, +15 Důvěra, +12 Loajalita, +10 Touha"
                    ),
                    DialogueChoice(
                        id = "t1_graceful",
                        text = "Polož ruku na její tvář a podej jí číši kořeněného vína. „Napij se a zahřej. Dnes v noci ti nikdo neublíží.“",
                        style = "graceful",
                        reactionText = "„Děkuji ti, můj vládce... tvé gesto mě dojalo. Led v mém srdci pomalu ustupuje tvému teplu.“",
                        affinityGain = 18,
                        statChanges = mapOf("duvera" to 18, "strach" to -10, "srdce" to 8),
                        outcomeSummary = "+18 Náklonnost, +18 Důvěra, -10 Strach, +8 Srdce"
                    )
                )
            )
        )
    }

    private fun generateTier2Event(character: Character): EventTemplate {
        return EventTemplate(
            type = HaremEventType.CHAMBER_SUMMONS,
            title = "🗝️ Příprava nočního lože",
            teaser = "„Ustelula jsem tvé lůžko, můj pane. Dovol mi sloužit ti osobněji než kdy dřív.“",
            sequence = SpecialDialogueSequence(
                prologue = "Komnata je provoněna jasmínem a santalovým dřevem. ${character.name} v jemném splývavém hedvábí klečí u lůžka s připraveným stříbrným pohárem.",
                characterMonologue = "„Můj pane, naučila jsem se rozpoznávat každý tvůj pohled. Dnes vypadáš unaveně po starostech o panství. Dovol mi, abych ti sňala plášť a namasírovala tvá ramena... Pokud si přeješ, zůstanu s tebou celou noc.“",
                options = listOf(
                    DialogueChoice(
                        id = "t2_romantic",
                        text = "Pohlaď ji po vlasech a stáhni ji k sobě na lůžko: „Tvá péče je tím nejlepším lékem. Zůstaň v mé náruči.“",
                        style = "romantic",
                        reactionText = "„Jsem tak šťastná... tvá blízkost mi rozechvívá srdce. Udělám cokoliv, abys byl dnes spokojený.“",
                        affinityGain = 22,
                        statChanges = mapOf("touha" to 18, "loajalita" to 15, "vlhkost" to 15, "sexEnergy" to 8),
                        outcomeSummary = "+22 Náklonnost, +18 Touha, +15 Loajalita, +8 Sexuální energie"
                    ),
                    DialogueChoice(
                        id = "t2_dominant",
                        text = "„Poslušná služebná. Klekni a ukaž mi, jak dokonale dokážeš plnit mé choutky.“",
                        style = "dominant",
                        reactionText = "„S radostí, můj vládce. Mé tělo existuje jen proto, aby ti přinášelo rozkoš.“",
                        affinityGain = 16,
                        statChanges = mapOf("poslusnost" to 20, "submisivita" to 18, "touha" to 12),
                        outcomeSummary = "+16 Náklonnost, +20 Poslušnost, +18 Submisivita"
                    ),
                    DialogueChoice(
                        id = "t2_graceful",
                        text = "Daruj jí stříbrný prstýnek a pochval její oddanost: „Tvé služby si cením. Tento dar je důkazem tvého postavení.“",
                        style = "graceful",
                        reactionText = "„Takový nádherný šperk... budu ho hrdě nosit jako pečeť tvé přízně!“",
                        affinityGain = 25,
                        statChanges = mapOf("loajalita" to 22, "duvera" to 16, "gold" to -15),
                        outcomeSummary = "+25 Náklonnost, +22 Loajalita, +16 Důvěra"
                    )
                )
            )
        )
    }

    private fun generateTier3Event(character: Character): EventTemplate {
        return EventTemplate(
            type = HaremEventType.SECRET_CONFESSION,
            title = "🗝️ Svěření temného tajemství",
            teaser = "„Nikdy jsem nikomu neřekla, odkud pochází mé vnitřní jizvy... ale tobě věřím, pane.“",
            sequence = SpecialDialogueSequence(
                prologue = "Měsíční svit proniká vysokým oknem do komnaty. ${character.name} stojí u krbu a tiše hledí do plamenů. Když tě spatří, v jejích očích se zaleskne slza úlevy.",
                characterMonologue = "„Můj pane... v mém minulém životě mě zradili ti, kterým jsem nejvíce věřila. Myslela jsem, že mé srdce už navždy zůstane chladným kamenem. Ale ty... tvá síla a tvá blízkost ve mně probudily něco, co jsem považovala za mrtvé. Polož ruku na mou hruď a ciť, jak divoce pro tebe bije.“",
                options = listOf(
                    DialogueChoice(
                        id = "t3_romantic",
                        text = "Přitiskni ji k sobě a polib ji na spánek: „Zde jsi v bezpečí. Nikdo z minulosti se tě už nikdy nedotkne.“",
                        style = "romantic",
                        reactionText = "„V tvém objetí mizí veškerý strach světa... Jsem navždy tvá důvěrnice a milenka.“",
                        affinityGain = 28,
                        statChanges = mapOf("duvera" to 25, "loajalita" to 20, "touha" to 15, "srdce" to 12),
                        outcomeSummary = "+28 Náklonnost, +25 Důvěra, +20 Loajalita, +12 Srdce"
                    ),
                    DialogueChoice(
                        id = "t3_dominant",
                        text = "Uchop ji za bradu a pohlédni jí hluboko do očí: „Tvá minulost shořela v prach. Nyní patříš mně a tvá oddanost je mou zbraní.“",
                        style = "dominant",
                        reactionText = "„Ano, pane! Spálím pro tebe vše, co bylo. Mé tělo i má duše ti slouží bez zaváhání.“",
                        affinityGain = 24,
                        statChanges = mapOf("poslusnost" to 22, "loajalita" to 18, "darkEnergy" to 10),
                        outcomeSummary = "+24 Náklonnost, +22 Poslušnost, +18 Loajalita, +10 Temná energie"
                    ),
                    DialogueChoice(
                        id = "t3_graceful",
                        text = "Odměň její důvěru nočním rituálem uvolnění a rozkoše: „Dnes v noci oslavíme tvé znovuzrození.“",
                        style = "graceful",
                        reactionText = "„Zavedu tě do nejhlubších zákoutí své rozkoše, pane...“",
                        affinityGain = 26,
                        statChanges = mapOf("touha" to 25, "vlhkost" to 20, "sexEnergy" to 15),
                        outcomeSummary = "+26 Náklonnost, +25 Touha, +20 Vlhkost, +15 Sexuální energie"
                    )
                )
            )
        )
    }

    private fun generateTier4Event(character: Character): EventTemplate {
        return EventTemplate(
            type = HaremEventType.DESIRE_SURGE,
            title = "🔥 Plamenná noční vášeň",
            teaser = "„Mé tělo hoří touhou a nemohu spát... Vezmi si mě teď, bez zábran a bez milosti!“",
            sequence = SpecialDialogueSequence(
                prologue = "Jakmile za sebou zavřeš dveře, ${character.name} se ti vrhne kolem krku. Její kůže sálá žárem a její rty divoce hledají tvá ústa.",
                characterMonologue = "„Celý den jsem nemyslela na nic jiného než na tebe! Když se na mě podíváš tím svým dravým pohledem, celá se chvěji nevydržitelnou touhou. Nechci něžná slova ani odpočinek... Vezmi si mě hned teď, ať celý hrad ví, komu patřím!“",
                options = listOf(
                    DialogueChoice(
                        id = "t4_dominant",
                        text = "Přitlač ji ke zdi, pevně sevři její boky a podrob si ji v divokém rytmu: „Tvá touha bude uspokojena přesně tak, jak žádáš.“",
                        style = "dominant",
                        reactionText = "„Ach... ano! Přesně tohle jsem potřebovala! Jsi můj bůh rozkoše, můj neúprosný vládce!“",
                        affinityGain = 30,
                        statChanges = mapOf("touha" to 30, "vlhkost" to 25, "poslusnost" to 20, "sexEnergy" to 20),
                        outcomeSummary = "+30 Náklonnost, +30 Touha, +20 Poslušnost, +20 Sexuální energie"
                    ),
                    DialogueChoice(
                        id = "t4_romantic",
                        text = "Vezmi ji na lůžko a pomalu ochutnávej každý centimetr jejího rozpáleného těla: „Tvá vášeň mě spaluje. Společně propadneme extázi.“",
                        style = "romantic",
                        reactionText = "„Tvé doteky jsou jako oheň i med zároveň... Nikdy mě nepouštěj ze své náruče!“",
                        affinityGain = 32,
                        statChanges = mapOf("touha" to 28, "loajalita" to 25, "srdce" to 15, "duvera" to 15),
                        outcomeSummary = "+32 Náklonnost, +28 Touha, +25 Loajalita, +15 Srdce"
                    ),
                    DialogueChoice(
                        id = "t4_graceful",
                        text = "Dej jí vypít kapku esence Krystalické extáze a spojte své síly v temném magickém orgasmu.",
                        style = "graceful",
                        reactionText = "„Ta vlna magie... vidím hvězdy a cítím tvou esenci v každé buňce svého těla!“",
                        affinityGain = 35,
                        statChanges = mapOf("touha" to 35, "darkEnergy" to 15, "mana" to 10),
                        outcomeSummary = "+35 Náklonnost, +35 Touha, +15 Temná energie, +10 Mana"
                    )
                )
            )
        )
    }

    private fun generateTier5Event(character: Character): EventTemplate {
        return EventTemplate(
            type = HaremEventType.DEVOTION_PLEDGE,
            title = "💖 Pouto krve a spřízněné duše",
            teaser = "„I kdyby se proti tobě spikly armády světa, já budu tvým štítem i tvou dýkou.“",
            sequence = SpecialDialogueSequence(
                prologue = "V jejích komnatách panuje slavnostní a posvátná atmosféra. ${character.name} pokleká před tebou s obnaženým zápěstím a stříbrnou dýkou.",
                characterMonologue = "„Můj věčný pane... naše spojení už dávno přesáhlo hranice pouhého pána a otrokyně. Tvé vítězství je mým vítězstvím, tvá bolest mou bolestí. Přísahám před temnými bohy, že pro tvou slávu položím život a roztrhám každého, kdo by ti chtěl zkřivit vlas na hlavě. Přijmi mou bezvýhradnou krevní přísahu.“",
                options = listOf(
                    DialogueChoice(
                        id = "t5_romantic",
                        text = "Zvedni ji na nohy, polib její zápěstí a obejmi ji: „Tvá duše je mou druhou polovinou. Vládneme tomuto světu společně.“",
                        style = "romantic",
                        reactionText = "„Společně jsme nepřemožitelní. Moje srdce bije jen v rytmu tvého dechu, můj milovaný pane.“",
                        affinityGain = 40,
                        statChanges = mapOf("loajalita" to 35, "duvera" to 30, "touha" to 20, "maxHp" to 15),
                        outcomeSummary = "+40 Náklonnost, +35 Loajalita, +30 Důvěra, +15 Trvalé Max HP"
                    ),
                    DialogueChoice(
                        id = "t5_dominant",
                        text = "Vezmi dýku a symbolicky pečetíte pakt kapkou spojené krve: „Tvůj meč i tvé tělo patří mému impériu. Nikdo nás nerozdělí.“",
                        style = "dominant",
                        reactionText = "„Krev pro krev, duše pro duši! Temnota v mých žilách zpívá tvé slavné jméno!“",
                        affinityGain = 38,
                        statChanges = mapOf("poslusnost" to 30, "loajalita" to 30, "darkEnergy" to 25, "combatDamage" to 10),
                        outcomeSummary = "+38 Náklonnost, +30 Loajalita, +25 Temná energie, +Bojové poškození"
                    ),
                    DialogueChoice(
                        id = "t5_graceful",
                        text = "Daruj jí starobylý artefakt panství a jmenuj ji svou vrchní strážkyní a rádkyní.",
                        style = "graceful",
                        reactionText = "„Tuto poctu budu střežit do posledního dechu. Tvé dominium pod naší vládou neporazitelně rozkvete.“",
                        affinityGain = 45,
                        statChanges = mapOf("loajalita" to 40, "duvera" to 35, "gold" to 50),
                        outcomeSummary = "+45 Náklonnost, +40 Loajalita, +35 Důvěra, +50 Zlato"
                    )
                )
            )
        )
    }

    private fun generateTier6Event(character: Character): EventTemplate {
        return EventTemplate(
            type = HaremEventType.ROYAL_COUNCIL,
            title = "👑 Rituál věčné královny",
            teaser = "„Jsme vládci této noci, můj věčný králi. Celé dominium se sklání před naší mocí.“",
            sequence = SpecialDialogueSequence(
                prologue = "Komnata je proměněna v královský sál. ${character.name} v majestátní temné róbě a se zlatou čelenkou sedí po tvém boku. Z jejích očí vyzařuje absolutní oddanost i majestátní autorita.",
                characterMonologue = "„Můj věčný králi a pane mého života. Dokázali jsme to, o čem se jiným ani nesnilo. Všechny concubines v harému vzhlížejí k mému trůnu a všichni poddaní se třesou před tvou armádou. Dnes v noci zpečetíme naši věčnou dynastii rituálem nadvlády. Rozkaž, a svět lehne popelem pod našima nohama.“",
                options = listOf(
                    DialogueChoice(
                        id = "t6_romantic",
                        text = "Přitiskni ji na trůn a polib ji jako svou věčnou císařovnu: „Má říše je tvou říší a mé tělo tvým chrámem. Zplodíme nesmrtelné dědice.“",
                        style = "romantic",
                        reactionText = "„Ano! Naše krev bude vládnout věkům! V tvém objetí jsem dosáhla absolutního ráje na zemi.“",
                        affinityGain = 50,
                        statChanges = mapOf("loajalita" to 50, "touha" to 40, "plodnost" to 20, "sexEnergy" to 30),
                        outcomeSummary = "+50 Náklonnost, +50 Loajalita, +40 Touha, +20 Plodnost, +30 Sexuální energie"
                    ),
                    DialogueChoice(
                        id = "t6_dominant",
                        text = "„Předveď harému naši absolutní nadvládu. Žádná z dívek nesmí pochybovat o tom, kdo vládne jejich osudům.“",
                        style = "dominant",
                        reactionText = "„S radostí, můj králi. Všechny skloní hlavy do prachu před naší společnou temnou vůlí!“",
                        affinityGain = 48,
                        statChanges = mapOf("poslusnost" to 45, "darkEnergy" to 40, "gold" to 100),
                        outcomeSummary = "+48 Náklonnost, +45 Poslušnost, +40 Temná energie, +100 Zlato"
                    ),
                    DialogueChoice(
                        id = "t6_graceful",
                        text = "Uspořádejte velkolepou noční hostinu spojenou s orgiemi a poctami pro celé panství.",
                        style = "graceful",
                        reactionText = "„Dnešní noc vstoupí do legend našeho dominia jako noc nekonečného triumfu!“",
                        affinityGain = 55,
                        statChanges = mapOf("loajalita" to 45, "duvera" to 45, "srdce" to 25, "darkEnergy" to 30),
                        outcomeSummary = "+55 Náklonnost, +45 Loajalita, +45 Důvěra, +25 Srdce"
                    )
                )
            )
        )
    }

    private data class EventTemplate(
        val type: HaremEventType,
        val title: String,
        val teaser: String,
        val sequence: SpecialDialogueSequence
    )
}
