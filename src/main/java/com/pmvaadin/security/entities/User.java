package com.pmvaadin.security.entities;

import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.pmvaadin.security.user.labor.resource.UserLaborResource;

import java.util.List;

public interface User {

    Integer getId();
    void setId(Integer id);
    Integer getVersion();

    String getName();
    void setName(String name);

    List<UserRole> getRoles();
    void setRoles(List<UserRole> roles);
    UserRole getUserRoleInstance();

    boolean isActive();
    void setActive(boolean active);

    boolean isPredefined();

    byte[] getPassword();
    void setPassword(byte[] password);

    AccessType getAccessType();
    void setAccessType(AccessType accessType);

    List<UserProject> getProjects();
    void setProjects(List<UserProject> UserProjects);
    UserProject getUserProjectInstance();
    ProjectTask getRootProject();
    void setRootProject(ProjectTask rootProject);
    Integer getRootProjectId();
    void setRootProjectId(Integer id);
    void addProjects(List<UserProject> newProjects);

    UserLaborResource getUserLaborResourceInstance();
    List<UserLaborResource> getUserLaborResources();
    void setUserLaborResources(List<UserLaborResource> laborResources);

}
