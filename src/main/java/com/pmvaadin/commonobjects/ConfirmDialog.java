package com.pmvaadin.commonobjects;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.Objects;

public class ConfirmDialog extends Dialog {

    private final Details details = new Details("Details");
    private final TextArea detailsText = new TextArea();
    private final Span message = new Span();
    private final Button cancelButton = new Button("Cancel");
    private ComponentEventListener<ClickEvent<Button>> cancelEvent;
    private final Button rejectButton = new Button("Reject");
    private ComponentEventListener<ClickEvent<Button>> rejectEvent;
    private final Button confirmButton = new Button("Confirm");
    private ComponentEventListener<ClickEvent<Button>> confirmEvent;

    public ConfirmDialog() {
        super();
        VerticalLayout verticalLayout = new VerticalLayout(message, details);
        super.add(verticalLayout);
        super.setModal(false);
        super.setWidth("25%");
        super.setDraggable(true);
        super.setResizable(true);
        super.getFooter().add(cancelButton, rejectButton, confirmButton);
        details.addContent(detailsText);
        details.setWidthFull();
        detailsText.setWidthFull();
        configureButton();
    }

    public void addCancelText(String text) {
        cancelButton.setText(text);
    }

    public void addCancelListener(ComponentEventListener<ClickEvent<Button>> event) {
        cancelEvent = event;
    }

    public void setCancelable(boolean visible) {
        cancelButton.setVisible(visible);
    }

    public void addRejectText(String text) {
        rejectButton.setText(text);
    }

    public void addRejectListener(ComponentEventListener<ClickEvent<Button>> event) {
        rejectEvent = event;
    }

    public void setRejectable(boolean visible) {
        rejectButton.setVisible(visible);
    }

    public void addConfirmText(String text) {
        confirmButton.setText(text);
    }

    public void addConfirmListener(ComponentEventListener<ClickEvent<Button>> event) {
        confirmEvent = event;
    }

    public void setConfirmable(boolean visible) {
        confirmButton.setVisible(visible);
    }

    @Override
    public void add(String text) {
        message.add(text);
    }

    public void setDetailable(boolean visible) {
        details.setVisible(visible);
    }

    public void addDetailSummary(String summary) {
        details.setSummaryText(summary);
    }

    public void addDetailsText(String text) {
        detailsText.setValue(text);
    }

    private void configureButton() {

        cancelButton.addClickShortcut(Key.ESCAPE);
        cancelButton.getStyle().set("margin-right", "auto");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(event -> {this.close(); if (Objects.nonNull(cancelEvent)) cancelEvent.onComponentEvent(event);});
        rejectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        rejectButton.addClickListener(event -> {this.close(); if (Objects.nonNull(rejectEvent)) rejectEvent.onComponentEvent(event);});
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmButton.addClickListener(event -> {this.close(); if (Objects.nonNull(confirmEvent)) confirmEvent.onComponentEvent(event);});

    }

}
