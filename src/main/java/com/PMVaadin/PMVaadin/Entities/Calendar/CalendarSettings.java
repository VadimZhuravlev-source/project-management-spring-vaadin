package com.PMVaadin.PMVaadin.Entities.Calendar;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public enum CalendarSettings {
    EIGHTHOURWORKINGDAY,
    HOURSHIFT12,
    HOURSHIFT24,
    DAYSOFWEEKSETTINGS;

    private static final CalendarSettings[] ENUMS = values();

    CalendarSettings(){
    }

    public static CalendarSettings of(int calendarSettings) {
        if (calendarSettings >= 1 && calendarSettings <= 4) {
            return ENUMS[calendarSettings - 1];
        } else {
            throw new NoSuchElementException("Invalid value for Day of the week: " + calendarSettings);
        }
    }

    @Override
    public String toString() {

        if (this == EIGHTHOURWORKINGDAY) {
            return "Eight hour working day";
        }else if (this == HOURSHIFT12) {
            return "Twelve hour shift";
        }else if (this == HOURSHIFT24) {
            return "Twenty four hour shift";
        }else if (this == DAYSOFWEEKSETTINGS) {
            return "Days of the week settings";
        }
        return "";

    }

    public List<DayOfWeekSettings> getDaysOfWeekSettings() {

        List<DayOfWeekSettings> daysOfWeekSettings = new ArrayList<>();

        if (this == EIGHTHOURWORKINGDAY) {
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.MONDAY.getValue(), new  BigDecimal("8.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.TUESDAY.getValue(), new BigDecimal("8.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.WEDNESDAY.getValue(), new BigDecimal("8.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.THURSDAY.getValue(), new BigDecimal("8.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.FRIDAY.getValue(), new BigDecimal("8.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.SATURDAY.getValue(), new BigDecimal("0.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.SUNDAY.getValue(), new BigDecimal("0.00")));
        }else if (this == HOURSHIFT12) {
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.MONDAY.getValue(), new BigDecimal("12.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.TUESDAY.getValue(), new BigDecimal("12.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.WEDNESDAY.getValue(), new BigDecimal("12.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.THURSDAY.getValue(), new BigDecimal("12.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.FRIDAY.getValue(), new BigDecimal("12.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.SATURDAY.getValue(), new BigDecimal("12.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.SUNDAY.getValue(), new BigDecimal("12.00")));
        }else if (this == HOURSHIFT24) {
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.MONDAY.getValue(), new BigDecimal("24.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.TUESDAY.getValue(), new BigDecimal("24.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.WEDNESDAY.getValue(), new BigDecimal("24.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.THURSDAY.getValue(), new BigDecimal("24.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.FRIDAY.getValue(), new BigDecimal("24.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.SATURDAY.getValue(), new BigDecimal("24.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.SUNDAY.getValue(), new BigDecimal("24.00")));
        }else {
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.MONDAY.getValue(), new BigDecimal("0.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.TUESDAY.getValue(), new BigDecimal("0.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.WEDNESDAY.getValue(), new BigDecimal("0.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.THURSDAY.getValue(), new BigDecimal("0.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.SATURDAY.getValue(), new BigDecimal("0.00")));
            daysOfWeekSettings.add(new DayOfWeekSettings(DayOfWeek.SUNDAY.getValue(), new BigDecimal("0.00")));
        }

        return daysOfWeekSettings;

    }

}
