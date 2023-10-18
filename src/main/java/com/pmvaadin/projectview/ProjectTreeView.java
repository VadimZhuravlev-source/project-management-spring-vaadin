package com.pmvaadin.projectview;

import com.pmvaadin.MainLayout;
import com.pmvaadin.commonobjects.ConfirmDialog;
import com.pmvaadin.projectstructure.ProjectHierarchicalDataProvider;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import com.pmvaadin.projecttasks.services.ProjectTreeService;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projecttasks.services.TreeHierarchyChangeService;
import com.pmvaadin.projecttasks.views.ProjectTaskForm;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.component.grid.dnd.GridDropEvent;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import javax.annotation.security.PermitAll;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.List;

@Route(value="", layout = MainLayout.class)
@PageTitle("Projects | PM")
@PermitAll
public class ProjectTreeView extends VerticalLayout {

    private final ProjectTreeService projectTreeService;
    private final TreeHierarchyChangeService treeHierarchyChangeService;
    private final TreeGrid<ProjectTask> treeGrid = new TreeGrid<>();
    private final TextField filterText = new TextField();
    private final ProjectTaskForm projectTaskForm;
    private ProjectTaskForm editingForm;
    private final ProjectHierarchicalDataProvider dataProvider;
    private boolean isEditingFormOpen;
    private Set<String> currentColumns = new HashSet<>();

    private ProjectTaskPropertyNames projectTaskPropertyNames = new ProjectTaskPropertyNames();

    public ProjectTreeView(ProjectTreeService projectTreeService, TreeHierarchyChangeService treeHierarchyChangeService, ProjectTaskForm projectTaskForm) {

        this.projectTreeService = projectTreeService;
        this.treeHierarchyChangeService = treeHierarchyChangeService;
        this.projectTaskForm = projectTaskForm;
        dataProvider = new ProjectHierarchicalDataProvider(treeHierarchyChangeService);
        addClassName("project-tasks-view");
        setSizeFull();
        configureTreeGrid();

        Component toolBar = getToolbar();
        add(toolBar, treeGrid);

        updateTreeGrid();

    }

