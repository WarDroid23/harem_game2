package com.example.haremdark.data

import com.example.haremdark.R
import com.example.haremdark.models.Character
import com.example.haremdark.models.CharacterAttributes
import com.example.haremdark.models.Element
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable
import java.util.UUID

@Serializable
data class SlaveMarketCandidate(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val title: String,
    val archetypeId: String,
    val rarityName: String, // "Běžná", "Vznešená", "Královská", "Mýtická"
    val rarityColorHex: Long = 0xFFFFD700,
    val backgroundTraitName: String,
    val backgroundTraitDesc: String,
    val backstory: String,
    val element: Element = Element.PHYSICAL,
    val initialHp: Int = 110,
    val initialMana: Int = 50,
    val initialAttack: Int = 22,
    val initialDefense: Int = 12,
    val initialSpeed: Int = 15,
    val costWood: Int = 0,
    val costStone: Int = 0,
    val costGold: Int = 250,
    val costMana: Int = 0,
    val costSexEnergy: Int = 0,
    val portraitRes: Int = R.drawable.img_slave_elena_portrait_1790459398016,
    val personalityTags: List<String> = emptyList(),
    var isRecruited: Boolean = false
) : JavaSerializable

object SlaveMarketCatalog {

    val PRESET_SLAVE_CANDIDATES = listOf(
        SlaveMarketCandidate(
            id = "slave_cand_1",
            name = "Lyra ze Stínové Hory",
            title = "Padlá Kněžka Temnoty",
            archetypeId = "posedla",
            rarityName = "Vznešená",
            rarityColorHex = 0xFFAB47BC,
            backgroundTraitName = "Svatokrádežná Aura",
            backgroundTraitDesc = "+30% Produkce many ve Věži Arcana & +15 Magický útok",
            backstory = "Bývalá představená astrálního chrámu, která byla po pádu své svatyně jatkou prodána na trhu. Její tělo uchovává silné astrální zřídlo.",
            element = Element.DARK,
            initialHp = 125,
            initialMana = 85,
            initialAttack = 28,
            initialDefense = 10,
            initialSpeed = 16,
            costWood = 100,
            costStone = 150,
            costGold = 450,
            costMana = 80,
            portraitRes = R.drawable.img_slave_elena_portrait_1790459398016,
            personalityTags = listOf("Tajemná", "Miluje Temnotu", "Posedlá Ctižádostí")
        ),
        SlaveMarketCandidate(
            id = "slave_cand_2",
            name = "Kaelen Vznosná",
            title = "Barbarská Drakobijka",
            archetypeId = "draci_divka",
            rarityName = "Královská",
            rarityColorHex = 0xFFFF7043,
            backgroundTraitName = "Dračí Žár",
            backgroundTraitDesc = "+40% Těžba kamene & +20% Fyzické poškození v boji",
            backstory = "Zajatá válečnice ze Severních Pustin. Odmítá sklopit zrak před kýmkoliv, dokud nezjistí silnější vůli než je její vlastní.",
            element = Element.FIRE,
            initialHp = 180,
            initialMana = 40,
            initialAttack = 36,
            initialDefense = 18,
            initialSpeed = 14,
            costWood = 200,
            costStone = 250,
            costGold = 700,
            costSexEnergy = 15,
            portraitRes = R.drawable.img_slave_aurelia_portrait_1790459410403,
            personalityTags = listOf("Hrdá", "Dračí Hněv", "Nezlomná Vůle")
        ),
        SlaveMarketCandidate(
            id = "slave_cand_3",
            name = "Gwyneth z Koruny",
            title = "Uvězněná Šlechtična",
            archetypeId = "slechticna",
            rarityName = "Mýtická",
            rarityColorHex = 0xFFFFD700,
            backgroundTraitName = "Císařský Rod",
            backgroundTraitDesc = "+50% Zisk zlata v Mincovně & +100 Prestiže Pána",
            backstory = "Dcera popraveného vévody, zakovaná v hedvábných okovech. Její diplomacie a intriky dokážou přinést dominiu obrovské bohatství.",
            element = Element.HOLY,
            initialHp = 100,
            initialMana = 90,
            initialAttack = 18,
            initialDefense = 14,
            initialSpeed = 18,
            costWood = 150,
            costStone = 100,
            costGold = 1200,
            costMana = 150,
            portraitRes = R.drawable.img_slave_vampire_queen_1790459434350,
            personalityTags = listOf("Chladná", "Zapomenutá Dědička", "Taktická Geniálnost")
        ),
        SlaveMarketCandidate(
            id = "slave_cand_4",
            name = "Vespera Vášnivá",
            title = "Sukubí Společnice",
            archetypeId = "sukuba",
            rarityName = "Královská",
            rarityColorHex = 0xFFEC407A,
            backgroundTraitName = "Čarovný Dotek",
            backgroundTraitDesc = "+35% Generování Sexuální Energie & Šance na Okouzlení nepřátel",
            backstory = "Svedená a lapená v bezprostřední blízkosti astrálních bran. Její přítomnost v harému probouzí nevídanou touhu a oddanost.",
            element = Element.ICE,
            initialHp = 140,
            initialMana = 110,
            initialAttack = 32,
            initialDefense = 12,
            initialSpeed = 22,
            costWood = 80,
            costStone = 80,
            costGold = 850,
            costSexEnergy = 30,
            portraitRes = R.drawable.img_slave_lilith_portrait_1790459422573,
            personalityTags = listOf("Vášnivá", "Svůdný Půvab", "Divoká Vášnivost")
        ),
        SlaveMarketCandidate(
            id = "slave_cand_5",
            name = "Silvia Lesní",
            title = "Strážkyně Hvozdu",
            archetypeId = "odvazna",
            rarityName = "Běžná",
            rarityColorHex = 0xFF66BB6A,
            backgroundTraitName = "Lesní Moudrost",
            backgroundTraitDesc = "+45% Těžba černého dřeva & +15 Rychlost v boji",
            backstory = "Mladá hraničářka zajatá při průzkumu hranic dominia. Její znalosti lesa jsou neocenitelné pro těžbu surovin.",
            element = Element.EARTH,
            initialHp = 130,
            initialMana = 45,
            initialAttack = 24,
            initialDefense = 14,
            initialSpeed = 20,
            costWood = 120,
            costStone = 80,
            costGold = 320,
            portraitRes = R.drawable.img_slave_elena_portrait_1790459398016,
            personalityTags = listOf("Horlivá", "Nespoutaný Duch", "Srdce ze Zlata")
        ),
        SlaveMarketCandidate(
            id = "slave_cand_6",
            name = "Serafina Zlomená",
            title = "Svatá Panna Řádu",
            archetypeId = "ticha_panenka",
            rarityName = "Vznešená",
            rarityColorHex = 0xFF29B6F6,
            backgroundTraitName = "Posvátný Balzám",
            backgroundTraitDesc = "+25% Léčení družiny v bojích & Pasivní růst náklonnosti",
            backstory = "Panna, která obětovala svou svobodu výkupným za svou vesnici. Smluvený slib ji váže k věčné poslušnosti pánu dominia.",
            element = Element.HOLY,
            initialHp = 115,
            initialMana = 95,
            initialAttack = 16,
            initialDefense = 16,
            initialSpeed = 15,
            costWood = 150,
            costStone = 150,
            costGold = 500,
            costMana = 100,
            portraitRes = R.drawable.img_skin_valkyrie_golden_dominion_1790460252949,
            personalityTags = listOf("Submisivní", "Křehká Duše", "Svaté Požehnání")
        )
    )

