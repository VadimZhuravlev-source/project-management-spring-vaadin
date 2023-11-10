package com.pmvaadin.terms.calendars.workingweeks;

import java.util.NoSuchElementException;

public enum IntervalSettings {

    DEFAULT(0),
    NONWORKING(1),
    CUSTOM(2);

    private final Integer code;

    private static final IntervalSettings[] ENUMS = values();

    IntervalSettings(int code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static IntervalSettings of(int intervalSettings) {
        if (intervalSettings >= 1 && intervalSettings <= 4) {
            return ENUMS[intervalSettings - 1];
        } else {
            throw new NoSuchElementException("Invalid value for interval settings: " + intervalSettings);
        }
    }

    @Override
    public String toString() {

        if (this == DEFAULT) {
            return "Use Project default times for these days.";
        }else if (this == NONWORKING) {
            return "Set days to nonworking time.";
        }else if (this == CUSTOM) {
            return "Set day(s) to these specific working times:";
        }

        return "";

    }

}
