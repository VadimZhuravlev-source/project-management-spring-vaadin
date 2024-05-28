package com.pmvaadin.security.user.labor.resource;

import com.pmvaadin.resources.labor.entity.LaborResourceRepresentation;
import com.pmvaadin.security.entities.UserImpl;

public interface UserLaborResource {

    UserImpl getUser();

    void setUser(UserImpl user);

    Integer getId();

    void setId(Integer id);

    Integer getLaborResourceId();

    void setLaborResourceId(Integer laborResourceId);

    Integer getVersion();

    LaborResourceRepresentation getLaborResource();
    void setLaborResource(LaborResourceRepresentation laborResource);

}
