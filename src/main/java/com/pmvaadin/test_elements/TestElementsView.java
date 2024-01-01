package com.pmvaadin.test_elements;

import com.pmvaadin.MainLayout;
import com.pmvaadin.common.SelectedComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value="Tests", layout = MainLayout.class)
@PageTitle("Tests | Tests")
@PermitAll
public class TestElementsView extends FormLayout {


    public TestElementsView() {
        super();
        add(new SelectedComboBox<>(), new TextField());
    }
}
