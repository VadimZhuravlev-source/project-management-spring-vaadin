package com.pmvaadin.security.entities;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "user_projects")
@Getter
public class UserProjectImpl implements UserProject, HasIdentifyingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Integer id;

    @Version
    private Integer version;

    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserImpl user;

    @Setter
    @Column(name = "project_id")
    private Integer projectId;

    @Setter
    @Transient
    private ProjectTask project;

    @Override
    public void nullIdentifyingFields() {
        this.id = null;
    }

    @Override
    public String toString() {
        return projectId.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProjectImpl that)) return false;
        return Objects.equals(getUser(), that.getUser()) && Objects.equals(this.getProjectId(), that.getProjectId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser(), this.getProjectId());
    }

}
