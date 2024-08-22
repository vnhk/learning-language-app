package com.bervan.languageapp;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Repository;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class TranslationRecordFileRepository {
    private String path = "src/main/resources/db";
    private String fileName = "translation-base.xlsx";
    private List<TranslationRecord> translations = new ArrayList<>();
    private final String SHEET_NAME = "Translations";

    public TranslationRecord add(TranslationRecord translationRecord) {
        translations.add(translationRecord);
        return translationRecord;
    }

    public void delete(TranslationRecord translationRecord) {
        translations.remove(translationRecord);
    }

    public List<TranslationRecord> getAll() {
        List<TranslationRecord> newList = new ArrayList<>();
        for (TranslationRecord translation : translations) {
            newList.add(new TranslationRecord(translation));
        }
        return newList;
    }

    public void saveAll() {
        Workbook workbook = new HSSFWorkbook();
        try (FileOutputStream outputStream = new FileOutputStream(Path.of(path, fileName).toFile())) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);
            createHeaders(sheet);
            for (TranslationRecord translation : translations) {
                createRow(sheet, translation);
            }

            workbook.write(outputStream);
            workbook.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save to excel file!", e);
        }
    }

    private void createRow(Sheet sheet, TranslationRecord translation) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue(translation.getSourceText());
        row.createCell(1).setCellValue(translation.getTextTranslation());
        row.createCell(2).setCellValue(translation.getInSentence());
        row.createCell(3).setCellValue(translation.getInSentenceTranslation());
//        row.createCell(4).setCellValue(translation.getTextPronunciationPath());
//        row.createCell(5).setCellValue(translation.getInSentencePronunciationPath());
        row.createCell(6).setCellValue(String.valueOf(translation.getId()));
    }

    private void createHeaders(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        Cell textCell = headerRow.createCell(0);
        textCell.setCellValue("Text");

        Cell transactionCell = headerRow.createCell(1);
        transactionCell.setCellValue("Translation");

        Cell inSentenceText = headerRow.createCell(2);
        inSentenceText.setCellValue("In Sentence");

        Cell inSentenceTranslation = headerRow.createCell(3);
        inSentenceTranslation.setCellValue("In Sentence Translation");

        Cell translationSoundPath = headerRow.createCell(4);
        translationSoundPath.setCellValue("Translation Sound File");

        Cell inSentenceTranslationSoundPath = headerRow.createCell(5);
        inSentenceTranslationSoundPath.setCellValue("In Sentence Translation Sound File");

        Cell uuid = headerRow.createCell(6);
        uuid.setCellValue("UUID");
    }

    public void loadAll() {
        translations = new ArrayList<>();
        try (Workbook workbook = new HSSFWorkbook(new FileInputStream(Path.of(path, fileName).toFile()))) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            int lastRowNum = sheet.getLastRowNum();
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                TranslationRecord t = TranslationRecord.TranslationRecordBuilder.aTranslationRecord()
                        .withSourceText(row.getCell(0).getStringCellValue())
                        .withTextTranslation(row.getCell(1).getStringCellValue())
                        .withInSentence(row.getCell(2).getStringCellValue())
                        .withInSentenceTranslation(row.getCell(3).getStringCellValue())
//                        .textPronunciationPath(row.getCell(4).getStringCellValue())
//                        .inSentencePronunciationPath(row.getCell(5).getStringCellValue())
                        .withId(getUUID(row))
                        .build();
                translations.add(t);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save to excel file!", e);
        }
    }

    private static UUID getUUID(Row row) {
        String stringCellValue = row.getCell(6).getStringCellValue();
        if (stringCellValue == null || stringCellValue.equals("null")) {
            return UUID.randomUUID();
        }
        return UUID.fromString(stringCellValue);
    }
}
