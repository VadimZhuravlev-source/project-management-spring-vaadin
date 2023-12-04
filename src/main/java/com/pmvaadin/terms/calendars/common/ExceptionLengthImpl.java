package com.pmvaadin.terms.calendars.common;

import java.util.List;

public record ExceptionLengthImpl(int duration, List<Interval> intervals)
        implements ExceptionLength {
    @Override
    public List<Interval> getIntervals() {
        return intervals;
    }
    @Override
    public int getDuration() {
        return duration;
    }
}
