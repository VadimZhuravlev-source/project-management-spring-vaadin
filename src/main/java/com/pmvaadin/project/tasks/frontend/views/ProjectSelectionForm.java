package com.pmvaadin.project.tasks.frontend.views;

import com.pmvaadin.common.DialogForm;
import com.pmvaadin.project.structure.ProjectHierarchicalDataProvider;
import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.pmvaadin.project.tasks.services.TreeHierarchyChangeService;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.util.HashSet;
import java.util.function.Consumer;

@SpringComponent
public class ProjectSelectionForm extends DialogForm {

    private final TreeHierarchyChangeService hierarchyService;
    private final TreeGrid<ProjectTask> treeGrid = new TreeGrid<>();
    private final ProjectHierarchicalDataProvider dataProvider;
    private Consumer<ProjectTask> selection;

    public ProjectSelectionForm(TreeHierarchyChangeService hierarchyService) {

        this.hierarchyService = hierarchyService;

        dataProvider = new ProjectHierarchicalDataProvider(hierarchyService);

        setAsSelectForm();
        getRefresh().setVisible(true);
        getRefresh().addClickListener(event -> dataProvider.refreshAll());

        customizeTreeGrid();
        customizeHeader();
        add(treeGrid);

        setWidth("70%");
        setHeight("70%");
        treeGrid.setSizeFull();
        setDraggable(true);
        setResizable(true);
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);

        getSelect().addClickListener(event -> {
            ProjectTask selectedTask = treeGrid.getSelectedItems().stream().findFirst().orElse(null);
            fireEvent(new SelectEvent(this, treeGrid.getSelectedItems()));
            selectItem(selectedTask);
        });
        addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                treeGrid.getDataProvider().refreshAll();
            }
        });
    }

    public void addSelectionListener(Consumer<ProjectTask> selection) {
        this.selection = selection;
    }

    public ProjectSelectionForm newInstance() {
        return new ProjectSelectionForm(hierarchyService);
    }

    private void customizeHeader() {

        setHeaderTitle("Choose project");
        getCrossClose().addClickListener(event -> fireEvent(new CloseEvent(this, null)));

    }

    private void customizeTreeGrid() {

        treeGrid.setDataProvider(dataProvider);
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
            var set = new HashSet<ProjectTask>(1);
            set.add(projectTask);
            fireEvent(new SelectEvent(this, set));
            selectItem(projectTask);
        });

    }

    private void selectItem(ProjectTask projectTask) {
        if (projectTask == null) return;
        if (selection != null) selection.accept(projectTask);
        close();
    }

}
