package com.pmvaadin.terms.calendars.exceptions;

import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import com.pmvaadin.terms.calendars.common.Interval;
import com.pmvaadin.terms.calendars.entity.CalendarImpl;
import com.pmvaadin.terms.calendars.entity.CalendarSettings;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Entity
@Table(name = "calendar_exceptions")
public class CalendarExceptionImpl implements HasIdentifyingFields, CalendarException {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Integer version;

    @Setter
    @ManyToOne
    @JoinColumn(name = "calendar_id", nullable = false)
    private CalendarImpl calendar;

    @Setter
    private String name = "";

    @Setter
    @Column(name = "setting_id")
    private CalendarExceptionSetting setting = CalendarExceptionSetting.NONWORKING;

    @Setter
    private LocalDate start;

    @Setter
    @Column(name = "end_by_after_id")
    private RecurrenceEnd endByAfter = RecurrenceEnd.AFTER;
    @Setter
    private LocalDate finish;
    @Setter
    @Column(name = "number_of_occurrence")
    private int numberOfOccurrence = 1;

    @Setter
    private Integer sort = 0;

    @Setter
    @Column(name = "pattern_id")
    private RecurrencePattern pattern = RecurrencePattern.DAILY;

    // Daily pattern
    @Setter
    @Column(name = "every_number_of_days")
    private int everyNumberOfDays = 1;

    // Weekly pattern
    @Setter
    @Column(name = "every_number_of_weeks")
    private int everyNumberOfWeeks = 1;
    @Setter
    @Column(name = "end_of_week")
    private DayOfWeek endOfWeek = DayOfWeek.SUNDAY;
    @Setter
    @Column(name = "every_monday")
    private boolean everyMonday;
    @Setter
    @Column(name = "every_tuesday")
    private boolean everyTuesday;
    @Setter
    @Column(name = "every_wednesday")
    private boolean everyWednesday;
    @Setter
    @Column(name = "every_thursday")
    private boolean everyThursday;
    @Setter
    @Column(name = "every_friday")
    private boolean everyFriday;
    @Setter
    @Column(name = "every_saturday")
    private boolean everySaturday;
    @Setter
    @Column(name = "every_sunday")
    private boolean everySunday;

    // Monthly pattern
    @Setter
    @Column(name = "monthly_pattern_id")
    private MonthlyPattern monthlyPattern = MonthlyPattern.DAY;
    @Setter
    @Column(name = "day_of_month")
    private byte dayOfMonth = 1;
    @Setter
    @Column(name = "every_number_of_months")
    private int everyNumberOfMonths = 1;
    @Setter
    @Column(name = "number_of_weeks_the_id")
    private NumberOfWeek numberOfWeekThe;
    @Setter
    @Column(name = "day_of_week_the")
    private DayOfWeek dayOfWeekThe;
    @Setter
    @Column(name = "every_number_of_months_the")
    private int everyNumberOfMonthsThe = 1;

    // Yearly pattern
    @Setter
    @Column(name = "yearly_pattern_id")
    private YearlyPattern yearlyPattern = YearlyPattern.ON;
    @Setter
    @Column(name = "on_date_day")
    private byte onDateDay;
    @Setter
    @Column(name = "on_date_month")
    private Month onDateMonth;
    @Setter
    @Column(name = "number_of_week_year_id")
    private NumberOfWeek numberOfWeekYear;
    @Setter
    @Column(name = "day_of_week_year")
    private DayOfWeek dayOfWeekYear;
    @Setter
    @Column(name = "month_year")
    private Month monthYear;

