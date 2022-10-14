package com.PMVaadin.PMVaadin.ProjectView;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.ValueProvider;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@Tag("object-grid")
public class ObjectGrid<T> extends Component implements HasComponents {

    private final VerticalLayout element = new VerticalLayout();
    private final HorizontalLayout toolBar = new HorizontalLayout();
    private Grid<T> grid;

    private final List<T> addedItems = new ArrayList<>();
    private final Map<T, Boolean> deletedItems = new HashMap<>();

    private T currentEditedItem;

    private Binder<T> binder;
    private Editor<T> editor;

    private Supplier<T> createNewItem;
    private UnaryOperator<T> copy;

    private final Button addButton = new Button("Add");
    private final Button copyButton = new Button("Copy");
    private final Button deleteButton = new Button("Delete");

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

    public HorizontalLayout getToolBar() {
        return toolBar;
    }

    protected Grid<T> getGrid() {
        return grid;
    }

    public void setCopyable(UnaryOperator<T> copy) {
        this.copy = copy;
        copyButton.setVisible(true);
    }

    public void setInstantiatable(Supplier<T> createInstance) {
        this.createNewItem = createInstance;
        addButton.setVisible(true);
    }

    public void setInlineEditor(InlineEditor<T> editor) {
        editor.customize(this.binder, this.editor);
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

    public Grid.Column<T> addColumn(Renderer<T> renderer, String sortingProperties) {
        return grid.addColumn(renderer, sortingProperties);
    }

    public void clear() {
        addedItems.clear();
        deletedItems.clear();
        grid.getDataProvider().refreshAll();
    }

//    public void setItems(Query<T, Void> query) {
//        grid.setItems((Stream<T>) query);
//    }

    private void initialSettings() {
        add(element);
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
                ((Focusable) editorComponent).focus();
            }
        });
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
            currentEditedItem = createNewItem.get();
            addedItems.add(currentEditedItem);
            grid.setItems(addedItems);
            editor.editItem(currentEditedItem);
            Component editorComponent = grid.getColumns().get(0).getEditorComponent();
            if (editorComponent instanceof Focusable) {
                ((Focusable) editorComponent).focus();
            }
        });
    }

    private void customizeCopyButton() {
        copyButton.setVisible(false);
        copyButton.addClickListener(buttonClickEvent -> {
            if (copy == null) return;
            T selectedItem = grid.getSelectionModel().getSelectedItems().stream().findFirst().orElse(null);
            if (selectedItem == null) return;
            currentEditedItem = copy.apply(selectedItem);
            addedItems.add(currentEditedItem);
            grid.setItems(currentEditedItem);
            editor.editItem(currentEditedItem);
            Component editorComponent = grid.getColumns().get(0).getEditorComponent();
            if (editorComponent instanceof Focusable) {
                ((Focusable) editorComponent).focus();
            }
        });
    }

    private void customizeDeleteButton() {
        grid.getSelectionModel().getSelectedItems().stream().forEach(item -> {
            deletedItems.put(item, true);
        });
    }

    interface InlineEditor<T> {
        void customize(Binder<T> binder, Editor<T> editor);
    }

}
