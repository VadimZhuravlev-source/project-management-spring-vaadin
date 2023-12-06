package com.pmvaadin.projectstructure;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import com.pmvaadin.terms.calendars.entity.Calendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ProjectRecalculationImpl implements ProjectRecalculation {

    private Set<ProjectTask> projects;

    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    public void setProjectTaskRepository(ProjectTaskRepository projectTaskRepository) {
        this.projectTaskRepository = projectTaskRepository;
    }

    @Override
    @Async
    public void recalculate(Set<ProjectTask> projects) {
        this.projects = projects;
    }

    @Override
    @Async
    public void recalculate(Calendar savedCalendar, Calendar oldCalendar) {
        // TODO to define alterations of the DaysOfWeekSettings and the CalendarException and to find tasks that used this calendar

        System.out.println(this);

    }

}
