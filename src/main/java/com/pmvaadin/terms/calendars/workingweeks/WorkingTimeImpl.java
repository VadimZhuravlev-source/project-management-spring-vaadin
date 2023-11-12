package com.pmvaadin.terms.calendars.workingweeks;

import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import com.pmvaadin.terms.calendars.common.Interval;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "working_times")
@Getter
public class WorkingTimeImpl implements WorkingTime, HasIdentifyingFields {

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
    @Column(name = "interval_setting_id")
    private IntervalSetting intervalSetting = IntervalSetting.DEFAULT;

    @Setter
    @OneToMany(mappedBy = "workingTime", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("sort ASC")
    private List<DayOfWeekInterval> intervals = new ArrayList<>();

    @Override
    public void nullIdentifyingFields() {

        this.id = null;
        this.version = null;

        if (intervals == null) return;

        intervals.forEach(DayOfWeekInterval::nullIdentifyingFields);

    }

    @Override
    public List<Interval> getIntervals() {
        return intervals.stream().map(i -> (Interval) i).collect(Collectors.toList());
    }

}
