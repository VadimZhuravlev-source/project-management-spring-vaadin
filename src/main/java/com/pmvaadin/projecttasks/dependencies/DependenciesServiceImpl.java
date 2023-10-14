package com.pmvaadin.projecttasks.dependencies;

import com.pmvaadin.AppConfiguration;
import com.pmvaadin.terms.calculation.TermCalculationData;
import com.pmvaadin.terms.calculation.TermCalculationDataImpl;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.repositories.LinkRepository;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import java.util.*;

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
    public <I> TermCalculationData getAllDependenciesForTermCalc(EntityManager entityManager, Set<I> ids) {

        //return new DependenciesSetImpl(new ArrayList<>(), new ArrayList<>(), false);
        return getAllDependenciesForTermCalc(entityManager, ids, getQueryTextForDependenciesAtTermCalc());

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

    private <I, L> TermCalculationData getAllDependenciesForTermCalc(EntityManager entityManager, Set<I> ids, String queryText) {

        var isNullElement = ids.stream().anyMatch(Objects::isNull);

        if (isNullElement) throw new IllegalArgumentException("Passed parameters haven't to contain null.");

        String pairsOfValues = String.join(";", ids.stream().map(Object::toString).toList());

        pairsOfValues = "'" + pairsOfValues + "'";
        String convertedQueryText = queryText.replace(":pairsOfValues", pairsOfValues);

        List<Object[]> rows;
        //EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {

            Query query = entityManager.createNativeQuery(convertedQueryText);

            rows = query.getResultList();

        } finally {
           // entityManager.close();
        }

        HashSet<I> projectTaskIds = new HashSet<>(rows.size());
        HashSet<L> linkIds = new HashSet<>();

        var isCycleIndex = 0;
        var projectTaskIdIndex = 1;
        var linkIdIndex = 2;
        var childrenCountIndex = 3;
        var isCycle = false;
        Map<Object, Integer> childrenCountMap = new HashMap<>(rows.size());

        for (Object[] row: rows) {
            isCycle = (boolean) row[isCycleIndex];
            if (isCycle) break;
            I id = (I) row[projectTaskIdIndex];
            L linkId = (L) row[linkIdIndex];
            if (id != null) {
                projectTaskIds.add(id);
                int childrenCount = (int) row[childrenCountIndex];
                childrenCountMap.put(id, childrenCount);
            }
            if (linkId != null) linkIds.add(linkId);
        }

        if (isCycle) {
            projectTaskIds.clear();
            linkIds.clear();
        }

        var projectTasks = projectTaskRepository.findAllById(projectTaskIds);
        projectTasks.forEach(projectTask -> {
            Integer childrenCount = childrenCountMap.getOrDefault(projectTask.getId(), 0);
            projectTask.setChildrenCount(childrenCount);
        });
        var links = linkRepository.findAllById(linkIds);

        return new TermCalculationDataImpl(projectTasks, links, isCycle);

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
                with dependencies as(
                SELECT
                    dep.checked_id,
                    dep.id,
                    dep.path,
                    dep.is_cycle,
                    dep.link_id
                FROM get_all_dependencies(:pairsOfValues) dep
                ),
                        
                proceeding_execution AS (
                SELECT
                    bool_or(dependencies.is_cycle) is_cycle
                FROM dependencies
                ),
                        
                unique_ids AS (
                SELECT DISTINCT
                    dependencies.id
                FROM dependencies, proceeding_execution
                WHERE
                    NOT proceeding_execution.is_cycle
                        
                ),
                        
                searched_ids AS (
                SELECT DISTINCT
                    unique_ids.id,
                    -- I don not know why the hibernate query plan cache returns query text of this query without one of ":" and
                    -- throws exception, that's why 0 is used as NULL in the row of the query below.
                    --NULL::INT link_id
                    0 link_id
                FROM unique_ids
                        
                UNION
                        
                SELECT
                    project_tasks.id,
                    --NULL::INT
                    0
                FROM unique_ids
                JOIN project_tasks
                    ON unique_ids.id = project_tasks.parent_id
                        
                UNION
                        
                SELECT
                    links.linked_project_task,
                    links.id
                FROM unique_ids
                JOIN links
                    ON unique_ids.id = links.project_task
                ),
                        
                searched_ids_with_children_count AS (
                SELECT
                    searched_ids.id,
                    CASE WHEN searched_ids.link_id = 0
                        THEN NULL
                        ELSE searched_ids.link_id
                    END link_id,
                    count(p.id) children_count
                FROM searched_ids
                LEFT JOIN project_tasks p
                    ON searched_ids.id = p.parent_id
                GROUP BY
                    searched_ids.id,
                    searched_ids.link_id)
                        
                SELECT
                    proceeding_execution.is_cycle,
                    searched_ids.id,
                    searched_ids.link_id,
                    CAST(searched_ids.children_count AS INT) children_count
                FROM proceeding_execution
                LEFT JOIN searched_ids_with_children_count searched_ids
                    ON TRUE
                """;

    }

}
