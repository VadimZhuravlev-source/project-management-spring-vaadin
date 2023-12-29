package com.pmvaadin.terms.calendars.exceptions;

import java.util.NoSuchElementException;

public enum RecurrencePattern {

    DAILY((short) 0),
    WEEKLY((short) 1),
    MONTHLY((short) 2),
    YEARLY((short) 3);

    private final Short code;

    private static final RecurrencePattern[] ENUMS = values();

    RecurrencePattern(short code) {
        this.code = code;
    }

    public Short getCode() {
        return code;
    }

    public static RecurrencePattern of(short code) {
        if (code >= 0 && code <= 3) {
            return ENUMS[code];
        } else {
            throw new NoSuchElementException("Invalid value for recurrence pattern code: " + code);
        }
    }

    @Override
    public String toString() {

        if (this == DAILY) {
            return "Daily";
        }else if (this == WEEKLY) {
            return "Weekly";
        }else if (this == MONTHLY) {
            return "Monthly";
        }else if (this == YEARLY) {
            return "Yearly";
        }

        return "";

    }

}
