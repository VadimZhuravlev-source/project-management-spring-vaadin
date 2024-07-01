package com.pmvaadin.project.resources.labor.frontend.elements;

import com.pmvaadin.common.ObjectGrid;
import com.pmvaadin.project.structure.StandardError;
import com.pmvaadin.project.common.BigDecimalToDoubleConverter;
import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.pmvaadin.project.resources.labor.entity.TaskResource;
import com.pmvaadin.resources.labor.entity.LaborResourceRepresentation;
import com.pmvaadin.resources.labor.frontend.elements.LaborResourceComboBox;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Objects;

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

    public boolean validate() {
        var items = getItems();
        var mapResource = new HashMap<LaborResourceRepresentation, Boolean>();
        items.forEach(taskResource -> {
            if (taskResource.getLaborResource() == null || taskResource.getResourceId() == null) {
                grid.getEditor().editItem(taskResource);
                throw new StandardError("The labor resource can not be empty");
            }
            if (taskResource.getDuration() == null || taskResource.getDuration().compareTo(new BigDecimal(0)) <= 0) {
                grid.getEditor().editItem(taskResource);
                throw new StandardError("The duration must be greater than 0");
            }
            if (mapResource.containsKey(taskResource.getLaborResource())) {
                grid.getEditor().editItem(taskResource);
                throw new StandardError("The table can not contain a labor resource duplicates");
            }
            mapResource.put(taskResource.getLaborResource(), true);
        });
        return true;
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
                .withValidator(Objects::nonNull, "Can not be empty")
                .bind(TaskResource::getLaborResource,
                        (taskResource, laborResource) -> {
                    taskResource.setLaborResource(laborResource);
                    taskResource.setResourceId(laborResource.getId());
                        });
        nameColumn.setEditorComponent(resourceField);

        // Duration column
        var durationColumn = addColumn(TaskResource::getDuration).
                setHeader("Duration, hours");
        NumberField duration = new NumberField();
        duration.setWidthFull();
        duration.setStepButtonsVisible(true);
        duration.setStep(0.01);
        duration.setMin(0);
        duration.setValue(1.0);
        duration.addValueChangeListener(event -> {
            var value = event.getValue();
            if (value < 0.0)
                duration.setValue(0.0);
        });

        addCloseHandler(duration, editor);
        binder.forField(duration)
                .withConverter(new BigDecimalToDoubleConverter(duration))
                .withValidator(bigDecimal -> bigDecimal.compareTo(new BigDecimal(0)) >= 0, "Must be greater than 0")
                .bind(TaskResource::getDuration, TaskResource::setDuration);
        durationColumn.setEditorComponent(duration);

    }

}