    @OneToMany(mappedBy = "exception", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("from ASC")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<CalendarExceptionInterval> intervals = new ArrayList<>();

    @Override
    public void nullIdentifyingFields() {

        this.id = null;
        this.version = null;

        if (intervals == null) return;

        intervals.forEach(CalendarExceptionInterval::nullIdentifyingFields);

    }

    @Override
    public List<Interval> getIntervals() {
        return intervals.stream().map(i -> (Interval) i).collect(Collectors.toList());
    }

    @Override
    public List<Interval> getCopyOfIntervals() {
        return intervals.stream().map(CalendarExceptionInterval::new).collect(Collectors.toList());
    }

    @Override
    public Interval getIntervalInstance() {
        var newInterval = new CalendarExceptionInterval();
        newInterval.setException(this);
        return newInterval;
    }

    @Override
    public List<Interval> getDefaultIntervals() {
        if (this.calendar == null || calendar.getSetting() == null)
            return new ArrayList<>();

        var settings = calendar.getSetting();
        ArrayList<Interval> list = new ArrayList<>();
        if (settings == CalendarSettings.STANDARD) {
            list.add(new CalendarExceptionInterval(this, LocalTime.of(8, 0), LocalTime.of(12, 0)));
            list.add(new CalendarExceptionInterval(this, LocalTime.of(13, 0), LocalTime.of(17, 0)));
        } else if (settings == CalendarSettings.NIGHT_SHIFT) {
            list.add(new CalendarExceptionInterval(this, LocalTime.of(0, 0), LocalTime.of(3, 0)));
            list.add(new CalendarExceptionInterval(this, LocalTime.of(4, 0), LocalTime.of(8, 0)));
            list.add(new CalendarExceptionInterval(this, LocalTime.of(23, 0), LocalTime.of(0, 0)));
        } else if (settings == CalendarSettings.FULL_DAY) {
            list.add(new CalendarExceptionInterval(this, LocalTime.of(0, 0), LocalTime.of(0, 0)));
        }

        return list;

    }

    @Override
    public void setIntervals(List<Interval> intervals) {
        this.intervals = intervals.stream().map(i -> (CalendarExceptionInterval) i).collect(Collectors.toList());
    }

    public static CalendarException getInstance(CalendarImpl calendar) {
        var exception = new CalendarExceptionImpl();
        exception.calendar = calendar;
        exception.start = LocalDate.now();
        exception.finish = LocalDate.now();
        return exception;
    }

    @Override
    public Map<LocalDate, ExceptionLength> getExceptionAsDayConstraint() {

        if (this.start == null
                || this.numberOfOccurrence == 0
                || this.setting == null)
            return new HashMap<>(0);

        Map<LocalDate, ExceptionLength> map;
        if (this.pattern == RecurrencePattern.DAILY)
            map = getExceptionAsDayConstraintDaily();
        else if (this.pattern == RecurrencePattern.WEEKLY)
            map = getExceptionAsDayConstraintWeekly();
        else if (this.pattern == RecurrencePattern.MONTHLY)
            map = getExceptionAsDayConstraintMonthly();
        else if (this.pattern == RecurrencePattern.YEARLY)
            map = getExceptionAsDayConstraintYearly();
        else
            map = new HashMap<>(0);

        return map;

    }

    private ExceptionLength getExceptionLength() {
        var duration = 0;
        var durationDay = 24 * 3600;
        if (this.setting == CalendarExceptionSetting.WORKING_TIMES) {
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
        }

        if (duration >= durationDay) duration = durationDay;

        return new ExceptionLengthImpl(duration, this.getIntervals());

    }

    private Map<LocalDate, ExceptionLength> getExceptionAsDayConstraintDaily() {

        var exceptionLength = getExceptionLength();
        var map = new HashMap<LocalDate, ExceptionLength>(numberOfOccurrence);
        var startPoint = this.start;

        for(int iterator = 1; iterator <= numberOfOccurrence; iterator++) {
            map.put(startPoint, exceptionLength);
            startPoint = startPoint.plusDays(everyNumberOfDays);
        }

        return map;

    }

    private Map<LocalDate, ExceptionLength> getExceptionAsDayConstraintWeekly() {

        if (!everyMonday && !everyTuesday && !everyWednesday && !everyThursday
                && !everyFriday && !everySaturday && !everySunday)
            return new HashMap<>();

        var exceptionLength = getExceptionLength();
        var map = new HashMap<LocalDate, ExceptionLength>(numberOfOccurrence);
        var startPoint = this.start;

        var currentNumberOfOccurrence = 1;
        while (currentNumberOfOccurrence <= numberOfOccurrence) {
            var dayOfWeek = startPoint.getDayOfWeek();
            if (everyMonday && dayOfWeek == DayOfWeek.MONDAY
                || everyTuesday && dayOfWeek == DayOfWeek.TUESDAY
                || everyWednesday && dayOfWeek == DayOfWeek.WEDNESDAY
                || everyThursday && dayOfWeek == DayOfWeek.THURSDAY
                || everyFriday && dayOfWeek == DayOfWeek.FRIDAY
                || everySaturday && dayOfWeek == DayOfWeek.SATURDAY
                || everySunday && dayOfWeek == DayOfWeek.SUNDAY) {
                map.put(startPoint, exceptionLength);
                currentNumberOfOccurrence++;
            }

            if (dayOfWeek == endOfWeek) {
                startPoint = startPoint.plusWeeks(everyNumberOfWeeks);
            }
            startPoint = startPoint.plusDays(1);
        }

        return map;

    }

    private Map<LocalDate, ExceptionLength> getExceptionAsDayConstraintMonthly() {

    }

    private Map<LocalDate, ExceptionLength> getExceptionAsDayConstraintYearly() {

    }

    private record ExceptionLengthImpl(int duration, List<Interval> intervals)
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

}
