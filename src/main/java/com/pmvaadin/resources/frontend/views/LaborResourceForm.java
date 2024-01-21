package com.pmvaadin.resources.frontend.views;

import com.pmvaadin.common.DialogForm;
import com.pmvaadin.resources.entity.LaborResource;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.Nonnull;

@SpringComponent
public class LaborResourceForm extends DialogForm {

    private LaborResource laborResource;
    private final TextField name = new TextField("Name");
    private final Binder<LaborResource> binder = new Binder<>(LaborResource.class);

    public LaborResourceForm() {
        binder.bindInstanceFields(this);
        add(name);
        customizeButton();
    }

    public void read(@Nonnull LaborResource laborResource) {
        this.laborResource = laborResource;
        binder.readBean(this.laborResource);
        var laborResourceName = this.laborResource.getName();
        if (laborResourceName == null) laborResourceName = "";
        var title = "Labor resource: ";
        setHeaderTitle(title + laborResourceName);
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
            binder.writeBean(this.laborResource);
        } catch (ValidationException error) {
            var confDialog = new ConfirmDialog();
            confDialog.setText(error.getMessage());
            confDialog.open();
            return;
        }
        fireEvent(new SaveEvent(this, this.laborResource));
    }

    private void closeEvent(ClickEvent<Button> event) {
        fireEvent(new CloseEvent(this, this.laborResource));
        this.close();
    }

}
