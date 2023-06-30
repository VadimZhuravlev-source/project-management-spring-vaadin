package com.pmvaadin.projectstructure.termscalculation;

import com.pmvaadin.commonobjects.tree.SimpleTreeItem;
import com.pmvaadin.commonobjects.tree.TreeItem;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projecttasks.dependencies.DependenciesSet;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;
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

        LinkedTreeItem<ProjectTask, Link> rootItem = constructTree(dependenciesSet);

        return calculate(rootItem);

    }

    private LinkedTreeItem<ProjectTask, Link> constructTree(DependenciesSet dependenciesSet) {

        List<ProjectTask> projectTasks = dependenciesSet.getProjectTasks();
        List<Link> links = dependenciesSet.getLinks();

        Map<?, LinkedTreeItem<ProjectTask, Link>> mapProjectTasks = projectTasks.stream()
                .collect(Collectors.toMap(ProjectTask::getId, SimpleLinkedTreeItem::new));

        LinkedTreeItem<ProjectTask, Link> rootItem = new SimpleLinkedTreeItem<>();

        // fill links
        for (Link link: links) {

            LinkedTreeItem<ProjectTask, Link> treeItem = mapProjectTasks.get(link.getLinkedProjectTaskId());
            LinkedTreeItem<ProjectTask, Link> parentTreeItem = mapProjectTasks.get(link.getProjectTaskId());

            if (treeItem == null || parentTreeItem == null)
                throw new StandardError("The passed list of project tasks does not include tasks that have corresponding links with matching IDs.");

            parentTreeItem.getLinks().add(treeItem);

        }

        // fill children
        for (ProjectTask projectTask: projectTasks) {

            LinkedTreeItem<ProjectTask, Link> treeItem = mapProjectTasks.get(projectTask.getId());

            LinkedTreeItem<ProjectTask, Link> parentTreeItem;
            if (projectTask.getParentId() != null)
                parentTreeItem = mapProjectTasks.get(projectTask.getParentId());
            else {
                parentTreeItem = rootItem;
            }

            treeItem.setParent(parentTreeItem);
            parentTreeItem.getChildren().add(treeItem);

        }

        return rootItem;

    }

    private Set<ProjectTask> calculate(LinkedTreeItem<ProjectTask, Link> rootItem) {

        return new HashSet<>(0);

    }

    private static class SimpleLinkedTreeItem<V, L> extends SimpleTreeItem<V> implements LinkedTreeItem<V, L> {

        private final List<LinkedTreeItem<V, L>> links = new ArrayList<>();

        public SimpleLinkedTreeItem() {
            super();
        }

        public SimpleLinkedTreeItem(V value) {
            super(value);
        }

        public List<LinkedTreeItem<V, L>> getLinks() {
            return links;
        }

    }

    private interface LinkedTreeItem<V, L> extends TreeItem<V> {

        List<LinkedTreeItem<V, L>> getLinks();

    }

}
