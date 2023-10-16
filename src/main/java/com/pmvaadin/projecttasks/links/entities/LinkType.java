package com.pmvaadin.projecttasks.links.entities;

public enum LinkType {
    STARTSTART(0),
    STARTFINISH(1),
    FINISHSTART(2),
    FINISHFINISH(3);

    private final Integer code;

    LinkType(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String toString() {
        if (this.code.equals(0)) {
            return "Start-to-start (SS)";
        }else if (this.code.equals(1)) {
            return "Start-to-finish (SF)";
        }else if (this.code.equals(2)) {
            return "Finish-to-start (FS)";
        }else if (this.code.equals(3)) {
            return "Finish-to-Finish (FF)";
        }
        return "";
    }

}
