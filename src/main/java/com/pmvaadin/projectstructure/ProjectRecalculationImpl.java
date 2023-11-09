package com.pmvaadin.projectstructure;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ProjectRecalculationImpl implements ProjectRecalculation {

    private Set<ProjectTask> projects;

    @Override
    @Async
    public void recalculate(Set<ProjectTask> projects) {
        this.projects = projects;
    }



}
