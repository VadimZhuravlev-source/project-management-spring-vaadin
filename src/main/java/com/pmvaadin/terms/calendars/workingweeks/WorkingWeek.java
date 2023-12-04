package com.pmvaadin.terms.calendars.workingweeks;

import com.pmvaadin.terms.calendars.common.ExceptionLength;
import com.pmvaadin.terms.calendars.entity.Calendar;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface WorkingWeek {

    static String getHeaderName() {
        return "Details for ";
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
    void fillDefaultWorkingTimes();

    WorkingWeek getInstance(Calendar calendar);

    boolean isDefault();
    void setDefault(boolean isDefault);

    WorkingTime getWorkingTimeInstance();

    Map<LocalDate, ExceptionLength> getExceptionAsDayConstraint();

}
