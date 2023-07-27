package com.pmvaadin.projecttasks.entity;

import org.springframework.data.relational.core.sql.In;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    LocalDateTime getStartDate();
    void setStartDate(LocalDateTime startDate);
    LocalDateTime getFinishDate();
    void setFinishDate(LocalDateTime finishDate);
    boolean isProject();
    void setProject(boolean isProject);
    BigDecimal getDuration();
    void setDuration(BigDecimal duration);
    TermsPlanningType getTermsPlanningType();
    void setTermsPlanningType(TermsPlanningType termsPlanningType);
    Integer getCalendarId();
    void setCalendarId(Integer calendarId);

    String getCalendarRepresentation();
    void setCalendarRepresentation(String calendarRepresentation);

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
