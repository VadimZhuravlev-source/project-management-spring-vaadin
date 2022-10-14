package com.PMVaadin.PMVaadin.Entities.Links;

import com.PMVaadin.PMVaadin.Entities.ProjectTask.ProjectTask;

public interface Link {

    Integer getId();
    void setId(Integer id);
    Integer getVersion();

    Integer getProjectTaskId();
    void setProjectTaskId(Integer id);
    Integer getLinkedProjectTaskId();
    void setLinkedProjectTaskId(Integer id);
//    ProjectTask getProjectTask();
//    void setProjectTask(ProjectTask projectTask);
//    ProjectTask getLinkedProjectTask();
//    void setLinkedProjectTask(ProjectTask projectTask);

    LinkType getLinkType();
    void setLinkType(LinkType linkType);

}
