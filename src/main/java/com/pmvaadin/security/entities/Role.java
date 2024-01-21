package com.pmvaadin.security.entities;

import java.util.NoSuchElementException;

public enum Role {
    ADMIN(0),
    PROJECT_MANAGER(1),
    WORKER(2);

    private final Integer code;

    private static final Role[] ENUMS = values();

    Role(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static Role of(Integer code) {
        if (code >= 0 && code <= 2) {
            return ENUMS[code];
        } else {
            throw new NoSuchElementException("Invalid value for Role code: " + code);
        }
    }

    @Override
    public String toString() {

        if (this == ADMIN) {
            return "Admin";
        } else if (this == PROJECT_MANAGER) {
            return "Project manager";
        } else if (this == WORKER) {
            return "Worker";
        }

        return "";

    }

}
