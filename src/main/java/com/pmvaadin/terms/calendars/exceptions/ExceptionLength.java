package com.pmvaadin.terms.calendars.exceptions;

import com.pmvaadin.terms.calendars.common.Interval;

import java.util.List;

public interface ExceptionLength {
    List<Interval> getIntervals();
    int getDuration();
}
