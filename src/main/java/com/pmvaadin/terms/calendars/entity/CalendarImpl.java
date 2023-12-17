package com.pmvaadin.terms.calendars.entity;

import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import com.pmvaadin.terms.calendars.common.Interval;
import com.pmvaadin.terms.calendars.OperationListenerForCalendar;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.terms.calendars.exceptions.CalendarException;
import com.pmvaadin.terms.calendars.exceptions.CalendarExceptionImpl;
import com.pmvaadin.terms.calendars.common.ExceptionLength;
import com.pmvaadin.terms.calendars.workingweeks.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@EntityListeners(OperationListenerForCalendar.class)
@Getter
@NoArgsConstructor
@Table(name = "calendars")
public class CalendarImpl implements Calendar, Serializable, HasIdentifyingFields {

    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    @Setter
    private Integer version;

    @Setter
    private String name;

    @Setter
    @Column(name = "settings_id")
    private CalendarSettings setting = CalendarSettings.STANDARD;

    @Setter
    @Column(name = "start_time")
    private LocalTime startTime = LocalTime.of(9, 0);

    @Setter
    @Column(name = "predefined")
    private boolean isPredefined;

    @Setter
    @Column(name = "end_of_week")
    private DayOfWeek endOfWeek = DayOfWeek.SUNDAY;

