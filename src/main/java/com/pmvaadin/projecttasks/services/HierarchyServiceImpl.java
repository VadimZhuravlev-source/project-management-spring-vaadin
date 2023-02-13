package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("get_children_in_depth_fast",
                ProjectTaskImpl.class);
        query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);

        List<?> projectTaskIds = projectTasks.stream().map(ProjectTask::getId).toList();
        String parameterValue = String.valueOf(projectTaskIds).replace('[', '{').replace(']', '}');
        query.setParameter(1, parameterValue);
        query.execute();

        return (List<ProjectTask>) query.getResultList();

    }

    @Override
    public List<ProjectTask> getParentsOfParent(List<?> ids) {

        if (ids.size() == 0) return new ArrayList<>();

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("get_parents_in_depth",
                ProjectTaskImpl.class);
        query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);

        //List<Integer> projectTaskIds = projectTasks.stream().map(ProjectTask::getId).toList();
        String parameterValue = String.valueOf(ids).replace('[', '{').replace(']', '}');
        query.setParameter(1, parameterValue);
        query.execute();

        return (List<ProjectTask>) query.getResultList();

    }

    @Override
    public List<ProjectTask> getParentsOfParent(ProjectTask projectTask) {

        List<Integer> ids = new ArrayList<>(1);
        ids.add(projectTask.getId());
        return getParentsOfParent(ids);

    }

}