    fun generateFreshMarketRoster(): List<SlaveMarketCandidate> {
        val presetTake = PRESET_SLAVE_CANDIDATES.shuffled().take(2).map { candidate ->
            candidate.copy(
                id = UUID.randomUUID().toString(),
                isRecruited = false
            )
        }
        val proceduralTake = listOf(
            SlaveMarketGenerator.generateProceduralCandidate(),
            SlaveMarketGenerator.generateProceduralCandidate()
        )
        return (presetTake + proceduralTake).shuffled()
    }

    fun convertCandidateToCharacter(candidate: SlaveMarketCandidate): Character {
        val traitsList = mutableListOf(candidate.backgroundTraitName)
        traitsList.addAll(candidate.personalityTags)

        val memory = com.example.haremdark.models.KeyMemory(
            id = UUID.randomUUID().toString(),
            title = "Původ: ${candidate.title}",
            description = candidate.backstory
        )

        return Character(
            id = candidate.id,
            name = candidate.name,
            archetypeId = candidate.archetypeId,
            hp = candidate.initialHp,
            maxHp = candidate.initialHp,
            mana = candidate.initialMana,
            maxMana = candidate.initialMana,
            strength = candidate.initialAttack,
            attributes = CharacterAttributes(
                strength = candidate.initialAttack,
                defense = candidate.initialDefense,
                loyalty = 35,
                affection = 30,
                obedience = 40,
                morale = 50,
                lust = 40,
                fear = 20
            ),
            traits = traitsList,
            keyMemories = mutableListOf(memory),
            affinityPoints = 20
        )
    }
}