    @Setter
    @OneToMany(mappedBy = "calendar",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @OrderBy("sort ASC")
    private List<CalendarExceptionImpl> exceptions = new ArrayList<>();

    @OneToMany(mappedBy = "calendar",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @OrderBy("sort ASC")
    private List<WorkingWeekImpl> workingWeeks = new ArrayList<>();

    @Transient
    private Map<LocalDate, ExceptionLength> exceptionDays;

    @Transient
    private Map<DayOfWeek, ExceptionLength> workingTimesOfDefaultWorkingWeek;

    public CalendarImpl(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        if (id != null) return Objects.hash(id);
        else return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof CalendarImpl that)) {
            return false;
        }

        return getId().equals(that.getId());
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean isNew() {
        return id == null;
    }

    @Override
    public void nullIdentifyingFields() {

        this.id = null;
        this.version = null;
        this.isPredefined = false;

        workingWeeks.forEach(WorkingWeekImpl::nullIdentifyingFields);
        exceptions.forEach(CalendarExceptionImpl::nullIdentifyingFields);

    }

    @Override
    public WorkingWeek getWorkingWeekInstance() {
        return new WorkingWeekImpl().getInstance(this);
    }

    @Override
    public CalendarException getCalendarExceptionInstance() {
        return CalendarExceptionImpl.getInstance(this);
    }

    @Override
    public List<CalendarException> getCalendarExceptions() {
        return this.exceptions.stream().map(ce -> (CalendarException) ce).collect(Collectors.toList());
    }

    @Override
    public void setCalendarExceptions(List<CalendarException> calendarExceptions) {
        this.exceptions = calendarExceptions.stream().map(ce -> (CalendarExceptionImpl) ce).collect(Collectors.toList());
    }

    @Override
    public List<WorkingWeek> getWorkingWeeks() {
        if (this.id == null || this.workingWeeks.size() == 0) {
            var defaultWorkingWeek = WorkingWeekImpl.getDefaultInstance(this);
            this.workingWeeks.add(defaultWorkingWeek);
        }
        return this.workingWeeks.stream().map(workingWeek -> (WorkingWeek) workingWeek).collect(Collectors.toList());
    }

    @Override
    public void setWorkingWeeks(List<WorkingWeek> workingWeeks) {
        this.workingWeeks = workingWeeks.stream().map(workingWeek -> (WorkingWeekImpl) workingWeek).toList();
    }

    @Override
    public Calendar getDefaultCalendar() {

        Calendar calendar = new CalendarImpl("Standard");
        calendar.setSetting(CalendarSettings.STANDARD);
        return calendar;

    }

    @Override
    public void fillWorkingWeekSort() {
        var sort = 0;
        for (WorkingWeek workingWeek: workingWeeks) {
            workingWeek.setSort(sort++);
        }
    }
    @Override
    public void fillExceptionSort() {
        var sort = 0;
        for (CalendarException calendarException1: exceptions) {
            calendarException1.setSort(sort++);
        }
    }

    @Override
    public long getDuration(LocalDateTime start, LocalDateTime finish) {

        initiateCacheData();
        return getDurationWithoutInitiateCache(start, finish);

    }

    @Override
    public LocalDateTime getDateByDuration(LocalDateTime date, long duration) {

        initiateCacheData();
        return getDateByDurationWithoutInitiateCache(date, duration);

    }

    @Override
    public long getDurationWithoutInitiateCache(LocalDateTime start, LocalDateTime finish) {

        if (start.compareTo(finish) > 0) throw new StandardError("Illegal argument exception");
        if (start.equals(finish)) return 0;

        var startDay = start.toLocalDate();
        var startTime = start.toLocalTime();

        if (exceptionDays == null || workingTimesOfDefaultWorkingWeek == null) initiateCacheData();

        var duration = 0L;
        var finishDay = finish.toLocalDate();
        var finishTime = finish.toLocalTime();
        // increase days
        while (startDay.compareTo(finishDay) < 0) {

            var exception = getExceptionFromCache(startDay);

            if (startTime.equals(LocalTime.MIN)) {
                duration = duration + exception.getDuration();
            } else {
                for (Interval interval : exception.getIntervals()) {
                    if (startTime.compareTo(interval.getFrom()) < 0) {
                        startTime = interval.getFrom();
                    }
                    if (startTime.compareTo(interval.getTo()) >= 0 && !interval.getTo().equals(LocalTime.MIN))
                        continue;
                    var secondOfDayTo = interval.getTo().toSecondOfDay();
                    if (interval.getTo().equals(LocalTime.MIN))
                        secondOfDayTo = Calendar.FULL_DAY_SECONDS;
                    duration = duration + secondOfDayTo - startTime.toSecondOfDay();
                }
            }

            startDay = startDay.plusDays(1);
            startTime = LocalTime.MIN;

        }

        var exception = getExceptionFromCache(startDay);
        if (exception.getDuration() == 0) {
            return duration;
        }

        for (Interval interval : exception.getIntervals()) {
            if (startTime.compareTo(interval.getFrom()) < 0)
                startTime = interval.getFrom();

            if (startTime.compareTo(finishTime) >= 0)
                break;
            if (interval.getTo().equals(LocalTime.MIN) || finishTime.compareTo(interval.getTo()) <= 0) {
                duration = duration + finishTime.toSecondOfDay() - startTime.toSecondOfDay();
                break;
            }

            duration = duration + interval.getTo().toSecondOfDay() - startTime.toSecondOfDay();

        }

        return duration;

    }

    @Override
    public LocalDateTime getDateByDurationWithoutInitiateCache(LocalDateTime date, long duration) {

        if (duration == 0L) return date;

        boolean isAscend = duration > 0L;

        DateComputationVersion2 dateComputation = new DateComputationVersion2();
        if (exceptionDays == null || workingTimesOfDefaultWorkingWeek == null) initiateCacheData();

        LocalDateTime startDate;
        if (isAscend) {
            startDate = dateComputation.increaseDateByDuration(date, duration);
        } else {
            duration = -duration;
            startDate = dateComputation.decreaseDateByDuration(date, duration);
        }

        return startDate;

    }

    @Override
    public void initiateCacheData() {

        Map<LocalDate, ExceptionLength> map = new HashMap<>();
        //w.getExceptionAsDayConstraint().forEach((d, e) -> map.put(d, e))
        workingWeeks.forEach(w -> map.putAll(w.getExceptionAsDayConstraint()));
        exceptions.forEach(e -> map.putAll(e.getExceptionAsDayConstraint()));
        exceptionDays = map;
        var defaultWorkingWeek = workingWeeks.stream().filter(WorkingWeekImpl::isDefault)
                .findFirst().orElse(WorkingWeekImpl.getDefaultInstance(this));
        workingTimesOfDefaultWorkingWeek = new HashMap<>(DayOfWeek.values().length);
        defaultWorkingWeek.getWorkingTimes().forEach(workingTime -> {
            workingTime.getIntervals().forEach(Interval::fillDuration);
            workingTimesOfDefaultWorkingWeek.put(workingTime.getDayOfWeek(), workingTime.getExceptionLength());
        });

    }

    @Override
    public LocalDateTime getClosestWorkingDay(LocalDateTime date) {
        initiateCacheData();
        return getClosestWorkingDayWithoutInitiateCache(date);
    }

    @Override
    public LocalDateTime getClosestWorkingDayWithoutInitiateCache(LocalDateTime date) {

        if (exceptionDays == null || workingTimesOfDefaultWorkingWeek == null) initiateCacheData();

        var startDay = date.toLocalDate();
        var startTime = date.toLocalTime();
        var exception = getExceptionFromCache(startDay);
        if (exception.getDuration() != 0) {
            for (var interval : exception.getIntervals()) {
                if (startTime.compareTo(interval.getFrom()) < 0)
                    return LocalDateTime.of(startDay, interval.getFrom());
                if (startTime.compareTo(interval.getFrom()) >= 0 && startTime.compareTo(interval.getTo()) < 0
                    || startTime.equals(LocalTime.MIN) && interval.getTo().equals(LocalTime.MIN))
                    return date;
            }
        }
        startTime = LocalTime.MIN;

        do {
            startDay = startDay.plusDays(1);
            exception = getExceptionFromCache(startDay);
        } while (exception.getDuration() == 0 || exception.getIntervals().isEmpty());

        for (var interval : exception.getIntervals()) {
            startTime = interval.getFrom();
            break;
        }

        return LocalDateTime.of(startDay, startTime);

    }

    @Override
    public LocalDateTime getEndOfWorkingDay(LocalDate day) {

        initiateCacheData();
        var newDate = getClosestWorkingDayWithoutInitiateCache(LocalDateTime.of(day, LocalTime.MIN));

        var newDay = newDate.toLocalDate();
        var newTime = newDate.toLocalTime();
        var exception = getExceptionFromCache(newDay);

        var intervals = exception.getIntervals();

        if (!intervals.isEmpty()) {
            newTime = intervals.get(intervals.size() - 1).getTo();
        }
//        var intervals = exception.getIntervals();
//
//        var iterator = intervals.listIterator(intervals.size());
//        while (iterator.hasPrevious()) {
//            var interval = iterator.previous();
//            newTime = interval.getTo();
//            break;
//        }

        if (newTime.equals(LocalTime.MIN))
            newDay = newDay.plusDays(1);

        return LocalDateTime.of(newDay, newTime);

    }

    private ExceptionLength getExceptionFromCache(LocalDate date) {
        var exception = exceptionDays.get(date);
        if (exception == null) {
            exception = workingTimesOfDefaultWorkingWeek.get(date.getDayOfWeek());
        }
        return exception;
    }

    // Classes

    private class DateComputationVersion2 {

        private final static int fullDay = Calendar.FULL_DAY_SECONDS;
        private LocalDate day;
        private LocalTime time;
        private long remainedDuration;
        private Function<List<Interval>, Boolean> intervalCalculation;
        private DayChanger dayChanger;

        private LocalDateTime increaseDateByDuration(LocalDateTime date, long duration) {
            init(date, duration);
            intervalCalculation = this::calculateIntervalsAscendOrder;
            dayChanger = this::plusDay;
            return calculateDate();
        }

        private LocalDateTime calculateDate() {

            var counter = 0;
            var limit = 10000000; // limit of number of day
            while (remainedDuration > 0 && ++counter < limit) {

                var exception = getExceptionFromCache(day);
                if (exception.getDuration() != 0) {
                    if (time.equals(LocalTime.MIN) && exception.getDuration() < remainedDuration)
                        remainedDuration = remainedDuration - exception.getDuration();
                    else {
                        var returnDate = intervalCalculation.apply(exception.getIntervals());
                        if (returnDate) return LocalDateTime.of(day, time);
                    }
                }
                dayChanger.change();

            }

            if (counter == limit)
                throw new StandardError("The calculated duration has exceeded the limit of 10 000 000 days. Please contact the developers.");

            if (time.equals(LocalTime.MAX)) {
                time = LocalTime.MIN;
                day = day.plusDays(1);
            }

            return LocalDateTime.of(day, time);

        }

        private boolean calculateIntervalsAscendOrder(List<Interval> intervals) {

            for (Interval interval : intervals) {

                var intervalEnd = interval.getTo();
                var intervalEndIsMin = intervalEnd.equals(LocalTime.MIN);
                if (!intervalEndIsMin && time.compareTo(intervalEnd) >= 0)
                    continue;

                var from = interval.getFrom();
                if (time.compareTo(from) < 0)
                    time = from;

                var secondsToIntervalEnd = 0;
                if (intervalEndIsMin) {
                    secondsToIntervalEnd = fullDay;
                } else
                    secondsToIntervalEnd = intervalEnd.toSecondOfDay();

                var availableDuration = secondsToIntervalEnd - time.toSecondOfDay();
                if (availableDuration >= remainedDuration) {
                    time = time.plusSeconds(remainedDuration);
                    if (time.equals(LocalTime.MIN))
                        day = day.plusDays(1);
                    return true;
                } else {
                    remainedDuration = remainedDuration - availableDuration;
                }

                if (intervalEndIsMin)
                    break;

            }
            return false;
        }

        private void plusDay() {
            day = day.plusDays(1);
            time = LocalTime.MIN;
        }

        private void init(LocalDateTime date, long duration) {
            remainedDuration = duration;
            day = date.toLocalDate();
            time = date.toLocalTime();
        }

        private LocalDateTime decreaseDateByDuration(LocalDateTime date, long duration) {
            init(date, duration);
            intervalCalculation = this::calculateIntervalsDescendOrder;
            dayChanger = this::minusDay;
            return calculateDate();
        }

        private boolean calculateIntervalsDescendOrder(List<Interval> intervals) {

            var iterator = intervals.listIterator(intervals.size());
            while (iterator.hasPrevious()) {
                var interval = iterator.previous();

                var intervalStart = interval.getFrom();
                var intervalStartIsMin = intervalStart.equals(LocalTime.MIN);
                if (!intervalStartIsMin && time.compareTo(intervalStart) <= 0)
                    continue;

                var intervalEnd = interval.getTo();
                if (time.compareTo(intervalEnd) > 0)
                    time = intervalEnd;

                var secondsToIntervalStart = intervalStart.toSecondOfDay();
                var secondsTime = time.toSecondOfDay();
                if (time.equals(LocalTime.MAX)) secondsTime = fullDay;
                var availableDuration = secondsToIntervalStart - secondsTime;
                if (intervalStartIsMin && time.equals(intervalStart))
                    availableDuration = fullDay;
                availableDuration = Math.abs(availableDuration);
                if (availableDuration >= remainedDuration) {
                    time = time.minusSeconds(remainedDuration);
                    if (time.equals(LocalTime.MIN)
                            && availableDuration == remainedDuration
                            && remainedDuration == fullDay)
                        day = day.minusDays(1);
                    return true;
                } else {
                    remainedDuration = remainedDuration - availableDuration;
                }

                if (intervalStartIsMin)
                    break;

            }
            return false;
        }

        private void minusDay() {
            day = day.minusDays(1);
            time = LocalTime.MAX;
        }

        @FunctionalInterface
        interface DayChanger {
            void change();
        }

    }

}

