package com.PMVaadin.PMVaadin.Calendars;

import com.PMVaadin.PMVaadin.Calendars.DayOfWeekSettings.DayOfWeekSettings;

import javax.persistence.PostLoad;

public class OperationListenerForDayOfWeekSettings {

    @PostLoad
    public void postLoad(DayOfWeekSettings dayOfWeekSettings) {

        dayOfWeekSettings.fillDayOfWeekString();

    }

}
