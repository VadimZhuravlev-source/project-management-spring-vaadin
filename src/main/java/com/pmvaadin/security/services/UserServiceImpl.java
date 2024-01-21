package com.pmvaadin.security.services;

import com.pmvaadin.common.services.ListService;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projecttasks.services.ProjectTaskService;
import com.pmvaadin.security.entities.*;
import com.pmvaadin.security.repositories.UserRepository;
import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService, ListService<UserRepresentation, User> {

    private UserRepository userRepository;
    private ProjectTaskService projectTaskService;

    @Override
    public User getUserByName(String name) {
        return userRepository.findByName(name).orElse(null);
    }

    // ListService

    @Override
    public User save(User user) {
        var savedUser = userRepository.save(user);
        fillNamesOfProjects(savedUser);
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
        fillNamesOfProjects(foundUser);
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
        fillNamesOfProjects(user);
        if (user instanceof HasIdentifyingFields)
            ((HasIdentifyingFields) user).nullIdentifyingFields();

        return user;

    }

    private void fillNamesOfProjects(User user) {
        var projects = user.getProjects();
        if (projects.isEmpty())
            return;
        var projectIds = projects.stream().map(UserProject::getProjectId).filter(Objects::nonNull).toList();
        var mapIdName = projectTaskService.getTasksById(projectIds);
        projects.forEach(p -> p.setProject(mapIdName.getOrDefault(p.getProject(), null)));
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
