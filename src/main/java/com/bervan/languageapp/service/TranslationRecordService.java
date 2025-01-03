package com.bervan.languageapp.service;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.AuthService;
import com.bervan.common.service.BaseService;
import com.bervan.ieentities.ExcelIEEntity;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.TranslationRecordRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TranslationRecordService extends BaseService<UUID, TranslationRecord> {
    private final TranslationRecordRepository repository;

    public TranslationRecordService(TranslationRecordRepository repository,
                                    SearchService searchService) {
        super(repository, searchService);
        this.repository = repository;
    }

    public TranslationRecord save(TranslationRecord record) {
        setLevel(record);
        return repository.save(record);
    }

    private void setLevel(TranslationRecord record) {
        if (record.getLevel() == null || record.getLevel().isBlank()) {
            String level = autoDetermineLevel(record.getSourceText());
            if (level != null) {
                record.setLevel("N/A");
            }
        }
    }

    private String autoDetermineLevel(String sourceText) {
        // TODO: 28/12/2024
        return "N/A";
    }

    @PostFilter("(T(com.bervan.common.service.AuthService).hasAccess(filterObject.owners))")
    public Set<TranslationRecord> load() {
        return repository.findAll(AuthService.getLoggedUserId());
    }

    @PostFilter("(T(com.bervan.common.service.AuthService).hasAccess(filterObject.owners))")
    public List<TranslationRecord> getAllForLearning(List<String> levels, Pageable pageable) {
        return repository.getRecordsForLearning(LocalDateTime.now(), AuthService.getLoggedUserId(), levels, pageable);
    }

    public void updateNextLearningDate(UUID uuid, String score) {
        TranslationRecord translationRecord = repository.findById(uuid).get();

        if (translationRecord.getFactor() == null || translationRecord.getFactor() < 1) {
            translationRecord.setFactor(1);
        }

        int nextFactor = getNextFactor(score, translationRecord.getFactor());
        translationRecord.setFactor(nextFactor);
        translationRecord.setNextRepeatTime(
                getNextRepeatTime(nextFactor, score)
        );
        repository.save(translationRecord);
    }

    public static LocalDateTime getNextRepeatTime(Integer factor, String score) {
        return LocalDateTime.now().plusHours(getHoursUntilNextRepeatTime(factor, score));
    }

    public static int getHoursUntilNextRepeatTime(Integer factor, String score) {
        if (score.equals("AGAIN")) {
            return 0; //again means that card will appear in the same learning session
        }

        if (factor >= 50) {
            return 200 + factor;
        }

        return factor * 4;
    }

    public static int getNextFactor(String score, Integer factor) {
        if (factor == null) {
            return 1;
        }

        if (factor < 1) {
            factor = 1;
        }

        return switch (score) {
            //again means that you forgot or its new word, when you spent 3 months learning this word
            //you don't want to reset your progress to 1.
            case "AGAIN" -> Math.min(1, (int) (factor / 2.0));
            case "HARD" -> (int) Math.max(1, factor * 0.6);
            case "GOOD" -> factor * 2;
            case "EASY" -> factor * 4;
            default -> throw new IllegalArgumentException("Invalid grade");
        };
    }

    public void delete(TranslationRecord record) {
        record.setDeleted(true);
        save(record);
    }

    public void delete(UUID uuid) {
        TranslationRecord translationRecord = repository.findById(uuid).get();
        translationRecord.setDeleted(true);
        repository.save(translationRecord);
    }

    public Integer getFactor(UUID uuid) {
        TranslationRecord translationRecord = repository.findById(uuid).get();
        return translationRecord.getFactor();
    }

    @Override
    public void save(List<TranslationRecord> data) {
        for (TranslationRecord datum : data) {
            save(datum);
        }
    }
}
