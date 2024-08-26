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

    public void updateNextLearningDate(UUID uuid, String score) {
        TranslationRecord translationRecord = translationRecordRepository.findById(uuid).get();

        if (translationRecord.getFactor() == null || translationRecord.getFactor() < 1) {
            translationRecord.setFactor(1);
        }

        int nextFactor = getNextFactor(score, translationRecord.getFactor());
        translationRecord.setFactor(nextFactor);
        translationRecord.setNextRepeatTime(
                getNextRepeatTime(nextFactor)
        );
        translationRecordRepository.save(translationRecord);
    }

    public static LocalDateTime getNextRepeatTime(Integer factor) {
        return LocalDateTime.now().plusHours(getHoursUntilNextRepeatTime(factor));
    }

    public static int getHoursUntilNextRepeatTime(Integer factor) {
        return factor * 4;
    }

    public static int getNextFactor(String score, Integer factor) {
        switch (score) {
            case "AGAIN":
                return 1;
            case "HARD":
                return (int) Math.max(1, factor * 0.6);
            case "GOOD":
                return factor * 2;
            case "EASY":
                return factor * 4;
            default:
                throw new IllegalArgumentException("Invalid grade");
        }
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

    public Integer getFactor(UUID uuid) {
        TranslationRecord translationRecord = translationRecordRepository.findById(uuid).get();
        return translationRecord.getFactor();
    }
}
