package com.pmvaadin.project.links.entities;

import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.pmvaadin.terms.timeunit.entity.TimeUnitRepresentation;

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
    String getWbs();
    void setWbs(String wbs);


    LinkType getLinkType();
    void setLinkType(LinkType linkType);

    long getLag();
    void setLag(long lag);

    BigDecimal getLagRepresentation();
    void setLagRepresentation(BigDecimal lagRepresentation);

    Link getInstance();
    Link copy(Link link);

    TimeUnitRepresentation getTimeUnit();
    void setTimeUnit(TimeUnitRepresentation timeUnit);

//    Integer getTimeUnitId();
//    void setTimeUnitId(Integer timeUnitId);

}
