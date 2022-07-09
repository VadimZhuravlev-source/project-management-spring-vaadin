package com.PMVaadin.PMVaadin.Entities.Calendar;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import java.util.UUID;

public class OperationListenerForCalendar {

    @PostLoad
    public void postLoad(Calendar calendar) {

        calendar.setSettingString(calendar.getSetting().toString());

    }

}
