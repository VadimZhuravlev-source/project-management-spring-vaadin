package com.pmvaadin.common;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;

import java.util.Collection;
import java.util.Optional;

@Tag("tool-select")
public class ComboBoxWithButtons<T> extends HorizontalLayout implements HasValue<HasValue.ValueChangeEvent<T>, T> {

    private final ComboBox<T> comboBox;
    private final Button selectionAction = new Button(new Icon("lumo", "menu"));
    private final Button openAction = new Button(new Icon("lumo", "search"));

    public ComboBoxWithButtons(int pageSize) {
        this.comboBox = new ComboBox<>(pageSize);
        init();
    }

    public ComboBoxWithButtons() {
        this.comboBox = new ComboBox<>();
        init();
    }

    public ComboBoxWithButtons(String label) {
        this.comboBox = new ComboBox<>(label);
        init();
    }

    public ComboBoxWithButtons(String label, Collection<T> items) {
        this.comboBox = new ComboBox<>(label, items);
        init();
    }

    public ComboBoxWithButtons(String label, T... items) {
        this.comboBox = new ComboBox<>(label, items);
        init();
    }

    public ComboBoxWithButtons(HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<T>, T>> listener) {
        this.comboBox = new ComboBox<>(listener);
        init();
    }

    public ComboBoxWithButtons(String label, HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<T>, T>> listener) {
        this.comboBox = new ComboBox<>(label, listener);
        init();
    }

    public ComboBoxWithButtons(String label, HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<T>, T>> listener, T... items) {
        this.comboBox = new ComboBox<>(label, listener, items);
        init();
    }

    public ComboBox<T> getComboBox() {
        return comboBox;
    }

    public Button getSelectionAction() {
        return selectionAction;
    }

    public Button getOpenAction() {
        return openAction;
    }

    public boolean isAutofocus() {
        return comboBox.isAutofocus();
    }

    public void setAutofocus(boolean autofocus) {
        comboBox.setAutofocus(autofocus);
    }

    @Override
    public void setValue(T value) {
        this.comboBox.setValue(value);
    }

    @Override
    public T getValue() {
        return this.comboBox.getValue();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super ValueChangeEvent<T>> listener) {
        return this.comboBox.addValueChangeListener(listener);
    }

    @Override
    public T getEmptyValue() {
        return this.comboBox.getEmptyValue();
    }

    @Override
    public Optional<T> getOptionalValue() {
        return this.comboBox.getOptionalValue();
    }

    @Override
    public boolean isEmpty() {
        return this.comboBox.isEmpty();
    }

    @Override
    public void clear() {
        this.comboBox.clear();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return this.isReadOnly();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        this.setRequiredIndicatorVisible(requiredIndicatorVisible);
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return this.isRequiredIndicatorVisible();
    }

    private void init() {
        add(comboBox, selectionAction, openAction);
        selectionAction.setVisible(false);
        openAction.setVisible(false);
    }

}
