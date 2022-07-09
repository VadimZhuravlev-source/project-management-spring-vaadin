package com.PMVaadin.PMVaadin.ProjectView;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;
import com.PMVaadin.PMVaadin.Entities.ProjectTaskOrderedHierarchy;
import com.PMVaadin.PMVaadin.MainLayout;
import com.PMVaadin.PMVaadin.Services.ProjectTaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import javax.annotation.security.PermitAll;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value="", layout = MainLayout.class)
@PageTitle("Projects | PM")
@PermitAll
public class ProjectTasksView extends VerticalLayout {

    private ProjectTaskService projectTaskService;
    private final TreeData<ProjectTask> treeData = new TreeData<>();

    private TreeGrid<ProjectTask> treeGrid = new TreeGrid<>();
    private TextField filterText = new TextField();


    public ProjectTasksView(ProjectTaskService projectTaskService) {

        this.projectTaskService = projectTaskService;
        addClassName("project-tasks-view");
        setSizeFull();
        configureGrid();

        populateTreeData();

        add(getToolbar(), treeGrid);

    }

    private void configureGrid() {

        treeGrid.addClassNames("project-tasks-grid");
        treeGrid.setSizeFull();
        treeGrid.addHierarchyColumn(ProjectTask::getName).setHeader("Name");
        treeGrid.addColumn(ProjectTask::getWbs).setHeader("wbs");
        treeGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData));

    }

    private void populateTreeData() {

        treeData.clear();
        List<ProjectTask> projectTasks = projectTaskService.getProjectTasks();
        Map<Integer, ProjectTask> map = projectTasks.stream().collect(
                Collectors.toMap(ProjectTaskOrderedHierarchy::getId, projectTask -> projectTask)
        );

        for (ProjectTask projectTask: projectTasks) {
            if (projectTask.getParentId() == null) {
                treeData.addRootItems(projectTask);
                continue;
            }
            treeData.addItem(map.get(projectTask.getParentId()), projectTask);
        }

    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        //filterText.addValueChangeListener(e -> updateList());

        Button addContactButton = new Button("Add task");
        addContactButton.addClickListener(click -> addProjectTask());

        Button updateTreeData = new Button("Update");
        updateTreeData.addClickListener(click -> populateTreeData());

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addContactButton, updateTreeData);
        toolbar.addClassName("toolbar");
        return toolbar;
    }



    private void addProjectTask() {

    }

}
