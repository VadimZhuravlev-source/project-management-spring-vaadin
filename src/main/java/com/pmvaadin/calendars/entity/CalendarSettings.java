package com.pmvaadin.calendars.entity;

import com.pmvaadin.calendars.dayofweeksettings.DayOfWeekSettings;
import com.pmvaadin.calendars.dayofweeksettings.DefaultDaySetting;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.*;

public enum CalendarSettings {
    // after new value has been added, there is a need to add an initialization of DayOfWeekSettings set in the class Sets below
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

        return getDefaultDaySettings().stream().map(DayOfWeekSettings::new).toList();

    }

    public List<DefaultDaySetting> getDefaultDaySettings() {
        return Sets.map.get(this);
    }

    private static class Sets {

        public static Map<CalendarSettings, List<DefaultDaySetting>> map = initiateMap();

        private static Map<CalendarSettings, List<DefaultDaySetting>> initiateMap() {

            Map<CalendarSettings, List<DefaultDaySetting>> map = new HashMap<>(CalendarSettings.values().length);

            map.put(CalendarSettings.EIGHTHOURWORKINGDAY, initiateSettingsEightHour());
            map.put(CalendarSettings.HOURSHIFT12, initiateShiftSettings("12.00"));
            map.put(CalendarSettings.HOURSHIFT24, initiateShiftSettings("24.00"));
            map.put(CalendarSettings.DAYSOFWEEKSETTINGS, initiateSettingsEightHour());

            return map;

        }

        private static List<DefaultDaySetting> initiateShiftSettings(String number) {

            List<DefaultDaySetting> list = new ArrayList<>(DayOfWeek.values().length);
            for (DayOfWeek dayOfWeek: DayOfWeek.values()) {
                list.add(new DefaultDaySetting(dayOfWeek.getValue(), new BigDecimal(number)));
            }
            return Collections.unmodifiableList(list);

        }

        private static List<DefaultDaySetting> initiateSettingsEightHour() {

            List<DefaultDaySetting> list = new ArrayList<>(DayOfWeek.values().length);
            list.add(new DefaultDaySetting(DayOfWeek.MONDAY.getValue(), new  BigDecimal("8.00")));
            list.add(new DefaultDaySetting(DayOfWeek.TUESDAY.getValue(), new BigDecimal("8.00")));
            list.add(new DefaultDaySetting(DayOfWeek.WEDNESDAY.getValue(), new BigDecimal("8.00")));
            list.add(new DefaultDaySetting(DayOfWeek.THURSDAY.getValue(), new BigDecimal("8.00")));
            list.add(new DefaultDaySetting(DayOfWeek.FRIDAY.getValue(), new BigDecimal("8.00")));
            list.add(new DefaultDaySetting(DayOfWeek.SATURDAY.getValue(), new BigDecimal("0.00")));
            list.add(new DefaultDaySetting(DayOfWeek.SUNDAY.getValue(), new BigDecimal("0.00")));

            return Collections.unmodifiableList(list);

        }

    }

}
