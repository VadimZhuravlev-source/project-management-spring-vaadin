package com.pmvaadin.projectview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ColumnSelectionForm extends Dialog {

    private final Set<String> currentColumns;
    private final MultiSelectListBox<BoxItem> listBox = new MultiSelectListBox<>();

    private Consumer<Set<String>> onCloseEvent;
    private final ProjectTaskPropertyNames propertyNames = new ProjectTaskPropertyNames();

    public ColumnSelectionForm(Set<String> currentColumns) {
        this.currentColumns = currentColumns;
        Button button = new Button("Select");
        button.addClickListener(l -> {
            Set<String> set = listBox.getSelectedItems().stream().map(BoxItem::getName).collect(Collectors.toSet());
            if (onCloseEvent != null) {
                onCloseEvent.accept(set);
            }
            close();
        });
        HorizontalLayout toolBar = new HorizontalLayout(button);
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

    public void setOnCloseEvent(Consumer<Set<String>> onCloseEvent) {
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
            if (!currentColumns.contains(boxItem.name)) return;
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
