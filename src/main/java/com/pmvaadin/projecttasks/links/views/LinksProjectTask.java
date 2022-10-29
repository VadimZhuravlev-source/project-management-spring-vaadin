package com.pmvaadin.projecttasks.links.views;

import com.pmvaadin.commonobjects.ObjectGrid;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.links.entities.LinkImpl;
import com.pmvaadin.projecttasks.links.entities.LinkType;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.services.LinkService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
public class LinksProjectTask extends ObjectGrid<Link> {

    private final LinkService linkService;
    private ProjectTask projectTask;

    LinksProjectTask(LinkService linkService) {
        super();
        this.linkService = linkService;
        customizeLinks();
    }

    public void setProjectTask(ProjectTask projectTask) {
        this.projectTask = projectTask;
        setItems(linkService.getLinks(projectTask));
    }

    public Changes<Link> getChanges() {
        return super.getChanges();
    }

    private void customizeLinks() {

        Grid.Column<Link> linkedProjectTaskIdColumn = addColumn(Link::getLinkedProjectTaskId).
                setHeader("Linked project task id");
        Grid.Column<Link> linkTypeColumn = addColumn(Link::getLinkType).setHeader("Link type");

        setDeletable(true);
        setInstantiatable(LinkImpl::new);
        setCopyable(link -> {
            Link newLink = new LinkImpl();
            newLink.setProjectTaskId(link.getProjectTaskId());
            newLink.setLinkType(link.getLinkType());
            newLink.setLinkedProjectTaskId(link.getLinkedProjectTaskId());
            return newLink;
        });

        setInlineEditor((linkBinder, editor) -> {

            TextField firstNameField = new TextField();
            firstNameField.setWidthFull();
            addCloseHandler(firstNameField, editor);
            linkBinder.forField(firstNameField)
                    .asRequired("First name must not be empty")
                    .bind(link -> {
                                if (link.getLinkedProjectTaskId() == null) {
                                    return "null";
                                }
                                return link.getLinkedProjectTaskId().toString();
                            },
                            (link, s) -> {
                        link.setLinkedProjectTaskId(Integer.valueOf(s));
                    });
            linkedProjectTaskIdColumn.setEditorComponent(firstNameField);

            Select<LinkType> linkTypeField = new Select<>();
            linkTypeField.setItems(LinkType.values());
            linkTypeField.setWidthFull();
            addCloseHandler(linkTypeField, editor);
            linkBinder.forField(linkTypeField)
                    .asRequired("Last name must not be empty")
                    .bind(link -> link.getLinkType(), Link::setLinkType);
            linkTypeColumn.setEditorComponent(linkTypeField);

        });

    }

    private static void addCloseHandler(Component textField,
                                        Editor<Link> editor) {
        textField.getElement().addEventListener("keydown", e -> editor.cancel())
                .setFilter("event.code === 'Escape'");
        textField.getElement().addEventListener("keydown", e -> {
            editor.save();
            editor.closeEditor();
        }).setFilter("event.code === 'Enter'");
    }

}
