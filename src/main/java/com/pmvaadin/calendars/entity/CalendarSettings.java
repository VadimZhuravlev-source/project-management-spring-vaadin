package com.pmvaadin.calendars.entity;

import com.pmvaadin.calendars.dayofweeksettings.DayOfWeekSettings;
import com.pmvaadin.calendars.dayofweeksettings.DefaultDaySetting;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.*;

public enum CalendarSettings {
    EIGHTHOURWORKINGDAY(initiateSettingsEightHour()),
    HOURSHIFT12(initiateShiftSettings("12.00")),
    HOURSHIFT24(initiateShiftSettings("24.00")),
    DAYSOFWEEKSETTINGS(initiateShiftSettings("0.00"));

    private final Set<DefaultDaySetting> defaultSettings;

    private static final CalendarSettings[] ENUMS = values();

    CalendarSettings(Set<DefaultDaySetting> defaultSettings){

        this.defaultSettings = defaultSettings;

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

    private static Set<DefaultDaySetting> initiateShiftSettings(String number) {

        int quantityOfDayInWeek = 7;
        Set<DefaultDaySetting> set = new HashSet<>(quantityOfDayInWeek);
        for (DayOfWeek dayOfWeek: DayOfWeek.values()) {
            set.add(new DefaultDaySetting(dayOfWeek.getValue(), new BigDecimal(number)));
        }
        return Collections.unmodifiableSet(set);

    }

    private static Set<DefaultDaySetting> initiateSettingsEightHour() {

        int quantityOfDayInWeek = 7;
        Set<DefaultDaySetting> setEightHour = new HashSet<>(quantityOfDayInWeek);
        setEightHour.add(new DefaultDaySetting(DayOfWeek.MONDAY.getValue(), new  BigDecimal("8.00")));
        setEightHour.add(new DefaultDaySetting(DayOfWeek.TUESDAY.getValue(), new BigDecimal("8.00")));
        setEightHour.add(new DefaultDaySetting(DayOfWeek.WEDNESDAY.getValue(), new BigDecimal("8.00")));
        setEightHour.add(new DefaultDaySetting(DayOfWeek.THURSDAY.getValue(), new BigDecimal("8.00")));
        setEightHour.add(new DefaultDaySetting(DayOfWeek.FRIDAY.getValue(), new BigDecimal("8.00")));
        setEightHour.add(new DefaultDaySetting(DayOfWeek.SATURDAY.getValue(), new BigDecimal("0.00")));
        setEightHour.add(new DefaultDaySetting(DayOfWeek.SUNDAY.getValue(), new BigDecimal("0.00")));

        return Collections.unmodifiableSet(setEightHour);

    }

    public Set<DefaultDaySetting> getDefaultSettings() {
        return defaultSettings;
    }

    public List<DayOfWeekSettings> getDaysOfWeekSettings() {

        return defaultSettings.stream().map(DayOfWeekSettings::new).toList();

    }

}
