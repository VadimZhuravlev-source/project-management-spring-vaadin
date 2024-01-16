package com.pmvaadin.resources.frontend.views;

import com.pmvaadin.common.DialogForm;
import com.pmvaadin.common.services.ListService;
import com.pmvaadin.resources.frontend.elements.LaborResourceList;
import com.pmvaadin.resources.services.LaborResourceService;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
public class LaborResourceSelectionForm extends DialogForm {

    private final LaborResourceService service;
    private LaborResourceList list;

    public LaborResourceSelectionForm(LaborResourceService service) {
        this.service = service;
        if (!(service instanceof ListService)) {
            return;
        }

        list = new LaborResourceList((ListService) service);
        add(list);
        customizeForm();
        this.addOpenedChangeListener(event -> list.removeSelectionColumn());
    }



    public LaborResourceSelectionForm newInstance() {
        return new LaborResourceSelectionForm(this.service);
    }

    private void customizeForm() {

        this.setHeaderTitle("Choose a labor resource");
        setAsSelectForm();
        getCrossClose().addClickListener(e -> this.close());
        getSelect().addClickListener(event -> fireEvent());
        list.onMouseDoubleClick(e -> fireEvent());

    }

    private void fireEvent() {
        fireEvent(new SelectEvent(this, list.getGrid().getSelectedItems()));
        this.close();
    }

}
