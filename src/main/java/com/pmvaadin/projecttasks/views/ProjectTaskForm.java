package com.pmvaadin.projecttasks.views;

import com.pmvaadin.commonobjects.ObjectGrid;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.links.views.LinksProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.services.ProjectTaskService;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePickerVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
public class ProjectTaskForm extends Dialog {

    private ProjectTask projectTask;
    private final ProjectTaskService projectTaskService;
    private final LinksProjectTask linksGrid;

    private final DatePicker dateOfCreation = new DatePicker();
    private final DatePicker updateDate = new DatePicker();
    private final TextField name = new TextField();
    private final TextField wbs = new TextField();
    private final TextField version = new TextField();
    private final DatePicker startDate = new DatePicker();
    private final DatePicker finishDate = new DatePicker();
    private final Binder<ProjectTask> binder = new BeanValidationBinder<>(ProjectTask.class);

    private final VerticalLayout content = new VerticalLayout();
    private final Tab mainDataTab = new Tab("Main data");
    private final VerticalLayout mainDataLayout = new VerticalLayout();
    private final Tab linksTab = new Tab("Links");
    private final VerticalLayout linksLayout = new VerticalLayout();

    private final Button save = new Button("Save");
    private final Button delete = new Button("Delete");
    private final Button close = new Button("Cancel");

    public ProjectTaskForm(ProjectTaskService projectTaskService, LinksProjectTask linksGrid) {

        super();

        this.projectTaskService = projectTaskService;
        this.linksGrid = linksGrid;

        customizeForm();
        customizeHeader();
        customizeTabs();
        customizeFields();

        binder.bindInstanceFields(this);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(false);

        mainLayout.add(getMetadataFields(), getTabs(), content);

        add(mainLayout, createButtonsLayout());

    }

    public void setProjectTask(ProjectTask projectTask) {
        this.projectTask = projectTask;
        setHeaderTitle("Project task: " + projectTask.getName());
        linksGrid.setProjectTask(projectTask);
        binder.readBean(projectTask);
        name.focus();
    }

    private Tabs getTabs() {

        Tabs tabs = new Tabs(mainDataTab, linksTab);
        tabs.addSelectedChangeListener(this::tabsSelectedListener);
        tabs.setSelectedTab(mainDataTab);

        return tabs;

    }

    private void customizeTabs() {

        customizeMainDataLayout();
        linksLayout.add(linksGrid);

    }

    private void customizeMainDataLayout() {

        FormLayout formLayout = new FormLayout();
        formLayout.addFormItem(name, ProjectTask.getHeaderName());
        formLayout.addFormItem(wbs, ProjectTask.getHeaderWbs());
        formLayout.addFormItem(startDate, ProjectTask.getHeaderStartDate());
        formLayout.addFormItem(finishDate, ProjectTask.getHeaderFinishDate());

        mainDataLayout.add(formLayout);

    }

    private void customizeFields() {

        wbs.setEnabled(false);
        dateOfCreation.setEnabled(false);
        updateDate.setEnabled(false);
        name.setAutofocus(true);
        dateOfCreation.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        updateDate.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        version.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        name.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        wbs.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        startDate.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        finishDate.addThemeVariants(DatePickerVariant.LUMO_SMALL);

    }

    private void customizeForm() {

        setDraggable(true);
        setResizable(true);
        addClassName("project-task-form");
        addThemeVariants(DialogVariant.MATERIAL_NO_PADDING);

    }

    private void tabsSelectedListener(Tabs.SelectedChangeEvent selectedChangeEvent) {
        content.removeAll();
        if (selectedChangeEvent.getSelectedTab() == mainDataTab) {
            content.add(mainDataLayout);
        } else if (selectedChangeEvent.getSelectedTab() == linksTab) {
            content.add(linksLayout);
        }
    }

    private void customizeHeader() {

        Button closeButton = new Button(new Icon("lumo", "cross"),
                (e) -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getHeader().add(closeButton);

    }

    private FormLayout getMetadataFields() {
        FormLayout formLayout = new FormLayout();
        formLayout.addFormItem(version, ProjectTask.getHeaderVersion());
        formLayout.addFormItem(dateOfCreation, ProjectTask.getHeaderDateOfCreation());
        formLayout.addFormItem(updateDate, ProjectTask.getHeaderUpdateDate());
        //formLayout.add(version, dateOfCreation, updateDate);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("320px", 2),
                new FormLayout.ResponsiveStep("500px", 3));
        return formLayout;
    }

    private void validateAndSave() {
        try {
            binder.writeBean(projectTask);
            ObjectGrid.Changes<Link> changes = linksGrid.getChanges();
            //fireEvent(new SaveEvent(this, projectTask));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    private HorizontalLayout createButtonsLayout() {

        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

//        save.addClickShortcut(Key.ENTER);
//        close.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, projectTask)));
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));


        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

        return new HorizontalLayout(save, delete, close);
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

