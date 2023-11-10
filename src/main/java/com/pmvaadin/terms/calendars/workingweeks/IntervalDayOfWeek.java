package com.pmvaadin.terms.calendars.workingweeks;

import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import com.pmvaadin.terms.calendars.common.Interval;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "intervals")
@Getter
public class IntervalDayOfWeek implements Interval, HasIdentifyingFields {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "working_day_setting_id", nullable = false)
    private DayOfWeekSettingImpl workingDaySetting;

    @Version
    private Integer version;

    @Setter
    private LocalTime from;

    @Setter
    private LocalTime to;

    @Setter
    private Integer sort;

    @Override
    public void nullIdentifyingFields() {
        this.id = null;
        this.version = null;
    }

}
