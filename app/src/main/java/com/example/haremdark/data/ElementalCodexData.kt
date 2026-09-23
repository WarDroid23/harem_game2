package com.example.haremdark.data

import androidx.compose.ui.graphics.Color
import com.example.haremdark.models.Character
import com.example.haremdark.models.Element
import com.example.haremdark.models.ElementalCodexEntry
import com.example.haremdark.models.GameSave

/**
 * Repository containing lore, historical origins, and discovery logic
 * for all elemental affinities in the Dark Dominium world.
 */
object ElementalCodexData {

    val ENTRIES: List<ElementalCodexEntry> = listOf(
        ElementalCodexEntry(
            element = Element.FIRE,
            id = "element_fire",
            name = "Oheň",
            epithet = "Prvotní Plamen Popela",
            icon = "🔥",
            primaryColor = Color(0xFFFF5722),
            secondaryColor = Color(0xFFFFB300),
            historicalOrigin = """
                Prvotní oheň byl zažehnut ještě v První Éře, kdy zemské jádro prorazil Sluneční fénix Ignis. Podle starých svitků z Knihovny popela nebyl oheň pouhou chemickou reakcí, ale horkou krví padlých hvězd. Když nastalo Věčné Zatmění a Slunce zemřelo, povrchový oheň vyhasl – avšak jeho esence se stáhla hluboko do podzemních žil a magmatických proudů pod ruinami měst. 

                Válečníci Prvního Dominia se naučili vyvolávat tento spící plamen skrze spalující vášeň a hněv. Oheň netoleruje nerozhodnost; jakýkoli náznak pochybnosti popálí svého vlastního vyvolávače.
            """.trimIndent(),
            trainingLore = """
                Při elementárním tréninku ohně v komnatách afinity je hrdinka vystavena pulzujícímu žáru lávových kamenů a rituálnímu dýmu. Trénink učí postavu pumpovat vroucí krev do svalových vláken a zažehnout vnitřní plamen v okamžiku kontaktu se zbraní. Každý nárůst násobiče (+0.05x) rozšiřuje spalující zónu kolem jejích úderů a činí její plamenné útoky neúprosnějšími.
            """.trimIndent(),
            tacticalNature = "Způsobuje zničující status 'Spálení' (Burn), který s každým kolem pohlcuje procento protivníkova zdraví a taví ochranné krunýře. Má drtivou převahu proti zemským a ledovým formám, avšak jeho sílu dusí hlubinné vodní bariéry.",
            ancientArchon = "Ignis, Věčný Fénix Popela",
            sacredRelic = "Rubínové Srdce Sopky",
            statusEffectName = "🔥 Spálení (Burn)",
            resonanceBlessing = "+15% šance na kritický úžeh a prorážení zbroje"
        ),

        ElementalCodexEntry(
            element = Element.WATER,
            id = "element_water",
            name = "Voda",
            epithet = "Bezedný Proud Zapomnění",
            icon = "💧",
            primaryColor = Color(0xFF0288D1),
            secondaryColor = Color(0xFF26C6DA),
            historicalOrigin = """
                Historie vodní afinity sahá k potopení Města perel v dobách před velkou potopou. Bohyně Nereida, plačící nad arogancí smrtelníků, zaplavila staré kontinenty přílivem z temných propastí. Po příchodu Věčné noci se povrchová moře zkalila černým slizem, avšak hlubinné prameny si uchovaly svou fluidní paměť.

                Voda v tomto světě představuje jak zdroj nezbytného života a očisty, tak nelítostný tekutý jed. Dokáže uhasit největší peklo, prorazit granit i nejtlustší ocelové brány a rozpustit tkáně protivníků v bezhlesém víru.
            """.trimIndent(),
            trainingLore = """
                Během sparringu s vodním živlem se hrdinka noří do rituálních bazénů pod zámkem Dominia. Učí se neklást odpor přicházejícím úderům, ale obtéct je jako horská bystřina a pohltit kinetickou energii oponenta. Zvýšení násobiče afinity posiluje cirkulaci tekutin a přeměňuje pot i krev na chladivý štít obohacený paralyzujícími neurotoxiny.
            """.trimIndent(),
            tacticalNature = "Spouští nebezpečný status 'Otrava' (Poison) a dodává trvalou regeneraci many spojencům. Voda bez milosti uhasí plameny a oslabuje ohnivé nepřátele, avšak blesková kouzla jí způsobují devastující zkraty.",
            ancientArchon = "Nereida, Matka Bezedných Hlubin",
            sacredRelic = "Slza Půlnočního Oceánu",
            statusEffectName = "💧 Otrava (Poison)",
            resonanceBlessing = "+20% obnova many a odolnost proti přehřátí"
        ),

        ElementalCodexEntry(
            element = Element.EARTH,
            id = "element_earth",
            name = "Země",
            epithet = "Nezlomný Titánův Monolit",
            icon = "🪨",
            primaryColor = Color(0xFF43A047),
            secondaryColor = Color(0xFF8D6E63),
            historicalOrigin = """
                Zemní afinita pochází z kostí titána Gorgonotha, jenž nesl na svých bedrech první tektonické desky světa. Během zkázy Slunce se jeho tělo proměnilo v nezlomné pohoří Železných skal. Legendy praví, že každý balvan a ruda v sobě nese vzpomínku na stovky vymřelých království a krev prolitou v jejich základech.

                Kdo dokáže otevřít svou mysl k tichému šepotu minerálů, stane se nehybným bodem v jakékoliv bitevní vřavě. Žulový postoj odvrací katastrofy, které by běžného člověka roztrhaly na kusy.
            """.trimIndent(),
            trainingLore = """
                Trénink probíhá v podzemních jeskyních s masivními balvany a křemennými sloupy. Postava se učí zarýt své vědomí do podlahy a absorbovat otřesy přímo do země. Svalová tkáň hrdinky se zahušťuje a kůže získává nepatrný kamenný lesk. S každou tréninkovou seancí se její postoj stává nezlomným a rány protivníků se od ní odrážejí jako suché větvičky.
            """.trimIndent(),
            tacticalNature = "Aplikuje masivní štíty, posiluje obranu celé linie a uzemňuje elektrické výboje. Drtí větrné a bleskové bytosti, avšak je náchylná k prorážení horkým plamenem a erozí způsobenou proudící vodou.",
            ancientArchon = "Gorgonoth, Žulový Kolos",
            sacredRelic = "Monolit Prvního Světa",
            statusEffectName = "🛡️ Kamenný krunýř (Armor)",
            resonanceBlessing = "+25% redukce plošného poškození a imunita vůči sražení"
        ),

        ElementalCodexEntry(
            element = Element.AIR,
            id = "element_air",
            name = "Vzduch",
            epithet = "Vichřice Neviditelných Čepelí",
            icon = "🌪️",
            primaryColor = Color(0xFF00BCD4),
            secondaryColor = Color(0xFFE0F7FA),
            historicalOrigin = """
                Vzdušný živel vznikl rozpadem Nebeských věží v bouřlivé epoše před Zatměním. Pán vichřic Vaelthas roztrhal větrnou oponu na tisíce neviditelných vzdušných čepelí, které od té doby neúnavně obíhají svět. Ve zničené krajině Dominia se vzduch stal zbraní tichých zabijáků a akrobatů.

                Vzduch odmítá být spoután zdmi či řetězy. Může být jemným vánkem tišícím horečku, nebo náhlým cyklónem schopným vyvrátit hradní cimbuří a odříznout hlavu nepřítele dříve, než stačí mrknout.
            """.trimIndent(),
            trainingLore = """
                V tréninkových komnatách větrného živlu jsou nainstalovány vysokotlaké ventily a houpající se kyvadla s břity. Hrdinka se učí předvídat pohyb vzduchu pouhým citem pokožky a provádět mikroskopické úskoky na vzdálenost vlasu. Zvýšený násobič zrychluje její útočné tempo a dává jejím zbraním schopnost generovat ostré vakuové záseky.
            """.trimIndent(),
            tacticalNature = "Spouští status 'Omráčení' (Stun) vyražením dechu z protivníka a poskytuje nejvyšší šanci na úskok. Prořezává vodní proudy a zemské bariéry rychlostí větru, ale v těžké bouři blesků ztrácí kontrolu.",
            ancientArchon = "Vaelthas, Pán Vichřic a Bleskového Vánku",
            sacredRelic = "Zobák Větrného Gryfa",
            statusEffectName = "🌪️ Omráčení (Stun)",
            resonanceBlessing = "+18% šance na úskok a bleskové první tahy"
        ),

        ElementalCodexEntry(
            element = Element.ICE,
            id = "element_ice",
            name = "Led",
            epithet = "Věčný Permafrost Absolutní Nuly",
            icon = "❄️",
            primaryColor = Color(0xFF29B6F6),
            secondaryColor = Color(0xFF80DEEA),
            historicalOrigin = """
                V okamžiku, kdy Slunce zemřelo a svět zahalila tma, část severních provincií okamžitě zamrzla v jediném smrtelném nádechu. Bůh mrazu Moroz proměnil rozkvetlá pole v diamantovou pláň věčného ledu, ve které zůstal čas uvězněn na věčnost.

                Ledová afinita představuje absolutní kontrolu a neochvějný klid. Na rozdíl od tekuté vody led neuhýbá; krystalizuje samotnou vlhkost v okolí, drtí svaly mrazivou křečí a vysává z těl poslední zbytky tělesného tepla.
            """.trimIndent(),
            trainingLore = """
                Trénink v kryokomorách nutí dívku zklidnit tep na hranici klinické smrti a ovládnout třes těla. Hrdinka se učí přetavit mrazivý chlad ve zbraň: při výdechu kondenzuje krystaly ledu na čepeli či drápech. S vyšším násobičem afinity způsobuje každý její zásah protivníkovi mikroskopické zamrznutí cév a svalových vláken.
            """.trimIndent(),
            tacticalNature = "Aplikuje stav 'Zmrazení' (Freeze) a masivní snížení rychlosti nepřátelské formace. Ledové zdi tříští fyzické projektily a paralyzují nepřátele, avšak koncentrovaný žár ohně a lávy dokáže led rychle rozpustit.",
            ancientArchon = "Moroz, Vládce Mrazivého Severu",
            sacredRelic = "Krystal Věčného Permafrostu",
            statusEffectName = "❄️ Zmrazení (Freeze)",
            resonanceBlessing = "+22% zpomalení nepřátel a ledové krystalické bariéry"
        ),

        ElementalCodexEntry(
            element = Element.LIGHTNING,
            id = "element_lightning",
            name = "Blesk",
            epithet = "Bouřný Úder Nebeského Kladiva",
            icon = "⚡",
            primaryColor = Color(0xFFFFD600),
            secondaryColor = Color(0xFFFF9100),
            historicalOrigin = """
                Blesk je nejrychlejší a nejdivočejší z elementů. Vznikl při rozbití Nebeského kovadla, když blesková archontka Astrape odmítla složit zbraně před stínovými hordami. Blesková energie je myšlenka a hněv vesmíru koncentrovaný do mikrosekundy.

                V temném světě Dominia se elektřina stala vzácným nervem: protéká ruinami starých strojů a rozsvěcí oblohu v purpurových bouřích, které roztrhávají vzduch a zanechávají za sebou zápach ozónu a spáleného kovu.
            """.trimIndent(),
            trainingLore = """
                Během elektrizujícího tréninku hrdinka přijímá slabé výboje do akupunkturních bodů a učí se vést proud skrze nervovou soustavu bez poškození srdce. Výsledkem tréninku je nadlidská reakční rychlost a schopnost generovat bleskové jiskry přímo z konečků prstů a zbraní, které řetězově přeskakují mezi nepřáteli.
            """.trimIndent(),
            tacticalNature = "Spouští status 'Šok' (Shock) s řetězovým poškozením sousedních nepřátel ve formaci. Dominuje nad vodními a vzdušnými cíli, ale selhává proti těžkým zemským monolithům, které proud bezpečně svedou do země.",
            ancientArchon = "Astrape, Bouřná Paní Blesků",
            sacredRelic = "Hromová Špice Nebeského Kovadla",
            statusEffectName = "⚡ Šok (Shock)",
            resonanceBlessing = "+25% řetězové poškození a okamžitý startovní tah"
        ),

        ElementalCodexEntry(
            element = Element.DARK,
            id = "element_dark",
            name = "Temnota",
            epithet = "Hladová Prázdnota Propasti",
            icon = "🔮",
            primaryColor = Color(0xFFAB47BC),
            secondaryColor = Color(0xFF311B92),
            historicalOrigin = """
                Temná afinita nevznikla pouze jako nepřítomnost světla. Je to živoucí, hladová esence zrozená ze stínu, který vrhlo padlé Slunce v den Věčného Zatmění. Arcidémon Malakor otevřel brány hluboké Propasti a nechal stínovou mlhu zaplavit mysli smrtelníků.

                Temnota se živí skrytými touhami, zakázanou rozkoší, strachem a tajemstvím. V rukou vládce Dominia se stává nejmocnějším poutem: dokáže spoutat vůli nepřátel, proniknout jakoukoliv fyzickou zbrojí a vysávat duševní energii přímo z jejich těl.
            """.trimIndent(),
            trainingLore = """
                Trénink temné afinity vyžaduje naprosté odevzdání se stínům v nejhlubších kobkách panství. Hrdinka medituje v absolutním temnu, učí se přijmout svou temnou stránku a přeměnit bolest ve zvrácenou extázi. Růst násobiče afinity (+0.05x) zahaluje její zbraně černou aurou, která při zásahu saje krev a posiluje Doménovou expanzi.
            """.trimIndent(),
            tacticalNature = "Vysává životy (Lifesteal), obchází fyzické štíty a spouští zhoubné kletby, které oslabují útok i obranu oponentů. Má brutální synergie s Doménovou expanzí a stínovými kouzly, avšak je zranitelná vůči svatému světlu.",
            ancientArchon = "Malakor, Arcidémon Bezedné Propasti",
            sacredRelic = "Černý Orb Zkaženého Slunce",
            statusEffectName = "🔮 Kletba stínů (Curse)",
            resonanceBlessing = "+15% vysávání životů (Lifesteal) a bonus k Doméně"
        ),

        ElementalCodexEntry(
            element = Element.HOLY,
            id = "element_holy",
            name = "Světlo",
            epithet = "Svatá Záře Ztraceného Úsvitu",
            icon = "✨",
            primaryColor = Color(0xFFFFD54F),
            secondaryColor = Color(0xFFFFF9C4),
            historicalOrigin = """
                Poslední fragmenty ryzího světla pocházejí ze zrcadel Slunečního chrámu, která serafové rozbili, aby zabránili jejich úplnému znesvěcení temnými pány. I v době Věčné noci zůstala tato svatá jiskra uchována v srdcích těch, kteří nepřestávají věřit v očistu a nový úsvit.

                Svaté světlo je ve světě Dominia vzácné a spalující pro vše nečisté. Není to slabé světélko svíčky, ale oslnivý paprsek milosti a spravedlnosti, který spaluje démony na prach a hojí rány smrtelníků, kteří mu složí slib věrnosti.
            """.trimIndent(),
            trainingLore = """
                Trénink probíhá pod posvěcenými křišťály za zpěvu rituálních hymnů. Postava učí svou duši vyzařovat nezlomnou víru a proměnit lásku a oddanost v neproniknutelnou bariéru. Každý nárůst multiplikátoru posiluje léčivé vibrace a dává jejím útokům schopnost rozehnat temné kletby a navrátit spojencům sílu.
            """.trimIndent(),
            tacticalNature = "Poskytuje absolutní očistu všech negativních stavů (debuffů), posvátné absorpční štíty a dvojnásobné zničující poškození proti nemrtvým, stínovým bytostem a démonům temnoty.",
            ancientArchon = "Solaria, Padlý Seraf Ztraceného Úsvitu",
            sacredRelic = "Zlaté Křídlo Prvního Serafa",
            statusEffectName = "✨ Svatá očista (Blessing)",
            resonanceBlessing = "+30% bonus proti démonům a automatická očista debuffů"
        ),

        ElementalCodexEntry(
            element = Element.PHYSICAL,
            id = "element_physical",
            name = "Fyzický",
            epithet = "Kalená Ocel a Krev Válečníků",
            icon = "⚔️",
            primaryColor = Color(0xFFEF5350),
            secondaryColor = Color(0xFFB0BEC5),
            historicalOrigin = """
                Fyzická afinita je nejstarší ze všech sil – zrodila se v okamžiku, kdy první smrtelník uchopil ostrý kámen a vzepřel se dravým šelmám. Zatímco bohové padají, magie vysychá a kouzla selhávají, kalená ocel, pevné svaly a neochvějná lidská vůle zůstávají jedinou jistotou.

                Vulkan, legendární První Kovář, vykoval v temných výhních zbraně, které nepodléhají rozmarům many. Fyzický živel je o čisté mechanické síle, dokonalé geometrii řezu a neúprosné kinetické energii drcených kostí.
            """.trimIndent(),
            trainingLore = """
                Sparring s fyzickým elementem je surový dril: tisíce opakovaných seků, boj se závažím a zpevňování šlach. Dívka se učí vést čepel tak, aby využila celou váhu těla a nalezla nejslabší spáru ve zbroji nepřítele. Zvyšování násobiče zahušťuje svalová vlákna a prohlubuje rány protivníků do nezastavitelného krvácení.
            """.trimIndent(),
            tacticalNature = "Způsobuje surové 'Krvácení' (Bleed), čisté prorážení zbroje bez spotřeby many a masivní poškození v boji tváří v tvář. Je imunní vůči umlčení (Silence) a antimagickým polím.",
            ancientArchon = "Vulkan, První Mistr Kovář",
            sacredRelic = "Železné Kladivo První Kovárny",
            statusEffectName = "⚔️ Krvácení (Bleed)",
            resonanceBlessing = "+20% poškození zbraněmi a nulová závislost na maně"
        )
    )

