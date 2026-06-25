package com.bervan.languageapp.service;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.AIService;
import com.bervan.common.service.AuthService;
import com.bervan.common.service.BaseService;
import com.bervan.common.service.OpenAIService;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.TranslationRecordRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class TranslationRecordService extends BaseService<UUID, TranslationRecord> {
    private static final Logger log = LoggerFactory.getLogger(TranslationRecordService.class);
    private final TranslationRecordRepository repository;
    private final AIService englishLanguageLevelAI;
    private final AIService spanishLanguageLevelAI;
    private final AIService englishUsefulPhrasesAI;
    @Value("${openai.api.key}")
    private String apiKey;

    public TranslationRecordService(TranslationRecordRepository repository,
                                    SearchService searchService) {
        super(repository, searchService);
        this.repository = repository;
        this.spanishLanguageLevelAI =
                new OpenAIService(
                        """
                                Your task is to evaluate the language level of the given spanish text.
                                Your possible answers are: A1, A2, B1, B2, C1, C2.
                                Example: "¿Hola, cómo estás?" -> A1
                                Respond only with the language level. Nothing else.
                                """);
        this.englishLanguageLevelAI =
                new OpenAIService(
                        """
                                Your task is to evaluate the language level of the given english text.
                                Your possible answers are: A1, A2, B1, B2, C1, C2.
                                Example: "Hello, how are you?" -> A1
                                Respond only with the language level. Nothing else.
                                """);
        englishUsefulPhrasesAI = new OpenAIService("""
                You are an English language learning assistant for a Polish user.
                Your task is to analyze subtitles from an episode of a TV series or a movie, extract only useful sentences or phrases worth learning with at least B2-level English (skip random slang, filler words, or swear words), and return them in JSON format.
                
                JSON format for each item:
                {
                  "sourceText": "English sentence (feel free to slightly modify the sentence for clarity)",
                  "textTranslation": "Polish translation",
                  "level": "Estimated CEFR level (minimum B2; skip easier sentences; allowed values: B2, C1, C2)"
                }
                
                Example output:
                [
                  {
                    "sourceText": "This is your first useful sentence.",
                    "textTranslation": "To jest twoje pierwsze przydatne zdanie.",
                    "level": "C1"
                  },
                  {
                    "sourceText": "Here's another practical phrase.",
                    "textTranslation": "Oto kolejny praktyczny zwrot.",
                    "level": "B2"
                  }
                ]
                
                Return only meaningful expressions that would be practical for conversations or understanding English media.
                """);
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
            //again means that you forgot - reset to 1 but keep card in current session
            case "AGAIN" -> 1;
            case "HARD" -> (int) Math.max(1, factor * 0.6);
            case "GOOD" -> factor * 2;
            case "EASY" -> factor * 4;
            default -> throw new IllegalArgumentException("Invalid grade");
        };
    }

    public TranslationRecord save(TranslationRecord record) {
        setLevel(record);

        if (record.getLanguage() == null) {
            throw new IllegalArgumentException("Language must be set!");
        }

        try {
            return repository.save(record);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setNewAndReplaceImages(TranslationRecord record) {
        try {
            List<String> imageUrls = new ArrayList<>();
            Document doc = null;
            try {
                doc = Jsoup.connect("https://unsplash.com/s/photos/" + record.getSourceText()).get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Elements links = doc.select("a[href^=/photos/]");

            for (Element link : links) {
                Elements images = link.select("img");
                for (Element img : images) {
                    String imageUrl = img.absUrl("src");
                    if (imageUrls.size() > 10) {
                        break;
                    }
                    imageUrls.add(imageUrl);
                }
            }

            if (imageUrls.isEmpty()) {
                return;
            }

            for (String image : record.getImages()) {
                record.removeImage(image);
            }

            for (String imageUrl : imageUrls) {
                record.addImage(convertImageToBase64(imageUrl));
            }
        } catch (Exception e) {
            log.error("Unable to load and set images!", e);
        }
    }

    private void setLevel(TranslationRecord record) {
        if (record.getLevel() == null || record.getLevel().isBlank() ||
                "N/A".equals(record.getLevel())) {
            String level = autoDetermineLevel(record.getSourceText(), record.getLanguage());
            if (level == null || level.isBlank() ||
                    !List.of("A1", "A2", "B1", "B2", "C1", "C2").contains(level)) {
                record.setLevel("N/A");
            } else {
                record.setLevel(level);
            }
        }
    }

    private String autoDetermineLevel(String sourceText, String language) {
        if (language == null) {
            return null;
        }
        if (language.equals("EN")) {
            return englishLanguageLevelAI.askAI(sourceText, OpenAIService.GPT_3_5_TURBO, 0.2, apiKey);
        } else if (language.equals("ES")) {
            return spanishLanguageLevelAI.askAI(sourceText, OpenAIService.GPT_3_5_TURBO, 0.2, apiKey);
        } else {
            throw new IllegalArgumentException("Language symbol is not supported!");
        }
    }

//    public List<TranslationRecord> createUsefulPhrasesForInputText(String sourceText) {
//        try {
//            String jsonResponse = englishUsefulPhrasesAI.askAI(sourceText, OpenAIService.GPT_3_5_TURBO, 0.1, apiKey);
//            ObjectMapper objectMapper = new ObjectMapper();
//            return objectMapper.readValue(
//                    jsonResponse,
//                    new TypeReference<List<TranslationRecord>>() {
//                    }
//            );
//        } catch (Exception e) {
//            log.error("Failed to createUsefulPhrasesForInputText!", e);
//            throw new RuntimeException("Failed to create useful phrases!");
//        }
//    }

    @PostFilter("(T(com.bervan.common.service.AuthService).hasAccess(filterObject.owners))")
    public List<TranslationRecord> findAllByLanguage(String language) {
        return repository.findAllByLanguageAndOwner(language.toUpperCase(), AuthService.getLoggedUserId());
    }

    @PostFilter("(T(com.bervan.common.service.AuthService).hasAccess(filterObject.owners))")
    public List<TranslationRecord> getAllForLearning(String language, List<String> levels, Pageable pageable) {
        return repository.getRecordsForLearning(LocalDateTime.now(), AuthService.getLoggedUserId(), levels, language, pageable);
    }

    @PostFilter("(T(com.bervan.common.service.AuthService).hasAccess(filterObject.owners))")
    public List<TranslationRecord> getRecordsForQuiz(String language, List<String> levels, Pageable pageable) {
        return repository.getRecordsForQuiz(AuthService.getLoggedUserId(), levels, language, pageable);
    }

    private String convertImageToBase64(String img) {
        if (img == null) return null;

        if (img.startsWith("http")) {
            try (InputStream inputStream = new URL(img).openStream()) {

                BufferedImage original = ImageIO.read(inputStream);
                if (original == null) {
                    throw new RuntimeException("Cannot read image from URL");
                }

                // Scale image
                return scaleImage(original);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return img;
    }

    private String scaleImage(BufferedImage original) throws IOException {
        BufferedImage scaled = scaleProportional(original, 300);

        // Write to JPEG with compression
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();

        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(0.6f); // 0.0 - 1.0

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            jpgWriter.setOutput(ios);
            jpgWriter.write(null, new IIOImage(scaled, null, null), jpgWriteParam);
        }

        jpgWriter.dispose();

        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private BufferedImage scaleProportional(BufferedImage original, int maxSize) {
        int w = original.getWidth();
        int h = original.getHeight();

        double scale = Math.min((double) maxSize / w, (double) maxSize / h);

        int newW = Math.max(1, (int) (w * scale));
        int newH = Math.max(1, (int) (h * scale));

        BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.drawImage(original, 0, 0, newW, newH, null);
        g2d.dispose();

        return scaled;
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

    public void delete(TranslationRecord record) {
        record.setDeleted(true);
        save(record);
    }

    public void saveAll(List<TranslationRecord> records) {
        repository.saveAll(records);
    }

    @Override
    public void save(List<TranslationRecord> data) {
        for (TranslationRecord datum : data) {
            save(datum);
        }
    }
}
