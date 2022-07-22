package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;
import com.PMVaadin.PMVaadin.Entities.ProjectTaskImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class EntityManagerServiceImpl implements EntityManagerService {

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public List<ProjectTask> getElementsChildrenInDepth(List<? extends ProjectTask> projectTasks) {

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("get_children_of_parent_in_depth",
                ProjectTaskImpl.class);
        query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);

        List<Integer> projectTaskIds = projectTasks.stream().map(ProjectTask::getId).toList();
        String parameterValue = String.valueOf(projectTaskIds).replace('[', '{').replace(']', '}');
        query.setParameter(1, parameterValue);
        query.execute();

        return (List<ProjectTask>) query.getResultList();

    }

    @Override
    public List<ProjectTask> getParentsOfParent(List<? extends ProjectTask> projectTasks) {

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("get_parents_of_parent",
                ProjectTaskImpl.class);
        query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);

        List<Integer> projectTaskIds = projectTasks.stream().map(ProjectTask::getId).toList();
        String parameterValue = String.valueOf(projectTaskIds).replace('[', '{').replace(']', '}');
        query.setParameter(1, parameterValue);
        query.execute();

        return (List<ProjectTask>) query.getResultList();

    }

    @Override
    public List<ProjectTask> getParentsOfParent(ProjectTask projectTask) {

        List<ProjectTask> projectTasks = new ArrayList<>(1);
        projectTasks.add(projectTask);
        return getParentsOfParent(projectTasks);

    }

}
