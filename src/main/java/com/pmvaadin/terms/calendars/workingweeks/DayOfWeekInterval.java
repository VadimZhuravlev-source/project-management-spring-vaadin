package com.pmvaadin.terms.calendars.workingweeks;

import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import com.pmvaadin.terms.calendars.common.Interval;
import com.pmvaadin.terms.calendars.entity.Calendar;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "working_time_intervals")
@Getter
@NoArgsConstructor
public class DayOfWeekInterval implements Interval, HasIdentifyingFields {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Integer version;

    @Setter
    @ManyToOne
    @JoinColumn(name = "working_time_id", nullable = false)
    private WorkingTimeImpl workingTime;

    @Setter
    @Column(name = "from_time")
    private LocalTime from;

    @Setter
    @Column(name = "to_time")
    private LocalTime to;

    @Setter
    @Transient
    private int duration = 0;

    public DayOfWeekInterval(WorkingTimeImpl workingTime, LocalTime from, LocalTime to) {
        this.workingTime = workingTime;
        this.from = from;
        this.to = to;
    }

    public DayOfWeekInterval(DayOfWeekInterval interval) {

        this.id = interval.id;
        this.version = interval.version;
        this.workingTime = interval.workingTime;
        this.from = interval.from;
        this.to = interval.to;

    }

    @Override
    public void nullIdentifyingFields() {
        this.id = null;
        this.version = null;
    }

    @Override
    public Interval getInstance() {
        return new DayOfWeekInterval();
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
