package com.pmvaadin.projecttasks.dependencies;

import com.pmvaadin.AppConfiguration;
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
    public  <I, L> DependenciesSet getAllDependencies(I pid, List<I> checkedIds) {

        String parameterValue = String.valueOf(checkedIds).replace('[', '{').replace(']', '}');

        List<Object[]> rows;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {

            Query query = entityManager.createNativeQuery(getQueryTextForDependencies())
                    .setParameter("pid", pid)
                    .setParameter("checkedIds", parameterValue);

            rows = query.getResultList();

        } finally {
            entityManager.close();
        }

        List<I> projectTaskIds = new ArrayList<>(rows.size());
        List<L> linkIds = new ArrayList<>();
        String path = "";
        var projectTaskIdIndex = 0;
        var pathIndex = 1;
        var isCycleIndex = 2;
        var linkIdIndex = 3;
        var isCycle = false;
        for (Object[] row: rows) {
            I id = (I) row[projectTaskIdIndex];
            L linkId = (L) row[linkIdIndex];
            if (linkId != null) linkIds.add(linkId);
            projectTaskIds.add(id);

            isCycle = (boolean) row[isCycleIndex];
            if (isCycle) {
                path = (String) row[pathIndex];
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

        projectTaskIds.removeAll(checkedIds);

        var projectTasks = projectTaskRepository.findAllById(projectTaskIds);
        var links = linkRepository.findAllById(linkIds);

        return new DependenciesSetImpl(projectTasks, links, isCycle);

    }

    @Override
    public <I> DependenciesSet checkCycleDependencies(I pid, List<I> childrenIds) {

        String parameterValue = String.valueOf(childrenIds).replace('[', '{').replace(']', '}');

        List<Object[]> rows;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {

            Query query = entityManager.createNativeQuery(getQueryTextForCheckCycle())
                    .setParameter("pid", pid)
                    .setParameter("childrenIds", parameterValue);

            rows = query.getResultList();

        } finally {
            entityManager.close();
        }

        String path = "";
        var pathIndex = 0;
        var isCycleIndex = 1;
        var isCycle = false;
        for (Object[] row: rows) {
            isCycle = (boolean) row[isCycleIndex];
            if (isCycle) {
                path = (String) row[pathIndex];
                break;
            }
        }

        List<I> projectTaskIds = new ArrayList<>(0);
        if (isCycle) {
            ApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);
            ProjectTasksIdConversion idConversion = context.getBean(ProjectTasksIdConversion.class);
            projectTaskIds = idConversion.convert(path);
        }

        projectTaskIds.removeAll(childrenIds);

        var projectTasks = projectTaskRepository.findAllById(projectTaskIds);

        return new DependenciesSetImpl(projectTasks, new ArrayList<>(0), isCycle);

    }

    private String getQueryTextForDependencies() {
        return
        """
        SELECT\s
            dep.id,
            array_to_string(dep.path, ',') path,
            dep.is_cycle,
            dep.link_id
        FROM get_all_dependencies(:pid, :checkedIds) dep
        """;

    }

    private String getQueryTextForCheckCycle() {

        return
        """
        WITH all_children_ids AS (
            SELECT id
            FROM get_children_in_depth_fast(:childrenIds)
        ),
                        
        all_checked_ids AS (
        SELECT DISTINCT\s
            id
        FROM all_children_ids
                        
        UNION
            
        SELECT
            linked_project_task
        FROM links\s
        WHERE\s
            project_task = ANY(\s
                ARRAY(SELECT\s
                      id\s
                      FROM all_children_ids
                     )\s
            )
            
        )
                        
        SELECT\s
            array_to_string(dep.path, ',') path,
            dep.is_cycle
        FROM get_all_dependencies(:pid, ARRAY(SELECT id FROM all_checked_ids)) dep
        WHERE dep.is_cycle
        """;

    }

}
