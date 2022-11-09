package com.pmvaadin.projectstructure;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;

public class NotificationDialogs {

    public static void notifyValidationErrors(String errorMessage) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Error");
        dialog.setWidth("25%");
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.add(errorMessage);
        Button closeButton = new Button("Close");
        closeButton.addClickListener(e -> dialog.close());
        dialog.getFooter().add(closeButton);
        dialog.open();
    }

}
