package com.pmvaadin.terms.calendars.common;

import java.time.LocalTime;

public interface Interval {

    public Integer getId();
    public void setId(Integer id);

    public Integer getVersion();

    public LocalTime getFrom();
    public void setFrom(LocalTime from);

    public LocalTime getTo();
    public void setTo(LocalTime to);

    int getSort();
    void setSort(int sort);

}
