package com.pmvaadin.costs.labor.frontend.views;

import com.pmvaadin.common.DialogForm;
import com.pmvaadin.costs.labor.entities.LaborCost;
import com.pmvaadin.costs.labor.entities.LaborCostRepresentation;
import com.pmvaadin.costs.labor.entities.WorkInterval;
import com.pmvaadin.costs.labor.services.LaborCostService;
import com.pmvaadin.project.structure.StandardError;
import com.pmvaadin.project.tasks.entity.ProjectTaskRep;
import com.pmvaadin.resources.labor.entity.LaborResourceRepresentation;
import com.pmvaadin.resources.labor.frontend.elements.FilteredLaborResourceComboBox;
import com.pmvaadin.terms.calendars.common.IntervalGrid;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.LocalDateToDateConverter;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.Nonnull;

import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@SpringComponent
public class LaborCostForm extends DialogForm {

    private LaborCost laborCost;
    private final LaborCostService laborCostService;
//    private final TextField name = new TextField();
    private final FilteredLaborResourceComboBox resource;
    private final DatePicker day = new DatePicker();
    private final NumberField totalSeconds = new NumberField();
    private final TextField totalHoursRep = new TextField();
    private final Binder<LaborCost> binder = new Binder<>(LaborCost.class);
    private final Grid<ProjectTaskRep> assignedTasks = new Grid<>();
    private final VerticalLayout assignedTasksContainer = new VerticalLayout();
    private final Intervals intervals = new Intervals();
    private final VerticalLayout intervalsContainer = new VerticalLayout();
    private ProjectTaskRep draggedItem;
    private boolean isReading;

    public LaborCostForm(LaborCostService laborCostService, FilteredLaborResourceComboBox resource) {
        this.laborCostService = laborCostService;
        this.resource = resource.getInstance();
        this.resource.addValueChangeListener(this::refillAssignedTasks);
        day.addValueChangeListener(this::refillAssignedTasks);
        intervalsContainer.add(intervals);
        intervalsContainer.setSizeFull();
        intervalsContainer.setPadding(false);
        var mainLayout = new FormLayout();
        mainLayout.addFormItem(totalHoursRep, "Total hours");
        var verticalLayout = new VerticalLayout(this.resource, day, mainLayout, intervalsContainer);
        verticalLayout.setPadding(false);
        assignedTasksContainer.setPadding(false);
        assignedTasksContainer.add(assignedTasks);
        assignedTasksContainer.setSizeFull();
        assignedTasksContainer.setWidth("30%");
        var horizontalLayout = new HorizontalLayout(verticalLayout, assignedTasksContainer);
        add(horizontalLayout);
        horizontalLayout.setPadding(false);
        totalHoursRep.setReadOnly(true);
        // TODO recalculation totalSeconds after any actions of intervals changing
        totalSeconds.addValueChangeListener(event -> {
            var value = String.valueOf(totalSeconds.getValue() != null ? totalSeconds.getValue() / Calendar.SECONDS_IN_HOUR : 0);
            totalHoursRep.setValue(value);
        });
//        name.setReadOnly(true);
        customizeForm();
        customizeButton();
        customizeBinder();
        customizeAssignedTasks();
    }

    public void read(@Nonnull LaborCost laborCost) {
        this.laborCost = laborCost;
        isReading = true;
        binder.readBean(this.laborCost);
        intervals.setItems(laborCost.getIntervals());
        String title = "";
        if (laborCost instanceof LaborCostRepresentation representation
                && laborCost.getDateOfCreation() != null
                && laborCost.getId() != null) {
            title = representation.getRepresentation();
        } else {
            var laborCostName = this.laborCost.getName();
            if (laborCostName == null) laborCostName = "";
            title = "Labor cost: " + laborCostName;
        }
        setHeaderTitle(title);
        if (this.day.getValue() == null)
            this.day.setValue(LocalDate.now());
//        var chosenResource = this.resource.getValue();
//        var chosenDay = this.day.getValue();
//        refillAssignedTasks(chosenDay, chosenResource);
        isReading = false;
    }

    private void customizeAssignedTasks() {
        assignedTasks.addColumn(ProjectTaskRep::getRep).setHeader("Task");
        assignedTasks.setRowsDraggable(true);
        assignedTasks.addDragStartListener(l -> {
            draggedItem = l.getDraggedItems().get(0);//.getFirst();
            intervals.setDropMode(GridDropMode.ON_GRID);
        });

        assignedTasks.addDragEndListener(l -> {
            draggedItem = null;
            intervals.setDropMode(null);
        });
    }

    private void customizeForm() {

        setAsItemForm();
        setDraggable(true);
        setResizable(true);
        setWidth("50%");
        addClassName("labor-cost-form");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        setModal(false);
        setCloseOnEsc(false);
        getSave().setVisible(true);
        getRefresh().setVisible(true);

    }

    private void refillAssignedTasks(AbstractField.ComponentValueChangeEvent<DatePicker, LocalDate> event) {
        var chosenResource = this.resource.getValue();
        var chosenDay = event.getValue();
        refillAssignedTasks(chosenDay, chosenResource);
    }

