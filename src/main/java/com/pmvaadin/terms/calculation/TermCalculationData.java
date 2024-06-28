package com.pmvaadin.terms.calculation;

import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.pmvaadin.project.links.entities.Link;

import java.util.List;

public interface TermCalculationData {

    List<ProjectTask> getProjectTasks();
    List<Link> getLinks();
    boolean isCycle();
    List<Calendar> getCalendars();
    void setCalendars(List<Calendar> calendars);
    Calendar getDefaultCalendar();
    void setDefaultCalendar(Calendar calendar);

    boolean isCalculateFinishAlways();
    void setCalculateFinishAlways(boolean calculateFinishAlways);

}
