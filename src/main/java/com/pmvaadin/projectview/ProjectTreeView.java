package com.pmvaadin.projectview;

import com.pmvaadin.MainLayout;
import com.pmvaadin.commonobjects.ConfirmDialog;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import com.pmvaadin.projectstructure.ProjectData;
import com.pmvaadin.projectstructure.ProjectDataService;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projecttasks.views.ProjectTaskForm;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.component.grid.dnd.GridDropEvent;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@Route(value="", layout = MainLayout.class)
@PageTitle("Projects | PM")
@PermitAll
public class ProjectTreeView extends VerticalLayout {

    private final ProjectDataService projectDataService;
    private final TreeGrid<ProjectTask> treeGrid = new TreeGrid<>();
    private final TextField filterText = new TextField();
    private final ProjectTaskForm editingForm;

    public ProjectTreeView(ProjectDataService projectDataService, ProjectTaskForm form) {

        this.projectDataService = projectDataService;
        this.editingForm = form;
        addClassName("project-tasks-view");
        setSizeFull();
        configureTreeGrid();

        editingForm.addListener(ProjectTaskForm.SaveEvent.class, this::saveProjectTask);
        editingForm.addListener(ProjectTaskForm.DeleteEvent.class, this::deleteProjectTaskEvent);
        editingForm.addListener(ProjectTaskForm.CloseEvent.class, event -> closeEditor());

        add(getToolbar(), treeGrid);

        updateTreeGrid();

    }

    private void configureTreeGrid() {

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
        treeGrid.setDataProvider(new TreeDataProvider<>(new TreeData<>()));

        // hide checkbox column
        treeGrid.getElement().getNode().runWhenAttached(ui ->
                ui.beforeClientResponse(this, context ->
                        getElement().executeJs(
                                "if (this.querySelector('vaadin-grid-flow-selection-column')) {" +
                                        " this.querySelector('vaadin-grid-flow-selection-column').hidden = true }")));

        // DragDrop
        treeGrid.setRowsDraggable(true);
        treeGrid.setDropMode(GridDropMode.ON_TOP_OR_BETWEEN);

        treeGrid.addDragStartListener(this::dragStartListener);

        treeGrid.addDropListener(this::dropEvent);

        treeGrid.addItemClickListener(this::onMouseClick);
        treeGrid.addItemDoubleClickListener(this::onMouseDoubleClick);

    }

    private void updateTreeGrid() {

        try {

            List<ProjectTask> projectTasks = projectDataService.getTreeProjectTasks();

            treeGrid.getTreeData().clear();
            Map<?, Boolean> selectedIds = treeGrid.asMultiSelect().getSelectedItems()
                    .stream().collect(Collectors.toMap(ProjectTask::getId, p -> true));

            treeGrid.asMultiSelect().clear();
            Set<ProjectTask> selectedTasks = new HashSet<>(selectedIds.size());

            populateTreeData(projectTasks, selectedIds, selectedTasks);
            treeGrid.asMultiSelect().setValue(selectedTasks);

            treeGrid.getDataProvider().refreshAll();

        } catch (Throwable e) {
            showProblem(e);
        }

    }

    private void populateTreeData(List<ProjectTask> projectTasks, Map<?, Boolean> selectedIds,
                                             Set<ProjectTask> selectedTasks) {

        TreeData<ProjectTask> treeData = treeGrid.getTreeData();
        Map<?, ProjectTask> mapIdProjectTask = projectTasks.stream().
                collect(Collectors.toMap(ProjectTask::getId, projectTask -> projectTask));
        for (ProjectTask projectTask: projectTasks) {
            treeData.addItem(mapIdProjectTask.getOrDefault(projectTask.getParentId(), null), projectTask);
            if (selectedIds.getOrDefault(projectTask.getId(), false)) selectedTasks.add(projectTask);
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

        Button moveUp = new Button("Move up");
        moveUp.addClickListener(event -> moveTasks(Direction.UP));

        Button moveDown = new Button("Move down");
        moveDown.addClickListener(event -> moveTasks(Direction.DOWN));

        Button expandAll = new Button("Expand all");
        expandAll.addClickListener(this::expandAll);

        Button collapseAll = new Button("Collapse all");
        collapseAll.addClickListener(this::collapseAll);

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addProjectTask, deleteProjectTask, updateTreeData,
                moveUp, moveDown, expandAll, collapseAll);

        toolbar.addClassName("toolbar");
        return toolbar;

    }

    private void expandAll(ClickEvent<Button> clickEvent) {

        treeGrid.expandRecursively(treeGrid.getTreeData().getRootItems(), 20);

    }

    private void collapseAll(ClickEvent<Button> clickEvent) {

        treeGrid.collapseRecursively(treeGrid.getTreeData().getRootItems(), 20);

    }

