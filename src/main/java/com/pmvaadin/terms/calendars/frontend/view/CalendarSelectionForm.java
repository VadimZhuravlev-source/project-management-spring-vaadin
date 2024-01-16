package com.pmvaadin.terms.calendars.frontend.view;

import com.pmvaadin.common.DialogForm;
import com.pmvaadin.common.services.ListService;
import com.pmvaadin.terms.calendars.frontend.elements.CalendarList;
import com.pmvaadin.terms.calendars.services.CalendarService;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
public class CalendarSelectionForm extends DialogForm {
    private final CalendarService service;
    private final CalendarForm calendarForm;
    private CalendarList list;

    public CalendarSelectionForm(CalendarService service, CalendarForm calendarForm) {
        this.service = service;
        this.calendarForm = calendarForm;
        if (!(service instanceof ListService)) {
            return;
        }

        list = new CalendarList((ListService) service, calendarForm);
        add(list);
        customizeForm();
        this.addOpenedChangeListener(event -> list.removeSelectionColumn());
    }

    public CalendarSelectionForm newInstance() {
        return new CalendarSelectionForm(this.service, this.calendarForm);
    }

    private void customizeForm() {

        this.setHeaderTitle("Choose a calendar");
        setAsSelectForm();
        getCrossClose().addClickListener(event -> this.close());
        getSelect().addClickListener(event -> fireEvent());
        list.onMouseDoubleClick(e -> fireEvent());

    }

    private void fireEvent() {
        fireEvent(new DialogForm.SelectEvent(this, list.getGrid().getSelectedItems()));
        this.close();
    }
}
