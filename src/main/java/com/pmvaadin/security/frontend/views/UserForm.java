package com.pmvaadin.security.frontend.views;

import com.pmvaadin.common.DialogForm;
import com.pmvaadin.projectstructure.NotificationDialogs;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.frontend.elements.ProjectComboBox;
import com.pmvaadin.security.entities.*;
import com.pmvaadin.security.frontend.elements.ProjectsTable;
import com.pmvaadin.security.frontend.elements.UserLaborResources;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SpringComponent
public class UserForm extends DialogForm {

    private User user;

    private final TextField name = new TextField();
    private final PasswordField password = new PasswordField();
    private final Checkbox isActive = new Checkbox();
    private final RolesTable rolesTable = new RolesTable();
    private final ComboBox<AccessType> accessType = new ComboBox<>();
    private final ProjectComboBox projectComboBox;
    private final ProjectsTable projectsTable;
    private final UserLaborResources userLaborResources;
    private final Binder<User> binder = new Binder<>(User.class);

    public UserForm(ProjectComboBox projectComboBox, ProjectsTable projectsTable, UserLaborResources userLaborResources) {
        this.projectComboBox = projectComboBox.getInstance();
        this.projectsTable = projectsTable.getInstance();
        this.userLaborResources = userLaborResources.getInstance();
        configureForm();
        configureMainButtons();
        configureBinder();
    }

    public void read(User user) {
        this.user = user;
        binder.readBean(user);
        fillRoles();
        projectsTable.setUser(user);
        userLaborResources.setUser(user);
        var name = this.user.getName();
        if (name == null) name = "";
        var title = "User: ";
        setHeaderTitle(title + name);
        customizePredefinedUser();
    }

    public UserForm getInstance() {
        return new UserForm(this.projectComboBox, projectsTable, userLaborResources);
    }

    private void customizePredefinedUser() {
        this.rolesTable.setReadOnly(user.isPredefined());
        this.isActive.setReadOnly(user.isPredefined());
    }

    private void configureBinder() {
        binder.forField(accessType)
                .withValidator(Objects::nonNull, "The field must not be empty")
                .bind(User::getAccessType, User::setAccessType);
        binder.forField(projectComboBox).bind(User::getRootProject, this::setRootProject);
        binder.forField(password).bind(user1 -> convertPasswordToString(user1.getPassword()), (user1, s) -> user1.setPassword(s.getBytes(StandardCharsets.UTF_8)));

        binder.forField(isActive)
                .bind(User::isActive, User::setActive);
        binder.bindInstanceFields(this);
    }

    private void setRootProject(User user, ProjectTask projectTask) {
        user.setRootProject(projectTask);
        if (projectTask != null)
            user.setRootProjectId(projectTask.getId());
        else
            user.setRootProjectId(null);
    }

    private String convertPasswordToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private void configureMainButtons() {
        getClose().addClickListener(event -> closeEvent());
        getCrossClose().addClickListener(event -> closeEvent());
        getSave().addClickListener(event -> saveEvent());
        getSaveAndClose().addClickListener(event -> {
            saveEvent();
            this.close();
        });
        getRefresh().setVisible(true);
        getRefresh().addClickListener(event -> fireEvent(new RefreshEvent(this, this.user)));
    }

    private void closeEvent() {
        fireEvent(new CloseEvent(this, this.user));
        this.close();
    }

    private void saveEvent() {

        try {
            binder.writeBean(this.user);
            projectsTable.validate();
            user.setProjects(projectsTable.getItems());
            userLaborResources.validate();
            user.setUserLaborResources(userLaborResources.getItems());
            fillUserRoles();
            fireEvent(new SaveEvent(this, this.user));
        } catch (Throwable e) {
            NotificationDialogs.notifyValidationErrors(e.getMessage());
        }

    }

    private void fillUserRoles() {
        var userRoles = user.getRoles();//.stream().map(UserRole::getRole).collect(Collectors.toSet());
        var roleSet = rolesTable.getListDataView().getItems()
                .filter(RolesRow::isOn).map(RolesRow::getRole).collect(Collectors.toSet());
        var deletedRoles = userRoles.stream().filter(role -> !roleSet.contains(role.getRole())).toList();
        userRoles.removeAll(deletedRoles);
        if (userRoles.size() == roleSet.size()) {
            user.setRoles(userRoles);
            return;
        }
        var remainedRoles = userRoles.stream().map(UserRole::getRole).collect(Collectors.toSet());
        roleSet.removeAll(remainedRoles);
         roleSet.stream().map(role -> {
            var newUserRole = user.getUserRoleInstance();
            newUserRole.setRole(role);
            return newUserRole;
        }).forEach(userRoles::add);
        user.setRoles(userRoles);
    }

