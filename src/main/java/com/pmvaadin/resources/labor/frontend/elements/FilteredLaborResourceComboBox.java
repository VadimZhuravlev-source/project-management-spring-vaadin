package com.pmvaadin.resources.labor.frontend.elements;

import com.pmvaadin.resources.labor.frontend.views.LaborResourceForm;
import com.pmvaadin.resources.labor.frontend.views.LaborResourceSelectionForm;
import com.pmvaadin.resources.labor.services.FilteredLaborResourceService;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
public class FilteredLaborResourceComboBox extends LaborResourceComboBox {

    public FilteredLaborResourceComboBox(FilteredLaborResourceService service,
                                         LaborResourceSelectionForm selectionForm,
                                         LaborResourceForm itemForm) {
        super(service, selectionForm, itemForm);
    }

}
