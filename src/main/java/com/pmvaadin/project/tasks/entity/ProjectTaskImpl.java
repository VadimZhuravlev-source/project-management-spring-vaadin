package com.pmvaadin.project.tasks.entity;

import com.pmvaadin.project.resources.entity.TaskResource;
import com.pmvaadin.project.resources.entity.TaskResourceImpl;
import com.pmvaadin.terms.timeunit.entity.TimeUnitRepresentation;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.*;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "project_tasks")
public class ProjectTaskImpl implements ProjectTask, Serializable {

    // hierarchy and order fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    @Column(name = "id")
    private Integer id;

    @Setter
    @Column(name = "parent_id")
    private Integer parentId;

    @Setter
    @Column(name = "level_order")
    private Integer levelOrder;

    // service fields
    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "date_of_creation")
    @CreationTimestamp
    private Date dateOfCreation;

    @Column(name = "update_date")
    @UpdateTimestamp
    private Date updateDate;

    // Fields of project task properties
    @Setter
    @Column(name = "name")
    private String name;

    @Setter
    @Transient
    private String wbs;

    @Setter
    @Column(name = "is_project")
    private boolean isProject = false;

    // Terms begin
    @Setter
    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Setter
    @Column(name = "finish_date")
    private LocalDateTime finishDate;

    @Setter
    @Column(name = "duration")
    private long duration = 1L;

    @Setter
    @Column(name = "schedule_mode_id")
    private ScheduleMode scheduleMode = ScheduleMode.AUTO;

    //@ManyToOne
    @Setter
    //@JoinColumn(name = "calendar_id")
    @Column(name = "calendar_id")
    private Integer calendarId;
    // Terms end

    @Setter
    @Column(name = "links_check_sum")
    private int linksCheckSum;

    @Setter
    @Column(name = "time_unit_id")
    private Integer timeUnitId;

    @Setter
    @Column(name = "status_id")
    private Status status = Status.PLANNED;

    @Setter
    @Column(name = "progress")
    private int progress;

    @Setter
    @Column(name = "is_milestone")
    private boolean isMilestone;

    @Setter
    @Column(name = "resources_check_sum")
    private int resourcesCheckSum;

    // The field is not intended to store data
    @Setter
    @Transient
    private int amountOfChildren;

    @Setter
    @Transient
    private String calendarRepresentation;

    @Setter
    @Transient
    private ProjectTask parent;

    @Setter
    @Transient
    private BigDecimal durationRepresentation;

    @Setter
    @Transient
    private String timeUnitRepresentation;

    @Setter
    @Transient
    private TimeUnitRepresentation timeUnit;

    @Setter
    @Transient
    private String linkRepresentation;

    @Setter
    @Transient
    private String laborResourceRepresentation;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectTaskImpl that)) return false;
        if (getId() == null || that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        if (getId() == null) return super.hashCode();
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "ProjectTaskImpl{" +
                "name=" + name +
                ", id=" + id +
                ", version=" + version +
                ", parentId=" + parentId +
                ", wbs='" + wbs + '\'' +
                '}';
    }

    @Override
    public boolean isNew() {
        return getId() == null;
    }

    @Override
    public Integer getNullId() {
        return 0;
    }

    @Override
    public void setUniqueValueIfParentIdNull() {
        if (this.parentId == null)
            this.parentId = Integer.MIN_VALUE;
    }

    @Override
    public void revertParentIdNull() {
        if (this.parentId != null && this.parentId.equals(Integer.MIN_VALUE))
            this.parentId = null;
    }

    @Override
    public TaskResource getTaskResourceInstance() {
        var taskResource = new TaskResourceImpl();
        taskResource.setProjectTaskId(this.id);
        return taskResource;
    }

}
