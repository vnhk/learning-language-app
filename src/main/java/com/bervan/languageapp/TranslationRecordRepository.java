package com.bervan.languageapp;

import com.bervan.history.model.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TranslationRecordRepository extends BaseRepository<TranslationRecord, UUID> {
    @Query("SELECT t FROM TranslationRecord t " +
            "JOIN t.owners o " +
            "WHERE (t.deleted IS FALSE OR t.deleted IS NULL) " +
            "AND o.id = :ownerId")
    Set<TranslationRecord> findAll(UUID ownerId);

    @Query("SELECT t FROM TranslationRecord t " +
            "JOIN t.owners o " +
            "WHERE (t.deleted IS FALSE OR t.deleted IS NULL) " +
            "AND (t.nextRepeatTime IS NULL OR t.nextRepeatTime < :dateTime) " +
            "AND o.id = :ownerId")
    Set<TranslationRecord> getRecordsForLearning(
            @Param("dateTime") LocalDateTime dateTime,
            @Param("ownerId") UUID ownerId);
}
