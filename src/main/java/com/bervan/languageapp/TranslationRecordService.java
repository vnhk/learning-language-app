package com.bervan.languageapp;

import org.springframework.stereotype.Service;

import java.util.Comparator;
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
        List<TranslationRecord> all = translationRecordRepository.findAllByDeletedIsFalseOrDeletedIsNull();
        all.sort(Comparator.comparing(TranslationRecord::getSourceText));
        return all;
    }

    public void delete(TranslationRecord record) {
        record.setDeleted(true);
        translationRecordRepository.save(record);
    }

    public void delete(UUID uuid) {
        TranslationRecord translationRecord = translationRecordRepository.findById(uuid).get();
        translationRecord.setDeleted(true);
        translationRecordRepository.save(translationRecord);
    }
}
