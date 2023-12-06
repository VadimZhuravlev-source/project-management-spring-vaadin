package com.pmvaadin.terms.calendars.workingweeks.views;

import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.terms.calendars.common.Interval;
import com.pmvaadin.terms.calendars.common.IntervalGrid;
import com.pmvaadin.terms.calendars.validators.CalendarValidation;
import com.pmvaadin.terms.calendars.validators.CalendarValidationImpl;
import com.pmvaadin.terms.calendars.workingweeks.IntervalSetting;
import com.pmvaadin.terms.calendars.workingweeks.WorkingTime;
import com.pmvaadin.terms.calendars.workingweeks.WorkingWeek;
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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.shared.Registration;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class WorkingWeekForm extends Dialog {

    private final WorkingWeek workingWeek;
    private final WorkingTime workingTimeInstance;

    private final TextField name = new TextField();
    private final DatePicker start = new DatePicker();
    private final DatePicker finish = new DatePicker();

    private final Grid<DayOfWeek> days = new Grid<>();

    private final IntervalGrid2 intervals = new IntervalGrid2();
    private final RadioButtonGroup<IntervalSetting> radioGroup = new RadioButtonGroup<>();
    private final Map<DayOfWeek, WorkingDaysSetting> mapIntervalChanges = new HashMap<>();

    private final CalendarValidation calendarValidation = new CalendarValidationImpl();

    public WorkingWeekForm(WorkingWeek workingWeek) {
        this.workingWeek = workingWeek;
        this.workingTimeInstance = workingWeek.getWorkingTimeInstance();
        fillMapIntervalChanges();
        customizeForm();
        customizeElements();
        customizeHeader();
        createButtons();
        refreshHeader();
        name.setValue(this.workingWeek.getName());
        start.setValue(this.workingWeek.getStart());
        finish.setValue(this.workingWeek.getFinish());
        days.select(DayOfWeek.MONDAY);
    }

    private void fillMapIntervalChanges() {
        var workingTimes = workingWeek.getWorkingTimes();
        workingTimes.forEach(workingTime -> mapIntervalChanges.put(workingTime.getDayOfWeek(),
                new WorkingDaysSetting(workingTime.getIntervalSetting(), workingTime.getCopyOfIntervals())));
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

    private void startValueChangeListener(AbstractField.ComponentValueChangeEvent<DatePicker, LocalDate> event) {
        var value = event.getValue();
        if (value != null && (finish.getValue() == null || value.compareTo(finish.getValue()) > 0)) finish.setValue(value);
    }

    private void finishValueChangeListener(AbstractField.ComponentValueChangeEvent<DatePicker, LocalDate> event) {
        var value = event.getValue();
        if (value != null && (start.getValue() == null || value.compareTo(start.getValue()) > 0)) start.setValue(value);
    }


    private void daysSelectionListener(SelectionEvent<Grid<DayOfWeek>, DayOfWeek> event) {

        var selectedDay = event.getFirstSelectedItem().orElse(null);
        if (selectedDay == null) return;
        var values = mapIntervalChanges.getOrDefault(selectedDay, null);
        if (values == null) return;

        intervals.closeEditingItem();

        if (radioGroup.getValue() == values.intervalSetting)
            refreshIntervals(values.intervalSetting);
        radioGroup.setValue(values.intervalSetting);

    }

    private void customizeForm() {

        setDraggable(true);
        setResizable(true);
        addClassName("working-week-form");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        setModal(false);
        setCloseOnEsc(false);

        var mainLayout = new FormLayout();
        mainLayout.addFormItem(name, "Name");
        mainLayout.addFormItem(start, "Start");
        mainLayout.addFormItem(finish, "Finish");
        mainLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("300px", 3));


        radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroup.setItems(IntervalSetting.values());
        radioGroup.addValueChangeListener(this::radioButtonChangeListener);

        var intervalSetting = new VerticalLayout(radioGroup, intervals);
        intervalSetting.setWidthFull();

        var horizontalLayout = new HorizontalLayout(days, intervalSetting);
        horizontalLayout.setWidthFull();

        var verticalLayout = new VerticalLayout(mainLayout, horizontalLayout);
        verticalLayout.setWidthFull();

        add(verticalLayout);


    }

    private void radioButtonChangeListener(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<IntervalSetting>, IntervalSetting> event) {

        var selectedSetting = event.getValue();
        refreshIntervals(selectedSetting);

    }

    private void refreshIntervals(IntervalSetting selectedSetting) {

        intervals.setEnabled(selectedSetting == IntervalSetting.CUSTOM);
        intervals.endEditing();

        var selectedDays = days.getSelectedItems();

        selectedDays.forEach(dayOfWeek -> {
            var workingDaysSetting = mapIntervalChanges.get(dayOfWeek);
            if (workingDaysSetting == null) {
                workingDaysSetting = new WorkingDaysSetting(IntervalSetting.CUSTOM, new ArrayList<>());
                mapIntervalChanges.put(dayOfWeek, workingDaysSetting);
            }
            workingDaysSetting.setIntervalSetting(selectedSetting);
            if (selectedSetting == IntervalSetting.CUSTOM) {
                intervals.setItems(workingDaysSetting.getIntervals());
                return;
            }
            workingDaysSetting.getIntervals().clear();
            if (selectedSetting == IntervalSetting.DEFAULT) {
                var intervalList = workingTimeInstance.getDefaultIntervals(dayOfWeek, workingWeek.getCalendar().getSetting());
                workingDaysSetting.getIntervals().addAll(intervalList);
                intervals.setItems(intervalList);
            }
        });

    }

    private void customizeHeader() {

        Button closeButton = new Button(new Icon("lumo", "cross"),
                e -> {
            fireEvent(new CloseEvent(this, this.workingWeek));
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
            fireEvent(new SaveEvent(this, this.workingWeek));
            close();

        });

        Button close = new Button("Cancel");
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        close.addClickListener(event ->
        {
            fireEvent(new CloseEvent(this, this.workingWeek));
            close();
        });

        getFooter().add(ok, close);

    }

    private boolean validate() {

        calendarValidation.validateDates(this.workingWeek, this.start.getValue(), this.finish.getValue());
        mapIntervalChanges.forEach((dayOfWeek, workingDaysSetting) ->
                calendarValidation.validateIntervals(dayOfWeek, workingDaysSetting.getIntervals()));

        return true;

    }

    private void writeChanges() {

        this.workingWeek.setName(name.getValue());
        this.workingWeek.setStart(start.getValue());
        this.workingWeek.setFinish(finish.getValue());

        if (this.workingWeek.getWorkingTimes().size() != DayOfWeek.values().length)
            this.workingWeek.fillDefaultWorkingTimes();
        this.workingWeek.getWorkingTimes().forEach(workingTime -> {
            var changes = mapIntervalChanges.get(workingTime.getDayOfWeek());
            if (changes == null)
                return;
            workingTime.setIntervalSetting(changes.getIntervalSetting());
            workingTime.setIntervals(changes.getIntervals());
        });

    }

    private void refreshHeader() {
        var workingWeekName = workingWeek.getName();
        if (workingWeekName == null) workingWeekName = "";
        var title = WorkingWeek.getHeaderName();
        setHeaderTitle(title + workingWeekName);
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    public static abstract class WorkingWeekFormEvent extends ComponentEvent<WorkingWeekForm> {

        private final WorkingWeek workingWeek;

        protected WorkingWeekFormEvent(WorkingWeekForm source, WorkingWeek workingWeek) {
            super(source, false);
            this.workingWeek = workingWeek;
        }

        public WorkingWeek getWorkingWeek() {
            return workingWeek;
        }

    }

    public static class CloseEvent extends WorkingWeekFormEvent {
        CloseEvent(WorkingWeekForm source, WorkingWeek workingWeek) {
            super(source, workingWeek);
        }
    }

    public static class SaveEvent extends WorkingWeekFormEvent {
        SaveEvent(WorkingWeekForm source, WorkingWeek workingWeek) {
            super(source, workingWeek);
        }
    }

    @Data
    @AllArgsConstructor
    private static class WorkingDaysSetting {

        private IntervalSetting intervalSetting;
        private List<Interval> intervals;

    }

    private class IntervalGrid2 extends IntervalGrid {

        IntervalGrid2() {

            customizeGrid();

        }

        public void closeEditingItem() {

            if (!isEditing() || binder.isValid()) return;

            var unmatchedInterval = editor.getItem();
            mapIntervalChanges.forEach((dayOfWeek, workingDaysSetting) -> {
                var iterator = workingDaysSetting.getIntervals().iterator();
                while (iterator.hasNext()) {
                    var interval = iterator.next();
                    if (interval != unmatchedInterval) return;
                    iterator.remove();
                }
            });

        }

        private void customizeGrid() {

            this.setInstantiatable(this::onAddInterval);
            this.setDeletable(true);

        }

        private Interval onAddInterval() {

            var newInterval = workingTimeInstance.getIntervalInstance();
            var previousTimeTo = this.grid.getListDataView().getItems().map(Interval::getTo)
                    .max(Comparator.naturalOrder()).orElse(LocalTime.MIN);
            newInterval.setFrom(previousTimeTo.plusHours(1));
            newInterval.setTo(previousTimeTo.plusHours(2));
            var selectedDays = days.getSelectedItems();
            selectedDays.forEach(dayOfWeek -> {
                var workingDaysSetting = mapIntervalChanges.get(dayOfWeek);
                if (workingDaysSetting == null) return;
                var intervalList = workingDaysSetting.getIntervals();
                intervalList.add(newInterval);
            });
            return newInterval;

        }

    }

}

