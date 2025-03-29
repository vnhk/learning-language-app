package com.bervan.languageapp.view;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.*;

public abstract class AbstractCrosswordView extends VerticalLayout {
    public static final String ROUTE_NAME = "learning-english-app/crossword";
    private char[][] grid;
    private List<Word> words;
    private Grid<Word> wordGrid;
    private Div crosswordContainer;
    private int gridSize = 13;
    private Text messageLabel;
    private Map<String, TextField> inputFields = new HashMap<>();

    public AbstractCrosswordView() {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background-color", "#121212");

        messageLabel = new Text("");

        crosswordContainer = new Div();
        crosswordContainer.getStyle().set("margin-top", "20px");
        crosswordContainer.getStyle().set("border", "2px solid #3b82f6");
        crosswordContainer.getStyle().set("padding", "10px");
        crosswordContainer.getStyle().set("border-radius", "12px");

        wordGrid = new Grid<>(Word.class);
        wordGrid.setColumns("word", "definition");
        wordGrid.getStyle().set("max-width", "400px");
        wordGrid.getStyle().set("margin-top", "20px");
        wordGrid.getStyle().set("background-color", "#1f2937");
        wordGrid.getStyle().set("color", "#ffffff");
//        wordGrid.getHeaderRows().forEach(row ->
//                row.getCells().forEach(cell ->
//                        cell.getElement().getStyle().set("color", "#ffdb58")
//                )
//        );

        Button generateButton = new Button("Generuj Krzyżówkę", this::generateCrossword);
        generateButton.getStyle().set("margin-top", "20px");
        generateButton.getStyle().set("background-color", "#4caf50");
        generateButton.getStyle().set("color", "#ffffff");
        generateButton.getStyle().set("font-size", "16px");
        generateButton.getStyle().set("padding", "10px 20px");
        generateButton.getStyle().set("border-radius", "8px");
        generateButton.getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)");
        generateButton.addThemeVariants();

