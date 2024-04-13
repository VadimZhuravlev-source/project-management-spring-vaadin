package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class HierarchyServiceImpl implements HierarchyService {

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

   @Autowired
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public List<ProjectTask> getElementsChildrenInDepth(List<? extends ProjectTask> projectTasks) {

        if (projectTasks.size() == 0) return new ArrayList<>();
        var projectTaskIds = projectTasks.stream().map(ProjectTask::getId).toList();

        var isNullElement = projectTaskIds.stream().anyMatch(Objects::isNull);
        if (isNullElement) throw new IllegalArgumentException();

        var parameterValue = String.valueOf(projectTaskIds).replace('[', '{').replace(']', '}');
        parameterValue = "'" + parameterValue + "'";

        var queryText = getQueryTextForChildrenInDepth();
        queryText = queryText.replace(":ids", parameterValue);

        List<ProjectTask> projectTasksList;
        var entityManager = entityManagerFactory.createEntityManager();
        try {
            var query = entityManager.createNativeQuery(queryText,  ProjectTaskImpl.class);
            projectTasksList = (List<ProjectTask>) query.getResultList();

        }finally {
            entityManager.close();
        }

        return projectTasksList;

    }

    @Override
    public List<ProjectTask> getParentsOfParent(Collection<?> ids) {

        if (ids.size() == 0) return new ArrayList<>();

        var isNullElement = ids.stream().anyMatch(Objects::isNull);
        if (isNullElement) throw new IllegalArgumentException("The provided project tasks must not contain parent id = null");

        var parameterValue = String.valueOf(ids).replace('[', '{').replace(']', '}');
        parameterValue = "'" + parameterValue + "'";

        var queryText = getQueryTextForParentsInDepth();
        queryText = queryText.replace(":ids", parameterValue);

        List<ProjectTask> projectTasks;
        var entityManager = entityManagerFactory.createEntityManager();
        try {
            var query = entityManager.createNativeQuery(queryText,  ProjectTaskImpl.class);
            projectTasks = (List<ProjectTask>) query.getResultList();

        }finally {
            entityManager.close();
        }

        return projectTasks;

    }

    @Override
    public List<ProjectTask> getParentsOfParent(ProjectTask projectTask) {

        var ids = new ArrayList<>(1);
        ids.add(projectTask.getId());
        return getParentsOfParent(ids);

    }

    private String getQueryTextForChildrenInDepth() {
        return
                """
                SELECT
                    *
                FROM project_tasks
                WHERE project_tasks.id = ANY(ARRAY(SELECT id FROM get_children_in_depth_fast(:ids)))
                """;
    }

    private String getQueryTextForParentsInDepth() {
        return
                """
                SELECT
                    *
                FROM project_tasks
                WHERE project_tasks.id = ANY(ARRAY(SELECT id FROM get_parents_in_depth(:ids)))
                """;
    }

}
