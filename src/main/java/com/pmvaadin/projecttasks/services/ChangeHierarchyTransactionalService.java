package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.terms.calculation.TermCalculationRespond;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import jakarta.persistence.EntityManager;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ChangeHierarchyTransactionalService {

    TermCalculationRespond changeLocation(Set<ProjectTask> projectTasks, ProjectTask target, GridDropLocation dropLocation);
    TermCalculationRespond changeLocation(Set<ProjectTask> projectTasks, ProjectTreeService.Direction direction);
    TermCalculationRespond recalculateTerms(EntityManager entityManager, Set<?> taskIds);
    Map<?, ProjectTask> getProjectTasksByIdWithFilledWbs(Collection<?> ids);
    TermCalculationRespond recalculateTerms(ProjectTask projectTask);

}
