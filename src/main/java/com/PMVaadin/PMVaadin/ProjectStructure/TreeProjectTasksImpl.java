package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.ProjectTask.ProjectTask;
import com.PMVaadin.PMVaadin.Tree.SimpleTree;
import com.PMVaadin.PMVaadin.Tree.SimpleTreeItem;
import com.PMVaadin.PMVaadin.Tree.Tree;
import com.PMVaadin.PMVaadin.Tree.TreeItem;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class TreeProjectTasksImpl implements TreeProjectTasks {

    private TreeItem<ProjectTask> rootItem;
    private List<TreeItem<ProjectTask>> treeItems = new ArrayList<>();
    private final Validations validations = new ValidationsImpl();
    private final Unloopable unloopable = new UnloopableImpl();

    public TreeProjectTasksImpl() {

    }

    @Override
    public void fillWbs() {

        fillWbsRecursively(rootItem.getChildren(), "");

    }

    @Override
    public void populateTreeByList(List<ProjectTask> projectTasks) {

        Tree<ProjectTask> tree = new SimpleTree<>(projectTasks, ProjectTask::getId, ProjectTask::getParentId);
        this.treeItems = tree.getTreeItems();
        this.rootItem = tree.getRootItem();

    }

    @Override
    public void validateTree(){

        unloopable.detectCycle(treeItems);
        validations.checkQuantitiesTreeItemInTree(rootItem, treeItems);

    }

    @Override
    public TreeItem<ProjectTask> getRootItem() {
        return rootItem;
    }

    @Override
    public List<ProjectTask> recalculateThePropertiesOfTheWholeProject() {

        return getProjectTasksWithChangedLevelOrder(this.rootItem);

    }

    @Override
    public List<ProjectTask> recalculateLevelOrderForProjectTasks(List<ProjectTask> projectTasks) {

        Map<?, List<ProjectTask>> groupedList =
                projectTasks.stream().collect(Collectors.groupingBy(p -> {
                    if (p.getParentId() == null) return p.getNullId(); return p.getParentId();
                    },
                        Collectors.toList()));

        List<ProjectTask> savedTasks = new ArrayList<>();
        for (Map.Entry<?, List<ProjectTask>> kv: groupedList.entrySet()) {

            TreeItem<ProjectTask> parent = new SimpleTreeItem<>();
            for (ProjectTask projectTask: kv.getValue()) {
                TreeItem<ProjectTask> children = new SimpleTreeItem<>(projectTask);
                parent.getChildren().add(children);
                children.setParent(parent);
            }
            calculateLevelOrderRecursively(parent, savedTasks);

        }

        return savedTasks;

    }

    private void fillWbsRecursively(List<TreeItem<ProjectTask>> children, String previousWbs) {

        for (TreeItem<ProjectTask> child: children) {

            ProjectTask hierarchyElement = child.getValue();
            String newWbs = previousWbs + hierarchyElement.getLevelOrder().toString();
            hierarchyElement.setWbs(newWbs);
            fillWbsRecursively(child.getChildren(), newWbs + ".");

        }

    }

    private List<ProjectTask> getProjectTasksWithChangedLevelOrder(TreeItem<ProjectTask> rootItem){

        List<ProjectTask> savedTasks = new ArrayList<>();
        calculateLevelOrderRecursively(rootItem, savedTasks);

        return savedTasks;

    }

    private void calculateLevelOrderRecursively(TreeItem<ProjectTask> rootItem, List<ProjectTask> savedTasks){

        Integer iterator = 1;
        for (TreeItem<ProjectTask> treeItem: rootItem.getChildren()) {

            ProjectTask projectTask = treeItem.getValue();
            if (!(iterator).equals(projectTask.getLevelOrder())) {
                projectTask.setLevelOrder(iterator);
                savedTasks.add(projectTask);
            }
            iterator++;
            calculateLevelOrderRecursively(treeItem, savedTasks);
        }

    }

}
