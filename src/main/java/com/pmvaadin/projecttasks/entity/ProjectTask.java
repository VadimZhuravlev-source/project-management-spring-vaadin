package com.pmvaadin.projecttasks.entity;

import java.util.Date;

public interface ProjectTask extends ProjectTaskOrderedHierarchy {

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

    static String getHeaderCalendar() {
        return "Calendar";
    }

    String getName();
    void setName(String name);
    Date getStartDate();
    void setStartDate(Date startDate);
    Date getFinishDate();
    void setFinishDate(Date finishDate);

    int getLinksCheckSum();
    void setLinksCheckSum(int linksCheckSum);

    default String getRepresentation() {
        if (getWbs() == null) return getName();
        return getName() + " 'wbs:' " + getWbs();
    }

    int getChildrenCount();
    void setChildrenCount(int childrenCount);

    ProjectTask getParent();
    void setParent(ProjectTask parent);

}
