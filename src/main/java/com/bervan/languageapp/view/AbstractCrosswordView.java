package com.bervan.languageapp.view;

import com.bervan.common.MenuNavigationComponent;
import com.bervan.common.component.BervanButton;
import com.bervan.common.component.BervanButtonStyle;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.service.TranslationRecordService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.data.domain.Pageable;

import java.util.*;

public abstract class AbstractCrosswordView extends VerticalLayout {

    private static final int GRID_SIZE = 15;
    private static final int MAX_WORDS = 12;
    private static final char EMPTY = '\0';

    private final TranslationRecordService translationRecordService;
    private final String language;

    // Level filters
    private final Checkbox levelNA = new Checkbox("N/A", true);
    private final Checkbox levelA1 = new Checkbox("A1", true);
    private final Checkbox levelA2 = new Checkbox("A2", true);
    private final Checkbox levelB1 = new Checkbox("B1", true);
    private final Checkbox levelB2 = new Checkbox("B2", true);
    private final Checkbox levelC1 = new Checkbox("C1", true);
    private final Checkbox levelC2 = new Checkbox("C2", true);

    // Game state
    private char[][] grid;
    private List<CrosswordWord> placedWords;
    private Map<String, TextField> cellInputs;
    private Div gridContainer;
    private VerticalLayout cluesContainer;
    private Div resultsDiv;

    public AbstractCrosswordView(TranslationRecordService translationRecordService,
                                  String language,
                                  MenuNavigationComponent menuNavigationLayout) {
        this.translationRecordService = translationRecordService;
        this.language = language;

        addClassName("crossword-view");
        setPadding(true);
        setSpacing(true);

        add(menuNavigationLayout);

        // Level filters
        Div levelFiltersContainer = new Div();
        levelFiltersContainer.addClassName("language-level-filters");

        Span filterLabel = new Span("Filter by level:");
        filterLabel.addClassName("language-filter-label");

        HorizontalLayout checkboxes = new HorizontalLayout(levelNA, levelA1, levelA2, levelB1, levelB2, levelC1, levelC2);
        checkboxes.setSpacing(true);
        checkboxes.setAlignItems(FlexComponent.Alignment.CENTER);

        levelFiltersContainer.add(filterLabel, checkboxes);
        add(levelFiltersContainer);

        // Action buttons
        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.setSpacing(true);
        actionButtons.addClassName("crossword-action-buttons");

        Button generateButton = new BervanButton("Generate Crossword", e -> generateCrossword());
        generateButton.addClassName("primary");

        Button checkButton = new BervanButton("Check Answers", e -> checkAnswers());
        Button revealButton = new BervanButton("Reveal All", e -> revealAnswers(), BervanButtonStyle.WARNING);
        Button resetButton = new BervanButton("Reset", e -> resetGame(), BervanButtonStyle.SECONDARY);

        actionButtons.add(generateButton, checkButton, revealButton, resetButton);
        add(actionButtons);

        // Results container
        resultsDiv = new Div();
        resultsDiv.addClassName("crossword-results");
        resultsDiv.setVisible(false);
        add(resultsDiv);

        // Main game container
        HorizontalLayout gameContainer = new HorizontalLayout();
        gameContainer.addClassName("crossword-game-container");
        gameContainer.setWidthFull();
        gameContainer.setSpacing(true);

        // Grid container
        gridContainer = new Div();
        gridContainer.addClassName("crossword-grid-wrapper");

        // Clues container
        cluesContainer = new VerticalLayout();
        cluesContainer.addClassName("crossword-clues-container");
        cluesContainer.setPadding(true);
        cluesContainer.setSpacing(true);

        gameContainer.add(gridContainer, cluesContainer);
        add(gameContainer);

        // Initialize
        resetGame();
    }

    private void resetGame() {
        grid = new char[GRID_SIZE][GRID_SIZE];
        placedWords = new ArrayList<>();
        cellInputs = new HashMap<>();
        gridContainer.removeAll();
        cluesContainer.removeAll();
        resultsDiv.setVisible(false);

        Div placeholder = new Div();
        placeholder.setText("Click 'Generate Crossword' to start!");
        placeholder.addClassName("crossword-placeholder");
        gridContainer.add(placeholder);
    }

