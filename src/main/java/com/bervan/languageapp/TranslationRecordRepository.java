package com.bervan.languageapp;

import com.bervan.history.model.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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
            "AND t.markedForLearning IS TRUE " +
            "AND o.id = :ownerId " +
            "AND t.level IN :levels " +
            "ORDER BY t.sourceText ASC")
    List<TranslationRecord> getRecordsForLearning(
            @Param("dateTime") LocalDateTime dateTime,
            @Param("ownerId") UUID ownerId,
            @Param("levels") List<String> levels,
            Pageable pageable);

    @Query(value = """
                SELECT * FROM translation_record t
                JOIN translation_record_owners o ON t.id = o.translation_record_id
                WHERE (t.deleted IS FALSE OR t.deleted IS NULL)
                  AND t.marked_for_learning IS TRUE
                  AND o.owners_id = :ownerId
                  AND t.level IN (:levels)
                ORDER BY t.factor ASC
            """, nativeQuery = true)
    List<TranslationRecord> getRecordsForQuiz(
            @Param("ownerId") UUID ownerId,
            @Param("levels") List<String> levels,
            Pageable pageable);


    @Query("SELECT tr.id, tr.images FROM TranslationRecord tr WHERE tr.id IN (:ids)")
    List<Object[]> getImages(@Param("ids") List<UUID> ids);
}
