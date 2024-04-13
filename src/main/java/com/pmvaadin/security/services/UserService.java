package com.pmvaadin.security.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.security.entities.User;

import java.util.List;

public interface UserService {

    User getUserByName(String name);
    void addProjectTaskToUserProject(ProjectTask projectTask, List<ProjectTask> parents);

    Integer getRootProject();

}
