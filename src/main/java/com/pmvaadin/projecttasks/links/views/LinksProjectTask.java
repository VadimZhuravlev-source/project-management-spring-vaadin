package com.pmvaadin.projecttasks.links.views;

import com.pmvaadin.commonobjects.ObjectGrid;
import com.pmvaadin.commonobjects.SelectableTextField;
import com.pmvaadin.projectstructure.NotificationDialogs;
import com.pmvaadin.projecttasks.data.ProjectTaskData;
import com.pmvaadin.projecttasks.links.LinkValidation;
import com.pmvaadin.projecttasks.links.LinkValidationImpl;
import com.pmvaadin.projecttasks.links.LinkValidationMessage;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.links.entities.LinkImpl;
import com.pmvaadin.projecttasks.links.entities.LinkType;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.services.LinkService;
import com.pmvaadin.projecttasks.views.ProjectSelectionForm;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.util.List;

@SpringComponent
public class LinksProjectTask extends ObjectGrid<Link> {

    private final LinkService linkService;
    private final ProjectSelectionForm projectSelectionForm;
    private ProjectTaskData projectTaskDate;

    private final Link example = new LinkImpl();
    private final Binder<Link> binder = new Binder<>();
    private final Editor<Link> editor;

    LinksProjectTask(LinkService linkService, ProjectSelectionForm projectSelectionForm) {
        super();
        this.linkService = linkService;
        this.projectSelectionForm = projectSelectionForm;
        customizeLinks();
        editor = grid.getEditor();
        editor.setBinder(binder);
    }

    public LinksProjectTask newInstance() {
        return new LinksProjectTask(linkService, projectSelectionForm.newInstance());
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

    private void customizeLinks() {

        setDeletable(true);
        setInstantiatable(example::getInstance);
        setCopyable(example::copy);

        customizeBinder();

    }

    private void customizeBinder() {

        Grid.Column<Link> linkedProjectTaskIdColumn = addColumn(Link::getRepresentation).
                setHeader("Project task");
        Grid.Column<Link> linkTypeColumn = addColumn(Link::getLinkType).setHeader("Link type");

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
        addCloseHandler(linkTypeField, editor);
        binder.forField(linkTypeField)
                .asRequired("The link type has not to be empty")
                .bind(Link::getLinkType, Link::setLinkType);
        linkTypeColumn.setEditorComponent(linkTypeField);

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

    private static void addCloseHandler(Component textField,
                                        Editor<? extends Link> editor) {
        textField.getElement().addEventListener("keydown", e -> editor.cancel())
                .setFilter("event.code === 'Escape'");
        textField.getElement().addEventListener("keydown", e -> {
            editor.save();
            editor.closeEditor();
        }).setFilter("event.code === 'Enter'");
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
