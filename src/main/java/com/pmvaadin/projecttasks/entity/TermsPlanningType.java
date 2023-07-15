package com.pmvaadin.projecttasks.entity;

public enum TermsPlanningType {

    AUTOMATIC(0),
    MANUAL(1);

    private final Integer code;

    TermsPlanningType(int code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
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
