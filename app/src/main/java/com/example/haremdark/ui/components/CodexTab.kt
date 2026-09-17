package com.example.haremdark.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.airbnb.lottie.compose.*
import com.example.haremdark.models.GameSave
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.expandVertically
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CodexEntry(
    val id: String,
    val title: String,
    val category: String,
    val content: String,
    val icon: String,
    val tags: List<String> = emptyList()
)

data class GlossaryTerm(
    val term: String,
    val definition: String
)

val GLOSSARY_TERMS = listOf(
    GlossaryTerm("Dominium", "Temná říše ovládaná Pány Stínů, která byla po staletí útočištěm pro ty, kdo hledali moc v temnotě."),
    GlossaryTerm("Pánové Stínů", "Mocní mágové a válečníci schopní manipulovat s temnou energií a ovládat mysli i těla ostatních."),
    GlossaryTerm("Inkviziční Legie", "Fanatická náboženská organizace zasvěcená vyhlazení veškeré 'nečisté' magie, zejména té stínové."),
    GlossaryTerm("Stínová magie", "Zakázané umění čerpající sílu z prázdnoty a negativních emocí, umožňující pokřivení reality."),
    GlossaryTerm("Temná energie", "Základní surovina Dominia, získávaná z loajality, strachu a utrpení, sloužící k pohánění mocných rituálů."),
    GlossaryTerm("Archetyp", "Vrozená psychologická šablona dívky, určující její základní reakce na svět a trénink."),
    GlossaryTerm("Milníky", "Klíčové úrovně oddanosti, při jejichž dosažení dochází k trvalé změně v psychice a loajalitě.")
)

val CODEX_ENTRIES = listOf(
    CodexEntry(
        id = "harem_basics",
        title = "Základy Řízení Harému",
        category = "Správa a Ekonomika",
        icon = "🏰",
        tags = listOf("ekonomika", "správa", "začátek"),
        content = "Udržování harému vyžaduje vyvážený přístup k morálce a strachu. Zlomené otrokyně neprodukují kvalitní temnou energii, zatímco ty příliš zhýčkané mohou ztratit respekt. Optimalizuj denní rutinu a pravidelně kontroluj vztahové milníky."
    ),
    CodexEntry(
        id = "submissivity_psych",
        title = "Psychologie Submisivity",
        category = "Výcvik",
        icon = "⛓️",
        tags = listOf("výcvik", "psychologie", "submisivita"),
        content = "Každý archetyp reaguje na trénink jinak. Šlechtičny vyžadují systematické lámání pýchy a ponižování, aby přijaly svou novou roli. Naopak 'Subky' prosperují z pochvaly, odměn a jasných pravidel."
    ),
    CodexEntry(
        id = "dominium_history",
        title = "Historie Dominia",
        category = "Příběhové Pozadí",
        icon = "📜",
        tags = listOf("lore", "historie", "svět"),
        content = "Dominium bylo kdysi světlem severních království, dokud Inkviziční Legie neoznačila stínovou magii za kacířství. Původní pánové byli vyhlazeni a přeživší zahnáni do podzemí. Ty jsi jedním z posledních Pánů Stínů, shromažďující sílu pro návrat."
    ),
    CodexEntry(
        id = "training_presets",
        title = "Tréninkové Presety",
        category = "Pokročilé Techniky",
        icon = "🎯",
        tags = listOf("výcvik", "presety", "vylepšení"),
        content = "Opakováním určitých tréninkových úkonů si dívky osvojí 'Presety'. Tyto dlouhodobé dovednosti poskytují permanentní bonusy k odolnosti vůči poškození na misích, poslušnosti nebo efektivitě produkce."
    ),
    CodexEntry(
        id = "personality_rivalry",
        title = "Systém Osobností a Rivalita",
        category = "Správa Harému",
        icon = "⚔️",
        tags = listOf("osobnost", "rivalita", "správa"),
        content = "Dívky nejsou bezduché stroje. Každá má své rysy (Arogantní, Pracovitá, Líná). Pokud dvě arogantní dívky pracují spolu, jejich morálka a loajalita bude klesat kvůli hádkám. Odděluj problémové povahy do různých pracovních skupin."
    )
)

