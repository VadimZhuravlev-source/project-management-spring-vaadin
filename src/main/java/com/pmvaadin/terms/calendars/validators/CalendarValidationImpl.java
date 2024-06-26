package com.pmvaadin.terms.calendars.validators;

import com.pmvaadin.project.structure.StandardError;
import com.pmvaadin.terms.calendars.common.Interval;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.exceptions.CalendarException;
import com.pmvaadin.terms.calendars.workingweeks.WorkingWeek;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;

public class CalendarValidationImpl implements CalendarValidation {

    private Calendar calendar;
    private WorkingWeek week;

    @Override
    public boolean validate(Calendar calendar) {
        this.calendar = calendar;
        this.calendar.getWorkingWeeks().forEach(this::validateWorkingWeekTerms);
        validateExceptions();
        return true;
    }

    @Override
    public boolean validate(WorkingWeek workingWeek) {
        this.week = workingWeek;
        validateDates(this.week, this.week.getStart(), this.week.getFinish());
        validateWorkingTimes();
        return true;
    }
    @Override
    public void validateIntervals(DayOfWeek dayOfWeek, List<Interval> intervals) {

        LocalTime previousTo = null;
        for (Interval interval: intervals) {
            if (interval.getFrom().compareTo(interval.getTo()) >= 0 && !interval.getTo().equals(LocalTime.MIN))
                throw new StandardError("The start of the shift must be later than the end in the " + dayOfWeek + ".");
            if (previousTo != null && previousTo.compareTo(interval.getFrom()) > 0)
                throw new StandardError("The start of the shift must be later than the end of the previous shift in the " + dayOfWeek + ".");
            previousTo = interval.getTo();
        }

    }

    @Override
    public void validateIntervals(List<Interval> intervals) {

        LocalTime previousTo = null;
        for (Interval interval: intervals) {
            if (interval.getFrom().compareTo(interval.getTo()) >= 0 && !interval.getTo().equals(LocalTime.MIN))
                throw new StandardError("The start of the shift must be later than the end.");
            if (previousTo != null && previousTo.compareTo(interval.getFrom()) > 0)
                throw new StandardError("The start of the shift must be later than the end of the previous shift.");
            previousTo = interval.getTo();
        }

    }

    @Override
    public void validateDates(WorkingWeek workingWeek, LocalDate start, LocalDate finish) {

        if (!workingWeek.isDefault()) {
            if (start == null || finish == null)
                throw new StandardError("The dates must not be empty.");
            if (start.compareTo(finish) > 0)
                throw new StandardError("The finish must be greater than the start.");
        }

    }

    private void validateWorkingWeekTerms(WorkingWeek workingWeek) {

        // TODO null validation for the principal fields
        WorkingWeek foundedWeek = null;
        for (var ww: this.calendar.getWorkingWeeks()) {
            if (ww == workingWeek || ww.isDefault()) return;
            if (ww.getStart().compareTo(workingWeek.getFinish()) > 0 || ww.getFinish().compareTo(workingWeek.getStart()) < 0) return;
            foundedWeek = ww;
            break;
        }
        if (foundedWeek == null) return;
//        var foundedWeekOpt = this.calendar.getWorkingWeeks().stream()
//                .filter(ww -> !(ww == workingWeek || ww.isDefault()))
//                .filter(ww ->
//                        !(ww.getStart().compareTo(workingWeek.getFinish()) > 0 || ww.getFinish().compareTo(workingWeek.getStart()) < 0)
//                ).findFirst();
//
//        if (foundedWeekOpt.isEmpty()) return;
//        var foundedWeek = foundedWeekOpt.get();

        var rep = foundedWeek.getName() + ": start at " + foundedWeek.getStart() + ", end at " + foundedWeek.getFinish();

        throw new StandardError("Terms of current working week overlap with " + rep);

    }

    private void validateWorkingTimes() {
        var workingTimes = week.getWorkingTimes();
        if (workingTimes.size() != 7)
            throw new StandardError("Number of days in Working week do not equals 7");

        workingTimes.forEach(workingTime -> validateIntervals(workingTime.getDayOfWeek(), workingTime.getIntervals()));

    }

    private void validateExceptions() {
        var exceptions = this.calendar.getCalendarExceptions();
        var mapDaysException = new HashMap<LocalDate, CalendarException>();
        for (CalendarException exception: exceptions) {
            // TODO null validation for the principal fields
            var map = exception.getExceptionAsDayConstraint();
            map.forEach((localDate, exceptionLength) -> {
                var previousException = mapDaysException.get(localDate);
                if (previousException != null)
                    throw new StandardError(exception + " overlap " + previousException);
                mapDaysException.put(localDate, exception);
            });

        }

    }

}
