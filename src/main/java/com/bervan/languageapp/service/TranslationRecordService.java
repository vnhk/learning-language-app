package com.bervan.languageapp.service;

import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.TranslationRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public List<TranslationRecord> getAllForLearning() {
        List<TranslationRecord> all = translationRecordRepository.findAllByDeletedIsFalseOrDeletedIsNullAndNextRepeatTimeNullOrNextRepeatTimeBefore(LocalDateTime.now());
        all.sort(Comparator.comparing(TranslationRecord::getSourceText));
        return all;
    }

    public void updateNextLearningDate(String uuid, String score) {
        TranslationRecord translationRecord = translationRecordRepository.findById(UUID.fromString(uuid)).get();

        if (translationRecord.getFactor() == null || translationRecord.getFactor() < 1) {
            translationRecord.setFactor(1);
        }

        switch (score) {
            case "AGAIN":
                translationRecord.setFactor(1);
                break;
            case "HARD":
                translationRecord.setFactor((int) Math.max(1, translationRecord.getFactor() * 0.6));
                break;
            case "GOOD":
                translationRecord.setFactor(translationRecord.getFactor() * 2);
                break;
            case "EASY":
                translationRecord.setFactor(translationRecord.getFactor() * 4);
                break;
            default:
                throw new IllegalArgumentException("Invalid grade");
        }
        translationRecord.setNextRepeatTime(
                LocalDateTime.now().plusHours(translationRecord.getFactor() * 4)
        );
        translationRecordRepository.save(translationRecord);
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
