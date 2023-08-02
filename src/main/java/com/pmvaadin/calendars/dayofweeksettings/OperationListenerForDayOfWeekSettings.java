package com.pmvaadin.calendars.dayofweeksettings;

import com.pmvaadin.calendars.dayofweeksettings.DayOfWeekSettings;

import javax.persistence.PostLoad;

public class OperationListenerForDayOfWeekSettings {

    @PostLoad
    public void postLoad(DayOfWeekSettings dayOfWeekSettings) {

        dayOfWeekSettings.fillDayOfWeekString();

    }

}
