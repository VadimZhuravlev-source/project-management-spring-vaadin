package com.pmvaadin.terms.calendars.exceptions;

import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import com.pmvaadin.terms.calendars.common.Interval;
import com.pmvaadin.terms.calendars.entity.CalendarImpl;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Entity
@Table(name = "calendar_exceptions")
public class CalendarExceptionImpl implements CalendarException, HasIdentifyingFields {

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
    private CalendarExceptionSetting setting = CalendarExceptionSetting.NONWORKING;

    @Setter
    private LocalDate start = LocalDate.now();
    @Setter
    private LocalDate finish = LocalDate.now();

    @Setter
    private Integer sort = 0;

    @Setter
    @OneToMany(mappedBy = "exception", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
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
