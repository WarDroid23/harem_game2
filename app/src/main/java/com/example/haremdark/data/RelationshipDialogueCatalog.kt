package com.example.haremdark.data

import com.example.haremdark.models.*

/**
 * Curated Catalog of unlockable character narrative paths and branching dialogues.
 */
object RelationshipDialogueCatalog {

    /**
     * Gets all narrative paths available for a specific companion based on archetype and current affection.
     */
    fun getPathsForCharacter(character: Character): List<CharacterNarrativePath> {
        val name = character.name
        val archetype = character.archetypeId

        return listOf(
            // --- STAGE 1: GUARDED TRUST PATH (Affection 25+) ---
            CharacterNarrativePath(
                id = "path_origin_${character.id}",
                title = "Tajemství starého domova",
                icon = "🌿",
                requiredStage = RelationshipStage.GUARDED_TRUST,
                requiredInteractionsCount = 2,
                synopsis = "$name se ohlíží za svou minulostí před příchodem do dominia a ptá se, co s ní zamýšlíš.",
                promptDialogue = "Pane... často v noci přemýšlím o dnech, kdy jsem byla volná. Proč jsi ze všech dívek v kraji zvolil zrovna mě?",
                characterInitialThought = "$name se ti dívá přímo do očí, v hlase má lehkou nejistotu, ale i upřímnou zvědavost.",
                options = listOf(
                    DialogueChoiceOption(
                        id = "opt_origin_heart",
                        playerText = "Viděl jsem v tvých očích oheň a sílu, kterou nechci zhasnout, ale probudit.",
                        tendency = RelationshipTendency.ROMANTIC_DEVOTION,
                        affectionDelta = 12,
                        loyaltyDelta = 10,
                        desireDelta = 8,
                        responseText = "(Zardí se a sklopí zrak) Nikdo se mnou ještě takhle nepromluvil... slibuji, že tě nezklamu.",
                        responseEmotion = "Dojatá & Zardělá",
                        statBonusDescription = "+12 Náklonnost, +10 Loajalita"
                    ),
                    DialogueChoiceOption(
                        id = "opt_origin_power",
                        playerText = "Patříš ke mně, protože jsi cenná součást moci mého dominia.",
                        tendency = RelationshipTendency.DOMINANT_AUTHORITY,
                        affectionDelta = 6,
                        obedienceDelta = 14,
                        desireDelta = 10,
                        responseText = "Rozumím, můj pane. Má poslušnost a síla jsou plně k tvým službám.",
                        responseEmotion = "Pokorná & Odevzdaná",
                        statBonusDescription = "+14 Poslušnost, +6 Náklonnost"
                    ),
                    DialogueChoiceOption(
                        id = "opt_origin_warrior",
                        playerText = "Potřebuji silné bojovnice, na které se mohu spolehnout v bitvě i v sídle.",
                        tendency = RelationshipTendency.WARRIOR_COMRADE,
                        affectionDelta = 8,
                        loyaltyDelta = 12,
                        responseText = "Bojovat po tvém boku je pro mě ctí. Můj meč nikdy nezaváhá!",
                        responseEmotion = "Odhodlaná",
                        statBonusDescription = "+12 Loajalita, +8 Náklonnost"
                    )
                ),
                perkRewardTitle = "Požehnání důvěry",
                perkRewardDescription = "+5% k efektivitě výcviku a rozhovorů"
            ),

            // --- STAGE 2: AFFECTION ALLIANCE PATH (Affection 50+) ---
            CharacterNarrativePath(
                id = "path_night_talk_${character.id}",
                title = "Noční rozmluva v soukromí",
                icon = "💖",
                requiredStage = RelationshipStage.AFFECTION_ALLIANCE,
                requiredInteractionsCount = 4,
                synopsis = "V pozdních nočních hodinách tě $name navštíví v pracovně se sklenkou vína a osobním přiznáním.",
                promptDialogue = "Nemohla jsem usnout, můj pane... pokaždé, když zavřu oči, vidím tvou tvář. Už nejsi jen mým vládcem... začínáš být mým celým světem.",
                characterInitialThought = "V jejím hlase je cítit silné chvění. Její přítomnost zaplňuje místnost vůní jasmínu a nočních květů.",
                options = listOf(
                    DialogueChoiceOption(
                        id = "opt_night_embrace",
                        playerText = "Přitáhni si ji do náruče a polib ji: 'Tvé místo je navždy po mém boku.'",
                        tendency = RelationshipTendency.ROMANTIC_DEVOTION,
                        affectionDelta = 16,
                        loyaltyDelta = 12,
                        desireDelta = 18,
                        responseText = "(Pevně tě obejme a přitiskne svou hruď k tvé) Můj drahý pane... mé tělo i duše se chvějí touhou.",
                        responseEmotion = "Vášnivá & Zamilovaná",
                        statBonusDescription = "+16 Náklonnost, +18 Touha"
                    ),
                    DialogueChoiceOption(
                        id = "opt_night_corrupt",
                        playerText = "Zašeptej jí do ucha tajemství temné magie: 'Nech temnotu proniknout do tvého srdce.'",
                        tendency = RelationshipTendency.CORRUPTED_INTIMACY,
                        affectionDelta = 12,
                        obedienceDelta = 15,
                        desireDelta = 20,
                        responseText = "Cítím ten temný žár... udělej se mnou cokoliv, co tě potěší, můj pane.",
                        responseEmotion = "Extatická & Poddajná",
                        statBonusDescription = "+12 Náklonnost, +15 Poslušnost, +20 Touha"
                    ),
                    DialogueChoiceOption(
                        id = "opt_night_protect",
                        playerText = "Pohlaď ji po tváři: 'Dokud stojím, žádná hrozba se tě nedotkne.'",
                        tendency = RelationshipTendency.WARRIOR_COMRADE,
                        affectionDelta = 15,
                        loyaltyDelta = 15,
                        responseText = "V tvé přítomnosti se cítím v bezpečí, jaké jsem nikdy nepoznala.",
                        responseEmotion = "Oddaná & Klidná",
                        statBonusDescription = "+15 Náklonnost, +15 Loajalita"
                    )
                ),
                perkRewardTitle = "Pouto spojenectví",
                perkRewardDescription = "+10% poškození při společném boji v aréně"
            ),

            // --- STAGE 3: PASSIONATE DEVOTION PATH (Affection 75+) ---
            CharacterNarrativePath(
                id = "path_talisman_${character.id}",
                title = "Darování rodinného talismanu",
                icon = "🔥",
                requiredStage = RelationshipStage.PASSIONATE_DEVOTION,
                requiredInteractionsCount = 6,
                synopsis = "$name ti nabízí svůj nejposvátnější rodinný klenot na znamení absolutní odevzdanosti.",
                promptDialogue = "Tento amulet nosila má matka a její matka před ní. Přísahala jsem, že ho daruji jen tomu, komu bude patřit můj život i mé tělo. Chci, abys ho nosil ty.",
                characterInitialThought = "Podává ti stříbrný medailon se vsazeným rubínem. Její prsty se lehce dotýkají tvých dlaní.",
                options = listOf(
                    DialogueChoiceOption(
                        id = "opt_talisman_accept_kiss",
                        playerText = "Přijmi talisman a něžně ji polib: 'Budu ho střežit jako svůj největší poklad.'",
                        tendency = RelationshipTendency.ROMANTIC_DEVOTION,
                        affectionDelta = 20,
                        loyaltyDelta = 18,
                        desireDelta = 15,
                        responseText = "(V očích se jí zatřpytí slzy štěstí) Děkuji ti, můj pane... jsem navěky tvá.",
                        responseEmotion = "Naprosto Oddaná",
                        statBonusDescription = "+20 Náklonnost, +18 Loajalita, +15 Touha"
                    ),
                    DialogueChoiceOption(
                        id = "opt_talisman_mark",
                        playerText = "Vezmi amulet a nasaď jí na hrdlo zlatý obojek pána: 'A ty jsi můj nejcennější klenot.'",
                        tendency = RelationshipTendency.DOMINANT_AUTHORITY,
                        affectionDelta = 15,
                        obedienceDelta = 22,
                        desireDelta = 22,
                        responseText = "(Pohladí studený kov na svém krku s povzdechem rozkoše) Tvé označení nosím s hrdostí...",
                        responseEmotion = "Extatická otrokyně lásky",
                        statBonusDescription = "+15 Náklonnost, +22 Poslušnost, +22 Touha"
                    )
                ),
                perkRewardTitle = "Aura oddané bojovnice",
                perkRewardDescription = "+15% k obraně a automatická regenerace +10 HP v boji"
            ),

            // --- STAGE 4: SOULBOUND ETERNITY PATH (Affection 90+) ---
            CharacterNarrativePath(
                id = "path_eternity_${character.id}",
                title = "Krvavá přísaha věčnosti",
                icon = "👑",
                requiredStage = RelationshipStage.SOULBOUND_ETERNITY,
                requiredInteractionsCount = 8,
                synopsis = "V nejhlubší svatyni dominia uzavíráte s $name prastaré krvavé pouto, které propojí vaše duše navždy.",
                promptDialogue = "Můj pane... můj milovaný. Neexistuje síla na tomto světě ani v podsvětí, která by nás mohla rozdělit. Chci s tebou spojit svou krev i duši pro celou věčnost.",
                characterInitialThought = "V místnosti září posvátné runy pouta. $name nastavuje svou ruku k rituálnímu řezu bez špetky strachu.",
                options = listOf(
                    DialogueChoiceOption(
                        id = "opt_eternity_soulmate",
                        playerText = "Spoj své dlaně s jejími a zpečeť rituál: 'V životě i ve smrti, tvá duše je mou součástí.'",
                        tendency = RelationshipTendency.ROMANTIC_DEVOTION,
                        affectionDelta = 25,
                        loyaltyDelta = 25,
                        desireDelta = 25,
                        responseText = "Cítím tvůj tep ve své hrudi! Naše pouto je věčné, můj božský pane!",
                        responseEmotion = "Božské souznění",
                        statBonusDescription = "+25 Náklonnost, +25 Loajalita, Odemčeno Ultimátní Kombo"
                    ),
                    DialogueChoiceOption(
                        id = "opt_eternity_dark_empress",
                        playerText = "Prohlás ji svou Temnou Královnou po boku na trůnu dominia.",
                        tendency = RelationshipTendency.CORRUPTED_INTIMACY,
                        affectionDelta = 25,
                        obedienceDelta = 25,
                        desireDelta = 25,
                        responseText = "Budu vládnout po tvém boku a zničím každého, kdo by se odvážil vzepřít tvé vůli!",
                        responseEmotion = "Temná Královna",
                        statBonusDescription = "+25 Všechny statistiky, +30 Vliv v metropoli"
                    )
                ),
                perkRewardTitle = "Pouto věčnosti (Soulbound)",
                perkRewardDescription = "+25% ke všem bojovým atributům a plná imunita vůči zmatení a strachu"
            )
        )
    }
}
