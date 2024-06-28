package com.pmvaadin.security.frontend.elements;

import com.pmvaadin.common.ObjectGrid;
import com.pmvaadin.project.structure.StandardError;
import com.pmvaadin.resources.labor.frontend.elements.LaborResourceComboBox;
import com.pmvaadin.security.entities.User;
import com.pmvaadin.security.user.labor.resource.UserLaborResource;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.util.HashSet;
import java.util.Objects;

@SpringComponent
public class UserLaborResources extends ObjectGrid<UserLaborResource> {

    private final LaborResourceComboBox laborResourceComboBox;

    UserLaborResources(LaborResourceComboBox laborResourceComboBox) {
        this.laborResourceComboBox = laborResourceComboBox.getInstance();
        customizeBinder();
        customizeElement();
    }

    public UserLaborResources getInstance() {
        return new UserLaborResources(this.laborResourceComboBox);
    }

    public void setUser(User user) {

        if (user == null)
            return;

        setInstantiatable(user::getUserLaborResourceInstance);
        var resources = user.getUserLaborResources();
        if (resources != null)
            this.setItems(resources);
    }

    public boolean validate() {
        var items = getItems();
        var mapResource = new HashSet<>();
        items.forEach(laborResource -> {
            if (laborResource.getLaborResourceId() == null) {
                grid.getEditor().editItem(laborResource);
                throw new StandardError("The labor resource can not be empty");
            }
            if (mapResource.contains(laborResource.getLaborResourceId())) {
                grid.getEditor().editItem(laborResource);
                throw new StandardError("The table can not contain a labor resource duplicates");
            }
            mapResource.add(laborResource.getLaborResourceId());
        });
        return true;
    }

    private void customizeElement() {
        setDeletable(true);
    }

    private void customizeBinder() {

        var nameColumn = addColumn(this::getTitle).
                setHeader("Labor resource");

        var laborResourceField = laborResourceComboBox.getInstance();
        laborResourceField.setWidthFull();
        laborResourceField.setAutofocus(false);
        addCloseHandler(laborResourceField, editor);
        binder.forField(laborResourceField)
                .withValidator(Objects::nonNull, "Can not be empty")
                .bind(UserLaborResource::getLaborResource,
                        (userLaborResource, laborResourceRepresentation) -> {
                            userLaborResource.setLaborResource(laborResourceRepresentation);
                            userLaborResource.setLaborResourceId(laborResourceRepresentation.getId());
                        });
        nameColumn.setEditorComponent(laborResourceField);

    }

    private String getTitle(UserLaborResource userLaborResource) {
        if (userLaborResource.getLaborResource() == null)
            return "";
        return userLaborResource.getLaborResource().getName();
    }

}