@Composable
fun CodexTab(gameState: GameSave, modifier: Modifier = Modifier) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var bookmarkedIds by rememberSaveable { mutableStateOf(setOf<String>()) }
    var showOnlyBookmarked by rememberSaveable { mutableStateOf(false) }
    var selectedGlossaryTerm by remember { mutableStateOf<GlossaryTerm?>(null) }
    
    // Track "seen" ids in this session to highlight truly "newly unlocked" ones
    val seenIds = rememberSaveable { mutableStateOf(setOf<String>()) }

    val unlockedEntries = CODEX_ENTRIES.filter { entry ->
        gameState.player.unlockedCodexIds.contains(entry.id) || entry.id == "harem_basics" || entry.id == "dominium_history"
    }

    val filteredEntries = unlockedEntries.filter { entry ->
        val matchesSearch = entry.title.contains(searchQuery, ignoreCase = true) ||
                entry.category.contains(searchQuery, ignoreCase = true) ||
                entry.tags.any { it.contains(searchQuery, ignoreCase = true) }
        val matchesBookmarkFilter = !showOnlyBookmarked || bookmarkedIds.contains(entry.id)
        matchesSearch && matchesBookmarkFilter
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 4.dp)) {
        // Search & Filter Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Hledat v kodexu...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Vymazat", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = !showOnlyBookmarked,
                        onClick = { showOnlyBookmarked = false },
                        label = { Text("Všechny", fontSize = 11.sp) },
                        leadingIcon = if (!showOnlyBookmarked) {
                            { Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                    FilterChip(
                        selected = showOnlyBookmarked,
                        onClick = { showOnlyBookmarked = true },
                        label = { Text("Záložky (${bookmarkedIds.size})", fontSize = 11.sp) },
                        leadingIcon = if (showOnlyBookmarked) {
                            { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else {
                            { Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            if (filteredEntries.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (showOnlyBookmarked) "Nemáte žádné záložky." else "Nebyly nalezeny žádné záznamy.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            items(filteredEntries, key = { it.id }) { entry ->
                val isNew = !seenIds.value.contains(entry.id) && gameState.player.unlockedCodexIds.contains(entry.id)
                
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically()
                ) {
                    CodexEntryCard(
                        entry = entry,
                        isBookmarked = bookmarkedIds.contains(entry.id),
                        isNew = isNew,
                        onToggleBookmark = {
                            bookmarkedIds = if (bookmarkedIds.contains(entry.id)) {
                                bookmarkedIds - entry.id
                            } else {
                                bookmarkedIds + entry.id
                            }
                        },
                        onTermClick = { term ->
                            selectedGlossaryTerm = GLOSSARY_TERMS.find { it.term.equals(term, ignoreCase = true) }
                        },
                        onSeen = {
                            if (isNew) {
                                seenIds.value = seenIds.value + entry.id
                            }
                        }
                    )
                }
            }
        }
    }

    // Glossary Popup
    if (selectedGlossaryTerm != null) {
        AlertDialog(
            onDismissRequest = { selectedGlossaryTerm = null },
            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(selectedGlossaryTerm!!.term, fontWeight = FontWeight.Bold) },
            text = { Text(selectedGlossaryTerm!!.definition, fontSize = 14.sp, lineHeight = 20.sp) },
            confirmButton = {
                TextButton(onClick = { selectedGlossaryTerm = null }) {
                    Text("Zavřít")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun CodexEntryCard(
    entry: CodexEntry,
    isBookmarked: Boolean,
    isNew: Boolean,
    onToggleBookmark: () -> Unit,
    onTermClick: (String) -> Unit,
    onSeen: () -> Unit
) {
    LaunchedEffect(Unit) {
        if (isNew) {
            // Delay marking as seen to let animation play
            kotlinx.coroutines.delay(3000)
            onSeen()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box {
                    Text(entry.icon, fontSize = 18.sp)
                    if (isNew) {
                        LottieNewBadge(modifier = Modifier.size(24.dp).align(Alignment.Center))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.title,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                        if (isNew) {
                            Surface(
                                color = Color(0xFFFFD700),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    "NOVÉ",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = entry.category,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onToggleBookmark, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Záložka",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            
            InteractiveCodexContent(
                text = entry.content,
                onTermClick = onTermClick
            )

            if (entry.tags.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(entry.tags) { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LottieNewBadge(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Url("https://assets5.lottiefiles.com/packages/lf20_at6mdfsq.json")
    )
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier
    )
}

@Composable
fun InteractiveCodexContent(
    text: String,
    onTermClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
) {
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        val terms = GLOSSARY_TERMS.map { it.term }.sortedByDescending { it.length }
        
        val sortedTerms = terms.map { it to text.indexOf(it, ignoreCase = true) }
            .filter { it.second != -1 }
            .sortedBy { it.second }

        sortedTerms.forEach { (term, index) ->
            if (index >= lastIndex) {
                append(text.substring(lastIndex, index))
                
                pushStringAnnotation(tag = "GLOSSARY", annotation = term)
                withStyle(style = SpanStyle(
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )) {
                    append(text.substring(index, index + term.length))
                }
                pop()
                lastIndex = index + term.length
            }
        }
        append(text.substring(lastIndex))
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodySmall.copy(
            color = color,
            fontSize = 12.sp,
            lineHeight = 18.sp
        ),
        modifier = modifier,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "GLOSSARY", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    onTermClick(annotation.item)
                }
        }
    )
}
