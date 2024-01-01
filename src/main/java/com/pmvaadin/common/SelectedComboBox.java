package com.pmvaadin.common;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.dataview.ComboBoxDataView;
import com.vaadin.flow.component.combobox.dataview.ComboBoxLazyDataView;
import com.vaadin.flow.component.combobox.dataview.ComboBoxListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

import java.util.Collection;
import java.util.Optional;

public class SelectedComboBox<T> extends HorizontalLayout implements HasValue<HasValue.ValueChangeEvent<T>, T> {

    private final ComboBox<T> comboBox;
    private final Button selectionAction = new Button(new Icon("lumo", "menu"));
    private final Button openAction = new Button(new Icon("lumo", "search"));

    public SelectedComboBox(int pageSize) {
        comboBox = new ComboBox<>(pageSize);
        addButtons();
    }

    public SelectedComboBox() {
        comboBox = new ComboBox<>();
        addButtons();
    }

    public SelectedComboBox(String label) {
        comboBox = new ComboBox<>(label);
        addButtons();
    }

    public SelectedComboBox(String label, Collection<T> items) {
        comboBox = new ComboBox<>(label, items);
        addButtons();
    }

    public SelectedComboBox(String label, T... items) {
        comboBox = new ComboBox<>(label, items);
        addButtons();
    }

    public SelectedComboBox(HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<T>, T>> listener) {
        comboBox = new ComboBox<>(listener);
        addButtons();
    }

    public SelectedComboBox(String label, HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<T>, T>> listener) {
        comboBox = new ComboBox<>(label, listener);
        addButtons();
    }

    public SelectedComboBox(String label, HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<T>, T>> listener, T... items) {
        comboBox = new ComboBox<>(label, listener, items);
        addButtons();
    }

    public ComboBoxListDataView<T> setItems(Collection<T> items) {
        return comboBox.setItems(items);
    }

    public ComboBoxListDataView<T> setItems(ComboBox.ItemFilter<T> itemFilter, Collection<T> items) {
        return comboBox.setItems(itemFilter, items);
    }

    public ComboBoxListDataView<T> setItems(ComboBox.ItemFilter<T> itemFilter, T... items) {
        return comboBox.setItems(itemFilter, items);
    }

    public ComboBoxDataView<T> setItems(DataProvider<T, String> dataProvider) {
        return comboBox.setItems(dataProvider);
    }

    public ComboBoxDataView<T> setItems(InMemoryDataProvider<T> inMemoryDataProvider, SerializableFunction<String, SerializablePredicate<T>> filterConverter) {
        return comboBox.setItems(inMemoryDataProvider, filterConverter);
    }

    public ComboBoxLazyDataView<T> setItems(BackEndDataProvider<T, String> dataProvider) {
        return comboBox.setItems(dataProvider);
    }

    public ComboBoxListDataView<T> setItems(ListDataProvider<T> dataProvider) {
        return comboBox.setItems(dataProvider);
    }

    public <C> ComboBoxLazyDataView<T> setItemsWithFilterConverter(CallbackDataProvider.FetchCallback<T, C> fetchCallback, SerializableFunction<String, C> filterConverter) {
        return comboBox.setItemsWithFilterConverter(fetchCallback, filterConverter);
    }

    public <C> ComboBoxLazyDataView<T> setItemsWithFilterConverter(CallbackDataProvider.FetchCallback<T, C> fetchCallback, CallbackDataProvider.CountCallback<T, C> countCallback, SerializableFunction<String, C> filterConverter) {
        return comboBox.setItemsWithFilterConverter(fetchCallback, countCallback, filterConverter);
    }

    public ComboBoxListDataView<T> setItems(ComboBox.ItemFilter<T> itemFilter, ListDataProvider<T> listDataProvider) {
        return comboBox.setItems(itemFilter, listDataProvider);
    }

    public ComboBoxLazyDataView<T> setItems(CallbackDataProvider.FetchCallback<T, String> fetchCallback) {
        return comboBox.setItems(fetchCallback);
    }

    public ComboBoxLazyDataView<T> setItems(CallbackDataProvider.FetchCallback<T, String> fetchCallback, CallbackDataProvider.CountCallback<T, String> countCallback) {
        return comboBox.setItems(fetchCallback, countCallback);
    }

    @Override
    public T getEmptyValue() {
        return comboBox.getEmptyValue();
    }

    @Override
    public Optional<T> getOptionalValue() {
        return comboBox.getOptionalValue();
    }

    @Override
    public boolean isEmpty() {
        return comboBox.isEmpty();
    }

    @Override
    public void clear() {
        comboBox.clear();
    }

    @Override
    public void setValue(T value) {
        comboBox.setValue(value);
    }

    @Override
    public T getValue() {
        return comboBox.getValue();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener listener) {
        return comboBox.addValueChangeListener(listener);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        selectionAction.setVisible(!readOnly);
        openAction.setVisible(!readOnly);
        comboBox.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return comboBox.isReadOnly();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        comboBox.setRequiredIndicatorVisible(requiredIndicatorVisible);
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return comboBox.isRequiredIndicatorVisible();
    }

    @Override
    public void setWidthFull() {
        comboBox.setWidthFull();
    }

    public Registration addSelectionListener(ComponentEventListener<ClickEvent<Button>> listener) {
        return selectionAction.addClickListener(listener);
    }

    public Registration addOpeningListener(ComponentEventListener<ClickEvent<Button>> listener) {
        return openAction.addClickListener(listener);
    }

    private void addButtons() {

        getStyle().set("gap", "0");
        add(comboBox, selectionAction, openAction);

    }

}
