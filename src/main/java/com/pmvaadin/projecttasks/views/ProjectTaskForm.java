package com.pmvaadin.projecttasks.views;

import com.pmvaadin.projecttasks.commonobjects.BigDecimalToDoubleConverter;
import com.pmvaadin.projecttasks.entity.ScheduleMode;
import com.pmvaadin.projectview.ProjectTaskPropertyNames;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.projectstructure.NotificationDialogs;
import com.pmvaadin.terms.calendars.view.CalendarSelectionForm;
import com.pmvaadin.commonobjects.SelectableTextField;
import com.pmvaadin.projecttasks.data.ProjectTaskData;
import com.pmvaadin.projecttasks.links.views.LinksProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.services.ProjectTaskDataService;
import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.pmvaadin.terms.timeunit.services.TimeUnitService;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.*;
import com.vaadin.flow.data.converter.LocalDateToDateConverter;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Stream;

@SpringComponent
public class ProjectTaskForm extends Dialog {

    private ProjectTaskData projectTaskData;
    private final ProjectTaskDataService projectTaskDataService;
    private final TimeUnitService timeUnitService;
    private final LinksProjectTask linksGrid;
    private final CalendarSelectionForm calendarSelectionForm;
    private final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private final TextField id = new TextField();
    private final TextField version = new TextField();
    private final TextField dateOfCreation = new TextField();
    private final TextField updateDate = new TextField();
    private final TextField name = new TextField();
    private final TextField wbs = new TextField();

    // Term fields
    private final SelectableTextField<Calendar> calendarField = new SelectableTextField<>();
    private final DatePicker startDate = new DatePicker();
    private final DatePicker finishDate = new DatePicker();
    private final NumberField durationRepresentation = new NumberField();
    private final ComboBox<ScheduleMode> scheduleMode = new ComboBox<>();
    private final ComboBox<TimeUnit> timeUnitComboBox = new ComboBox<>();
    private boolean changeDuration = true;

    // End term fields

    private final Binder<ProjectTask> binder = new BeanValidationBinder<>(ProjectTask.class);

    private final Tab mainDataTab = new Tab("Main");
    private final Tab linksTab = new Tab("Predecessors");
    private final TabSheet tabSheet = new TabSheet();

    // this need to stretch a grid in a tab
    private final VerticalLayout linksGridContainer = new VerticalLayout();
    private final ProjectTaskPropertyNames propertyNames = new ProjectTaskPropertyNames();


    public ProjectTaskForm(ProjectTaskDataService projectTaskDataService, LinksProjectTask linksGrid,
                           CalendarSelectionForm calendarSelectionForm, TimeUnitService timeUnitService) {

        this.projectTaskDataService = projectTaskDataService;
        this.linksGrid = linksGrid;
        this.calendarSelectionForm = calendarSelectionForm;
        this.timeUnitService = timeUnitService;

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
        return new ProjectTaskForm(projectTaskDataService, linksGrid.newInstance(), calendarSelectionForm,
                timeUnitService);
    }

    public void setProjectTask(ProjectTask projectTask) {

        projectTaskData = projectTaskDataService.read(projectTask);
        readData(projectTaskData);
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
        setCloseOnEsc(false);
        //this.addListener(Class<ProjectTaskForm>, )

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
        tabSheet.addSelectedChangeListener(this::selectedTabChangeListener);

    }

    private void selectedTabChangeListener(TabSheet.SelectedChangeEvent event) {

        if (event.getSelectedTab() != linksTab && linksGrid.isEditing()) {
            linksGrid.endEditing();
        }

    }

    private void customizeMainDataLayout() {

        FormLayout formLayout = new FormLayout();
        formLayout.addFormItem(name, propertyNames.getHeaderName());
        formLayout.addFormItem(wbs, propertyNames.getHeaderWbs());
        FormLayout termsLayout = new FormLayout();
        termsLayout.addFormItem(startDate, propertyNames.getHeaderStartDate());
        termsLayout.addFormItem(finishDate, propertyNames.getHeaderFinishDate());
        termsLayout.addFormItem(scheduleMode, propertyNames.getHeaderScheduleMode());
        termsLayout.addFormItem(calendarField, propertyNames.getHeaderCalendar());
        termsLayout.addFormItem(durationRepresentation, propertyNames.getHeaderDurationRepresentation());
        termsLayout.addFormItem(timeUnitComboBox, propertyNames.getHeaderTimeUnit());

        VerticalLayout verticalLayout = new VerticalLayout(formLayout, termsLayout);
        tabSheet.add(mainDataTab, verticalLayout);

    }

