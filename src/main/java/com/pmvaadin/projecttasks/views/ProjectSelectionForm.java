package com.pmvaadin.projecttasks.views;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.services.ProjectTaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.util.function.Consumer;
import java.util.stream.Stream;

@SpringComponent
public class ProjectSelectionForm extends Dialog {

    private final ProjectTaskService projectTaskService;
    private final TreeGrid<ProjectTask> treeGrid = new TreeGrid<>();

    private Consumer<ProjectTask> selection;

    public ProjectSelectionForm(ProjectTaskService projectTaskService) {
        super();
        this.projectTaskService = projectTaskService;

        Button selectionAction = new Button("Select");
        selectionAction.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(selectionAction);
        selectionAction.getStyle().set("margin-right", "auto");
        customizeTreeGrid();
        customizeHeader();
        add(treeGrid);

        setWidth("70%");
        setHeight("70%");
        treeGrid.setSizeFull();
        setDraggable(true);
        setResizable(true);
        addClassName("project-tasks-selection-form");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);

        selectionAction.addClickListener(event -> {
            ProjectTask selectedTask = treeGrid.getSelectedItems().stream().findFirst().orElse(null);
            selectItem(selectedTask);
        });
        addOpenedChangeListener(event -> {
            if (event.isOpened()) treeGrid.getDataProvider().refreshAll();
        });
    }

    public void addSelectionListener(Consumer<ProjectTask> selection) {
        this.selection = selection;
    }

    private void customizeHeader() {

        setHeaderTitle("Project task selection");
        Button closeButton = new Button(new Icon("lumo", "cross"),
                (e) -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getHeader().add(closeButton);

    }

    private void customizeTreeGrid() {

        treeGrid.setDataProvider(getDataProvider());
        treeGrid.addClassNames("project-tasks-selection-grid");
        treeGrid.setSizeFull();
        treeGrid.setColumnReorderingAllowed(true);
        treeGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        treeGrid.addHierarchyColumn(ProjectTask::getName).setHeader(ProjectTask.getHeaderName()).setFrozen(true)
                .setResizable(true).setSortable(false).setWidth("25em");
        treeGrid.addColumn(ProjectTask::getWbs).setHeader(ProjectTask.getHeaderWbs()).setResizable(true).setWidth("5em");
        treeGrid.addColumn(ProjectTask::getStartDate).setHeader(ProjectTask.getHeaderStartDate()).setResizable(true).setAutoWidth(true);
        treeGrid.addColumn(ProjectTask::getFinishDate).setHeader(ProjectTask.getHeaderFinishDate()).setResizable(true).setAutoWidth(true);
        treeGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        treeGrid.addItemDoubleClickListener(event -> {
            if (event == null) return;
            ProjectTask projectTask = event.getItem();
            selectItem(projectTask);
        });

    }

    private void selectItem(ProjectTask projectTask) {
        if (projectTask == null) return;
        if (selection != null) selection.accept(projectTask);
        close();
    }

    private HierarchicalDataProvider<ProjectTask, Void> getDataProvider() {

        return new AbstractBackEndHierarchicalDataProvider<>() {

            @Override
            public int getChildCount(HierarchicalQuery<ProjectTask, Void> query) {
                return projectTaskService.getChildrenCount(query.getParent());
            }

            @Override
            public boolean hasChildren(ProjectTask item) {
                return projectTaskService.hasChildren(item);
            }

            @Override
            protected Stream<ProjectTask> fetchChildrenFromBackEnd(HierarchicalQuery<ProjectTask, Void> query) {
                return projectTaskService.fetchChildren(query.getParent()).stream();
            }
        };

    }

}
