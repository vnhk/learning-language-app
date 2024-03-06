package com.bervan.languageapp;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TranslationRecordService {
    private final TranslationRecordRepository translationRecordRepository;

    public TranslationRecordService(TranslationRecordRepository translationRecordRepository) {
        this.translationRecordRepository = translationRecordRepository;
    }

    public TranslationRecord add(TranslationRecord record) {
        return translationRecordRepository.save(record);
    }

    public List<TranslationRecord> getAll() {
        return translationRecordRepository.findAll();
    }

    public void delete(TranslationRecord record) {
        translationRecordRepository.delete(record);
    }

    public void delete(UUID uuid) {
        translationRecordRepository.deleteById(uuid);
    }
}
