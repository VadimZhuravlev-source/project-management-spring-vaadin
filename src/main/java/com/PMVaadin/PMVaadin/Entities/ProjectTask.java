package com.PMVaadin.PMVaadin.Entities;

import java.util.Date;

public interface ProjectTask extends ProjectTaskOrderedHierarchy {

    Integer getVersion();
    Date getDateOfCreation();
    Date getUpdateDate();

    String getName();
    void setName(String name);
    Date getStartDate();
    void setStartDate(Date startDate);
    Date getFinishDate();
    void setFinishDate(Date finishDate);


}
