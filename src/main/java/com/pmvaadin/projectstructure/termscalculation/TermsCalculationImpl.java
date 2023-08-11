package com.pmvaadin.projectstructure.termscalculation;

import com.pmvaadin.calendars.dayofweeksettings.DayOfWeekSettings;
import com.pmvaadin.calendars.dayofweeksettings.DefaultDaySetting;
import com.pmvaadin.calendars.entity.Calendar;
import com.pmvaadin.calendars.entity.CalendarSettings;
import com.pmvaadin.calendars.exceptiondays.ExceptionDays;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ScheduleMode;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.links.entities.LinkType;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class TermsCalculationImpl implements TermsCalculation {

    private Map<?, CalendarData> calendarsData = new HashMap<>();
    private CalendarData defaultCalendar;

    private final DateComputation dateComputation = new DateComputation();

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

        calendarsData = termCalculationData.getCalendars().stream()
                .collect(Collectors.toMap(Calendar::getId, this::getCalendarData));

        defaultCalendar = getCalendarData(termCalculationData.getDefaultCalendar());

    }

    private CalendarData getCalendarData(Calendar calendar) {

        List<ExceptionDays> exceptionDaysList = calendar.getCalendarException();

        List<DefaultDaySetting> settingList;
        if (calendar.getSetting() == CalendarSettings.DAYSOFWEEKSETTINGS) {
            List<DayOfWeekSettings> list = calendar.getDaysOfWeekSettings();
            settingList = list.stream().map(d -> new DefaultDaySetting(d.getDayOfWeek(), d.getCountHours())).toList();
        } else {
            settingList = calendar.getSetting().getDefaultDaySettings();
        }

        Map<LocalDate, Integer> mapExceptions =
                exceptionDaysList.stream().collect(
                        Collectors.toMap(ExceptionDays::getDate, ExceptionDays::getDuration)
                );

        BigDecimal startTime = calendar.getStartTime();
        BigDecimal secondInHour = new BigDecimal(3600);
        int secondFromBeggingOfDay = startTime.multiply(secondInHour).intValue();

        return new CalendarData(secondFromBeggingOfDay, settingList, mapExceptions);

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

        LocalDateTime minStartDate = LocalDateTime.of(0, 0, 0, 0, 0, 0);
        boolean isSumTask = !treeItem.getChildren().isEmpty() || currentTask.getChildrenCount() != 0;
        for (SimpleLinkedTreeItem item: treeItem.getChildren()) {
            calculateRecursively(item, savedTasks);
            ProjectTask task = item.getValue();
            LocalDateTime startDate = task.getStartDate();
            if (minStartDate.compareTo(startDate) > 0) {
                minStartDate = startDate;
            }
        }

        if (!isSumTask) {
            minStartDate = calculateStartDateFromLinks(treeItem.links, currentTask, savedTasks);
        }

        LocalDateTime startDate = currentTask.getStartDate();
        if (startDate.compareTo(minStartDate) > 0) {
            currentTask.setStartDate(minStartDate);
            savedTasks.add(currentTask);
        }

        treeItem.isCalculated = true;

    }

    private LocalDateTime calculateStartDateFromLinks(List<LinkRef> links, ProjectTask calculatedTask, Set<ProjectTask> savedTasks) {

        LocalDateTime maxStartDate = LocalDateTime.of(0, 0, 0, 0, 0);
        CalendarData calendarData = calendarsData.getOrDefault(calculatedTask.getCalendarId(), defaultCalendar);
        long duration = calculatedTask.getDuration();
        for (LinkRef item : links) {
            calculateRecursively(item.refToTreeItem, savedTasks);
            ProjectTask linkedTask = item.refToTreeItem.getValue();

            Link link = item.value;
            LinkType linkType = link.getLinkType();
            LocalDateTime startDate;
            if (linkType == LinkType.STARTSTART) {
                startDate = calculateDate(calendarData, linkedTask.getStartDate(), link.getLag());
            }
            else if (linkType == LinkType.STARTFINISH) {
                startDate = calculateDate(calendarData, linkedTask.getStartDate(), -duration + link.getLag());
            }
            else if (linkType == LinkType.FINISHSTART) {
                startDate = calculateDate(calendarData, linkedTask.getFinishDate(), link.getLag());
            }
            else if (linkType == LinkType.FINISHFINISH) {
                startDate = calculateDate(calendarData, linkedTask.getFinishDate(), duration + link.getLag());
            }
            else throw new StandardError("Illegal link type of the predecessor: " + linkedTask);

            if (maxStartDate.compareTo(startDate) < 0) {
                maxStartDate = startDate;
            }

        }

        return maxStartDate;

    }

    private LocalDateTime calculateDate(CalendarData calendarData, LocalDateTime date, long duration) {

        if (duration == 0L) return LocalDateTime.of(
                LocalDate.ofEpochDay(date.toLocalDate().toEpochDay()),
                LocalTime.ofSecondOfDay(date.toLocalTime().toSecondOfDay())
        );

        boolean isAscend = duration > 0L;

        dateComputation.setDateComputation(date, duration, calendarData);
        LocalDateTime startDate;
        if (isAscend) {
            startDate = dateComputation.increaseDateByDuration();
        } else {
            startDate = dateComputation.decreaseDateByDuration();
        }

        return startDate;

    }

    private long getDuration(LocalDateTime start, LocalDateTime finish, CalendarData calendarData) {

        if (start.compareTo(finish) > 0) throw new StandardError("Illegal argument exception");

        LocalDate startDay = LocalDate.ofEpochDay(start.toLocalDate().toEpochDay());
        LocalTime startTime = LocalTime.ofSecondOfDay(start.toLocalTime().toSecondOfDay());

        ComputingDataOfWorkingDay computingDataOfWorkingDay =
                new ComputingDataOfWorkingDay(startDay, calendarData, start.getDayOfWeek().getValue());

        int startTimeCalendar = calendarData.startTime();
        int finishTimeCalendar = startTimeCalendar + computingDataOfWorkingDay.getNumberOfSecondsInWorkingTime();

        long duration = finishTimeCalendar - startTime.toSecondOfDay();

        LocalDate finishDay = finish.toLocalDate();
        while (startDay.compareTo(finishDay) < 0) {
            computingDataOfWorkingDay.increaseDay();
            duration =+ computingDataOfWorkingDay.getNumberOfSecondsInWorkingTime();
        }

        if (startTimeCalendar < finish.toLocalTime().toSecondOfDay()) {
            duration =+ finish.toLocalTime().toSecondOfDay() - startTimeCalendar;
        }

        return duration;

    }

    // classes

    private class DateComputation {

        private LocalDateTime date;
        private long duration;
        private CalendarData calendarData;

        // general variables
        private LocalDate day;
        private LocalTime time;
        private ComputingDataOfWorkingDay computingDataOfWorkingDay;
        private int startTime;
        private int finishTime;
        private long remainderOfDuration;
        private int secondOfDay;

        public void setDateComputation(LocalDateTime date, long duration,
                        CalendarData calendarData) {
            this.date = date;
            this.duration = duration;
            this.calendarData = calendarData;
        }

        public LocalDateTime increaseDateByDuration() {

            if (duration < 0) throw new StandardError("Illegal argument exception");

            initiate();

            if (secondOfDay >= finishTime) {
                computingDataOfWorkingDay.increaseDay();
                time = LocalTime.ofSecondOfDay(startTime);
            } else if (secondOfDay <= startTime) {
                time = LocalTime.ofSecondOfDay(startTime);
            } else {
                if (secondOfDay + remainderOfDuration < finishTime) {
                    time.plusSeconds(remainderOfDuration);
                    return LocalDateTime.of(day, time);
                } else {
                    computingDataOfWorkingDay.increaseDay();
                    time = LocalTime.ofSecondOfDay(startTime);
                    int numberOfSecondUntilEndOfWorkingDay = finishTime - secondOfDay;
                    remainderOfDuration =- numberOfSecondUntilEndOfWorkingDay;
                }
            }

            // increase/decrease days
            int numberOfSecondsInWorkingDay;
            do {
                numberOfSecondsInWorkingDay = computingDataOfWorkingDay.getNumberOfSecondsInWorkingTime();
                remainderOfDuration =- numberOfSecondsInWorkingDay;
                if (remainderOfDuration > 0L) {
                    computingDataOfWorkingDay.increaseDay();
                }

            } while (remainderOfDuration > 0L);

            if (remainderOfDuration == 0) return LocalDateTime.of(day, time);

            // reclaim remainderOfDuration to add it to the time
            remainderOfDuration =+ numberOfSecondsInWorkingDay;

            time.plusSeconds(remainderOfDuration);

            return LocalDateTime.of(day, time);

        }

        public LocalDateTime decreaseDateByDuration() {

            if (duration < 0) throw new StandardError("Illegal argument");

            initiate();

            if (secondOfDay <= startTime) {
                computingDataOfWorkingDay.decreaseDay();
                time = LocalTime.ofSecondOfDay(finishTime);
            } else if (secondOfDay >= finishTime) {
                time = LocalTime.ofSecondOfDay(finishTime);
            } else {
                if (secondOfDay + remainderOfDuration > startTime) {
                    time.minusSeconds(-remainderOfDuration);
                    return LocalDateTime.of(day, time);
                } else {
                    computingDataOfWorkingDay.decreaseDay();
                    time = LocalTime.ofSecondOfDay(finishTime);
                    int numberOfSecondUntilEndOfWorkingDay = startTime - secondOfDay;
                    // numberOfSecondUntilEndOfWorkingDay < 0 by the condition above, so after
                    // the operation below is completed, remainderOfDuration will be increased
                    remainderOfDuration =- numberOfSecondUntilEndOfWorkingDay;
                }
            }

            // increase/decrease days
            int numberOfSecondsInWorkingDay;
            do {
                numberOfSecondsInWorkingDay = computingDataOfWorkingDay.getNumberOfSecondsInWorkingTime();
                remainderOfDuration =+ numberOfSecondsInWorkingDay;
                if (remainderOfDuration < 0L) {
                    computingDataOfWorkingDay.decreaseDay();
                }

            } while (remainderOfDuration < 0L);

            if (remainderOfDuration == 0) return LocalDateTime.of(day, time);

            // reclaim remainderOfDuration to add it to the time
            remainderOfDuration =- numberOfSecondsInWorkingDay;

            time.minusSeconds(-remainderOfDuration);

            return LocalDateTime.of(day, time);

        }

        private void initiate() {

            day = LocalDate.ofEpochDay(date.toLocalDate().toEpochDay());
            time = LocalTime.ofSecondOfDay(date.toLocalTime().toSecondOfDay());

            computingDataOfWorkingDay =
                    new ComputingDataOfWorkingDay(day, calendarData, date.getDayOfWeek().getValue());

            startTime = calendarData.startTime();
            finishTime = startTime + computingDataOfWorkingDay.getNumberOfSecondsInWorkingTime();

            remainderOfDuration = duration;
            secondOfDay = time.toSecondOfDay();

        }

    }

    private class ComputingDataOfWorkingDay {

        private int index;
        private LocalDate day;
        private CalendarData calendarData;

        ComputingDataOfWorkingDay(LocalDate day, CalendarData calendarData, int dayOfWeek) {

            this.day = day;
            this.calendarData = calendarData;

            List<DefaultDaySetting> durationOfDaysOfWeek = calendarData.amountOfHourInDay();
            int index = 0;
            for (int i = 0; i < durationOfDaysOfWeek.size(); i++) {
                if (durationOfDaysOfWeek.get(i).dayOfWeek() == dayOfWeek) {
                    index = i;
                    break;
                }
            }
            this.index = index;

        }

        public void increaseDay() {
            index++;
            if (index == calendarData.amountOfHourInDay().size()) index = 0;
            day.plusDays(1L);
        }

        public void decreaseDay() {
            if (index == 0) index = calendarData.amountOfHourInDay().size();
            index--;
            day = day.minusDays(1L);
        }

        public int getNumberOfSecondsInWorkingTime() {

            int durationDay = calendarData.amountOfHourInDay().get(index).countSeconds();
            Integer durationOfException = calendarData.exceptionDays().get(day);

            int numberOfSecondsInDay;
            if (durationOfException != null) {
                numberOfSecondsInDay = durationOfException;
            } else {
                numberOfSecondsInDay = durationDay;
            }

            return numberOfSecondsInDay;

        }

    }

    private record CalendarData(int startTime, List<DefaultDaySetting> amountOfHourInDay, Map<LocalDate, Integer> exceptionDays) {}

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
