package com.pmvaadin.terms.calendars.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.pmvaadin.terms.calendars.dayofweeksettings.DayOfWeekSettings;
import com.pmvaadin.terms.calendars.exceptiondays.ExceptionDay;

public interface Calendar {

    static long DAY_DURATION_SECONDS = 8 * 3600;

    static int NUMBER_OF_SECONDS_IN_AN_HOUR = 3600;

    static String getHeaderName() {
        return "Name";
    }

    static Double getCountOfHoursDouble(Integer seconds) {

        var countOfHours = ((double) seconds) / NUMBER_OF_SECONDS_IN_AN_HOUR;
        return new BigDecimal(countOfHours).setScale(2, RoundingMode.CEILING).doubleValue();

    }

    static Integer getCountOfHoursInteger(Double aDouble) {

        var scaledValue = new BigDecimal(aDouble).setScale(2, RoundingMode.CEILING).doubleValue();
        return (int) (scaledValue * Calendar.NUMBER_OF_SECONDS_IN_AN_HOUR);

    }

    Integer getId();

    void setId(Integer id);

    Integer getVersion();
    void setVersion(Integer version);

    String getName();

    void setName(String name);

    CalendarSettings getSetting();

    void setSetting(CalendarSettings calendarSettings);

    List<DayOfWeekSettings> getDaysOfWeekSettings();

    void setDaysOfWeekSettings(List<DayOfWeekSettings> daysOfWeekSettings);

    List<ExceptionDay> getCalendarException();

    void setCalendarException(List<ExceptionDay> exceptionDayList);

    LocalTime getStartTime();
    void setStartTime(LocalTime time);

    Calendar getDefaultCalendar();

    long getDuration(LocalDateTime start, LocalDateTime finish);

    LocalDateTime getDateByDuration(LocalDateTime date, long duration);

    long getDurationWithoutInitiateCache(LocalDateTime start, LocalDateTime finish);

    LocalDateTime getDateByDurationWithoutInitiateCache(LocalDateTime date, long duration);

    void initiateCacheData();

    LocalDateTime getClosestWorkingDay(LocalDateTime date);

    LocalDateTime getClosestWorkingDayWithoutInitiateCache(LocalDateTime date);

    LocalDateTime getEndOfWorkingDay(LocalDate day);

    String getRepresentation();

    boolean isPredefined();
    void setPredefined(boolean isPredefined);

    boolean isNew();

}
