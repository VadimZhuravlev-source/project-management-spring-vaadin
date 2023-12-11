package com.pmvaadin.terms.calendars.view;

import com.pmvaadin.commonobjects.ObjectGrid;
import com.pmvaadin.projectstructure.NotificationDialogs;
import com.pmvaadin.terms.calendars.common.ExceptionLength;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.entity.CalendarSettings;
import com.pmvaadin.terms.calendars.exceptions.CalendarException;
import com.pmvaadin.terms.calendars.validators.CalendarValidation;
import com.pmvaadin.terms.calendars.validators.CalendarValidationImpl;
import com.pmvaadin.terms.calendars.workingweeks.views.WorkingWeekForm;
import com.pmvaadin.terms.calendars.services.CalendarService;
import com.pmvaadin.terms.calendars.workingweeks.WorkingWeek;
import com.pmvaadin.terms.calendars.exceptions.views.CalendarExceptionForm;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.select.SelectVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@SpringComponent
public class CalendarForm extends Dialog {

    private Calendar calendar;
    private final CalendarService calendarService;
    private final CalendarValidation calendarValidation = new CalendarValidationImpl();

    private final TextField name = new TextField();
    private final ComboBox<CalendarSettings> setting = new ComboBox<>();

    private final Binder<Calendar> binder = new BeanValidationBinder<>(Calendar.class);

    private final Button sync = new Button("Refresh", new Icon("lumo", "reload"));

//    private final Map<DayOfWeek, NumberField> dayOfWeekMap = new LinkedHashMap<>(7);

    private final Select<DayOfWeek> endOfWeek = new Select<>();

    // Working week
    private final WorkingWeeks workingWeeks = new WorkingWeeks();

    // Exceptions
    private final Exceptions exceptions = new Exceptions();

    // tabs
    private final Tab exceptionsTab = new Tab("Exceptions");
    private final Tab workWeeksTab = new Tab("Work Weeks");
    private final TabSheet tabSheet = new TabSheet();

    public CalendarForm(CalendarService calendarService) {

        this.calendarService = calendarService;
        //fillDayOfWeekMap();
        customizeHeader();
        customizeForm();
        customizeDataLayout();
        createButtons();
        customizeBinder();
        customizeElements();

    }

    public CalendarForm newInstance() {
        return new CalendarForm(calendarService);
    }

    public void read(Calendar calendar) {

        this.calendar = calendar;
        read();

    }

    public Map<LocalDate, ExceptionLength> getExceptionsDate() {
        var map = new HashMap<LocalDate, ExceptionLength>();
        this.exceptions.getItems().forEach(e -> map.putAll(e.getExceptionAsDayConstraint()));
        return map;
    }

    private void customizeElements() {

        endOfWeek.setItems(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        endOfWeek.addThemeVariants(SelectVariant.LUMO_SMALL);
        endOfWeek.setRenderer(new ComponentRenderer<>(dayOfWeek -> new Text(dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()))));

        tabSheet.setSizeFull();

        this.setting.setItems(CalendarSettings.values());

    }

    private void customizeBinder() {

        binder.bindInstanceFields(this);

    }

    private void read() {

        binder.readBean(this.calendar);
        refreshHeader();
        workingWeeks.setWorkingWeeks(calendar.getWorkingWeeks());
        exceptions.setInstantiatable(this::getCalendarExceptionInstance);
        exceptions.setCalendarExceptions(this.calendar.getCalendarExceptions());

        if (this.calendar.isNew()) sync.setEnabled(false);

    }

    private CalendarException getCalendarExceptionInstance() {
        var instance = this.calendar.getCalendarExceptionInstance();
        instance.setEndOfWeek(this.endOfWeek.getValue());
        return instance;
    }

    private void customizeForm() {

        setDraggable(true);
        setResizable(true);
        addClassName("calendar-form");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        setModal(false);
        setCloseOnEsc(false);
        //this.addListener(Class<ProjectTaskForm>, )

    }

    private void customizeDataLayout() {

        var mainLayout = new FormLayout();
        mainLayout.addFormItem(name, "Name");
        mainLayout.addFormItem(setting, "Setting");

        var vertLayout = new VerticalLayout(endOfWeek, exceptions);

        tabSheet.add(exceptionsTab, vertLayout);
        tabSheet.add(workWeeksTab, workingWeeks);

        var yearCalendar = new YearCalendar(this);

        var verticalLayout = new VerticalLayout(yearCalendar, mainLayout, tabSheet);
        super.add(verticalLayout);

    }

    private void refreshHeader() {
        var calendarName = calendar.getName();
        if (calendarName == null) calendarName = "";
        var title = Calendar.getHeaderName();
        setHeaderTitle(title + ": " + calendarName);
    }

    private void customizeHeader() {

        Button closeButton = new Button(new Icon("lumo", "cross"),
                e -> fireEvent(new CloseEvent(this))
        );
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickShortcut(Key.ESCAPE);

        getHeader().add(closeButton);

    }

