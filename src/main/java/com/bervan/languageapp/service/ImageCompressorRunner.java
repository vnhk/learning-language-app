package com.bervan.languageapp.service;

import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.model.SortDirection;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.logging.JsonLogger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class ImageCompressorRunner implements ApplicationRunner {
    private final TranslationRecordService translationRecordService;
    private JsonLogger log = JsonLogger.getLogger(ImageCompressorRunner.class, "language-app");

    public ImageCompressorRunner(TranslationRecordService translationRecordService) {
        this.translationRecordService = translationRecordService;
    }

    @Override
    public void run(ApplicationArguments args) {
        compressAllImages();
    }

    private void compressAllImages() {
        log.info("Compressing all images");
        int page = 0;
        int size = 50;
        long count = translationRecordService.loadCount();
        int iterations = (int) Math.ceil((double) count / size);
        List<TranslationRecord> batch;
        log.info("Compressing images for {} records", count);
        while (iterations > 0) {
            iterations--;
            SearchRequest request = new SearchRequest();
            request.setAddOwnerCriterion(false);
            batch = translationRecordService.load(request, PageRequest.of(page, size), "id", SortDirection.ASC);
            List<TranslationRecord> updated = new ArrayList<>();
            for (TranslationRecord translationRecord : batch) {
                List<String> compressed = translationRecord.getImages().stream()
                        .filter(s -> s != null && !s.isEmpty())
                        .filter(s -> !s.startsWith("http"))
                        .map(this::compressImage)
                        .toList();

                translationRecord.setImages(compressed);
                updated.add(translationRecord);
            }
            if (updated.size() > 0) {
                translationRecordService.saveAll(updated);
                log.info("Compressed {} images", updated.size());
            }

            page++;
        }
        log.info("Done compressing images");
    }

    private String compressImage(String s) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(s);
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (original == null) {
                return s; // skip invalid image
            }
            return scaleImage(original);
        } catch (Exception e) {
            log.error("Failed to compress image", e);
            return s;
        }
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
}