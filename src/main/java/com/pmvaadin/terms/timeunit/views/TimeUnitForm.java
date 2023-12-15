package com.pmvaadin.terms.timeunit.views;

import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projecttasks.commonobjects.BigDecimalToDoubleConverter;
import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

public class TimeUnitForm extends Dialog {

    private final TimeUnit timeUnit;
    private final TextField name = new TextField("Name");
    private final NumberField numberOfHours = new NumberField("Hours");
    private final Binder<TimeUnit> binder = new Binder<>(TimeUnit.class);

    public TimeUnitForm(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        name.setEnabled(!timeUnit.isPredefined());
        numberOfHours.setEnabled(!timeUnit.isPredefined());
        numberOfHours.setStepButtonsVisible(true);
        numberOfHours.setStep(1);
        numberOfHours.setMin(1);
        binder.forField(numberOfHours).withConverter(new BigDecimalToDoubleConverter(numberOfHours))
                .withValidator(bigDecimal -> bigDecimal.doubleValue() > 0, getMessage())
                .bind(TimeUnit::getNumberOfHours, TimeUnit::setNumberOfHours);
        binder.bindInstanceFields(this);
        binder.readBean(this.timeUnit);
        add(name, numberOfHours);
        customizeHeader();
        createButtons();
    }

    private void customizeHeader() {

        var timeUnitName = this.timeUnit.getName();
        if (timeUnitName == null) timeUnitName = "";
        var title = "Time unit: ";
        setHeaderTitle(title + timeUnitName);

        Button closeButton = new Button(new Icon("lumo", "cross"),
                e -> {
                    fireEvent(new CloseEvent(this, this.timeUnit));
                    this.close();
                });
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickShortcut(Key.ESCAPE);

        getHeader().add(closeButton);

    }

    private void createButtons() {

        Button ok = new Button("Ok");
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        ok.setEnabled(!timeUnit.isPredefined());
        ok.addClickListener(event -> {

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
                return;
            }
            fireEvent(new SaveEvent(this, this.timeUnit));
            close();

        });

        Button close = new Button("Cancel");
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        close.addClickListener(event ->
        {
            fireEvent(new CloseEvent(this, this.timeUnit));
            close();
        });

        getFooter().add(ok, close);

    }

    private String getMessage() {
        return "The hours must be greater than 0";
    }

    private boolean validate() {
        if (numberOfHours.getValue() < 0)
            throw new StandardError(getMessage());
        return true;
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    public static abstract class TimeUnitFormEvent extends ComponentEvent<TimeUnitForm> {

        private final TimeUnit timeUnit;
        protected TimeUnitFormEvent(TimeUnitForm source, TimeUnit timeUnit) {
            super(source, false);
            this.timeUnit = timeUnit;
        }

        public TimeUnit getTimeUnit() {
            return this.timeUnit;
        }

    }

    public static class CloseEvent extends TimeUnitForm.TimeUnitFormEvent {
        CloseEvent(TimeUnitForm source, TimeUnit timeUnit) {
            super(source, timeUnit);
        }
    }

    public static class SaveEvent extends TimeUnitForm.TimeUnitFormEvent {
        SaveEvent(TimeUnitForm source, TimeUnit timeUnit) {
            super(source, timeUnit);
        }
    }


}
