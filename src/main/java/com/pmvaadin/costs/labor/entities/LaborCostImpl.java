package com.pmvaadin.costs.labor.entities;

import com.pmvaadin.projecttasks.entity.ProjectTaskRep;
import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "labor_costs")
public class LaborCostImpl implements LaborCost, HasIdentifyingFields {

    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Integer version;

    @Column(name = "date_of_creation")
    @CreationTimestamp
    private Date dateOfCreation;

    @Column(name = "update_date")
    @UpdateTimestamp
    private Date updateDate;

    @Setter
    private String name;

    @Setter
    @Transient
    private String employeeName;

    @Setter
    @Column(name = "employee_id")
    private Integer employeeId;

    @Setter
    @Column(name = "day")
    private LocalDate day = LocalDate.now();

    @OneToMany(mappedBy = "laborCost",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("from ASC")
    private List<WorkIntervalImpl> intervals = new ArrayList<>();

    @Transient
    @Setter
    private List<ProjectTaskRep> assignedTasks = new ArrayList<>();

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LaborCostImpl that)) return false;
        if (getId() == null || that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        if (getId() == null) return super.hashCode();
        return Objects.hash(getId());
    }

    @Override
    public void nullIdentifyingFields() {
        this.id = null;
        this.version = null;
        dateOfCreation = null;
        updateDate = null;
        intervals.forEach(WorkIntervalImpl::nullIdentifyingFields);
        assignedTasks.clear();
    }

    @Override
    public List<WorkInterval> getIntervals() {
        return new ArrayList<>(intervals);
    }

    @Override
    public void setIntervals(List<WorkInterval> intervals) {
        this.intervals = intervals.stream().map(i -> (WorkIntervalImpl) i).toList();
    }

    @Override
    public LaborCostRepresentation getRep() {
        return new LaborCostRepresentationDTO(id, name, day);
    }

}
