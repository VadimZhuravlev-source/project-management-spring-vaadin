package com.pmvaadin.security.services;

import com.pmvaadin.project.tasks.entity.HierarchyElement;
import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.pmvaadin.security.entities.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface UserService {

    User getUserByName(String name);
    void addProjectTaskToUserProject(List<ProjectTask> projectTasks, User user);
    String getUserName();
    User getCurrentUser();
    boolean isUserFollowingRLS(User user);
    Map<?, AccessRights> getUserAccessTable(Collection<? extends HierarchyElement<?>> checkingProjectTasks, User user, Collection<? extends HierarchyElement<?>> parents);

    record AccessRights(boolean isCurrentTaskAnAllowedProject, boolean isChildOfRoot, boolean isOneOfAllowedChildren, boolean isRootProject) {}

}
