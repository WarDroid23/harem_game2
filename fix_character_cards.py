import re

with open('app/src/main/java/com/example/haremdark/ui/components/CharacterCard.kt', 'r') as f:
    text = f.read()

# Add Coil imports if not present
if "import coil.compose.AsyncImage" not in text:
    text = text.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport coil.compose.AsyncImage\nimport coil.compose.SubcomposeAsyncImage\nimport coil.request.ImageRequest\nimport androidx.compose.ui.platform.LocalContext\nimport androidx.compose.ui.layout.ContentScale")

# CharacterCard Box replacement
target1 = """                    // Avatar Placeholder with Initial
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = character.name.take(1),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }"""

replacement1 = """                    // Avatar with Coil
                    val portraitRes = StaticData.getPortraitForArchetype(character.archetypeId)
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(portraitRes)
                            .crossfade(true)
                            .build(),
                        contentDescription = character.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = character.name.take(1),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )"""
text = text.replace(target1, replacement1)

# CharacterGridCard replacement
target2 = """            // Character Portrait Image
            Image(
                painter = androidx.compose.ui.res.painterResource(id = portraitRes),
                contentDescription = character.name,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )"""

replacement2 = """            // Character Portrait Image with Coil
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(portraitRes)
                    .crossfade(true)
                    .build(),
                contentDescription = character.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Fallback",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            )"""
text = text.replace(target2, replacement2)

with open('app/src/main/java/com/example/haremdark/ui/components/CharacterCard.kt', 'w') as f:
    f.write(text)
