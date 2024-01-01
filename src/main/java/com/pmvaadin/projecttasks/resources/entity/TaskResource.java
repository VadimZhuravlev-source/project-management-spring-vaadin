package com.pmvaadin.projecttasks.resources.entity;

import java.math.BigDecimal;

public interface TaskResource {

    Integer getId();

    void setId(Integer id);

    Integer getVersion() ;

    Integer getProjectTaskId();

    void setProjectTaskId(Integer projectTaskId);

    Integer getResourceId();

    void setResourceId(Integer resourceId);

    BigDecimal getDuration();

    void setDuration(BigDecimal duration);

    String getName();

    void setName(String resourceName);

}
