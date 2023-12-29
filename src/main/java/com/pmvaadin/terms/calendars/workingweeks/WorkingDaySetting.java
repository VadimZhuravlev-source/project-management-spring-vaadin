package com.pmvaadin.terms.calendars.workingweeks;

import com.pmvaadin.terms.calendars.common.Interval;

import java.time.DayOfWeek;
import java.util.List;

public interface WorkingDaySetting {

    DayOfWeek getDayOfWeek();
    void setDayOfWeek(DayOfWeek dayOfWeek);

    List<Interval> getIntervals();

}
