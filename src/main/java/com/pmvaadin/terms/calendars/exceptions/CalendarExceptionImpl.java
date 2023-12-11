package com.pmvaadin.terms.calendars.exceptions;

import com.pmvaadin.terms.calendars.common.ExceptionLength;
import com.pmvaadin.terms.calendars.common.ExceptionLengthImpl;
import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import com.pmvaadin.terms.calendars.common.Interval;
import com.pmvaadin.terms.calendars.entity.CalendarImpl;
import com.pmvaadin.terms.calendars.entity.CalendarSettings;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
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
    public String toString() {
        return "Exception " + this.name + " with start: " + this.start + " and finish: " + this.finish;
    }

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
        exception.endOfWeek = calendar.getEndOfWeek();
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

        ExceptionLength exceptionLength;
        if (this.setting == CalendarExceptionSetting.WORKING_TIMES) {
            exceptionLength = Interval.getExceptionLength(this.getIntervals());
        } else
            exceptionLength = new ExceptionLengthImpl(0, new ArrayList<>(0));

        return exceptionLength;

    }

    private Map<LocalDate, ExceptionLength> getExceptionAsDayConstraintDaily() {

        var exceptionLength = getExceptionLength();
        var map = new HashMap<LocalDate, ExceptionLength>(this.numberOfOccurrence);
        var startPoint = this.start;

        for (int iterator = 1; iterator <= this.numberOfOccurrence; iterator++) {
            map.put(startPoint, exceptionLength);
            startPoint = startPoint.plusDays(this.everyNumberOfDays);
        }

        return map;

    }

    private Map<LocalDate, ExceptionLength> getExceptionAsDayConstraintWeekly() {

        if (!this.everyMonday && !this.everyTuesday && !this.everyWednesday && !this.everyThursday
                && !this.everyFriday && !this.everySaturday && !this.everySunday)
            return new HashMap<>();

        var exceptionLength = getExceptionLength();
        var map = new HashMap<LocalDate, ExceptionLength>(this.numberOfOccurrence);
        var startPoint = this.start;

        var currentNumberOfOccurrence = 1;
        while (currentNumberOfOccurrence <= this.numberOfOccurrence) {
            var dayOfWeek = startPoint.getDayOfWeek();
            if (this.everyMonday && dayOfWeek == DayOfWeek.MONDAY
                || this.everyTuesday && dayOfWeek == DayOfWeek.TUESDAY
                || this.everyWednesday && dayOfWeek == DayOfWeek.WEDNESDAY
                || this.everyThursday && dayOfWeek == DayOfWeek.THURSDAY
                || this.everyFriday && dayOfWeek == DayOfWeek.FRIDAY
                || this.everySaturday && dayOfWeek == DayOfWeek.SATURDAY
                || this.everySunday && dayOfWeek == DayOfWeek.SUNDAY) {
                map.put(startPoint, exceptionLength);
                currentNumberOfOccurrence++;
            }

            if (dayOfWeek == this.endOfWeek) {
                startPoint = startPoint.plusWeeks(this.everyNumberOfWeeks - 1);
            }
            startPoint = startPoint.plusDays(1);
        }

        return map;

    }

    private Map<LocalDate, ExceptionLength> getExceptionAsDayConstraintMonthly() {

        var exceptionLength = getExceptionLength();
        var calculation = new CalculationMonthlyExceptionDays(exceptionLength);
        return calculation.calculate();

    }

    private Map<LocalDate, ExceptionLength> getExceptionAsDayConstraintYearly() {

        var exceptionLength = getExceptionLength();
        var calculation = new CalculationYearlyExceptionDays(exceptionLength);
        return calculation.calculate();

    }

    private class CalculationMonthlyExceptionDays {

        private int currentNumberOfOccurrence = 1;
        private LocalDate startPoint;
        private final Map<LocalDate, ExceptionLength> map = new HashMap<>(numberOfOccurrence);
        private final ExceptionLength exceptionLength;

        CalculationMonthlyExceptionDays(ExceptionLength exceptionLength) {
            this.exceptionLength = exceptionLength;
        }

        public Map<LocalDate, ExceptionLength> calculate() {

            Supplier<Byte> consumer;
            Supplier<Byte> monthAugmenter;
            if (monthlyPattern == MonthlyPattern.DAY) {
                startPoint = LocalDate.of(start.getYear(), start.getMonth(), dayOfMonth);
                consumer = this::increase;
                monthAugmenter = this::increaseMonth;

            } else if (monthlyPattern == MonthlyPattern.THE) {
                startPoint = CalendarException.getDayOfMonth(start, dayOfWeekThe, numberOfWeekThe);
                consumer = this::increaseThe;
                monthAugmenter = this::increaseMonthThe;
            }
            else return map;

            if (startPoint.compareTo(start) < 0) {
                monthAugmenter.get();
            }
            while (currentNumberOfOccurrence <= numberOfOccurrence) {
                consumer.get();
            }

            return map;

        }

        private void addToMap() {
            currentNumberOfOccurrence++;
            map.put(startPoint, exceptionLength);
        }

        private byte increase() {
            addToMap();
            increaseMonth();
            return 0;
        }

        private byte increaseThe() {
            addToMap();
            increaseMonthThe();
            return 0;
        }

        private byte increaseMonth() {
            startPoint = startPoint.plusMonths(everyNumberOfMonths);
            return 0;
        }

        private byte increaseMonthThe() {
            startPoint = startPoint.plusMonths(everyNumberOfMonthsThe);
            startPoint = CalendarException.getDayOfMonth(startPoint, dayOfWeekThe, numberOfWeekThe);
            return 0;
        }

    }

    private class CalculationYearlyExceptionDays {

        private int currentNumberOfOccurrence = 1;
        private LocalDate startPoint = start;
        private final Map<LocalDate, ExceptionLength> map = new HashMap<>(numberOfOccurrence);
        private final ExceptionLength exceptionLength;
        private LocalDate iteratedDate;
        private final int daysOfFebruaryOfLeapYear = 29;

        CalculationYearlyExceptionDays(ExceptionLength exceptionLength) {
            this.exceptionLength = exceptionLength;
        }

        public Map<LocalDate, ExceptionLength> calculate() {

            Supplier<Byte> consumer;
            Supplier<Byte> yearAugmenter;

            if (yearlyPattern == YearlyPattern.ON) {

                if (!startPoint.isLeapYear() && onDateDay == daysOfFebruaryOfLeapYear && onDateMonth == Month.FEBRUARY) {
                    while (!startPoint.isLeapYear())
                        startPoint = startPoint.plusYears(1);
                }
                startPoint = LocalDate.of(startPoint.getYear(), onDateMonth, onDateDay);
                consumer = this::increase;
                yearAugmenter = this::increaseYear;

            } else if (yearlyPattern == YearlyPattern.THE) {
                iteratedDate = LocalDate.of(start.getYear(), monthYear, 1);
                startPoint = CalendarException.getDayOfMonth(iteratedDate, dayOfWeekYear, numberOfWeekYear);
                consumer = this::increaseThe;
                yearAugmenter = this::increaseYearThe;
            }
            else return map;

            if (startPoint.compareTo(start) < 0) {
                yearAugmenter.get();
            }
            while (currentNumberOfOccurrence <= numberOfOccurrence) {
                consumer.get();
            }

            return map;

        }

        private void addToMap() {
            currentNumberOfOccurrence++;
            map.put(startPoint, exceptionLength);
        }

        private byte increase() {
            if (!(!startPoint.isLeapYear() && onDateDay == daysOfFebruaryOfLeapYear && onDateMonth == Month.FEBRUARY)) {
                addToMap();
            }
            increaseYear();
            return 0;
        }

        private byte increaseThe() {
            addToMap();
            increaseYearThe();
            return 0;
        }

        private byte increaseYear() {
            startPoint = startPoint.plusYears(1);
            if (startPoint.isLeapYear() && onDateDay == daysOfFebruaryOfLeapYear && onDateMonth == Month.FEBRUARY)
                startPoint = LocalDate.of(startPoint.getYear(), onDateMonth, onDateDay);
            return 0;
        }

        private byte increaseYearThe() {
            iteratedDate = iteratedDate.plusYears(1);
            startPoint = CalendarException.getDayOfMonth(iteratedDate, dayOfWeekYear, numberOfWeekYear);
            return 0;
        }

    }

}
