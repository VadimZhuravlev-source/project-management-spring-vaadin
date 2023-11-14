package com.pmvaadin.terms.calendars.exceptions;

import java.util.NoSuchElementException;

public enum YearlyPattern {

    ON((short) 0),
    THE((short) 1);

    private final Short code;

    private static final YearlyPattern[] ENUMS = values();

    YearlyPattern(short code) {
        this.code = code;
    }

    public Short getCode() {
        return code;
    }

    public static YearlyPattern of(short code) {
        if (code >= 0 && code <= 1) {
            return ENUMS[code];
        } else {
            throw new NoSuchElementException("Invalid value for yearly pattern code: " + code);
        }
    }

    @Override
    public String toString() {

        if (this == ON) {
            return "On";
        }else if (this == THE) {
            return "The";
        }

        return "";

    }

}
