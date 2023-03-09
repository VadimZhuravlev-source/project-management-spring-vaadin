package com.pmvaadin.projecttasks.entity;

import com.pmvaadin.calendars.entity.Calendar;
import com.pmvaadin.calendars.entity.CalendarImpl;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "project_tasks")
//@EntityListeners(ProjectTaskImpl.class)
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

    // Terms
    @Setter
    @Column(name = "start_date")
    private Date startDate;
    @Setter
    @Column(name = "finish_date")
    private Date finishDate;

    @Setter
    @Column(name = "links_check_sum")
    private int linksCheckSum;

    // The field is not intended to store data
    @Setter
    @Column(name = "children_count")
    @OptimisticLock(excluded = true)
    private int childrenCount;

    //    @Setter
//    @Column(name = "duration")
//    private new BigDecimal duration;
    @ManyToOne
    @JoinColumn(name = "calendar_id")
    private CalendarImpl calendar;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectTaskImpl that)) return false;
        if (getId() == null && that.getId() == null) return false;
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

    @PrePersist
    public void prePersist() {
        childrenCount = 0;
    }

}
