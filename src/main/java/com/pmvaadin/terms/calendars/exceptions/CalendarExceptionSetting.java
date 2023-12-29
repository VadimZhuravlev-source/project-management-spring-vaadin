package com.pmvaadin.terms.calendars.exceptions;

import java.util.NoSuchElementException;

public enum CalendarExceptionSetting {

    NONWORKING(0),
    WORKING_TIMES(1);

    private final Integer code;

    private static final CalendarExceptionSetting[] ENUMS = values();

    CalendarExceptionSetting(int code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static CalendarExceptionSetting of(int settings) {
        if (settings >= 0 && settings <= 1) {
            return ENUMS[settings];
        } else {
            throw new NoSuchElementException("Invalid value for calendar exception settings: " + settings);
        }
    }

    @Override
    public String toString() {

        if (this == NONWORKING) {
            return "Nonworking";
        }else if (this == WORKING_TIMES) {
            return "Working times";
        }

        return "";

    }

}
