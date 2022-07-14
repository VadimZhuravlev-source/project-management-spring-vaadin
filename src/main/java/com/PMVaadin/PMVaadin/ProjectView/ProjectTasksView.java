package com.PMVaadin.PMVaadin.ProjectView;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;
import com.PMVaadin.PMVaadin.Entities.ProjectTaskImpl;
import com.PMVaadin.PMVaadin.MainLayout;
import com.PMVaadin.PMVaadin.ProjectStructure.TreeItem;
import com.PMVaadin.PMVaadin.Services.ProjectTaskService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
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
import java.util.*;
import java.util.stream.Collectors;

@Route(value="", layout = MainLayout.class)
@PageTitle("Projects | PM")
@PermitAll
public class ProjectTasksView extends VerticalLayout {

    private ProjectTaskService projectTaskService;
    private final TreeData<ProjectTask> treeData = new TreeData<>();

    private final TreeGrid<ProjectTask> treeGrid = new TreeGrid<>();
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
        form.addListener(ProjectTaskForm.DeleteEvent.class, this::deleteProjectTaskEvent);
        form.addListener(ProjectTaskForm.CloseEvent.class, event -> closeEditor());

        SplitLayout content = new SplitLayout(treeGrid, form);
        content.addClassNames("content", "gap-m");
        content.setSizeFull();

        add(getToolbar(), content);

        updateTreeData();

        closeEditor();

        treeGrid.asMultiSelect().addValueChangeListener(event ->
                editProjectTask(event.getValue()));

    }

    private void configureGrid() {

        treeGrid.addClassNames("project-tasks-grid");
        treeGrid.setSizeFull();
        treeGrid.setColumnReorderingAllowed(true);
        treeGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        treeGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        treeGrid.addHierarchyColumn(ProjectTask::getName).setHeader(ProjectTask.getHeaderName()).setFrozen(true)
                .setFlexGrow(0).setResizable(true).setSortable(false).setWidth("25em");
        treeGrid.addColumn(ProjectTask::getWbs).setHeader(ProjectTask.getHeaderWbs()).setResizable(true).setWidth("5em");
        treeGrid.addColumn(ProjectTask::getStartDate).setHeader(ProjectTask.getHeaderStartDate()).setResizable(true).setAutoWidth(true);
        treeGrid.addColumn(ProjectTask::getFinishDate).setHeader(ProjectTask.getHeaderFinishDate()).setResizable(true).setAutoWidth(true);
        treeGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData));

    }

    private void updateTreeData() {

        TreeItem<ProjectTask> rootTask;
        try {
            rootTask = projectTaskService.getTreeProjectTasks();
        } catch (Exception exception) {
            Notification.show(exception.getMessage());
            return;
        }

        treeData.clear();
        populateTreeDataRecursively(rootTask);

        treeGrid.getDataProvider().refreshAll();

    }

    private void populateTreeDataRecursively(TreeItem<ProjectTask> treeItem) {

        for (TreeItem<ProjectTask> child: treeItem.getChildren()) {
            treeData.addItem(child.getParent().getValue(), child.getValue());
            populateTreeDataRecursively(child);
        }

    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        //filterText.addValueChangeListener(e -> updateList());

        Button addProjectTask = new Button("Add");
        addProjectTask.addClickListener(click -> addProjectTask());

        Button updateTreeData = new Button("Update");
        updateTreeData.addClickListener(click -> updateTreeData());

        Button deleteProjectTask = new Button("Delete");
        deleteProjectTask.addClickListener(this::deleteProjectTaskClick);
        deleteProjectTask.addClickShortcut(Key.DELETE);

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addProjectTask, deleteProjectTask, updateTreeData);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void saveProjectTask(ProjectTaskForm.SaveEvent event) {

        ProjectTask selectedProjectTask = treeGrid.asMultiSelect().getValue().stream().findFirst().orElse(null);

        ProjectTask savedProjectTask = event.getProjectTask();
        if (selectedProjectTask != null && savedProjectTask.isNew()) savedProjectTask.setParentId(selectedProjectTask.getId());
        try {
            savedProjectTask = projectTaskService.saveTask(savedProjectTask);
        } catch (Exception e) {
            Notification.show(e.getMessage());
            return;
        }
        updateTreeData();
        treeGrid.asMultiSelect().clear();
        Set<ProjectTask> set = new HashSet<>();
        set.add(savedProjectTask);
        treeGrid.asMultiSelect().setValue(set);
        closeEditor();
    }

    private void deleteProjectTaskEvent(ProjectTaskForm.DeleteEvent event) {
        List<ProjectTask> projectTasks = new ArrayList<>();
        projectTasks.add(event.getProjectTask());
        deleteProjectTask(projectTasks);
    }

    private void deleteProjectTaskClick(ClickEvent<Button> clickEvent) {
        List<ProjectTask> projectTasks = treeGrid.asMultiSelect().getValue().stream().toList();
        deleteProjectTask(projectTasks);
    }

    private void deleteProjectTask(List<ProjectTask> projectTasks) {
        try {
            projectTaskService.deleteTasks(projectTasks);
        } catch (Exception e) {
            Notification.show(e.getMessage());
            return;
        }
        updateTreeData();
        closeEditor();
    }

    private void editProjectTask(Set<ProjectTask> projectTasks) {
        if (projectTasks == null || projectTasks.size() != 1) {
            closeEditor();
        } else {
            ProjectTask projectTask = projectTasks.stream().findFirst().orElse(null);
            form.setProjectTask(projectTask);
            form.setVisible(true);
            form.name.setAutofocus(true);
            addClassName("editing");
        }
    }

    private void addProjectTask() {

        Set<ProjectTask> set = new HashSet<>();
        set.add(new ProjectTaskImpl());
        editProjectTask(set);

    }

    private void closeEditor() {
        form.setProjectTask(null);
        form.setVisible(false);
        removeClassName("editing");
    }

}
