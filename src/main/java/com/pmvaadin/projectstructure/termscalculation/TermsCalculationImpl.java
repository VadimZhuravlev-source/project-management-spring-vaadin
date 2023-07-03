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

        checkCycle(rootItem, path);

        return calculate(rootItem);

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

    private void checkCycle(SimpleLinkedTreeItem treeItem, Map<SimpleLinkedTreeItem, Boolean> path) {

        for (TreeItem<ProjectTask> item: treeItem.getChildren()) {

            if (! (item instanceof SimpleLinkedTreeItem linkedTreeItem)) continue;

            path.put(linkedTreeItem, true);
            checkCycle(linkedTreeItem, path);
            path.remove(linkedTreeItem);

        }

    }

    private Set<ProjectTask> calculate(SimpleLinkedTreeItem rootItem) {

        return new HashSet<>(0);

    }

    private static class SimpleLinkedTreeItem extends SimpleTreeItem<ProjectTask> {

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

    }

    @AllArgsConstructor
    private static class LinkRef {
        private Link value;
        private SimpleLinkedTreeItem refToTreeItem;
    }

    private interface LinkedTreeItem<V, L> extends TreeItem<V> {

        List<? extends LinkedTreeItem<V, L>> getLinks();

    }

}
