package com.PMVaadin.PMVaadin.Entities.calendar;

import javax.persistence.PostLoad;

public class OperationListenerForDayOfWeekSettings {

    @PostLoad
    public void postLoad(DayOfWeekSettings dayOfWeekSettings) {

        dayOfWeekSettings.fillDayOfWeekString();

    }

}
