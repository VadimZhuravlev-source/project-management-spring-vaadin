package com.pmvaadin.projecttasks.dependencies;

import com.pmvaadin.AppConfiguration;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.links.repositories.LinkRepository;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class DependenciesServiceImpl implements DependenciesService {

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    private ProjectTaskRepository projectTaskRepository;
    private LinkRepository linkRepository;

    @Autowired
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Autowired
    public void setProjectTaskRepository(ProjectTaskRepository projectTaskRepository) {
        this.projectTaskRepository = projectTaskRepository;
    }

    @Autowired
    public void setLinkRepository(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
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

    public  <I, L> DependenciesSet getAllDependencies(I pid, List<?> checkedIds) {

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        String parameterValue = String.valueOf(checkedIds).replace('[', '{').replace(']', '}');

        Query query = entityManager.createNativeQuery(
                "SELECT " +
                        "dep.id," +
                        "array_to_string(dep.path, ',',) path," +
                        "dep.is_cycle," +
                        "dep.link_id" +
                        "FROM get_all_dependencies(:pid, :checkedIds) dep"
        )
                .setParameter("pid", pid)
                .setParameter("checkedIds", parameterValue);

        List<Object[]> rows = query.getResultList();

        List<I> projectTaskIds = new ArrayList<>(rows.size());
        List<L> linkIds = new ArrayList<>();
        String path = "";
        boolean isCycle = false;
        for (Object[] row: rows) {
            I id = (I) row[0];
            L linkId = (L) row[3];
            linkIds.add(linkId);
            projectTaskIds.add(id);

            isCycle = isCycle || (boolean) row[2];
            if (isCycle) {
                path = (String) row[1];
                break;
            }
        }

        if (isCycle) {
            projectTaskIds.clear();
            linkIds.clear();
            ApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);
            ProjectTasksIdConversion idConversion = context.getBean(ProjectTasksIdConversion.class);
            projectTaskIds = idConversion.convert(path);
        }

        List<ProjectTask> projectTasks = projectTaskRepository.findAllById(projectTaskIds);
        List<Link> links = linkRepository.findAllById(linkIds);

        DependenciesSet dependenciesSet = new DependenciesSetImpl(projectTasks, links, isCycle);

//        Session session = entityManager.unwrap(Session.class);

//        List<Object> result = session
//                .createNativeQuery(
//                        "SELECT {p.*}, {l.*} FROM get_all_dependencies(:pid, :checkedIds) dep " +
//                                "JOIN project_tasks p ON dep.id = p.id" +
//                                "JOIN links l ON dep.id = l.id", Object.class)
//                .setParameter("pid", pid)
//                .setParameter("checkedIds", parameterValue)
//                .addEntity("p", ProjectTaskImpl.class)
//                .addEntity("l", LinkImpl.class)
//                .getResultList();

        return dependenciesSet;

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
