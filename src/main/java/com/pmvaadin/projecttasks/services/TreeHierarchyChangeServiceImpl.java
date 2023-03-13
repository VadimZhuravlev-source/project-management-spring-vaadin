package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import java.util.*;

@Service
public class TreeHierarchyChangeServiceImpl implements TreeHierarchyChangeService {

    private ProjectTaskRepository projectTaskRepository;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Autowired
    public void setProjectTaskRepository(ProjectTaskRepository projectTaskRepository) {
        this.projectTaskRepository = projectTaskRepository;
    }

    @Override
    public List<ProjectTask> fetchChildren(ProjectTask projectTask) {

        List<ProjectTask> projectTasks;
        if (Objects.isNull(projectTask) || Objects.isNull(projectTask.getId())) {
            projectTasks = projectTaskRepository.findByParentIdIsNullOrderByLevelOrderAsc();
        } else {
            projectTasks = projectTaskRepository.findByParentIdOrderByLevelOrderAsc(projectTask.getId());
        }

        fillChildrenCount(projectTasks);
        fillWbs(projectTasks, projectTask);
        return projectTasks;
    }

    private void fillWbs(List<ProjectTask> children, ProjectTask projectTask) {

        String wbs = "";
        if (!Objects.isNull(projectTask)) wbs = projectTask.getWbs() + ".";
        final String parentWbs = wbs;
        children.forEach(child -> child.setWbs(parentWbs + child.getLevelOrder()));

    }

    private void fillChildrenCount(List<ProjectTask> projectTasks) {

        Map<?, Integer> map = getProjectTasksChildrenCount(projectTasks);

        projectTasks.forEach(projectTask -> {
            int childrenCount = map.getOrDefault(projectTask.getId(), 0);
            if (childrenCount == 0) return;
            projectTask.setChildrenCount(childrenCount);
        });

    }

    private <I> Map<I, Integer> getProjectTasksChildrenCount(List<ProjectTask> projectTasks) {

        String queryText =
                """
        SELECT
        	p.parent_id id,
            COUNT(p.id) children_count
        FROM project_tasks p
        WHERE p.parent_id IN (:pids)
        GROUP BY p.parent_id
        HAVING COUNT(p.id) > 0
        """;

        var projectTaskIds = projectTasks.stream().map(ProjectTask::getId).toList();
        String parameterValue = String.valueOf(projectTaskIds).replace('[', '{').replace(']', '}');
        parameterValue = "'" + parameterValue + "'";

        queryText = queryText.replace(":pids", parameterValue);

        List<Object[]> rows;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            Query query = entityManager.createNativeQuery(queryText);
            rows = (List<Object[]>) query.getResultList();

        }finally {
            entityManager.close();
        }

        Map<I, Integer> map = new HashMap<>(rows.size());

        var idIndex = 0;
        var countIndex = 1;

        for (Object[] row: rows) {
            I id = (I) row[idIndex];
            Integer count = (int) row[countIndex];
            map.put(id, count);
        }

        return map;

    }

}
