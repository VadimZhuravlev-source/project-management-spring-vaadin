package com.pmvaadin.terms.calendars.validators;

import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.terms.calendars.common.Interval;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.workingweeks.WorkingWeek;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ValidationImpl implements Validation {

    private Calendar calendar;
    private WorkingWeek week;

    @Override
    public void validate(Calendar calendar) {
        this.calendar = calendar;
        this.calendar.getWorkingWeeks().forEach(this::validateWorkingWeekTerms);
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
            if (interval.getFrom().compareTo(interval.getTo()) >= 0)
                throw new StandardError("The start of the shift must be later than the end in the " + dayOfWeek + ".");
            if (previousTo != null && previousTo.compareTo(interval.getFrom()) > 0)
                throw new StandardError("The start of the shift must be later than the end of the previous shift in the " + dayOfWeek + ".");
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

        var foundedWeekOpt = this.calendar.getWorkingWeeks().stream()
                .filter(ww -> !(ww == workingWeek || ww.isDefault()))
                .filter(ww ->
                        !(ww.getStart().compareTo(workingWeek.getFinish()) > 0 || ww.getFinish().compareTo(workingWeek.getStart()) < 0)
                ).findFirst();

        if (foundedWeekOpt.isEmpty()) return;
        var foundedWeek = foundedWeekOpt.get();

        var rep = foundedWeek.getName() + ": start at " + foundedWeek.getStart() + ", end at " + foundedWeek.getFinish();

        throw new StandardError("Terms of current working week overlap with " + rep);

    }

    private void validateWorkingTimes() {
        var workingTimes = week.getWorkingTimes();
        if (workingTimes.size() != 7)
            throw new StandardError("Number of days in Working week do not equals 7");

        workingTimes.forEach(workingTime -> validateIntervals(workingTime.getDayOfWeek(), workingTime.getIntervals()));

    }


}