    private void createButtons() {

        Button saveAndClose = new Button("Save and close");
        saveAndClose.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveAndClose.addClickListener(event -> {

            boolean validationDone = validateAndSave();
            if (!validationDone) return;
            fireEvent(new SaveEvent(this));

        });
        binder.addStatusChangeListener(e -> saveAndClose.setEnabled(binder.isValid()));

        sync.addClickListener(event -> syncData());
        sync.getStyle().set("margin-right", "auto");

        Button close = new Button("Close");
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));

        Button save = new Button("Save");
        save.addClickListener(event -> {
            validateAndSave();
            read();
        });
        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

        getFooter().add(saveAndClose, save, sync, close);

    }

    private boolean validateAndSave() {
        try {
            binder.writeBean(this.calendar);
            calendarValidation.validate(this.calendar);
            //this.calendar.setCalendarException(exceptionDays.getItems());
            this.calendar.setWorkingWeeks(workingWeeks.getWorkingWeeks());
            this.calendar.setCalendarExceptions(exceptions.getCalendarExceptions());
            this.calendar = calendarService.save(this.calendar);
        } catch (Throwable e) {
            NotificationDialogs.notifyValidationErrors(e.getMessage());
            return false;
        }

        return true;

    }

    private void syncData() {
        if (this.calendar.isNew()) return;
        this.calendar = calendarService.getCalendarById(this.calendar.getId());
        read();
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    public static abstract class CalendarFormEvent extends ComponentEvent<CalendarForm> {

        protected CalendarFormEvent(CalendarForm source) {
            super(source, false);
        }

    }

    public static class CloseEvent extends CalendarFormEvent {
        CloseEvent(CalendarForm source) {
            super(source);
        }
    }

    public static class SaveEvent extends CalendarFormEvent {
        SaveEvent(CalendarForm source) {
            super(source);
        }
    }

    private static class Exceptions extends ObjectGrid<CalendarException> {

        Exceptions() {
            this.addColumns();
            this.customizeGrid();
        }

        public void setCalendarExceptions(List<CalendarException> exceptions) {
            this.grid.setItems(exceptions);
        }

        public List<CalendarException> getCalendarExceptions() {
            return this.grid.getListDataView().getItems().collect(Collectors.toList());
        }

        private void addColumns() {

            this.grid.addColumn(CalendarException::getName).setHeader("Name");
            this.grid.addColumn(CalendarException::getStart).setHeader("Start");
            this.grid.addColumn(CalendarException::getFinish).setHeader("Finish");

        }

        private void customizeGrid() {
            this.setDeletable(true);
            this.grid.addItemDoubleClickListener(event -> {
                var exception = event.getItem();
                if (exception == null) return;
                var calendarExceptionForm = new CalendarExceptionForm(exception);
                calendarExceptionForm.addListener(CalendarExceptionForm.SaveEvent.class, this::saveCalendarExceptionListener);
                calendarExceptionForm.open();
            });
        }

        private void saveCalendarExceptionListener(CalendarExceptionForm.SaveEvent event) {
            var calendarException = event.getCalendarException();
            if (calendarException == null) return;
            this.grid.getListDataView().refreshItem(calendarException);
        }

    }

    private class WorkingWeeks extends VerticalLayout {

        private final Grid<WorkingWeek> grid = new Grid<>();
        private final HorizontalLayout toolbar = new HorizontalLayout();

        WorkingWeeks() {
            this.addColumns();
            this.addButtonToToolbar();
            this.customizeGrid();
            this.add(toolbar, grid);
        }

        public void setWorkingWeeks(List<WorkingWeek> workingWeeks) {
            this.grid.setItems(workingWeeks);
        }

        public List<WorkingWeek> getWorkingWeeks() {
            return this.grid.getListDataView().getItems().collect(Collectors.toList());
        }

        private void addColumns() {

            grid.addColumn(WorkingWeek::getName).setHeader("Name");
            grid.addColumn(this::getStartRep).setHeader("Start");
            grid.addColumn(this::getFinishRep).setHeader("Finish");

        }

        private String getStartRep(WorkingWeek workingWeek) {
            if (!workingWeek.isDefault()) return workingWeek.getStart().toString();
            return "NA";
        }

        private String getFinishRep(WorkingWeek workingWeek) {
            if (!workingWeek.isDefault()) return workingWeek.getFinish().toString();
            return "NA";
        }

        private void addButtonToToolbar() {

            var addButton = new Button(new Icon(VaadinIcon.PLUS_CIRCLE));
            addButton.addClickListener(this::addWorkingWeek);
            var deleteButton = new Button(new Icon(VaadinIcon.CLOSE_CIRCLE));
            deleteButton.addClickListener(this::deleteWorkingWeek);
            toolbar.add(addButton, deleteButton);

        }

        private void customizeGrid() {
            this.grid.addItemDoubleClickListener(event -> {
                var workingWeek = event.getItem();
                if (workingWeek == null) return;
                var workingWeekForm = new WorkingWeekForm(workingWeek);
                workingWeekForm.addListener(WorkingWeekForm.SaveEvent.class, this::saveWorkingWeekListener);
                workingWeekForm.open();
            });
        }

        private void saveWorkingWeekListener(WorkingWeekForm.SaveEvent event) {
            var workingWeek = event.getWorkingWeek();
            if (workingWeek == null) return;
            grid.getListDataView().refreshItem(workingWeek);
        }

        private void addWorkingWeek(ClickEvent<Button> event) {

            var workingWeek = calendar.getWorkingWeekInstance();
            var start = this.grid.getListDataView().getItems()
                    .filter(w -> !w.isDefault()).map(WorkingWeek::getFinish)
                    .max(LocalDate::compareTo)
                    .orElse(null);
            if (start == null) start = LocalDate.now();
            else start = start.plusDays(1);
            workingWeek.setStart(start);
            workingWeek.setFinish(start);
            this.grid.getListDataView().addItem(workingWeek);

        }

        private void deleteWorkingWeek(ClickEvent<Button> event) {

            var selectedWeeks = this.grid.getSelectedItems();
            var weeksToDeletion = new ArrayList<WorkingWeek>();
            selectedWeeks.forEach(workingWeek -> {
                if (workingWeek.isDefault()) return;
                weeksToDeletion.add(workingWeek);
            });
            this.grid.getListDataView().removeItems(weeksToDeletion);

        }

    }

}
