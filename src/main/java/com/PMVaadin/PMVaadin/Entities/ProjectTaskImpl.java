package com.PMVaadin.PMVaadin.Entities;

import com.PMVaadin.PMVaadin.Entities.Calendar.Calendar;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
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

    // Terms
    @Setter
    @Column(name = "start_date")
    private Date startDate;
    @Setter
    @Column(name = "finish_date")
    private Date finishDate;
//    @Setter
//    @Column(name = "duration")
//    private BigDecimal duration;
    @ManyToOne
    @JoinColumn(name = "calendar_id")
    private Calendar calendar;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectTask projectTask)) return false;

        boolean equalsId;
        Integer id = getId();
        if (id == null){
            equalsId = id == projectTask.getId();
        } else {
            equalsId = id.equals(projectTask.getId());
        }

        return equalsId;

    }

    @Override
    public int hashCode() {
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

}
