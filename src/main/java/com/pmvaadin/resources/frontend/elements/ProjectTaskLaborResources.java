package com.pmvaadin.resources.frontend.elements;

import com.pmvaadin.common.ComboBoxWithButtons;
import com.pmvaadin.common.ObjectGrid;
import com.pmvaadin.projecttasks.common.BigDecimalToDoubleConverter;
import com.pmvaadin.projecttasks.resources.entity.TaskResource;
import com.pmvaadin.resources.entity.LaborResource;
import com.pmvaadin.resources.frontend.views.LaborResourceForm;
import com.pmvaadin.resources.frontend.views.LaborResourceSelectionForm;
import com.pmvaadin.resources.services.LaborResourceService;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
public class ProjectTaskLaborResources extends ObjectGrid<TaskResource> {

//    private final LaborResourceService laborResourceService;
//    private final LaborResourceSelectionForm laborResourceSelectionForm;
//    private final LaborResourceForm laborResourceForm;
    private final LaborResourceComboBox laborResourceComboBox;

    public ProjectTaskLaborResources(LaborResourceComboBox laborResourceComboBox
//            LaborResourceService laborResourceService,
//                                     LaborResourceSelectionForm laborResourceSelectionForm,
//                                     LaborResourceForm laborResourceForm
    ) {
//        this.laborResourceService = laborResourceService;
//        this.laborResourceSelectionForm = laborResourceSelectionForm.getInstance();
//        this.laborResourceForm = laborResourceForm;
        this.laborResourceComboBox = laborResourceComboBox.getInstance();
        customizeBinder();
    }

    private void customizeBinder() {

        var nameColumn = addColumn(TaskResource::getLaborResource).
                setHeader("Name");

//        var resourceField = new LaborResourceComboBox(this.laborResourceService, this.laborResourceSelectionForm, this.laborResourceForm);
//        var resourceField = new ComboBoxWithButtons<LaborResource>();

        var resourceField = laborResourceComboBox;
        resourceField.setWidthFull();
        resourceField.setAutofocus(false);
        addCloseHandler(resourceField, editor);
        binder.forField(resourceField)
//                .asRequired("The field can not be empty")
                .bind(TaskResource::getLaborResource,
                        (taskResource, laborResource) -> {
                    taskResource.setResourceId(laborResource.getId());
                        });
        nameColumn.setEditorComponent(resourceField);

        // Duration column
        var durationColumn = addColumn(TaskResource::getDuration).
                setHeader("Duration");
        NumberField lagRepresentation = new NumberField();
        lagRepresentation.setWidthFull();
        lagRepresentation.setStepButtonsVisible(true);
        lagRepresentation.setStep(1);
        lagRepresentation.setMin(0.01);
        lagRepresentation.addValueChangeListener(event -> {
            var value = event.getValue();
            if (value < 0.01)
                lagRepresentation.setValue(0.01);
        });

        addCloseHandler(lagRepresentation, editor);
        binder.forField(lagRepresentation).withConverter(new BigDecimalToDoubleConverter(lagRepresentation))
                .bind(TaskResource::getDuration, TaskResource::setDuration);
        durationColumn.setEditorComponent(lagRepresentation);

    }

}
