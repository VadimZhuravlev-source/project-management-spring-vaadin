package com.pmvaadin.projecttasks.dependencies;

import com.pmvaadin.AppConfiguration;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.repositories.LinkRepository;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public  <I, L> DependenciesSet getAllDependencies(I pid, List<I> ids) {

        return getAllDependencies(pid, ids, getQueryTextForDependencies());

    }

    @Override
    public <I> DependenciesSet getAllDependenciesWithCheckedChildren(I pid, List<I> ids) {

        return getAllDependencies(pid, ids, getQueryTextForDependenciesWithCheckedChildren());

    }

    @Override
    public String getCycleLinkMessage(DependenciesSet dependenciesSet) {

        List<ProjectTask> projectTasks = dependenciesSet.getProjectTasks();

        StringBuilder stringBuilder = new StringBuilder();

        String delimiter = " -> ";
        projectTasks.forEach(projectTask -> {
            stringBuilder.append(projectTask.getRepresentation());
            stringBuilder.append(delimiter);
        });

        stringBuilder.replace(stringBuilder.length()  - delimiter.length(), stringBuilder.length(), "");

        String message = "The cycle has detected: " + stringBuilder + "\n";

        return message;

    }

    private <I, L> DependenciesSet getAllDependencies(I pid, List<I> ids, String queryText) {

        var isNullElement = ids.stream().anyMatch(Objects::isNull);

        if (pid == null || isNullElement) throw new IllegalArgumentException();

        String parameterValue = String.valueOf(ids).replace('[', '{').replace(']', '}');
        parameterValue = "'" + parameterValue + "'";

        String convertedQueryText = queryText.replace(":checkedIds", parameterValue);
        List<Object[]> rows;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {

            Query query = entityManager.createNativeQuery(convertedQueryText)
                    .setParameter("pid", pid);
                    //.setParameter("checkedIds", parameterValue);

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

        var projectTasks = projectTaskRepository.findAllById(projectTaskIds);
        var links = linkRepository.findAllById(linkIds);

        return new DependenciesSetImpl(projectTasks, links, isCycle);

    }

    private String getQueryTextForDependencies() {
        return
        """
        SELECT
            dep.id,
            dep.path,
            dep.is_cycle,
            dep.link_id
        FROM get_all_dependencies(:pid, :checkedIds) dep
        """;

    }

    private String getQueryTextForDependenciesWithCheckedChildren() {

        return
        """
        WITH all_children_ids AS (
            SELECT id
            FROM get_children_in_depth_fast(:checkedIds)
        ),
                        
        all_checked_ids AS (
        SELECT DISTINCT
            id
        FROM all_children_ids
                        
        UNION
            
        SELECT
            linked_project_task
        FROM links
        WHERE
            project_task = ANY(
                ARRAY(SELECT
                      id
                      FROM all_children_ids
                     )
            )
            
        )
                        
        SELECT
            dep.id,
            dep.path,
            dep.is_cycle,
            dep.link_id
        FROM get_all_dependencies(:pid, ARRAY(SELECT id FROM all_checked_ids)) dep
        """;

    }

}
