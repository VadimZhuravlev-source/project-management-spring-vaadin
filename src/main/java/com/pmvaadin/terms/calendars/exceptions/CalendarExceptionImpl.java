package com.pmvaadin.terms.calendars.exceptions;

import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import com.pmvaadin.terms.calendars.common.Interval;
import com.pmvaadin.terms.calendars.entity.CalendarImpl;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
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
    private LocalDate finish;
    @Setter
    @Column(name = "finish_after")
    private int finishAfter = 1;

    @Setter
    private Integer sort = 0;

    @Setter
    @Column(name = "pattern_id")
    private RecurrencePattern pattern;

    // Daily pattern
    @Setter
    @Column(name = "number_of_days")
    private int numberOfDays;

    // Weekly pattern
    @Setter
    @Column(name = "number_of_weeks")
    private int numberOfWeeks;
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
    private MonthlyPattern monthlyPattern;
    @Setter
    @Column(name = "day_of_month")
    private byte dayOfMonth;
    @Setter
    @Column(name = "number_of_months")
    private int numberOfMonth;
    @Setter
    @Column(name = "number_of_weeks_the_id")
    private NumberOfWeek numberOfWeekThe;
    @Setter
    @Column(name = "day_of_week_the")
    private DayOfWeek dayOfWeekThe;
    @Setter
    @Column(name = "number_of_months_the")
    private int numberOfMonthThe;

    // Yearly pattern
    @Setter
    @Column(name = "yearly_pattern_id")
    private YearlyPattern yearlyPattern;
    @Setter
    @Column(name = "on_date")
    private LocalDate onDate;
    @Setter
    @Column(name = "number_of_week_year_id")
    private NumberOfWeek numberOfWeekYear;
    @Setter
    @Column(name = "day_of_week_year")
    private DayOfWeek dayOfWeekYear;
    @Setter
    @Column(name = "month_year")
    private Month monthYear;


    private final Interval intervalInstance = new CalendarExceptionInterval();

    @Setter
    @OneToMany(mappedBy = "exception")
    @OrderBy("sort ASC")
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

}
