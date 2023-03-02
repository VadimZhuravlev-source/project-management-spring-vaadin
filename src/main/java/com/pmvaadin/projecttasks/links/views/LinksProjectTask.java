package com.pmvaadin.projecttasks.links.views;

import com.pmvaadin.commonobjects.ObjectGrid;
import com.pmvaadin.commonobjects.SelectableTextField;
import com.pmvaadin.projectstructure.NotificationDialogs;
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
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringComponent
public class LinksProjectTask extends ObjectGrid<Link> {

    private final LinkService linkService;
    private final ProjectSelectionForm projectSelectionForm;
    private ProjectTask projectTask;

    LinksProjectTask(LinkService linkService, ProjectSelectionForm projectSelectionForm) {
        super();
        this.linkService = linkService;
        this.projectSelectionForm = projectSelectionForm;
        customizeLinks();
    }

    @Override
    public void setItems(List<Link> links) {
        clear();
        super.setItems(links);
    }

    public void setProjectTask(ProjectTask projectTask) {
        this.projectTask = projectTask;
        List<Link> links = (List<Link>) (linkService.getLinksWithProjectTaskRepresentation(projectTask));
        setItems(links);
    }

    public boolean validate() {

        LinkValidation linkValidation = new LinkValidationImpl();
        LinkValidationMessage linkValidationMessage = linkValidation.validate(getGrid().getListDataView().getItems().toList());

//        Map<Integer, Boolean> mapIdentity = new HashMap<>();
//        boolean isOk = true;
//        String message = "";
//        Link tableRow = null;
//
//        for (Link link: getGrid().getListDataView().getItems().toList()) {
//            if (link.getLinkedProjectTaskId() == null) {
//                isOk = false;
//                tableRow = link;
//                message = getTextErrorNotFilledProjectTask();
//                break;
//            }
//            if (link.getLinkType() == null) {
//                isOk = false;
//                tableRow = link;
//                message = getTextErrorNotFilledLinkType();
//                break;
//            }
//            if (mapIdentity.getOrDefault(link.getLinkedProjectTaskId(), false)) {
//                isOk = false;
//                tableRow = link;
//                message = getTextErrorDuplicatedTasks();
//                break;
//            }
//            mapIdentity.put(link.getLinkedProjectTaskId(), true);
//        }

        if (!linkValidationMessage.isOk()) {
            NotificationDialogs.notifyValidationErrors(linkValidationMessage.getMessage());
            getGrid().deselectAll();
            getGrid().select(linkValidationMessage.getTableRow());
        }

        return linkValidationMessage.isOk();

    }

    private String getTextErrorNotFilledProjectTask() {
        return "In the predecessors, the project task is not filled";
    }

    private String getTextErrorNotFilledLinkType() {
        return "In the predecessors, the link type is not filled";
    }

    private void customizeLinks() {

        Grid.Column<Link> linkedProjectTaskIdColumn = addColumn(Link::getRepresentation).
                setHeader("Project task");
        Grid.Column<Link> linkTypeColumn = addColumn(Link::getLinkType).setHeader("Link type");

        setDeletable(true);
        setInstantiatable(() -> {
            Link newLink = new LinkImpl();
            newLink.setLinkType(LinkType.STARTFINISH);
            return newLink;
        });
        setCopyable(link -> {
            Link newLink = new LinkImpl();
            newLink.setProjectTaskId(link.getProjectTaskId());
            newLink.setLinkType(link.getLinkType());
            //newLink.setLinkedProjectTaskId(link.getLinkedProjectTaskId());
            return newLink;
        });

        setInlineEditor((linkBinder, editor) -> {

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
                            ptField.getValue().setRepresentation(selectedProjectTask.getLinkPresentation());
                            ptField.refreshTextValue();
                        });
                projectSelectionForm.open();
            });
            ptField.setWidthFull();
            addCloseHandler(ptField, editor);
            linkBinder.forField(ptField)
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
            linkBinder.forField(linkTypeField)
                    .asRequired("Field link type must not be empty")
                    .bind(Link::getLinkType, Link::setLinkType);
            linkTypeColumn.setEditorComponent(linkTypeField);

        });

    }

    private boolean isAddable(ProjectTask projectTask) {

        if (this.projectTask.equals(projectTask)) {
            NotificationDialogs.notifyValidationErrors(getTextErrorEqualsThis());
            return false;
        }

        boolean isContainedAlready = getGrid().getListDataView().getItems().anyMatch(link ->
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
