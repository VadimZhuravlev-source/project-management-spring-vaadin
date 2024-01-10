package com.pmvaadin.terms.timeunit.frontend.elements;

import com.pmvaadin.common.ComboBoxWithButtons;
import com.pmvaadin.common.services.ListService;
import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.pmvaadin.terms.timeunit.entity.TimeUnitRepresentation;
import com.pmvaadin.terms.timeunit.frontend.views.TimeUnitForm;
import com.pmvaadin.terms.timeunit.frontend.views.TimeUnitSelectionForm;
import com.pmvaadin.terms.timeunit.services.TimeUnitService;
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
        this.selectionForm = selectionForm.getInstance();
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
            if (selectedItem instanceof TimeUnitRepresentation item)
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

    private void saveEvent(TimeUnitForm.SaveEvent event) {
        this.itemForm.close();
        var item = event.getItem();
        if (item instanceof TimeUnit timeUnit) {
            var savedItem = service.save(timeUnit);
            getComboBox().setValue(savedItem.getRep());
        }

    }

}
