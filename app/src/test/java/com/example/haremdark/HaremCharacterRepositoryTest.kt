package com.example.haremdark

import com.example.haremdark.data.HaremCharacterRepositoryImpl
import com.example.haremdark.data.HaremCharacterSort
import com.example.haremdark.models.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HaremCharacterRepositoryTest {

    private lateinit var repository: HaremCharacterRepositoryImpl

    @Before
    fun setup() {
        // Initialize with controlled test characters
        val testCharacters = listOf(
            HaremCharacter(
                id = "char_1",
                name = "Aurelia",
                affection = 80,
                powerLevel = 350,
                role = HaremRole.WARRIOR,
                isFavorite = true
            ),
            HaremCharacter(
                id = "char_2",
                name = "Lilith",
                affection = 95,
                powerLevel = 300,
                role = HaremRole.SORCERESS,
                isFavorite = true
            ),
            HaremCharacter(
                id = "char_3",
                name = "Elena",
                affection = 50,
                powerLevel = 180,
                role = HaremRole.HEALER,
                isFavorite = false
            ),
            HaremCharacter(
                id = "char_4",
                name = "Valeriya",
                affection = 40,
                powerLevel = 280,
                role = HaremRole.GUARDIAN,
                isFavorite = false
            )
        )
        repository = HaremCharacterRepositoryImpl(initialCharacters = testCharacters)
    }

    @Test
    fun testHaremCharacterAttributes() {
        val character = HaremCharacter(
            name = "Katarina",
            affection = 75,
            powerLevel = 250,
            role = HaremRole.ASSASSIN
        )

        assertEquals("Katarina", character.name)
        assertEquals(75, character.affection)
        assertEquals(250, character.powerLevel)
        assertEquals(HaremRole.ASSASSIN, character.role)
        assertEquals("Hluboká oddanost", character.affectionStage)
        assertEquals("Elitní", character.powerTier)
        assertEquals("🗡️ Vražedkyně", character.formattedRole)
        assertTrue(character.isCombatReady)
    }

    @Test
    fun testRepositoryGetters() {
        val all = repository.getAll()
        assertEquals(4, all.size)

        val aurelia = repository.getById("char_1")
        assertNotNull(aurelia)
        assertEquals("Aurelia", aurelia?.name)
        assertEquals(80, aurelia?.affection)
        assertEquals(350, aurelia?.powerLevel)
        assertEquals(HaremRole.WARRIOR, aurelia?.role)

        val lilith = repository.getByName("Lilith")
        assertNotNull(lilith)
        assertEquals(HaremRole.SORCERESS, lilith?.role)

        val healers = repository.getByRole(HaremRole.HEALER)
        assertEquals(1, healers.size)
        assertEquals("Elena", healers.first().name)

        val favorites = repository.getFavorites()
        assertEquals(2, favorites.size)
    }

    @Test
    fun testRepositoryAddAndRemove() {
        val newCompanion = HaremCharacter(
            id = "char_new",
            name = "Morgana",
            affection = 60,
            powerLevel = 220,
            role = HaremRole.SIREN
        )

        repository.add(newCompanion)
        assertEquals(5, repository.getAll().size)
        assertEquals(newCompanion, repository.getById("char_new"))

        val removed = repository.remove("char_new")
        assertTrue(removed)
        assertEquals(4, repository.getAll().size)
        assertNull(repository.getById("char_new"))
    }

    @Test
    fun testRepositoryUpdateAffection() {
        // Increment affection
        val updated = repository.updateAffection("char_1", 10)
        assertNotNull(updated)
        assertEquals(90, updated?.affection)
        assertEquals(90, repository.getById("char_1")?.affection)

        // Decrement affection
        val decreased = repository.updateAffection("char_1", -5)
        assertEquals(85, decreased?.affection)

        // Affection cannot fall below 0
        val clamped = repository.updateAffection("char_1", -200)
        assertEquals(0, clamped?.affection)

        // Directly set affection
        repository.setAffection("char_1", 77)
        assertEquals(77, repository.getById("char_1")?.affection)
    }

    @Test
    fun testRepositoryUpdatePowerLevel() {
        // Increment power level
        val buffed = repository.updatePowerLevel("char_3", 50)
        assertNotNull(buffed)
        assertEquals(230, buffed?.powerLevel)
        assertEquals(230, repository.getById("char_3")?.powerLevel)

        // Directly set power level
        repository.setPowerLevel("char_3", 400)
        assertEquals(400, repository.getById("char_3")?.powerLevel)
        assertEquals("Legendární", repository.getById("char_3")?.powerTier)
    }

    @Test
    fun testRepositoryUpdateRole() {
        // Change Elena from HEALER to CONSORT
        val updated = repository.updateRole("char_3", HaremRole.CONSORT)
        assertNotNull(updated)
        assertEquals(HaremRole.CONSORT, updated?.role)
        assertEquals("👑 Družka", updated?.formattedRole)
        assertEquals(HaremRole.CONSORT, repository.getById("char_3")?.role)
    }

    @Test
    fun testRepositoryFilterAndSort() {
        // Filter by role
        val warriors = repository.filter(role = HaremRole.WARRIOR)
        assertEquals(1, warriors.size)
        assertEquals("Aurelia", warriors.first().name)

        // Filter by min affection
        val highAffection = repository.filter(minAffection = 80)
        assertEquals(2, highAffection.size)

        // Filter by min power
        val highPower = repository.filter(minPower = 300)
        assertEquals(2, highPower.size)

        // Search query
        val queryResult = repository.filter(query = "Lil")
        assertEquals(1, queryResult.size)
        assertEquals("Lilith", queryResult.first().name)

        // Sort by power level descending
        val sortedByPower = repository.sort(HaremCharacterSort.POWER_LEVEL, ascending = false)
        assertEquals("Aurelia", sortedByPower[0].name) // 350
        assertEquals("Lilith", sortedByPower[1].name)  // 300
        assertEquals("Valeriya", sortedByPower[2].name) // 280
        assertEquals("Elena", sortedByPower[3].name)   // 180

        // Sort by affection descending
        val sortedByAffection = repository.sort(HaremCharacterSort.AFFECTION, ascending = false)
        assertEquals("Lilith", sortedByAffection[0].name) // 95
        assertEquals("Aurelia", sortedByAffection[1].name) // 80
    }

    @Test
    fun testBidirectionalMappingWithLegacyCharacter() {
        val legacyChar = Character(
            id = "legacy_1",
            name = "Aurelia Test",
            strength = 25,
            hp = 120,
            maxHp = 120,
            archetypeId = "draci_divka",
            role = "Válečnice",
            attributes = CharacterAttributes(affection = 85, strength = 20)
        )

        val haremChar = legacyChar.toHaremCharacter()
        assertEquals("legacy_1", haremChar.id)
        assertEquals("Aurelia Test", haremChar.name)
        assertEquals(85, haremChar.affection)
        assertEquals(HaremRole.WARRIOR, haremChar.role)
        assertTrue(haremChar.powerLevel > 100)

        // Modify in haremChar and apply back
        haremChar.name = "Aurelia Znovuzrozená"
        haremChar.affection = 95
        haremChar.role = HaremRole.CONSORT
        haremChar.applyToCharacter(legacyChar)

        assertEquals("Aurelia Znovuzrozená", legacyChar.name)
        assertEquals(95, legacyChar.attributes.affection)
        assertEquals("Družka", legacyChar.role)
    }
}
