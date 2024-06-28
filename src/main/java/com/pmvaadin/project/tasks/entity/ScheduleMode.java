package com.pmvaadin.project.tasks.entity;

import java.util.NoSuchElementException;

public enum ScheduleMode {

    AUTO(0),
    MANUALLY(1);

    private final Integer code;
    private static final ScheduleMode[] ENUMS = values();

    ScheduleMode(int code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static ScheduleMode of(int code) {
        if (code >= 0 && code <= 1) {
            return ENUMS[code];
        } else {
            throw new NoSuchElementException("Invalid value for ScheduleMode: " + code);
        }
    }

    @Override
    public String toString() {
        if (this.code.equals(0)) {
            return "Automatic";
        }else if (this.code.equals(1)) {
            return "Manual";
        }
        return "";
    }

}
