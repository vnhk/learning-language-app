package com.bervan.languageapp;

public class Cell {
    private char letter = '\0';
    private TranslationRecord acrossWord;
    private TranslationRecord downWord;

    public char getLetter() {
        return letter;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }

    public TranslationRecord getAcrossWord() {
        return acrossWord;
    }

    public void setAcrossWord(TranslationRecord acrossWord) {
        this.acrossWord = acrossWord;
    }

    public TranslationRecord getDownWord() {
        return downWord;
    }

    public void setDownWord(TranslationRecord downWord) {
        this.downWord = downWord;
    }

    public boolean isEmpty() {
        return letter == '\0';
    }
}