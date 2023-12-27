package com.pmvaadin.projectview;

import com.pmvaadin.MainLayout;
import com.pmvaadin.commonobjects.ConfirmDialog;
import com.pmvaadin.projectstructure.TaskTreeProvider;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import com.pmvaadin.projecttasks.services.ProjectTreeService;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projecttasks.services.TreeHierarchyChangeService;
import com.pmvaadin.projecttasks.views.ProjectTaskForm;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayoutVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import org.vaadin.tltv.gantt.Gantt;
import org.vaadin.tltv.gantt.model.Resolution;
import org.vaadin.tltv.gantt.model.Step;

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
    private final TaskTreeProvider dataProvider;
    private boolean isEditingFormOpen;
    private final List<String> chosenColumns;
    private final ProjectTaskPropertyNames projectTaskPropertyNames = new ProjectTaskPropertyNames();
    private boolean isGanttDisplayed;
    private final Button displayGantt = new Button("Gantt chart display");
    private Gantt ganttChart = new Gantt();
    private final HorizontalLayout treeGridContainer = new HorizontalLayout();
    private final Button updateTreeData = new Button("Update");

    public ProjectTreeView(ProjectTreeService projectTreeService, TreeHierarchyChangeService treeHierarchyChangeService, ProjectTaskForm projectTaskForm) {

        this.projectTreeService = projectTreeService;
        this.treeHierarchyChangeService = treeHierarchyChangeService;
        this.projectTaskForm = projectTaskForm;
        chosenColumns = projectTaskPropertyNames.getTreeDefaultColumns();
        dataProvider = new TaskTreeProvider(this.treeHierarchyChangeService, chosenColumns, treeGrid);
        dataProvider.addDataProviderListener(this::dataProviderListener);
        setSizeFull();
        configureTreeGrid();

        Component toolBar = getToolbar();
        treeGridContainer.add(treeGrid);
        treeGridContainer.setSizeFull();
        add(toolBar, treeGridContainer);

        updateTreeGrid();

    }

    private void dataProviderListener(DataChangeEvent<ProjectTask> event) {
        if (!isGanttDisplayed) {
            return;
        }
//        fillGantt();
    }

    private void fillGantt() {
        ganttChart = new Gantt();
        ganttChart.setHeightFull();
        var tempTree = dataProvider.getTempTree();
        var rootTasks = tempTree.get(null);
        if (rootTasks == null || rootTasks.isEmpty()) return;
        fillGanttRecursively(rootTasks, tempTree);

        ganttChart.setResolution(Resolution.Day);
        var minStart = ganttChart.getSteps().map(Step::getStartDate).min(Comparator.naturalOrder());
        var maxStart = ganttChart.getSteps().map(Step::getEndDate).max(Comparator.naturalOrder());
        ganttChart.setStartDate(minStart.get().toLocalDate().minusDays(1));
        ganttChart.setEndDate(maxStart.get().toLocalDate().plusDays(1));
        ganttChart.setTimeZone(TimeZone.getDefault());
    }
    
    private void fillGanttRecursively(List<ProjectTask> tasks, Map<ProjectTask, List<ProjectTask>> tempTree) {
        for (var task: tasks) {
            var step = getStep(task);
            ganttChart.addStep(step);
            var children = tempTree.get(task);
            if (children == null) continue;
            fillGanttRecursively(children, tempTree);
        }   
    }
    
    private Step getStep(ProjectTask projectTask) {
        var step = new Step();
        step.setResizable(false);
        step.setMovable(false);
        step.setShowProgress(false);
        step.setSubstep(false);
        step.setCaption(projectTask.getName());
        step.setIdentifier(projectTask.getId().longValue());
        step.setStartDate(projectTask.getStartDate());
        step.setEndDate(projectTask.getFinishDate());
        return step;
    }

    private void configureTreeGrid() {

        treeGrid.setDataProvider(dataProvider);
        treeGrid.addClassNames("tree-gantt");
        treeGrid.setSizeFull();
        treeGrid.setColumnReorderingAllowed(true);
        treeGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        treeGrid.setSelectionMode(Grid.SelectionMode.MULTI);

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
        treeGrid.setId("treeGrid");

        customizeColumns();

    }

    private void customizeColumns() {

        treeGrid.removeAllColumns();

        treeGrid.addHierarchyColumn(ProjectTask::getName).setHeader(projectTaskPropertyNames.getHeaderName()).setFrozen(true)
                .setResizable(true).setSortable(false).setWidth("25em");
        var columnCustomizations = projectTaskPropertyNames.getAvailableColumnProps();
        for (String name: chosenColumns) {
            if (name.equals(projectTaskPropertyNames.getPropertyWbs())) {
                treeGrid.addColumn(ProjectTask::getWbs).setHeader(projectTaskPropertyNames.getHeaderWbs()).setResizable(true).setWidth("5em");
                continue;
            }
            var colProp = columnCustomizations.get(name);
            if (colProp == null) continue;
            Grid.Column<ProjectTask> column;
            if (name.equals(projectTaskPropertyNames.getPropertyIsProject())) {
                column = addIsProjectColumn();
            } else {
                column = treeGrid.addColumn(colProp.valueProvider());
            }
            column.setHeader(colProp.representation()).setResizable(true).setAutoWidth(true);
        }

    }

    private Grid.Column<ProjectTask> addIsProjectColumn() {
        return treeGrid.addComponentColumn((item) -> {
            Icon icon = null;
            if(item.isProject()){
                icon = VaadinIcon.CHECK.create();
                icon.setColor("green");
            }
            return icon;
        }).setHeader(projectTaskPropertyNames.getHeaderIsProject());
    }

    private void updateTreeGrid() {

        if (isEditingFormOpen) return;

        try {
            var selectedItems = treeGrid.getSelectedItems();
            dataProvider.setSelectedItems(selectedItems);
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

//        Button updateTreeData = new Button("Update");
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

        displayGantt.addClickListener(this::displayGanttListener);

        HorizontalLayout toolbar = new HorizontalLayout(
                //filterText,
                addProjectTask, deleteProjectTask, updateTreeData,
                changeLevelUp,
                changeLevelDown,
                createTestCase,
                moveUp, moveDown,
                displayGantt);
        toolbar.addClassName("toolbar");

        MenuBar menuBar = new MenuBar();
        MenuItem settingsItem = menuBar.addItem("Settings");
        SubMenu subMenu = settingsItem.getSubMenu();

        ComponentEventListener<ClickEvent<MenuItem>> listener = e -> {
            ColumnSelectionForm columnSelectionForm = new ColumnSelectionForm(chosenColumns);
            columnSelectionForm.setOnCloseEvent(chosenColumns -> {
                this.chosenColumns.clear();
                this.chosenColumns.addAll(chosenColumns);
                customizeColumns();
            });
            columnSelectionForm.open();
        };
        subMenu.addItem("Column settings", listener);
        toolbar.add(menuBar);

        return toolbar;

    }

    private void displayGanttListener(ClickEvent<Button> clickEvent) {

        if (isGanttDisplayed) {
            isGanttDisplayed = false;
            displayGantt.removeThemeVariants(ButtonVariant.LUMO_ERROR);
            treeGridContainer.removeAll();
            treeGridContainer.add(treeGrid);
            dataProvider.setFormTempTree(false);
        } else {
            isGanttDisplayed = true;
            displayGantt.addThemeVariants(ButtonVariant.LUMO_ERROR);
            treeGridContainer.removeAll();
            fillGantt();

            var splitLayout = new SplitLayout(treeGrid, new HorizontalLayout(ganttChart));
            splitLayout.setSizeFull();
            splitLayout.addThemeVariants(SplitLayoutVariant.LUMO_MINIMAL);

            ganttChart.getElement().executeJs("this.registerScrollElement($0.$.table)", treeGrid);
            treeGridContainer.add(splitLayout);
            treeGrid.getStyle().set("overflow-y", "hidden");
//            treeGridContainer.add(treeGrid, ganttChart);
//            dataProvider.setFormTempTree(true);
//            updateTreeData.click();
//            fillGantt();
        }

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
