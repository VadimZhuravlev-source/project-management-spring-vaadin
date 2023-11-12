package com.pmvaadin.terms.calendars.workingweeks;

import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import com.pmvaadin.terms.calendars.common.Interval;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "working_time_intervals")
@Getter
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
    private int sort;

    @Override
    public void nullIdentifyingFields() {
        this.id = null;
        this.version = null;
    }

}