    private void configureTreeGrid() {

        treeGrid.setDataProvider(dataProvider);
//        var hierarchicalDataCommunicator = treeGrid.getDataCommunicator();
//        hierarchicalDataCommunicator.reset();

        treeGrid.addClassNames("project-tasks-grid");
        treeGrid.setSizeFull();
        treeGrid.setColumnReorderingAllowed(true);
        treeGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        treeGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        treeGrid.addHierarchyColumn(ProjectTask::getName).setHeader(projectTaskPropertyNames.getHeaderName()).setFrozen(true)
                .setResizable(true).setSortable(false).setWidth("25em");
        treeGrid.addColumn(ProjectTask::getWbs).setHeader(ProjectTask.getHeaderWbs()).setResizable(true).setWidth("5em");
        treeGrid.addColumn(ProjectTask::getStartDate).setHeader(ProjectTask.getHeaderStartDate()).setResizable(true).setAutoWidth(true);
        treeGrid.addColumn(ProjectTask::getFinishDate).setHeader(ProjectTask.getHeaderFinishDate()).setResizable(true).setAutoWidth(true);
        treeGrid.getColumns().forEach(col -> col.setAutoWidth(true));

        //treeGrid.setDataProvider(new TreeDataProvider<>(new TreeData<>()));

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

    private void customizeColumns() {

    }

    private void updateTreeGrid() {

        if (isEditingFormOpen) return;

        try {
            treeGrid.asMultiSelect().clear();
            treeGrid.getDataProvider().refreshAll();
        } catch (Throwable e) {
            showProblem(e);
        }

    }

    private HorizontalLayout getToolbar() {

        filterText.setPlaceholder("Filter...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        //filterText.addValueChangeListener(e -> updateProject());

        Button addProjectTask = new Button("Add");
        addProjectTask.addClickListener(click -> addProjectTask());

        Button updateTreeData = new Button("Update");
        updateTreeData.addClickListener(click -> updateTreeGrid());
        updateTreeData.addClickShortcut(Key.F5);

        Button deleteProjectTask = new Button("Delete");
        deleteProjectTask.addClickListener(this::deleteProjectTaskClick);
        deleteProjectTask.addClickShortcut(Key.DELETE);

        Button moveUp = new Button("Move up");
        moveUp.addClickListener(event -> moveTasks(ProjectTreeService.Direction.UP));

        Button moveDown = new Button("Move down");
        moveDown.addClickListener(event -> moveTasks(ProjectTreeService.Direction.DOWN));

        Button expandAll = new Button("Expand all");
        expandAll.addClickListener(this::expandAll);

        Button collapseAll = new Button("Collapse all");
        collapseAll.addClickListener(this::collapseAll);

        Button changeLevelUp = new Button("Increase task level");
        changeLevelUp.addClickListener(this::increaseTaskLevel);

        Button changeLevelDown = new Button("Decrease task level");
        changeLevelDown.addClickListener(this::decreaseTaskLevel);

        Button createTestCase = new Button("Create test case");
        createTestCase.addClickListener(this::createTestCase);

        HorizontalLayout toolbar = new HorizontalLayout(
                //filterText,
                addProjectTask, deleteProjectTask, updateTreeData,
                changeLevelUp,
                changeLevelDown,
                createTestCase,
                moveUp, moveDown);
        toolbar.addClassName("toolbar");

        MenuBar menuBar = new MenuBar();
        MenuItem settingsItem = menuBar.addItem("Settings");
        SubMenu subMenu = settingsItem.getSubMenu();
        ColumnSelectionForm columnSelectionForm = new ColumnSelectionForm(currentColumns);
        columnSelectionForm.setOnCloseEvent(e -> {
            currentColumns.clear();
            currentColumns.addAll(e);
        });
        ComponentEventListener<ClickEvent<MenuItem>> listener = e -> columnSelectionForm.open();
        subMenu.addItem("Column settings", listener);
        toolbar.add(menuBar);

        return toolbar;

    }

    private void increaseTaskLevel(ClickEvent<Button> clickEvent) {

        changeLocation(ProjectTreeService.Direction.UP);

    }

    private void decreaseTaskLevel(ClickEvent<Button> clickEvent) {

        changeLocation(ProjectTreeService.Direction.DOWN);

    }

    private void changeLocation(ProjectTreeService.Direction direction) {

        try {

            Set<ProjectTask> selectedProjectTasks = treeGrid.asMultiSelect().getValue();

            projectTreeService.changeLocation(selectedProjectTasks, direction);

            //treeGrid.asMultiSelect().setValue(changedTasks);

            updateTreeGrid();

        } catch (Throwable e) {
            showProblem(e);
        }

    }

    private void createTestCase(ClickEvent<Button> clickEvent) {

        projectTreeService.createTestCase();
        updateTreeGrid();

    }

    private void expandAll(ClickEvent<Button> clickEvent) {

        //treeGrid.expandRecursively(treeGrid.getTreeData().getRootItems(), 20);

    }

    private void collapseAll(ClickEvent<Button> clickEvent) {

        //treeGrid.collapseRecursively(treeGrid.getTreeData().getRootItems(), 20);

    }

    private void saveProjectTask(ProjectTaskForm.SaveEvent event) {

        ProjectTask savedProjectTask = event.getProjectTask();
        if (savedProjectTask == null) return;
//        ProjectTask refreshedItem = savedProjectTask.getParent();
//        if (refreshedItem == null) refreshedItem = savedProjectTask;
//        treeGrid.asMultiSelect().deselectAll();
//        treeGrid.asMultiSelect().select(savedProjectTask);
        //treeGrid.getDataProvider().refreshItem(refreshedItem, true);
        closeEditor();
        updateTreeGrid();

    }

    private void deleteProjectTaskClick(ClickEvent<Button> clickEvent) {
        if (isEditingFormOpen) return;
        List<ProjectTask> projectTasks = treeGrid.asMultiSelect().getValue().stream().toList();
        deleteProjectTask(projectTasks);
    }

    private void deleteProjectTask(List<ProjectTask> projectTasks) {
        try {
            projectTreeService.delete(projectTasks);
        } catch (Throwable e) {
            showProblem(e);
            return;
        }
        updateTreeGrid();
        //closeEditor();
    }

    private void editProjectTask(ProjectTask projectTask) {

        editingForm = projectTaskForm.newInstance();
        editingForm.setProjectTask(projectTask);
        editingForm.addListener(ProjectTaskForm.SaveEvent.class, this::saveProjectTask);
        editingForm.addListener(ProjectTaskForm.CloseEvent.class, event -> closeEditor());
        editingForm.open();
        isEditingFormOpen = true;

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
        isEditingFormOpen = false;
        removeClassName("editing");
    }

    private void moveTasks(ProjectTreeService.Direction direction) {

        Set<ProjectTask> selectedTasks = treeGrid.asMultiSelect().getValue();

        try {
            projectTreeService.changeSortOrder(selectedTasks, direction);
        } catch (Throwable e) {
            showProblem(e);
        }
        updateTreeGrid();

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

        if (projectTask == null) return;

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
        ProjectTask syncedProjectTask = projectTreeService.sync(projectTask);
        if (syncedProjectTask == null) {
            showUpdatableDialog("Selected task does not exist. Please, update project.");
            return;
        }

        if (!projectTask.getVersion().equals(syncedProjectTask.getVersion())) {
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
            if (dropTargetItem == null) return;

            Set<ProjectTask> draggedItems = event.getSource().getSelectedItems();
            if (draggedItems == null) return;

            GridDropLocation dropLocation = event.getDropLocation();

            if (dropLocation == GridDropLocation.ON_TOP && draggedItems.contains(dropTargetItem)) return;

            //if (!checkMovableDraggedItemsInDroppedItem(draggedItems, dropTargetItem)) return;

            projectTreeService.changeLocation(draggedItems, dropTargetItem, dropLocation);

//            treeGrid.asMultiSelect().clear();
//            treeGrid.asMultiSelect().setValue(updatedTasks);

            updateTreeGrid();

        } catch (Throwable e) {
            showProblem(e);
        }

    }

}
