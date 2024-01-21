package com.pmvaadin.security.entities;

import java.util.NoSuchElementException;

public enum AccessType {

    ALL_ALLOWED(0),
    ONLY_IN_LIST(1),
    EXCEPT_IN_LIST(2),
    ALL_DENIED(3);

    private final Integer code;

    private static final AccessType[] ENUMS = values();

    AccessType(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static AccessType of(Integer code) {
        if (code >= 0 && code <= 3) {
            return ENUMS[code];
        } else {
            throw new NoSuchElementException("Invalid value for AccessType code: " + code);
        }
    }

    @Override
    public String toString() {

        if (this == ALL_ALLOWED) {
            return "All allowed";
        } else if (this == ONLY_IN_LIST) {
            return "Only in list allowed";
        } else if (this == EXCEPT_IN_LIST) {
            return "Allowed all except";
        } else if (this == ALL_DENIED) {
            return "All denied";
        }

        return "";

    }

}
