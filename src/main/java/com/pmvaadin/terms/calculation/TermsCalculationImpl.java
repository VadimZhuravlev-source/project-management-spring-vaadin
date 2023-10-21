package com.pmvaadin.terms.calculation;

import com.pmvaadin.terms.calendars.entity.Calendar;
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
    private boolean calculateFinishAlways;

    @Override
    public TermCalculationRespond calculate(TermCalculationData termCalculationData) {

        if (termCalculationData.isCycle()) throw new StandardError("Cycle detected in the dependent tasks. Terms calculation is not possible.");

        SimpleLinkedTreeItem rootItem = constructTree(termCalculationData);
        fillParameters(termCalculationData);

        detectCycle(rootItem);
        TermCalculationRespond termCalculationRespond = calculate(rootItem);

        mapIdCalendar.clear();
        defaultCalendar = null;

        return termCalculationRespond;

    }

    private void fillParameters(TermCalculationData termCalculationData) {

        List<Calendar> calendars = termCalculationData.getCalendars();
        calendars.forEach(Calendar::initiateCacheData);

        mapIdCalendar = calendars.stream()
                .collect(Collectors.toMap(Calendar::getId, c -> c));

        defaultCalendar = termCalculationData.getDefaultCalendar();
        calculateFinishAlways = termCalculationData.isCalculateFinishAlways();

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
                continue;
//                throw new StandardError("The passed list of project tasks does not include tasks that have matching IDs.");

            treeItem.setParent(parentTreeItem);
            parentTreeItem.getChildren().add(treeItem);

        }

        return rootItem;

    }

    private void detectCycle(SimpleLinkedTreeItem treeItem) {

        Map<SimpleLinkedTreeItem, Boolean> path = new HashMap<>();
        detectCycleRecursively(treeItem, path);

    }

    private void detectCycleRecursively(SimpleLinkedTreeItem treeItem, Map<SimpleLinkedTreeItem, Boolean> path) {

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
        detectCycleRecursively(treeItem, path);
        path.remove(treeItem);

    }

    private TermCalculationRespond calculate(SimpleLinkedTreeItem rootItem) {

        Set<ProjectTask> savedTasks = new HashSet<>();
        Set<ProjectTask> projectsForRecalculation = new HashSet<>();
        for (SimpleLinkedTreeItem item: rootItem.getChildren()) {
            calculateRecursively(item, savedTasks, projectsForRecalculation);
        }

        for (SimpleLinkedTreeItem item: rootItem.getChildren()) {
            ProjectTask project = item.getValue();
            if (!projectsForRecalculation.contains(project)) continue;
            changeStartDateFromProjectRecursively(item, project.getStartDate(), savedTasks, projectsForRecalculation);
        }

        // recalculate again if it is necessary
        if (!projectsForRecalculation.isEmpty()) {
            // at first, there is a need for clearing of "isCalculated" field in all tree elements
            clearIsCalculatedRecursively(rootItem);
            for (SimpleLinkedTreeItem item : rootItem.getChildren()) {
                calculateRecursively(item, savedTasks, projectsForRecalculation);
            }
        }

        return new TermCalculationRespondImpl(savedTasks, projectsForRecalculation);

    }

    private void calculateRecursively(SimpleLinkedTreeItem treeItem, Set<ProjectTask> savedTasks,
                                      Set<ProjectTask> projectsForRecalculation) {

        if (treeItem.isCalculated) return;

        ProjectTask currentTask = treeItem.getValue();
        fillFinish(currentTask);

        // It is a condition that this task is the last one in the chain of dependencies.
        if (treeItem.getChildren().isEmpty() && treeItem.links.isEmpty()) {
            treeItem.isCalculated = true;
            return;
        }

        if (currentTask.getScheduleMode().equals(ScheduleMode.MANUALLY)) {
            treeItem.isCalculated = true;
            return;
        }

        //LocalDateTime nullTime = getNullDateTime();
        LocalDateTime minStartDate = LocalDateTime.MAX;
        LocalDateTime maxFinishDate = LocalDateTime.MIN;
        boolean isChildren = currentTask.getAmountOfChildren() != 0;
        boolean isSumTask = !treeItem.getChildren().isEmpty() || isChildren;
        for (SimpleLinkedTreeItem item: treeItem.getChildren()) {
            // a method below are also called in the method calculateStartDateFromLinks
            calculateRecursively(item, savedTasks, projectsForRecalculation);
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

        Calendar calendar = mapIdCalendar.getOrDefault(currentTask.getCalendarId(), defaultCalendar);
        if (!treeItem.getChildren().isEmpty() &&
                minStartDate != LocalDateTime.MAX && maxFinishDate != LocalDateTime.MIN
                && (!minStartDate.equals(currentTask.getStartDate()) || !maxFinishDate.equals(currentTask.getFinishDate()))) {
            if (currentTask.getParentId() == null || currentTask.isProject()) {
                projectsForRecalculation.add(currentTask);
            }
            currentTask.setStartDate(minStartDate);
            currentTask.setFinishDate(maxFinishDate);
            long duration = calendar.getDurationWithoutInitiateCache(minStartDate, maxFinishDate);
            currentTask.setDuration(duration);
            savedTasks.add(currentTask);
        }

        if (!isSumTask) {
            minStartDate = calculateStartDateFromLinks(treeItem.links, currentTask, savedTasks, projectsForRecalculation);
            if (!minStartDate.equals(LocalDateTime.MIN) && !minStartDate.equals(currentTask.getStartDate())) {
                minStartDate = calendar.getClosestWorkingDayWithoutInitiateCache(minStartDate);
                maxFinishDate = calendar.getDateByDurationWithoutInitiateCache(minStartDate, currentTask.getDuration());
                currentTask.setStartDate(minStartDate);
                currentTask.setFinishDate(maxFinishDate);
                savedTasks.add(currentTask);
            }
        }

        treeItem.isCalculated = true;

    }

    private LocalDateTime calculateStartDateFromLinks(List<LinkRef> links, ProjectTask calculatedTask,
                                                      Set<ProjectTask> savedTasks, Set<ProjectTask> projectsForRecalculation) {

        LocalDateTime maxStartDate = LocalDateTime.MIN;
        Calendar calendar = mapIdCalendar.getOrDefault(calculatedTask.getCalendarId(), defaultCalendar);
        long duration = calculatedTask.getDuration();
        for (LinkRef item : links) {
            calculateRecursively(item.refToTreeItem, savedTasks, projectsForRecalculation);
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

    private void changeStartDateFromProjectRecursively(SimpleLinkedTreeItem treeItem, LocalDateTime newDate, Set<ProjectTask> savedTasks,
                                            Set<ProjectTask> projects) {

        ProjectTask currentTask = treeItem.getValue();

        for (SimpleLinkedTreeItem item: treeItem.getChildren()) {
            changeStartDateFromProjectRecursively(item, newDate, savedTasks, projects);
        }

        if (currentTask.getScheduleMode().equals(ScheduleMode.MANUALLY)) return;
        if (currentTask.getAmountOfChildren() != 0) return;
        if (currentTask.getStartDate().equals(newDate)) return;

        currentTask.setStartDate(newDate);
        Calendar calendar = mapIdCalendar.getOrDefault(currentTask.getCalendarId(), defaultCalendar);
        LocalDateTime newFinishDate = calendar.getDateByDurationWithoutInitiateCache(newDate, currentTask.getDuration());
        currentTask.setFinishDate(newFinishDate);
        savedTasks.add(currentTask);

        if (currentTask.getParentId() == null || currentTask.isProject()) projects.add(currentTask);

    }

    private void clearIsCalculatedRecursively(SimpleLinkedTreeItem item) {

        item.isCalculated = false;
        for (SimpleLinkedTreeItem treeItem : item.getChildren()) {
            clearIsCalculatedRecursively(treeItem);
        }

    }

    private void fillFinish(ProjectTask currentTask) {

        if (!calculateFinishAlways) {
            return;
        }

        Calendar calendar = mapIdCalendar.getOrDefault(currentTask.getCalendarId(), defaultCalendar);
        LocalDateTime start = currentTask.getStartDate();
        long duration = currentTask.getDuration();
        LocalDateTime finish = currentTask.getFinishDate();
        LocalDateTime calculatedFinish = calendar.getDateByDurationWithoutInitiateCache(start, duration);
        if (!finish.equals(calculatedFinish)) currentTask.setFinishDate(calculatedFinish);

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