    private void generateCrossword() {
        resetGame();
        gridContainer.removeAll();

        // Get words from database
        List<TranslationRecord> records = translationRecordService.getRecordsForQuiz(
                language, getSelectedLevels(), Pageable.ofSize(200)
        ).stream().toList();

        if (records.isEmpty()) {
            showError("No flashcards found for selected levels!");
            return;
        }

        // Filter and prepare words
        List<CrosswordWord> candidates = new ArrayList<>();
        for (TranslationRecord record : records) {
            String word = cleanWord(record.getSourceText());
            if (word.length() >= 3 && word.length() <= GRID_SIZE - 2) {
                candidates.add(new CrosswordWord(word, record.getTextTranslation()));
            }
        }

        if (candidates.size() < 3) {
            showError("Not enough valid words found. Add more flashcards!");
            return;
        }

        // Shuffle and try to place words
        Collections.shuffle(candidates);

        // Sort by length (longer first) for better placement
        candidates.sort((a, b) -> b.word.length() - a.word.length());

        // Place first word in center horizontally
        CrosswordWord firstWord = candidates.get(0);
        int startRow = GRID_SIZE / 2;
        int startCol = (GRID_SIZE - firstWord.word.length()) / 2;
        placeWord(firstWord, startRow, startCol, true);
        placedWords.add(firstWord);

        // Try to place remaining words
        for (int i = 1; i < candidates.size() && placedWords.size() < MAX_WORDS; i++) {
            CrosswordWord word = candidates.get(i);
            if (tryPlaceWord(word)) {
                placedWords.add(word);
            }
        }

        if (placedWords.size() < 3) {
            showError("Could not generate crossword. Try again!");
            return;
        }

        // Assign numbers to words
        assignWordNumbers();

        // Build UI
        buildGridUI();
        buildCluesUI();
    }

    private String cleanWord(String text) {
        return text.replaceAll("[^a-zA-ZąćęłńóśźżĄĆĘŁŃÓŚŹŻáéíóúüñÁÉÍÓÚÜÑ]", "")
                   .toUpperCase()
                   .trim();
    }

