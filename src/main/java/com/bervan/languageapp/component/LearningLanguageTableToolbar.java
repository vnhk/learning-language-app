package com.bervan.languageapp.component;

import com.bervan.common.BervanTableToolbar;
import com.bervan.common.component.BervanButton;
import com.bervan.common.component.BervanButtonStyle;
import com.bervan.common.service.BaseService;
import com.bervan.common.service.GridActionService;
import com.bervan.languageapp.TranslationRecord;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LearningLanguageTableToolbar extends BervanTableToolbar<UUID, TranslationRecord> {
    private BervanButton markToLearnButton;
    private BervanButton markNotToLearnButton;
    private BaseService<UUID, TranslationRecord> service;

    public LearningLanguageTableToolbar(GridActionService<UUID, TranslationRecord> gridActionService, List<Checkbox> checkboxes, List<TranslationRecord> data, Class<?> tClass, Checkbox selectAllCheckbox, List<Button> buttonsForCheckboxesForVisibilityChange,
                                        BaseService<UUID, TranslationRecord> service) {
        super(gridActionService, checkboxes, data, tClass, selectAllCheckbox, buttonsForCheckboxesForVisibilityChange);
        this.service = service;
    }

    public LearningLanguageTableToolbar withMarkNotToLearn() {
        markNotToLearnButton = new BervanButton("Deactivate", setNotToLearnEvent -> {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Confirm deactivation");
            confirmDialog.setText("Are you sure you want to deactivate selected item(s)?");

            confirmDialog.setConfirmText("Yes");
            confirmDialog.setConfirmButtonTheme("primary");
            confirmDialog.addConfirmListener(event -> {
                Set<String> itemsId = getSelectedItemsByCheckbox();

                List<TranslationRecord> toSet = data.stream()
                        .filter(e -> e.getId() != null)
                        .filter(e -> itemsId.contains(e.getId().toString()))
                        .filter(TranslationRecord::isMarkedForLearning)
                        .toList();

                for (TranslationRecord translationRecord : toSet) {
                    translationRecord.setMarkedForLearning(false);
                    TranslationRecord translationRecordInDB = service.loadById(translationRecord.getId()).get();
                    translationRecordInDB.setMarkedForLearning(false);
                    service.save(translationRecordInDB);
                }

                checkboxes.stream().filter(AbstractField::getValue).forEach(e -> e.setValue(false));
                selectAllCheckbox.setValue(false);

                gridActionService.refreshData(data);
                showSuccessNotification("Changed state of " + toSet.size() + " items");
            });

            confirmDialog.setCancelText("Cancel");
            confirmDialog.setCancelable(true);
            confirmDialog.addCancelListener(event -> {
            });

            confirmDialog.open();
        }, BervanButtonStyle.WARNING);

        actionsToBeAdded.add(markNotToLearnButton);
        return this;
    }

    public LearningLanguageTableToolbar withMarkToLearn() {
        markToLearnButton = new BervanButton("Activate", setToLearnEvent -> {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Confirm activation");
            confirmDialog.setText("Are you sure you want to activate selected item(s)?");

            confirmDialog.setConfirmText("Yes");
            confirmDialog.setConfirmButtonTheme("primary");
            confirmDialog.addConfirmListener(event -> {
                Set<String> itemsId = getSelectedItemsByCheckbox();

                List<TranslationRecord> toSet = data.stream()
                        .filter(e -> e.getId() != null)
                        .filter(e -> itemsId.contains(e.getId().toString()))
                        .filter(e -> !e.isMarkedForLearning())
                        .toList();

                for (TranslationRecord translationRecord : toSet) {
                    translationRecord.setMarkedForLearning(true);
                    TranslationRecord translationRecordInDB = service.loadById(translationRecord.getId()).get();
                    translationRecordInDB.setMarkedForLearning(true);
                    service.save(translationRecordInDB);
                }

                checkboxes.stream().filter(AbstractField::getValue).forEach(e -> e.setValue(false));
                selectAllCheckbox.setValue(false);

                gridActionService.refreshData(data);
                showSuccessNotification("Changed state of " + toSet.size() + " items");
            });

            confirmDialog.setCancelText("Cancel");
            confirmDialog.setCancelable(true);
            confirmDialog.addCancelListener(event -> {
            });

            confirmDialog.open();
        }, BervanButtonStyle.WARNING);

        actionsToBeAdded.add(markToLearnButton);
        return this;
    }
}