        Button checkButton = new Button("Sprawdź", this::checkAnswers); // Dodaj przycisk "Sprawdź"
        checkButton.getStyle().set("margin-top", "20px");
        checkButton.getStyle().set("background-color", "#007bff");
        checkButton.getStyle().set("color", "#ffffff");
        checkButton.getStyle().set("font-size", "16px");
        checkButton.getStyle().set("padding", "10px 20px");
        checkButton.getStyle().set("border-radius", "8px");
        checkButton.getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)");
        checkButton.addThemeVariants();

        add(messageLabel, crosswordContainer, wordGrid, generateButton, checkButton); // Dodaj przycisk do layoutu
    }

    private void generateCrossword(ClickEvent event) {
        words = new ArrayList<>();
        words.add(new Word("JAVA", "Język programowania"));
        words.add(new Word("SPRING", "Framework Spring Boot"));
        words.add(new Word("VAADIN", "Framework Vaadin"));
        words.add(new Word("KOTLIN", "Język programowania"));
        words.add(new Word("GROOVY", "Język skryptowy"));
        words.add(new Word("SCALA", "Język programowania"));
        words.add(new Word("CLOJURE", "Dialekt Lispa"));
        words.add(new Word("GRADLE", "System budowania"));
        words.add(new Word("MAVEN", "Narzędzie do zarządzania projektami"));
        words.add(new Word("HIBERNATE", "ORM Framework"));
        words.add(new Word("THYMELEAF", "Silnik szablonów"));
        words.add(new Word("SECURITY", "Moduł bezpieczeństwa Spring"));
        words.add(new Word("REST", "Architektura oprogramowania"));
        words.add(new Word("API", "Interfejs programowania aplikacji"));
        words.add(new Word("DATABASE", "Baza danych"));
        words.add(new Word("TRANSACTION", "Jednostka pracy w DB"));
        words.add(new Word("QUERY", "Zapytanie do bazy danych"));
        words.add(new Word("SERVER", "Komputer udostępniający zasoby"));
        words.add(new Word("CLIENT", "Aplikacja korzystająca z zasobów serwera"));
        words.add(new Word("THREAD", "Wątek wykonania"));

        // Inicjalizacja i generowanie krzyżówki
        grid = new char[gridSize][gridSize];
        for (char[] row : grid) {
            Arrays.fill(row, ' ');
        }

        if (solveCrossword()) {
            displayCrossword();
            wordGrid.setItems(words);
            messageLabel.setText("Krzyżówka została wygenerowana! Wpisz słowa i kliknij 'Sprawdź'.");
        } else {
            messageLabel.setText("Nie udało się wygenerować krzyżówki z podanych słów.");
            crosswordContainer.removeAll();
            wordGrid.setItems();
        }
    }

    private boolean solveCrossword() {
        Collections.sort(words, (a, b) -> b.word.length() - a.word.length());
        List<Word> placedWords = new ArrayList<>();

        for (int i = 0; i < words.size(); i++) {
            Word currentWord = words.get(i);
            boolean placed = false;

            for (int row = 0; row < gridSize && !placed; row++) {
                for (int col = 0; col < gridSize && !placed; col++) {
                    if (grid[row][col] == ' ' || grid[row][col] == currentWord.word.charAt(0)) {
                        if (canPlaceWord(currentWord, row, col, true)) {
                            placeWord(currentWord, row, col, true);
                            placed = true;
                            placedWords.add(currentWord);
                            break;
                        } else if (canPlaceWord(currentWord, row, col, false)) {
                            placeWord(currentWord, row, col, false);
                            placed = true;
                            placedWords.add(currentWord);
                            break;
                        }
                    }
                }
            }
            if (!placed) {
                // Jeśli nie udało się umieścić słowa, zresetuj planszę i spróbuj od nowa
                for (Word word : placedWords) {
                    removeWord(word);
                }
                return false;
            }
        }
        return true;
    }

    private boolean canPlaceWord(Word word, int row, int col, boolean horizontal) {
        if (horizontal) {
            if (col + word.word.length() > gridSize) return false;
            for (int i = 0; i < word.word.length(); i++) {
                if (grid[row][col + i] != ' ' && grid[row][col + i] != word.word.charAt(i)) {
                    return false;
                }
            }
        } else {
            if (row + word.word.length() > gridSize) return false;
            for (int i = 0; i < word.word.length(); i++) {
                if (grid[row + i][col] != ' ' && grid[row + i][col] != word.word.charAt(i)) {
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
                grid[word.y][word.x + i] = ' ';
            }
        } else {
            for (int i = 0; i < word.word.length(); i++) {
                grid[word.y + i][word.x] = ' ';
            }
        }
    }

    private void displayCrossword() {
        crosswordContainer.removeAll();
        inputFields.clear(); // Wyczyść mapę pól tekstowych
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
                String cellId = row + "-" + col; // Unikalny identyfikator dla każdej komórki

                if (grid[row][col] != ' ') { // Jeśli to litera słowa, dodaj pole tekstowe
                    TextField inputField = new TextField();
                    inputField.setValue("");  // Ustaw puste pole na początku
                    inputField.setMaxLength(1); // Ogranicz do jednego znaku
                    inputField.setWidth("30px");
                    inputField.setHeight("30px");
                    inputField.getStyle().set("font-size", "16px");
                    inputField.getStyle().set("text-transform", "uppercase"); // Automatyczna konwersja na duże litery
                    inputField.getStyle().set("text-align", "center");
                    inputField.getStyle().set("padding", "0");
                    inputField.getStyle().set("border-radius", "4px");
                    inputField.getStyle().set("background-color", "#ffffff");
                    inputField.getStyle().set("color", "#000000");

                    inputFields.put(cellId, inputField); // Dodaj do mapy
                    cell.add(inputField);
                } else { // W przeciwnym razie wyświetl puste pole
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
                }
            }
        }

        resultMessage.append("Poprawne odpowiedzi: ").append(correctAnswers).append(" z ").append(totalLetters).append(" liter.\n");
        if (correctAnswers == totalLetters) {
            resultMessage.append("Gratulacje! Rozwiązałeś/aś krzyżówkę!");
            messageLabel.setText(resultMessage.toString());
        } else {
            resultMessage.append("Spróbuj ponownie.");
            messageLabel.setText(resultMessage.toString());
        }
    }


    public static class Word {
        String word;
        String definition;
        int x;
        int y;
        boolean isHorizontal;

        public Word(String word, String definition) {
            this.word = word;
            this.definition = definition;
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