package com.pmvaadin.resources.frontend.elements;

import com.pmvaadin.common.ComboBoxWithButtons;
import com.pmvaadin.common.services.ListService;
import com.pmvaadin.resources.entity.LaborResource;
import com.pmvaadin.resources.entity.LaborResourceRepresentation;
import com.pmvaadin.resources.frontend.views.LaborResourceForm;
import com.pmvaadin.resources.frontend.views.LaborResourceSelectionForm;
import com.pmvaadin.resources.services.LaborResourceService;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
public class LaborResourceComboBox extends ComboBoxWithButtons<LaborResourceRepresentation> {

    private final LaborResourceService service;
    private final LaborResourceSelectionForm selectionForm;
    private final LaborResourceForm itemForm;
    private ListService<LaborResourceRepresentation, LaborResource> itemService;

    public LaborResourceComboBox(LaborResourceService service,
                                 LaborResourceSelectionForm selectionForm,
                                 LaborResourceForm itemForm) {

        this.service = service;
        this.selectionForm = selectionForm.getInstance();
        this.itemForm = itemForm;
        if (service instanceof ListService<?, ?> itemService) {
            this.itemService = (ListService<LaborResourceRepresentation, LaborResource>) itemService;
        } else
            return;

        this.setDefaultDataProvider(this.itemService);

        getSelectionAction().setVisible(true);
        getOpenAction().setVisible(true);
        this.selectionForm.addListener(LaborResourceSelectionForm.SelectEvent.class, event -> {
            var selectedItems = event.getSelectedItems();
            var selectedItemOpt = selectedItems.stream().findFirst();
            if (selectedItemOpt.isEmpty()) return;
            var selectedItem = selectedItemOpt.get();
            if (selectedItem instanceof LaborResourceRepresentation item)
                getComboBox().setValue(item);

        });
        this.getSelectionAction().addClickListener(event -> this.selectionForm.open());

        this.getOpenAction().addClickListener(event -> {
            var value = this.getComboBox().getValue();
            if (value == null) return;
            var item = this.itemService.get(value);
            this.itemForm.read(item);
            this.itemForm.open();
            this.itemForm.addListener(LaborResourceForm.SaveEvent.class, this::saveEvent);
        });

        this.setWidthFull();
    }

    public LaborResourceComboBox getInstance() {
        return new LaborResourceComboBox(service, selectionForm, itemForm);
    }

    private void saveEvent(LaborResourceForm.SaveEvent event) {
//        this.itemForm.close();
        var item = event.getItem();
        if (item instanceof LaborResource laborResource) {
            var laborRes = service.save(laborResource);
            this.itemForm.read(laborRes);
            getComboBox().setValue(laborRes.getRep());
        }
    }

}
