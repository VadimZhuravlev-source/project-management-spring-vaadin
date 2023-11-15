package com.pmvaadin.terms.calendars.workingweeks;

import com.pmvaadin.terms.calendars.entity.Calendar;

import java.time.LocalDate;
import java.util.List;

public interface WorkingWeek {

    static String getHeaderName() {
        return "Details";
    }

    Integer getId();
    void setId(Integer id);

    Integer getVersion();

    Calendar getCalendar();

    String getName();
    void setName(String name);

    LocalDate getStart();
    void setStart(LocalDate start);

    LocalDate getFinish();
    void setFinish(LocalDate finish);

    int getSort();
    void setSort(int sort);

    List<WorkingTime> getWorkingTimes();

    WorkingWeek getInstance();

    boolean isDefault();
    boolean setDefault(boolean isDefault);

    WorkingTime getWorkingTimeInstance();

}
