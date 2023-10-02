package com.pmvaadin.commonobjects;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.ValueProvider;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ObjectGrid<T> extends VerticalLayout {

    protected final HorizontalLayout toolBar = new HorizontalLayout();
    protected final Grid<T> grid;

    protected Supplier<T> createNewItem;
    protected UnaryOperator<T> copy;

    protected final Button addButton = new Button("Add");
    protected final Button copyButton = new Button("Copy");
    protected final Button deleteButton = new Button("Delete");

    public ObjectGrid() {
        grid = new Grid<>();
        initialSettings();
    }

    public ObjectGrid(int pageSize) {
        grid = new Grid<>(pageSize);
        initialSettings();
    }

    public ObjectGrid(Class<T> beanType, boolean autoCreateColumns) {
        grid = new Grid<>(beanType, autoCreateColumns);
        initialSettings();
    }

    public ObjectGrid(Class<T> beanType) {
        grid = new Grid<>(beanType);
        initialSettings();
    }

    public Grid.Column<T> addColumn(String propertyName) {
        return grid.addColumn(propertyName);
    }

    public Grid.Column<T> addColumn(ValueProvider<T, ?> valueProvider) {
        return grid.addColumn(valueProvider);
    }

    public <V extends java.lang.Comparable<? super V>> Grid.Column<T> addColumn(ValueProvider<T, V> valueProvider, String sortingProperties) {
        return grid.addColumn(valueProvider, sortingProperties);
    }

    public Grid.Column<T> addColumn(Renderer<T> renderer) {
        return grid.addColumn(renderer);
    }

    public void setItems(List<T> items) {
        grid.setItems(items);
    }

    public void setCopyable(UnaryOperator<T> copy) {
        this.copy = copy;
        copyButton.setVisible(true);
    }

    public void setInstantiatable(Supplier<T> createInstance) {
        this.createNewItem = createInstance;
        addButton.setVisible(true);
    }

    public void setDeletable(boolean deletable) {
        deleteButton.setVisible(deletable);
    }


    private void initialSettings() {
        add(toolBar, grid);
        initializeObjectGrid();
    }

    private void initializeObjectGrid() {
        toolBar.add(addButton, copyButton, deleteButton);
        add(toolBar, grid);
        customizeButtons();
    }

    private void customizeButtons() {
        customizeAddButton();
        customizeCopyButton();
        customizeDeleteButton();
    }

    private void customizeAddButton() {
        addButton.setVisible(false);
        addButton.addClickListener(buttonClickEvent -> {
            if (createNewItem == null) return;
            T currentEditedItem = createNewItem.get();
            addNewItem(currentEditedItem);
        });
    }

    private void customizeCopyButton() {
        copyButton.setVisible(false);
        copyButton.addClickListener(buttonClickEvent -> {
            if (copy == null) return;
            T selectedItem = grid.getSelectionModel().getSelectedItems().stream().findFirst().orElse(null);
            if (selectedItem == null) return;
            T currentEditedItem = copy.apply(selectedItem);
            addNewItem(currentEditedItem);
        });
    }

    private void addNewItem(T item) {

        grid.getListDataView().addItem(item);

    }

    private void customizeDeleteButton() {

        deleteButton.setVisible(false);
        deleteButton.addClickListener(event -> {
            Set<T> selectedItems = grid.getSelectedItems();

            T selectedItem = selectedItems.stream().reduce((t, t2) -> t2).orElse(null);
            T nextSelectedItem = null;
            if (selectedItem != null) {
                nextSelectedItem = grid.getListDataView().getNextItem(selectedItem).orElse(null);
            }

            grid.getListDataView().removeItems(selectedItems);
            grid.getSelectionModel().deselectAll();

            if (nextSelectedItem != null) grid.select(nextSelectedItem);

        });

    }

}
