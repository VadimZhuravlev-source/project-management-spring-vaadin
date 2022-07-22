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
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.dnd.GridDropEvent;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
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

        updateTreeGrid();

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
                .setResizable(true).setSortable(false).setWidth("25em");
        treeGrid.addColumn(ProjectTask::getWbs).setHeader(ProjectTask.getHeaderWbs()).setResizable(true).setWidth("5em");
        treeGrid.addColumn(ProjectTask::getStartDate).setHeader(ProjectTask.getHeaderStartDate()).setResizable(true).setAutoWidth(true);
        treeGrid.addColumn(ProjectTask::getFinishDate).setHeader(ProjectTask.getHeaderFinishDate()).setResizable(true).setAutoWidth(true);
        treeGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData));

        // hide checkbox column
        treeGrid.getElement().getNode().runWhenAttached(ui ->
                ui.beforeClientResponse(this, context ->
                        getElement().executeJs(
                                "if (this.querySelector('vaadin-grid-flow-selection-column')) {" +
                                        " this.querySelector('vaadin-grid-flow-selection-column').hidden = true }")));

        // DragDrop
        treeGrid.setRowsDraggable(true);
        treeGrid.setDropMode(GridDropMode.ON_TOP_OR_BETWEEN);

        treeGrid.addDragStartListener(
                e -> {
                    List<ProjectTask> draggedItems = e.getDraggedItems();
                    if (!treeGrid.asMultiSelect().getSelectedItems().containsAll(draggedItems)) {
                        treeGrid.asMultiSelect().clear();
                        treeGrid.asMultiSelect().setValue(new HashSet<>(draggedItems));
                    }
                }
        );

        treeGrid.addDropListener(this::dropEvent);

        treeGrid.addItemClickListener(this::onMouseClick);

    }

    private void onMouseClick(ItemClickEvent<ProjectTask> event) {
        if (event == null) {
            return;
        }

        ProjectTask projectTask = event.getItem();

        Set<ProjectTask> newSelectedProjectTasks = new HashSet<>();
        if (event.isCtrlKey()) {
            newSelectedProjectTasks.addAll(treeGrid.asMultiSelect().getSelectedItems());
        }

        if (newSelectedProjectTasks.contains(projectTask))
            newSelectedProjectTasks.remove(projectTask);
        else
            newSelectedProjectTasks.add(projectTask);

        treeGrid.asMultiSelect().setValue(newSelectedProjectTasks);

    }

    private void dropEvent(GridDropEvent<ProjectTask> event) {

        ProjectTask dropTargetItem = event.getDropTargetItem().orElse(null);

        Set<ProjectTask> draggedItems = event.getSource().getSelectedItems();
        if (dropTargetItem == null || draggedItems == null || draggedItems.contains(dropTargetItem)) return;

        if (!checkMovableDraggedItemsInDroppedItem(draggedItems, dropTargetItem)) return;

        try {
            projectTaskService.setNewParentOfTheTasks(draggedItems, dropTargetItem);
        } catch (Exception e) {
            Notification.show(e.getMessage());
            return;
        }

        updateTreeGrid();

    }

    private boolean checkMovableDraggedItemsInDroppedItem(Set<ProjectTask> draggedItems, ProjectTask dropTargetItem) {

        TreeData<ProjectTask> treeData = treeGrid.getTreeData();
        Map<ProjectTask, Boolean> rootItems = treeData.getRootItems().stream()
                .collect(Collectors.toMap(projectTask -> projectTask, o -> true));

        ProjectTask parent = dropTargetItem;
        Map<ProjectTask, Boolean> allItemParents = new HashMap<>();
        while (parent != null) {
            if (rootItems.getOrDefault(parent, false)) break;
            parent = treeData.getParent(parent);
            allItemParents.put(parent, true);
        }

        for (ProjectTask draggedItem:draggedItems) {
            if (allItemParents.getOrDefault(draggedItem, false)) return false;
        }

        return true;
    }

    private void updateTreeGrid() {

        TreeItem<ProjectTask> rootTask;
        try {
            rootTask = projectTaskService.getTreeProjectTasks();
        } catch (Exception exception) {
            Notification.show(exception.getMessage());
            return;
        }

        treeData.clear();
        Map<Integer, Boolean> selectedIds = treeGrid.asMultiSelect().getSelectedItems()
                .stream().collect(Collectors.toMap(ProjectTask::getId, p -> true));

        treeGrid.asMultiSelect().clear();
        Set<ProjectTask> selectedTasks = new HashSet<>(selectedIds.size());

        populateTreeDataRecursively(rootTask, selectedIds, selectedTasks);
        treeGrid.asMultiSelect().setValue(selectedTasks);

        treeGrid.getDataProvider().refreshAll();

    }

    private void populateTreeDataRecursively(TreeItem<ProjectTask> treeItem, Map<Integer, Boolean> selectedIds,
                                             Set<ProjectTask> selectedTasks) {

        for (TreeItem<ProjectTask> child: treeItem.getChildren()) {
            treeData.addItem(child.getParent().getValue(), child.getValue());
            if (selectedIds.getOrDefault(child.getValue().getId(), false)) selectedTasks.add(child.getValue());
            populateTreeDataRecursively(child, selectedIds, selectedTasks);
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
        updateTreeData.addClickListener(click -> updateTreeGrid());
        updateTreeData.addClickShortcut(Key.F5);

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
            projectTaskService.saveTask(savedProjectTask);
        } catch (Exception e) {
            Notification.show(e.getMessage());
            return;
        }
        updateTreeGrid();

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
        updateTreeGrid();
        closeEditor();
    }

    private void editProjectTask(Set<ProjectTask> projectTasks) {
        if (projectTasks == null || projectTasks.size() != 1) {
            closeEditor();
        } else {
            ProjectTask projectTask = projectTasks.stream().findFirst().orElse(null);
            form.setProjectTask(projectTask);
            form.setVisible(true);
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
