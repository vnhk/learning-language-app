package com.bervan.languageapp.service;

import com.bervan.common.service.AuthService;
import com.bervan.common.service.BaseService;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.TranslationRecordRepository;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TranslationRecordService implements BaseService<UUID, TranslationRecord> {
    private final TranslationRecordRepository translationRecordRepository;

    public TranslationRecordService(TranslationRecordRepository translationRecordRepository) {
        this.translationRecordRepository = translationRecordRepository;
    }

    public TranslationRecord save(TranslationRecord record) {
        return translationRecordRepository.save(record);
    }

    @PostFilter("filterObject.owner != null && filterObject.owner.getId().equals(T(com.bervan.common.service.AuthService).getLoggedUserId())")
    public Set<TranslationRecord> load() {
        return translationRecordRepository.findAllByDeletedIsFalseOrDeletedIsNullAndOwnerId(AuthService.getLoggedUserId());
    }

    @PostFilter("filterObject.owner != null && filterObject.owner.getId().equals(T(com.bervan.common.service.AuthService).getLoggedUserId())")
    public Set<TranslationRecord> getAllForLearning() {
        return translationRecordRepository.findAllByDeletedIsFalseOrDeletedIsNullAndNextRepeatTimeNullOrNextRepeatTimeBeforeAndOwnerId(LocalDateTime.now(), AuthService.getLoggedUserId());
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
        if (factor == null) {
            return 1;
        }
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

    @Override
    public void save(List<TranslationRecord> data) {
        for (TranslationRecord datum : data) {
            save(datum);
        }
    }
}
