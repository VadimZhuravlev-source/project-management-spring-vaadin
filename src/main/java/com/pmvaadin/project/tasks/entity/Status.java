package com.pmvaadin.project.tasks.entity;

import java.util.NoSuchElementException;

public enum Status {
    PLANNED(0),
    IN_PROGRESS(1),
    COMPLETED(2),
    CLOSED(3);

    private final Integer code;
    private static final Status[] ENUMS = values();
    Status(int code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static Status of(int statusCode) {
        if (statusCode >= 0 && statusCode <= 3) {
            return ENUMS[statusCode];
        } else {
            throw new NoSuchElementException("Invalid value for Status: " + statusCode);
        }
    }

    @Override
    public String toString() {
        if (this.code.equals(0)) {
            return "Planned";
        } else if (this.code.equals(1)) {
            return "In progress";
        } else if (this.code.equals(2)) {
            return "Completed";
        } else if (this.code.equals(3)) {
            return "Closed";
        }
        return "";
    }

}
