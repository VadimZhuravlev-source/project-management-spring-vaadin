package com.pmvaadin.projecttasks.links.views;

import com.pmvaadin.commonobjects.ObjectGrid;
import com.pmvaadin.commonobjects.SelectableTextField;
import com.pmvaadin.projectstructure.NotificationDialogs;
import com.pmvaadin.projecttasks.commonobjects.BigDecimalToDoubleConverter;
import com.pmvaadin.projecttasks.data.ProjectTaskData;
import com.pmvaadin.projecttasks.links.LinkValidation;
import com.pmvaadin.projecttasks.links.LinkValidationImpl;
import com.pmvaadin.projecttasks.links.LinkValidationMessage;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.links.entities.LinkType;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.services.LinkService;
import com.pmvaadin.projecttasks.views.ProjectSelectionForm;
import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.pmvaadin.terms.timeunit.services.TimeUnitService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

@SpringComponent
public class LinksProjectTask extends ObjectGrid<Link> {

    private final LinkService linkService;
    private final ProjectSelectionForm projectSelectionForm;
    private final TimeUnitService timeUnitService;
    private ProjectTaskData projectTaskDate;

    LinksProjectTask(LinkService linkService, ProjectSelectionForm projectSelectionForm,
                     TimeUnitService timeUnitService) {
        super();
        this.linkService = linkService;
        this.projectSelectionForm = projectSelectionForm;
        this.timeUnitService = timeUnitService;
        customizeLinks();

    }

    public LinksProjectTask newInstance() {
        return new LinksProjectTask(linkService, projectSelectionForm.newInstance(), timeUnitService);
    }

    public void setProjectTask(ProjectTaskData projectTaskDate) {
        this.projectTaskDate = projectTaskDate;
        setItems(projectTaskDate.getLinks());
    }

    public List<Link> getLinks() {
        return grid.getListDataView().getItems().toList();
    }

    public boolean validate() {

        LinkValidation linkValidation = new LinkValidationImpl();
        LinkValidationMessage linkValidationMessage = linkValidation.validate(grid.getListDataView().getItems().toList());

        if (!linkValidationMessage.isOk()) {
            NotificationDialogs.notifyValidationErrors(linkValidationMessage.getMessage());
            grid.deselectAll();
            grid.select(linkValidationMessage.getTableRow());
        }

        return linkValidationMessage.isOk();

    }

    public boolean isEditing() {
        return editor.isOpen();
    }

    public void endEditing() {
        editor.closeEditor();
    }

    private void customizeLinks() {

        setDeletable(true);
        setInstantiatable(this::setInstantiatable);
        setCopyable(this::setCopyable);

        customizeBinder();

    }

    private Link setInstantiatable() {
        Link newLink = projectTaskDate.getLinkSample().getInstance();
        newLink.setTimeUnit(projectTaskDate.getTimeUnit());
        return newLink;
    }

    private Link setCopyable(Link link) {
        Link copy = projectTaskDate.getLinkSample().copy(link);
        Integer maxSort = grid.getListDataView().getItems().map(Link::getSort).max(Integer::compareTo).orElse(0);
        copy.setSort(++maxSort);
        return copy;
    }

