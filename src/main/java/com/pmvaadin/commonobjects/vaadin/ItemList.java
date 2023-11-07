package com.pmvaadin.commonobjects.vaadin;

import com.pmvaadin.commonobjects.services.ListService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.function.Consumer;

public class ItemList<T, I> extends SearchableGrid<T> {

    private boolean isDeletionAvailable = true;

    protected Button add = new Button(new Icon(VaadinIcon.PLUS_CIRCLE));
    protected Button delete = new Button(new Icon(VaadinIcon.CLOSE_CIRCLE));
    protected Button copy = new Button(new Icon(VaadinIcon.COPY));

    protected Consumer<I> beforeAddition;

    protected Consumer<I> onMouseDoubleClick;

    protected Consumer<I> onCoping;

    public ItemList(ListService<T, I> listService) {

        super(listService);

        add.setTooltipText("Add");
        add.addClickListener(this::additionListener);
        copy.setTooltipText("Copy");
        copy.addClickListener(this::copingListener);
        delete.setTooltipText("Delete");
        delete.addClickListener(this::deletionListener);
        delete.addClickShortcut(Key.DELETE);

        toolBar.add(add, copy, delete);

        searchField.addFocusListener(focusEvent ->
            isDeletionAvailable = false
        );
        grid.addFocusListener(focusEvent ->
            isDeletionAvailable = true
        );
        this.grid.addItemDoubleClickListener(this::onMouseDoubleClick);

    }

    public void beforeAddition(Consumer<I> beforeAddition) {
        this.beforeAddition = beforeAddition;
    }

    public void onCoping(Consumer<I> onCoping) {
        this.onCoping = onCoping;
    }

    public void onMouseDoubleClick(Consumer<I> onMouseDoubleClick) {
        this.onMouseDoubleClick = onMouseDoubleClick;
    }

    private void onMouseDoubleClick(ItemDoubleClickEvent<T> event) {

        if (event == null) return;

        var item = event.getItem();

        var doubleClickItem = ((ListService<T, I>) itemService).get(item);
        if (onMouseDoubleClick != null) onMouseDoubleClick.accept(doubleClickItem);

    }

    public void setDeletionAvailable(boolean deletionAvailable) {
        this.isDeletionAvailable = deletionAvailable;
    }

    public Grid<T> getGrid() {
        return grid;
    }

    private void additionListener(ClickEvent<Button> event) {

        var newItem = ((ListService<T, I>) itemService).add();
        if (beforeAddition != null) beforeAddition.accept(newItem);

    }

    private void deletionListener(ClickEvent<Button> event) {

        if (!isDeletionAvailable) return;

        var selectedItems = grid.getSelectedItems();
        if (selectedItems.isEmpty()) return;
        try {
            ((ListService<T, I>) itemService).delete(selectedItems);
            grid.deselectAll();
            grid.getDataProvider().refreshAll();
        } catch (Throwable e) {
            var dialog = new ConfirmDialog();
            dialog.add(e.getMessage());
            dialog.open();
            //NotificationDialogs.notifyValidationErrors(e.getMessage());
        }

    }

    private void copingListener(ClickEvent<Button> event) {

        if (onCoping == null) return;
        var selectedItems = grid.getSelectedItems();
        if (selectedItems.isEmpty()) return;
        var item = selectedItems.stream().findFirst().get();
        var copyItem = ((ListService<T, I>) itemService).copy(item);
        onCoping.accept(copyItem);

    }

}
