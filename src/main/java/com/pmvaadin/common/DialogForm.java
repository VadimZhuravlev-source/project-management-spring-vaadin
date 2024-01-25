package com.pmvaadin.common;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;

import java.util.HashSet;
import java.util.Set;

public class DialogForm extends Dialog {

    private final Button crossClose = new Button(new Icon("lumo", "cross"));
    private final Button select = new Button("Choose");
    private final Button saveAndClose = new Button("Save and close");
    private final Button save = new Button("Save");
    private final Button close = new Button("Close");
    private final Button refresh = new Button("Refresh", new Icon("lumo", "reload"));

    public DialogForm() {
        customizeHeader();
        customizeFooter();
        addClassName("dialog-padding-1");
        refresh.setVisible(false);
    }

    public Button getCrossClose() {
        return crossClose;
    }

    public Button getSelect() {
        return select;
    }

    public Button getSaveAndClose() {
        return saveAndClose;
    }

    public Button getSave() {
        return save;
    }

    public Button getClose() {
        return close;
    }

    public Button getRefresh() {
        return refresh;
    }

    public void setAsSelectForm() {
        close.setVisible(false);
        save.setVisible(false);
        saveAndClose.setVisible(false);
        refresh.setVisible(false);
    }

    public void setAsItemForm() {
        select.setVisible(false);
    }

    private void customizeHeader() {

        crossClose.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        crossClose.addClickShortcut(Key.ESCAPE);
        var expander = new HorizontalLayout();
        expander.setWidthFull();

        getHeader().add(expander, crossClose);

    }

    private void customizeFooter() {

        saveAndClose.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        select.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        var expander = new HorizontalLayout();
        expander.setWidthFull();
        var container = new HorizontalLayout(select, saveAndClose, save, refresh, expander, close);
        getFooter().add(container);

    }

    public <I extends ComponentEvent<?>> Registration addListener(Class<I> eventType,
                                                                  ComponentEventListener<I> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    public static abstract class DialogFormEvent extends ComponentEvent<DialogForm> {

        private final Object item;
        protected DialogFormEvent(DialogForm source, Object item) {
            super(source, false);
            this.item = item;
        }

        public Object getItem() {
            return this.item;
        }

    }

    public static abstract class DialogFormSelectEvent extends ComponentEvent<DialogForm> {

        private final Set<Object> selectedItems = new HashSet<>();
        protected DialogFormSelectEvent(DialogForm source, Set<?> selectedItems) {
            super(source, false);
            this.selectedItems.addAll(selectedItems);
        }

        public Set<Object> getSelectedItems() {
            return this.selectedItems;
        }

    }

    public static class SelectEvent extends DialogFormSelectEvent {
        public SelectEvent(DialogForm source, Set<?> item) {
            super(source, item);
        }
    }

    public static class CloseEvent extends DialogFormEvent {
        public CloseEvent(DialogForm source, Object item) {
            super(source, item);
        }
    }

    public static class SaveEvent extends DialogFormEvent {
        public SaveEvent(DialogForm source, Object item) {
            super(source, item);
        }
    }

    public static class RefreshEvent extends DialogFormEvent {
        public RefreshEvent(DialogForm source, Object item) {
            super(source, item);
        }
    }

}
