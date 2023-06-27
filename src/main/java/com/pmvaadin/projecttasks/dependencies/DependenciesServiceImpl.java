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
import java.util.Set;

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
    public  <I> DependenciesSet getAllDependencies(I pid, List<I> ids) {

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

        return "The cycle has detected: " + stringBuilder + "\n";

    }

    @Override
    public <I> DependenciesSet getAllDependencies(Set<I> ids) {

        return getAllDependencies(ids, getQueryTextForDependenciesAtTermCalc());

    }

    private <I, L> DependenciesSet getAllDependencies(I pid, List<I> ids, String queryText) {

        var isNullElement = ids.stream().anyMatch(Objects::isNull);

        if (pid == null || isNullElement) throw new IllegalArgumentException("Passed parameters haven't to be null.");

        String parameterValue = String.valueOf(ids).replace('[', '{').replace(']', '}');
        String pairOfValues = "'" + pid + "#" + parameterValue + "'";
        parameterValue = "'" + parameterValue + "'";

        String convertedQueryText = queryText.replace(":checkedIds", parameterValue)
                .replace(":pid", pid.toString())
                .replace(":pairOfValues", pairOfValues);

        List<Object[]> rows;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {

            Query query = entityManager.createNativeQuery(convertedQueryText);

            rows = query.getResultList();

        } finally {
            entityManager.close();
        }

        List<I> projectTaskIds = new ArrayList<>(rows.size());
        List<L> linkIds = new ArrayList<>();
        String path = "";
        var checkedIdIndex = 0;
        var projectTaskIdIndex = 1;
        var pathIndex = 2;
        var isCycleIndex = 3;
        var linkIdIndex = 4;
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

    private <I> DependenciesSet getAllDependencies(Set<I> ids, String queryText) {

        var isNullElement = ids.stream().anyMatch(Objects::isNull);

        if (isNullElement) throw new IllegalArgumentException("Passed parameters haven't to be null.");

        String pairsOfValues = String.join(";", ids.stream().map(Object::toString).toList());

        String convertedQueryText = queryText.replace(":pairsOfValues", pairsOfValues);

        List<Object[]> rows;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {

            Query query = entityManager.createNativeQuery(convertedQueryText);

            rows = query.getResultList();

        } finally {
            entityManager.close();
        }



    }

    private String getQueryTextForDependencies() {

        return

        """
        SELECT
            dep.checked_id,
            dep.id,
            dep.path,
            dep.is_cycle,
            dep.link_id
        FROM get_all_dependencies(:pairOfValues) dep
        """;

    }

    private String getQueryTextForDependenciesWithCheckedChildren() {

        return

        """
        WITH all_children_ids AS (
        SELECT
            id
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
        
        ),
        
        pair_of_values AS (
        SELECT
            :pid || '#{' || array_to_string(ARRAY(SELECT id FROM all_checked_ids), ',','NULL') || '}' text
        )
        
        SELECT
            dep.checked_id,
            dep.id,
            dep.path,
            dep.is_cycle,
            dep.link_id
        FROM get_all_dependencies((SELECT text FROM pair_of_values)) dep
        """;

    }

    private String getQueryTextForDependenciesAtTermCalc() {

        return

        """
        SELECT
            dep.checked_id,
            dep.id,
            dep.path,
            dep.is_cycle,
            dep.link_id
        FROM get_all_dependencies(:pairsOfValues) dep
        """;

    }

}
