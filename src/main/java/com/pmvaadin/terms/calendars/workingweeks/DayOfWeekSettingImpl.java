package com.pmvaadin.terms.calendars.workingweeks;

import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "day_of_week_settings")
@Getter
public class DayOfWeekSettingImpl implements DayOfWeekSetting, HasIdentifyingFields {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Integer version;

    @Setter
    @ManyToOne
    @JoinColumn(name = "working_week_id", nullable = false)
    private WorkingWeekImpl workingWeek;

    @Setter
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Setter
    @Column(name = "interval_id")
    private IntervalSettings intervalSettings = IntervalSettings.DEFAULT;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<IntervalDayOfWeek> intervals = new ArrayList<>();

    @Override
    public void nullIdentifyingFields() {
        this.id = null;
        this.version = null;
    }

}
