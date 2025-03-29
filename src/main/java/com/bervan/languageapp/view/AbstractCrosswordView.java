package com.bervan.languageapp.view;

import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.service.CrosswordService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.*;

public abstract class AbstractCrosswordView extends VerticalLayout {
    public static final String ROUTE_NAME = "learning-english-app/crossword";
    private final CrosswordService crosswordService;
    private final char EMPTY_CELL_CHAR = '-';

    private char[][] grid;
    private List<Word> words;
    private Grid<Word> wordGrid;
    private Div crosswordContainer;
    private int gridSize = 13;
    private Span messageLabel;
    private Map<String, TextField> inputFields = new HashMap<>();
    private Map<Integer, Integer> wordNumbers = new HashMap<>();

    public AbstractCrosswordView(CrosswordService crosswordService) {
        this.crosswordService = crosswordService;
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background-color", "#121212");

        messageLabel = new Span();
        messageLabel.getStyle().set("color", "#ffdb58");

        crosswordContainer = new Div();
        crosswordContainer.getStyle().set("margin-top", "20px");
        crosswordContainer.getStyle().set("border", "2px solid #3b82f6");
        crosswordContainer.getStyle().set("padding", "10px");
        crosswordContainer.getStyle().set("border-radius", "12px");

        wordGrid = new Grid<>(Word.class);
        wordGrid.setColumns("index", "word", "definition", "placed");
        wordGrid.getStyle().set("max-width", "400px");
        wordGrid.getStyle().set("margin-top", "20px");
        wordGrid.getStyle().set("background-color", "#1f2937");
        wordGrid.getStyle().set("color", "#ffffff");


        Button generateButton = new Button("Generate Crossword Puzzle", this::generateCrossword);
        generateButton.getStyle().set("margin-top", "20px");
        generateButton.getStyle().set("background-color", "#4caf50");
        generateButton.getStyle().set("color", "#ffffff");
        generateButton.getStyle().set("font-size", "16px");
        generateButton.getStyle().set("padding", "10px 20px");
        generateButton.getStyle().set("border-radius", "8px");
        generateButton.getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)");
        generateButton.addThemeVariants();

