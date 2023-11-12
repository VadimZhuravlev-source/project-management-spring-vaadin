package com.pmvaadin.terms.calendars.workingweeks;

import com.pmvaadin.terms.calendars.common.Interval;

import java.time.DayOfWeek;
import java.util.List;

public interface WorkingTime {

    Integer getId();
    void setId(Integer id);

    Integer getVersion();

    WorkingWeek getWorkingWeek();

    DayOfWeek getDayOfWeek();
    void setDayOfWeek(DayOfWeek dayOfWeek);

    IntervalSetting getIntervalSetting();
    void setIntervalSetting(IntervalSetting intervalSetting);

    List<Interval> getIntervals();

}
