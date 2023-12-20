package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.terms.calculation.TermCalculationRespond;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import jakarta.persistence.EntityManager;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ChangeHierarchyTransactionalService {

    TermCalculationRespond moveTasksInHierarchy(Set<ProjectTask> projectTasks, ProjectTask target, GridDropLocation dropLocation);
    TermCalculationRespond moveTasksInHierarchy(Set<ProjectTask> projectTasks, ProjectTreeService.Direction direction);
    TermCalculationRespond recalculateTerms(EntityManager entityManager, Set<?> taskIds);
    Map<?, ProjectTask> getProjectTasksByIdWithFilledWbs(Collection<?> ids);
    void recalculateTerms(ProjectTask projectTask);
    void changeSortOrder(Set<ProjectTask> projectTasks, ProjectTreeService.Direction direction);
    TermCalculationRespond delete(List<? extends ProjectTask> projectTasks);

}
