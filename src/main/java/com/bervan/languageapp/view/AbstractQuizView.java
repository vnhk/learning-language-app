package com.bervan.languageapp.view;

import com.bervan.common.MenuNavigationComponent;
import com.bervan.common.component.BervanButton;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.en.LearningEnglishLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.data.domain.Pageable;

import java.util.*;

public abstract class AbstractQuizView extends VerticalLayout {
    private final int amountOfQuestions = 10;
    private final Checkbox levelNotClass = new Checkbox("N/A", true);
    private final Checkbox levelA1 = new Checkbox("A1", true);
    private final Checkbox levelA2 = new Checkbox("A2", true);
    private final Checkbox levelB1 = new Checkbox("B1", true);
    private final Checkbox levelB2 = new Checkbox("B2", true);
    private final Checkbox levelC1 = new Checkbox("C1", true);
    private final Checkbox levelC2 = new Checkbox("C2", true);
    private final TranslationRecordService translationRecordService;
    private final ExampleOfUsageService exampleOfUsageService;
    private final String language;
    private Map<String, String> quizQuestions;
    private Map<String, HorizontalLayout> questions = new HashMap<>();
    private final BervanButton generateQuizButton = new BervanButton("Generate", buttonClickEvent -> generateQuiz());

    public AbstractQuizView(TranslationRecordService translationRecordService, ExampleOfUsageService exampleOfUsageService, String language, MenuNavigationComponent menuNavigationLayout) {
        super();
        this.translationRecordService = translationRecordService;
        this.exampleOfUsageService = exampleOfUsageService;
        this.language = language;
        add(menuNavigationLayout);

        Div levelCheckBoxesLayout = new Div(levelNotClass, levelA1, levelA2, levelB1, levelB2, levelC1, levelC2);
        add(levelCheckBoxesLayout, generateQuizButton);

        Button buttonCheck = new Button("Check Answers");
        buttonCheck.addClassName("option-button");
        buttonCheck.addClickListener(e -> {
            checkAnswers();
        });
        add(buttonCheck);

    }

    private void checkAnswers() {
        for (Map.Entry<String, HorizontalLayout> quizQ : questions.entrySet()) {
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

            if (quizQuestions.get(quizQ.getKey()).replace("_", quizQ.getKey()).contentEquals(sentenceBuilder)) {
                quizQ.getValue().getStyle().set("background-color", "green");
            } else {
                quizQ.getValue().getStyle().set("background-color", "red");
            }
        }
    }

    private void generateQuiz() {
        List<TranslationRecord> records = new ArrayList<>(translationRecordService.getRecordsForQuiz(language, getSelectedLevels(),
                        Pageable.ofSize(amountOfQuestions * 5))
                .stream().toList());
        Collections.shuffle(records);
        buildQuizQuestionsMapping(records);

        List<String> sourceText = quizQuestions.keySet().stream().sorted().toList();
        add(new H4("Use: " + sourceText));

        buildQuestionsLayouts(sourceText);
    }

    private List<String> getSelectedLevels() {
        List<String> levels = new ArrayList<>();
        if (levelNotClass.getValue()) levels.add("N/A");
        if (levelA1.getValue()) levels.add("A1");
        if (levelA2.getValue()) levels.add("A2");
        if (levelB1.getValue()) levels.add("B1");
        if (levelB2.getValue()) levels.add("B2");
        if (levelC1.getValue()) levels.add("C1");
        if (levelC2.getValue()) levels.add("C2");
        return levels;
    }

    private void buildQuestionsLayouts(List<String> sourceText) {
        questions = new HashMap<>();
        for (Map.Entry<String, String> quizQ : quizQuestions.entrySet()) {
            String[] questionParts = quizQ.getValue().split("_");
            HorizontalLayout questionL = new HorizontalLayout();
            for (String questionPart : questionParts) {
                ComboBox<String> options = new ComboBox<>("", sourceText);
                questionL.add(new H4(questionPart), options);
            }

            if (questionParts.length % 2 == 0) {
                questionL.remove(questionL.getComponentAt(questionL.getComponentCount() - 1));
            }

            add(questionL);
            questions.put(quizQ.getKey(), questionL);
        }
    }

    private void buildQuizQuestionsMapping(List<TranslationRecord> translationRecords) {
        quizQuestions = new HashMap<>();
        int counter = 0;
        for (TranslationRecord translationRecord : translationRecords) {
            if (counter == amountOfQuestions) {
                break;
            }

            Map<String, List<String>> exampleOfUsage = exampleOfUsageService.createExampleOfUsage(translationRecord.getSourceText(), language,1, true);
            if (!exampleOfUsage.isEmpty()) {
                List<Integer> checkedIndexes = new ArrayList<>();

                String sourceWordInFormat = exampleOfUsage.keySet().iterator().next();
                int randomIndex = findRandomIndex(exampleOfUsage.get(sourceWordInFormat).size(), checkedIndexes);
                while (randomIndex != -1) {
                    checkedIndexes.add(randomIndex);
                    String example = exampleOfUsage.get(sourceWordInFormat).get(randomIndex);
                    int oldLength = example.length();
                    example = example.replace(translationRecord.getSourceText(), "_");
                    if (example.length() != oldLength) {
                        quizQuestions.put(sourceWordInFormat, example);
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