    private void customizeBinder() {

        Grid.Column<Link> linkedProjectTaskIdColumn = addColumn(Link::getRepresentation).
                setHeader("Project task");
        Grid.Column<Link> linkTypeColumn = addColumn(Link::getLinkType).setHeader("Link type");
        Grid.Column<Link> lagColumn = addColumn(Link::getLagRepresentation).
                setHeader("Lag");
        Grid.Column<Link> timeUnitColumn = addColumn(Link::getTimeUnit).setHeader("Time unit");

        SelectableTextField<Link> ptField = new SelectableTextField<>();
        ptField.setMapValueToText(Link::getRepresentation);
        ptField.setReadOnly(true);
        ptField.setSelectable(true);
        ptField.addSelectionListener(event -> {
            projectSelectionForm.addSelectionListener(
                    selectedProjectTask -> {

                        if (!isAddable(selectedProjectTask)) return;

                        ptField.getValue().setLinkedProjectTask(selectedProjectTask);
                        ptField.getValue().setLinkedProjectTaskId(selectedProjectTask.getId());
                        ptField.getValue().setRepresentation(selectedProjectTask.getRepresentation());
                        ptField.refreshTextValue();
                    });
            projectSelectionForm.open();
        });
        ptField.setWidthFull();
        //ptField.setAutofocus(false);
        addCloseHandler(ptField, editor);
        binder.forField(ptField)
                .asRequired(getTextErrorEmptyProjectTask())
                .bind(link -> {
                            ptField.setValue(link);
                            return link;
                        },
                        (link, pt) -> {});
        linkedProjectTaskIdColumn.setEditorComponent(ptField);

        Select<LinkType> linkTypeField = new Select<>();
        linkTypeField.setItems(LinkType.values());
        linkTypeField.setWidthFull();
        //linkTypeField.setAutofocus(false);
        addCloseHandler(linkTypeField, editor);
        binder.forField(linkTypeField)
                .asRequired("The link type has not to be empty")
                .bind(Link::getLinkType, Link::setLinkType);
        linkTypeColumn.setEditorComponent(linkTypeField);

        NumberField lagRepresentation = new NumberField();
        lagRepresentation.setWidthFull();
        //lagRepresentation.setAutofocus(false);
        lagRepresentation.setStepButtonsVisible(true);
        lagRepresentation.setStep(1);
        addCloseHandler(lagRepresentation, editor);
        binder.forField(lagRepresentation).withConverter(new BigDecimalToDoubleConverter(lagRepresentation))
                .bind(Link::getLagRepresentation, this::setLagRepresentation);
        lagColumn.setEditorComponent(lagRepresentation);

        ComboBox<TimeUnit> timeUnitComboBox = new ComboBox<>();
        timeUnitComboBox.setItems(this::getPageTimeUnit, this::getCountItemsInPageByName);
        timeUnitComboBox.setWidthFull();
        //timeUnitComboBox.setAutofocus(false);
        addCloseHandler(timeUnitComboBox, editor);
        binder.forField(timeUnitComboBox)
                .bind(Link::getTimeUnit, this::setTimeUnit);
        timeUnitColumn.setEditorComponent(timeUnitComboBox);

    }

    private void setTimeUnit(Link link, TimeUnit timeUnit) {
        TimeUnit currentTimeUnit = timeUnit;
        if (timeUnit == null) currentTimeUnit = projectTaskDate.getTimeUnit();
        BigDecimal lagRepresentation = currentTimeUnit.getDurationRepresentation(link.getLag());
        link.setLagRepresentation(lagRepresentation);
        link.setTimeUnit(timeUnit);
        //binder.refreshFields();
    }

    private Stream<TimeUnit> getPageTimeUnit(Query<TimeUnit, String> query) {
        return timeUnitService.getPageByName(query).stream();
    }

    private int getCountItemsInPageByName(Query<TimeUnit, String> query) {
        return timeUnitService.getCountPageItemsByName(query);
    }

    private void setLagRepresentation(Link link, BigDecimal value) {

        TimeUnit timeUnit = link.getTimeUnit();
        if (timeUnit == null) timeUnit = projectTaskDate.getTimeUnit();

        long lag = timeUnit.getDuration(value);
        link.setLag(lag);
        link.setLagRepresentation(value);
        //binder.refreshFields();

    }

    private boolean isAddable(ProjectTask projectTask) {

        if (this.projectTaskDate.getProjectTask().equals(projectTask)) {
            NotificationDialogs.notifyValidationErrors(getTextErrorEqualsThis());
            return false;
        }

        boolean isContainedAlready = grid.getListDataView().getItems().anyMatch(link ->
        {
            if (link.getLinkedProjectTask() != null) {
                return link.getLinkedProjectTask().equals(projectTask);
            }
            return false;
        });

        if (isContainedAlready) {
            NotificationDialogs.notifyValidationErrors(getTextErrorDuplicatedTasks());
            return false;
        }

        // TODO check circle dependency

        return true;

    }

    private String getTextErrorDuplicatedTasks() {
        return """
                An error occurred while establishing a connection between tasks.

                A double link from a predecessor task to a single successor task is not allowed.""";
    }

    private String getTextErrorEmptyProjectTask() {
        return "Field Project task must not be empty.";
    }

    private String getTextErrorEqualsThis() {
        return "Cannot link to current task";
    }

}
