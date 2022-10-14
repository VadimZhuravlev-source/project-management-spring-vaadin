package com.PMVaadin.PMVaadin.Entities.Links;

public enum LinkType {
    STARTSTART(0),
    STARTFINISH(1),
    FINISHSTART(2),
    FINISHFINISH(3);

    private Integer code;

    LinkType(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String toString() {
        if (this.code.equals(0)) {
            return "SS";
        }else if (this.code.equals(1)) {
            return "SF";
        }else if (this.code.equals(2)) {
            return "FS";
        }else if (this.code.equals(3)) {
            return "FF";
        }
        return "";
    }

}
