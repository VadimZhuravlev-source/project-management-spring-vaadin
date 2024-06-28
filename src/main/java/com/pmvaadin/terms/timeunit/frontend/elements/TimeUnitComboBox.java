package com.pmvaadin.terms.timeunit.frontend.elements;

import com.pmvaadin.common.ComboBoxWithButtons;
import com.pmvaadin.common.services.ListService;
import com.pmvaadin.project.data.ProjectTaskData;
import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.pmvaadin.terms.timeunit.entity.TimeUnitRepresentation;
import com.pmvaadin.terms.timeunit.frontend.views.TimeUnitForm;
import com.pmvaadin.terms.timeunit.frontend.views.TimeUnitSelectionForm;
import com.pmvaadin.terms.timeunit.services.TimeUnitService;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
public class TimeUnitComboBox extends ComboBoxWithButtons<TimeUnitRepresentation> {

    private final TimeUnitService service;
    private final TimeUnitSelectionForm selectionForm;
    private final TimeUnitForm itemForm = new TimeUnitForm();
    private ListService<TimeUnitRepresentation, TimeUnit> itemService;

    public TimeUnitComboBox(TimeUnitService service,
                            TimeUnitSelectionForm selectionForm) {

        this.service = service;
        this.selectionForm = selectionForm.newInstance();
        if (service instanceof ListService<?, ?> itemService) {
            this.itemService = (ListService<TimeUnitRepresentation, TimeUnit>) itemService;
        } else
            return;

        this.setDefaultDataProvider(this.itemService);

        getSelectionAction().setVisible(true);
        getOpenAction().setVisible(true);
        this.selectionForm.addListener(TimeUnitSelectionForm.SelectEvent.class, event -> {
            var selectedItems = event.getSelectedItems();
            var selectedItemOpt = selectedItems.stream().findFirst();
            if (selectedItemOpt.isEmpty()) return;
            var selectedItem = selectedItemOpt.get();
            if (!(selectedItem instanceof TimeUnitRepresentation))
                return;
            var selectedItem2 = (TimeUnitRepresentation) selectedItem;
            TimeUnit item;
            if (selectedItem2 instanceof TimeUnit) {
                item = (TimeUnit) selectedItem2;
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
            this.itemForm.addListener(TimeUnitForm.SaveEvent.class, this::saveEvent);
        });

        this.setWidthFull();
    }

    public TimeUnitComboBox getInstance() {
        return new TimeUnitComboBox(service, selectionForm);
    }

    public TimeUnit getByRepresentation(TimeUnitRepresentation representation) {
        return this.itemService.get(representation);
    }

    public TimeUnit getTimeUnitValueChangeListener(HasValue.ValueChangeEvent<TimeUnitRepresentation> event,
                                                   ProjectTaskData projectTaskData) {

        var timeUnitRep = event.getValue();
        if (timeUnitRep == null) {
            timeUnitRep = event.getOldValue();
        }
        if (timeUnitRep == null) {
            timeUnitRep = projectTaskData.getTimeUnit();
            this.setValue(timeUnitRep);
        }

        TimeUnit timeUnit;
        if ((timeUnitRep instanceof TimeUnit))
            timeUnit = (TimeUnit) timeUnitRep;
        else
            timeUnit = this.getByRepresentation(timeUnitRep);
        return timeUnit;

    }

    private void saveEvent(TimeUnitForm.SaveEvent event) {
        var item = event.getItem();
        if (item instanceof TimeUnit timeUnit) {
            var savedItem = service.save(timeUnit);
            this.itemForm.read(savedItem);
            getComboBox().setValue(savedItem);
        }
    }

}
