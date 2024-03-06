package com.bervan.languageapp;

import com.bervan.languageapp.component.ComponentCommonUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Route("learning")
public class LearningView extends VerticalLayout {
    private final TranslationRecordService translationRecordService;
    private List<Integer> learned = new ArrayList<>();
    private Div toLearn;
    private Button nextTranslation = new Button("Next Card");
    private Button showAnswerButton = new Button("<->");

    public LearningView(TranslationRecordService translationRecordService) {
        toLearn = new Div();
        toLearn.add(showAnswerButton);
        toLearn.add(nextTranslation);
        toLearn.add(new Hr());
        toLearn.add(new Span());
        toLearn.add(new Hr());
        toLearn.add(new Span());
        toLearn.add(new Div());
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
    }
}
