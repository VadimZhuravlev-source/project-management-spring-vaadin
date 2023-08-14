package com.pmvaadin.projectstructure.termscalculation;

import com.pmvaadin.calendars.entity.Calendar;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ScheduleMode;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.links.entities.LinkType;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class TermsCalculationImpl implements TermsCalculation {

    private Map<?, Calendar> mapIdCalendar = new HashMap<>();
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

        List<Calendar> calendars = termCalculationData.getCalendars();
        calendars.forEach(Calendar::initiateCacheData);

        mapIdCalendar = calendars.stream()
                .collect(Collectors.toMap(Calendar::getId, c -> c));

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
        if (currentTask.getScheduleMode().equals(ScheduleMode.MANUALLY)) {
            treeItem.isCalculated = true;
            return;
        }

        LocalDateTime nullTime = getNullDateTime();
        LocalDateTime minStartDate = nullTime;
        LocalDateTime maxFinishDate = nullTime;
        boolean isSumTask = !treeItem.getChildren().isEmpty() || currentTask.getChildrenCount() != 0;
        for (SimpleLinkedTreeItem item: treeItem.getChildren()) {
            // a method below are also called in the method calculateStartDateFromLinks
            calculateRecursively(item, savedTasks);
            ProjectTask task = item.getValue();
            LocalDateTime startDate = task.getStartDate();
            LocalDateTime finishDate = task.getFinishDate();
            if (minStartDate.compareTo(startDate) > 0) {
                minStartDate = startDate;
            }
            if (maxFinishDate.compareTo(finishDate) < 0) {
                maxFinishDate = finishDate;
            }
        }

        if (!treeItem.getChildren().isEmpty() && minStartDate != nullTime && maxFinishDate != nullTime) {
            currentTask.setStartDate(minStartDate);
            currentTask.setFinishDate(maxFinishDate);
            Calendar calendar = mapIdCalendar.getOrDefault(currentTask.getCalendarId(), defaultCalendar);
            long duration = calendar.getDurationWithoutInitiateCache(minStartDate, maxFinishDate);
            currentTask.setDuration(duration);
        }

        if (!isSumTask) {
            minStartDate = calculateStartDateFromLinks(treeItem.links, currentTask, savedTasks);
            if (minStartDate.compareTo(nullTime) != 0) {
                Calendar calendar = mapIdCalendar.getOrDefault(currentTask.getCalendarId(), defaultCalendar);
                maxFinishDate = calendar.getDateByDurationWithoutInitiateCache(minStartDate, currentTask.getDuration());
                currentTask.setStartDate(minStartDate);
                currentTask.setFinishDate(maxFinishDate);
            }
        }

        treeItem.isCalculated = true;

    }

    private LocalDateTime calculateStartDateFromLinks(List<LinkRef> links, ProjectTask calculatedTask, Set<ProjectTask> savedTasks) {

        LocalDateTime maxStartDate = getNullDateTime();
        Calendar calendar = mapIdCalendar.getOrDefault(calculatedTask.getCalendarId(), defaultCalendar);
        long duration = calculatedTask.getDuration();
        for (LinkRef item : links) {
            calculateRecursively(item.refToTreeItem, savedTasks);
            ProjectTask linkedTask = item.refToTreeItem.getValue();

            Link link = item.value;
            LinkType linkType = link.getLinkType();
            LocalDateTime startDate;
            if (linkType == LinkType.STARTSTART) {
                startDate = calendar.getDateByDurationWithoutInitiateCache(linkedTask.getStartDate(), link.getLag());
            }
            else if (linkType == LinkType.STARTFINISH) {
                startDate = calendar.getDateByDurationWithoutInitiateCache(linkedTask.getStartDate(), -duration + link.getLag());
            }
            else if (linkType == LinkType.FINISHSTART) {
                startDate = calendar.getDateByDurationWithoutInitiateCache(linkedTask.getFinishDate(), link.getLag());
            }
            else if (linkType == LinkType.FINISHFINISH) {
                startDate = calendar.getDateByDurationWithoutInitiateCache(linkedTask.getFinishDate(), duration + link.getLag());
            }
            else throw new StandardError("Illegal link type of the predecessor: " + linkedTask);

            if (maxStartDate.compareTo(startDate) < 0) {
                maxStartDate = startDate;
            }

        }

        return maxStartDate;

    }

    private LocalDateTime getNullDateTime() {
        return LocalDateTime.of(0, 1, 1, 0, 0);
    }

    // Classes

    private static class SimpleLinkedTreeItem {

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
