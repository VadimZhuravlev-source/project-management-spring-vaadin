package com.pmvaadin.costs.labor.entities;

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

    // now, the employeeName is a name of a user.
    @Setter
    @Transient
    private String employeeName;

    @Setter
    @Column(name = "labor_resource_id")
    private Integer labor_resource_id;

    @Setter
    @Column(name = "day")
    private LocalDate day = LocalDate.now();

    @OneToMany(mappedBy = "laborCost",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("from ASC")
    private List<WorkIntervalImpl> intervals = new ArrayList<>();

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LaborCostImpl laborCost = (LaborCostImpl) o;
        return Objects.equals(getId(), laborCost.getId());
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
    }

    @Override
    public WorkInterval getWorkIntervalInstance() {
        var workInterval = new WorkIntervalImpl();
        workInterval.setLaborCost(this);
        return workInterval;
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
