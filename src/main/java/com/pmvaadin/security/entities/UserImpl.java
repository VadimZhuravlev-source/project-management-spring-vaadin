package com.pmvaadin.security.entities;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserImpl implements User, HasIdentifyingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Integer id;

    @Version
    private Integer version;

    @Column(name = "name")
    @Setter
    private String name;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<UserRoleImpl> roles = new ArrayList<>();

    @Column(name = "is_active")
    @Setter
    private boolean isActive = true;

    @Column(name = "is_predefined")
    private boolean isPredefined;

    @Column(name = "password")
    @Setter
    private byte[] password = new byte[0];

    @Column(name = "access_type_id")
    @Setter
    private AccessType accessType = AccessType.ONLY_IN_LIST;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<UserProjectImpl> projects = new ArrayList<>();

    @Column(name = "root_project_id")
    @Setter
    private Integer rootProjectId;

    @Transient
    private ProjectTaskImpl rootProject;

    @Override
    public UserRole getUserRoleInstance() {
        var userRole = new UserRoleImpl();
        userRole.setUser(this);
        return userRole;
    }

    @Override
    public List<UserProject> getProjects() {
        return projects.stream().map(u -> (UserProject) u).collect(Collectors.toList());
    }

    @Override
    public void setProjects(List<UserProject> projects) {
        this.projects = projects.stream().map(u -> (UserProjectImpl) u).toList();
    }

    @Override
    public void setRootProject(ProjectTask projectTask) {
        if (!(projectTask instanceof ProjectTaskImpl))
            return;
        this.rootProject = (ProjectTaskImpl) projectTask;
    }

    @Override
    public UserProject getUserProjectInstance() {
        var userRole = new UserProjectImpl();
        userRole.setUser(this);
        return userRole;
    }

    @Override
    public List<UserRole> getRoles() {
        return roles.stream().map(u -> (UserRole) u).collect(Collectors.toList());
    }

    @Override
    public void setRoles(List<UserRole> userRoles) {
        this.roles = userRoles.stream().map(u -> (UserRoleImpl) u).toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserImpl user = (UserImpl) o;
        return Objects.equals(id, user.getId());
    }

    @Override
    public int hashCode() {

        if (id == null) return super.hashCode();
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", name=" + name + ", isActive=" + isActive + '}';
    }

    @Override
    public void nullIdentifyingFields() {
        this.id = null;
        this.version = null;
        roles.forEach(UserRoleImpl::nullIdentifyingFields);
        projects.forEach(UserProjectImpl::nullIdentifyingFields);
        this.isPredefined = false;
    }

}