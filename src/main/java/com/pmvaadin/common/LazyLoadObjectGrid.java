package com.pmvaadin.common;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.Query;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@Tag("object-grid")
public class LazyLoadObjectGrid<T> extends ObjectGrid<T> {

    private final List<T> addedItems = new ArrayList<>();
    private final Map<T, Boolean> deletedItems = new HashMap<>();
    private final Map<T, Boolean> changedItems = new HashMap<>();
    private boolean isItemChanged;
    private CallbackDataProvider.FetchCallback<T, Void> fetchCallback;
    private CallbackDataProvider.CountCallback<T, Void> countCallback;
    private int previousPage;
    private int lastIndex = -1;

//    private Binder<T> binder;
//    private Editor<T> editor;

    public LazyLoadObjectGrid() {
        super();
        binder = new Binder<>();
        initialSettings();
    }

    public LazyLoadObjectGrid(int pageSize) {
        super(pageSize);
        binder = new Binder<>();
        initialSettings();
    }

    public LazyLoadObjectGrid(Class<T> beanType, boolean autoCreateColumns) {
        super(beanType, autoCreateColumns);
        binder = new Binder<>(beanType);
        initialSettings();
    }

    public LazyLoadObjectGrid(Class<T> beanType) {
        super(beanType);
        binder = new Binder<>(beanType);
        initialSettings();
    }

    public ChangedTableData<T> getChanges() {
        return new ChangedTableDataImpl<>(
            new ArrayList<>(addedItems),
            new ArrayList<>(changedItems.keySet()),
            new ArrayList<>(deletedItems.keySet())
        );
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

    public void setDeletable(boolean deletable) {
        deleteButton.setVisible(deletable);
    }

    public void setInlineEditor(InlineEditor<T> editor) {
        editor.customize(this.binder, this.editor);
    }

    public void clear() {
        addedItems.clear();
        deletedItems.clear();
        changedItems.clear();
    }

    public void setItems(CallbackDataProvider.FetchCallback<T, Void> fetchCallback, CallbackDataProvider.CountCallback<T, Void> countCallback) {

        this.fetchCallback = fetchCallback;
        this.countCallback = countCallback;
        grid.setItems(this::getGridFetchCallBack, this::getGridCountCallback);

    }

    public void setSizeFull() {
        toolbar.setSizeFull();
        grid.setSizeFull();
    }

    private Stream<T> getGridFetchCallBack(Query<T, Void> query) {

        int limit = query.getLimit();
        int offset = query.getOffset();
        int page = query.getPage();
        int pageSize = query.getPageSize();

        int countInstance = addedItems.size();

        int direction = page - previousPage;
        List<T> items = null;
        if (direction >= 0) {
            if (lastIndex == -1) {
                items = this.fetchCallback.fetch(query).toList();

            }
        }

        if (items == null) return Stream.of();//new ArrayList<T>().stream();

        int countItems = items.size();
        if (countItems >= pageSize) {
            return items.stream();
        }
        if (countItems == 0) {

        }
        return items.stream();

    }

    private int getGridCountCallback(Query<T, Void> query) {
        return countCallback.count(query) + addedItems.size();
    }

    private void initialSettings() {
        add(toolbar, grid);
        initializeObjectGrid();
        customizeGrid();

        binder.addValueChangeListener(event -> {
            T item = binder.getBean();
            if (item == null || addedItems.contains(item)) return;
            changedItems.put(item, true);
        });
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
        //toolBar.add(addButton, copyButton, deleteButton);
        add(toolbar, grid);
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

        addedItems.add(item);
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
        deleteButton.addClickListener(event -> {
            Set<T> selectedItems = grid.getSelectedItems();
            selectedItems.forEach(item -> {
                deletedItems.put(item, true);
                changedItems.remove(item);
            });

            T selectedItem = selectedItems.stream().reduce((t, t2) -> t2).orElse(null);
            T nextSelectedItem = null;
            if (selectedItem != null) {
                nextSelectedItem = grid.getListDataView().getNextItem(selectedItem).orElse(null);
            }

            grid.getListDataView().removeItems(selectedItems);
            grid.getSelectionModel().deselectAll();

            if (nextSelectedItem != null) grid.select(nextSelectedItem);

            Iterator<T> iterator = addedItems.iterator();
            while (iterator.hasNext()) {
                T item = iterator.next();
                if (deletedItems.getOrDefault(item, false)) {
                    deletedItems.remove(item);
                    iterator.remove();
                }
            }

        });

    }

}
