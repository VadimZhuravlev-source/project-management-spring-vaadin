package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;

import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProjectTaskService extends ProjectTreeService {

    List<ProjectTask> getTreeProjectTasks();
    ProjectTask save(ProjectTask projectTask, boolean validate, boolean recalculateTerms);
    void recalculateProject();
    boolean validate(ProjectTask projectTask);
    Map<?, ProjectTask> getProjectTasksByIdWithFilledWbs(Collection<?> ids);
    <I> void fillParent(ProjectTask projectTask);
    //List<ProjectTask> recalculateTerms(Set<?> taskIds);
    List<ProjectTask> recalculateTerms(EntityManager entityManager, Set<?> taskIds);

}
