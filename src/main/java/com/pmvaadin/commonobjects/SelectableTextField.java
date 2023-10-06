package com.pmvaadin.commonobjects;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.shared.Registration;

import java.util.Objects;
import java.util.function.Function;

@Tag("select-text-field")
public class SelectableTextField<T> extends HorizontalLayout implements HasValue<HasValue.ValueChangeEvent<T>, T>, HasThemeVariant<TextFieldVariant> {

    private final TextFieldWithButtons textField;
    private T value;
    private Function<T, String> mapValueToText;

    private final Button selectionAction = new Button(new Icon("lumo", "menu"));
    private final Button openAction = new Button(new Icon("lumo", "search"));

    public SelectableTextField() {
        textField = new TextFieldWithButtons();
        add(textField);
    }

    @Override
    public void setValue(T value) {
        this.value = value;
        boolean isReadOnly = textField.isReadOnly();
        if (isReadOnly) textField.setReadOnly(false);
        refreshTextValue();
        if (isReadOnly) textField.setReadOnly(true);
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener listener) {
        return textField.addValueChangeListener(listener);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        textField.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return textField.isReadOnly();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        //textField.setRequiredIndicatorVisible(requiredIndicatorVisible);
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;//textField.isRequiredIndicatorVisible();
    }

    public void refreshTextValue() {

//        boolean turnOffReadOnly = isReadOnly();
//        if(turnOffReadOnly) textField.setReadOnly(false);

        if (Objects.isNull(value)) {
            textField.setValue("");
            //if (turnOffReadOnly) textField.setReadOnly(true);
            return;
        }

        if (mapValueToText != null) {
            String valueString = mapValueToText.apply(value);
            if (valueString == null) valueString = "";
            textField.setValue(valueString);
            //if (turnOffReadOnly) textField.setReadOnly(true);
            return;
        }

        textField.setValue(value.toString());

        //if (turnOffReadOnly) textField.setReadOnly(true);

    }

    public void setMapValueToText(Function<T, String> mapValueToText) {
        this.mapValueToText = mapValueToText;
    }

    public void setWidthFull() {
        textField.setWidthFull();
    }

    public void setSelectable(boolean selectable) {
        selectionAction.setVisible(selectable);
    }

    public void setOpenable(boolean openable) {
        openAction.setVisible(openable);
    }

    public Registration addSelectionListener(ComponentEventListener<ClickEvent<Button>> listener) {
        return selectionAction.addClickListener(listener);
    }

    public Registration addOpeningListener(ComponentEventListener<ClickEvent<Button>> listener) {
        return openAction.addClickListener(listener);
    }

    public void setAutofocus(boolean autofocus) {
        textField.setAutofocus(autofocus);
    }

    class TextFieldWithButtons extends TextField {

        TextFieldWithButtons() {
            super();
            addToSuffix(selectionAction, openAction);
            selectionAction.setVisible(false);
            openAction.setVisible(false);
        }

    }

}
