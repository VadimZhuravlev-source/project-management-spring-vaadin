package com.pmvaadin.resources.frontend.views;

import com.pmvaadin.common.DialogForm;
import com.pmvaadin.common.services.ListService;
import com.pmvaadin.resources.entity.LaborResource;
import com.pmvaadin.resources.frontend.elements.LaborResourceList;
import com.pmvaadin.resources.services.LaborResourceService;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
public class LaborResourceSelectionForm extends DialogForm {

    private final LaborResourceService laborResourceService;
    private LaborResourceList list;

    public LaborResourceSelectionForm(LaborResourceService laborResourceService) {
        this.laborResourceService = laborResourceService;
        if (!(laborResourceService instanceof ListService)) {
            return;
        }

        list = new LaborResourceList((ListService) laborResourceService);
        add(list);
        customizeForm();
    }

    public LaborResourceSelectionForm getInstance() {
        return new LaborResourceSelectionForm(this.laborResourceService);
    }

    private void customizeForm() {

        this.setHeaderTitle("Choose a labor resource");
        getClose().setVisible(false);
        getSave().setVisible(false);
        getSave().setVisible(false);
        getSaveAndClose().setVisible(false);
        getSelect().addClickListener(event -> fireEvent());
        list.onMouseDoubleClick(e -> fireEvent());

    }

    private void fireEvent() {
        fireEvent(new SelectEvent(this, list.getGrid().getSelectedItems()));
        this.close();
    }

}
