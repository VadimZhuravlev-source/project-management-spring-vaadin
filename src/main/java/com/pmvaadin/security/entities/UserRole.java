package com.pmvaadin.security.entities;

public interface UserRole {

    Integer getId();
    Integer getVersion();
    Role getRole();
    void setRole(Role role);

}
