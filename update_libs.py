import re

with open('gradle/libs.versions.toml', 'r') as f:
    text = f.read()

versions_target = """[versions]
agp = "8.8.2\""""

versions_replacement = """[versions]
vico = "1.15.0"
agp = "8.8.2\""""

libs_target = """[libraries]
androidx-core-ktx ="""

libs_replacement = """[libraries]
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose", version.ref = "vico" }
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }
vico-core = { group = "com.patrykandpatrick.vico", name = "core", version.ref = "vico" }
androidx-core-ktx ="""

text = text.replace(versions_target, versions_replacement)
text = text.replace(libs_target, libs_replacement)

with open('gradle/libs.versions.toml', 'w') as f:
    f.write(text)