        Button checkButton = new Button("Check", this::checkAnswers);
        checkButton.getStyle().set("margin-top", "20px");
        checkButton.getStyle().set("background-color", "#007bff");
        checkButton.getStyle().set("color", "#ffffff");
        checkButton.getStyle().set("font-size", "16px");
        checkButton.getStyle().set("padding", "10px 20px");
        checkButton.getStyle().set("border-radius", "8px");
        checkButton.getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)");
        checkButton.addThemeVariants();

        add(messageLabel, crosswordContainer, wordGrid, generateButton, checkButton);
    }

    private void generateCrossword(ClickEvent event) {
        words = getWords();

        grid = new char[gridSize][gridSize];
        for (char[] row : grid) {
            Arrays.fill(row, EMPTY_CELL_CHAR);
        }

        wordNumbers.clear();
        if (solveCrossword()) {
            displayCrossword();
            for (int i = 0; i < words.size(); i++) {
                words.get(i).setIndex(i + 1);
            }
            wordGrid.setItems(words);
            messageLabel.setText("Crossword has been generated!");
        } else {
            messageLabel.setText("Crossword could not been generated.....");
            crosswordContainer.removeAll();
            wordGrid.setItems();
        }
    }

    private List<Word> getWords() {
        List<Word> res = new ArrayList<>();
        List<TranslationRecord> translationRecords = crosswordService.getCrosswordWords(gridSize, gridSize);
        for (TranslationRecord translationRecord : translationRecords) {
            res.add(new Word(translationRecord.getSourceText(), translationRecord.getTextTranslation()));
        }
        return res;
    }

    private boolean solveCrossword() {
        Collections.sort(words, (a, b) -> b.word.length() - a.word.length());
        List<Word> notPlacedWords = new ArrayList<>();
        int wordNumber = 1;

        for (int i = 0; i < words.size(); i++) {
            Word currentWord = words.get(i);
            boolean placed = false;

            for (int row = 0; row < gridSize && !placed; row++) {
                for (int col = 0; col < gridSize && !placed; col++) {
                    if (grid[row][col] == EMPTY_CELL_CHAR || grid[row][col] == currentWord.word.charAt(0)) {
                        if (canPlaceWord(currentWord, row, col, true)) {
                            placeWord(currentWord, row, col, true);
                            placed = true;
                            wordNumbers.put(wordNumber, row * gridSize + col);
                            wordNumber++;
                            break;
                        } else if (canPlaceWord(currentWord, row, col, false)) {
                            placeWord(currentWord, row, col, false);
                            placed = true;
                            wordNumbers.put(wordNumber, row * gridSize + col);
                            wordNumber++;
                            break;
                        }
                    }
                }
            }
            if (!placed) {
                notPlacedWords.add(currentWord);
//                for (Word word : placedWords) {
//                    removeWord(word);
//                }
//                return false;
            }
        }
        for (Word notPlacedWord : notPlacedWords) {
            words.remove(notPlacedWord);
        }
        return true;
    }

    private boolean canPlaceWord(Word word, int row, int col, boolean horizontal) {
        if (horizontal) {
            if (col + word.word.length() > gridSize) return false;
            for (int i = 0; i < word.word.length(); i++) {
                if (grid[row][col + i] != EMPTY_CELL_CHAR && grid[row][col + i] != word.word.charAt(i)) {
                    return false;
                }
            }
        } else {
            if (row + word.word.length() > gridSize) return false;
            for (int i = 0; i < word.word.length(); i++) {
                if (grid[row + i][col] != EMPTY_CELL_CHAR && grid[row + i][col] != word.word.charAt(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void placeWord(Word word, int row, int col, boolean horizontal) {
        if (horizontal) {
            for (int i = 0; i < word.word.length(); i++) {
                grid[row][col + i] = word.word.charAt(i);
            }
            word.x = col;
            word.y = row;
            word.isHorizontal = true;
        } else {
            for (int i = 0; i < word.word.length(); i++) {
                grid[row + i][col] = word.word.charAt(i);
            }
            word.x = col;
            word.y = row;
            word.isHorizontal = false;
        }
    }

    private void removeWord(Word word) {
        if (word.isHorizontal) {
            for (int i = 0; i < word.word.length(); i++) {
                grid[word.y][word.x + i] = EMPTY_CELL_CHAR;
            }
        } else {
            for (int i = 0; i < word.word.length(); i++) {
                grid[word.y + i][word.x] = EMPTY_CELL_CHAR;
            }
        }
    }

    private void displayCrossword() {
        crosswordContainer.removeAll();
        inputFields.clear();
        Div gridContainer = new Div();
        gridContainer.getStyle().set("display", "grid");
        gridContainer.getStyle().set("grid-template-columns", "repeat(" + gridSize + ", 30px)");
        gridContainer.getStyle().set("gap", "2px");
        gridContainer.getStyle().set("padding", "10px");
        gridContainer.getStyle().set("border", "2px solid #3b82f6");
        gridContainer.getStyle().set("border-radius", "12px");
        gridContainer.getStyle().set("background-color", "#0f172a");

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Div cell = new Div();
                cell.getStyle().set("position", "relative");

                String cellId = row + "-" + col;

                if (grid[row][col] != EMPTY_CELL_CHAR) {
                    TextField inputField = new TextField();
                    inputField.setValue("");
                    inputField.setMaxLength(1);
                    inputField.setWidth("30px");
                    inputField.setHeight("30px");
                    inputField.getStyle().set("font-size", "16px");
                    inputField.getStyle().set("text-transform", "uppercase");
                    inputField.getStyle().set("text-align", "center");
                    inputField.getStyle().set("padding", "0");
                    inputField.getStyle().set("border-radius", "4px");
                    inputField.getStyle().set("background-color", "#ffffff");
                    inputField.getStyle().set("color", "#000000");

                    int position = row * gridSize + col;
                    for (Map.Entry<Integer, Integer> entry : wordNumbers.entrySet()) {
                        if (entry.getValue() == position) {
                            Span numberLabel = new Span(String.valueOf(entry.getKey()));
                            numberLabel.getStyle().set("position", "absolute");
                            numberLabel.getStyle().set("top", "2px");
                            numberLabel.getStyle().set("left", "2px");
                            numberLabel.getStyle().set("z-index", "500");
                            numberLabel.getStyle().set("font-size", "10px");
                            numberLabel.getStyle().set("color", "#ffdb58");
                            cell.add(numberLabel);
                            break;
                        }
                    }

                    inputFields.put(cellId, inputField);
                    cell.add(inputField);
                } else {
                    cell.setText("");
                    cell.getStyle().set("width", "30px");
                    cell.getStyle().set("height", "30px");
                    cell.getStyle().set("display", "flex");
                    cell.getStyle().set("align-items", "center");
                    cell.getStyle().set("justify-content", "center");
                    cell.getStyle().set("border", "1px solid #4b5563");
                    cell.getStyle().set("font-size", "16px");
                    cell.getStyle().set("color", "#ffffff");
                    cell.getStyle().set("background-color", "#1f2937");
                    cell.getStyle().set("border-radius", "4px");
                }
                gridContainer.add(cell);
            }
        }
        crosswordContainer.add(gridContainer);
    }

    private void checkAnswers(ClickEvent event) {
        int correctAnswers = 0;
        int totalLetters = 0;
        StringBuilder resultMessage = new StringBuilder();

        for (Word word : words) {
            totalLetters += word.word.length();
            for (int i = 0; i < word.word.length(); i++) {
                int row = word.y;
                int col = word.x;
                if (word.isHorizontal) {
                    col += i;
                } else {
                    row += i;
                }
                String cellId = row + "-" + col;
                TextField inputField = inputFields.get(cellId);
                if (inputField != null && inputField.getValue() != null && inputField.getValue().length() == 1 && inputField.getValue().toUpperCase().charAt(0) == grid[row][col]) {
                    correctAnswers++;
                } else if (inputField != null && inputField.getValue() != null && inputField.getValue().length() == 0 && grid[row][col] == ' ') { //space
                    correctAnswers++;
                }
            }
        }

        resultMessage.append("Correct answers: ").append(correctAnswers).append(" - ").append(totalLetters).append(" letters.\n");
        if (correctAnswers == totalLetters) {
            resultMessage.append("GZ! You won the game!");
            messageLabel.setText(resultMessage.toString());
        } else {
            resultMessage.append("Check again.");
            messageLabel.setText(resultMessage.toString());
        }
    }

    public static class Word {
        int index;
        String word;
        String definition;
        int x;
        int y;
        boolean isHorizontal;

        public Word(String word, String definition) {
            this.word = word;
            this.definition = definition;
        }

        public String getPlaced() {
            if (isHorizontal) {
                return "Horizontal";
            } else {
                return "Vertical";
            }
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public String getWord() {
            return word;
        }

        public String getDefinition() {
            return definition;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public boolean isHorizontal() {
            return isHorizontal;
        }
    }
}
