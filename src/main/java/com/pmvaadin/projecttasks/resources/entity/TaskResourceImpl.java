package com.pmvaadin.projecttasks.resources.entity;

import com.pmvaadin.resources.entity.LaborResource;
import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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
    private LaborResource laborResource;

    @Override
    public void nullIdentifyingFields() {
        this.id = null;
        this.version = null;
    }

}
