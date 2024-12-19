package com.bervan.languageapp.view;

import com.bervan.common.AbstractPageView;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.component.Flashcard;
import com.bervan.languageapp.service.TranslationRecordService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractLearningView extends AbstractPageView {
    public static final String ROUTE_NAME = "learning-english-app/learning-view";
    private final TranslationRecordService translationRecordService;

    private final Button againButton = new Button("Again");
    private final Button hardButton = new Button("Hard");
    private final Button goodButton = new Button("Good");
    private final Button easyButton = new Button("Easy");
    private final Div buttonsLayout = new Div();
    private UUID currentCardId = UUID.randomUUID();
    private Flashcard currentFlashCard = null;
    private final Checkbox reversedSwitch = new Checkbox("Reversed flashcards?");

    public AbstractLearningView(TranslationRecordService translationRecordService) {
        super();

        reversedSwitch.getElement().setAttribute("theme", "switch");

        add(new LearningEnglishLayout(ROUTE_NAME));
        againButton.addClassName("option-button");
        hardButton.addClassName("option-button");
        goodButton.addClassName("option-button");
        easyButton.addClassName("option-button");
        againButton.addClassName("flashcard-knowledge");
        hardButton.addClassName("flashcard-knowledge");
        goodButton.addClassName("flashcard-knowledge");
        easyButton.addClassName("flashcard-knowledge");
        buttonsLayout.add(againButton);
        buttonsLayout.add(hardButton);
        buttonsLayout.add(goodButton);
        buttonsLayout.add(easyButton);
        buttonsLayout.setVisible(false);

        TextField uuid = new TextField("");
        uuid.setId("uuid");
        uuid.setVisible(false);
        this.translationRecordService = translationRecordService;

        LocalDateTime now = LocalDateTime.now();
        List<TranslationRecord> all = loadLearningRecords(translationRecordService, now);

        againButton.addClickListener(buttonClickEvent -> {
            postButtonClickActions(translationRecordService, "AGAIN", all);
        });

        hardButton.addClickListener(buttonClickEvent -> {
            postButtonClickActions(translationRecordService, "HARD", all);
        });

        goodButton.addClickListener(buttonClickEvent -> {
            postButtonClickActions(translationRecordService, "GOOD", all);
        });

        easyButton.addClickListener(buttonClickEvent -> {
            postButtonClickActions(translationRecordService, "EASY", all);
        });

        setNextToLearn(all);
    }

    private List<TranslationRecord> loadLearningRecords(TranslationRecordService translationRecordService, LocalDateTime now) {
        return translationRecordService.getAllForLearning().stream()
                .filter(e -> !(e.getNextRepeatTime() != null && e.getNextRepeatTime().isAfter(now)))
                .sorted(Comparator.comparing(TranslationRecord::getNextRepeatTime, Comparator.nullsFirst(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    private void postButtonClickActions(TranslationRecordService translationRecordService, String button, List<TranslationRecord> all) {
        translationRecordService.updateNextLearningDate(currentCardId, button);
        buttonsLayout.setVisible(false);
        remove(currentFlashCard);

        for (int i = 0; i < all.size(); i++) {
            TranslationRecord next = all.iterator().next();
            if (next.getId().equals(currentCardId)) {
                all.remove(next);
                break;
            }
        }

        setNextToLearn(all);
    }

    private void setNextToLearn(List<TranslationRecord> all) {
        if (all.size() == 0) {
            all.addAll(loadLearningRecords(translationRecordService, LocalDateTime.now()));
            if (all.size() == 0) {
                showPrimaryNotification("No flashcards for that moment. Come back later!");
                return;
            }
        }

        TranslationRecord translationRecord = all.iterator().next();
        currentCardId = translationRecord.getId();

        currentFlashCard = new Flashcard(translationRecord, buttonsLayout, reversedSwitch.getValue());
        add(reversedSwitch, currentFlashCard);

        reversedSwitch.addValueChangeListener(checkboxBooleanComponentValueChangeEvent -> {
            remove(currentFlashCard);
            setNextToLearn(all);
        });

        againButton.setTooltipText("<" + TranslationRecordService.getHoursUntilNextRepeatTime(TranslationRecordService.getNextFactor("AGAIN", translationRecordService.getFactor(currentCardId)), "AGAIN") + "h");
        hardButton.setTooltipText("<" + TranslationRecordService.getHoursUntilNextRepeatTime(TranslationRecordService.getNextFactor("HARD", translationRecordService.getFactor(currentCardId)), "HARD") + "h");
        goodButton.setTooltipText("<" + TranslationRecordService.getHoursUntilNextRepeatTime(TranslationRecordService.getNextFactor("GOOD", translationRecordService.getFactor(currentCardId)), "GOOD") + "h");
        easyButton.setTooltipText("<" + TranslationRecordService.getHoursUntilNextRepeatTime(TranslationRecordService.getNextFactor("EASY", translationRecordService.getFactor(currentCardId)), "EASY") + "h");
    }
}
