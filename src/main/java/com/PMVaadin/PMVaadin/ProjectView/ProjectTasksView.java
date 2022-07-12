package com.PMVaadin.PMVaadin.ProjectView;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;
import com.PMVaadin.PMVaadin.Entities.ProjectTaskImpl;
import com.PMVaadin.PMVaadin.MainLayout;
import com.PMVaadin.PMVaadin.Services.ProjectTaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
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
    private ProjectTaskForm form;


    public ProjectTasksView(ProjectTaskService projectTaskService) {

        this.projectTaskService = projectTaskService;
        addClassName("project-tasks-view");
        setSizeFull();
        configureGrid();

        form = new ProjectTaskForm();
        form.setWidth("30em");
        form.addListener(ProjectTaskForm.SaveEvent.class, this::saveProjectTask);
        form.addListener(ProjectTaskForm.DeleteEvent.class, this::deleteProjectTask);
        form.addListener(ProjectTaskForm.CloseEvent.class, event -> closeEditor());

        SplitLayout content = new SplitLayout(treeGrid, form);
//        content.setFlexGrow(2, treeGrid);
//        content.setFlexGrow(1, form);
//        content.setFlexShrink(0, form);
        content.addClassNames("content", "gap-m");
        content.setSizeFull();

        add(getToolbar(), content);

        updateTreeData();

        closeEditor();

        treeGrid.asSingleSelect().addValueChangeListener(event ->
                editProjectTask(event.getValue()));

    }

    private void configureGrid() {

        treeGrid.addClassNames("project-tasks-grid");
        treeGrid.setSizeFull();
        treeGrid.setColumnReorderingAllowed(true);
        treeGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        treeGrid.addHierarchyColumn(ProjectTask::getName).setHeader(ProjectTask.getHeaderName()).setFrozen(true)
                .setAutoWidth(true).setFlexGrow(0).setResizable(true).setSortable(false);
        treeGrid.addColumn(ProjectTask::getWbs).setHeader(ProjectTask.getHeaderWbs()).setResizable(true);
        treeGrid.addColumn(ProjectTask::getStartDate).setHeader(ProjectTask.getHeaderStartDate()).setResizable(true);
        treeGrid.addColumn(ProjectTask::getFinishDate).setHeader(ProjectTask.getHeaderFinishDate()).setResizable(true);
        treeGrid.getColumns().forEach(col -> col.setAutoWidth(true));
//        TreeData<ProjectTask> treeData = new TreeData<>();
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData));

    }

    private void updateTreeData() {

        List<ProjectTask> projectTasks = null;
        try {
            projectTasks = projectTaskService.getProjectTasks();
        } catch (Exception exception) {
            Notification.show(exception.getMessage());
            return;
        }

        Map<Integer, ProjectTask> map = projectTasks.stream().collect(
                Collectors.toMap(ProjectTask::getId, projectTask -> projectTask)
        );

        treeData.clear();
        for (ProjectTask projectTask: projectTasks) {
            treeData.addItem(map.get(projectTask.getParentId()), projectTask);
        }

        treeGrid.getDataProvider().refreshAll();

    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        //filterText.addValueChangeListener(e -> updateList());

        Button addProjectTask = new Button("Add task");
        addProjectTask.addClickListener(click -> addProjectTask());

        Button updateTreeData = new Button("Update");
        updateTreeData.addClickListener(click -> updateTreeData());

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addProjectTask, updateTreeData);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void saveProjectTask(ProjectTaskForm.SaveEvent event) {
        ProjectTask selectedProjectTask = treeGrid.asSingleSelect().getValue();
        ProjectTask savedProjectTask = event.getProjectTask();
        if (selectedProjectTask != null && savedProjectTask.isNew()) savedProjectTask.setParentId(selectedProjectTask.getId());
        projectTaskService.saveTask(savedProjectTask);
        updateTreeData();
        treeGrid.asSingleSelect().clear();
        treeGrid.asSingleSelect().setValue(savedProjectTask);
        closeEditor();
    }

    private void deleteProjectTask(ProjectTaskForm.DeleteEvent event) {
        //service.deleteProjectTask(event.getContact());
        updateTreeData();
        closeEditor();
    }

    public void editProjectTask(ProjectTask projectTask) {
        if (projectTask == null) {
            closeEditor();
        } else {
            form.setProjectTask(projectTask);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void addProjectTask() {

        //treeGrid.asSingleSelect().clear();
        editProjectTask(new ProjectTaskImpl());

    }

    private void closeEditor() {
        form.setProjectTask(null);
        form.setVisible(false);
        removeClassName("editing");
    }

}
