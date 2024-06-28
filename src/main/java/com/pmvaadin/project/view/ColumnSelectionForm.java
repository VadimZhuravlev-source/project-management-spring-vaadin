package com.pmvaadin.project.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ColumnSelectionForm extends Dialog {

    private final List<String> chosenColumns;
    private final MultiSelectListBox<BoxItem> listBox = new MultiSelectListBox<>();

    private Consumer<List<String>> onCloseEvent;
    private final ProjectTaskPropertyNames propertyNames = new ProjectTaskPropertyNames();

    public ColumnSelectionForm(List<String> chosenColumns) {
        this.chosenColumns = chosenColumns;
        Button button = new Button("Select");
        button.addClickListener(l -> {
            var props = propertyNames.getAvailableColumnProps();
            var set = listBox.getSelectedItems().stream().map(BoxItem::getName).collect(Collectors.toSet());
            List<String> names = new ArrayList<>(set.size());
            props.forEach((k, v) -> {
                if (!set.contains(k)) return;
                names.add(k);
            });
            if (onCloseEvent != null) {
                onCloseEvent.accept(names);
            }
            close();
        });
        new Icon("lumo", "menu");

        Button selectAll = new Button(VaadinIcon.CHECK_SQUARE.create());
        selectAll.setTooltipText("Select all");
        selectAll.addClickListener(l -> listBox.select(listBox.getListDataView().getItems().toList()));
        Button deSelectAll = new Button(VaadinIcon.CLOSE_SMALL.create());
        deSelectAll.setTooltipText("Unselect all");
        deSelectAll.addClickListener(l -> listBox.deselectAll());
        HorizontalLayout toolBar = new HorizontalLayout(button, selectAll, deSelectAll);
        VerticalLayout verticalLayout = new VerticalLayout(toolBar, listBox);
        add(verticalLayout);
        fillListBox();
        Button closeButton = new Button(new Icon("lumo", "cross"),
                e -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getHeader().add(closeButton);
        setHeaderTitle("Column selection");
        setDraggable(true);
        setResizable(true);

    }

    public void setOnCloseEvent(Consumer<List<String>> onCloseEvent) {
        this.onCloseEvent = onCloseEvent;
    }

    private void fillListBox() {

        Map<String, ProjectTaskPropertyNames.ColumnProperties> availableColumns = propertyNames.getAvailableColumnProps();

        List<BoxItem> boxItems = new ArrayList<>();
        availableColumns.forEach((k, v) -> {
            BoxItem newField = new BoxItem(k, v.representation());
            boxItems.add(newField);
        });

        listBox.setItems(boxItems);

        BoxItem scheduleModeField = null;
        for (BoxItem boxItem: listBox.getListDataView().getItems().toList()) {
            if (!chosenColumns.contains(boxItem.name)) continue;
            listBox.select(boxItem);
            if (boxItem.name.equals(propertyNames.getPropertyScheduleMode())) scheduleModeField = boxItem;
        }

        if (scheduleModeField != null) listBox.addComponents(scheduleModeField, new Hr());

    }

    @Getter
    @AllArgsConstructor
    private static class BoxItem {
        private String name;
        private String representation;
        @Override
        public String toString() {
            return representation;
        }
    }

}