    private void configureForm() {
        setAsItemForm();
        accessType.setItems(AccessType.values());
        var mainLayout = new FormLayout();
        mainLayout.addFormItem(name, "Name");
        mainLayout.addFormItem(password, "Password");
        mainLayout.addFormItem(isActive, "Is active");
        mainLayout.addFormItem(projectComboBox, "Root project");
        var wrapper = rolesTable;//new Div(rolesTable);
        wrapper.setWidthFull();
        wrapper.setHeight("12em");
        var horLayout = new HorizontalLayout(mainLayout, wrapper);
        var accessTypeLayout = new FormLayout();
        accessTypeLayout.addFormItem(accessType, "Access type");
        var vertLayoutProjects = new VerticalLayout(accessTypeLayout, this.projectsTable);
        vertLayoutProjects.setSpacing(false);
        vertLayoutProjects.setPadding(false);
        var tabProjects = new Tab("Projects");
        var tabResources = new Tab("Labor resources");
        var tabSheet = new TabSheet();
        tabSheet.add(tabProjects, vertLayoutProjects);
        tabSheet.add(tabResources, this.userLaborResources);
        tabSheet.setSizeFull();
        var rootFormElement = new VerticalLayout(horLayout, tabSheet);
        rootFormElement.setSpacing(false);
        rootFormElement.setPadding(false);
        add(rootFormElement);
    }

    private void fillRoles() {
        var userRoles = user.getRoles();
        rolesTable.getListDataView().getItems().forEach(rolesRow -> {
            for (var userRole: userRoles) {
                if (userRole.getRole() == rolesRow.getRole()) {
                    rolesRow.setOn(true);
                    return;
                }

            }
            rolesRow.setOn(false);
        });
        rolesTable.getListDataView().refreshAll();
    }

    @Data
    private static class RolesRow {
        private boolean on = false;
        private Role role;
        RolesRow(Role role) {
            this.role = role;
        }
    }

    private static class RolesTable extends Grid<RolesRow> {

        private final Binder<RolesRow> binder = new Binder<>();
        private boolean readOnly;

        public RolesTable() {
            customizeBinder();
            populateGrid();
            configureGrid();
            this.setWidthFull();
        }

        private void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
        }

        private void configureGrid() {
            var editor = this.getEditor();
            editor.setBinder(this.binder);
            this.addItemDoubleClickListener(e -> {
                if (this.readOnly || e.getItem() == null)
                    return;
                editor.editItem(e.getItem());
                Component editorComponent = e.getColumn().getEditorComponent();
                if (editorComponent instanceof Focusable) {
                    ((Focusable<?>) editorComponent).focus();
                }
            });
            this.addItemClickListener(event -> {
                if (this.readOnly)
                    return;
                var item = event.getItem();
                var editingItem = editor.getItem();
                if (editor.isOpen() && !Objects.equals(item, editingItem)) {
                    editor.save();
                    editor.closeEditor();
                    this.getListDataView().refreshAll();
                }
            });
        }

        private Grid.Column<RolesRow> addPredefinedColumn(Predicate<RolesRow> predicate) {

            return this.addComponentColumn((item) -> {

                if(!predicate.test(item)){
                    return new Div();
                }
                var icon = VaadinIcon.CHECK.create();
                icon.setColor("green");
                return icon;

            });

        }

        protected void addCloseHandler(Component component, Editor<RolesRow> editor) {

            component.getElement().addEventListener("keydown", e -> editor.cancel())
                    .setFilter("event.code === 'Escape'");
            component.getElement().addEventListener("keydown", e -> {
                editor.save();
                editor.closeEditor();
                this.getListDataView().refreshAll();
            }).setFilter("event.code === 'Enter'");

        }

        private void populateGrid() {
            this.setItems(Arrays.stream(Role.values()).map(RolesRow::new).toList());
        }

        private void customizeBinder() {

            var onColumn = this.addPredefinedColumn(RolesRow::isOn).setHeader("On");
            var checkbox = new Checkbox();
            checkbox.setWidthFull();
            checkbox.setAutofocus(false);
            this.binder.forField(checkbox)
                    .bind(RolesRow::isOn, RolesRow::setOn);
            onColumn.setEditorComponent(checkbox);
            addCloseHandler(onColumn, this.getEditor());

            this.addColumn(RolesRow::getRole).setHeader("Role");

        }

    }

}
