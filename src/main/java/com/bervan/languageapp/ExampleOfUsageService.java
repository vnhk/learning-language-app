package com.bervan.languageapp;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ExampleOfUsageService {
    private final String url = "https://www.diki.pl/slownik-angielskiego?q=";

    public ExampleOfUsageService() {

    }

    public List<String> createExampleOfUsage(String sourceText) {
        try {
            return find(sourceText);
        } catch (Exception e) {
            log.error("Failed to find example of usage!", e);
        }
        return new ArrayList<>();
    }

    private List<String> find(String sourceText) throws IOException {
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
                    log.error(e.getMessage(), e);
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

        return examples;
    }
}
