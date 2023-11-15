package com.pmvaadin.terms.calendars.exceptions.views;

import com.pmvaadin.commonobjects.ObjectGrid;
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
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkingWeekForm extends Dialog {

    private final WorkingWeek workingWeek;
    private final WorkingTime workingTimeInstance;

    private final TextField name = new TextField("Name");
    private final DatePicker start = new DatePicker("Start");
    private final DatePicker finish = new DatePicker("Finish");

    private final Grid<DayOfWeek> days = new Grid<>();

    private final Binder<WorkingWeek> binder = new Binder<>();

    private final ObjectGrid<Interval> intervals = new ObjectGrid<>();

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
    }

    private void fillMapIntervalChanges() {
        var workingTimes = workingWeek.getWorkingTimes();
        workingTimes.forEach(workingTime -> {
            mapIntervalChanges.put(workingTime.getDayOfWeek(),
                    new WorkingDaysSetting(workingTime.getIntervalSetting(), workingTime.getIntervals()));
        });
    }

    private void customizeElements() {

        binder.readBean(this.workingWeek);
        days.setItems(DayOfWeek.values());
        days.addColumn(DayOfWeek::toString);
        days.setSelectionMode(Grid.SelectionMode.MULTI);
        days.getElement().getNode().runWhenAttached(ui ->
                ui.beforeClientResponse(this, context ->
                        getElement().executeJs(
                                "if (this.querySelector('vaadin-grid-flow-selection-column')) {" +
                                        " this.querySelector('vaadin-grid-flow-selection-column').hidden = true }")));
        intervals.setEnabled(false);
        days.select(DayOfWeek.MONDAY);

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

        RadioButtonGroup<IntervalSetting> radioGroup = new RadioButtonGroup<>();
        radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroup.setItems(IntervalSetting.values());
        radioGroup.addValueChangeListener(this::radioButtonChangeListener);

        var intervalSetting = new VerticalLayout(radioGroup, intervals);

        var horizontalLayout = new HorizontalLayout(days, intervalSetting);

        var verticalLayout = new VerticalLayout(mainLayout, horizontalLayout);

        add(verticalLayout);


    }

    private void radioButtonChangeListener(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<IntervalSetting>, IntervalSetting> event) {

        var selectedSetting = event.getValue();
        if (selectedSetting == IntervalSetting.CUSTOM) {
            intervals.setEnabled(true);
            return;
        }
        intervals.setEnabled(false);

        var selectedDays = days.getSelectedItems();
        if (selectedDays.isEmpty()) return;

        selectedDays.forEach(dayOfWeek -> {
            var workingDaysSetting = mapIntervalChanges.get(dayOfWeek);
            if (workingDaysSetting == null) return;
            if (selectedSetting == IntervalSetting.NONWORKING)
            var intervalList = workingTimeInstance.getDefaultIntervals(dayOfWeek, workingWeek.getCalendar().getSetting());
            workingDaysSetting.intervalSetting
        });


    }

    private void customizeHeader() {

        Button closeButton = new Button(new Icon("lumo", "cross"),
                e -> fireEvent(new CloseEvent(this))
        );
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickShortcut(Key.ESCAPE);

        getHeader().add(closeButton);

    }

    private void createButtons() {

        Button ok = new Button("Ok");
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        ok.addClickListener(event -> {

            boolean validationDone = validateAndSave();
            if (!validationDone) return;
            fireEvent(new SaveEvent(this));

        });

        Button close = new Button("Cancel");
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));

        getFooter().add(ok, close);

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

        protected WorkingWeekFormEvent(WorkingWeekForm source) {
            super(source, false);
        }

    }

    public static class CloseEvent extends WorkingWeekFormEvent {
        CloseEvent(WorkingWeekForm source) {
            super(source);
        }
    }

    public static class SaveEvent extends WorkingWeekFormEvent {
        SaveEvent(WorkingWeekForm source) {
            super(source);
        }
    }

    private record WorkingDaysSetting(IntervalSetting intervalSetting, List<Interval> intervals) {}

}

