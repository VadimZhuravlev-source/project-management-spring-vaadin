package com.pmvaadin.terms.calendars.exceptions;

import java.util.NoSuchElementException;

public enum NumberOfWeek {

    FIRST((short) 0),
    SECOND((short) 1),
    THIRD((short) 2),
    FOURTH((short) 3),
    LAST((short) 4);

    private final Short code;

    private static final NumberOfWeek[] ENUMS = values();

    NumberOfWeek(short code) {
        this.code = code;
    }

    public Short getCode() {
        return code;
    }

    public static NumberOfWeek of(short code) {
        if (code >= 0 && code <= 1) {
            return ENUMS[code];
        } else {
            throw new NoSuchElementException("Invalid value for number of week code: " + code);
        }
    }

    @Override
    public String toString() {

        if (this == FIRST) {
            return "First";
        }else if (this == SECOND) {
            return "Second";
        }else if (this == THIRD) {
            return "Third";
        }else if (this == FOURTH) {
            return "Fourth";
        }else if (this == LAST) {
            return "Last";
        }

        return "";

    }

}