    private void saveProjectTask(ProjectTaskForm.SaveEvent event) {

        ProjectTask savedProjectTask = event.getProjectTask();
        if (savedProjectTask == null) return;
        try {
            projectDataService.saveTask(savedProjectTask);
        } catch (Throwable e) {
            showProblem(e);
            return;
        }

        treeGrid.asMultiSelect().clear();
        Set<ProjectTask> selectedTasks = new HashSet<>(1);
        selectedTasks.add(savedProjectTask);
        treeGrid.asMultiSelect().setValue(selectedTasks);

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
            projectDataService.deleteTasks(projectTasks);
        } catch (Throwable e) {
            showProblem(e);
            return;
        }
        updateTreeGrid();
        closeEditor();
    }

    private void editProjectTask(ProjectTask projectTask) {
//        if (projectTasks == null || projectTasks.size() != 1) {
//            closeEditor();
//        } else {
//            ProjectTask projectTask = projectTasks.stream().findFirst().orElse(null);
//            editingForm.setProjectTask(projectTask);
//            editingForm.setVisible(true);
        editingForm.setProjectTask(projectTask);
        editingForm.open();
        addClassName("editing");
//        }
    }

    private void addProjectTask() {

        ProjectTask selectedProjectTask = treeGrid.asMultiSelect().getValue().stream().findFirst().orElse(null);

        Integer parentId = null;
        if (selectedProjectTask != null) parentId = selectedProjectTask.getParentId();

        //Set<ProjectTask> set = new HashSet<>();
        ProjectTask newProjectTasks = new ProjectTaskImpl();
        newProjectTasks.setParentId(parentId);
        //set.add(newProjectTasks);
        editProjectTask(newProjectTasks);

    }

    private void closeEditor() {
        editingForm.close();
        removeClassName("editing");
    }

    private void moveTasks(Direction direction) {

        ProjectTask projectTasks1 = treeGrid.asMultiSelect().getValue().stream().findFirst().orElse(null);
        TreeData<ProjectTask> treeData = treeGrid.getTreeData();
        ProjectTask parent = treeData.getParent(projectTasks1);
        List<ProjectTask> children = treeData.getChildren(parent);

        ProjectTask projectTasks2 = null;
        if (direction == Direction.UP) {
            for (ProjectTask child : children) {
                if (child.equals(projectTasks1)) break;
                projectTasks2 = child;
            }
        }else {
            for (int i = children.size() - 1; i >= 0; i--) {
                if (children.get(i).equals(projectTasks1)) break;
                projectTasks2 = children.get(i);
            }
        }

        if (projectTasks2 == null) return;

        List<ProjectTask> replacedTasks;
        try {
            Map<ProjectTask, ProjectTask> swappedTasks = new HashMap<>();
            swappedTasks.put(projectTasks1, projectTasks2);
            replacedTasks = projectDataService.swapTasks(swappedTasks);
        } catch (Throwable e) {
            showProblem(e);
            return;
        }
        int countReplacedTasks = 2;
        if (replacedTasks.size() != countReplacedTasks) {
            return;
        }

        updateTreeGrid();

//        ProjectTask replacedTasks1 = null;
//        ProjectTask replacedTasks2 = null;
//        for (ProjectTask projectTask: replacedTasks) {
//            if (projectTask.equals(projectTasks1)) replacedTasks1 = projectTask;
//            if (projectTask.equals(projectTasks2)) replacedTasks2 = projectTask;
//        }
//
//        if (replacedTasks1 == null || replacedTasks2 == null) return;
//
//        treeGrid.getDataProvider().refreshItem(replacedTasks1);
//        treeGrid.getDataProvider().refreshItem(replacedTasks2);
//
//        if (direction == Direction.UP) {
//            treeData.moveAfterSibling(replacedTasks1, replacedTasks2);
//        } else {
//            treeData.moveAfterSibling(replacedTasks2, replacedTasks1);
//        }
//        treeGrid.getDataProvider().refreshItem(parent, true);

    }

    private void showProblem(Throwable exception) {

        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeaderTitle("Error");
        confirmDialog.addCancelText("Close");
        confirmDialog.setRejectable(false);
        confirmDialog.addConfirmText("Ok");
        String message;
        if (exception instanceof StandardError) {
            message = exception.getMessage();
        } else {
            message = "An unexpected error occurred: \n" + exception.getMessage();
        }
        confirmDialog.add(message);

        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));

        confirmDialog.addDetailsText(sw.toString());

        confirmDialog.open();

    }

    private enum Direction {
        UP,
        DOWN
    }

    // Events

    private void dragStartListener(GridDragStartEvent<ProjectTask> event) {

        List<ProjectTask> draggedItems = event.getDraggedItems();
        if (!treeGrid.asMultiSelect().getSelectedItems().containsAll(draggedItems)) {
            treeGrid.asMultiSelect().clear();
            treeGrid.asMultiSelect().setValue(new HashSet<>(draggedItems));
        }

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

    private void onMouseDoubleClick(ItemDoubleClickEvent<ProjectTask> event) {

        if (event == null) return;

        ProjectTask projectTask = event.getItem();
        if (projectTask == null) return;
        ProjectData projectData = projectDataService.getProjectData(projectTask);
        ProjectTask refreshedProjectTask = projectData.getProjectTask();
        if (refreshedProjectTask == null) {
            showUpdatableDialog("Selected task does not exist. Please, update project.");
            return;
        }

        if (!projectTask.getVersion().equals(refreshedProjectTask.getVersion())) {
            showUpdatableDialog("Selected task is changed by another user. Please, update project.");
            return;
        }

        editProjectTask(projectTask);

    }

    private void showUpdatableDialog(String message) {

        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeaderTitle("Error");
        confirmDialog.addCancelText("Close");
        confirmDialog.setRejectable(false);
        confirmDialog.addConfirmText("Update");
        confirmDialog.add(message);
        confirmDialog.addConfirmListener(event1 -> updateTreeGrid());
        confirmDialog.open();

    }

    private void dropEvent(GridDropEvent<ProjectTask> event) {

        try {

            ProjectTask dropTargetItem = event.getDropTargetItem().orElse(null);

            Set<ProjectTask> draggedItems = event.getSource().getSelectedItems();
            if (dropTargetItem == null || draggedItems == null || draggedItems.contains(dropTargetItem)) return;

            if (!checkMovableDraggedItemsInDroppedItem(draggedItems, dropTargetItem)) return;

            projectDataService.setNewParentOfTheTasks(draggedItems, dropTargetItem);

            updateTreeGrid();

        } catch (Throwable e) {
            showProblem(e);
        }

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

}
