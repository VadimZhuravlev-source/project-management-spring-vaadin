package com.pmvaadin.commonobjects;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.ValueProvider;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ObjectGrid<T> extends VerticalLayout {

    protected final HorizontalLayout toolBar = new HorizontalLayout();
    protected final Grid<T> grid;

    protected Binder<T> binder;
    protected Editor<T> editor;

    protected Supplier<T> createNewItem;
    protected UnaryOperator<T> copy;

    protected Button addButton = new Button(new Icon(VaadinIcon.PLUS_CIRCLE));
    protected Button deleteButton = new Button(new Icon(VaadinIcon.CLOSE_CIRCLE));
    protected Button copyButton = new Button(new Icon(VaadinIcon.COPY));

//    protected final Button addButton = new Button("Add");
//    protected final Button copyButton = new Button("Copy");
//    protected final Button deleteButton = new Button("Delete");

    public ObjectGrid() {
        grid = new Grid<>();
        binder = new Binder<>();
        initialSettings();
    }

    public ObjectGrid(int pageSize) {
        grid = new Grid<>(pageSize);
        binder = new Binder<>();
        initialSettings();
    }

    public ObjectGrid(Class<T> beanType, boolean autoCreateColumns) {
        grid = new Grid<>(beanType, autoCreateColumns);
        binder = new Binder<>(beanType);
        initialSettings();
    }

    public ObjectGrid(Class<T> beanType) {
        grid = new Grid<>(beanType);
        binder = new Binder<>(beanType);
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

    public void setInlineEditor(LazyLoadObjectGrid.InlineEditor<T> editor) {
        editor.customize(this.binder, this.editor);
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
        customizeGrid();
    }

    private void customizeGrid() {

        editor = grid.getEditor();
        editor.setBinder(binder);
        grid.addItemDoubleClickListener(e -> {
            editor.editItem(e.getItem());
            Component editorComponent = e.getColumn().getEditorComponent();
            if (editorComponent instanceof Focusable) {
                ((Focusable<?>) editorComponent).focus();
            }
        });

    }

    private void initializeObjectGrid() {
        toolBar.add(addButton, copyButton, deleteButton);
        add(toolBar, grid);
        customizeButtons();
    }

    private void customizeButtons() {

        addButton.setTooltipText("Add");
        deleteButton.setTooltipText("Delete");
        copyButton.setTooltipText("Copy");

        customizeAddButton();
        customizeCopyButton();
        customizeDeleteButton();
    }

    private void customizeAddButton() {
        addButton.setVisible(false);
        addButton.addClickListener(this::addButtonClickListener);
    }

    private void addButtonClickListener(ClickEvent<Button> event) {
        if (createNewItem == null) return;
        T currentEditedItem = createNewItem.get();
        addNewItem(currentEditedItem);
    }

    private void customizeCopyButton() {
        copyButton.setVisible(false);
        //copyButton.setAutofocus(false);
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
        editor.editItem(item);
        Component editorComponent = grid.getColumns().get(0).getEditorComponent();
        grid.getSelectionModel().select(item);
        if (editorComponent instanceof Focusable) {
            ((Focusable<?>) editorComponent).focus();
        }

    }

    private void customizeDeleteButton() {

        deleteButton.setVisible(false);
        //deleteButton.setAutofocus(false);
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

    public interface InlineEditor<T> {
        void customize(Binder<T> binder, Editor<T> editor);
    }

}
