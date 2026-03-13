# Learning Language App - Project Notes

> **IMPORTANT**: Keep this file updated when making significant changes to the codebase. This file serves as persistent memory between Claude Code sessions.

## Overview
Language learning application with flashcards (spaced repetition), quizzes, and crossword puzzles. Supports English and Spanish.

## Key Architecture

### Entities
- **TranslationRecord** - Main flashcard entity with:
  - `sourceText` - Word/phrase in target language
  - `textTranslation` - Translation
  - `inSentence` - Example sentence
  - `inSentenceTranslation` - Translated example
  - `textSound`, `inSentenceSound` - Base64 encoded MP3 audio
  - `images` - List of Base64 encoded images
  - `level` - CEFR level (A1, A2, B1, B2, C1, C2, N/A)
  - `factor` - Spaced repetition factor
  - `nextRepeatTime` - When to show card next
  - `markedForLearning` - Active/inactive flag
  - `language` - "EN" or "ES"

### Spaced Repetition Algorithm (SM-2 based)

**Factor calculation:**
```java
AGAIN -> 1  // Reset, card appears again in same session
HARD  -> factor * 0.6
GOOD  -> factor * 2
EASY  -> factor * 4
```

**Interval calculation:**
```java
AGAIN          -> 0 hours (same session)
factor < 50    -> factor * 4 hours
factor >= 50   -> 200 + factor hours
```

**Mastered threshold:** `factor >= 512`

### Views

#### Flashcard Learning (AbstractLearningView)
- Displays cards due for review (`nextRepeatTime <= now`)
- Level filtering via checkboxes (A1-C2, N/A)
- Reversed mode toggle (show translation first)
- Keyboard shortcuts: Space (flip), Q/W/E/R (Again/Hard/Good/Easy), P (play audio)

**CRITICAL - Keyboard Cleanup:**
```java
@Override
protected void onDetach(DetachEvent detachEvent) {
    super.onDetach(detachEvent);
    getElement().executeJs(
        "if (window.flashcardListener) {" +
        "  document.removeEventListener('keydown', window.flashcardListener);" +
        "  window.flashcardListener = null;" +
        "}"
    );
}
```

#### Quiz (AbstractQuizView)
- Fill-in-the-blank quiz (10 questions)
- Uses AI (OpenAI) to generate example sentences
- Level filtering
- Case-insensitive answer checking
- Visual feedback (green/red backgrounds)
- Score summary with progress bar

#### Word List (AbstractLearningTableView)
- Table view with all words
- Add/edit flashcards
- Generate audio and images
- Fast import mode

#### Crossword (AbstractCrosswordView)
- Professional crossword puzzle generator from vocabulary
- 15x15 grid with intelligent word placement algorithm
- Level filters (A1-C2, N/A) same as Quiz
- Shows clues (translations) with word length hints
- Separated "Across" and "Down" clue sections
- Check answers with visual feedback (green/red cells)
- Reveal all answers option
- Responsive design for mobile

**Key features:**
- Places first word in center, then finds intersections for remaining words
- Maximum 12 words per puzzle for playability
- Words cleaned: punctuation/spaces removed, uppercase
- Supports Polish and Spanish special characters
- Uses TranslationRecordService directly (no separate CrosswordService)

### Services

- **TranslationRecordService** - Core CRUD + spaced repetition logic
- **ExampleOfUsageService** - Fetches/generates example sentences (diki.pl or OpenAI)
- **AddFlashcardService** - Async flashcard creation with translation, audio, examples
- **TextToSpeechService** - Google Cloud TTS for audio
- **TranslatorService** - Google Cloud Translation

### External APIs
- **Google Cloud Translation** - Word translation
- **Google Cloud TTS** - Audio generation
- **OpenAI GPT-3.5** - Level determination, example sentences
- **Unsplash** - Image fetching
- **diki.pl** - Polish dictionary for examples

## File Structure

### Java
- `component/Flashcard.java` - Flashcard UI component with flip animation
- `component/AudioPlayer.java` - HTML5 audio player
- `view/AbstractLearningView.java` - Flashcard learning view
- `view/AbstractQuizView.java` - Quiz view
- `view/AbstractLearningTableView.java` - Word list table
- `view/AbstractLearningAppHomeView.java` - Dashboard with stats
- `view/en/LearningEnglishLayout.java` - Navigation for English
- `view/es/LearningSpanishLayout.java` - Navigation for Spanish
- `service/TranslationRecordService.java` - Core service
- `service/ExampleOfUsageService.java` - Example sentences

### Frontend
- CSS styles are in `my-tools-vaadin-app/src/main/frontend/themes/my-theme/main-layout.css`
- Sections: "FLASHCARD STYLES" and "QUIZ STYLES"

## CSS Classes

### Flashcard
- `.flashcard-layout` - Main container
- `.flashcard-container` - Flip animation wrapper
- `.flashcard` - Card styling
- `.flashcard-knowledge` - Response buttons (colored by nth-child)
- `.a1-level`, `.b1-level`, etc. - Level-specific colors
- `.flashcard-shortcuts-info`, `.shortcut-key` - Keyboard hints

### Quiz
- `.quiz-view` - Main container
- `.language-level-filters` - Filter checkboxes
- `.quiz-results` - Score display
- `.quiz-word-bank`, `.quiz-word-chip` - Words to use
- `.quiz-question-card` - Question container
- `.quiz-question-correct`, `.quiz-question-incorrect` - Answer feedback

### Crossword
- `.crossword-view` - Main container
- `.crossword-game-container` - Flex container for grid + clues
- `.crossword-grid-wrapper`, `.crossword-grid` - Grid styling
- `.crossword-cell`, `.crossword-cell-active`, `.crossword-cell-empty` - Cell types
- `.crossword-cell-number` - Word number indicator
- `.crossword-input`, `.crossword-input-correct`, `.crossword-input-incorrect` - Input fields
- `.crossword-clues-container`, `.crossword-clues-title` - Clues section
- `.crossword-clue-item`, `.crossword-clue-number` - Individual clue styling

## Important Notes

1. **Keyboard listener cleanup is critical** - Without proper cleanup in `onDetach()`, shortcuts persist across navigation
2. **Audio stored as Base64** - Works offline but increases DB size
3. **Images from Unsplash** - Require internet, loaded async
4. **Language codes** - "EN" for English, "ES" for Spanish
5. **Level auto-detection** - Uses OpenAI to determine CEFR level
6. **Quiz answer checking** - Case-insensitive comparison with trimming
