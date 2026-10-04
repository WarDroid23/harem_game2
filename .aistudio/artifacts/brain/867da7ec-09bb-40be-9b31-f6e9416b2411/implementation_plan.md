# Character Profile Affinity Milestone Flavor Dialogue System

An interactive, responsive flavor dialogue system that triggers dynamic character dialogue lines upon crossing affinity milestones (30, 70, 120, 180, 250, 350) and renders an animated, tappable speech bubble directly above the character avatar in the Character Profile screen.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions were clarified and confirmed in Phase 1:
> - **Presentation Style**: Animated speech bubble floating above the character avatar banner in `CharacterDetailDialog`.
> - **Milestone Thresholds**: Triggers at every tier threshold: **30, 70, 120, 180, 250, and 350 Affinity Points**.
> - **Interaction**: Players can tap the character avatar or the speech bubble at any time to cycle through newly unlocked flavor dialogue lines with haptic feedback and character emotion reactions.

---

### 1. Overview & Core Concept

- **What It Does**: Enhances the character profile experience by giving companions a vibrant voice that deepens as their bond with the master grows. When specific affinity milestones are crossed, companions unlock new contextual speech lines and proactively greet the player with an animated speech bubble above their avatar. Players can also tap the avatar or bubble to trigger random flavor quotes from their unlocked dialogue pool.
- **Target Audience**: Players invested in companion narrative progression, romance mechanics, and character roleplay in the dark fantasy harem RPG.
- **Key Value**: Replaces static text labels with an expressive, living companion interface that rewards affinity progression with tangible personality shifts (from fearful hostage to loyal confidant, passionate lover, and dark queen).

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Entering Character Profile (`CharacterDetailDialog`)**:
   - The character banner loads with an animated entrance (spring offset and fade).
   - An elegant floating speech bubble appears with a gentle pop-in and bounce above the character's portrait banner, presenting a random flavor line from the character's highest unlocked affinity tier.
2. **Crossing an Affinity Milestone**:
   - When an action (gifting, intimate ritual, training, victory) pushes affinity past a milestone threshold (30, 70, 120, 180, 250, 350):
   - A celebratory milestone badge ("🌟 Milník Pouta Odemčen!") animates into view on the bubble.
   - The bubble pulses with gold/neon border glow and displays the newly unlocked milestone quote.
   - An emotion reaction (Blush, Love, or Sparkle) and particle burst trigger simultaneously.
3. **Interactive Dialogue Tapping**:
   - Tapping the avatar portrait or the speech bubble produces a subtle haptic click (`HapticManager.vibrateClick()`).
   - The speech bubble animates a light scale bounce and transitions to another random line from the unlocked pool.
   - The companion's emotion icon briefly updates (e.g., smiling, blushing, playful).

#### Visual Identity & Theme
- **Speech Bubble Container**:
  - Dark glass surface: `Color(0xF0180B26)` with rounded corners (`RoundedCornerShape(16.dp)`).
  - Subtle pointer tail anchoring the bubble downward toward the character portrait.
  - Border: Animated border stroke shifting from deep violet (`Color(0xFFAB47BC)`) to gold (`Color(0xFFFFD700)`) on high affinity tiers.
- **Typography & Layout**:
  - Quote text: Styled italic font in `MaterialTheme.typography.bodyMedium`, colored `Color(0xFFF3E5F5)` with quotation glyphs `„ ... “`.
  - Affinity Tag: Micro-chip at the top of the bubble indicating the bond level: e.g. `💎 Důvěrnice (Úr. 3)` or `✨ Nový milník!`.
  - Tap prompt hint: Discrete dot indicator `● ● ●` or `💬 Klepni pro další myšlenku` with low alpha.
- **Touch Targets**:
  - The entire speech bubble and character portrait banner have touch targets exceeding 48dp with ripple and scale feedback.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Floating Overlay Bubble vs Fixed Card in Tab Content**
  - *Chosen Approach*: Render an animated speech bubble overlapping the bottom of the portrait banner and the top of the tab container.
  - *Why*: Ensures the companion's voice is the very first thing players see upon opening the profile regardless of which sub-tab (Biography, Stats, Gear, Affinity) is selected, creating immediate emotional connection.
  - *Alternatives Considered*: Putting it only inside the "Affinity" tab was rejected because players spend most of their time in Biography and Equipment tabs.

- **Decision 2: Milestone Tracking & State Persistence**
  - *Chosen Approach*: Track the last acknowledged milestone per character in `GameSave` / `Character` entity (`lastAcknowledgedAffinityMilestone: Int`), comparing against current `affinityPoints`.
  - *Why*: Allows the game to recognize when a new milestone was crossed since the last time the profile was inspected, enabling a one-time "New Milestone Unlocked!" celebration state.
  - *Alternatives Considered*: Re-triggering on every level-up was too intrusive; checking points against defined threshold gates (30, 70, 120, 180, 250, 350) gives clear pacing.

- **Decision 3: Fallback & Archetype Flavor Depth**
  - *Chosen Approach*: Expand `AffinityData` with archetype-specific flavor dialogue pools for all 6 tiers across all archetypes (Submisivní, Domina, Tsundere, Yandere, Hrdá, Masochistka, Posedlá, etc.), ensuring every character has unique lines.
  - *Why*: High narrative variety prevents dialogue repetition and reinforces distinct archetype personalities.

---

### 4. Technical Architecture & Data Strategy

#### Component & Flow Diagram
```
┌────────────────────────────────────────────────────────────────────────┐
│                        CharacterDetailDialog                           │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │                      Hero Portrait Banner                      │   │
│   │   [Portrait Asset] + [Mood Particles] + [Emotion Overlays]     │   │
│   └────────────────────────────────┬───────────────────────────────┘   │
│                                    │ (Overlapped Anchor)               │
│                                    ▼                                   │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │               CharacterSpeechBubbleComponent                   │   │
│   │   "✨ [Milník 70 pts] Důvěrnice"                               │   │
│   │   „Už se tě nebojím, můj pane... začínám v tobě vidět oporu.“  │   │
│   │   [💬 Klepni pro další myšlenku]                               │   │
│   └────────────────────────────────┬───────────────────────────────┘   │
│                                    │                                   │
│                 (User taps bubble or milestone crossed)                │
│                                    ▼                                   │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │                      AffinityDialogueEngine                    │   │
│   │   - Check affinityPoints vs milestones [30, 70, 120, 180, 250] │   │
│   │   - Query AffinityData.getUnlockedFlavorDialogues(char)        │   │
│   │   - Select randomized flavor quote with archetype personality  │   │
│   │   - Trigger HapticManager & LottieEmotionOverlay               │   │
│   └────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

#### Key Models & Helpers
1. **Milestone Definition in `AffinityData.kt`**:
   ```kotlin
   val AFFINITY_MILESTONES = listOf(30, 70, 120, 180, 250, 350)
   
   fun getUnlockedMilestoneLevel(points: Int): Int
   fun getMilestoneFlavorDialogue(character: Character, isMilestoneCelebration: Boolean): String
   ```

2. **Composables & Components**:
   - `CharacterSpeechBubble`: Floating animated bubble with speech pointer tail, tier badge, quote text, and tap-to-cycle action.
   - Integrated inside `CharacterDetailDialog` directly above the tab selector.

3. **Interactivity & State**:
   - `currentBubbleQuote: String`: Current text displayed in the bubble.
   - `isMilestoneCelebration: Boolean`: Glows gold with celebratory banner if a milestone was crossed recently.
   - `onBubbleTap()`: Vibrates, animates bubble bounce, updates quote, and triggers an emotion reaction.
