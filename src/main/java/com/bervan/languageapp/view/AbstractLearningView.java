package com.bervan.languageapp.view;

import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.component.ComponentCommonUtils;
import com.bervan.languageapp.service.TranslationRecordService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public abstract class AbstractLearningView extends VerticalLayout {
    public static final String ROUTE_NAME = "learning-english-app/learning-view";
    private final TranslationRecordService translationRecordService;
    private List<Integer> learned = new ArrayList<>();
    private Div toLearn;
    private Button showAnswerButton = new Button("<->");
    private final ConfirmDialog confirmDialog = new ConfirmDialog();
    Button againButton = new Button("Again");
    Button hardButton = new Button("Hard");
    Button goodButton = new Button("Good");
    Button easyButton = new Button("Easy");
    HorizontalLayout buttonsLayout = new HorizontalLayout();


    public AbstractLearningView(TranslationRecordService translationRecordService) {
        confirmDialog.setText("Are you sure you want to delete it?");
        confirmDialog.setHeader("Deleting");
        confirmDialog.setCancelable(true);
        confirmDialog.setCancelText("Cancel");
        confirmDialog.setConfirmText("Yes, I am sure.");

        buttonsLayout.add(againButton, hardButton, goodButton, easyButton);
        buttonsLayout.setVisible(false);

        toLearn = new Div();
        toLearn.add(showAnswerButton);
        toLearn.add(new Div());
        toLearn.add(new Hr());
        toLearn.add(new Span());
        toLearn.add(new Hr());
        toLearn.add(new Span());
        toLearn.add(new Div());
        toLearn.add(new Hr());
        toLearn.add(buttonsLayout);
        TextField uuid = new TextField("");
        uuid.setId("uuid");
        uuid.setVisible(false);
        toLearn.add(uuid);
        this.translationRecordService = translationRecordService;
        List<TranslationRecord> all = translationRecordService.getAllForLearning();
        setNextToLearn(all);

        Button translationsButton = new Button("Back to Translations");
        translationsButton.addClickListener(buttonClickEvent -> {
            UI.getCurrent().navigate(AbstractLearningAppHomeView.ROUTE_NAME);
        });

        againButton.addClickListener(buttonClickEvent -> {
            translationRecordService.updateNextLearningDate(getUUIDComponent().getValue(), "AGAIN");
            buttonsLayout.setVisible(false);
            setNextToLearn(all);
        });

        hardButton.addClickListener(buttonClickEvent -> {
            translationRecordService.updateNextLearningDate(getUUIDComponent().getValue(), "HARD");
            buttonsLayout.setVisible(false);
            setNextToLearn(all);
        });

        goodButton.addClickListener(buttonClickEvent -> {
            translationRecordService.updateNextLearningDate(getUUIDComponent().getValue(), "GOOD");
            buttonsLayout.setVisible(false);
            setNextToLearn(all);
        });

        easyButton.addClickListener(buttonClickEvent -> {
            translationRecordService.updateNextLearningDate(getUUIDComponent().getValue(), "EASY");
            buttonsLayout.setVisible(false);
            setNextToLearn(all);
        });

        add(translationsButton, new Hr(), toLearn, new Hr());
    }

    private void setNextToLearn(List<TranslationRecord> all) {
        if (all.size() == 0) {
            return;
        }

        Random random = new Random();
        if (learned.size() == all.size()) {
            learned = new ArrayList<>();
        }
        int r;
        while (learned.contains((r = random.nextInt(all.size())))) {
        }

        TextField uuid = getUUIDComponent();
        uuid.setValue(String.valueOf(all.get(r).getId()));

        int i = 0;
        for (; i < all.size(); i++) {
            if (all.get(i).getId().equals(UUID.fromString(uuid.getValue()))) {
                all.remove(i);
                break;
            }
        }


        ((Span) toLearn.getComponentAt(3)).setText(all.get(r).getSourceText());
        ComponentCommonUtils.addAudioIfExist(((Span) toLearn.getComponentAt(3)), all.get(r).getTextSound());
        ((Span) toLearn.getComponentAt(5)).setText(all.get(r).getInSentence());
        ComponentCommonUtils.addAudioIfExist(((Span) toLearn.getComponentAt(5)), all.get(r).getInSentenceSound());
        toLearn.getComponentAt(0).setVisible(true);
        toLearn.getComponentAt(1).setVisible(false);
        int finalR = r;
        ((Button) toLearn.getComponentAt(0)).addClickListener(buttonClickEvent -> {
            //It runs when you click first time on flashcard
            ((Span) toLearn.getComponentAt(3)).setText(all.get(finalR).getTextTranslation());
            ((Span) toLearn.getComponentAt(5)).setText(all.get(finalR).getInSentenceTranslation());
            toLearn.getComponentAt(0).setVisible(false);
            toLearn.getComponentAt(1).setVisible(true);

            buttonsLayout.setVisible(true);
        });
    }

    private TextField getUUIDComponent() {
        TextField uuid = (TextField) toLearn.getChildren().filter(e -> e.getId().isPresent() && e.getId().get().equals("uuid"))
                .findFirst().get();
        return uuid;
    }
}
