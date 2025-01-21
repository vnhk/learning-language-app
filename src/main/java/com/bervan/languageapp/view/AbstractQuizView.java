package com.bervan.languageapp.view;

import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.data.domain.Pageable;

import java.util.*;

public abstract class AbstractQuizView extends VerticalLayout {
    public static final String ROUTE_NAME = "learning-english-app/quiz-view";
    private final int amountOfQuestions = 10;


    public AbstractQuizView(TranslationRecordService translationRecordService, ExampleOfUsageService exampleOfUsageService) {
        super();
        add(new LearningEnglishLayout(ROUTE_NAME));
        Map<TranslationRecord, String> quizQuestions = new HashMap<>();

        List<TranslationRecord> records = new ArrayList<>(translationRecordService.load(Pageable.ofSize(amountOfQuestions)).stream().toList());
        Collections.shuffle(records);
        buildQuizQuestionsMapping(records, exampleOfUsageService, quizQuestions);

        List<String> sourceText = quizQuestions.keySet().stream().map(TranslationRecord::getSourceText).sorted().toList();
        add(new H4("Use: " + sourceText));

        Map<TranslationRecord, HorizontalLayout> questions = new HashMap<>();
        buildQuestionsLayouts(quizQuestions, sourceText, questions);

        Button buttonCheck = new Button("Check Answers");
        buttonCheck.addClassName("option-button");
        buttonCheck.addClickListener(e -> {
            checkAnswers(quizQuestions, questions);
        });
        add(buttonCheck);

    }

    private void checkAnswers(Map<TranslationRecord, String> quizQuestions, Map<TranslationRecord, HorizontalLayout> questions) {
        for (Map.Entry<TranslationRecord, HorizontalLayout> quizQ : questions.entrySet()) {
            StringBuilder sentenceBuilder = new StringBuilder();
            for (int i = 0; i < quizQ.getValue().getComponentCount(); i++) {
                Component componentAt = quizQ.getValue().getComponentAt(i);
                if (componentAt instanceof H4) {
                    sentenceBuilder.append(((H4) componentAt).getText());
                } else {
                    Object value = ((ComboBox) componentAt).getValue();
                    if (value != null) {
                        sentenceBuilder.append(value);
                    }
                }
            }

            if (quizQuestions.get(quizQ.getKey()).replace("_", quizQ.getKey().getSourceText()).contentEquals(sentenceBuilder)) {
                quizQ.getValue().getStyle().set("background-color", "green");
            } else {
                quizQ.getValue().getStyle().set("background-color", "red");
            }
        }
    }

    private void buildQuestionsLayouts(Map<TranslationRecord, String> quizQuestions, List<String> sourceText, Map<TranslationRecord, HorizontalLayout> questions) {
        for (Map.Entry<TranslationRecord, String> quizQ : quizQuestions.entrySet()) {
            String[] questionParts = quizQ.getValue().split("_");
            HorizontalLayout questionL = new HorizontalLayout();
            for (String questionPart : questionParts) {
                ComboBox<String> options = new ComboBox<>("", sourceText);
                questionL.add(new H4(questionPart), options);
            }
            questionL.remove(questionL.getComponentAt(questionL.getComponentCount() - 1)); //remove last inputField
            add(questionL);
            questions.put(quizQ.getKey(), questionL);
        }
    }

    private void buildQuizQuestionsMapping(List<TranslationRecord> translationRecords, ExampleOfUsageService exampleOfUsageService, Map<TranslationRecord, String> quizQuestions) {
        int counter = 0;
        for (TranslationRecord translationRecord : translationRecords) {
            if (counter == amountOfQuestions) {
                break;
            }

            List<String> exampleOfUsage = exampleOfUsageService.createExampleOfUsage(translationRecord.getSourceText(), 20);
            if (exampleOfUsage.size() > 0) {
                List<Integer> checkedIndexes = new ArrayList<>();

                int randomIndex = findRandomIndex(exampleOfUsage.size(), checkedIndexes);
                while (randomIndex != -1) {
                    checkedIndexes.add(randomIndex);
                    String example = exampleOfUsage.get(randomIndex);
                    int oldLength = example.length();
                    example = example.replace(translationRecord.getSourceText(), "_");
                    if (example.length() != oldLength) {
                        quizQuestions.put(translationRecord, example);
                        counter++;
                        break;
                    } else {
                        randomIndex = findRandomIndex(exampleOfUsage.size(), checkedIndexes);
                    }
                }
            }
        }
    }

    private int findRandomIndex(int size, List<Integer> alreadyUsed) {
        Random r = new Random();
        if (size == alreadyUsed.size()) {
            return -1;
        }
        int res = -1;
        while (res == -1 || alreadyUsed.contains(res)) {
            res = r.nextInt(size);
        }

        return res;
    }
}
