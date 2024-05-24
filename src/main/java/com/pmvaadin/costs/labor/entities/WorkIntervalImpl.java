package com.pmvaadin.costs.labor.entities;

import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import com.pmvaadin.terms.calendars.entity.Calendar;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "labor_cost_intervals")
@Getter
@NoArgsConstructor
public class WorkIntervalImpl implements WorkInterval, HasIdentifyingFields {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Integer version;

    @Setter
    @Transient
    private String Name;

    @Setter
    @Column(name = "task_id")
    private Integer taskId;

    @ManyToOne
    @JoinColumn(name = "labor_cost_id", nullable = false)
    private LaborCostImpl laborCost;

    @Setter
    @Column(name = "from_time")
    private LocalTime from;

    @Setter
    @Column(name = "to_time")
    private LocalTime to;

    @Setter
    private int duration = 0;

    public WorkIntervalImpl(LaborCostImpl laborCost, LocalTime from, LocalTime to) {
        this.laborCost = laborCost;
        this.from = from;
        this.to = to;
    }

    public WorkIntervalImpl(WorkIntervalImpl interval) {

        this.id = interval.id;
        this.version = interval.version;
        this.laborCost = interval.laborCost;
        this.from = interval.from;
        this.to = interval.to;

    }

    @Override
    public void setLaborCost(LaborCost laborCost) {
        this.laborCost = (LaborCostImpl) laborCost;
    }

    @Override
    public void nullIdentifyingFields() {
        this.id = null;
        this.version = null;
    }

    @Override
    public WorkInterval getInstance() {
        return new WorkIntervalImpl();
    }

    @Override
    public int getDuration() {
        return this.duration;
    }

    @Override
    public void fillDuration() {
        var to = this.to.toSecondOfDay();
        if (this.to.equals(LocalTime.MIN)) to = Calendar.FULL_DAY_SECONDS;
        this.duration = to - this.from.toSecondOfDay();
    }


}
