package com.pmvaadin.projecttasks.links.entities;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.terms.timeunit.entity.TimeUnit;

import java.math.BigDecimal;

public interface Link {

    Integer getId();
    void setId(Integer id);
    Integer getVersion();

    Integer getSort();
    void setSort(Integer sort);

    Integer getProjectTaskId();
    void setProjectTaskId(Integer id);
    Integer getLinkedProjectTaskId();
    void setLinkedProjectTaskId(Integer id);
    ProjectTask getLinkedProjectTask();
    void setLinkedProjectTask(ProjectTask projectTask);
    String getRepresentation();
    void setRepresentation(String representation);

    LinkType getLinkType();
    void setLinkType(LinkType linkType);

    long getLag();
    void setLag(long lag);

    BigDecimal getLagRepresentation();
    void setLagRepresentation(BigDecimal lagRepresentation);

    Link getInstance();
    Link copy(Link link);

    TimeUnit getTimeUnit();
    void setTimeUnit(TimeUnit timeUnit);

}
