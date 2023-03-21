package com.pmvaadin.projecttasks.views;

import com.pmvaadin.projectstructure.ProjectHierarchicalDataProvider;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.services.TreeHierarchyChangeService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.util.function.Consumer;

@SpringComponent
public class ProjectSelectionForm extends Dialog {

    private final TreeHierarchyChangeService hierarchyService;
    private final TreeGrid<ProjectTask> treeGrid = new TreeGrid<>();
    private final ProjectHierarchicalDataProvider dataProvider;
    private Consumer<ProjectTask> selection;

    public ProjectSelectionForm(TreeHierarchyChangeService hierarchyService) {

        this.hierarchyService = hierarchyService;

        dataProvider = new ProjectHierarchicalDataProvider(hierarchyService);

        Button selectionAction = new Button("Select");
        selectionAction.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(selectionAction);
        //selectionAction.getStyle().set("margin-right", "auto");

        Button refreshButton = new Button(new Icon("lumo", "reload"),
                (e) -> {
            dataProvider.setFirstInitialization(true);
            treeGrid.getDataProvider().refreshAll();
                });
        getFooter().add(refreshButton);
        refreshButton.getStyle().set("margin-right", "auto");

        customizeTreeGrid();
        customizeHeader();
        add(treeGrid);
        treeGrid.addExpandListener(event -> {
            for (ProjectTask projectTask: event.getItems()) {
                ProjectTask parent = treeGrid.getDataCommunicator().getParentItem(projectTask);
            }
        });
        treeGrid.addCollapseListener(event -> {
            for (ProjectTask projectTask: event.getItems()) {
                ProjectTask parent = treeGrid.getDataCommunicator().getParentItem(projectTask);
            }
        });

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
            if (event.isOpened()) {
                dataProvider.setFirstInitialization(true);
                treeGrid.getDataProvider().refreshAll();
            }
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

        treeGrid.setDataProvider(dataProvider);
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

}
