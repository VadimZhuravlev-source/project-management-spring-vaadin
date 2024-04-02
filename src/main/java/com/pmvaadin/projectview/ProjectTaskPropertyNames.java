package com.pmvaadin.projectview;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.vaadin.flow.function.ValueProvider;

import java.util.*;

public class ProjectTaskPropertyNames {

    private final Map<String, String> propertiesMap = getPropertiesMap();
    private final Map<String, ColumnProperties> propertiesTree = getPropertiesTree();
    private final List<String> defaultColumns = getDefaultColumns();

    public Map<String, String> getProjectTaskPropertyNames() {
        return propertiesMap;
    }

    public Map<String, ColumnProperties> getAvailableColumnProps() {
        return propertiesTree;
    }

    public List<String> getTreeDefaultColumns() {
        return new ArrayList<>(defaultColumns);
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

    public String getPropertyIsProject() {
        return "isProject";
    }

    public String getPropertyLinks() {
        return "links";
    }

    public String getPropertyProgress() {
        return "progress";
    }

    public String getPropertyStatus() {
        return "status";
    }

    public String getPropertyLaborResources() {
        return "laborResources";
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

    public String getHeaderLinks() {
        return "Predecessors";
    }

    public String getHeaderIsProject() {
        return "Project";
    }

    public String getHeaderProgress() {
        return "Progress";
    }

    public String getHeaderStatus() {
        return "Status";
    }

    public String getHeaderIsMilestone() {
        return "Milestone";
    }

    public String getHeaderLaborResources() {
        return "Labor resources";
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
        map.put(getPropertyLaborResources(), getHeaderLaborResources());

        return Collections.unmodifiableMap(map);

    }

    private Map<String, ColumnProperties> getPropertiesTree() {

        Map<String, ColumnProperties> map = new LinkedHashMap<>();
        map.put(getPropertyWbs(), new ColumnProperties(getHeaderWbs(), ProjectTask::getWbs));
        map.put(getPropertyId(), new ColumnProperties(getHeaderId(), ProjectTask::getId));
        map.put(getPropertyCalendar(), new ColumnProperties(getHeaderCalendar(), ProjectTask::getCalendarRepresentation));
        map.put(getPropertyDurationRepresentation(), new ColumnProperties(getHeaderDurationRepresentation(), ProjectTask::getDurationRepresentation));
        map.put(getPropertyTimeUnit(), new ColumnProperties(getHeaderTimeUnit(), ProjectTask::getTimeUnitRepresentation));
        map.put(getPropertyIsProject(), new ColumnProperties(getHeaderIsProject(), ProjectTask::isProject));
        map.put(getPropertyScheduleMode(), new ColumnProperties(getHeaderScheduleMode(), ProjectTask::getScheduleMode));
        map.put(getPropertyStart(), new ColumnProperties(getHeaderStartDate(), ProjectTask::getStartDate));
        map.put(getPropertyFinish(), new ColumnProperties(getHeaderFinishDate(), ProjectTask::getFinishDate));
        map.put(getPropertyLinks(), new ColumnProperties(getHeaderLinks(), ProjectTask::getLinkRepresentation));
        map.put(getPropertyProgress(), new ColumnProperties(getHeaderProgress(), ProjectTask::getProgress));
        map.put(getPropertyStatus(), new ColumnProperties(getHeaderStatus(), ProjectTask::getStatus));
        map.put(getPropertyLaborResources(), new ColumnProperties(getHeaderLaborResources(), ProjectTask::getLaborResourceRepresentation));

        return Collections.unmodifiableMap(map);

    }

    private List<String> getDefaultColumns() {
        List<String> list = new ArrayList<>(3);
        list.add(getPropertyWbs());
        list.add(getPropertyStart());
        list.add(getPropertyFinish());
        return Collections.unmodifiableList(list);
    }

    record ColumnProperties(String representation, ValueProvider<ProjectTask, ?> valueProvider) {}

}
