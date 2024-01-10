package com.pmvaadin.common;

import com.pmvaadin.common.services.ListService;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;
import org.springframework.data.domain.PageRequest;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

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

    public void setDefaultDataProvider(ListService<T, ?> itemService) {
        this.comboBox.setDataProvider(this.getDefaultDataProvider(itemService), s -> s);
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
        var background = comboBox.getElement().getProperty("--vaadin-input-field-background");
        selectionAction.getElement().setProperty("--vaadin-button-background", background);
        openAction.getElement().setProperty("--vaadin-button-background", background);
    }

    private DataProvider<T, String> getDefaultDataProvider(ListService<T, ?> itemService) {

        return new DataProvider<>() {

            @Override
            public boolean isInMemory() {
                return false;
            }

            @Override
            public int size(Query<T, String> query) {
                var pageable = PageRequest.of(query.getPage(), query.getPageSize());
                var filter = query.getFilter().orElse("");
                return itemService.sizeInBackEnd(filter, pageable);
            }

            @Override
            public Stream<T> fetch(Query<T, String> query) {
                var pageable = PageRequest.of(query.getPage(), query.getPageSize());
                var filter = query.getFilter().orElse("");
                return itemService.getItems(filter, pageable).stream();
            }

            @Override
            public void refreshItem(T item) {

            }

            @Override
            public void refreshAll() {

            }

            @Override
            public Registration addDataProviderListener(DataProviderListener<T> listener) {
                return null;
            }
        };

    }

}
