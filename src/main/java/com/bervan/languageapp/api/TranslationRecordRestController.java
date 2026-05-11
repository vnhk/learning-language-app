package com.bervan.languageapp.api;

import com.bervan.common.config.EntityConfigValidator;
import com.bervan.common.controller.BaseOwnedController;
import com.bervan.common.controller.BaseOwnedController.ImportResult;
import com.bervan.common.mapper.BervanDTOMapper;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.service.CrosswordService;
import com.bervan.languageapp.service.TranslationRecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/language-learning/words")
public class TranslationRecordRestController extends BaseOwnedController<TranslationRecord, UUID> {

    private final TranslationRecordService translationRecordService;
    private final CrosswordService crosswordService;

    protected TranslationRecordRestController(TranslationRecordService translationRecordService,
                                              CrosswordService crosswordService,
                                              BervanDTOMapper mapper,
                                              EntityConfigValidator validator) {
        super(translationRecordService, mapper, validator, "TranslationRecord");
        this.translationRecordService = translationRecordService;
        this.crosswordService = crosswordService;
    }

    @GetMapping
    public ResponseEntity<Page<TranslationRecordDto>> list(
            @RequestParam MultiValueMap<String, String> allParams,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String language) {
        SearchRequest baseRequest = new SearchRequest();
        if (language != null && !language.isBlank()) {
            baseRequest.addCriterion("LANGUAGE", TranslationRecord.class, "language",
                    SearchOperation.EQUALS_OPERATION, language.toUpperCase());
        }
        return super.search(baseRequest, allParams, page, size, TranslationRecordDto.class, TranslationRecord.class);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TranslationRecordDetailDto> getById(@PathVariable UUID id) {
        return super.getById(id, TranslationRecordDetailDto.class);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TranslationRecordCreateRequest req) {
        if (req.getLanguage() == null || req.getLanguage().isBlank()) {
            return ResponseEntity.badRequest().body("language is required");
        }
        if (req.getFactor() == null) {
            req.setFactor(1);
        }
        return super.create(req, TranslationRecordDto.class);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody TranslationRecordDto req) {
        Optional<TranslationRecord> match = translationRecordService.loadById(id);
        if (match.isEmpty()) return ResponseEntity.notFound().build();

        TranslationRecord record = match.get();
        if (req.getSourceText() != null) record.setSourceText(req.getSourceText());
        if (req.getLevel() != null) record.setLevel(req.getLevel());
        if (req.getTextTranslation() != null) record.setTextTranslation(req.getTextTranslation());
        if (req.getType() != null) record.setType(req.getType());
        if (req.getInSentence() != null) record.setInSentence(req.getInSentence());
        if (req.getInSentenceTranslation() != null) record.setInSentenceTranslation(req.getInSentenceTranslation());
        if (req.getFactor() != null) record.setFactor(req.getFactor());
        if (req.getNextRepeatTime() != null) record.setNextRepeatTime(req.getNextRepeatTime());
        record.setMarkedForLearning(req.isMarkedForLearning());

        TranslationRecord saved = translationRecordService.save(record);
        return ResponseEntity.ok(mapper.map(saved, TranslationRecordDto.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        return super.delete(id);
    }

    // ── Flashcard learning ─────────────────────────────────────────────────────

    @GetMapping("/flashcards")
    public ResponseEntity<List<TranslationRecordDetailDto>> flashcards(
            @RequestParam String language,
            @RequestParam(required = false) String levels,
            @RequestParam(defaultValue = "50") int size) {
        List<String> levelList = parseLevels(levels);
        List<TranslationRecord> records = translationRecordService.getAllForLearning(
                language.toUpperCase(), levelList, Pageable.ofSize(size));
        List<TranslationRecordDetailDto> dtos = records.stream()
                .map(r -> mapper.map(r, TranslationRecordDetailDto.class))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // ── Quiz words ─────────────────────────────────────────────────────────────

    @GetMapping("/quiz")
    public ResponseEntity<List<TranslationRecordDto>> quiz(
            @RequestParam String language,
            @RequestParam(required = false) String levels,
            @RequestParam(defaultValue = "10") int size) {
        List<String> levelList = parseLevels(levels);
        List<TranslationRecord> records = translationRecordService.getRecordsForQuiz(
                language.toUpperCase(), levelList, Pageable.ofSize(size));
        List<TranslationRecordDto> dtos = records.stream()
                .map(r -> mapper.map(r, TranslationRecordDto.class))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // ── SM-2 review ────────────────────────────────────────────────────────────

    @PostMapping("/{id}/review")
    public ResponseEntity<?> review(@PathVariable UUID id, @RequestBody ReviewRequest req) {
        if (!List.of("AGAIN", "HARD", "GOOD", "EASY").contains(req.getScore())) {
            return ResponseEntity.badRequest().body("score must be one of: AGAIN, HARD, GOOD, EASY");
        }
        translationRecordService.updateNextLearningDate(id, req.getScore());
        return ResponseEntity.ok().build();
    }

    // ── Crossword ──────────────────────────────────────────────────────────────

    @PostMapping("/crossword")
    public ResponseEntity<CrosswordResultDto> crossword(
            @RequestParam String language,
            @RequestParam(required = false) String levels) {
        List<String> levelList = parseLevels(levels);
        CrosswordResultDto result = crosswordService.generate(language.toUpperCase(), levelList);
        if (result == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(result);
    }

    // ── Stats ──────────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<LanguageLearningStatsDto> stats(@RequestParam String language) {
        String lang = language.toUpperCase();
        LocalDateTime now = LocalDateTime.now();
        List<TranslationRecord> all = translationRecordService.load(PageRequest.of(0, 100_000)).stream()
                .filter(r -> lang.equals(r.getLanguage())).toList();
        long total = all.size();
        long mastered = all.stream().filter(r -> r.getFactor() != null && r.getFactor() >= 512).count();
        long dueNow = all.stream().filter(r -> r.isMarkedForLearning()
                && (r.getNextRepeatTime() == null || r.getNextRepeatTime().isBefore(now))).count();
        return ResponseEntity.ok(new LanguageLearningStatsDto(total, mastered, dueNow));
    }

    private List<String> parseLevels(String levels) {
        if (levels == null || levels.isBlank()) {
            return List.of("N/A", "A1", "A2", "B1", "B2", "C1", "C2");
        }
        return Arrays.asList(levels.split(","));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        return super.exportAll(TranslationRecordDto.class, "words");
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importData(@RequestParam("file") MultipartFile file) {
        return super.importAll(file, TranslationRecordDto.class);
    }
}
