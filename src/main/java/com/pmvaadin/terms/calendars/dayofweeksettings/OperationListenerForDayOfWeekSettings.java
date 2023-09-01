package com.pmvaadin.terms.calendars.dayofweeksettings;

import javax.persistence.PostLoad;

public class OperationListenerForDayOfWeekSettings {

    @PostLoad
    public void postLoad(DayOfWeekSettings dayOfWeekSettings) {

        dayOfWeekSettings.fillDayOfWeekString();

    }

}
