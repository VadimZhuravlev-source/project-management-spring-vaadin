package com.pmvaadin.projectstructure.termscalculation;

import com.pmvaadin.calendars.entity.Calendar;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;
import lombok.Data;

import java.util.List;

@Data
public class TermCalculationDataImpl implements TermCalculationData {

    private List<ProjectTask> projectTasks;
    private List<Link> links;
    private boolean isCycle;
    private List<Calendar> calendars;
    private Calendar defaultCalendar;

    public TermCalculationDataImpl(List<ProjectTask> projectTasks, List<Link> links, boolean isCycle) {
        this.projectTasks = projectTasks;
        this.links = links;
        this.isCycle = isCycle;
    }

}
