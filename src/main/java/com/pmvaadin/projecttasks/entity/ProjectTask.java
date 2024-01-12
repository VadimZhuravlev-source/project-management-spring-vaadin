package com.pmvaadin.projecttasks.entity;

import com.pmvaadin.projecttasks.resources.entity.TaskResource;
import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.pmvaadin.terms.timeunit.entity.TimeUnitRepresentation;

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

    static String getHeaderId() {
        return "Id";
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

    static String getHeaderDurationRepresentation() {
        return "Duration";
    }

    static String getHeaderTimeUnit() {
        return "Time unit";
    }

    static String getHeaderScheduleMode() {
        return "Schedule mode";
    }

    String getName();
    void setName(String name);
    LocalDateTime getStartDate();
    void setStartDate(LocalDateTime startDate);
    LocalDateTime getFinishDate();
    void setFinishDate(LocalDateTime finishDate);
    boolean isProject();
    void setProject(boolean isProject);
    long getDuration();
    void setDuration(long duration);
    ScheduleMode getScheduleMode();
    void setScheduleMode(ScheduleMode scheduleMode);
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

    int getAmountOfChildren();
    void setAmountOfChildren(int amountOfChildren);

    ProjectTask getParent();
    void setParent(ProjectTask parent);

    Integer getTimeUnitId();
    void setTimeUnitId(Integer timeUnitId);

    Status getStatus();
    void setStatus(Status status);
    int getProgress();
    void setProgress(int progress);
    boolean isMilestone();
    void setMilestone(boolean isMilestone);

    BigDecimal getDurationRepresentation();
    void setDurationRepresentation(BigDecimal durationRepresentation);

    String getTimeUnitRepresentation();
    void setTimeUnitRepresentation(String timeUnitRepresentation);

    TimeUnitRepresentation getTimeUnit();
    void setTimeUnit(TimeUnitRepresentation timeUnitRepresentation);

    String getLinkRepresentation();
    void setLinkRepresentation(String linkRepresentation);

    int getResourcesCheckSum();
    void setResourcesCheckSum(int resourcesCheckSum);

    TaskResource getTaskResourceInstance();

}
