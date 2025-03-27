package com.bervan.languageapp;

import java.util.ArrayList;
import java.util.List;

public class Crossword {
    private final int width;
    private final int height;
    private final Cell[][] grid;
    private final List<CrosswordWord> words = new ArrayList<>();

    public Crossword(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Cell[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = new Cell();
            }
        }
    }

    public boolean tryAddWord(TranslationRecord word, int x, int y, boolean across) {
        String text = word.getSourceText().toUpperCase().replaceAll("[^A-Z]", "");
        if (text.isEmpty()) return false;

        // Check boundaries
        if (across) {
            if (x + text.length() > width) return false;
        } else {
            if (y + text.length() > height) return false;
        }

        // Check if the word fits
        for (int i = 0; i < text.length(); i++) {
            int cx = across ? x + i : x;
            int cy = across ? y : y + i;
            char c = text.charAt(i);

            if (grid[cx][cy].getLetter() != '\0' && grid[cx][cy].getLetter() != c) {
                return false;
            }
        }

        // Add the word
        for (int i = 0; i < text.length(); i++) {
            int cx = across ? x + i : x;
            int cy = across ? y : y + i;
            char c = text.charAt(i);

            grid[cx][cy].setLetter(c);
            if (across) {
                grid[cx][cy].setAcrossWord(word);
            } else {
                grid[cx][cy].setDownWord(word);
            }
        }

        words.add(new CrosswordWord(word, x, y, across));
        return true;
    }

    // Getters
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Cell[][] getGrid() {
        return grid;
    }

    public List<CrosswordWord> getWords() {
        return words;
    }
}