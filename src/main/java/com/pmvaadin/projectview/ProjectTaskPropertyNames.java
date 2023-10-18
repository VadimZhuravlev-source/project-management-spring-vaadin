package com.pmvaadin.projectview;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.vaadin.flow.function.ValueProvider;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProjectTaskPropertyNames {

    private final Map<String, String> propertiesMap = getPropertiesMap();
    private final Map<String, ColumnProperties> propertiesTree = getPropertiesTree();

    public Map<String, String> getProjectTaskPropertyNames() {
        return new HashMap<>(propertiesMap);
    }

    public Map<String, ColumnProperties> getAvailableColumnProps() {
        return new LinkedHashMap<>(propertiesTree);
    }

    public String getPropertyName() {
        return "name";
    }

    public String getPropertyWbs() {
        return "wbs";
    }

    public String getPropertyId() {
        return "id";
    }

    public String getPropertyVersion() {
        return "version";
    }

    public String getPropertyDateOfCreation() {
        return "dateOfCreation";
    }

    public String getPropertyUpdateDate() {
        return "updateDate";
    }

    public String getPropertyStart() {
        return "start";
    }

    public String getPropertyFinish() {
        return "finish";
    }

    public String getPropertyCalendar() {
        return "calendar";
    }

    public String getPropertyDurationRepresentation() {
        return "duration";
    }

    public String getPropertyTimeUnit() {
        return "timeUnit";
    }

    public String getPropertyScheduleMode() {
        return "scheduleMode";
    }

    public String getHeaderName() {
        return "Name";
    }

    public String getHeaderWbs() {
        return "wbs";
    }

    public String getHeaderId() {
        return "Id";
    }

    public String getHeaderVersion() {
        return "Version";
    }

    public String getHeaderDateOfCreation() {
        return "Date of creation";
    }

    public String getHeaderUpdateDate() {
        return "Update date";
    }

    public String getHeaderStartDate() {
        return "Start date";
    }

    public String getHeaderFinishDate() {
        return "Finish date";
    }

    public String getHeaderCalendar() {
        return "Calendar";
    }

    public String getHeaderDurationRepresentation() {
        return "Duration";
    }

    public String getHeaderTimeUnit() {
        return "Time unit";
    }

    public String getHeaderScheduleMode() {
        return "Schedule mode";
    }

    private Map<String, String> getPropertiesMap() {

        Map<String, String> map = new HashMap<>();
        map.put(getPropertyName(), getHeaderName());
        map.put(getPropertyWbs(), getHeaderWbs());
        map.put(getPropertyId(), getHeaderId());
        map.put(getPropertyVersion(), getHeaderVersion());
        map.put(getPropertyDateOfCreation(), getHeaderDateOfCreation());
        map.put(getPropertyUpdateDate(), getHeaderUpdateDate());
        map.put(getPropertyStart(), getHeaderStartDate());
        map.put(getPropertyFinish(), getHeaderFinishDate());
        map.put(getPropertyCalendar(), getHeaderCalendar());
        map.put(getPropertyDurationRepresentation(), getHeaderDurationRepresentation());
        map.put(getPropertyTimeUnit(), getHeaderTimeUnit());
        map.put(getPropertyScheduleMode(), getHeaderScheduleMode());

        return map;

    }

    private Map<String, ColumnProperties> getPropertiesTree() {

        Map<String, ColumnProperties> map = new LinkedHashMap<>();
        map.put(getPropertyWbs(), new ColumnProperties(getHeaderWbs(), ProjectTask::getWbs));
        map.put(getPropertyId(), new ColumnProperties(getHeaderId(), ProjectTask::getId));
        map.put(getPropertyCalendar(), new ColumnProperties(getHeaderCalendar(), ProjectTask::getCalendarRepresentation));
        map.put(getPropertyDurationRepresentation(), new ColumnProperties(getHeaderDurationRepresentation(), ProjectTask::getDurationRepresentation));
        map.put(getPropertyTimeUnit(), new ColumnProperties(getHeaderTimeUnit(), ProjectTask::getTimeUnitRepresentation));
        map.put(getPropertyScheduleMode(), new ColumnProperties(getHeaderScheduleMode(), ProjectTask::getScheduleMode));
        map.put(getPropertyStart(), new ColumnProperties(getHeaderStartDate(), ProjectTask::getStartDate));
        map.put(getPropertyFinish(), new ColumnProperties(getHeaderFinishDate(), ProjectTask::getFinishDate));

        return map;

    }

    record ColumnProperties(String representation, ValueProvider<ProjectTask, ?> valueProvider) {}

}
