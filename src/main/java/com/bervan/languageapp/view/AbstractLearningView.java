package com.bervan.languageapp.view;

import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.component.Flashcard;
import com.bervan.languageapp.service.TranslationRecordService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;
import java.util.UUID;

public abstract class AbstractLearningView extends VerticalLayout {
    public static final String ROUTE_NAME = "learning-english-app/learning-view";
    private final TranslationRecordService translationRecordService;

    private final Button againButton = new Button("Again");
    private final Button hardButton = new Button("Hard");
    private final Button goodButton = new Button("Good");
    private final Button easyButton = new Button("Easy");
    private final HorizontalLayout buttonsLayout = new HorizontalLayout();
    private UUID currentCardId = UUID.randomUUID();
    private Flashcard currentFlashCard = null;


    public AbstractLearningView(TranslationRecordService translationRecordService) {
        buttonsLayout.add(againButton);
        buttonsLayout.add(hardButton);
        buttonsLayout.add(goodButton);
        buttonsLayout.add(easyButton);
        buttonsLayout.setVisible(false);

        TextField uuid = new TextField("");
        uuid.setId("uuid");
        uuid.setVisible(false);
        this.translationRecordService = translationRecordService;
        List<TranslationRecord> all = translationRecordService.getAllForLearning();

        Button translationsButton = new Button("Back to Translations");
        translationsButton.addClickListener(buttonClickEvent -> {
            UI.getCurrent().navigate(AbstractLearningAppHomeView.ROUTE_NAME);
        });

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

        add(translationsButton, new Hr());
        setNextToLearn(all);
    }

    private void postButtonClickActions(TranslationRecordService translationRecordService, String button, List<TranslationRecord> all) {
        translationRecordService.updateNextLearningDate(currentCardId, button);
        buttonsLayout.setVisible(false);
        remove(currentFlashCard);

        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(currentCardId)) {
                all.remove(i);
                break;
            }
        }

        setNextToLearn(all);
    }

    private void setNextToLearn(List<TranslationRecord> all) {
        if (all.size() == 0) {
            Notification.show("No flashcards for that moment. Come back later!");
            return;
        }

        TranslationRecord translationRecord = all.get(0);
        currentCardId = translationRecord.getId();

        currentFlashCard = new Flashcard(translationRecord, buttonsLayout);
        add(currentFlashCard);

        againButton.setTooltipText("<" + TranslationRecordService.getHoursUntilNextRepeatTime(TranslationRecordService.getNextFactor("AGAIN", translationRecordService.getFactor(currentCardId))) + "h");
        hardButton.setTooltipText("<" + TranslationRecordService.getHoursUntilNextRepeatTime(TranslationRecordService.getNextFactor("HARD", translationRecordService.getFactor(currentCardId))) + "h");
        goodButton.setTooltipText("<" + TranslationRecordService.getHoursUntilNextRepeatTime(TranslationRecordService.getNextFactor("GOOD", translationRecordService.getFactor(currentCardId))) + "h");
        easyButton.setTooltipText("<" + TranslationRecordService.getHoursUntilNextRepeatTime(TranslationRecordService.getNextFactor("EASY", translationRecordService.getFactor(currentCardId))) + "h");
    }
}
