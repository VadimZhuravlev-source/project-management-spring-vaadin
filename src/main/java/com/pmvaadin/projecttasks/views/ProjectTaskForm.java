package com.pmvaadin.projecttasks.views;

import com.pmvaadin.calendars.entity.Calendar;
import com.pmvaadin.projectstructure.NotificationDialogs;
import com.pmvaadin.calendars.view.CalendarSelectionForm;
import com.pmvaadin.commonobjects.SelectableTextField;
import com.pmvaadin.projecttasks.data.ProjectTaskData;
import com.pmvaadin.projecttasks.data.ProjectTaskDataImpl;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.links.views.LinksProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.services.ProjectTaskDataService;
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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.util.ArrayList;
import java.util.List;

@SpringComponent
public class ProjectTaskForm extends Dialog {

    private ProjectTask projectTask;
    private final ProjectTaskDataService projectTaskDataService;
    private final LinksProjectTask linksGrid;
    private final CalendarSelectionForm calendarSelectionForm;

    private final TextField version = new TextField(ProjectTask.getHeaderVersion());
    private final DatePicker dateOfCreation = new DatePicker(ProjectTask.getHeaderDateOfCreation());
    private final DatePicker updateDate = new DatePicker(ProjectTask.getHeaderUpdateDate());
    private final TextField name = new TextField();
    private final TextField wbs = new TextField();
    private final SelectableTextField<Calendar> calendar = new SelectableTextField<>();
    private final DatePicker startDate = new DatePicker();
    private final DatePicker finishDate = new DatePicker();
    private final Binder<ProjectTask> binder = new BeanValidationBinder<>(ProjectTask.class);

    private final Tabs tabs = new Tabs();
    private final VerticalLayout content = new VerticalLayout();
    private final Tab mainDataTab = new Tab("Main");
    private final VerticalLayout mainDataLayout = new VerticalLayout();
    private final Tab linksTab = new Tab("Predecessors");
    private final VerticalLayout linksLayout = new VerticalLayout();

    private final Button save = new Button("Save");
    private final Button close = new Button("Cancel");
    private final Button sync = new Button("Refresh", new Icon("lumo", "reload"));

    public ProjectTaskForm(ProjectTaskDataService projectTaskDataService, LinksProjectTask linksGrid,
                           CalendarSelectionForm calendarSelectionForm) {

        super();

        this.projectTaskDataService = projectTaskDataService;
        this.linksGrid = linksGrid;
        this.calendarSelectionForm = calendarSelectionForm;

        customizeForm();
        customizeHeader();
        customizeTabs();
        customizeFields();
        createButtons();
        addTabs();

        binder.bindInstanceFields(this);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(false);

        content.add(mainDataLayout);
        mainLayout.add(getMetadataFields(), tabs, content);

        add(mainLayout);

    }

    public void setProjectTask(ProjectTask projectTask) {
        this.projectTask = projectTask;
        refreshHeader();
        linksGrid.setProjectTask(projectTask);
        binder.readBean(projectTask);
        name.focus();
    }

    private void customizeForm() {

        setWidth("80%");
        setHeight("80%");
        setDraggable(true);
        setResizable(true);
        addClassName("project-task-form");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);

    }

    private void refreshHeader() {
        String projectTaskName = projectTask.getName();
        if (projectTaskName == null) projectTaskName = "";
        setHeaderTitle("Project task: " + projectTaskName);
    }

    private void addTabs() {

        tabs.add(mainDataTab, linksTab);
        tabs.addSelectedChangeListener(this::tabsSelectedListener);

    }

    private void customizeTabs() {

        customizeMainDataLayout();
        linksLayout.add(linksGrid);

    }

    private void customizeMainDataLayout() {

        FormLayout formLayout = new FormLayout();
        formLayout.addFormItem(name, ProjectTask.getHeaderName());
        formLayout.addFormItem(wbs, ProjectTask.getHeaderWbs());
        formLayout.addFormItem(calendar, ProjectTask.getHeaderCalendar());
        formLayout.addFormItem(startDate, ProjectTask.getHeaderStartDate());
        formLayout.addFormItem(finishDate, ProjectTask.getHeaderFinishDate());

        mainDataLayout.add(formLayout);

    }

    private void customizeFields() {

        wbs.setEnabled(false);
        dateOfCreation.setEnabled(false);
        updateDate.setEnabled(false);
        name.setAutofocus(true);
        calendar.setSelectable(true);
        calendar.addSelectionListener(event -> {
            calendarSelectionForm.addSelectionListener(
                    selectedProjectTask -> {
                        calendar.setValue(selectedProjectTask);
                        calendar.refreshTextValue();
                        calendar.setReadOnly(true);
                    });
            calendarSelectionForm.open();
        });
        dateOfCreation.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        updateDate.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        version.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        name.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        wbs.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        startDate.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        finishDate.addThemeVariants(DatePickerVariant.LUMO_SMALL);

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
        formLayout.add(version);
        formLayout.add(dateOfCreation);
        formLayout.add(updateDate);
        //formLayout.add(version, dateOfCreation, updateDate);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("300px", 3));
        return formLayout;
    }

    private boolean validateAndSave() {
        try {
            binder.writeBean(projectTask);
            boolean isOk = linksGrid.validate();
            if (!isOk) {
                tabs.setSelectedTab(linksTab);
                return false;
            }
            ProjectTaskData projectTaskData = new ProjectTaskDataImpl(
                    projectTask,
                    linksGrid.getChanges(),
                    new ArrayList<>()
            );
            ProjectTaskData savedData = projectTaskDataService.save(projectTaskData);
            readData(savedData);
        } catch (Throwable e) {
            NotificationDialogs.notifyValidationErrors(e.getMessage());
            return false;
        }

        return true;

    }

    private void syncData() {
        try {

            if (projectTask.isNew()) return;
            ProjectTaskData projectTaskData = projectTaskDataService.read(projectTask);
            readData(projectTaskData);

        } catch (Throwable e) {
            NotificationDialogs.notifyValidationErrors(e.getMessage());
        }

    }

    private void readData(ProjectTaskData projectTaskData) {
        this.projectTask = projectTaskData.getProjectTask();
        binder.readBean(projectTask);
        linksGrid.setItems((List<Link>) (projectTaskData.getLinks()));
        refreshHeader();
    }

    private void createButtons() {

        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickListener(event -> {

            boolean validationDone = validateAndSave();
            if (!validationDone) return;
            fireEvent(new SaveEvent(this, projectTask));

        });
//        save.getStyle().set("margin-right", "auto");
        sync.addClickListener(event -> syncData());
        sync.getStyle().set("margin-right", "auto");
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
        getFooter().add(save, sync, close);

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

