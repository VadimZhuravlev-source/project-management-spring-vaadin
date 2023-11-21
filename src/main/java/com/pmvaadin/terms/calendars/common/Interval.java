package com.pmvaadin.terms.calendars.common;

import java.time.LocalTime;

public interface Interval {

    Integer getId();
    void setId(Integer id);

    Integer getVersion();

    LocalTime getFrom();
    void setFrom(LocalTime from);

    LocalTime getTo();
    void setTo(LocalTime to);

    Interval getInstance();

}
