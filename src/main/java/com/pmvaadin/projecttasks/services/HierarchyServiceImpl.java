package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import java.util.ArrayList;
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
        List<?> projectTaskIds = projectTasks.stream().map(ProjectTask::getId).toList();

        var isNullElement = projectTaskIds.stream().anyMatch(Objects::isNull);
        if (isNullElement) throw new IllegalArgumentException();

        //String parameterValue =
        //        String.valueOf(projectTaskIds).replace('[', '{').replace(']', '}');

        List<ProjectTask> projectTasksList;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
//            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("get_children_in_depth_fast",
//                    ProjectTaskImpl.class);
//            query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
//            query.setParameter(1, parameterValue);
//            query.execute();

            Query query = entityManager.createNativeQuery(getQueryTextForChildrenInDepth(),  ProjectTaskImpl.class)
                    .setParameter("ids", projectTaskIds);

            projectTasksList = (List<ProjectTask>) query.getResultList();

        }finally {
            entityManager.close();
        }

        return projectTasksList;

    }

    @Override
    public List<ProjectTask> getParentsOfParent(List<?> ids) {

        if (ids.size() == 0) return new ArrayList<>();

        var isNullElement = ids.stream().anyMatch(Objects::isNull);
        if (isNullElement) throw new IllegalArgumentException();

        //String parameterValue = String.valueOf(ids).replace('[', '{').replace(']', '}');

        List<ProjectTask> projectTasks;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
//            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("get_parents_in_depth",
//                    ProjectTaskImpl.class);
//            query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
//            query.setParameter(1, parameterValue);
//            query.execute();

            Query query = entityManager.createNativeQuery(getQueryTextForParentsInDepth(),  ProjectTaskImpl.class)
                    .setParameter("ids", ids);

            projectTasks = (List<ProjectTask>) query.getResultList();

        }finally {
            entityManager.close();
        }

        return projectTasks;

    }

    @Override
    public List<ProjectTask> getParentsOfParent(ProjectTask projectTask) {

        List<Object> ids = new ArrayList<>(1);
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
