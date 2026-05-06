package com.bervan.languageapp.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CrosswordWordDto {
    private String word;
    private String clue;
    private int row;
    private int col;
    private boolean horizontal;
    private int number;
}
