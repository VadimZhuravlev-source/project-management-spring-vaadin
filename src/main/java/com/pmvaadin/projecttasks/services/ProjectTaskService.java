package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.List;
import java.util.Map;

public interface ProjectTaskService extends ProjectTreeService {

    List<ProjectTask> getTreeProjectTasks();
    ProjectTask save(ProjectTask projectTask, boolean validate, boolean recalculateTerms);
    void recalculateProject();
    boolean validate(ProjectTask projectTask);
    Map<?, ProjectTask> getProjectTasksByIdWithFilledWbs(List<?> ids);

    <I> void fillParent(ProjectTask projectTask);

}
