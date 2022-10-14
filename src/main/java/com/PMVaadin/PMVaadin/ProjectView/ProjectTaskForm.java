package com.PMVaadin.PMVaadin.ProjectView;

import com.PMVaadin.PMVaadin.Entities.Links.Link;
import com.PMVaadin.PMVaadin.Entities.Links.LinkImpl;
import com.PMVaadin.PMVaadin.Entities.Links.LinkType;
import com.PMVaadin.PMVaadin.Entities.ProjectTask.ProjectTask;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

public class ProjectTaskForm extends FormLayout {

    private ProjectTask projectTask;

    private DatePicker dateOfCreation = new DatePicker(ProjectTask.getHeaderDateOfCreation());
    private DatePicker updateDate = new DatePicker(ProjectTask.getHeaderUpdateDate());
    private TextField name = new TextField(ProjectTask.getHeaderName());
    private TextField wbs = new TextField(ProjectTask.getHeaderWbs());
    private TextField version = new TextField(ProjectTask.getHeaderVersion());
    private DatePicker startDate = new DatePicker(ProjectTask.getHeaderStartDate());
    private DatePicker finishDate = new DatePicker(ProjectTask.getHeaderFinishDate());
    private Binder<ProjectTask> binder = new BeanValidationBinder<>(ProjectTask.class);

    private VerticalLayout content;
    private Tab mainData;
    private VerticalLayout mainDataLayout = new VerticalLayout();
    private Tab links;
    private VerticalLayout linksLayout = new VerticalLayout();

    private Button save = new Button("Save");
    private Button delete = new Button("Delete");
    private Button close = new Button("Cancel");

    public ProjectTaskForm() {
        addClassName("project-task-form");
        wbs.setEnabled(false);
        dateOfCreation.setEnabled(false);
        updateDate.setEnabled(false);
        binder.bindInstanceFields(this);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();

        mainData = new Tab("Main data");

        mainDataLayout.add(dateOfCreation,
                updateDate,
                version,
                name,
                wbs,
                startDate,
                finishDate);

        content = mainDataLayout;

        links = new Tab("Links");

        customizeLinksTab();

        Tabs tabs = new Tabs(mainData, links);

        tabs.addSelectedChangeListener(selectedChangeEvent -> {
            content.removeAll();
            if (selectedChangeEvent.getSelectedTab() == mainData) {
                content.add(mainDataLayout);
            } else if (selectedChangeEvent.getSelectedTab() == links) {
                content.add(linksLayout);
            }
        });

        mainLayout.add(tabs, content, createButtonsLayout());

        add(mainLayout);
        name.setAutofocus(true);
    }

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        close.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, projectTask)));
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));


        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

        return new HorizontalLayout(save, delete, close);
    }

    public void setProjectTask(ProjectTask projectTask) {
        this.projectTask = projectTask;
        binder.readBean(projectTask);
        name.focus();
    }

    private void customizeLinksTab() {

        ObjectGrid<Link> linksGrid = new ObjectGrid<>();
        linksLayout.add(linksGrid);

        Grid.Column<Link> linkedProjectTaskIdColumn = linksGrid.addColumn(Link::getLinkedProjectTaskId).
                setHeader("Linked project task id");
        Grid.Column<Link> linkTypeColumn = linksGrid.addColumn(Link::getLinkType).setHeader("Link type");

        linksGrid.setInstantiatable(LinkImpl::new);
        linksGrid.setCopyable(link -> {
            Link newLink = new LinkImpl();
            newLink.setProjectTaskId(link.getProjectTaskId());
            newLink.setLinkType(link.getLinkType());
            newLink.setLinkedProjectTaskId(link.getLinkedProjectTaskId());
            return newLink;
        });

        linksGrid.setInlineEditor((binder1, editor) -> {

            TextField firstNameField = new TextField();
            firstNameField.setWidthFull();
            addCloseHandler(firstNameField, editor);
            binder1.forField(firstNameField)
                    .asRequired("First name must not be empty")
                    .bind(link -> {
                                if (link.getLinkedProjectTaskId() == null) {
                                    return "null";
                                }
                                return link.getLinkedProjectTaskId().toString();
                            },
                            (link, s) -> {link.setLinkedProjectTaskId(Integer.getInteger(s));});
            linkedProjectTaskIdColumn.setEditorComponent(firstNameField);

            Select<LinkType> linkTypeField = new Select<>();
            linkTypeField.setItems(LinkType.values());
            linkTypeField.setWidthFull();
            addCloseHandler(linkTypeField, editor);
            binder1.forField(linkTypeField).asRequired("Last name must not be empty")
                    .bind(link -> link.getLinkType(), Link::setLinkType);
            linkTypeColumn.setEditorComponent(linkTypeField);

        });

    }

    private static void addCloseHandler(Component textField,
                                        Editor<Link> editor) {
        textField.getElement().addEventListener("keydown", e -> editor.cancel())
                .setFilter("event.code === 'Escape'");
    }

    private void validateAndSave() {
        try {
            binder.writeBean(projectTask);
            fireEvent(new SaveEvent(this, projectTask));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
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

    public static class DeleteEvent extends ProjectTaskFormEvent {
        DeleteEvent(ProjectTaskForm source, ProjectTask projectTask) {
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

