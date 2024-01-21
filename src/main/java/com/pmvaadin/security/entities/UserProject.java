package com.pmvaadin.security.entities;

import com.pmvaadin.projecttasks.entity.ProjectTask;

public interface UserProject {

    Integer getId();
    User getUser();
    Integer getProjectId();
    void setProjectId(Integer id);
    ProjectTask getProject();
    void setProject(ProjectTask project);

}
