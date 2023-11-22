package com.pmvaadin.terms.calendars.entity;

import com.pmvaadin.terms.calendars.dayofweeksettings.DayOfWeekSettings;
import com.pmvaadin.terms.calendars.dayofweeksettings.DefaultDaySetting;
import com.pmvaadin.terms.calendars.exceptiondays.ExceptionDay;
import com.pmvaadin.terms.calendars.OperationListenerForCalendar;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.terms.calendars.exceptions.CalendarException;
import com.pmvaadin.terms.calendars.exceptions.CalendarExceptionImpl;
import com.pmvaadin.terms.calendars.workingweeks.WorkingWeek;
import com.pmvaadin.terms.calendars.workingweeks.WorkingWeekImpl;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@EntityListeners(OperationListenerForCalendar.class)
@Getter
@NoArgsConstructor
@Table(name = "calendars")
public class CalendarImpl implements Calendar, Serializable {

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
    @Transient
    private String settingString;

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

    public CalendarImpl(String name) {
        this.name = name;
    }

    public static String getHeaderName() {
        return "Name";
    }

    public static String getSettingName() {
        return "Setting";
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
    public WorkingWeek getWorkingWeekInstance() {
        return new WorkingWeekImpl().getInstance(this);
    }

    @Override
    public CalendarException getCalendarExceptionInstance() {
        return CalendarExceptionImpl.getInstance(this);
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

        LocalDate startDay = LocalDate.ofEpochDay(start.toLocalDate().toEpochDay());
        LocalTime startTime = LocalTime.ofSecondOfDay(start.toLocalTime().toSecondOfDay());

        if (calendarData == null) initiateCacheData();

        int startTimeCalendar = calendarData.startTime();
        ComputingDataOfWorkingDay computingDataOfWorkingDay =
                new ComputingDataOfWorkingDay(startDay, startTimeCalendar);

        int finishTimeCalendar = computingDataOfWorkingDay.getFinishTimeSeconds();

        ////////////////////////

        if (startDay.equals(finish.toLocalDate())) {

            int startTimeSeconds = startTime.toSecondOfDay();
            if (startTimeSeconds < startTimeCalendar) startTimeSeconds = startTimeCalendar;
            if (startTimeSeconds > finishTimeCalendar) startTimeSeconds = finishTimeCalendar;

            int finishDateTimeSeconds = finish.toLocalTime().toSecondOfDay();
            if (finishDateTimeSeconds < startTimeCalendar) finishDateTimeSeconds = startTimeCalendar;
            if (finishDateTimeSeconds > finishTimeCalendar) finishDateTimeSeconds = finishTimeCalendar;

            return finishDateTimeSeconds - startTimeSeconds;

        }

        /////////////////////////////

        int startTimeSeconds = startTime.toSecondOfDay();
        if (startTimeSeconds < startTimeCalendar) startTimeSeconds = startTimeCalendar;
        if (startTimeSeconds > finishTimeCalendar) {
            startTimeSeconds = finishTimeCalendar;
        }
        computingDataOfWorkingDay.increaseDay();
        long duration = finishTimeCalendar - startTimeSeconds;

        LocalDate finishDay = finish.toLocalDate();
        while (computingDataOfWorkingDay.getDay().compareTo(finishDay) < 0) {
            duration = duration + computingDataOfWorkingDay.getNumberOfSecondsInWorkingTime();
            computingDataOfWorkingDay.increaseDay();
        }

        // finishDate duration
        int finishDateTimeSeconds = finish.toLocalTime().toSecondOfDay();
        if (finishDateTimeSeconds < startTimeCalendar) finishDateTimeSeconds = startTimeCalendar;
        finishTimeCalendar = computingDataOfWorkingDay.getFinishTimeSeconds();
        if (finishDateTimeSeconds > finishTimeCalendar) finishDateTimeSeconds = finishTimeCalendar;
        int durationFinishDay = finishDateTimeSeconds - startTimeCalendar;

        duration = duration + durationFinishDay;

        return duration;

    }

    @Override
    public LocalDateTime getDateByDurationWithoutInitiateCache(LocalDateTime date, long duration) {

        if (duration == 0L) return LocalDateTime.of(
                LocalDate.ofEpochDay(date.toLocalDate().toEpochDay()),
                LocalTime.ofSecondOfDay(date.toLocalTime().toSecondOfDay())
        );

        boolean isAscend = duration > 0L;

        DateComputation dateComputation = new DateComputation();
        if (calendarData == null) initiateCacheData();

        LocalDateTime startDate;
        if (isAscend) {
            startDate = dateComputation.increaseDateByDuration(date, duration);
        } else {
            startDate = dateComputation.decreaseDateByDuration(date, duration);
        }

        return startDate;

    }

    @Override
    public void initiateCacheData() {

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
    public LocalDateTime getClosestWorkingDay(LocalDateTime date) {
        initiateCacheData();
        return getClosestWorkingDayWithoutInitiateCache(date);
    }

    @Override
    public LocalDateTime getClosestWorkingDayWithoutInitiateCache(LocalDateTime date) {

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

