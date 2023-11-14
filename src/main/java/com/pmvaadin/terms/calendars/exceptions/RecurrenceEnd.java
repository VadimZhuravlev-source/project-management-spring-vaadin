package com.pmvaadin.terms.calendars.exceptions;

import java.util.NoSuchElementException;

public enum RecurrenceEnd {

    AFTER((short) 0),
    BY((short) 1);

    private final Short code;

    private static final RecurrenceEnd[] ENUMS = values();

    RecurrenceEnd(short code) {
        this.code = code;
    }

    public Short getCode() {
        return code;
    }

    public static RecurrenceEnd of(short code) {
        if (code >= 0 && code <= 1) {
            return ENUMS[code];
        } else {
            throw new NoSuchElementException("Invalid value for RecurrenceEnd: " + code);
        }
    }

    @Override
    public String toString() {

        if (this == AFTER) {
            return "End after: ";
        }else if (this == BY) {
            return "End by: ";
        }

        return "";

    }

}
