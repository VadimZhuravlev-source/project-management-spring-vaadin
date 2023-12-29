package com.pmvaadin.terms.calendars.entity;

import java.util.*;

public enum CalendarSettings {
    // after new value has been added, there is a need to add an initialization of DayOfWeekSettings set in the class Sets below
    STANDARD(0),
    NIGHT_SHIFT(1),
    FULL_DAY(2);

    private final Integer code;

    private static final CalendarSettings[] ENUMS = values();

    CalendarSettings(int code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
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

        if (this == STANDARD) {
            return "Standard";
        }else if (this == NIGHT_SHIFT) {
            return "Night Shift";
        }else if (this == FULL_DAY) {
            return "24 Hours";
        }
        return "";

    }

}
