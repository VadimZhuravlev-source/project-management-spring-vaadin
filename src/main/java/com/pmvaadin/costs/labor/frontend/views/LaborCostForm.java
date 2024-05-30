package com.pmvaadin.costs.labor.frontend.views;

import com.pmvaadin.common.DialogForm;
import com.pmvaadin.costs.labor.entities.LaborCost;
import com.pmvaadin.costs.labor.entities.WorkInterval;
import com.pmvaadin.costs.labor.services.LaborCostService;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projecttasks.entity.ProjectTaskRep;
import com.pmvaadin.resources.labor.entity.LaborResourceRepresentation;
import com.pmvaadin.resources.labor.frontend.elements.FilteredLaborResourceComboBox;
import com.pmvaadin.terms.calendars.common.IntervalGrid;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

@SpringComponent
public class LaborCostForm extends DialogForm {

    private LaborCost laborCost;
    private final LaborCostService laborCostService;
    private final TextField name = new TextField("Name");
    private final FilteredLaborResourceComboBox resource;
    private final DatePicker day = new DatePicker("Day");
    private final Binder<LaborCost> binder = new Binder<>(LaborCost.class);
    private final Grid<ProjectTaskRep> assignedTasks = new Grid<>();
    private final VerticalLayout assignedTasksContainer = new VerticalLayout();
    private final Intervals intervals = new Intervals();
    private final VerticalLayout intervalsContainer = new VerticalLayout();
    private ProjectTaskRep draggedItem;

    public LaborCostForm(LaborCostService laborCostService, FilteredLaborResourceComboBox resource) {
        this.laborCostService = laborCostService;
        this.resource = resource.getInstance();
        this.resource.addValueChangeListener(this::refillAssignedTasks);
        day.addValueChangeListener(this::refillAssignedTasks);
        assignedTasks.addColumn(ProjectTaskRep::getRep).setHeader("Task");
//        assignedTasks.setRowsDraggable(true);
        assignedTasks.addDragStartListener(l -> draggedItem = l.getDraggedItems().get(0));
        assignedTasks.addDragEndListener(l -> draggedItem = null);
        intervalsContainer.add(intervals);
        assignedTasksContainer.add(assignedTasks);
        assignedTasksContainer.setSizeFull();
        var verticalLayout = new VerticalLayout(name, this.resource, day, intervalsContainer);
        verticalLayout.setSizeFull();
        var horizontalLayout = new HorizontalLayout(verticalLayout, assignedTasksContainer);
        horizontalLayout.setSizeFull();
        add(horizontalLayout);
        customizeButton();
        customizeBinder();
    }

    public void read(@Nonnull LaborCost laborCost) {
        this.laborCost = laborCost;
        binder.readBean(this.laborCost);
        intervals.setItems(laborCost.getIntervals());
        var laborCostName = this.laborCost.getName();
        if (laborCostName == null) laborCostName = "";
        var title = "Labor cost: ";
        setHeaderTitle(title + laborCostName);
        if (this.day.getValue() == null)
            this.day.setValue(LocalDate.now());
        var chosenResource = this.resource.getValue();
        var chosenDay = this.day.getValue();
        refillAssignedTasks(chosenDay, chosenResource);
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
            return;
        }
        var assignedTasks = laborCostService.getAvailableTasks(chosenResource, chosenDay);
        this.assignedTasks.setItems(assignedTasks);
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
        getSaveAndClose().addClickListener(event -> {
            saveEvent(event);
            close();
        });
        getSave().addClickListener(this::saveEvent);
    }

    private void saveEvent(ClickEvent<Button> event) {
        try {
            binder.writeBean(this.laborCost);
        } catch (ValidationException | StandardError error) {
            var confDialog = new ConfirmDialog();
            confDialog.setText(error.getMessage());
            confDialog.open();
            return;
        }
        fireEvent(new SaveEvent(this, this.laborCost));
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

//        {
//            var taskNameCol = grid.addColumn(WorkInterval::getTaskName).setHeader("Task");
//        }

        Intervals() {
            super(getValidationMessageFrom(), getValidationMessageTo());
//            setInstantiatable(this::onAddInterval);
            setDeletable(true);
            grid.addThemeVariants(GridVariant.LUMO_COMPACT);
            this.addToolbarSmallThemeVariant();
            var taskNameCol = grid.addColumn(WorkInterval::getTaskName).setHeader("Task");
//            grid.getColumns().set(0, taskNameCol);
            grid.setDropMode(GridDropMode.ON_GRID);
            grid.setRowsDraggable(true);
            grid.addDropListener(l -> {
                if (draggedItem == null)
                    return;
                var gridItem = onAddInterval();
                if (gridItem == null)
                    return;
                grid.getListDataView().addItem(gridItem);
            });

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
