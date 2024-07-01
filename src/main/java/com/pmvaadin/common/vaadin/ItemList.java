package com.pmvaadin.common.vaadin;

import com.pmvaadin.common.services.ListService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.Set;
import java.util.function.Consumer;

public class ItemList<T, I> extends SearchableGrid<T> {

    private boolean isDeletionAvailable = true;

    protected Button add = new Button(new Icon(VaadinIcon.PLUS_CIRCLE));
    protected Button delete = new Button(new Icon(VaadinIcon.CLOSE_CIRCLE));
    protected Button copy = new Button(new Icon(VaadinIcon.COPY));

    protected Consumer<I> beforeAddition;
    protected Consumer<I> onMouseDoubleClick;
    protected Consumer<I> onCoping;
    private GridMenuItem<T> openItem;

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

    public void onContextMenuOpen(Consumer<I> openEvent) {
        var menu = this.grid.addContextMenu();
        if (openItem != null)
            menu.remove(openItem);

        this.openItem = menu.addItem("Open", event -> {
            if (openEvent == null)
                return;
            var itemOpt = event.getItem();
            if (itemOpt.isEmpty()) return;
            var item = itemOpt.get();
            try {
                var openingItem = ((ListService<T, I>) this.itemService).get(item);
                openEvent.accept(openingItem);
            } catch (Throwable error) {
                showDialog(error);
            }
        });
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

    public void setDeletionAvailable(boolean deletionAvailable) {
        this.isDeletionAvailable = deletionAvailable;
    }

    public Grid<T> getGrid() {
        return grid;
    }

    public void setEdible(boolean edible) {
        add.setVisible(edible);
        delete.setVisible(edible);
        copy.setVisible(edible);
        if (openItem != null)
            openItem.setVisible(edible);
    }

    private void onMouseDoubleClick(ItemDoubleClickEvent<T> event) {

        if (event == null) return;

        var item = event.getItem();

        try {
            var doubleClickItem = ((ListService<T, I>) itemService).get(item);
            if (onMouseDoubleClick != null)
                onMouseDoubleClick.accept(doubleClickItem);
        } catch (Throwable error) {
            showDialog(error);
        }

    }

    private void additionListener(ClickEvent<Button> event) {

        try {
            var newItem = ((ListService<T, I>) itemService).add();
            if (beforeAddition != null) beforeAddition.accept(newItem);
        } catch (Throwable error) {
            showDialog(error);
        }

    }

    private void deletionListener(ClickEvent<Button> event) {

        if (!isDeletionAvailable) return;

        var selectedItems = grid.getSelectedItems();
        if (selectedItems.isEmpty())
            return;
        var dialog = new ConfirmDialog("Deletion", "Are you sure you want to delete the selected objects?",
                "Ok", confirmEvent -> delete(selectedItems),
                "Cancel", cancelEvent -> {var a = 0;});
        dialog.open();
    }

    private void delete(Set<T> items) {
        try {
            ((ListService<T, I>) itemService).delete(items);
            grid.deselectAll();
            grid.getDataProvider().refreshAll();
        } catch (Throwable e) {
            showDialog(e);
        }
    }

    private void copingListener(ClickEvent<Button> event) {

        if (onCoping == null) return;
        var selectedItems = grid.getSelectedItems();
        if (selectedItems.isEmpty()) return;
        var item = selectedItems.stream().findFirst().get();
        try {
            var copyItem = ((ListService<T, I>) itemService).copy(item);
            onCoping.accept(copyItem);
        } catch (Throwable e) {
            showDialog(e);
        }
    }

}
