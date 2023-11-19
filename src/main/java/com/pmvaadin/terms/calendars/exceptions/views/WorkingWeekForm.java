package com.pmvaadin.terms.calendars.exceptions.views;

import com.pmvaadin.commonobjects.ObjectGrid;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.terms.calendars.common.Interval;
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
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.shared.Registration;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkingWeekForm extends Dialog {

    private final WorkingWeek workingWeek;
    private final WorkingTime workingTimeInstance;

    private final TextField name = new TextField();
    private final DatePicker start = new DatePicker();
    private final DatePicker finish = new DatePicker();

    private final Grid<DayOfWeek> days = new Grid<>();

    private final IntervalGrid intervals = new IntervalGrid();
    private final RadioButtonGroup<IntervalSetting> radioGroup = new RadioButtonGroup<>();
    private final Map<DayOfWeek, WorkingDaysSetting> mapIntervalChanges = new HashMap<>();

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
        if (value.compareTo(finish.getValue()) > 0) finish.setValue(value);
    }

    private void finishValueChangeListener(AbstractField.ComponentValueChangeEvent<DatePicker, LocalDate> event) {
        var value = event.getValue();
        if (value.compareTo(start.getValue()) < 0) start.setValue(value);
    }


    private void daysSelectionListener(SelectionEvent<Grid<DayOfWeek>, DayOfWeek> event) {

        var selectedDay = event.getFirstSelectedItem().orElse(null);
        if (selectedDay == null) return;
        var values = mapIntervalChanges.getOrDefault(selectedDay, null);
        if (values == null) return;
        if (radioGroup.getValue() == values.intervalSetting)
            refreshIntervals(values.intervalSetting);
        radioGroup.setValue(values.intervalSetting);

    }

    private void customizeForm() {

        setDraggable(true);
        setResizable(true);
        addClassName("calendar-form");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        setModal(false);
        setCloseOnEsc(false);
        //this.addListener(Class<ProjectTaskForm>, )

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
            if (workingDaysSetting == null) return;
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

        if (start.getValue() == null || finish.getValue() == null) return false;

        mapIntervalChanges.forEach((dayOfWeek, workingDaysSetting) -> {
            var intervals = workingDaysSetting.intervals;
            LocalTime previousTo = null;
            for (Interval interval: intervals) {
                if (interval.getFrom().compareTo(interval.getTo()) >= 0) return;
                if (previousTo != null && previousTo.compareTo(interval.getFrom()) > 0) return;
                previousTo = interval.getTo();
            }
        });

        return true;

    }

    private void refreshHeader() {
        var workingWeekName = workingWeek.getName();
        if (workingWeekName == null) workingWeekName = "";
        var title = WorkingWeek.getHeaderName();
        setHeaderTitle(title + " for " + workingWeekName);
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

    private class IntervalGrid extends ObjectGrid<Interval> {

        IntervalGrid() {

            customizeGrid();
            addColumns();

        }

        private void customizeGrid() {

            this.setInstantiatable(this::onAddInterval);
            this.setDeletable(true);

        }

        private Interval onAddInterval() {

            var newInterval = workingTimeInstance.getIntervalInstance();
            var maxSort = this.grid.getListDataView().getItems().map(Interval::getSort).max(Comparator.naturalOrder()).orElse(-1);
            newInterval.setSort(++maxSort);
            var selectedDays = days.getSelectedItems();
            selectedDays.forEach(dayOfWeek -> {
                var workingDaysSetting = mapIntervalChanges.get(dayOfWeek);
                if (workingDaysSetting == null) return;
                var intervalList = workingDaysSetting.getIntervals();
                intervalList.add(newInterval);
            });
            return newInterval;

        }

        private void addColumns() {

            var fromColumn = addColumn(Interval::getFrom).
                    setHeader("From");
            var fromPicker = new TimePicker();
            fromPicker.setWidthFull();
            addCloseHandler(fromPicker, this.editor);
            this.binder.forField(fromPicker)
                    .withValidator(localTime -> {
                var currentInterval = this.editor.getItem();
                var previousIntervalOpt = grid.getListDataView().getPreviousItem(currentInterval);
                if (previousIntervalOpt.isEmpty()) return true;
                return localTime.compareTo(previousIntervalOpt.get().getTo()) >= 0;
            }, "The start of a shaft must be later then the end of the previous shift.")
                    .bind(Interval::getFrom, Interval::setFrom);
            fromColumn.setEditorComponent(fromPicker);

            var toColumn = addColumn(Interval::getTo).
                    setHeader("To");
            var toPicker = new TimePicker();
            toPicker.setWidthFull();
            addCloseHandler(toPicker, this.editor);
            this.binder.forField(toPicker)
                    .withValidator(localTime -> {
                var currentInterval = this.editor.getItem();
                return localTime.compareTo(currentInterval.getFrom()) <= 0;
            }, "The end of a shaft must be later then the start.")
                    .bind(Interval::getTo, Interval::setTo);
            toColumn.setEditorComponent(toPicker);

        }

    }

}

