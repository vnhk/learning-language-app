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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractQuizView extends VerticalLayout {
    public static final String ROUTE_NAME = "learning-english-app/quiz-view";
    private final int amountOfQuestions = 10;


    public AbstractQuizView(TranslationRecordService translationRecordService, ExampleOfUsageService exampleOfUsageService) {
        super();
        add(new LearningEnglishLayout(ROUTE_NAME));
        Map<TranslationRecord, String> quizQuestions = new HashMap<>();

        int counter = 0;
        for (TranslationRecord translationRecord : translationRecordService.load()) {
            if (counter == amountOfQuestions) {
                break;
            }

            List<String> exampleOfUsage = exampleOfUsageService.createExampleOfUsage(translationRecord.getSourceText());
            if (exampleOfUsage.size() > 0) {
                for (String example : exampleOfUsage) {
                    int oldLength = example.length();
                    example = example.replace(translationRecord.getSourceText(), "_");
                    if (example.length() != oldLength) {
                        quizQuestions.put(translationRecord, example);
                        counter++;
                        break;
                    }
                }
            }
        }

        List<String> sourceText = quizQuestions.keySet().stream().map(TranslationRecord::getSourceText).sorted().toList();

        add(new H4("Use: " + sourceText));

        Map<TranslationRecord, HorizontalLayout> questions = new HashMap<>();

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

        Button buttonCheck = new Button("Check Answers");
        buttonCheck.addClickListener(e -> {
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
        });
        add(buttonCheck);

    }
}
