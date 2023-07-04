package com.pmvaadin.projectstructure.termscalculation;

import com.pmvaadin.commonobjects.tree.SimpleTreeItem;
import com.pmvaadin.commonobjects.tree.TreeItem;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projecttasks.dependencies.DependenciesSet;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class TermsCalculationImpl implements TermsCalculation {

    @Override
    public Set<ProjectTask> calculate(DependenciesSet dependenciesSet) {

        if (dependenciesSet.isCycle()) throw new StandardError("Cycle detected in the dependent tasks. Terms calculation is not possible.");

        SimpleLinkedTreeItem rootItem = constructTree(dependenciesSet);

        int count = dependenciesSet.getProjectTasks().size() + dependenciesSet.getLinks().size();
        Map<SimpleLinkedTreeItem, Boolean> path = new HashMap<>(count);

        detectCycle(rootItem, path);

        calculate(rootItem);

        return new HashSet<>(0);

    }

    private SimpleLinkedTreeItem constructTree(DependenciesSet dependenciesSet) {

        List<ProjectTask> projectTasks = dependenciesSet.getProjectTasks();
        List<Link> links = dependenciesSet.getLinks();

        Map<?, SimpleLinkedTreeItem> mapProjectTasks = projectTasks.stream()
                .collect(Collectors.toMap(ProjectTask::getId, SimpleLinkedTreeItem::new));

        SimpleLinkedTreeItem rootItem = new SimpleLinkedTreeItem();

        // fill links
        for (Link link: links) {

            SimpleLinkedTreeItem treeItem = mapProjectTasks.get(link.getLinkedProjectTaskId());
            SimpleLinkedTreeItem parentTreeItem = mapProjectTasks.get(link.getProjectTaskId());

            if (treeItem == null || parentTreeItem == null)
                throw new StandardError("The passed list of project tasks does not include tasks that have corresponding links with matching IDs.");

            LinkRef linkRef = new LinkRef(link, treeItem);
            parentTreeItem.getLinks().add(linkRef);

        }

        // fill children
        for (ProjectTask projectTask: projectTasks) {

            SimpleLinkedTreeItem treeItem = mapProjectTasks.get(projectTask.getId());

            SimpleLinkedTreeItem parentTreeItem;
            if (projectTask.getParentId() != null)
                parentTreeItem = mapProjectTasks.get(projectTask.getParentId());
            else {
                parentTreeItem = rootItem;
            }

            if (parentTreeItem == null) throw new StandardError("The passed list of project tasks does not include tasks that have matching IDs.");

            treeItem.setParent(parentTreeItem);
            parentTreeItem.getChildren().add(treeItem);

        }

        return rootItem;

    }

    private void detectCycle(SimpleLinkedTreeItem treeItem, Map<SimpleLinkedTreeItem, Boolean> path) {

        for (TreeItem<ProjectTask> item: treeItem.getChildren()) {
            if (! (item instanceof SimpleLinkedTreeItem linkedTreeItem)) continue;
            checkCycle(linkedTreeItem, path);
        }

        for (LinkRef item: treeItem.links) {
            checkCycle(item.refToTreeItem, path);
        }

    }

    private void checkCycle(SimpleLinkedTreeItem treeItem, Map<SimpleLinkedTreeItem, Boolean> path) {

        if (path.containsKey(treeItem)) throw new StandardError("Detect cycle in the data of term calculation.");

        path.put(treeItem, true);
        detectCycle(treeItem, path);
        path.remove(treeItem);

    }

    private void calculate(SimpleLinkedTreeItem treeItem) {

        if (treeItem.isCalculated) return;

        Date minStartDate = new Date();
        //Date maxFinishDate = new Date();
        for (TreeItem<ProjectTask> item: treeItem.getChildren()) {
            if (!(item instanceof SimpleLinkedTreeItem linkedTreeItem)) continue;
            calculate(linkedTreeItem);
            ProjectTask task = treeItem.getValue();
            Date startDate = task.getStartDate();
            if (minStartDate.compareTo(startDate) > 0) {
                minStartDate = startDate;
            }
//            Date finishDate = task.getFinishDate();
//            if (maxFinishDate.compareTo(finishDate) < 0) {
//                maxFinishDate = finishDate;
//            }
        }

        for (LinkRef item: treeItem.links) {
            calculate(item.refToTreeItem);
        }

        ProjectTask currentTask = treeItem.getValue();





    }

    private static class SimpleLinkedTreeItem extends SimpleTreeItem<ProjectTask> {

        private boolean isCalculated;

        private final List<LinkRef> links = new ArrayList<>();

        public SimpleLinkedTreeItem() {
            super();
        }

        public SimpleLinkedTreeItem(ProjectTask value) {
            super(value);
        }

        public List<LinkRef> getLinks() {
            return links;
        }

        public void setCalculated(boolean isCalculated) {
            this.isCalculated = isCalculated;
        }

        public boolean getCalculated() {
            return this.isCalculated;
        }

    }

    @AllArgsConstructor
    private static class LinkRef {
        private Link value;
        private SimpleLinkedTreeItem refToTreeItem;
    }

}
