package com.pmvaadin.projectstructure.termscalculation;

import com.pmvaadin.terms.calculation.TermCalculationData;
import com.pmvaadin.terms.calculation.TermCalculationDataImpl;
import com.pmvaadin.terms.calculation.TermsCalculation;
import com.pmvaadin.terms.calculation.TermsCalculationImpl;
import com.pmvaadin.terms.calendars.common.Interval;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.entity.CalendarImpl;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import com.pmvaadin.projecttasks.entity.ScheduleMode;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.links.entities.LinkImpl;
import com.pmvaadin.projecttasks.links.entities.LinkType;
import com.pmvaadin.terms.calendars.exceptions.CalendarException;
import com.pmvaadin.terms.calendars.exceptions.CalendarExceptionImpl;
import com.pmvaadin.terms.calendars.exceptions.CalendarExceptionSetting;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

class TermsCalculationImplTest {

    private final int secondInHour = 3600;
    private final int durationDay = 3600 * 8;

    @Test
    void calculate() {

        TermsCalculation termsCalculation = new TermsCalculationImpl();
        TermCalculationData termData = initiateTermCalculationData();
        termsCalculation.calculate(termData);

        Map<?, ProjectTask> map = termData.getProjectTasks().stream().collect(
                Collectors.toMap(ProjectTask::getId, p -> p));

        ProjectTask projectTask = map.get(6);
        LocalDateTime start = LocalDateTime.of(2022, 1, 27, 9, 0);
        assertEquals(start, projectTask.getStartDate());

        // if we change start date of the task with manually schedule mode, almost the all task is changed
        // TODO Have to add asserts to remained tasks
        projectTask = map.get(7);
        projectTask.setStartDate(projectTask.getStartDate().minusDays(1));
        projectTask.setFinishDate(projectTask.getFinishDate().minusDays(1));

        termsCalculation.calculate(termData);
        projectTask = map.get(6);
        start = start.minusDays(1);
        assertEquals(start, projectTask.getStartDate());

    }

    private TermCalculationData initiateTermCalculationData() {

        List<Calendar> calendars = new ArrayList<>(1);
        CalendarImpl calendar = new CalendarImpl();
        calendar.setId(1);
        fillExceptions(calendar);
        calendars.add(calendar);

        List<ProjectTask> projectTasks = new ArrayList<>();

        LocalDateTime start = LocalDateTime.of(2022, 1, 25, 9, 0);
        LocalDateTime finish = calendar.getDateByDuration(start, durationDay);
        ProjectTask projectTask = getInstanceOfProjectTask(1, null, 3,
                start,
                finish,
                durationDay, ScheduleMode.AUTO, 1);
        projectTasks.add(projectTask);

        // new level
        projectTask = getInstanceOfProjectTask(2, 1, 0,
                start,
                finish,
                durationDay, ScheduleMode.AUTO, 1);
        projectTasks.add(projectTask);
        projectTask = getInstanceOfProjectTask(3, 1, 12,
                start,
                finish,
                durationDay, ScheduleMode.AUTO, 1);
        projectTasks.add(projectTask);
        projectTask = getInstanceOfProjectTask(4, 1, 0,
                start,
                finish,
                durationDay, ScheduleMode.AUTO, 1);
        projectTasks.add(projectTask);

        // new level
        projectTask = getInstanceOfProjectTask(5, 3, 0,
                start,
                finish,
                durationDay, ScheduleMode.AUTO, 1);
        projectTasks.add(projectTask);
        projectTask = getInstanceOfProjectTask(6, 3, 0,
                start,
                finish,
                durationDay, ScheduleMode.AUTO, 1);
        projectTasks.add(projectTask);
        projectTask = getInstanceOfProjectTask(7, 3, 0,
                start,
                finish,
                durationDay, ScheduleMode.MANUALLY, 1);
        projectTasks.add(projectTask);

        // upper task
        projectTask = getInstanceOfProjectTask(8, null, 12,
                start,
                finish,
                durationDay, ScheduleMode.AUTO, 1);
        projectTasks.add(projectTask);

        // new level
        projectTask = getInstanceOfProjectTask(9, 8, 12,
                start,
                finish,
                durationDay, ScheduleMode.AUTO, 1);
        projectTasks.add(projectTask);
        projectTask = getInstanceOfProjectTask(10, 8, 12,
                start,
                finish,
                durationDay, ScheduleMode.AUTO, 1);
        projectTasks.add(projectTask);

        // upper task
        projectTask = getInstanceOfProjectTask(11, null, 0,
                start,
                finish,
                durationDay, ScheduleMode.AUTO, null);
        projectTasks.add(projectTask);

        List<Link> links = new ArrayList<>(3);

        Link link = getInstanceOfLink(1, 6, 11, LinkType.FINISHSTART, 0);
        links.add(link);
        link = getInstanceOfLink(2, 11, 5, LinkType.FINISHSTART, 0);
        links.add(link);

        link = getInstanceOfLink(3, 9, 3, LinkType.STARTFINISH, 0);
        links.add(link);

        TermCalculationDataImpl tData = new TermCalculationDataImpl(projectTasks, links, false);
        tData.setDefaultCalendar(new CalendarImpl().getDefaultCalendar());
        tData.setCalendars(calendars);

        return tData;

    }

