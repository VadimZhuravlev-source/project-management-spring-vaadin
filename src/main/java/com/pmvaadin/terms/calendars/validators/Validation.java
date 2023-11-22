package com.pmvaadin.terms.calendars.validators;

import com.pmvaadin.terms.calendars.common.Interval;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.workingweeks.WorkingWeek;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public interface Validation {

    void validate(Calendar calendar);
    boolean validate(WorkingWeek workingWeek);
    void validateIntervals(DayOfWeek dayOfWeek, List<Interval> intervals);
    void validateDates(WorkingWeek workingWeek, LocalDate start, LocalDate finish);

}
