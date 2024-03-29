package com.pmvaadin.terms.calendars.workingweeks;

import com.pmvaadin.terms.calendars.common.ExceptionLength;
import com.pmvaadin.terms.calendars.common.Interval;
import com.pmvaadin.terms.calendars.entity.CalendarSettings;

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
    void setIntervals(List<Interval> intervals);
    List<Interval> getCopyOfIntervals();

    Interval getIntervalInstance();

    List<Interval> getDefaultIntervals(DayOfWeek dayOfWeek, CalendarSettings settings);

    void fillDuration();
    int getDuration();
    ExceptionLength getExceptionLength();

}