    private void customizeFields() {

        id.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        version.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        wbs.setEnabled(false);
        wbs.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        dateOfCreation.setEnabled(false);
        dateOfCreation.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        updateDate.setEnabled(false);
        updateDate.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        name.setAutofocus(true);
        name.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        calendarField.setSelectable(true);

        calendarField.addSelectionListener(event -> {
            var currentInstanceOfCalendarSelectionForm = calendarSelectionForm.newInstance();
            currentInstanceOfCalendarSelectionForm.addSelectionListener(this::calendarSelectionListener);
            currentInstanceOfCalendarSelectionForm.open();
        });
        calendarField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        startDate.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        startDate.addValueChangeListener(this::startDateChangeListener);
        finishDate.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        finishDate.addValueChangeListener(this::finishDateChangeListener);
        durationRepresentation.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        durationRepresentation.setStepButtonsVisible(true);
        durationRepresentation.setStep(1);
        durationRepresentation.addValueChangeListener(this::durationValueChangeListener);
        scheduleMode.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        scheduleMode.setItems(ScheduleMode.values());
        scheduleMode.addValueChangeListener(this::scheduleModeAddListener);
        timeUnitComboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        timeUnitComboBox.setItems(this::getPageTimeUnit, this::getCountItemsInPageByName);
        timeUnitComboBox.addValueChangeListener(this::TimeUnitChangeListener);

        id.setLabel(propertyNames.getHeaderId());
        version.setLabel(propertyNames.getHeaderVersion());
        dateOfCreation.setLabel(propertyNames.getHeaderDateOfCreation());
        updateDate.setLabel(propertyNames.getHeaderUpdateDate());

    }

    private void startDateChangeListener(AbstractField.ComponentValueChangeEvent<DatePicker, LocalDate> component) {

        LocalDate selectedDate = component.getValue();
        if (selectedDate == null) {
            startDate.setValue(projectTaskData.getProjectTask().getStartDate().toLocalDate());
            return;
        }
        Calendar calendar = calendarField.getValue();
        LocalDateTime newStartDate;
        if (projectTaskData.getProjectStartDate().toLocalDate().equals(selectedDate))
            newStartDate = projectTaskData.getProjectStartDate();
        else
            newStartDate = calendar.getClosestWorkingDay(LocalDateTime.of(selectedDate, calendar.getStartTime()));
        if (!newStartDate.toLocalDate().equals(selectedDate)) startDate.setValue(newStartDate.toLocalDate());

        projectTaskData.getProjectTask().setStartDate(newStartDate);

        recalculateFinishDateByDuration();

    }

    private void finishDateChangeListener(AbstractField.ComponentValueChangeEvent<DatePicker, LocalDate> component) {

        LocalDate selectedDate = component.getValue();
        ProjectTask projectTask = projectTaskData.getProjectTask();
        if (selectedDate == null) {
            finishDate.setValue(projectTask.getFinishDate().toLocalDate());
            return;
        }

        Calendar calendar = calendarField.getValue();
        LocalDateTime newFinishDate = calendar.getEndOfWorkingDay(selectedDate);
        if (newFinishDate.toLocalTime().equals(calendar.getStartTime())) {
            newFinishDate = calendar.getClosestWorkingDay(newFinishDate);
            newFinishDate = calendar.getEndOfWorkingDay(newFinishDate.toLocalDate());
        }

        if (newFinishDate.compareTo(projectTask.getStartDate()) <= 0) {
            String message = "The selected date can not be less than the start date of the task";
            NotificationDialogs.notifyValidationErrors(message);
            finishDate.setValue(component.getOldValue());
        }

        projectTask.setFinishDate(newFinishDate);
        long duration = calendar.getDuration(projectTask.getStartDate(), newFinishDate);
        projectTask.setDuration(duration);
        BigDecimal bigDecimal = projectTaskData.getTimeUnit().getDurationRepresentation(duration);
        changeDuration = false;
        durationRepresentation.setValue(bigDecimal.doubleValue());

        if (!newFinishDate.toLocalDate().equals(selectedDate)) finishDate.setValue(newFinishDate.toLocalDate());

    }

    private void calendarSelectionListener(Calendar selectedItem) {

        if (selectedItem == null) return;
        calendarField.setValue(selectedItem);
        calendarField.refreshTextValue();
        calendarField.setReadOnly(true);
        projectTaskData.getProjectTask().setCalendarId(selectedItem.getId());
    }

    private void scheduleModeAddListener(AbstractField.ComponentValueChangeEvent<ComboBox<ScheduleMode>, ScheduleMode> component) {

        ScheduleMode currentScheduleMode = component.getValue();
        ProjectTask projectTask = projectTaskData.getProjectTask();
        if (currentScheduleMode == ScheduleMode.MANUALLY || projectTask.getParentId() == null) {
            startDate.setReadOnly(false);
            return;
        }
        startDate.setReadOnly(true);
        startDate.setValue(projectTaskData.getProjectStartDate().toLocalDate());

        projectTask.setStartDate(projectTaskData.getProjectStartDate());
        recalculateFinishDateByDuration();

    }

    private void recalculateFinishDateByDuration() {

        long duration = projectTaskData.getProjectTask().getDuration();
        LocalDateTime startDate = projectTaskData.getProjectTask().getStartDate();
        LocalDateTime newFinishDate = calendarField.getValue().getDateByDuration(startDate, duration);
        projectTaskData.getProjectTask().setFinishDate(newFinishDate);
        finishDate.setValue(newFinishDate.toLocalDate());

    }

