package com.pmvaadin.terms.calendars.entity;

import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import com.pmvaadin.terms.calendars.common.Interval;
import com.pmvaadin.terms.calendars.dayofweeksettings.DayOfWeekSettings;
import com.pmvaadin.terms.calendars.dayofweeksettings.DefaultDaySetting;
import com.pmvaadin.terms.calendars.exceptiondays.ExceptionDay;
import com.pmvaadin.terms.calendars.OperationListenerForCalendar;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.terms.calendars.exceptions.CalendarException;
import com.pmvaadin.terms.calendars.exceptions.CalendarExceptionImpl;
import com.pmvaadin.terms.calendars.common.ExceptionLength;
import com.pmvaadin.terms.calendars.exceptions.CalendarExceptionInterval;
import com.pmvaadin.terms.calendars.workingweeks.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
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
    @OneToMany(mappedBy = "calendar",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ExceptionDay> calendarException = new ArrayList<>();

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
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})//, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayOfWeek ASC")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<DayOfWeekSettings> daysOfWeekSettings = new ArrayList<>();

    @Setter
    @OneToMany(mappedBy = "calendar",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("sort ASC")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<CalendarExceptionImpl> exceptions = new ArrayList<>();

    @OneToMany(mappedBy = "calendar",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("sort ASC")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<WorkingWeekImpl> workingWeeks = new ArrayList<>();

    @Transient
    private CalendarData calendarData;

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

            var exception = exceptionDays.get(startDay);
            if (exception == null) {
                exception = workingTimesOfDefaultWorkingWeek.get(startDay.getDayOfWeek());
            }

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

        var exception = exceptionDays.get(startDay);
        if (exception == null) {
            exception = workingTimesOfDefaultWorkingWeek.get(startDay.getDayOfWeek());
        }
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

//        DateComputation dateComputation = new DateComputation();
//        if (calendarData == null) initiateCacheData();
//
//        LocalDateTime startDate;
//        if (isAscend) {
//            startDate = dateComputation.increaseDateByDuration(date, duration);
//        } else {
//            startDate = dateComputation.decreaseDateByDuration(date, duration);
//        }

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



        // old

        List<ExceptionDay> exceptionDayList = this.getCalendarException();

        List<DefaultDaySetting> settingList;

        settingList = this.getSetting().getDefaultDaySettings();

        Map<LocalDate, Integer> mapExceptions =
                exceptionDayList.stream().collect(
                        Collectors.toMap(ExceptionDay::getDate, ExceptionDay::getDuration)
                );

        int secondFromBeggingOfDay = startTime.toSecondOfDay();
        if (setting == CalendarSettings.FULL_DAY) {
            secondFromBeggingOfDay = 0;
        }

        calendarData = new CalendarData(secondFromBeggingOfDay, settingList, mapExceptions);

    }

    @Override
    public void clearCache() {
        this.exceptionDays = null;
        this.workingTimesOfDefaultWorkingWeek = null;
    }

    @Override
    public LocalDateTime getClosestWorkingDay(LocalDateTime date) {
        initiateCacheData();
        return getClosestWorkingDayWithoutInitiateCache(date);
    }

    @Override
    public LocalDateTime getClosestWorkingDayWithoutInitiateCache(LocalDateTime date) {

        if (exceptionDays == null || workingTimesOfDefaultWorkingWeek == null) initiateCacheData();





        if (calendarData == null) initiateCacheData();
        LocalDate day = date.toLocalDate();
        LocalTime time = date.toLocalTime();
        ComputingDataOfWorkingDay computingData = new ComputingDataOfWorkingDay(day);
        if (time.toSecondOfDay() >= computingData.getFinishTimeSeconds()) computingData.increaseDay();
        int numberOfSeconds = computingData.getNumberOfSecondsInWorkingTime();

        // introduce the counter to prevent a limitless loop
        int counter = 0;
        int limit = 100000;
        while (numberOfSeconds == 0) {
            computingData.increaseDay();
            numberOfSeconds = computingData.getNumberOfSecondsInWorkingTime();
            if (counter++ > limit) break;
        }

        if (!day.equals(computingData.getDay())) time = startTime;

        return LocalDateTime.of(computingData.getDay(), time);

    }

    // return day + start time if the passed day is not working
    @Override
    public LocalDateTime getEndOfWorkingDay(LocalDate day) {

        ComputingDataOfWorkingDay computingDataOfWorkingDay = new ComputingDataOfWorkingDay(day);
        int numberOfSeconds = computingDataOfWorkingDay.getFinishTimeSeconds();
        return LocalDateTime.of(day, LocalTime.ofSecondOfDay(numberOfSeconds));

    }

    @Override
    public String getRepresentation() {
        return name;
    }

    // Classes

    private record CalendarData(int startTime, List<DefaultDaySetting> amountOfHourInDay, Map<LocalDate, Integer> exceptionDays) {}

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

            while (remainedDuration > 0) {

                var exception = exceptionDays.get(day);
                if (exception == null) {
                    exception = workingTimesOfDefaultWorkingWeek.get(day.getDayOfWeek());
                }
                if (exception.getDuration() != 0) {
                    if (time.equals(LocalTime.MIN) && exception.getDuration() < remainedDuration)
                        remainedDuration = remainedDuration - exception.getDuration();
                    else {
                        var returnDate = intervalCalculation.apply(exception.getIntervals());
                        if (returnDate) return LocalDateTime.of(day, time);
                    }
                }
                dayChanger.change();
                time = LocalTime.MIN;

            }

            return LocalDateTime.of(day, time);

        }

        private boolean calculateIntervalsAscendOrder(List<Interval> intervals) {

            for (Interval interval : intervals) {
                if (time.compareTo(interval.getFrom()) <= 0) {
                    if (interval.getDuration() < remainedDuration) {
                        remainedDuration = remainedDuration - interval.getDuration();
                        time = interval.getTo();
                        if (time.equals(LocalTime.MIN))
                            break;
                        continue;
                    } else {
                        time = interval.getFrom().plusSeconds(remainedDuration);
                        return true;
                    }
                } else if (time.compareTo(interval.getTo()) >= 0 && !interval.getTo().equals(LocalTime.MIN))
                    continue;

                var secondsOfDayTo = interval.getTo().toSecondOfDay();
                if (interval.getTo().equals(LocalTime.MIN)) secondsOfDayTo = fullDay;
                var intervalDuration = secondsOfDayTo - time.toSecondOfDay();
                if (intervalDuration >= remainedDuration) {
                    time = time.plusSeconds(remainedDuration);
                    return true;
                } else {
                    remainedDuration = remainedDuration - intervalDuration;
                    time = interval.getTo();
                    if (time.equals(LocalTime.MIN))
                        break;
                }
            }
            return false;
        }

        private void plusDay() {
            day = day.plusDays(1);
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
                if (time.equals(LocalTime.MIN) || time.compareTo(interval.getTo()) >= 0) {
                    if (interval.getDuration() < remainedDuration) {
                        remainedDuration = remainedDuration - interval.getDuration();
                        time = interval.getFrom();
                        if (time.equals(LocalTime.MIN))
                            break;
                        continue;
                    } else {
                        time = interval.getTo().minusSeconds(remainedDuration);
                        return true;
                    }
                } else if (time.compareTo(interval.getFrom()) <= 0)
                    continue;

                var secondsOfDayFrom = interval.getFrom().toSecondOfDay();
                var intervalDuration = secondsOfDayFrom - time.toSecondOfDay();
                intervalDuration = - intervalDuration;
                if (intervalDuration >= remainedDuration) {
                    time = time.minusSeconds(remainedDuration);
                    return true;
                } else {
                    remainedDuration = remainedDuration - intervalDuration;
                    time = interval.getFrom();
                    if (time.equals(LocalTime.MIN))
                        break;
                }
            }
            return false;
        }

        private void minusDay() {
            day = day.minusDays(1);
        }

        @FunctionalInterface
        interface DayChanger {
            void change();
        }

    }

    private class DateComputation {

        private LocalDateTime date;
        private long duration;

        // general variables
        private LocalDate day;
        private LocalTime time;
        private ComputingDataOfWorkingDay computingDataOfWorkingDay;
        private int startTime;
        private int finishTime;
        private long remainderOfDuration;
        private int secondOfDay;

        private LocalDateTime increaseDateByDuration(LocalDateTime date, long duration) {

            if (duration < 0) throw new StandardError("Illegal argument exception");
            this.duration = duration;
            this.date = date;

            initiate();

            if (secondOfDay >= finishTime) {
                computingDataOfWorkingDay.increaseDay();
                time = LocalTime.ofSecondOfDay(startTime);
            } else if (secondOfDay < startTime) {
                time = LocalTime.ofSecondOfDay(startTime);
            } else {
                if (secondOfDay + remainderOfDuration <= finishTime) {
                    time = time.plusSeconds(remainderOfDuration);
                    return LocalDateTime.of(day, time);
                } else {
                    computingDataOfWorkingDay.increaseDay();
                    time = LocalTime.ofSecondOfDay(startTime);
                    int numberOfSecondUntilEndOfWorkingDay = finishTime - secondOfDay;
                    remainderOfDuration = remainderOfDuration - numberOfSecondUntilEndOfWorkingDay;
                }
            }

            // increase days
            int numberOfSecondsInWorkingDay;
            do {
                numberOfSecondsInWorkingDay = computingDataOfWorkingDay.getNumberOfSecondsInWorkingTime();
                remainderOfDuration = remainderOfDuration - numberOfSecondsInWorkingDay;
                if (remainderOfDuration > 0L) {
                    computingDataOfWorkingDay.increaseDay();
                }

            } while (remainderOfDuration > 0L);

//            LocalDate newDay = computingDataOfWorkingDay.day;
//            if (remainderOfDuration == 0) return LocalDateTime.of(newDay, time);

            // reclaim remainderOfDuration to add it to the time
            remainderOfDuration = remainderOfDuration + numberOfSecondsInWorkingDay;

            int startTimeSeconds = startTime;
            int newStartTimeSeconds = startTimeSeconds + (int) remainderOfDuration;
            time = LocalTime.ofSecondOfDay(newStartTimeSeconds);

            LocalDate newDay = computingDataOfWorkingDay.day;
            return LocalDateTime.of(newDay, time);

        }

        private LocalDateTime decreaseDateByDuration(LocalDateTime date, long duration) {

            if (duration > 0) throw new StandardError("Illegal argument");
            this.duration = duration;
            this.date = date;

            initiate();

            if (secondOfDay <= startTime) {
                computingDataOfWorkingDay.decreaseDay();
                time = LocalTime.ofSecondOfDay(computingDataOfWorkingDay.getFinishTimeSeconds());
            } else if (secondOfDay >= finishTime) {
                time = LocalTime.ofSecondOfDay(computingDataOfWorkingDay.getFinishTimeSeconds());
            } else {
                if (secondOfDay + remainderOfDuration >= startTime) {
                    time = time.minusSeconds(-remainderOfDuration);
                    return LocalDateTime.of(day, time);
                } else {
                    computingDataOfWorkingDay.decreaseDay();
                    time = LocalTime.ofSecondOfDay(computingDataOfWorkingDay.getFinishTimeSeconds());
                    int numberOfSecondUntilEndOfWorkingDay = startTime - secondOfDay;
                    // numberOfSecondUntilEndOfWorkingDay < 0 by the condition above, so after
                    // the operation below is completed, remainderOfDuration will be increased
                    remainderOfDuration = remainderOfDuration - numberOfSecondUntilEndOfWorkingDay;
                }
            }

            // decrease days
            int numberOfSecondsInWorkingDay;
            do {
                numberOfSecondsInWorkingDay = computingDataOfWorkingDay.getNumberOfSecondsInWorkingTime();
                remainderOfDuration = remainderOfDuration + numberOfSecondsInWorkingDay;
                if (remainderOfDuration < 0L) {
                    computingDataOfWorkingDay.decreaseDay();
                }

            } while (remainderOfDuration < 0L);

//            LocalDate newDay = computingDataOfWorkingDay.day;
//            if (remainderOfDuration == 0) return LocalDateTime.of(newDay, time);

            // reclaim remainderOfDuration to add it to the time
            remainderOfDuration = remainderOfDuration - numberOfSecondsInWorkingDay;

            int newStartTimeSeconds = computingDataOfWorkingDay.getFinishTimeSeconds() + (int) remainderOfDuration;
            time = LocalTime.ofSecondOfDay(newStartTimeSeconds);

            LocalDate newDay = computingDataOfWorkingDay.day;
            return LocalDateTime.of(newDay, time);

        }

        private void initiate() {

            day = date.toLocalDate();
            time = date.toLocalTime();

            startTime = calendarData.startTime();
            computingDataOfWorkingDay =
                    new ComputingDataOfWorkingDay(day, startTime);

            finishTime = computingDataOfWorkingDay.getFinishTimeSeconds();

            remainderOfDuration = duration;
            secondOfDay = time.toSecondOfDay();

        }

    }

    private class ComputingDataOfWorkingDay {

        private int startTimeSeconds;
        private int index;
        private LocalDate day;
        private int numberOfSecondsInDay;

        ComputingDataOfWorkingDay(LocalDate day) {
            this(day, calendarData.startTime());
        }

        ComputingDataOfWorkingDay(LocalDate day, int startTimeSeconds) {

            this.day = day;
            this.startTimeSeconds = startTimeSeconds;
            int dayOfWeek = day.getDayOfWeek().getValue();

            List<DefaultDaySetting> durationOfDaysOfWeek = calendarData.amountOfHourInDay();
            int index = 0;
            for (int i = 0; i < durationOfDaysOfWeek.size(); i++) {
                if (durationOfDaysOfWeek.get(i).dayOfWeek() == dayOfWeek) {
                    index = i;
                    break;
                }
            }
            this.index = index;

            refreshNumberOfSecondsInDay();

        }

        public void increaseDay() {
            index++;
            if (index == calendarData.amountOfHourInDay().size()) index = 0;
            day = day.plusDays(1L);
            refreshNumberOfSecondsInDay();
        }

        public void decreaseDay() {
            if (index == 0) index = calendarData.amountOfHourInDay().size();
            index--;
            day = day.minusDays(1L);
            refreshNumberOfSecondsInDay();
        }

        public int getNumberOfSecondsInWorkingTime() {
            return numberOfSecondsInDay;
        }

        public int getFinishTimeSeconds() {
            return startTimeSeconds + numberOfSecondsInDay;
        }

        public LocalDate getDay() {
            return day;
        }

        private void refreshNumberOfSecondsInDay() {
            int durationDay = calendarData.amountOfHourInDay().get(index).countSeconds();
            Integer durationOfException = calendarData.exceptionDays().get(day);

            numberOfSecondsInDay = Objects.requireNonNullElse(durationOfException, durationDay);
        }

    }

}

