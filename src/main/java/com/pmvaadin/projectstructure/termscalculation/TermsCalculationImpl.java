package com.pmvaadin.projectstructure.termscalculation;

import com.pmvaadin.calendars.entity.Calendar;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.TermsPlanningType;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.links.entities.LinkType;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class TermsCalculationImpl implements TermsCalculation {

    private Map<?, Calendar> calendarMap = new HashMap<>();
    private Calendar defaultCalendar;

    @Override
    public Set<ProjectTask> calculate(TermCalculationData termCalculationData) {

        if (termCalculationData.isCycle()) throw new StandardError("Cycle detected in the dependent tasks. Terms calculation is not possible.");

        SimpleLinkedTreeItem rootItem = constructTree(termCalculationData);

        fillParameters(termCalculationData);

        Map<SimpleLinkedTreeItem, Boolean> path = new HashMap<>();
        detectCycle(rootItem, path);

        calculate(rootItem);

        return new HashSet<>(0);

    }

    private void fillParameters(TermCalculationData termCalculationData) {

        calendarMap = termCalculationData.getCalendars().stream()
                .collect(Collectors.toMap(Calendar::getId, calendar -> calendar));
        defaultCalendar = termCalculationData.getDefaultCalendar();

    }

    private SimpleLinkedTreeItem constructTree(TermCalculationData termCalculationData) {

        List<ProjectTask> projectTasks = termCalculationData.getProjectTasks();
        List<Link> links = termCalculationData.getLinks();

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

            if (parentTreeItem == null)
                throw new StandardError("The passed list of project tasks does not include tasks that have matching IDs.");

            treeItem.setParent(parentTreeItem);
            parentTreeItem.getChildren().add(treeItem);

        }

        return rootItem;

    }

    private void detectCycle(SimpleLinkedTreeItem treeItem, Map<SimpleLinkedTreeItem, Boolean> path) {

        for (SimpleLinkedTreeItem item: treeItem.getChildren()) {
            checkCycle(item, path);
        }

        for (LinkRef linkItem: treeItem.links) {
            checkCycle(linkItem.refToTreeItem, path);
        }

    }

    private void checkCycle(SimpleLinkedTreeItem treeItem, Map<SimpleLinkedTreeItem, Boolean> path) {

        if (path.containsKey(treeItem)) throw new StandardError("Detect cycle in the data of term calculation.");

        path.put(treeItem, true);
        detectCycle(treeItem, path);
        path.remove(treeItem);

    }

    private void calculate(SimpleLinkedTreeItem rootItem) {

        Set<ProjectTask> savedTasks = new HashSet<>();
        for (SimpleLinkedTreeItem item: rootItem.getChildren()) {
            calculateRecursively(item, savedTasks);
        }

    }

    private void calculateRecursively(SimpleLinkedTreeItem treeItem, Set<ProjectTask> savedTasks) {

        if (treeItem.isCalculated) return;

        // It is a condition that this task is the last one in the chain of dependencies.
        if (treeItem.getChildren().isEmpty() && treeItem.links.isEmpty()) {
            treeItem.isCalculated = true;
            return;
        }

        ProjectTask currentTask = treeItem.getValue();
        if (currentTask.getTermsPlanningType().equals(TermsPlanningType.MANUAL)) {
            treeItem.isCalculated = true;
            return;
        }

        Date minStartDate = new Date();
        boolean isSumTask = !treeItem.getChildren().isEmpty() || currentTask.getChildrenCount() != 0;
        for (SimpleLinkedTreeItem item: treeItem.getChildren()) {
            calculateRecursively(item, savedTasks);
            ProjectTask task = item.getValue();
            Date startDate = task.getStartDate();
            if (minStartDate.compareTo(startDate) > 0) {
                minStartDate = startDate;
            }
        }

        if (!isSumTask) {
            minStartDate = calculateTermsFromLinks(treeItem.links, currentTask, savedTasks);
        }

        Date startDate = currentTask.getStartDate();
        if (startDate.compareTo(minStartDate) > 0) {
            currentTask.setStartDate(minStartDate);
            savedTasks.add(currentTask);
        }

        treeItem.isCalculated = true;

    }

    private Terms calculateTermsFromLinks(List<LinkRef> links, ProjectTask calculatedTask, Set<ProjectTask> savedTasks) {

        Date minStartDate = new Date();
        Calendar calendar = calendarMap.getOrDefault(calculatedTask.getCalendarId(), defaultCalendar);
        BigDecimal duration = calculatedTask.getDuration();
        for (LinkRef item : links) {
            calculateRecursively(item.refToTreeItem, savedTasks);
            ProjectTask task = item.refToTreeItem.getValue();

            Link link = item.value;
            LinkType linkType = link.getLinkType();
            Date startDate = null;
            Date finishDate = null;
            if (linkType == LinkType.STARTSTART) startDate = task.getStartDate();
            else if (linkType == LinkType.STARTFINISH) finishDate = task.getStartDate();
            else if (linkType == LinkType.FINISHSTART) startDate = task.getFinishDate();
            else if (linkType == LinkType.FINISHFINISH) finishDate = task.getFinishDate();
            else throw new StandardError("Illegal link type of the predecessor: " + task);

            if (startDate == null && finishDate == null)
                throw new StandardError("Illegal start date and finish date of the project task: " + task);

            Date calculatedDate;
            if (startDate == null) calculatedDate = finishDate;
            else calculatedDate = startDate;

            calendar

            if (minStartDate.compareTo(startDate) > 0) {
                minStartDate = startDate;
            }
        }

        return new Terms(s)

    }

    private record Terms(Date startDate, Date finishDate) {}

    private static class SimpleLinkedTreeItem {//extends SimpleTreeItem<ProjectTask> {

        private SimpleLinkedTreeItem parent;
        private boolean isCalculated;
        private ProjectTask value;
        private final List<SimpleLinkedTreeItem> children = new ArrayList<>();

        private final List<LinkRef> links = new ArrayList<>();

        public SimpleLinkedTreeItem() {

        }

        public SimpleLinkedTreeItem(ProjectTask value) {
            this.value = value;
        }

        public List<LinkRef> getLinks() {
            return links;
        }

        public SimpleLinkedTreeItem getParent() {
            return parent;
        }

        public void setParent(SimpleLinkedTreeItem parent) {
            this.parent = parent;
        }

        public boolean isCalculated() {
            return isCalculated;
        }

        public void setCalculated(boolean calculated) {
            isCalculated = calculated;
        }

        public ProjectTask getValue() {
            return value;
        }

        public void setValue(ProjectTask value) {
            this.value = value;
        }

        public List<SimpleLinkedTreeItem> getChildren() {
            return children;
        }
    }

    @AllArgsConstructor
    private static class LinkRef {
        private Link value;
        private SimpleLinkedTreeItem refToTreeItem;
    }

}
