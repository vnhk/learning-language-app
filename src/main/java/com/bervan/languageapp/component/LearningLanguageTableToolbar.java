package com.bervan.languageapp.component;

import com.bervan.common.BervanTableToolbar;
import com.bervan.common.component.BervanButton;
import com.bervan.common.component.table.BervanFloatingToolbar;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.service.BaseService;
import com.bervan.languageapp.TranslationRecord;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class LearningLanguageTableToolbar extends BervanTableToolbar<UUID, TranslationRecord> {
    private BervanButton markToLearnButton;
    private BervanButton markNotToLearnButton;
    private BaseService<UUID, TranslationRecord> service;
    private BervanFloatingToolbar floatingToolbar;

    public LearningLanguageTableToolbar(List<Checkbox> checkboxes, List<TranslationRecord> data, Checkbox selectAllCheckbox, List<Button> buttonsForCheckboxesForVisibilityChange,
                                        BaseService<UUID, TranslationRecord> service, BervanViewConfig bervanViewConfig, Function<Void, Void> refreshDataFunction) {
        super(checkboxes, data, TranslationRecord.class, bervanViewConfig, selectAllCheckbox, buttonsForCheckboxesForVisibilityChange, refreshDataFunction, service);
        this.service = service;
    }

    /**
     * Sets the floating toolbar to add custom actions to.
     * Also enables icon buttons for modern UI consistency.
     */
    public LearningLanguageTableToolbar withFloatingToolbar(BervanFloatingToolbar floatingToolbar) {
        this.floatingToolbar = floatingToolbar;
        this.useIconButtons = true; // Enable icon buttons for consistency
        return this;
    }

    public LearningLanguageTableToolbar withMarkNotToLearn() {
        // Create icon button for main toolbar
        markNotToLearnButton = new BervanButton(new Icon(VaadinIcon.BAN), setNotToLearnEvent -> {
            handleDeactivate();
        });
        markNotToLearnButton.getElement().setAttribute("title", "Deactivate selected");
        markNotToLearnButton.addClassName("bervan-icon-btn");
        markNotToLearnButton.addClassName("warning");

        actionsToBeAdded.add(markNotToLearnButton);

        // Add to floating toolbar if available
        if (floatingToolbar != null) {
            floatingToolbar.addCustomAction(
                    "deactivate",
                    "vaadin:ban",
                    "Deactivate",
                    "warning",
                    event -> handleDeactivate()
            );
        }

        return this;
    }

    private void handleDeactivate() {
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

            refreshDataFunction.apply(null);
            showSuccessNotification("Changed state of " + toSet.size() + " items");
        });

        confirmDialog.setCancelText("Cancel");
        confirmDialog.setCancelable(true);
        confirmDialog.addCancelListener(event -> {
        });

        confirmDialog.open();
    }

    public LearningLanguageTableToolbar withMarkToLearn() {
        // Create icon button for main toolbar
        markToLearnButton = new BervanButton(new Icon(VaadinIcon.CHECK), setToLearnEvent -> {
            handleActivate();
        });
        markToLearnButton.getElement().setAttribute("title", "Activate selected");
        markToLearnButton.addClassName("bervan-icon-btn");
        markToLearnButton.addClassName("success");

        actionsToBeAdded.add(markToLearnButton);

        // Add to floating toolbar if available
        if (floatingToolbar != null) {
            floatingToolbar.addCustomAction(
                    "activate",
                    "vaadin:check",
                    "Activate",
                    "success",
                    event -> handleActivate()
            );
        }

        return this;
    }

    private void handleActivate() {
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

            refreshDataFunction.apply(null);
            showSuccessNotification("Changed state of " + toSet.size() + " items");
        });

        confirmDialog.setCancelText("Cancel");
        confirmDialog.setCancelable(true);
        confirmDialog.addCancelListener(event -> {
        });

        confirmDialog.open();
    }
}
