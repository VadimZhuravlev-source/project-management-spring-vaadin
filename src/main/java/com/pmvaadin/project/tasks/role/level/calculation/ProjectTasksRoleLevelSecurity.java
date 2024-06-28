package com.pmvaadin.project.tasks.role.level.calculation;

import com.pmvaadin.common.ListOfObjectsToListItsIdConverter;
import com.pmvaadin.project.structure.Filter;
import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.pmvaadin.project.tasks.entity.ProjectTaskImpl;
import com.pmvaadin.project.tasks.role.level.queries.QueryBuilderForOnlyInListAccess;
import com.pmvaadin.security.entities.UserProject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.stream.Collectors;

public class ProjectTasksRoleLevelSecurity {
    private final EntityManager entityManager;
    private final QueryBuilderForOnlyInListAccess queryBuilder = new QueryBuilderForOnlyInListAccess();

    public ProjectTasksRoleLevelSecurity(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<ProjectTask> getProjectTasksIfParentIsNull(List<UserProject> userProjects,
                                                           Filter filter) {

        var ids = getIdsAsString(userProjects);
        var queryText = queryBuilder.getQueryTextForUpperLevel(false, ids,
                filter);

        return getProjectTasksByQueryText(queryText);

    }

    public int getCountProjectTasksIfParentIsNull(List<UserProject> userProjects, Filter filter) {

        var ids = getIdsAsString(userProjects);
        var queryText = queryBuilder.getQueryTextForUpperLevel(true, ids,
                filter);

        return getCountProjectTasksByQueryText(queryText);

    }

    public List<ProjectTask> getProjectTasksOfParent(List<UserProject> userProjects, ProjectTask parent,
                                                     Filter filter) {

        var ids = getIdsAsString(userProjects);
        var queryText = queryBuilder.getQueryTextForParent(false, ids, parent.getId().toString(),
                filter);

        return getProjectTasksByQueryText(queryText);

    }

    public int getCountProjectTasksOfParent(List<UserProject> userProjects, ProjectTask parent,
                                            Filter filter) {

        var ids = getIdsAsString(userProjects);
        var queryText = queryBuilder.getQueryTextForParent(true, ids, parent.getId().toString(),
                filter);

        return getCountProjectTasksByQueryText(queryText);

    }

    public List<ProjectTask> getProjectTasksOfParentFullRights(Object parentId,
                                                               Filter filter) {

        var queryText = queryBuilder.getQueryTextForParentFullRights(false, parentId,
                filter);

        return getProjectTasksByQueryText(queryText);

    }

    public int getCountProjectTasksOfParentFullRights(Object parentId,
                                                      Filter filter) {

        var queryText = queryBuilder.getQueryTextForParentFullRights(true, parentId,
                filter);

        return getCountProjectTasksByQueryText(queryText);

    }

    public List<ProjectTask> getProjectTasksOfParentFullRights(List<UserProject> userProjects, Object parentId,
                                                               Filter filter) {

        var excludedIds = getIdsAsString(userProjects);
        var queryText = queryBuilder.getQueryTextForParentFullRights(false, parentId,
                filter, excludedIds);

        return getProjectTasksByQueryText(queryText);

    }

    public int getCountProjectTasksOfParentFullRights(List<UserProject> userProjects, Object parentId,
                                                      Filter filter) {

        var excludedIds = getIdsAsString(userProjects);
        var queryText = queryBuilder.getQueryTextForParentFullRights(true, parentId,
                filter, excludedIds);

        return getCountProjectTasksByQueryText(queryText);

    }

    private int getCountProjectTasksByQueryText(String queryText) {
        var query = entityManager.createNativeQuery(queryText);
        List<Object> tasks = (List<Object>) query.getResultList();
        return tasks.stream().map(o -> (int) o).findFirst().orElse(0);
    }

    private List<ProjectTask> getProjectTasksByQueryText(String queryText) {
        var query = entityManager.createNativeQuery(queryText, ProjectTaskImpl.class);
        List<ProjectTaskImpl> tasks = (List<ProjectTaskImpl>) query.getResultList();
        return tasks.stream().map(o -> (ProjectTask) o).collect(Collectors.toList());
    }

    private String getIdsAsString(List<UserProject> userProjects) {
        return ListOfObjectsToListItsIdConverter.getIdsAsString(userProjects, UserProject::getProjectId);
    }

}
