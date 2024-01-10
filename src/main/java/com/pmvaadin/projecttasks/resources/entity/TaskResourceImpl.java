package com.pmvaadin.projecttasks.resources.entity;

import com.pmvaadin.resources.entity.LaborResourceRepresentation;
import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_labor_resources")
public class TaskResourceImpl implements TaskResource, HasIdentifyingFields {

    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    @Setter
    private Integer version;

    @Setter
    @Column(name = "project_task_id")
    private Integer projectTaskId;

    @Setter
    @Column(name = "resource_id")
    private Integer resourceId;

    @Setter
    private BigDecimal duration;

    @Setter
    private int sort;

    @Setter
    @Transient
    private LaborResourceRepresentation laborResource;

    @Override
    public void nullIdentifyingFields() {
        this.id = null;
        this.version = null;
    }

    @Override
    public TaskResource copy() {
        var copiedItem = new TaskResourceImpl();
        copiedItem.projectTaskId = this.projectTaskId;
        copiedItem.resourceId = this.resourceId;
        copiedItem.duration = this.duration;
        copiedItem.laborResource = this.laborResource;
        return copiedItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskResourceImpl that)) return false;
        if (getId() == null || that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        if (getId() == null) return super.hashCode();
        return Objects.hash(getId());
    }

}
