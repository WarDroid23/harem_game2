import re

with open('app/src/main/java/com/example/haremdark/ui/components/InteractiveDialogs.kt', 'r') as f:
    text = f.read()

# Add Coil imports if not present
if "import coil.compose.SubcomposeAsyncImage" not in text:
    text = text.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport coil.compose.SubcomposeAsyncImage\nimport coil.request.ImageRequest\nimport androidx.compose.ui.platform.LocalContext\nimport androidx.compose.ui.layout.ContentScale")

target = """                    Image(
                        painter = painterResource(id = portraitRes),
                        contentDescription = character.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )"""

replacement = """                    SubcomposeAsyncImage(
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
text = text.replace(target, replacement)

with open('app/src/main/java/com/example/haremdark/ui/components/InteractiveDialogs.kt', 'w') as f:
    f.write(text)
