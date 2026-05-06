package com.bervan.languageapp.service;

import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.api.CrosswordResultDto;
import com.bervan.languageapp.api.CrosswordWordDto;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CrosswordService {

    private static final int GRID_SIZE = 15;
    private static final int MAX_WORDS = 12;
    private static final char EMPTY = '\0';

    private final TranslationRecordService translationRecordService;

    public CrosswordService(TranslationRecordService translationRecordService) {
        this.translationRecordService = translationRecordService;
    }

    public CrosswordResultDto generate(String language, List<String> levels) {
        List<TranslationRecord> records = translationRecordService.getRecordsForQuiz(
                language, levels, Pageable.ofSize(200)
        );

        List<Word> candidates = new ArrayList<>();
        for (TranslationRecord record : records) {
            String word = cleanWord(record.getSourceText());
            if (word.length() >= 3 && word.length() <= GRID_SIZE - 2) {
                candidates.add(new Word(word, record.getTextTranslation()));
            }
        }

        if (candidates.size() < 3) {
            return null;
        }

        char[][] grid = new char[GRID_SIZE][GRID_SIZE];
        List<Word> placedWords = new ArrayList<>();

        Collections.shuffle(candidates);
        candidates.sort((a, b) -> b.word.length() - a.word.length());

        Word firstWord = candidates.get(0);
        int startRow = GRID_SIZE / 2;
        int startCol = (GRID_SIZE - firstWord.word.length()) / 2;
        placeWord(grid, firstWord, startRow, startCol, true);
        placedWords.add(firstWord);

        for (int i = 1; i < candidates.size() && placedWords.size() < MAX_WORDS; i++) {
            Word word = candidates.get(i);
            if (tryPlaceWord(grid, placedWords, word)) {
                placedWords.add(word);
            }
        }

        if (placedWords.size() < 3) {
            return null;
        }

        assignWordNumbers(placedWords);

        List<CrosswordWordDto> wordDtos = placedWords.stream()
                .map(w -> new CrosswordWordDto(w.word, w.clue, w.row, w.col, w.horizontal, w.number))
                .toList();

        return new CrosswordResultDto(grid, wordDtos);
    }

    private String cleanWord(String text) {
        return text.replaceAll("[^a-zA-ZąćęłńóśźżĄĆĘŁŃÓŚŹŻáéíóúüñÁÉÍÓÚÜÑ]", "")
                .toUpperCase()
                .trim();
    }

    private boolean tryPlaceWord(char[][] grid, List<Word> placedWords, Word word) {
        for (Word placed : placedWords) {
            for (int i = 0; i < word.word.length(); i++) {
                for (int j = 0; j < placed.word.length(); j++) {
                    if (word.word.charAt(i) == placed.word.charAt(j)) {
                        int newRow, newCol;
                        boolean horizontal = !placed.horizontal;

                        if (placed.horizontal) {
                            newRow = placed.row - i;
                            newCol = placed.col + j;
                        } else {
                            newRow = placed.row + j;
                            newCol = placed.col - i;
                        }

                        if (canPlace(grid, word, newRow, newCol, horizontal)) {
                            placeWord(grid, word, newRow, newCol, horizontal);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean canPlace(char[][] grid, Word word, int row, int col, boolean horizontal) {
        int len = word.word.length();

        if (row < 0 || col < 0) return false;
        if (horizontal && col + len > GRID_SIZE) return false;
        if (!horizontal && row + len > GRID_SIZE) return false;

        for (int i = 0; i < len; i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;

            char existing = grid[r][c];
            char needed = word.word.charAt(i);

            if (existing != EMPTY && existing != needed) return false;

            if (existing == EMPTY) {
                if (horizontal) {
                    if (r > 0 && grid[r - 1][c] != EMPTY && !isIntersection(grid, r - 1, c, r, c)) return false;
                    if (r < GRID_SIZE - 1 && grid[r + 1][c] != EMPTY && !isIntersection(grid, r + 1, c, r, c)) return false;
                } else {
                    if (c > 0 && grid[r][c - 1] != EMPTY && !isIntersection(grid, r, c - 1, r, c)) return false;
                    if (c < GRID_SIZE - 1 && grid[r][c + 1] != EMPTY && !isIntersection(grid, r, c + 1, r, c)) return false;
                }
            }
        }

        if (horizontal && col > 0 && grid[row][col - 1] != EMPTY) return false;
        if (!horizontal && row > 0 && grid[row - 1][col] != EMPTY) return false;
        if (horizontal && col + len < GRID_SIZE && grid[row][col + len] != EMPTY) return false;
        if (!horizontal && row + len < GRID_SIZE && grid[row + len][col] != EMPTY) return false;

        return true;
    }

    private boolean isIntersection(char[][] grid, int r1, int c1, int r2, int c2) {
        return grid[r1][c1] != EMPTY && grid[r2][c2] != EMPTY;
    }

    private void placeWord(char[][] grid, Word word, int row, int col, boolean horizontal) {
        word.row = row;
        word.col = col;
        word.horizontal = horizontal;

        for (int i = 0; i < word.word.length(); i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;
            grid[r][c] = word.word.charAt(i);
        }
    }

    private void assignWordNumbers(List<Word> placedWords) {
        placedWords.sort((a, b) -> a.row != b.row ? a.row - b.row : a.col - b.col);

        Map<String, Integer> positionNumbers = new HashMap<>();
        int number = 1;

        for (Word word : placedWords) {
            String posKey = word.row + "-" + word.col;
            if (!positionNumbers.containsKey(posKey)) {
                positionNumbers.put(posKey, number++);
            }
            word.number = positionNumbers.get(posKey);
        }
    }

    private static class Word {
        String word;
        String clue;
        int row;
        int col;
        boolean horizontal;
        int number;

        Word(String word, String clue) {
            this.word = word;
            this.clue = clue;
        }
    }
}
