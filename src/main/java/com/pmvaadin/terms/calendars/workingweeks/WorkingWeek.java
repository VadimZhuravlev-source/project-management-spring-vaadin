package com.pmvaadin.terms.calendars.workingweeks;

import java.time.LocalDate;
import java.util.List;

public interface WorkingWeek {

    Integer getId();
    void setId(Integer id);

    String getName();
    void setName(String name);

    LocalDate getStart();
    void setStart(LocalDate start);

    LocalDate getFinish();
    void setFinish(LocalDate finish);

    Integer getSort();
    void setSort(Integer sort);

    List<WorkingDaySetting> getWorkingDaySettings();

}
