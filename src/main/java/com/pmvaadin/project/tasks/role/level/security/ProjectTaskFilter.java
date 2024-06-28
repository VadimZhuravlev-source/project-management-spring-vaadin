package com.pmvaadin.project.tasks.role.level.security;

import com.pmvaadin.project.structure.Filter;
import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.pmvaadin.project.tasks.role.level.calculation.ProjectTasksRoleLevelSecurity;
import com.pmvaadin.project.tasks.repositories.ProjectTaskRepository;
import com.pmvaadin.security.entities.*;
import com.pmvaadin.security.services.UserService;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class ProjectTaskFilter {
    private ProjectTasksRoleLevelSecurity data;
    private ProjectTask projectTask;
    private User user;
    private ProjectTaskRepository projectTaskRepository;
    private Supplier<List<ProjectTask>> getProjectTasksMethod;
    private Supplier<Integer> getCountChildrenMethod;
    private Filter filter;

    public ProjectTaskFilter(EntityManager entityManager, UserService userService, ProjectTaskRepository projectTaskRepository) {

        this.getProjectTasksMethod = this::getEmptyList;
        this.getCountChildrenMethod = this::getNull;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return;
        }
        String currentPrincipalName = authentication.getName();
        this.user = userService.getUserByName(currentPrincipalName);
        if (user == null) {
            return;
        }

        this.projectTaskRepository = projectTaskRepository;
        this.data = new ProjectTasksRoleLevelSecurity(entityManager);

        var isAdmin = user.getRoles().stream().map(UserRole::getRole).anyMatch(role -> role == Role.ADMIN);
        if (!isAdmin) {
            var accessType = user.getAccessType();
            if (accessType == AccessType.ALL_DENIED) {
                return;
            } else if (accessType == AccessType.ONLY_IN_LIST) {
                getProjectTasksMethod = this::getProjectTasksForOnlyInList;
                getCountChildrenMethod = this::getCountProjectTasksForOnlyInList;
                return;
            } else if (accessType == AccessType.EXCEPT_IN_LIST) {
                getProjectTasksMethod = this::getProjectTasksForExceptInList;
                getCountChildrenMethod = this::getCountProjectTasksForExceptInList;
                return;
            }
        }

        getProjectTasksMethod = this::getProjectTasksWithFullRights;
        getCountChildrenMethod = this::getChildrenCountWithFullRights;

    }

    public List<ProjectTask> getProjectTasks(ProjectTask projectTask, Filter filter) {
        this.projectTask = projectTask;
        this.filter = filter;
        return getProjectTasksMethod.get();
    }

    public int getCountProjectTasks(ProjectTask projectTask, Filter filter) {
        this.projectTask = projectTask;
        this.filter = filter;
        return getCountChildrenMethod.get();
    }

    // TODO remake tasks finding for two method below by writing query counting the user projects
    public int sizeInBackEnd(String filter, PageRequest pageable) {
        return projectTaskRepository.findByNameLikeIgnoreCase("%" + filter + "%", pageable).size();
    }
    public List<ProjectTask> getItems(String filter, PageRequest pageable) {
        return projectTaskRepository.findByNameLikeIgnoreCase("%" + filter + "%", pageable);
    }

    private List<ProjectTask> getEmptyList() {
        return new ArrayList<>(0);
    }

    private int getNull() {
        return 0;
    }

    private List<ProjectTask> getProjectTasksForOnlyInList() {

        var allowedUserProjects = user.getProjects();
        if (isTaskNull(projectTask)) {
            return data.getProjectTasksIfParentIsNull(allowedUserProjects, filter);
        }
        return data.getProjectTasksOfParent(allowedUserProjects, projectTask, filter);

    }

    private int getCountProjectTasksForOnlyInList() {

        var allowedUserProjects = user.getProjects();
        if (isTaskNull(projectTask)) {
            return data.getCountProjectTasksIfParentIsNull(allowedUserProjects, filter);
        }
        return data.getCountProjectTasksOfParent(allowedUserProjects, projectTask, filter);

    }

    private boolean applyFilter() {
        return filter != null && filter.applyFilter();
    }

    private List<ProjectTask> getProjectTasksWithFullRights() {

        if (applyFilter()) {
            if (projectTask == null) {
                return data.getProjectTasksOfParentFullRights(null, filter);
            }
            return data.getProjectTasksOfParentFullRights(projectTask.getId(), filter);
        }

        if (isTaskNull(projectTask)) {
            return projectTaskRepository.findByParentIdIsNullOrderByLevelOrderAsc();
        }
        return projectTaskRepository.findByParentIdOrderByLevelOrderAsc(projectTask.getId());

    }

    private int getChildrenCountWithFullRights() {

        if (applyFilter()) {
            if (projectTask == null) {
                return data.getCountProjectTasksOfParentFullRights(null, filter);
            }
            return data.getCountProjectTasksOfParentFullRights(projectTask.getId(), filter);
        }

        if (isTaskNull(projectTask))
            return projectTaskRepository.getChildrenCount();

        return projectTaskRepository.getChildrenCount(projectTask.getId());
    }

    private List<ProjectTask> getProjectTasksForExceptInList() {

        if (applyFilter()) {
            if (projectTask == null) {
                return data.getProjectTasksOfParentFullRights(user.getProjects(),null, filter);
            }
            return data.getProjectTasksOfParentFullRights(user.getProjects(), projectTask.getId(), filter);
        }

        var excludedIds = user.getProjects().stream().map(UserProject::getProjectId).filter(Objects::nonNull).toList();
        if (isTaskNull(projectTask)) {
            if (excludedIds.isEmpty())
                return projectTaskRepository.findByParentIdIsNullOrderByLevelOrderAsc();
            return projectTaskRepository.findByParentIdIsNullAndIdNotInOrderByLevelOrderAsc(excludedIds);
        }
        if (excludedIds.isEmpty())
            return projectTaskRepository.findByParentIdOrderByLevelOrderAsc(projectTask.getId());

        return projectTaskRepository.findByParentIdAndIdNotInOrderByLevelOrderAsc(projectTask.getId(), excludedIds);

    }

    private int getCountProjectTasksForExceptInList() {

        if (applyFilter()) {
            if (projectTask == null) {
                return data.getCountProjectTasksOfParentFullRights(user.getProjects(),null, filter);
            }
            return data.getCountProjectTasksOfParentFullRights(user.getProjects(), projectTask.getId(), filter);
        }

        var excludedIds = user.getProjects().stream().map(UserProject::getProjectId).filter(Objects::nonNull).toList();
        if (isTaskNull(projectTask)) {
            if (excludedIds.isEmpty())
                return projectTaskRepository.getChildrenCount();
            return projectTaskRepository.getChildrenCountWithExcludedTasks(excludedIds);
        }
        if (excludedIds.isEmpty())
            return projectTaskRepository.getChildrenCount(projectTask.getId());

        return projectTaskRepository.getChildrenCountWithExcludedTasks(projectTask.getId(), excludedIds);

    }

    private boolean isTaskNull(ProjectTask projectTask) {
        return Objects.isNull(projectTask) || Objects.isNull(projectTask.getId());
    }

}
