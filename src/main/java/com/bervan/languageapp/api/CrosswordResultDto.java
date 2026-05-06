package com.bervan.languageapp.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CrosswordResultDto {
    private char[][] grid;
    private List<CrosswordWordDto> words;
}
