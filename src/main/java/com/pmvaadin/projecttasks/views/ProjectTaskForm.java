package com.pmvaadin.projecttasks.views;

import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.projectstructure.NotificationDialogs;
import com.pmvaadin.terms.calendars.view.CalendarSelectionForm;
import com.pmvaadin.commonobjects.SelectableTextField;
import com.pmvaadin.projecttasks.data.ProjectTaskData;
import com.pmvaadin.projecttasks.data.ProjectTaskDataImpl;
import com.pmvaadin.projecttasks.links.views.LinksProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.services.ProjectTaskDataService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePickerVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.LocalDateToDateConverter;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Function;

@SpringComponent
public class ProjectTaskForm extends Dialog {

    private ProjectTaskData projectTaskData;
    private final ProjectTaskDataService projectTaskDataService;
    private final LinksProjectTask linksGrid;
    private final CalendarSelectionForm calendarSelectionForm;
    private final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");;

    private LocalDateTime projectStartDate = LocalDateTime.now();

    private final TextField id = new TextField(ProjectTask.getHeaderId());
    private final TextField version = new TextField(ProjectTask.getHeaderVersion());
    private final TextField dateOfCreation = new TextField(ProjectTask.getHeaderDateOfCreation());
    private final TextField updateDate = new TextField(ProjectTask.getHeaderUpdateDate());
    private final TextField name = new TextField();
    private final TextField wbs = new TextField();

    // Term fields
    private final SelectableTextField<Calendar> calendarField = new SelectableTextField<>();
    private final DatePicker startDate = new DatePicker();
    private final DatePicker finishDate = new DatePicker();
    private final TextField duration = new TextField();
    private final TextField durationRepresentation = new TextField();
    private final TextField timeUnitId = new TextField();
    // End term fields

    private final Binder<ProjectTask> binder = new BeanValidationBinder<>(ProjectTask.class);

    private final Tab mainDataTab = new Tab("Main");
    private final Tab linksTab = new Tab("Predecessors");
    private final TabSheet tabSheet = new TabSheet();

    // this need to stretch a grid in a tab
    private final VerticalLayout linksGridContainer = new VerticalLayout();

    private final Button save = new Button("Save");
    private final Button close = new Button("Cancel");
    private final Button sync = new Button("Refresh", new Icon("lumo", "reload"));

    public ProjectTaskForm(ProjectTaskDataService projectTaskDataService, LinksProjectTask linksGrid,
                           CalendarSelectionForm calendarSelectionForm) {

        this.projectTaskDataService = projectTaskDataService;
        this.linksGrid = linksGrid;
        this.calendarSelectionForm = calendarSelectionForm;

        customizeForm();
        customizeHeader();
        customizeTabs();
        customizeFields();
        createButtons();
        customizeBinder();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);

        mainLayout.add(getMetadataFields(), tabSheet);