    private void fillExceptions(Calendar calendar) {
        var exceptions = new ArrayList<CalendarException>();

        var exception = (CalendarExceptionImpl) calendar.getCalendarExceptionInstance();
        exception.setStart(LocalDate.of(2022, 1, 3));
        exception.setNumberOfOccurrence(8);
        var finish = exception.getExceptionAsDayConstraint().keySet().stream().max(Comparator.naturalOrder());
        exception.setFinish(finish.get());
        exceptions.add(exception);

        exception = (CalendarExceptionImpl) calendar.getCalendarExceptionInstance();
        exception.setStart(LocalDate.of(2021, 12, 31));
        exception.setSetting(CalendarExceptionSetting.WORKING_TIMES);
        var intervals = new ArrayList<Interval>(2);
        var interval = exception.getIntervalInstance();
        interval.setFrom(LocalTime.of(8, 0));
        interval.setTo(LocalTime.of(12, 0));
        intervals.add(interval);
        interval = exception.getIntervalInstance();
        interval.setFrom(LocalTime.of(13, 0));
        interval.setTo(LocalTime.of(16, 0));
        intervals.add(interval);
        exception.setIntervals(intervals);
        exception.setNumberOfOccurrence(1);
        finish = exception.getExceptionAsDayConstraint().keySet().stream().max(Comparator.naturalOrder());
        exception.setFinish(finish.get());
        exceptions.add(exception);

        calendar.setCalendarExceptions(exceptions);

    }

    private Link getInstanceOfLink(Integer id, Integer prId, Integer lPrId, LinkType linkType, long lag) {

        Link link = new LinkImpl();
        link.setId(id);
        link.setProjectTaskId(prId);
        link.setLinkedProjectTaskId(lPrId);
        link.setLinkType(linkType);
        link.setLag(lag);

        return link;

    }

    private ProjectTask getInstanceOfProjectTask(Integer id, Integer parentId, Integer childrenCount,
                                                 LocalDateTime start, LocalDateTime finish, long duration,
                                                 ScheduleMode mode, Integer calendarId) {
        ProjectTask projectTask = new ProjectTaskImpl();
        projectTask.setId(id);
        projectTask.setParentId(parentId);
        projectTask.setStartDate(start);
        projectTask.setFinishDate(finish);
        projectTask.setAmountOfChildren(childrenCount);
        projectTask.setDuration(duration);
        projectTask.setScheduleMode(mode);
        projectTask.setCalendarId(calendarId);
        return projectTask;
    }

}