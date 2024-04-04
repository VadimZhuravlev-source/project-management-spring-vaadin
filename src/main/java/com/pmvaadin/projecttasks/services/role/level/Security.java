package com.pmvaadin.projecttasks.services.role.level;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import com.pmvaadin.projecttasks.services.role.level.calculation.DataOfAccessType;
import com.pmvaadin.security.entities.AccessType;
import com.pmvaadin.security.entities.Role;
import com.pmvaadin.security.entities.User;
import com.pmvaadin.security.entities.UserRole;
import com.pmvaadin.security.services.UserService;
import jakarta.persistence.EntityManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class Security {
    private DataOfAccessType data;
    private ProjectTask projectTask;
    private User user;
    private ProjectTaskRepository projectTaskRepository;
    private Supplier<List<ProjectTask>> getProjectTasksMethod;
    private Supplier<Integer> getCountChildrenMethod;

    public Security(EntityManager entityManager, UserService userService, ProjectTaskRepository projectTaskRepository) {

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
        this.data = new DataOfAccessType(entityManager);

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
            }
        }

        getProjectTasksMethod = this::getProjectTasksWithFullRights;
        getCountChildrenMethod = this::getChildrenWithFullRights;

    }

    public List<ProjectTask> getProjectTasks(ProjectTask projectTask) {
        this.projectTask = projectTask;
        return getProjectTasksMethod.get();
    }

    public int getCountProjectTasks(ProjectTask projectTask) {
        this.projectTask = projectTask;
        return getCountChildrenMethod.get();
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
            return data.getProjectTasksIfParentIsNull(allowedUserProjects);
        }
        return data.getProjectTasksOfParent(allowedUserProjects, projectTask);

    }

    private int getCountProjectTasksForOnlyInList() {

        var allowedUserProjects = user.getProjects();
        if (isTaskNull(projectTask)) {
            return data.getCountProjectTasksIfParentIsNull(allowedUserProjects);
        }
        return data.getCountProjectTasksOfParent(allowedUserProjects, projectTask);

    }

    private List<ProjectTask> getProjectTasksWithFullRights() {

        if (isTaskNull(projectTask)) {
            return projectTaskRepository.findByParentIdIsNullOrderByLevelOrderAsc();
        }
        return projectTaskRepository.findByParentIdOrderByLevelOrderAsc(projectTask.getId());

    }

    private int getChildrenWithFullRights() {
        if (isTaskNull(projectTask))
            return projectTaskRepository.getChildrenCount();

        return projectTaskRepository.getChildrenCount(projectTask.getId());
    }

    private List<ProjectTask> getProjectTasksForExceptInList() {
        var excludedUserProjects = user.getProjects();
        return new ArrayList<>();
    }

    private int getCountProjectTasksForExceptInList() {
        var excludedUserProjects = user.getProjects();
        return 0;
    }

    private boolean isTaskNull(ProjectTask projectTask) {
        return Objects.isNull(projectTask) || Objects.isNull(projectTask.getId());
    }

}
