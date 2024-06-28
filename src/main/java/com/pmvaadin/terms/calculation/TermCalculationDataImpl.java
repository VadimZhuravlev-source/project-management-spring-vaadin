package com.pmvaadin.terms.calculation;

import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.pmvaadin.project.links.entities.Link;
import lombok.Data;

import java.util.List;

@Data
public class TermCalculationDataImpl implements TermCalculationData {

    private List<ProjectTask> projectTasks;
    private List<Link> links;
    private boolean isCycle;
    private List<Calendar> calendars;
    private Calendar defaultCalendar;
    private boolean calculateFinishAlways;

    public TermCalculationDataImpl(List<ProjectTask> projectTasks, List<Link> links, boolean isCycle) {
        this.projectTasks = projectTasks;
        this.links = links;
        this.isCycle = isCycle;
    }

}
