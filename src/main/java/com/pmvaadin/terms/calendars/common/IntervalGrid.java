package com.pmvaadin.terms.calendars.common;

import com.pmvaadin.common.ObjectGrid;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.timepicker.TimePickerVariant;

import java.time.LocalTime;

public class IntervalGrid extends ObjectGrid<Interval> {

    public IntervalGrid() {

        addColumns();

    }

    private void addColumns() {

        var fromColumn = addColumn(Interval::getFrom).
                setHeader("From");
        var fromPicker = new TimePicker();
        fromPicker.addThemeVariants(TimePickerVariant.LUMO_SMALL);
        fromPicker.setWidthFull();
        addCloseHandler(fromPicker, this.editor);
        this.binder.forField(fromPicker)
                .withValidator(localTime -> {
                    var currentInterval = this.editor.getItem();
                    var previousIntervalOpt = grid.getListDataView().getPreviousItem(currentInterval);
                    if (previousIntervalOpt.isEmpty() || localTime == null)
                        return true;

                    return localTime.compareTo(previousIntervalOpt.get().getTo()) >= 0;
                }, "The start of a shaft must be later then the end of the previous shift.")
                .bind(Interval::getFrom, Interval::setFrom);
        fromColumn.setEditorComponent(fromPicker);

        var toColumn = addColumn(Interval::getTo).
                setHeader("To");
        var toPicker = new TimePicker();
        toPicker.addThemeVariants(TimePickerVariant.LUMO_SMALL);
        toPicker.setWidthFull();
        addCloseHandler(toPicker, this.editor);
        this.binder.forField(toPicker)
                .withValidator(localTime -> {
                    var currentInterval = this.editor.getItem();
                    if (localTime == null || localTime.equals(LocalTime.MIN)) return true;
                    return localTime.compareTo(currentInterval.getTo()) >= 0;
                }, "The end of a shaft must be later then the start.")
                .bind(Interval::getTo, Interval::setTo);
        toColumn.setEditorComponent(toPicker);

    }

}
