package com.bervan.languageapp;

public class CrosswordWord {
    private final TranslationRecord word;
    private final int x;
    private final int y;
    private final boolean across;

    public CrosswordWord(TranslationRecord word, int x, int y, boolean across) {
        this.word = word;
        this.x = x;
        this.y = y;
        this.across = across;
    }

    public TranslationRecord getWord() {
        return word;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isAcross() {
        return across;
    }
}