    private void durationValueChangeListener(AbstractField.ComponentValueChangeEvent<NumberField, Double> component) {

        if (!changeDuration) {
            changeDuration = true;
            return;
        }
        Double value = component.getValue();

        if (value == null || projectTaskData == null) return;
        TimeUnit timeUnit = projectTaskData.getTimeUnit();
        BigDecimal bigDecimal = new BigDecimal(value);
        long duration = timeUnit.getDuration(bigDecimal);
        projectTaskData.getProjectTask().setDuration(duration);
        // changing finish date
        LocalTime startTime = projectTaskData.getProjectTask().getStartDate().toLocalTime();
        LocalDateTime currentStartDate = LocalDateTime.of(startDate.getValue(), startTime);
        LocalDateTime newFinishDate = calendarField.getValue().getDateByDuration(currentStartDate, duration);
        finishDate.setValue(newFinishDate.toLocalDate());

    }

    private void TimeUnitChangeListener(AbstractField.ComponentValueChangeEvent<ComboBox<TimeUnit>, TimeUnit> component) {

        TimeUnit timeUnit = component.getValue();
        if (timeUnit == null) timeUnit = component.getOldValue();
        if (timeUnit == null) {
            timeUnit = projectTaskData.getTimeUnit();
            timeUnitComboBox.setValue(timeUnit);
        }
        projectTaskData.getProjectTask().setTimeUnitId(timeUnit.getId());
        projectTaskData.setTimeUnit(timeUnit);
        long duration = projectTaskData.getProjectTask().getDuration();
        changeDuration = false;
        durationRepresentation.setValue(timeUnit.getDurationRepresentation(duration).doubleValue());

    }

    private Stream<TimeUnit> getPageTimeUnit(Query<TimeUnit, String> query) {
        return timeUnitService.getPageByName(query).stream();
    }

    private int getCountItemsInPageByName(Query<TimeUnit, String> query) {
        return timeUnitService.getCountPageItemsByName(query);
    }

    private void customizeHeader() {

        Button closeButton = new Button(new Icon("lumo", "cross"),
                e -> fireEvent(new CloseEvent(this))
                );
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Shortcuts.addShortcutListener(this, this::onShortcutEvent, Key.ESCAPE);

        getHeader().add(closeButton);

    }

    private void onShortcutEvent(ShortcutEvent event) {

        if (event.matches(Key.ESCAPE)) {
            if (tabSheet.getSelectedTab() == linksTab && linksGrid.isEditing()) {
                linksGrid.endEditing();
                return;
            }
            fireEvent(new CloseEvent(this));
        }

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
            // TODO terms validator
            boolean isOk = linksGrid.validate();
            if (!isOk) {
                tabSheet.setSelectedTab(linksTab);
                return false;
            }

            //projectTaskData.setLinksChangedTableData(linksGrid.getChanges());
            projectTaskData.setLinks(linksGrid.getLinks());
            projectTaskData.setLinksChangedTableData(null);
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

            ProjectTask projectTask = projectTaskData.getProjectTask();
            if (projectTask.isNew()) return;
            ProjectTaskData projectTaskData = projectTaskDataService.read(projectTask);
            readData(projectTaskData);

        } catch (Throwable e) {
            NotificationDialogs.notifyValidationErrors(e.getMessage());
        }

    }

    private void readData(ProjectTaskData projectTaskData) {

        this.projectTaskData = projectTaskData;
        linksGrid.setProjectTask(projectTaskData);
        refreshHeader();
        calendarField.setValue(projectTaskData.getCalendar());
        calendarField.refreshTextValue();
        calendarField.setReadOnly(true);
        timeUnitComboBox.setValue(projectTaskData.getTimeUnit());
        changeDuration = false;
        durationRepresentation.setValue(projectTaskData.getProjectTask().getDurationRepresentation().doubleValue());
        binder.readBean(projectTaskData.getProjectTask());

    }

    private void createButtons() {

        Button saveAndClose = new Button("Save and close");
        saveAndClose.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveAndClose.addClickListener(event -> {

            boolean validationDone = validateAndSave();
            if (!validationDone) return;
            fireEvent(new SaveEvent(this, projectTaskData.getProjectTask()));

        });
        binder.addStatusChangeListener(e -> saveAndClose.setEnabled(binder.isValid()));

        Button sync = new Button("Refresh", new Icon("lumo", "reload"));
        sync.addClickListener(event -> syncData());
        sync.getStyle().set("margin-right", "auto");

        Button close = new Button("Close");
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));

        Button save = new Button("Save");
        save.addClickListener(event -> validateAndSave());
        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

        getFooter().add(saveAndClose, save, sync, close);

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
        binder.forField(durationRepresentation).withConverter(new BigDecimalToDoubleConverter(durationRepresentation))
                .bind(ProjectTask::getDurationRepresentation, ProjectTask::setDurationRepresentation);

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
        if (localDate == null) localDate = projectTaskData.getProjectStartDate();
        LocalTime time = localDate.toLocalTime();
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

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

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

}

