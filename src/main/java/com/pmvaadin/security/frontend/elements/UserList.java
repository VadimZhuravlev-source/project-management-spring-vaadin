package com.pmvaadin.security.frontend.elements;

import com.pmvaadin.common.services.ListService;
import com.pmvaadin.common.vaadin.ItemList;
import com.pmvaadin.project.structure.NotificationDialogs;
import com.pmvaadin.security.entities.User;
import com.pmvaadin.security.entities.UserRepresentation;
import com.pmvaadin.security.entities.UserRepresentationDTO;
import com.pmvaadin.security.frontend.views.UserForm;
import com.pmvaadin.terms.timeunit.entity.TimeUnit;

public class UserList extends ItemList<UserRepresentation, User> {

    private final ListService<UserRepresentation, User> listService;
    private UserForm editingForm;

    public UserList(ListService<UserRepresentation, User> listService, UserForm editingForm) {
        super(listService);
        this.listService = listService;
        this.editingForm = editingForm;
        configureGrid();
    }

    private void configureGrid() {

        this.grid.addColumn(UserRepresentation::getName).setHeader("Name");
        this.addFlagColumn(UserRepresentation::isActive).setHeader("Is active");
        this.addFlagColumn(UserRepresentation::isPredefined).setHeader("Is predefined");
        onMouseDoubleClick(this::openNewItem);

        beforeAddition(this::openNewItem);
        onCoping(this::openNewItem);
        onContextMenuOpen(this::openNewItem);
    }

    private void openNewItem(User user) {
        openEditingForm(user);
    }

    private void openEditingForm(User user) {
        editingForm = editingForm.getInstance();
        editingForm.read(user);
        editingForm.addListener(UserForm.SaveEvent.class, event -> saveAndClose(event.getItem(), false));
        editingForm.addListener(UserForm.SaveAndCloseEvent.class, event -> saveAndClose(event.getItem(), true));
        editingForm.addListener(UserForm.CloseEvent.class, closeEvent -> closeEditor());
        editingForm.addListener(UserForm.RefreshEvent.class, this::refreshEvent);
        editingForm.open();
        setDeletionAvailable(false);
    }

    private void refreshEvent(UserForm.RefreshEvent event) {
        var item = event.getItem();
        if (item instanceof User user) {
            var useRepresentation = new UserRepresentationDTO(user.getId(), user.getName(), user.isActive(), user.isPredefined());
            var refreshedUser = listService.get(useRepresentation);
            editingForm.read(refreshedUser);
        }
    }

    private void saveAndClose(Object item, boolean close) {
        if (item instanceof User castItem) {
            try {
                var savedItem = listService.save(castItem);
                if (close) {
                    closeEditor();
                    return;
                }
                if (editingForm.isOpened())
                    editingForm.read(savedItem);
            } catch (Throwable error) {
                showDialog(error);
            }
        }
    }

    private void closeEditor() {
        editingForm.close();
        setDeletionAvailable(true);
        this.grid.getDataProvider().refreshAll();
    }

}
