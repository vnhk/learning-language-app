package com.bervan.languageapp.view;

import com.bervan.common.MenuNavigationComponent;
import com.bervan.common.component.BervanButton;
import com.bervan.common.component.BervanButtonStyle;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
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
    private Map<String, VerticalLayout> questions = new HashMap<>();
    private final BervanButton generateQuizButton = new BervanButton("Generate Quiz", buttonClickEvent -> generateQuiz());
    private VerticalLayout quizContainer;
    private Div resultsDiv;
    private int correctCount = 0;
    private int totalQuestions = 0;

    public AbstractQuizView(TranslationRecordService translationRecordService, ExampleOfUsageService exampleOfUsageService, String language, MenuNavigationComponent menuNavigationLayout) {
        super();
        this.translationRecordService = translationRecordService;
        this.exampleOfUsageService = exampleOfUsageService;
        this.language = language;

        addClassName("quiz-view");
        setPadding(true);
        setSpacing(true);

        add(menuNavigationLayout);

        // Level filters in a styled container
        Div levelFiltersContainer = new Div();
        levelFiltersContainer.addClassName("language-level-filters");

        Span filterLabel = new Span("Filter by level:");
        filterLabel.addClassName("language-filter-label");

        HorizontalLayout checkboxes = new HorizontalLayout(levelNotClass, levelA1, levelA2, levelB1, levelB2, levelC1, levelC2);
        checkboxes.setSpacing(true);
        checkboxes.setAlignItems(FlexComponent.Alignment.CENTER);

        levelFiltersContainer.add(filterLabel, checkboxes);
        add(levelFiltersContainer);

        // Action buttons
        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.setSpacing(true);
        actionButtons.addClassName("quiz-action-buttons");

        generateQuizButton.addClassName("primary");
        Button checkButton = new BervanButton("Check Answers", e -> checkAnswers());
        Button resetButton = new BervanButton("Reset", e -> resetQuiz(), BervanButtonStyle.SECONDARY);

        actionButtons.add(generateQuizButton, checkButton, resetButton);
        add(actionButtons);

        // Results container
        resultsDiv = new Div();
        resultsDiv.addClassName("quiz-results");
        resultsDiv.setVisible(false);
        add(resultsDiv);

        // Quiz questions container
        quizContainer = new VerticalLayout();
        quizContainer.setPadding(false);
        quizContainer.setSpacing(true);
        quizContainer.addClassName("quiz-questions-container");
        add(quizContainer);
    }

    private void resetQuiz() {
        quizContainer.removeAll();
        resultsDiv.setVisible(false);
        questions.clear();
        correctCount = 0;
    }

    private void checkAnswers() {
        correctCount = 0;
        totalQuestions = questions.size();

        for (Map.Entry<String, VerticalLayout> quizQ : questions.entrySet()) {
            HorizontalLayout questionRow = (HorizontalLayout) quizQ.getValue().getComponentAt(1);
            StringBuilder sentenceBuilder = new StringBuilder();

            for (int i = 0; i < questionRow.getComponentCount(); i++) {
                Component componentAt = questionRow.getComponentAt(i);
                if (componentAt instanceof Span) {
                    sentenceBuilder.append(((Span) componentAt).getText());
                } else if (componentAt instanceof ComboBox) {
                    Object value = ((ComboBox<?>) componentAt).getValue();
                    if (value != null) {
                        sentenceBuilder.append(value);
                    }
                }
            }

            String expectedSentence = quizQuestions.get(quizQ.getKey()).replace("_", quizQ.getKey());
            String actualSentence = sentenceBuilder.toString();

            // Case-insensitive comparison with trimming
            boolean isCorrect = expectedSentence.trim().equalsIgnoreCase(actualSentence.trim());

            if (isCorrect) {
                quizQ.getValue().removeClassName("quiz-question-incorrect");
                quizQ.getValue().addClassName("quiz-question-correct");
                correctCount++;
            } else {
                quizQ.getValue().removeClassName("quiz-question-correct");
                quizQ.getValue().addClassName("quiz-question-incorrect");
            }
        }

        // Show results
        showResults();
    }

    private void showResults() {
        resultsDiv.removeAll();
        resultsDiv.setVisible(true);

        double percentage = totalQuestions > 0 ? (correctCount * 100.0 / totalQuestions) : 0;

        H3 scoreTitle = new H3("Score: " + correctCount + "/" + totalQuestions + " (" + String.format("%.0f", percentage) + "%)");
        scoreTitle.addClassName("quiz-score-title");

        ProgressBar progressBar = new ProgressBar(0, totalQuestions, correctCount);
        progressBar.addClassName("quiz-score-progress");

        String message;
        String messageClass;
        if (percentage == 100) {
            message = "Perfect! Excellent work!";
            messageClass = "quiz-message-perfect";
        } else if (percentage >= 80) {
            message = "Great job! Keep it up!";
            messageClass = "quiz-message-great";
        } else if (percentage >= 60) {
            message = "Good effort! Practice more.";
            messageClass = "quiz-message-good";
        } else {
            message = "Keep practicing! You'll improve.";
            messageClass = "quiz-message-practice";
        }

        Span messageSpan = new Span(message);
        messageSpan.addClassName(messageClass);

        resultsDiv.add(scoreTitle, progressBar, messageSpan);
    }

    private void generateQuiz() {
        resetQuiz();

        List<TranslationRecord> records = new ArrayList<>(translationRecordService.getRecordsForQuiz(language, getSelectedLevels(),
                        Pageable.ofSize(amountOfQuestions * 5))
                .stream().toList());

        if (records.isEmpty()) {
            Div noRecords = new Div();
            noRecords.setText("No flashcards found for the selected levels. Add some words first!");
            noRecords.addClassName("quiz-no-records");
            quizContainer.add(noRecords);
            return;
        }

        Collections.shuffle(records);
        buildQuizQuestionsMapping(records);

        if (quizQuestions.isEmpty()) {
            Div noQuestions = new Div();
            noQuestions.setText("Could not generate quiz questions. Try again or add more words.");
            noQuestions.addClassName("quiz-no-records");
            quizContainer.add(noQuestions);
            return;
        }

        List<String> sourceText = quizQuestions.keySet().stream().sorted().toList();

        // Word bank
        Div wordBank = new Div();
        wordBank.addClassName("quiz-word-bank");
        Span wordBankLabel = new Span("Words to use: ");
        wordBankLabel.addClassName("quiz-word-bank-label");
        wordBank.add(wordBankLabel);

        for (String word : sourceText) {
            Span wordChip = new Span(word);
            wordChip.addClassName("quiz-word-chip");
            wordBank.add(wordChip);
        }
        quizContainer.add(wordBank);

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
        int questionNumber = 1;

        for (Map.Entry<String, String> quizQ : quizQuestions.entrySet()) {
            // Question card container
            VerticalLayout questionCard = new VerticalLayout();
            questionCard.addClassName("quiz-question-card");
            questionCard.setPadding(true);
            questionCard.setSpacing(false);

            // Question number header
            Span questionNum = new Span("Question " + questionNumber);
            questionNum.addClassName("quiz-question-number");

            // Question content row
            HorizontalLayout questionRow = new HorizontalLayout();
            questionRow.addClassName("quiz-question-row");
            questionRow.setAlignItems(FlexComponent.Alignment.CENTER);
            questionRow.setSpacing(false);

            String[] questionParts = quizQ.getValue().split("_");
            for (int i = 0; i < questionParts.length; i++) {
                String part = questionParts[i];

                // Add text part
                if (!part.isEmpty()) {
                    Span textPart = new Span(part);
                    textPart.addClassName("quiz-text-part");
                    questionRow.add(textPart);
                }

                // Add dropdown (except after last part if even number of parts)
                if (i < questionParts.length - 1 || questionParts.length % 2 != 0) {
                    ComboBox<String> options = new ComboBox<>();
                    options.setItems(sourceText);
                    options.setPlaceholder("Select...");
                    options.addClassName("quiz-dropdown");
                    options.setWidth("150px");
                    questionRow.add(options);
                }
            }

            questionCard.add(questionNum, questionRow);
            quizContainer.add(questionCard);
            questions.put(quizQ.getKey(), questionCard);
            questionNumber++;
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
