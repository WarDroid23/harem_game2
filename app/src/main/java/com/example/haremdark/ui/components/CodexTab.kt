package com.example.haremdark.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CodexEntry(
    val title: String,
    val category: String,
    val content: String,
    val icon: String
)

val CODEX_ENTRIES = listOf(
    CodexEntry(
        title = "Základy Řízení Harému",
        category = "Správa a Ekonomika",
        icon = "🏰",
        content = "Udržování harému vyžaduje vyvážený přístup k morálce a strachu. Zlomené otrokyně neprodukují kvalitní temnou energii, zatímco ty příliš zhýčkané mohou ztratit respekt. Optimalizuj denní rutinu a pravidelně kontroluj vztahové milníky."
    ),
    CodexEntry(
        title = "Psychologie Submisivity",
        category = "Výcvik",
        icon = "⛓️",
        content = "Každý archetyp reaguje na trénink jinak. Šlechtičny vyžadují systematické lámání pýchy a ponižování, aby přijaly svou novou roli. Naopak 'Subky' prosperují z pochvaly, odměn a jasných pravidel."
    ),
    CodexEntry(
        title = "Historie Dominia",
        category = "Příběhové Pozadí",
        icon = "📜",
        content = "Dominium bylo kdysi světlem severních království, dokud Inkviziční Legie neoznačila stínovou magii za kacířství. Původní pánové byli vyhlazeni a přeživší zahnáni do podzemí. Ty jsi jedním z posledních Pánů Stínů, shromažďující sílu pro návrat."
    ),
    CodexEntry(
        title = "Tréninkové Presety",
        category = "Pokročilé Techniky",
        icon = "🎯",
        content = "Opakováním určitých tréninkových úkonů si dívky osvojí 'Presety'. Tyto dlouhodobé dovednosti (např. Železná Kázeň, Slepá Oddanost) poskytují permanentní bonusy k odolnosti vůči poškození na misích, poslušnosti nebo efektivitě produkce."
    ),
    CodexEntry(
        title = "Systém Osobností a Rivalita",
        category = "Správa Harému",
        icon = "⚔️",
        content = "Dívky nejsou bezduché stroje. Každá má své rysy (Arogantní, Pracovitá, Líná). Pokud dvě arogantní dívky pracují spolu, jejich morálka a loajalita bude klesat kvůli hádkám. Odděluj problémové povahy do různých pracovních skupin."
    )
)

@Composable
fun CodexTab(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Kodex Dominia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Záznamy o historii, strategii výcviku a fungování světa temné magie.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        items(CODEX_ENTRIES) { entry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(entry.icon, fontSize = 18.sp)
                        Column {
                            Text(
                                text = entry.title,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = entry.category,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Text(
                        text = entry.content,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