    private val entryMap: Map<Element, ElementalCodexEntry> = ENTRIES.associateBy { it.element }

    /**
     * Checks if the given element has been discovered in training.
     * An element is discovered if:
     * 1. It is explicitly tracked in player.discoveredElementAffinities
     * 2. It is unlocked in player.unlockedCodexIds (e.g. "element_fire")
     * 3. Any character in the harem has trained it (multiplier > 1.0f)
     */
    fun isDiscovered(state: GameSave, element: Element): Boolean {
        // 1. Explicit tracking
        if (state.player.discoveredElementAffinities.contains(element.name)) return true
        
        // 2. Codex unlocks
        val codexKey = "element_${element.name.lowercase()}"
        if (state.player.unlockedCodexIds.contains(codexKey)) return true
        
        // 3. Any character has multiplier > 1.0f
        val trainedByAny = state.characters.any { char ->
            (char.elementalMultipliers[element.name] ?: 1.0f) > 1.001f
        }
        if (trainedByAny) return true

        return false
    }

    /**
     * Returns the total count of discovered elemental affinities.
     */
    fun getDiscoveredCount(state: GameSave): Int {
        return ENTRIES.count { isDiscovered(state, it.element) }
    }

    /**
     * Returns the set of all discovered elements.
     */
    fun getDiscoveredElements(state: GameSave): Set<Element> {
        return ENTRIES.filter { isDiscovered(state, it.element) }.map { it.element }.toSet()
    }

    /**
     * Retrieves the codex entry for the given element.
     */
    fun getEntryForElement(element: Element): ElementalCodexEntry {
        return entryMap[element] ?: ENTRIES.first()
    }

    /**
     * Returns list of characters who have trained this element along with their multiplier.
     */
    fun getTrainedCharactersForElement(state: GameSave, element: Element): List<Pair<Character, Float>> {
        return state.characters
            .map { it to (it.elementalMultipliers[element.name] ?: 1.0f) }
            .filter { it.second > 1.001f }
            .sortedByDescending { it.second }
    }
}