    private boolean tryPlaceWord(CrosswordWord word) {
        // Find intersections with placed words
        for (CrosswordWord placed : placedWords) {
            for (int i = 0; i < word.word.length(); i++) {
                for (int j = 0; j < placed.word.length(); j++) {
                    if (word.word.charAt(i) == placed.word.charAt(j)) {
                        // Try to place perpendicular
                        int newRow, newCol;
                        boolean horizontal = !placed.horizontal;

                        if (placed.horizontal) {
                            // Placed is horizontal, new word vertical
                            newRow = placed.row - i;
                            newCol = placed.col + j;
                        } else {
                            // Placed is vertical, new word horizontal
                            newRow = placed.row + j;
                            newCol = placed.col - i;
                        }

                        if (canPlace(word, newRow, newCol, horizontal)) {
                            placeWord(word, newRow, newCol, horizontal);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean canPlace(CrosswordWord word, int row, int col, boolean horizontal) {
        int len = word.word.length();

        // Check bounds
        if (row < 0 || col < 0) return false;
        if (horizontal && col + len > GRID_SIZE) return false;
        if (!horizontal && row + len > GRID_SIZE) return false;

        // Check each cell
        for (int i = 0; i < len; i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;

            char existing = grid[r][c];
            char needed = word.word.charAt(i);

            if (existing != EMPTY && existing != needed) {
                return false;
            }

            // Check adjacent cells (no parallel words touching)
            if (existing == EMPTY) {
                if (horizontal) {
                    // Check above and below
                    if (r > 0 && grid[r - 1][c] != EMPTY && !isPartOfIntersection(r - 1, c, r, c)) return false;
                    if (r < GRID_SIZE - 1 && grid[r + 1][c] != EMPTY && !isPartOfIntersection(r + 1, c, r, c)) return false;
                } else {
                    // Check left and right
                    if (c > 0 && grid[r][c - 1] != EMPTY && !isPartOfIntersection(r, c - 1, r, c)) return false;
                    if (c < GRID_SIZE - 1 && grid[r][c + 1] != EMPTY && !isPartOfIntersection(r, c + 1, r, c)) return false;
                }
            }
        }

        // Check cell before word
        if (horizontal && col > 0 && grid[row][col - 1] != EMPTY) return false;
        if (!horizontal && row > 0 && grid[row - 1][col] != EMPTY) return false;

        // Check cell after word
        if (horizontal && col + len < GRID_SIZE && grid[row][col + len] != EMPTY) return false;
        if (!horizontal && row + len < GRID_SIZE && grid[row + len][col] != EMPTY) return false;

        return true;
    }

    private boolean isPartOfIntersection(int r1, int c1, int r2, int c2) {
        // Check if both cells are part of an intersection
        return grid[r1][c1] != EMPTY && grid[r2][c2] != EMPTY;
    }

    private void placeWord(CrosswordWord word, int row, int col, boolean horizontal) {
        word.row = row;
        word.col = col;
        word.horizontal = horizontal;

        for (int i = 0; i < word.word.length(); i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;
            grid[r][c] = word.word.charAt(i);
        }
    }

    private void assignWordNumbers() {
        // Sort words by position (top-to-bottom, left-to-right)
        placedWords.sort((a, b) -> {
            if (a.row != b.row) return a.row - b.row;
            return a.col - b.col;
        });

        Map<String, Integer> positionNumbers = new HashMap<>();
        int number = 1;

        for (CrosswordWord word : placedWords) {
            String posKey = word.row + "-" + word.col;
            if (!positionNumbers.containsKey(posKey)) {
                positionNumbers.put(posKey, number++);
            }
            word.number = positionNumbers.get(posKey);
        }
    }

    private void buildGridUI() {
        Div gridDiv = new Div();
        gridDiv.addClassName("crossword-grid");
        gridDiv.getStyle().set("display", "grid");
        gridDiv.getStyle().set("grid-template-columns", "repeat(" + GRID_SIZE + ", 1fr)");
        gridDiv.getStyle().set("gap", "2px");

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Div cell = new Div();
                cell.addClassName("crossword-cell");

                if (grid[row][col] != EMPTY) {
                    cell.addClassName("crossword-cell-active");

                    // Check if this is start of a word
                    Integer wordNumber = getWordNumberAt(row, col);
                    if (wordNumber != null) {
                        Span numSpan = new Span(String.valueOf(wordNumber));
                        numSpan.addClassName("crossword-cell-number");
                        cell.add(numSpan);
                    }

                    // Input field
                    TextField input = new TextField();
                    input.setMaxLength(1);
                    input.addClassName("crossword-input");
                    final int currentRow = row;
                    final int currentCol = col;
                    input.addKeyPressListener(Key.ENTER, e -> focusNextInput(currentRow, currentCol));

                    String cellId = row + "-" + col;
                    cellInputs.put(cellId, input);
                    cell.add(input);
                } else {
                    cell.addClassName("crossword-cell-empty");
                }

                gridDiv.add(cell);
            }
        }

        gridContainer.add(gridDiv);
    }

    private Integer getWordNumberAt(int row, int col) {
        for (CrosswordWord word : placedWords) {
            if (word.row == row && word.col == col) {
                return word.number;
            }
        }
        return null;
    }

    private void focusNextInput(int currentRow, int currentCol) {
        // Try to find next input in row, then next row
        for (int r = currentRow; r < GRID_SIZE; r++) {
            int startCol = (r == currentRow) ? currentCol + 1 : 0;
            for (int c = startCol; c < GRID_SIZE; c++) {
                String cellId = r + "-" + c;
                TextField next = cellInputs.get(cellId);
                if (next != null) {
                    next.focus();
                    return;
                }
            }
        }
    }

    private void buildCluesUI() {
        cluesContainer.removeAll();

        // Across clues
        H3 acrossTitle = new H3("Across →");
        acrossTitle.addClassName("crossword-clues-title");
        cluesContainer.add(acrossTitle);

        VerticalLayout acrossClues = new VerticalLayout();
        acrossClues.setPadding(false);
        acrossClues.setSpacing(false);

        for (CrosswordWord word : placedWords) {
            if (word.horizontal) {
                Div clue = createClueItem(word);
                acrossClues.add(clue);
            }
        }
        cluesContainer.add(acrossClues);

        // Down clues
        H3 downTitle = new H3("Down ↓");
        downTitle.addClassName("crossword-clues-title");
        cluesContainer.add(downTitle);

        VerticalLayout downClues = new VerticalLayout();
        downClues.setPadding(false);
        downClues.setSpacing(false);

        for (CrosswordWord word : placedWords) {
            if (!word.horizontal) {
                Div clue = createClueItem(word);
                downClues.add(clue);
            }
        }
        cluesContainer.add(downClues);
    }

    private Div createClueItem(CrosswordWord word) {
        Div clueDiv = new Div();
        clueDiv.addClassName("crossword-clue-item");

        Span numSpan = new Span(word.number + ". ");
        numSpan.addClassName("crossword-clue-number");

        Span textSpan = new Span(word.clue + " (" + word.word.length() + ")");
        textSpan.addClassName("crossword-clue-text");

        clueDiv.add(numSpan, textSpan);
        return clueDiv;
    }

    private void checkAnswers() {
        if (placedWords.isEmpty()) {
            showError("Generate a crossword first!");
            return;
        }

        int correctLetters = 0;
        int totalLetters = 0;

        for (CrosswordWord word : placedWords) {
            for (int i = 0; i < word.word.length(); i++) {
                int r = word.horizontal ? word.row : word.row + i;
                int c = word.horizontal ? word.col + i : word.col;
                String cellId = r + "-" + c;

                TextField input = cellInputs.get(cellId);
                char expected = word.word.charAt(i);
                totalLetters++;

                if (input != null) {
                    String value = input.getValue().toUpperCase().trim();
                    if (value.length() == 1 && value.charAt(0) == expected) {
                        correctLetters++;
                        input.removeClassName("crossword-input-incorrect");
                        input.addClassName("crossword-input-correct");
                    } else if (!value.isEmpty()) {
                        input.removeClassName("crossword-input-correct");
                        input.addClassName("crossword-input-incorrect");
                    }
                }
            }
        }

        showResults(correctLetters, totalLetters);
    }

    private void showResults(int correct, int total) {
        resultsDiv.removeAll();
        resultsDiv.setVisible(true);

        double percentage = total > 0 ? (correct * 100.0 / total) : 0;

        H3 scoreTitle = new H3("Score: " + correct + "/" + total + " (" + String.format("%.0f", percentage) + "%)");
        scoreTitle.addClassName("crossword-score-title");

        ProgressBar progressBar = new ProgressBar(0, total, correct);
        progressBar.addClassName("crossword-score-progress");

        String message;
        String messageClass;
        if (percentage == 100) {
            message = "Perfect! You solved the crossword!";
            messageClass = "crossword-message-perfect";
        } else if (percentage >= 80) {
            message = "Almost there! Great job!";
            messageClass = "crossword-message-great";
        } else if (percentage >= 50) {
            message = "Good progress! Keep going!";
            messageClass = "crossword-message-good";
        } else {
            message = "Keep trying! Check the clues again.";
            messageClass = "crossword-message-practice";
        }

        Span messageSpan = new Span(message);
        messageSpan.addClassName(messageClass);

        resultsDiv.add(scoreTitle, progressBar, messageSpan);
    }

    private void revealAnswers() {
        for (CrosswordWord word : placedWords) {
            for (int i = 0; i < word.word.length(); i++) {
                int r = word.horizontal ? word.row : word.row + i;
                int c = word.horizontal ? word.col + i : word.col;
                String cellId = r + "-" + c;

                TextField input = cellInputs.get(cellId);
                if (input != null) {
                    input.setValue(String.valueOf(word.word.charAt(i)));
                    input.addClassName("crossword-input-revealed");
                }
            }
        }
    }

    private void showError(String message) {
        gridContainer.removeAll();
        Div error = new Div();
        error.setText(message);
        error.addClassName("crossword-error");
        gridContainer.add(error);
    }

    private List<String> getSelectedLevels() {
        List<String> levels = new ArrayList<>();
        if (levelNA.getValue()) levels.add("N/A");
        if (levelA1.getValue()) levels.add("A1");
        if (levelA2.getValue()) levels.add("A2");
        if (levelB1.getValue()) levels.add("B1");
        if (levelB2.getValue()) levels.add("B2");
        if (levelC1.getValue()) levels.add("C1");
        if (levelC2.getValue()) levels.add("C2");
        return levels;
    }

    // Inner class for crossword word
    private static class CrosswordWord {
        String word;
        String clue;
        int row;
        int col;
        boolean horizontal;
        int number;

        CrosswordWord(String word, String clue) {
            this.word = word;
            this.clue = clue;
        }
    }
}