    private void refillAssignedTasks(HasValue.ValueChangeEvent<LaborResourceRepresentation> event) {
        var chosenResource = event.getValue();
        var chosenDay = this.day.getValue();
        refillAssignedTasks(chosenDay, chosenResource);
    }

    private void refillAssignedTasks(LocalDate chosenDay, LaborResourceRepresentation chosenResource) {
        if (chosenDay == null || chosenResource == null) {
            this.assignedTasks.setItems(new ArrayList<>(0));
            if (!isReading)
                intervals.clear();
            return;
        }
        var assignedTasks = laborCostService.getAvailableTasks(chosenResource, chosenDay);
        this.assignedTasks.setItems(assignedTasks);
        if (!isReading)
            intervals.clear();
    }

    private void customizeBinder() {
        binder.forField(day).withConverter(new LocalDateToDateConverter())
                .bind(this::getDay, this::setDay);
        binder.forField(resource).bind(LaborCost::getLaborResourceRepresentation, this::writeLaborCost);
        binder.bindInstanceFields(this);
    }

    private void writeLaborCost(LaborCost laborCost, LaborResourceRepresentation laborResourceRepresentation) {
        if (laborResourceRepresentation == null || laborResourceRepresentation.getId() == null)
            throw new StandardError("Resource must not be empty.");
        laborCost.setLaborResourceRepresentation(laborResourceRepresentation);
        laborCost.setLaborResourceId(laborResourceRepresentation.getId());
    }

    private void customizeButton() {
        setAsItemForm();
        getCrossClose().addClickListener(this::closeEvent);
        getClose().addClickListener(this::closeEvent);
        getSaveAndClose().addClickListener(event -> saveEvent(event, true));
        getSave().addClickListener(event -> saveEvent(event, false));
        getRefresh().addClickListener(event -> fireEvent(new RefreshEvent(this, laborCost.getRep())));
    }

    private void saveEvent(ClickEvent<Button> event, boolean close) {
        try {
            intervals.validate();
            binder.writeBean(this.laborCost);
            this.laborCost.setIntervals(intervals.getItems());
        } catch (ValidationException | StandardError error) {
            var confDialog = new ConfirmDialog();
            confDialog.setText(error.getMessage());
            confDialog.open();
            return;
        }
        if (close) {
            fireEvent(new SaveAndCloseEvent(this, this.laborCost));
        } else {
            fireEvent(new SaveEvent(this, this.laborCost));
        }
    }

    private void closeEvent(ClickEvent<Button> event) {
        fireEvent(new CloseEvent(this, this.laborCost));
        this.close();
    }

    private Date getDay(LaborCost laborCost) {

        var day = laborCost.getDay();
        return convertLocalDateTimeToDate(day);

    }

    private void setDay(LaborCost laborCost, Date date) {

        var newDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        laborCost.setDay(newDate);

    }

    private Date convertLocalDateTimeToDate(LocalDate date) {
        if (date == null) return new Date();
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private class Intervals extends IntervalGrid<WorkInterval> {

        Intervals() {
            super(getValidationMessageFrom(), getValidationMessageTo());
            var taskNameCol = grid.addColumn(WorkInterval::getTaskName).setHeader("Task");
            super.addColumns();
            setDeletable(true);
            grid.addThemeVariants(GridVariant.LUMO_COMPACT);
            this.addToolbarSmallThemeVariant();

            grid.addDropListener(l -> {
                if (draggedItem == null)
                    return;
                var gridItem = onAddInterval();
                if (gridItem == null)
                    return;
                grid.getListDataView().addItem(gridItem);
            });
            this.afterDeletionListener(this::recalculateTotalSeconds);
            grid.getEditor().addCloseListener(event ->
                recalculateTotalSeconds()
            );
        }

        @Override
        public void setItems(List<WorkInterval> items) {
            super.setItems(items);
            recalculateTotalSeconds();
        }

        private void validate() {
            // TODO validation
        }

        private void recalculateTotalSeconds() {
            var totalSum = (double) grid.getListDataView().getItems()
                    .map(wi -> {
                        wi.fillDuration();
                        return wi.getDuration();
                    })
                    .reduce(Integer::sum)
                    .orElse(0);
            totalSeconds.setValue(totalSum);
        }

        private static String getValidationMessageFrom() {
            return "The start of a period must be later then the end of the previous period.";
        }

        private static String getValidationMessageTo() {
            return "The end of a shaft must be later then the period.";
        }

        public void clear() {
            grid.getListDataView().removeItems(grid.getListDataView().getItems().toList());
        }

        public void setDropMode(GridDropMode dropMode) {
            grid.setDropMode(dropMode);
        }

        private WorkInterval onAddInterval() {

            if (laborCost == null || draggedItem == null)
                return null;
            var newInterval = laborCost.getWorkIntervalInstance();
            var previousTimeTo = this.grid.getListDataView().getItems().map(WorkInterval::getTo)
                    .max(Comparator.naturalOrder()).orElse(LocalTime.MIN);
            //TODO interval validation
            newInterval.setFrom(previousTimeTo);
            newInterval.setTo(previousTimeTo);
            newInterval.setTaskId(draggedItem.getId());
            newInterval.setTaskName(draggedItem.getRep());
            return newInterval;

        }

    }

}
