package com.pmvaadin.projecttasks.resources.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.resources.entity.TaskResource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

public class TaskResourceServiceImpl implements TaskResourceService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<TaskResource> getLaborResources(ProjectTask projectTask) {

        entityManager

    }

    private String getProjectTaskLaborResourcesQueryText() {
        return """
                SELECT
                    task_labor_resources.*,
                    labor_resources.name
                    
                FROM task_labor_resources
                    JOIN labor_resources
                        ON task_labor_resources.resource_id = labor_resources.id
                WHERE
                    project_task_id = :id
                """;
    }

}
