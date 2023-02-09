package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.links.entities.LinkImpl;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("get_children_in_depth_fast",
                ProjectTaskImpl.class);
        query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);

        List<Integer> projectTaskIds = projectTasks.stream().map(ProjectTask::getId).toList();
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

    @Override
    public <I> List<Link> getAllDependencies(I parentId, List<?> ids) {

        if (ids.size() == 0) return new ArrayList<>();

        // TODO if it will work, then do the same for above two methods
        //StoredProcedureQuery query = executeQueryByProcedureName("get_links_in_depth", LinkImpl.class, ids);
        //return query.getResultList();

        List<Object[]> dependencies = getDependencies(parentId, ids);

        for (Object[] dependency: dependencies) {
            ProjectTask projectTask = (ProjectTask) dependency[0];
            Link link = (Link) dependency[1];
        }

        return new ArrayList<>();
    }

    private <I> List<Object[]> getDependencies(I pid, List<?> checkedIds) {

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Session session = entityManager.unwrap(Session.class);
//        Query query = session
//                .createNativeQuery("SELECT * FROM get_all_dependencies(:pid, :checkedIds)", Object[].class);// dep, ProjectTaskImpl p, LinkImpl l"
//                       // + " WHERE dep.id = p.id AND dep.link_id = l.id");
        //StoredProcedureQuery query = entityManager.createStoredProcedureQuery("get_all_dependencies");
//        query.setParameter("pid", pid);
        String parameterValue = String.valueOf(checkedIds).replace('[', '{').replace(']', '}');
//        query.setParameter("checkedIds", parameterValue);
        //query.registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN);
        //query.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
//        query.setParameter(1, pid);
//        query.setParameter(2, parameterValue);
        //query.execute();

        List<ProjectTaskImpl> result = session
//                .createNativeQuery("SELECT * FROM get_all_dependencies(:pid, :checkedIds)", Object[].class)
                .createNativeQuery("SELECT p.* FROM project_tasks p JOIN get_all_dependencies(:pid, :checkedIds) dep ON dep.id = p.id", ProjectTaskImpl.class)
//                .addScalar("id", StandardBasicTypes.INTEGER)
//                .addScalar("name", StandardBasicTypes.STRING)
                .setParameter("pid", pid)
                .setParameter("checkedIds", parameterValue)
                .list();
        //List<Object[]> result = query.getResultList();

        return new ArrayList<>();

    }

    private StoredProcedureQuery executeQueryByProcedureName(String procedureName, Class resultClasses, List<?> ids) {

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(procedureName, resultClasses);
        query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
        String parameterValue = String.valueOf(ids).replace('[', '{').replace(']', '}');
        query.setParameter(1, parameterValue);
        query.execute();

        return query;

    }

}
