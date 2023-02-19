package com.pmvaadin.projecttasks.links.entities;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "links")
public class LinkImpl implements Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    @Column(name = "id")
    private Integer id;

    // service fields
    @Version
    @Column(name = "version")
    private Integer version;

    @Setter
    @Column(name = "row_order", nullable = false)
    private Integer sort;

    // main fields
    @Setter
    @Column(name = "project_task")
    private Integer projectTaskId;

    @Setter
    @Column(name = "linked_project_task")
    private Integer linkedProjectTaskId;

    @Setter
    @Column(name = "link_type")
    private LinkType linkType;

    @Setter
    @Transient
    private ProjectTask linkedProjectTask;

    @Setter
    @Transient
    private String representation;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinkImpl that)) return false;
        if (getId() == null && that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        if (getId() == null) return super.hashCode();
        return Objects.hash(getId());
    }

}
