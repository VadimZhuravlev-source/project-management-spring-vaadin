package com.PMVaadin.PMVaadin.security;

import com.PMVaadin.PMVaadin.MainLayout;
import com.PMVaadin.PMVaadin.security.Services.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.security.PermitAll;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Users | PM")
@PermitAll
@Transactional
public class AdminUsersView extends VerticalLayout {

    private final UserService userService;
    private final Grid<User> grid = new Grid<>(User.class, false);
    private TextField userName = new TextField("First name");
    private TextField userLastName = new TextField("Last name");
    private TextField phoneNumber = new TextField("Phone number");
    private TextField password = new TextField("Password");
    private TextField address = new TextField("Address");
    private Checkbox active = new Checkbox("Active");
    private ComboBox<Role> roleComboBox = new ComboBox<>("Role");
    private Dialog dialog;

    public AdminUsersView(UserService userService, Component... components) {

        this.userService = userService;
        addClassName("calendar-list-view");
        setSizeFull();
        configureGrid();

        populateDate();

        add(getToolbar(), grid);
    }

    private void configureGrid() {
        grid.addClassNames("calendar-grid");
        grid.setSizeFull();
        grid.addColumn(User::getFirstName).setHeader("Name");
        grid.addColumn(User::getPhoneNumber).setHeader("Phone number");
        grid.addColumn(User::getRole).setHeader("Role");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private HorizontalLayout getToolbar() {

        Button addContactButton = new Button("Add user");
        addContactButton.addClickListener(e -> addUser(new User()));

        Button deleteContactButton = new Button("Delete user");
        deleteContactButton.addClickListener(e -> deleteUser());

        Button editContactButton = new Button("Edit user");
        editContactButton.addClickListener(e->editUser());

        HorizontalLayout toolbar = new HorizontalLayout(addContactButton, editContactButton,
                deleteContactButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }


    private void populateDate() {
        List<User> users = userService.getUsers();
        if (null != users) grid.setItems(users);
    }

    private void addUser(User user) {
        dialog = new Dialog();
        dialog.setHeaderTitle("Add user");
        addButtonsAndFields(user);
        dialog.open();
    }

    private void addButtonsAndFields(User user) {
        Button saveUser = new Button("Save user");
        saveUser.addClickListener(e -> saveUser(user));

        Button cancel = new Button("Cancel", e -> dialog.close());
        roleComboBox.setItems(Role.values());

        dialog.add(new VerticalLayout(userName, phoneNumber, password, address, roleComboBox, active),
                new HorizontalLayout(saveUser, cancel));
    }

    private void saveUser(User user) {

        try {
            user.setFirstName(userName.getValue());
            user.setLastName(userLastName.getValue());
            user.setPhoneNumber(phoneNumber.getValue());
            user.setPassword(password.getValue().getBytes(StandardCharsets.UTF_8));
            user.setAddress(address.getValue());
            user.setRole(roleComboBox.getValue());
            user.setActive(active.getValue());
            userService.saveUser(user);
        } catch (Exception e) {
            Notification.show(e.getMessage());
        } finally {
            dialog.close();
            populateDate();
        }
    }

    private void editUser() {
        dialog = new Dialog();
        dialog.setHeaderTitle("Edit user");

        Integer selectedID = ((User)grid.getSelectionModel().getSelectedItems()
                .stream().findFirst().orElse(new User())).getId();
        System.out.println(selectedID);
        User user = userService.getUserById(selectedID);
        addButtonsAndFields(user);
        userName.setValue(user.getFirstName());
        userLastName.setValue(user.getLastName());
        address.setValue(user.getAddress());
        phoneNumber.setValue(user.getPhoneNumber());
        roleComboBox.setValue(user.getRole());
        dialog.open();
        addClassName("editing");
    }

    private void deleteUser() {
        User user = (User) grid.getSelectionModel().getSelectedItems()
                .stream().findFirst().orElseThrow();
        try {
            userService.deleteUser(user);
        } catch (Exception e) {
            Notification.show(e.getMessage());
            return;
        }

        populateDate();
    }

}
