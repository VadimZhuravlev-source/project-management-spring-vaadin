package com.pmvaadin.terms.calendars.frontend.elements;

import com.pmvaadin.common.ComboBoxWithButtons;
import com.pmvaadin.common.services.ListService;
import com.pmvaadin.project.data.ProjectTaskData;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.entity.CalendarRepresentation;
import com.pmvaadin.terms.calendars.frontend.view.CalendarForm;
import com.pmvaadin.terms.calendars.frontend.view.CalendarSelectionForm;
import com.pmvaadin.terms.calendars.services.CalendarService;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
public class CalendarComboBox extends ComboBoxWithButtons<CalendarRepresentation> {

    private final CalendarService service;
    private final CalendarSelectionForm selectionForm;
    private final CalendarForm itemForm;
    private ListService<CalendarRepresentation, Calendar> itemService;

    public CalendarComboBox(CalendarService service,
                            CalendarSelectionForm selectionForm,
                            CalendarForm itemForm) {

        this.service = service;
        this.selectionForm = selectionForm.newInstance();
        this.itemForm = itemForm.newInstance();
        if (service instanceof ListService<?, ?> itemService) {
            this.itemService = (ListService<CalendarRepresentation, Calendar>) itemService;
        } else
            return;

        this.setDefaultDataProvider(this.itemService);

        getSelectionAction().setVisible(true);
        getOpenAction().setVisible(true);
        this.selectionForm.addListener(CalendarSelectionForm.SelectEvent.class, event -> {
            var selectedItems = event.getSelectedItems();
            var selectedItemOpt = selectedItems.stream().findFirst();
            if (selectedItemOpt.isEmpty()) return;
            var selectedItem = selectedItemOpt.get();
            if (!(selectedItem instanceof CalendarRepresentation))
                return;
            var selectedItem2 = (CalendarRepresentation) selectedItem;
            Calendar item;
            if (selectedItem2 instanceof Calendar) {
                item = (Calendar) selectedItem2;
            } else {
                item = this.itemService.get(selectedItem2);
            }
            getComboBox().setValue(item);
        });
        this.getSelectionAction().addClickListener(event -> this.selectionForm.open());

        this.getOpenAction().addClickListener(event -> {
            var value = this.getComboBox().getValue();
            if (value == null) return;
            var item = this.itemService.get(value);
            this.itemForm.read(item);
            this.itemForm.open();
            this.itemForm.addListener(CalendarForm.SaveEvent.class, this::saveEvent);
        });

        this.setWidthFull();
    }

    public CalendarComboBox getInstance() {
        return new CalendarComboBox(service, selectionForm, itemForm);
    }

    public Calendar getByRepresentation(CalendarRepresentation representation) {
        return this.itemService.get(representation);
    }

    public Calendar getCalendarChangeValueListener(HasValue.ValueChangeEvent<CalendarRepresentation> event,
                                                   ProjectTaskData projectTaskData) {

        var calendarRep = event.getValue();
        if (calendarRep == null) {
            calendarRep = event.getOldValue();
        }
        if (calendarRep == null) {
            calendarRep = projectTaskData.getCalendar();
            this.setValue(calendarRep);
        }

        Calendar calendar;
        if ((calendarRep instanceof Calendar))
            calendar = (Calendar) calendarRep;
        else
            calendar = this.getByRepresentation(calendarRep);
        return calendar;

    }

    private void saveEvent(CalendarForm.SaveEvent event) {
        var item = event.getItem();
        if (item instanceof Calendar timeUnit) {
            var savedItem = service.save(timeUnit);
            this.itemForm.read(savedItem);
            getComboBox().setValue(savedItem);
        }
    }

}
