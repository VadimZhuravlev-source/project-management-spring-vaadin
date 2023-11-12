package com.pmvaadin.terms.calendars.workingweeks;

import java.util.NoSuchElementException;

public enum IntervalSetting {

    DEFAULT(0),
    NONWORKING(1),
    CUSTOM(2);

    private final Integer code;

    private static final IntervalSetting[] ENUMS = values();

    IntervalSetting(int code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static IntervalSetting of(int intervalSettings) {
        if (intervalSettings >= 0 && intervalSettings <= 2) {
            return ENUMS[intervalSettings];
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
