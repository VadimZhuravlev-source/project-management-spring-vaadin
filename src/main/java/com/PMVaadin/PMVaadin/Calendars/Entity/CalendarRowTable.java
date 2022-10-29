package com.PMVaadin.PMVaadin.Calendars.Entity;

public interface CalendarRowTable {

    static String getIdName(){return "Id";}
    static String getHeaderName(){return "Name";}
    static String getSettingName(){return "Setting";}
    Integer getId();
    String getName();
    CalendarSettings getSetting();

}
