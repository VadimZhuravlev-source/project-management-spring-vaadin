package com.pmvaadin.charts;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.function.SerializableComparator;
import org.vaadin.tltv.gantt.Gantt;
import org.vaadin.tltv.gantt.model.Step;


public class TreeGridGanttLayout extends HorizontalLayout {


    public TreeGridGanttLayout(Gantt gantt) {
        this.gantt = gantt;

        setSizeFull();
        setMargin(false);
        setSpacing(false);

        UI.getCurrent().getPage().getStyles().add(".v-treegrid tr th, .v-treegrid tr td { height: 37px; }");

        ganttTreeGrid.getDataCommunicator().

        ganttTreeGrid = createGridForGantt();

        add(ganttTreeGrid);
        add(gantt);

    }


    private TreeGrid<Step> createGridForGantt() {

        // create hierarchy just for demonstration purpose
        TreeData<Step> treeData = new TreeData<>();
        int index = 0;
        for (Step step : gantt.getSteps()) {
            if (index == 1) {
                treeData.addItems(gantt.getSteps().get(0), step);
            } else if (index == 2) {
                treeData.addItems(gantt.getSteps().get(1), step);
            } else {
                treeData.addItems(null, step);
            }
            index++;
        }
        dataProvider = new TreeDataProvider<>(treeData);
        dataProvider.setSortComparator(defaultSortOrderByIndex);
        TreeGrid<Step> grid = new TreeGrid<>(dataProvider);
        grid.setWidth(400, Unit.PIXELS);
        grid.setHeight(100, Unit.PERCENTAGE);

        Column<Step, ?> c = grid.addColumn(Step::getCaption);
        c.setSortable(false);
        c.setResizable(false);

        grid.expand(gantt.getSteps().get(0), gantt.getSteps().get(1));

        gantt.se.setVerticalScrollDelegateTarget(grid);

        grid.addExpandListener(new ExpandListener<Step>() {
            @Override
            public void itemExpand(ExpandEvent<Step> event) {
                if (event.isUserOriginated()) {
                    addChildStepRecursively(grid, event.getExpandedItem(), new MutableInt());
                }
            }
        });
        grid.addCollapseListener(new CollapseListener<Step>() {
            @Override
            public void itemCollapse(CollapseEvent<Step> event) {
                if (event.isUserOriginated()) {
                    removeChildStepRecursively(grid, event.getCollapsedItem());
                }
            }
        });
        return grid;
    }

    /**
     * Add all child steps directed by the TreeGrid's hierarchical data source.
     */
    private void addChildStepRecursively(TreeGrid<Step> grid, Step itemId, MutableInt index) {
        if (!dataProvider.hasChildren(itemId) || !grid.isExpanded(itemId)) {
            return;
        }
        if (index.getValue() == 0) {
            index.setValue(gantt.getSteps().indexOf(itemId) + 1);
        }
        for (Step child : dataProvider.getTreeData().getChildren(itemId)) {
            gantt.addStep(index.getValue(), child);
            index.increment();
            addChildStepRecursively(grid, child, index);
        }
    }

    /**
     * Remove all child steps directed by the TreeGrid's hierarchical data
     * source.
     */
    private void removeChildStepRecursively(TreeGrid<Step> grid, Step itemId) {
        if (dataProvider.hasChildren(itemId)) {
            for (Step child : dataProvider.getTreeData().getChildren(itemId)) {
                gantt.removeStep(child);
                removeChildStepRecursively(grid, child);
            }
        }
    }

    @Override
    public void stepModified(Step step) {
        if (!dataProvider.getTreeData().contains(step)) {
            dataProvider.getTreeData().addItem(null, step);
            dataProvider.refreshAll();
        } else {
            dataProvider.refreshItem(step);
        }
    }

    @Override
    public void stepDeleted(Step step) {
        removeChildStepRecursively(ganttTreeGrid, step);
        dataProvider.getTreeData().removeItem(step);
        dataProvider.refreshAll();
    }

    @Override
    public void stepMoved(Step step, int newStepIndex, int oldStepIndex) {
        List<Step> path = getParents(step);

        // remove all childrens
        if (dataProvider.hasChildren(step)) {
            removeChildStepRecursively(ganttTreeGrid, step);
            dataProvider.getTreeData().getChildren(step).stream().collect(Collectors.toList())
                    .forEach(dataProvider.getTreeData()::removeItem);
        }

        if (path.size() > 1) {
            Step s = path.remove(path.size() - 1);
            dataProvider.getTreeData().removeItem(s);
            dataProvider.getTreeData().addItem(null, s);
        }
        dataProvider.refreshAll();
    }

    private List<Step> getParents(Step step) {
        Integer parentIndex = ganttTreeGrid.getDataCommunicator().getParentIndex(step);
        List<Step> parentPath = new ArrayList<>();
        parentPath.add(step);
        while (parentIndex != null) {
            Step parentStep = gantt.getStep(parentIndex);
            if (parentStep != null) {
                parentPath.add(0, parentStep);
                parentIndex = ganttTreeGrid.getDataCommunicator().getParentIndex(parentStep);
            } else {
                parentIndex = null;
            }
        }
        return parentPath;
    }

}
