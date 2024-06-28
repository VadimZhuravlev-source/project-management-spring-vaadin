package com.pmvaadin.project.structure;

import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.pmvaadin.terms.calendars.entity.Calendar;

import java.util.Set;

public interface ProjectRecalculation {

    void recalculate(Set<ProjectTask> projectTasks);

    void recalculate(Calendar savedCalendar, Calendar oldCalendar);

}
