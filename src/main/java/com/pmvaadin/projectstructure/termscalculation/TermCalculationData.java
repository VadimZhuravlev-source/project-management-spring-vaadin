package com.pmvaadin.projectstructure.termscalculation;

import com.pmvaadin.calendars.entity.Calendar;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;

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
