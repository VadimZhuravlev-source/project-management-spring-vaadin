package com.PMVaadin.PMVaadin.Entities.Calendar;

public interface CalendarRowTable {

    static String getIdName(){return "Id";}
    static String getHeaderName(){return "Name";}
    static String getSettingName(){return "Setting";}
    Integer getId();
    String getName();
    CalendarSettings getSetting();

}
