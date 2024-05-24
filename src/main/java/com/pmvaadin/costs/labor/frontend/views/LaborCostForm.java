package com.pmvaadin.costs.labor.frontend.views;

import com.pmvaadin.common.DialogForm;
import com.pmvaadin.costs.labor.entities.LaborCost;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.Nonnull;

@SpringComponent
public class LaborCostForm extends DialogForm {

    private LaborCost laborCost;
    private final TextField name = new TextField("Name");
    private final Binder<LaborCost> binder = new Binder<>(LaborCost.class);

    public LaborCostForm() {
        binder.bindInstanceFields(this);
        add(name);
        customizeButton();
    }

    public void read(@Nonnull LaborCost laborCost) {
        this.laborCost = laborCost;
        binder.readBean(this.laborCost);
        var laborCostName = this.laborCost.getName();
        if (laborCostName == null) laborCostName = "";
        var title = "Labor cost: ";
        setHeaderTitle(title + laborCostName);
    }

    private void customizeButton() {
        setAsItemForm();
        getCrossClose().addClickListener(this::closeEvent);
        getClose().addClickListener(this::closeEvent);
        getSaveAndClose().addClickListener(event -> {
            saveEvent(event);
            close();
        });
        getSave().addClickListener(this::saveEvent);
    }

    private void saveEvent(ClickEvent<Button> event) {
        try {
            binder.writeBean(this.laborCost);
        } catch (ValidationException error) {
            var confDialog = new ConfirmDialog();
            confDialog.setText(error.getMessage());
            confDialog.open();
            return;
        }
        fireEvent(new SaveEvent(this, this.laborCost));
    }

    private void closeEvent(ClickEvent<Button> event) {
        fireEvent(new CloseEvent(this, this.laborCost));
        this.close();
    }

}
