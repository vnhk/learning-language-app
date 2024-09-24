package com.bervan.languageapp.service;

import com.bervan.core.model.BervanLogger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExampleOfUsageService {
    private final String url = "https://www.diki.pl/slownik-angielskiego?q=";
    private final BervanLogger logger;

    public ExampleOfUsageService(BervanLogger logger) {
        this.logger = logger;
    }

    public List<String> createExampleOfUsage(String sourceText) {
        try {
            return find(sourceText, 5);
        } catch (Exception e) {
            logger.error("Could not create example of usage!", e);
        }
        return new ArrayList<>();
    }

    public List<String> createExampleOfUsage(String sourceText, int MAX_EXAMPLES) {
        try {
            return find(sourceText, MAX_EXAMPLES);
        } catch (Exception e) {
            logger.error("Could not create example of usage!", e);
        }
        return new ArrayList<>();
    }

    private List<String> find(String sourceText, int MAX_EXAMPLES) throws IOException {
        sourceText = sourceText.toLowerCase();
        List<String> examples = new ArrayList<>();
        Connection connection = Jsoup.connect(url + sourceText)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1.2 Safari/605.1.15");
        Document document = connection.get();
        Elements exampleSentences = document.getElementsByClass("exampleSentence");
        if (exampleSentences.size() != 0) {
            for (Element exampleSentence : exampleSentences) {
                try {
                    String exampleSentenceTranslationToBeRemoved = exampleSentence.getElementsByClass("exampleSentenceTranslation").get(0).text().trim();
                    String val = exampleSentence.text().replace(exampleSentenceTranslationToBeRemoved, "").trim();
                    examples.add(val);
                } catch (Exception e) {
                    logger.error("Could not create example of usage!", e);
                }
            }
        } else {
            exampleSentences = document.getElementsByClass("foundCollocationContent");
            for (Element exampleSentence : exampleSentences) {
                Elements ps = exampleSentence.select("p");
                for (Element p : ps) {
                    examples.add(p.text().trim());
                }
            }
        }

        if (exampleSentences.size() == 0) {
            exampleSentences = document.select(".hiddenAdditionalSentences > p[lang='en']");
            for (Element exampleSentence : exampleSentences) {
                examples.add(exampleSentence.text().trim());
            }
        }

        if (examples.size() > MAX_EXAMPLES) {
            List<String> fiveExamples = new ArrayList<>();
            for (int i = 0; i < MAX_EXAMPLES; i++) {
                fiveExamples.add(examples.get(i));
            }

            return fiveExamples;
        }

        return examples;
    }
}
