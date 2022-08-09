package com.PMVaadin.PMVaadin.Entities.ProjectTask;

import java.util.Date;

public interface ProjectTask extends ProjectTaskOrderedHierarchy {

    Integer getVersion();
    Date getDateOfCreation();
    Date getUpdateDate();
    boolean isNew();

    static String getHeaderName() {
        return "Name";
    }

    static String getHeaderWbs() {
        return "wbs";
    }

    static String getHeaderVersion() {
        return "Version";
    }

    static String getHeaderDateOfCreation() {
        return "Date of creation";
    }

    static String getHeaderUpdateDate() {
        return "Update date";
    }

    static String getHeaderStartDate() {
        return "Start date";
    }

    static String getHeaderFinishDate() {
        return "Finish date";
    }

    String getName();
    void setName(String name);
    Date getStartDate();
    void setStartDate(Date startDate);
    Date getFinishDate();
    void setFinishDate(Date finishDate);



}
