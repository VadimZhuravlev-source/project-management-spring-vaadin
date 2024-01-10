package com.pmvaadin.terms.timeunit.frontend.views;

import com.pmvaadin.common.DialogForm;
import com.pmvaadin.common.services.ListService;
import com.pmvaadin.terms.timeunit.frontend.elements.TimeUnitList;
import com.pmvaadin.terms.timeunit.services.TimeUnitService;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
public class TimeUnitSelectionForm extends DialogForm {

    private final TimeUnitService service;
    private TimeUnitList list;

    public TimeUnitSelectionForm(TimeUnitService service) {
        this.service = service;
        if (!(service instanceof ListService)) {
            return;
        }

        list = new TimeUnitList((ListService) service);
        add(list);
        customizeForm();
        this.addOpenedChangeListener(event -> list.removeSelectionColumn());
    }

    public TimeUnitSelectionForm getInstance() {
        return new TimeUnitSelectionForm(this.service);
    }

    private void customizeForm() {

        this.setHeaderTitle("Choose a time unit");
        setAsSelectForm();
        getCrossClose().addClickListener(event -> close());
        getSelect().addClickListener(event -> fireEvent());
        list.onMouseDoubleClick(e -> fireEvent());

    }

    private void fireEvent() {
        fireEvent(new DialogForm.SelectEvent(this, list.getGrid().getSelectedItems()));
        this.close();
    }

}
