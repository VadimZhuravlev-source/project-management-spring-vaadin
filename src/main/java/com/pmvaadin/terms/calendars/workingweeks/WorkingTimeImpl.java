package com.pmvaadin.terms.calendars.workingweeks;

import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import com.pmvaadin.terms.calendars.common.Interval;
import com.pmvaadin.terms.calendars.entity.CalendarSettings;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
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

    @OneToMany(mappedBy = "workingTime")
    @OrderBy("from ASC")
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

    @Override
    public void setIntervals(List<Interval> intervals) {

        this.intervals = intervals.stream().map(i -> (DayOfWeekInterval) i).collect(Collectors.toList());

    }

    @Override
    public List<Interval> getDefaultIntervals(DayOfWeek dayOfWeek, CalendarSettings settings) {

        ArrayList<Interval> list = new ArrayList<>();
        if (settings == CalendarSettings.STANDARD) {
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY)
                return list;
            list.add(new DayOfWeekInterval(this, LocalTime.of(8, 0), LocalTime.of(12, 0)));
            list.add(new DayOfWeekInterval(this, LocalTime.of(13, 0), LocalTime.of(17, 0)));
        } else if (settings == CalendarSettings.NIGHT_SHIFT) {
            if (dayOfWeek == DayOfWeek.SATURDAY) {
                list.add(new DayOfWeekInterval(this, LocalTime.of(0, 0), LocalTime.of(3, 0)));
                list.add(new DayOfWeekInterval(this, LocalTime.of(4, 0), LocalTime.of(8, 0)));
                return list;
            }
            if (dayOfWeek == DayOfWeek.SUNDAY)
                return list;
            if (dayOfWeek == DayOfWeek.MONDAY) {
                list.add(new DayOfWeekInterval(this, LocalTime.of(23, 0), LocalTime.of(0, 0)));
                return list;
            }
            list.add(new DayOfWeekInterval(this, LocalTime.of(0, 0), LocalTime.of(3, 0)));
            list.add(new DayOfWeekInterval(this, LocalTime.of(4, 0), LocalTime.of(8, 0)));
            list.add(new DayOfWeekInterval(this, LocalTime.of(23, 0), LocalTime.of(0, 0)));
        } else if (settings == CalendarSettings.FULL_DAY) {
            list.add(new DayOfWeekInterval(this, LocalTime.of(0, 0), LocalTime.of(3, 0)));
        }

        return list;

    }

    @Override
    public Interval getIntervalInstance() {
        var interval = new DayOfWeekInterval();
        interval.setWorkingTime(this);
        return interval;
    }

    @Override
    public List<Interval> getCopyOfIntervals() {

        List<Interval> list = new ArrayList<>(intervals.size());

        for (DayOfWeekInterval interval: intervals) {
            var newInterval = new DayOfWeekInterval(interval);
            list.add(newInterval);
        }
        return list;

    }

    public void fillIntervalsByDefault() {
        this.intervals.clear();
        var newIntervals = getDefaultIntervals(this.dayOfWeek, this.workingWeek.getCalendar().getSetting());
        var newInterval2 = newIntervals.stream().map(i -> (DayOfWeekInterval) i).collect(Collectors.toList());
        this.intervals.addAll(newInterval2);
    }

}
