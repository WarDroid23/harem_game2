import re

with open('app/build.gradle.kts', 'r') as f:
    text = f.read()

target = """    implementation(libs.androidx.material3)"""

replacement = """    implementation(libs.androidx.material3)
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.core)"""

text = text.replace(target, replacement)

with open('app/build.gradle.kts', 'w') as f:
    f.write(text)
