package com.pmvaadin.terms.timeunit.frontend.views;

import com.pmvaadin.common.DialogForm;
import com.pmvaadin.project.structure.StandardError;
import com.pmvaadin.project.common.BigDecimalToDoubleConverter;
import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

public class TimeUnitForm extends DialogForm {

    private TimeUnit timeUnit;
    private final TextField name = new TextField("Name");
    private final NumberField numberOfHours = new NumberField("Hours");
    private final Binder<TimeUnit> binder = new Binder<>(TimeUnit.class);

    public TimeUnitForm() {

        numberOfHours.setStepButtonsVisible(true);
        numberOfHours.setStep(0.01);
        numberOfHours.setMin(0.01);
        binder.forField(numberOfHours).withConverter(new BigDecimalToDoubleConverter(numberOfHours))
                .withValidator(bigDecimal -> bigDecimal.doubleValue() > 0, getMessage())
                .bind(TimeUnit::getNumberOfHours, TimeUnit::setNumberOfHours);
        binder.forField(name).bind(TimeUnit::getName, TimeUnit::setName);
        binder.bindInstanceFields(this);
        add(name, numberOfHours);
        setAsItemForm();
        setDraggable(true);
        setResizable(true);
        customizeButtons();
    }

    public void read(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        name.setEnabled(!timeUnit.isPredefined());
        numberOfHours.setEnabled(!timeUnit.isPredefined());
        binder.readBean(this.timeUnit);
        var laborResourceName = this.timeUnit.getName();
        if (laborResourceName == null) laborResourceName = "";
        var title = "Time unit: ";
        setHeaderTitle(title + laborResourceName);
        getSaveAndClose().setEnabled(!timeUnit.isPredefined());
        getSave().setEnabled(!timeUnit.isPredefined());
    }

    private void customizeButtons() {
        getSaveAndClose().addClickListener(event -> {
            saveEvent(event);
            fireEvent(new SaveAndCloseEvent(this, this.timeUnit));
        });
        getSave().addClickListener(event -> {
            saveEvent(event);
            fireEvent(new SaveEvent(this, this.timeUnit));
        });
        getCrossClose().addClickListener(this::closeEvent);
        getClose().addClickListener(this::closeEvent);
    }

    private void saveEvent(ClickEvent<Button> event) {
        var validationDone = false;
        try {
            validationDone = validate();
        } catch (StandardError error) {
            var confDialog = new ConfirmDialog();
            confDialog.setText(error.getMessage());
            confDialog.open();
        }

        if (!validationDone) return;

        try {
            binder.writeBean(this.timeUnit);
        } catch (ValidationException error) {
            var confDialog = new ConfirmDialog();
            confDialog.setText(error.getMessage());
            confDialog.open();
//            return;
        }
    }

    private void closeEvent(ClickEvent<Button> event) {
        fireEvent(new CloseEvent(this, this.timeUnit));
    }

    private String getMessage() {
        return "The number of hours must be greater than 0";
    }

    private boolean validate() {
        if (numberOfHours.getValue() < 0)
            throw new StandardError(getMessage());
        return true;
    }

}
