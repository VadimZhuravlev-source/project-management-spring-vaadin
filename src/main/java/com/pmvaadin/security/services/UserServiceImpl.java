package com.pmvaadin.security.services;

import com.pmvaadin.common.services.ListService;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import com.pmvaadin.security.entities.*;
import com.pmvaadin.security.repositories.UserRepository;
import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService, ListService<UserRepresentation, User> {

    private UserRepository userRepository;
    private ProjectTaskRepository projectTaskRepository;

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
            throw new StandardError("A user with this name exists. Please choose a different name.");
        var savedUser = userRepository.save(user);
        fillProjectReferences(savedUser);
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
        fillProjectReferences(foundUser);
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
        fillProjectReferences(user);
        if (user instanceof HasIdentifyingFields)
            ((HasIdentifyingFields) user).nullIdentifyingFields();

        return user;

    }

    // End of the List service

    @Override
    public void addProjectTaskToUserProject(ProjectTask projectTask, List<ProjectTask> parents) {

        var user = getCurrentUser();
        var isUserFollowedRLS = isUserFollowedRLS(user);
        if (!isUserFollowedRLS)
            return;
        var pids = parents.stream().map(ProjectTask::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        var projects = user.getProjects();
        var projectIds = projects.stream().map(UserProject::getProjectId).anyMatch(pids::contains);
        if (projectIds)
            return;
        var userProject = user.getUserProjectInstance();
        userProject.setProjectId(projectTask.getId());
        projects.add(userProject);
        user.setProjects(projects);

        userRepository.save(user);

    }

    @Override
    public Integer getRootProject() {

        var user = getCurrentUser();
        var isUserFollowedRLS = isUserFollowedRLS(user);
        if (!isUserFollowedRLS)
            return null;
        return user.getRootProjectId();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            return null;

        var principal = authentication.getPrincipal();
        UserDetails userDetails;
        if (principal instanceof UserDetails) {
            userDetails = (UserDetails) principal;
        } else
            return null;
        var name = userDetails.getUsername();
        return getUserByName(name);
    }

    private boolean isUserFollowedRLS(User user) {
        if (user == null)
            return false;
        var roles = user.getRoles().stream().map(UserRole::getRole).toList();
        if (roles.contains(Role.ADMIN))
            return false;

        return user.getAccessType() == AccessType.ONLY_IN_LIST;

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

    private boolean checkIfItemsCanBeDeleted(List<?> ids) {

        var reps = userRepository.findByIdInAndIsPredefinedTrue(ids);
        if (!reps.isEmpty()) {
            var string = reps.stream().map(c -> c.getName() + " with id " + c.getId()).toList().toString();
            throw new StandardError("Cannot remove the users: " + string + ", because they are predefined");
        }
        return true;

    }

}
