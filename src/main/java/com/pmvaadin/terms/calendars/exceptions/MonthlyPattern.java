package com.pmvaadin.terms.calendars.exceptions;

import java.util.NoSuchElementException;

public enum MonthlyPattern {

    DAY((short) 0),
    THE((short) 1);

    private final Short code;

    private static final MonthlyPattern[] ENUMS = values();

    MonthlyPattern(Short code) {
        this.code = code;
    }

    public Short getCode() {
        return code;
    }

    public static MonthlyPattern of(Short code) {
        if (code >= 0 && code <= 1) {
            return ENUMS[code];
        } else {
            throw new NoSuchElementException("Invalid value for monthly pattern code: " + code);
        }
    }

    @Override
    public String toString() {

        if (this == DAY) {
            return "Day";
        }else if (this == THE) {
            return "The";
        }

        return "";

    }

}
