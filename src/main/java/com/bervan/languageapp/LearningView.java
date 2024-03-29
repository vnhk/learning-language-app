package com.bervan.languageapp;

import com.bervan.languageapp.component.ComponentCommonUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.util.*;

@Route("learning")
public class LearningView extends VerticalLayout {
    private final TranslationRecordService translationRecordService;
    private List<Integer> learned = new ArrayList<>();
    private Div toLearn;
    private Button nextTranslation = new Button("Next Card");
    private Button showAnswerButton = new Button("<->");
    private final ConfirmDialog confirmDialog = new ConfirmDialog();
    private Button deleteOne = new Button("I know it, delete it.");

    public LearningView(TranslationRecordService translationRecordService) {
        confirmDialog.setText("Are you sure you want to delete it?");
        confirmDialog.setHeader("Deleting");
        confirmDialog.setCancelable(true);
        confirmDialog.setCancelText("Cancel");
        confirmDialog.setConfirmText("Yes, I am sure.");

        deleteOne.addClickListener(buttonClickEvent -> {
            confirmDialog.open();
        });

        toLearn = new Div();
        toLearn.add(showAnswerButton);
        toLearn.add(nextTranslation);
        toLearn.add(new Hr());
        toLearn.add(new Span());
        toLearn.add(new Hr());
        toLearn.add(new Span());
        toLearn.add(new Div());
        toLearn.add(new Hr());
        toLearn.add(deleteOne);
        TextField uuid = new TextField("");
        uuid.setId("uuid");
        uuid.setVisible(false);
        toLearn.add(uuid);
        this.translationRecordService = translationRecordService;
        List<TranslationRecord> all = translationRecordService.getAll();
        setNextToLearn(all);

        Button translationsButton = new Button("Back to Translations");
        translationsButton.addClickListener(buttonClickEvent -> {
            UI.getCurrent().navigate("");
        });

        nextTranslation.addClickListener(buttonClickEvent -> {
            setNextToLearn(all);
        });

        add(translationsButton, new Hr(), toLearn, new Hr());
    }

    private void setNextToLearn(List<TranslationRecord> all) {
        Random random = new Random();
        if (learned.size() == all.size()) {
            learned = new ArrayList<>();
        }
        int r;
        while (learned.contains((r = random.nextInt(all.size())))) {
        }

        TextField uuid = getUUIDComponent();
        uuid.setValue(String.valueOf(all.get(r).getUuid()));
        ((Span) toLearn.getComponentAt(3)).setText(all.get(r).getSourceText());
        ComponentCommonUtils.addAudioIfExist(((Span) toLearn.getComponentAt(3)), all.get(r).getTextSound());
        ((Span) toLearn.getComponentAt(5)).setText(all.get(r).getInSentence());
        ComponentCommonUtils.addAudioIfExist(((Span) toLearn.getComponentAt(5)), all.get(r).getInSentenceSound());
        toLearn.getComponentAt(0).setVisible(true);
        toLearn.getComponentAt(1).setVisible(false);
        int finalR = r;
        ((Button) toLearn.getComponentAt(0)).addClickListener(buttonClickEvent -> {
            ((Span) toLearn.getComponentAt(3)).setText(all.get(finalR).getTextTranslation());
            ((Span) toLearn.getComponentAt(5)).setText(all.get(finalR).getInSentenceTranslation());
            toLearn.getComponentAt(0).setVisible(false);
            toLearn.getComponentAt(1).setVisible(true);
        });

        confirmDialog.addConfirmListener(confirmEvent -> {
            Optional<TranslationRecord> first = all.stream().filter(e -> e.getUuid().equals(UUID.fromString(getUUIDComponent().getValue())))
                    .findFirst();
            if (first.isPresent()) {
                TranslationRecord translationRecord = first.get();
                translationRecordService.delete(translationRecord);
                all.removeAll(all);
                all.addAll(translationRecordService.getAll());
                UI.getCurrent().getPage().reload();
            }
        });
    }

    private TextField getUUIDComponent() {
        TextField uuid = (TextField) toLearn.getChildren().filter(e -> e.getId().isPresent() && e.getId().get().equals("uuid"))
                .findFirst().get();
        return uuid;
    }
}
