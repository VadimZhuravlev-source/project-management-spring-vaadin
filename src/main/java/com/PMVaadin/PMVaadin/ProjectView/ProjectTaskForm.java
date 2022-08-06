package com.PMVaadin.PMVaadin.ProjectView;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

public class ProjectTaskForm extends FormLayout {

    private ProjectTask projectTask;

    DatePicker dateOfCreation = new DatePicker(ProjectTask.getHeaderDateOfCreation());
    DatePicker updateDate = new DatePicker(ProjectTask.getHeaderUpdateDate());
    TextField name = new TextField(ProjectTask.getHeaderName());
    TextField wbs = new TextField(ProjectTask.getHeaderWbs());
    TextField version = new TextField(ProjectTask.getHeaderVersion());
    DatePicker startDate = new DatePicker(ProjectTask.getHeaderStartDate());
    DatePicker finishDate = new DatePicker(ProjectTask.getHeaderFinishDate());
    Binder<ProjectTask> binder = new BeanValidationBinder<>(ProjectTask.class);

    Button save = new Button("Save");
    Button delete = new Button("Delete");
    Button close = new Button("Cancel");

    public ProjectTaskForm() {
        addClassName("project-task-form");
        wbs.setEnabled(false);
        dateOfCreation.setEnabled(false);
        updateDate.setEnabled(false);
        binder.bindInstanceFields(this);
        add(dateOfCreation,
                updateDate,
                version,
                name,
                wbs,
                startDate,
                finishDate,
                createButtonsLayout());
        name.setAutofocus(true);
    }

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        close.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, projectTask)));
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));


        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

        return new HorizontalLayout(save, delete, close);
    }

    public void setProjectTask(ProjectTask projectTask) {
        this.projectTask = projectTask;
        binder.readBean(projectTask);
        name.focus();
    }

    private void validateAndSave() {
        try {
            binder.writeBean(projectTask);
            fireEvent(new SaveEvent(this, projectTask));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    // Events
    public static abstract class ProjectTaskFormEvent extends ComponentEvent<ProjectTaskForm> {
        private ProjectTask projectTask;

        protected ProjectTaskFormEvent(ProjectTaskForm source, ProjectTask projectTask) {
            super(source, false);
            this.projectTask = projectTask;
        }

        public ProjectTask getProjectTask() {
            return projectTask;
        }
    }

    public static class SaveEvent extends ProjectTaskFormEvent {
        SaveEvent(ProjectTaskForm source, ProjectTask projectTask) {
            super(source, projectTask);
        }
    }

    public static class DeleteEvent extends ProjectTaskFormEvent {
        DeleteEvent(ProjectTaskForm source, ProjectTask projectTask) {
            super(source, projectTask);
        }
    }

    public static class CloseEvent extends ProjectTaskFormEvent {
        CloseEvent(ProjectTaskForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

}

