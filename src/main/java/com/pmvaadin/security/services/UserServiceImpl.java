package com.pmvaadin.security.services;

import com.pmvaadin.common.services.ListService;
import com.pmvaadin.project.structure.StandardError;
import com.pmvaadin.project.tasks.entity.HierarchyElement;
import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.pmvaadin.project.tasks.repositories.ProjectTaskRepository;
import com.pmvaadin.resources.labor.entity.LaborResourceRepresentation;
import com.pmvaadin.resources.labor.services.LaborResourceService;
import com.pmvaadin.security.entities.*;
import com.pmvaadin.security.repositories.UserRepository;
import com.pmvaadin.security.user.labor.resource.UserLaborResource;
import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserServiceImpl implements UserService, ListService<UserRepresentation, User> {

    private UserRepository userRepository;
    private ProjectTaskRepository projectTaskRepository;
    private LaborResourceService laborResourceService;

    @Autowired
    @Qualifier("LaborResourceService")
    public void setLaborResourceService(LaborResourceService laborResourceService) {
        this.laborResourceService = laborResourceService;
    }

    @Autowired
    public void setProjectTaskRepository(ProjectTaskRepository projectTaskRepository) {
        this.projectTaskRepository = projectTaskRepository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getUserByName(String name) {
        return userRepository.findByName(name).orElse(null);
    }

    // ListService

    @Override
    public User save(User user) {
        var name = user.getName();
        var foundUser = userRepository.findByName(name);
        if (foundUser.isPresent() && !Objects.equals(user, foundUser.get()))
            throw new StandardError("There is a user with this name. Please choose another name.");
        fillSortableFields(user);
        var savedUser = userRepository.save(user);
        fillAssistiveData(savedUser);
        return savedUser;
    }

    @Override
    public List<UserRepresentation> getItems(String filter, Pageable pageable) {
        var items = userRepository.findByNameLikeIgnoreCase("%" + filter + "%", pageable, UserRepresentationDTO.class);
        return items.stream().map(l -> (UserRepresentation) l).toList();
    }

    @Override
    public int sizeInBackEnd(String filter, Pageable pageable) {
        return userRepository.findByNameLikeIgnoreCase("%" + filter + "%", pageable, UserRepresentationDTO.class).size();
    }

    @Override
    public User add() {

        return new UserImpl();

    }

    @Override
    public User get(UserRepresentation representation) {
        var foundUser = userRepository.findById(representation.getId()).orElse(null);
        if (foundUser == null)
            return null;
        fillAssistiveData(foundUser);
        return foundUser;
    }

    @Transactional
    @Override
    public boolean delete(Collection<UserRepresentation> reps) {

        var ids = reps.stream().map(UserRepresentation::getId).toList();
        var delete = checkIfItemsCanBeDeleted(ids);

        if (delete)
            userRepository.deleteAllById(ids);

        return true;

    }

    @Override
    public User copy(UserRepresentation calRep) {

        var user = userRepository.findById(calRep.getId()).orElse(new UserImpl());
        fillAssistiveData(user);
        if (user instanceof HasIdentifyingFields)
            ((HasIdentifyingFields) user).nullIdentifyingFields();

        return user;

    }

    // End of the List service

    @Override
    public void addProjectTaskToUserProject(List<ProjectTask> projectTasks, User user) {
        var usersProjectsIds = user.getProjects().stream().map(UserProject::getProjectId)
                .collect(Collectors.toSet());
        var newUserProjects = projectTasks.stream().map(ProjectTask::getId)
                .filter(id -> !usersProjectsIds.contains(id))
                .map(id -> {
                    var userProject = user.getUserProjectInstance();
                    userProject.setProjectId(id);
                    return userProject;
                })
                .toList();

        var projects = user.getProjects();

        if (projects.addAll(newUserProjects)) {
            user.addProjects(projects);
            userRepository.save(user);
        }
    }

    @Override
    public String getUserName() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            return null;

        var principal = authentication.getPrincipal();
        UserDetails userDetails;
        if (principal instanceof UserDetails) {
            userDetails = (UserDetails) principal;
        } else
            return null;
        return userDetails.getUsername();
    }

    @Override
    public User getCurrentUser() {
        var name = getUserName();
        return getUserByName(name);
    }

    @Override
    public boolean isUserFollowingRLS(User user) {
        if (user == null)
            return false;
        var roles = user.getRoles().stream().map(UserRole::getRole).toList();
        if (roles.contains(Role.ADMIN))
            return false;

        return user.getAccessType() == AccessType.ONLY_IN_LIST;

    }

    @Override
    public Map<?, AccessRights> getUserAccessTable(Collection<? extends HierarchyElement<?>> checkingProjectTasks,
                                                   User user,
                                                   Collection<? extends HierarchyElement<?>> parents) {

        var parentsMap = parents.stream().collect(Collectors.toMap(HierarchyElement::getId, p -> p));
        var userAllowedProjects = user.getProjects().stream().map(UserProject::getProjectId).collect(Collectors.toSet());
        var projectRootId = user.getRootProjectId();
        var projectRootIdNull = projectRootId == null;
        var userAccessTable = new HashMap<Object, AccessRights>(checkingProjectTasks.size());
        for (var projectTask: checkingProjectTasks) {
            var isChildOfRoot = false;
            var isOneOfAllowedChildren = false;
            var parentId = projectTask.getParentId();
            var isCurrentTaskAnAllowedProject = userAllowedProjects.contains(projectTask.getId());
            var isRootProject = projectTask.getId().equals(projectRootId);
            if (parentId == null) {
                if (projectRootIdNull)
                    isChildOfRoot = true;
                var accessRow = new AccessRights(isCurrentTaskAnAllowedProject, isChildOfRoot,
                        false, isRootProject);
                userAccessTable.put(projectTask.getId(), accessRow);
                continue;
            }

            if (parentId.equals(projectRootId))
                isChildOfRoot = true;

            HierarchyElement<?> currentParent = parentsMap.get(parentId);
            while (currentParent != null && !isOneOfAllowedChildren) {
                isOneOfAllowedChildren = userAllowedProjects.contains(currentParent.getId());
                currentParent = parentsMap.get(currentParent.getParentId());
            }

            var accessRow = new AccessRights(isCurrentTaskAnAllowedProject, isChildOfRoot,
                    isOneOfAllowedChildren, isRootProject);
            userAccessTable.put(projectTask.getId(), accessRow);

        }

        return userAccessTable;

    }

    private void fillSortableFields(User user) {

        int sort = 0;
        for (var laborRes: user.getUserLaborResources()) {
            laborRes.setSort(sort++);
        }

    }

    private void fillAssistiveData(User user) {
        fillProjectReferences(user);
        fillLaborResourceRep(user);
    }

    private void fillProjectReferences(User user) {
        var projects = user.getProjects();
        var projectIds = Stream.concat(projects.stream().map(UserProject::getProjectId),
                Stream.of(user.getRootProjectId()))
                .filter(Objects::nonNull).toList();
        if (projectIds.isEmpty())
            return;
        var tasksById = projectTaskRepository.findAllById(projectIds).stream().collect(Collectors.toMap(ProjectTask::getId, p -> p));//.getTasksById(projectIds);
        projects.forEach(p -> p.setProject(tasksById.getOrDefault(p.getProjectId(), null)));
        var rootProject = tasksById.get(user.getRootProjectId());
        user.setRootProject(rootProject);
    }

    private void fillLaborResourceRep(User user) {
        var ids = user.getUserLaborResources().stream().map(UserLaborResource::getLaborResourceId).toList();
        if (ids.isEmpty())
            return;
        var map = laborResourceService.getAllById(ids).stream().collect(Collectors.toMap(LaborResourceRepresentation::getId, l -> l));
        user.getUserLaborResources().forEach(userLaborResource -> {
            userLaborResource.setLaborResource(map.get(userLaborResource.getLaborResourceId()));
        });
    }

    private boolean checkIfItemsCanBeDeleted(List<?> ids) {

        var reps = userRepository.findByIdInAndIsPredefinedTrue(ids);
        if (!reps.isEmpty()) {
            var string = reps.stream().map(c -> c.getName() + " with id " + c.getId()).toList().toString();
            throw new StandardError("Cannot remove the users: " + string + ", because they are predefined");
        }
        return true;

    }

}
