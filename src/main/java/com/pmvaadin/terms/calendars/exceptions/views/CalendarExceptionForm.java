package com.pmvaadin.terms.calendars.exceptions.views;

import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.terms.calendars.common.IntervalGrid;
import com.pmvaadin.terms.calendars.exceptions.CalendarException;
import com.pmvaadin.terms.calendars.exceptions.CalendarExceptionSetting;
import com.pmvaadin.terms.calendars.workingweeks.IntervalSetting;
import com.pmvaadin.terms.calendars.workingweeks.views.WorkingWeekForm;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;

import java.time.DayOfWeek;

public class CalendarExceptionForm extends Dialog {

    private final CalendarException calendarException;
    private final TextField name = new TextField();
    private final DatePicker start = new DatePicker();
    private final DatePicker finish = new DatePicker();
    private final RadioButtonGroup<CalendarExceptionSetting> settingRadioButton = new RadioButtonGroup<>();
    private final Intervals intervals = new Intervals();

    public CalendarExceptionForm(CalendarException calendarException) {
        this.calendarException = calendarException;
        customizeForm();
        customizeElements();
        customizeHeader();
        createButtons();
        refreshHeader();
    }

    private void customizeForm() {

        setDraggable(true);
        setResizable(true);
        addClassName("calendar_exception-form");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        setModal(false);
        setCloseOnEsc(false);

        var mainLayout = new FormLayout();
        mainLayout.addFormItem(name, "Name");
        mainLayout.addFormItem(start, "Start");
        mainLayout.addFormItem(finish, "Finish");
        mainLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("300px", 3));


        settingRadioButton.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        settingRadioButton.setItems(CalendarExceptionSetting.values());
        settingRadioButton.addValueChangeListener(this::settingRadioButtonChangeListener);

        var intervalSetting = new VerticalLayout(settingRadioButton, intervals);
        intervalSetting.setWidthFull();

        var horizontalLayout = new HorizontalLayout(days, intervalSetting);
        horizontalLayout.setWidthFull();

        var verticalLayout = new VerticalLayout(mainLayout, horizontalLayout);
        verticalLayout.setWidthFull();

        add(verticalLayout);


    }

    private void settingRadioButtonChangeListener(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<CalendarExceptionSetting>, CalendarExceptionSetting> event) {
        intervals.setEnabled(event.getValue() == CalendarExceptionSetting.WORKING_TIMES);
    }

    private void customizeElements() {

        days.setItems(DayOfWeek.values());
        days.addColumn(DayOfWeek::toString);
        days.setWidthFull();
        intervals.setEnabled(false);
        intervals.setWidthFull();
        start.setEnabled(!workingWeek.isDefault());
        start.addValueChangeListener(this::startValueChangeListener);
        finish.setEnabled(!workingWeek.isDefault());
        finish.addValueChangeListener(this::finishValueChangeListener);
        days.addSelectionListener(this::daysSelectionListener);

    }

    private void customizeHeader() {

        Button closeButton = new Button(new Icon("lumo", "cross"),
                e -> {
                    fireEvent(new WorkingWeekForm.CloseEvent(this, this.workingWeek));
                    this.close();
                });
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickShortcut(Key.ESCAPE);

        getHeader().add(closeButton);

    }

    private void createButtons() {

        Button ok = new Button("Ok");
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
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
            writeChanges();
            fireEvent(new WorkingWeekForm.SaveEvent(this, this.workingWeek));
            close();

        });

        Button close = new Button("Cancel");
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        close.addClickListener(event ->
        {
            fireEvent(new WorkingWeekForm.CloseEvent(this, this.workingWeek));
            close();
        });

        getFooter().add(ok, close);

    }

    private void refreshHeader() {
        var exceptionName = this.calendarException.getName();
        if (exceptionName == null) exceptionName = "";
        var title = CalendarException.getHeaderName();
        setHeaderTitle(title + exceptionName);
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    public static abstract class CalendarExceptionFormEvent extends ComponentEvent<CalendarExceptionForm> {

        private final CalendarException calendarException;

        protected CalendarExceptionFormEvent(CalendarExceptionForm source, CalendarException calendarException) {
            super(source, false);
            this.calendarException = calendarException;
        }

        public CalendarException getCalendarException() {
            return this.calendarException;
        }

    }

    public static class CloseEvent extends CalendarExceptionFormEvent {
        CloseEvent(CalendarExceptionForm source, CalendarException calendarException) {
            super(source, calendarException);
        }
    }

    public static class SaveEvent extends CalendarExceptionFormEvent {
        SaveEvent(CalendarExceptionForm source, CalendarException calendarException) {
            super(source, calendarException);
        }
    }

    private class Intervals extends IntervalGrid {

    }

}
