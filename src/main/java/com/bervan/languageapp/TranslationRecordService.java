package com.bervan.languageapp;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TranslationRecordService {
    private final TranslationRecordFileRepository translationRecordRepository;

    public TranslationRecordService(TranslationRecordFileRepository translationRecordRepository) {
        this.translationRecordRepository = translationRecordRepository;
    }

    public TranslationRecord add(TranslationRecord record) {
        TranslationRecord add = translationRecordRepository.add(record);
        saveAll();
        return add;
    }

    public List<TranslationRecord> getAll() {
        return translationRecordRepository.getAll();
    }

    public void delete(TranslationRecord record) {
        translationRecordRepository.delete(record);
        saveAll();
        loadAll();
    }

    public void saveAll() {
        translationRecordRepository.saveAll();
    }

    public void loadAll() {
        translationRecordRepository.loadAll();
    }
}
