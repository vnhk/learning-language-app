# learning-language-app

Language learning app with spaced-repetition flashcards, fill-in-the-blank quizzes, and crossword puzzles. Supports English and Spanish.

## Features

- **Flashcards**: SM-2 based spaced repetition; audio playback (Google TTS); example sentences; images (Unsplash); flip animation
- **Quiz**: 10 fill-in-the-blank questions, AI-generated example sentences (OpenAI GPT-3.5)
- **Crossword**: 15×15 grid generated from vocabulary, with across/down clues and answer checking
- **CEFR levels**: A1–C2 + N/A, with automatic level detection via OpenAI
- **Keyboard shortcuts**: Space (flip), Q/W/E/R (Again/Hard/Good/Easy), P (play audio)
- **Integration**: Receives flashcards from `english-text-stats-app`

## Spaced Repetition Algorithm

```
AGAIN → factor = 1,   next = 0 hours (same session)
HARD  → factor × 0.6
GOOD  → factor × 2
EASY  → factor × 4

Interval: factor < 50 → factor × 4 hours
          factor ≥ 50 → 200 + factor hours
Mastered: factor ≥ 512
```

## Key Entity: `TranslationRecord`

`sourceText`, `textTranslation`, `inSentence`, `inSentenceTranslation`, `textSound` (Base64 MP3), `images` (Base64 list), `level`, `factor`, `nextRepeatTime`, `language` (EN/ES)

## External APIs

| API | Purpose |
|-----|---------|
| Google Cloud Translation | Word translation |
| Google Cloud TTS | Audio generation |
| OpenAI GPT-3.5 | Level detection, example sentences |
| Unsplash | Flashcard images |
| diki.pl | Polish dictionary examples |

## Routes

| Path | Purpose |
|------|---------|
| `learning-en/` / `learning-es/` | Language-specific navigation |
| `learning/flashcards` | Spaced repetition review |
| `learning/quiz` | Fill-in-the-blank quiz |
| `learning/words` | Word list + management |
| `learning/crossword` | Crossword puzzle |

## Build

```bash
mvn clean install -DskipTests
```

Part of the `my-tools` multi-module Maven project. Requires `common` to be built first.
