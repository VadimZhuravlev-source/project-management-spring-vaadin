package com.pmvaadin.terms.calendars.common;

import com.pmvaadin.terms.calendars.entity.Calendar;

import java.time.LocalTime;
import java.util.List;

public interface Interval {

    static ExceptionLength getExceptionLength(List<Interval> intervals) {

        var duration = 0;
        var durationDay = Calendar.FULL_DAY_SECONDS;

        intervals.forEach(Interval::fillDuration);

        for (Interval interval: intervals) {

            if (interval.getTo().equals(interval.getFrom())
                    && interval.getTo().equals(LocalTime.MIN)) {
                duration = durationDay;
                break;
            }

            var secondOfDayOfTo = 0;
            var to = interval.getTo();
            if (to.equals(LocalTime.MIN))
                secondOfDayOfTo = durationDay;
            else
                secondOfDayOfTo = to.toSecondOfDay();

            duration = duration + secondOfDayOfTo - interval.getFrom().toSecondOfDay();

        }

        if (duration >= durationDay) duration = durationDay;

        return new ExceptionLengthImpl(duration, intervals);

    }

    Integer getId();
    void setId(Integer id);

    Integer getVersion();

    LocalTime getFrom();
    void setFrom(LocalTime from);

    LocalTime getTo();
    void setTo(LocalTime to);

    Interval getInstance();

    int getDuration();
    void fillDuration();

}