        add(mainLayout);

    }

    public ProjectTaskForm newInstance() {
        return new ProjectTaskForm(projectTaskDataService, linksGrid.newInstance(), calendarSelectionForm);
    }

    public void setProjectTask(ProjectTask projectTask) {
        //this.projectTask = projectTask;
        projectTaskData = projectTaskDataService.getInstance(projectTask);
        refreshHeader();
        linksGrid.setProjectTask(projectTaskData.getProjectTask());
        linksGrid.setItems(projectTaskData.getLinks());
        binder.readBean(projectTaskData.getProjectTask());
        name.focus();
    }

    private void customizeForm() {

        setWidth("90%");
        setHeight("90%");
        setDraggable(true);
        setResizable(true);
        addClassName("project-task-form");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        setModal(false);

    }

    private void refreshHeader() {
        String projectTaskName = projectTaskData.getProjectTask().getName();
        if (projectTaskName == null) projectTaskName = "";
        setHeaderTitle("Project task: " + projectTaskName);
    }

    private void customizeTabs() {

        tabSheet.setSizeFull();
        customizeMainDataLayout();
        linksGridContainer.add(linksGrid);
        linksGridContainer.setSizeFull();
        tabSheet.add(linksTab, linksGridContainer);

    }

    private void customizeMainDataLayout() {

        FormLayout formLayout = new FormLayout();
        formLayout.addFormItem(name, ProjectTask.getHeaderName());
        formLayout.addFormItem(wbs, ProjectTask.getHeaderWbs());
        FormLayout termsLayout = new FormLayout();
        termsLayout.addFormItem(calendarField, ProjectTask.getHeaderCalendar());
        termsLayout.addFormItem(startDate, ProjectTask.getHeaderStartDate());
        termsLayout.addFormItem(finishDate, ProjectTask.getHeaderFinishDate());
        termsLayout.addFormItem(duration, "Duration seconds");
        termsLayout.addFormItem(durationRepresentation, ProjectTask.getHeaderDurationRepresentation());
        termsLayout.addFormItem(timeUnitId, "Time unit id");

        VerticalLayout verticalLayout = new VerticalLayout(formLayout, termsLayout);
        tabSheet.add(mainDataTab, verticalLayout);

    }

    private void customizeFields() {

        wbs.setEnabled(false);
        dateOfCreation.setEnabled(false);
        updateDate.setEnabled(false);
        name.setAutofocus(true);
        calendarField.setSelectable(true);
        calendarField.addSelectionListener(event -> {
            calendarSelectionForm.addSelectionListener(
                    selectedItem -> {
                        calendarField.setValue(selectedItem);
                        calendarField.refreshTextValue();
                        calendarField.setReadOnly(true);
                        projectTaskData.getProjectTask().setCalendarId(selectedItem.getId());
                    });
            calendarSelectionForm.open();
        });
        dateOfCreation.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        updateDate.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        version.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        id.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        name.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        wbs.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        startDate.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        finishDate.addThemeVariants(DatePickerVariant.LUMO_SMALL);

    }

    private void customizeHeader() {

        Button closeButton = new Button(new Icon("lumo", "cross"),
                (e) -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getHeader().add(closeButton);

    }

    private Component getMetadataFields() {

        FormLayout formLayout = new FormLayout();
        formLayout.add(id);
        formLayout.add(version);
        formLayout.add(dateOfCreation);
        formLayout.add(updateDate);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("300px", 4));
        Details details = new Details("Metadata", formLayout);
        details.setOpened(false);
        return details;

    }

    private boolean validateAndSave() {
        try {
            binder.writeBean(projectTaskData.getProjectTask());
            boolean isOk = linksGrid.validate();
            if (!isOk) {
                tabSheet.setSelectedTab(linksTab);
                return false;
            }
//            ProjectTaskData projectTaskData = new ProjectTaskDataImpl(
//                    projectTask,
//                    linksGrid.getChanges(),
//                    new ArrayList<>(),
//                    projectStartDate);
            projectTaskData.setLinksChangedTableData(linksGrid.getChanges());
            projectTaskData.setLinks(new ArrayList<>());
            ProjectTaskData savedData = projectTaskDataService.save(projectTaskData);
            projectStartDate = savedData.getProjectStartDate();
            readData(savedData);
        } catch (Throwable e) {
            NotificationDialogs.notifyValidationErrors(e.getMessage());
            return false;
        }

        return true;

    }

    private void syncData() {
        try {

            ProjectTask projectTask = projectTaskData.getProjectTask();
            if (projectTask.isNew()) return;
            ProjectTaskData projectTaskData = projectTaskDataService.read(projectTask);
            projectStartDate = projectTaskData.getProjectStartDate();
            readData(projectTaskData);

        } catch (Throwable e) {
            NotificationDialogs.notifyValidationErrors(e.getMessage());
        }

    }

    private void readData(ProjectTaskData projectTaskData) {
        //this.projectTask = projectTaskData.getProjectTask();
        binder.readBean(projectTaskData.getProjectTask());
        linksGrid.setItems(projectTaskData.getLinks());
        refreshHeader();
    }

    private void createButtons() {

        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickListener(event -> {

            boolean validationDone = validateAndSave();
            if (!validationDone) return;
            fireEvent(new SaveEvent(this, projectTaskData.getProjectTask()));

        });
//        save.getStyle().set("margin-right", "auto");
        sync.addClickListener(event -> syncData());
        sync.getStyle().set("margin-right", "auto");
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
        getFooter().add(save, sync, close);

    }

    private void customizeBinder() {

        binder.forField(id).bindReadOnly((p) -> {
            Object id = p.getId();
            String idString = "";
            if (id != null) {
                idString = id.toString();
            }
            return idString;
        });

        binder.forField(dateOfCreation).bindReadOnly((p) -> convertDateToString(ProjectTask::getDateOfCreation, p));
        binder.forField(updateDate).bindReadOnly((p) -> convertDateToString(ProjectTask::getUpdateDate, p));

        binder.forField(startDate).withConverter(new LocalDateToDateConverter())
                .bind(this::getStartDate, this::setStartDate);
        binder.forField(finishDate).withConverter(new LocalDateToDateConverter())
                .bind(this::getFinishDate, this::setFinishDate);
        binder.bindInstanceFields(this);

    }

    private String convertDateToString(Function<ProjectTask, Date> dateGetter, ProjectTask projectTask) {
        Date date = dateGetter.apply(projectTask);
        String dateString = "";
        if (date != null) dateString = dateFormat.format(date);
        return dateString;
    }

    private Date convertLocalDateTimeToDate(LocalDateTime date) {
        if (date == null) return new Date();
        return Date.from(date.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime convertDateToLocalDateTime(LocalDateTime localDate, Date date) {
        LocalDate chosenDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        if (localDate == null) localDate = projectStartDate;
        LocalTime time = localDate.toLocalTime();
        // TODO check is from working day of the task calendar
        return LocalDateTime.of(chosenDate, time);
    }

    private Date getStartDate(ProjectTask task) {

        LocalDateTime startDate = task.getStartDate();
        return convertLocalDateTimeToDate(startDate);

    }

    private void setStartDate(ProjectTask task, Date date) {

        LocalDateTime startDate = task.getStartDate();
        LocalDateTime newDate = convertDateToLocalDateTime(startDate, date);
        task.setStartDate(newDate);

    }

    private Date getFinishDate(ProjectTask task) {

        return convertLocalDateTimeToDate(task.getFinishDate());

    }

    private void setFinishDate(ProjectTask task, Date date) {

        LocalDateTime newDate = convertDateToLocalDateTime(task.getFinishDate(), date);
        task.setFinishDate(newDate);

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

