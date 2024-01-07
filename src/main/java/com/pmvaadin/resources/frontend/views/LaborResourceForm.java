package com.pmvaadin.resources.frontend.views;

import com.pmvaadin.resources.entity.LaborResource;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.Nonnull;

@SpringComponent
public class LaborResourceForm extends Dialog {

    private LaborResource laborResource;
    private final TextField name = new TextField("Name");
    private final Binder<LaborResource> binder = new Binder<>(LaborResource.class);

    public LaborResourceForm() {
        binder.bindInstanceFields(this);
        add(name);
        customizeHeader();
        createButtons();
        addClassName("dialog-padding-1");
    }

    public void read(@Nonnull LaborResource laborResource) {
        this.laborResource = laborResource;
        binder.readBean(this.laborResource);
        var laborResourceName = this.laborResource.getName();
        if (laborResourceName == null) laborResourceName = "";
        var title = "Labor resource: ";
        setHeaderTitle(title + laborResourceName);
    }

    private void customizeHeader() {

        Button closeButton = new Button(new Icon("lumo", "cross"),
                e -> {
                    fireEvent(new CloseEvent(this, this.laborResource));
                    this.close();
                });
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickShortcut(Key.ESCAPE);

        getHeader().add(closeButton);

    }

    private void createButtons() {

        Button ok = new Button("Ok");
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        ok.addClickListener(event -> {

            try {
                binder.writeBean(this.laborResource);
            } catch (ValidationException error) {
                var confDialog = new ConfirmDialog();
                confDialog.setText(error.getMessage());
                confDialog.open();
                return;
            }
            fireEvent(new SaveEvent(this, this.laborResource));
            close();

        });

        Button close = new Button("Cancel");
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        close.addClickListener(event ->
        {
            fireEvent(new CloseEvent(this, this.laborResource));
            close();
        });

        getFooter().add(ok, close);

    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    public static abstract class LaborResourceFormEvent extends ComponentEvent<LaborResourceForm> {

        private final LaborResource laborResource;
        protected LaborResourceFormEvent(LaborResourceForm source, LaborResource laborResource) {
            super(source, false);
            this.laborResource = laborResource;
        }

        public LaborResource getLaborResource() {
            return this.laborResource;
        }

    }

    public static class CloseEvent extends LaborResourceFormEvent {
        CloseEvent(LaborResourceForm source, LaborResource laborResource) {
            super(source, laborResource);
        }
    }

    public static class SaveEvent extends LaborResourceFormEvent {
        SaveEvent(LaborResourceForm source, LaborResource laborResource) {
            super(source, laborResource);
        }
    }


}
