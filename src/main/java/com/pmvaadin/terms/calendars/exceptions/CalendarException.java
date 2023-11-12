package com.pmvaadin.terms.calendars.exceptions;

import com.pmvaadin.terms.calendars.common.Interval;

import java.time.LocalDate;
import java.util.List;

public interface CalendarException {

    Integer getId();
    void setId(Integer id);

    Integer getVersion();

    String getName();
    void setName(String name);

    LocalDate getStart();
    void setStart(LocalDate start);

    LocalDate getFinish();
    void setFinish(LocalDate finish);

    Integer getSort();
    void setSort(Integer sort);

    List<Interval> getIntervals();

}
