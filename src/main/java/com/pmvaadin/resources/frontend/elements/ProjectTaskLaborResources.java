package com.pmvaadin.resources.frontend.elements;

import com.pmvaadin.common.ObjectGrid;
import com.pmvaadin.projecttasks.common.BigDecimalToDoubleConverter;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.resources.entity.TaskResource;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
public class ProjectTaskLaborResources extends ObjectGrid<TaskResource> {

    private final LaborResourceComboBox laborResourceComboBox;

    public ProjectTaskLaborResources(LaborResourceComboBox laborResourceComboBox) {
        this.laborResourceComboBox = laborResourceComboBox.getInstance();
        customizeBinder();
        customizeElement();
    }

    public ProjectTaskLaborResources getInstance() {
        return new ProjectTaskLaborResources(this.laborResourceComboBox);
    }

    public void setProjectTask(ProjectTask projectTask) {
        if (projectTask != null)
            setInstantiatable(projectTask::getTaskResourceInstance);
    }

    private void customizeElement() {
        setCopyable(TaskResource::copy);
        setDeletable(true);
    }

    private void customizeBinder() {

        var nameColumn = addColumn(TaskResource::getLaborResource).
                setHeader("Resource");

        var resourceField = laborResourceComboBox;
        resourceField.setWidthFull();
        resourceField.setAutofocus(false);
        addCloseHandler(resourceField, editor);
        binder.forField(resourceField)
                .bind(TaskResource::getLaborResource,
                        (taskResource, laborResource) -> taskResource.setResourceId(laborResource.getId()));
        nameColumn.setEditorComponent(resourceField);

        // Duration column
        var durationColumn = addColumn(TaskResource::getDuration).
                setHeader("Duration");
        NumberField lagRepresentation = new NumberField();
        lagRepresentation.setWidthFull();
        lagRepresentation.setStepButtonsVisible(true);
        lagRepresentation.setStep(1);
        lagRepresentation.setMin(0);
        lagRepresentation.setValue(1.0);
        lagRepresentation.addValueChangeListener(event -> {
            var value = event.getValue();
            if (value < 0.0)
                lagRepresentation.setValue(0.0);
        });

        addCloseHandler(lagRepresentation, editor);
        binder.forField(lagRepresentation).withConverter(new BigDecimalToDoubleConverter(lagRepresentation))
                .bind(TaskResource::getDuration, TaskResource::setDuration);
        durationColumn.setEditorComponent(lagRepresentation);

    }

}
