package com.pmvaadin.projecttasks.services.calculation;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projectview.ProjectTaskPropertyNames;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.*;

public class ColumnsData {

    private final ProjectTask projectTask;
    private final List<String> columns;
    private String queryText;
    private final EntityManager entityManager;
    private final ProjectTaskPropertyNames propertyNames;

    public ColumnsData(ProjectTask projectTask, List<String> columns, EntityManager entityManager,
                       ProjectTaskPropertyNames propertyNames) {
        this.projectTask = projectTask;
        this.columns = columns;
        this.entityManager = entityManager;
        this.propertyNames = propertyNames;
        composeQueryText();
    }

    public <I> Map<?, Row> executeQuery() {

        List<Object[]> rows;
        Query query = this.entityManager.createNativeQuery(queryText);
        rows = (List<Object[]>) query.getResultList();

        var idIndex = 0;
        var countIndex = 1;
        var calIndex = 2;
        var timeIndex = 3;
        var durIndex = 4;
        var linksIndex = 5;
        var laborResourcesIndex = 6;
        Map<I, Row> map = new HashMap<>(rows.size());

        for (Object[] row: rows) {
            I id = (I) row[idIndex];
            var count = (Integer) row[countIndex];
            var calRep = Objects.toString(row[calIndex], "");
            var timeRep = Objects.toString(row[timeIndex], "");
            var durRep = Objects.toString(row[durIndex], "");
            var linkRep = Objects.toString(row[linksIndex], "");
            var laborResourcesRep = Objects.toString(row[laborResourcesIndex], "");
            var rowMap = new Row(count, calRep, timeRep, durRep, linkRep, laborResourcesRep);
            map.put(id, rowMap);
        }

        return map;

    }

    private void composeQueryText() {

        queryText = getQueryText();
        String condition;
        if (Objects.isNull(projectTask) || Objects.isNull(projectTask.getId())) {
            condition = "project_tasks.parent_id IS NULL";
        } else {
            condition = "project_tasks.parent_id = " + projectTask.getId();
        }

        var conditionName = "&condition";
        queryText = queryText.replace(conditionName, condition);

        changeQuery();

    }

    private void changeQuery() {
        List<TablesInfo> list = new ArrayList<>(4);
        var calendarQueryTexts = getCalendarRepTablesInfo();
        list.add(calendarQueryTexts);
        var timeUnitQueryTexts = getTimeUnitRepTablesInfo();
        list.add(timeUnitQueryTexts);
        var durationQueryTexts = getDurationRepTablesInfo();
        list.add(durationQueryTexts);

        var linksQueryTexts = getLinkRepTablesInfo();
        list.add(linksQueryTexts);
        var laborResourcesQueryTexts = getLaborResourcesRepTablesInfo();
        list.add(laborResourcesQueryTexts);

        list.forEach(tablesInfo -> {
            queryText = queryText.replace("&pt_field", tablesInfo.ptField);
            queryText = queryText.replace("&table", tablesInfo.table);
            queryText = queryText.replace("&result_field", tablesInfo.resultField);
            queryText = queryText.replace("&join", tablesInfo.join);
        });

        queryText = queryText.replace("&pt_field", "");
        queryText = queryText.replace("&table", "");
        queryText = queryText.replace("&result_field", "");
        queryText = queryText.replace("&join", "");

    }

    private TablesInfo getDefaultTableInfo(String resultField) {
        return new TablesInfo("&pt_field", "&table", resultField, "&join");
    }

    private TablesInfo getCalendarRepTablesInfo() {

        if (!columns.contains(propertyNames.getPropertyCalendar())) {
            var resField = """
                        ,'' calendar_rep
                        &result_field
                        """;
            return getDefaultTableInfo(resField);
        }

        var fieldPT =
                """
                --calendar representation
                                        ,project_tasks.calendar_id
                                        &pt_field
                """;

        var table = """
--calendar representation
                    calendar_representation AS (
                    SELECT
                        calendars.id,
                        calendars.name representation
                    FROM
                        calendars
                    WHERE
                        calendars.id IN (
                            SELECT DISTINCT\s
                                calendar_id\s
                            FROM found_pts
                        )
                    ),
                    
                    &table
                    """;
        var resultField = """
,calendar_representation.representation calendar_rep
                        &result_field
                    """;

        var resultJoin = """
LEFT JOIN calendar_representation
                		ON found_pts.calendar_id = calendar_representation.id
                	&join
                    """;

        return new TablesInfo(fieldPT, table, resultField, resultJoin);

    }

    private TablesInfo getTimeUnitRepTablesInfo() {

        if (!(columns.contains(propertyNames.getPropertyTimeUnit())
                || columns.contains(propertyNames.getPropertyDurationRepresentation()))) {
            var resField = """
                        ,'' time_rep
                        &result_field
                        """;
            return getDefaultTableInfo(resField);
        }

        var fieldPT =
                """
-- time unit representation
                    ,project_tasks.time_unit_id
                    &pt_field
                """;

        var table = """
-- time unit representation
                    time_units_representation AS (
                    SELECT
                        time_units.id,
                        time_units.name representation,
                        time_units.number_of_hours
                    FROM
                        time_units
                    WHERE
                        time_units.id IN (
                            SELECT DISTINCT\s
                                time_unit_id\s
                            FROM found_pts
                        )
                    ),
                    
                    &table
                    """;
        var resultField = """
,time_units_representation.representation time_rep
                        &result_field
                    """;

        var resultJoin = """
LEFT JOIN time_units_representation
                		ON found_pts.time_unit_id = time_units_representation.id
                	&join
                    """;

        return new TablesInfo(fieldPT, table, resultField, resultJoin);

    }

    private TablesInfo getDurationRepTablesInfo() {

        if (!columns.contains(propertyNames.getPropertyDurationRepresentation())) {
            var resField = """
                        ,'' dur_rep
                        &result_field
                        """;
            return getDefaultTableInfo(resField);
        }

        var fieldPT =
                """
-- duration representation
                    ,project_tasks.duration
                    &pt_field
                """;

        var table = """
-- duration representation
                    durations AS (
                    SELECT
                        found_pts.id,
                        CASE\s
                            WHEN (time_units_representation.number_of_hours <> 0 AND NOT time_units_representation.number_of_hours IS NULL)
                            THEN found_pts.duration / (time_units_representation.number_of_hours * 3600)\s
                            ELSE 0
                        END duration
                    FROM
                        found_pts
                    JOIN time_units_representation
                        ON found_pts.time_unit_id = time_units_representation.id
                    ),
                    
                    &table
                    """;
        var resultField = """
,durations.duration dur_rep
                        &result_field
                    """;

        var resultJoin = """
LEFT JOIN durations
                		ON found_pts.id = durations.id
                	&join
                    """;

        return new TablesInfo(fieldPT, table, resultField, resultJoin);

    }

    private TablesInfo getLinkRepTablesInfo() {

        if (!columns.contains(propertyNames.getPropertyLinks())) {
            var resField = """
                        ,'' links_rep
                        &result_field
                        """;
            return getDefaultTableInfo(resField);
        }

        var fieldPT =
                """
&pt_field
                """;

        var table = """
-- links representation
                    links_initial_recursive AS (
                    SELECT
                        links.project_task,
                        links.link_type,
                        links.linked_project_task id,
                        project_tasks.parent_id,
                        CAST(project_tasks.level_order AS TEXT) wbs
                    FROM
                        links
                    JOIN project_tasks
                        ON links.linked_project_task = project_tasks.id
                    WHERE
                        links.project_task IN (
                            SELECT
                                id
                            FROM found_pts
                        )
                    ),
                    
                    found_links_wbs AS (
                    
                    SELECT
                        links.project_task,
                        links.link_type,
                        links.id,
                        links.parent_id,
                        ARRAY[links.id] path,
                        FALSE is_cycle,
                        links.wbs
                    FROM
                        links_initial_recursive links
                    
                    UNION ALL
                    
                    SELECT
                        found_links_wbs.project_task,
                        found_links_wbs.link_type,
                        found_links_wbs.id,
                        project_tasks.parent_id,
                        found_links_wbs.path || project_tasks.id path,
                        project_tasks.id = ANY(found_links_wbs.path) is_cycle,
                        project_tasks.level_order || '.' || found_links_wbs.wbs
                    FROM
                        found_links_wbs
                    JOIN project_tasks
                        ON found_links_wbs.parent_id = project_tasks.id
                            AND NOT found_links_wbs.is_cycle
                    
                    ),
                    
                    links_rep AS (
                    SELECT
                        project_task id,
                        --id,
                        STRING_AGG(wbs || '-' || link_type, '; ') rep
                    FROM found_links_wbs
                    WHERE\s
                        found_links_wbs.parent_id IS NULL
                    GROUP BY
                        project_task
                    ),
                    
                    &table
                    """;
        var resultField = """
,links_rep.rep links_rep
                        &result_field
                    """;

        var resultJoin = """
LEFT JOIN links_rep
                		ON found_pts.id = links_rep.id
                	&join
                    """;

        return new TablesInfo(fieldPT, table, resultField, resultJoin);

    }

    private TablesInfo getLaborResourcesRepTablesInfo() {

        if (!columns.contains(propertyNames.getPropertyLaborResources())) {
            var resField = """
                        ,'' labor_resources_rep
                        &result_field
                        """;
            return getDefaultTableInfo(resField);
        }

        var fieldPT =
                """
--labor resources representation
                                        &pt_field
                """;

        var table = """
--labor resources representation
                    labor_resources_representation AS (
                    SELECT
                        task_labor_resources.project_task_id project_task_id,
                        STRING_AGG(labor_resources.name || '-' || task_labor_resources.duration, '; ') rep
                    FROM
                        task_labor_resources
                        JOIN labor_resources
                            ON task_labor_resources.resource_id = labor_resources.id
                    WHERE
                        task_labor_resources.project_task_id IN (
                            SELECT DISTINCT\s
                                id\s
                            FROM found_pts
                        )
                    GROUP BY
                        project_task_id
                    ),
                    
                    &table
                    """;
        var resultField = """
,labor_resources_representation.rep labor_resources_rep
                        &result_field
                    """;

        var resultJoin = """
LEFT JOIN labor_resources_representation
                		ON found_pts.id = labor_resources_representation.project_task_id
                	&join
                    """;

        return new TablesInfo(fieldPT, table, resultField, resultJoin);

    }

    private record TablesInfo(String ptField, String table, String resultField, String join) {}

    private String getQueryText() {

        var text =

                """
                        WITH RECURSIVE found_pts AS (
                            
                            SELECT
                                project_tasks.id id
                                &pt_field
                            FROM\s
                                project_tasks
                            WHERE
                                &condition
                                --project_tasks.parent_id IS NULL
                                --project_tasks.parent_id = 2
                            ),
                            
                            -- amount of children
                            amount_of_children AS (
                            SELECT
                                found_pts.id id,
                                COUNT(project_tasks.id) amount
                            FROM\s
                                found_pts
                            LEFT JOIN project_tasks
                                ON found_pts.id = project_tasks.parent_id
                            GROUP BY\s
                                found_pts.id
                            ),
                            
                            &table
                            
                            -- result data
                            result_query AS (
                            SELECT
                                found_pts.id,
                                amount_of_children.amount
                                &result_field
                            FROM
                                found_pts
                            LEFT JOIN amount_of_children
                                ON found_pts.id = amount_of_children.id
                            &join
                            )
                            
                            SELECT
                                result_query.id,
                                  CAST(result_query.amount AS INT) amount,
                                  result_query.calendar_rep,
                                result_query.time_rep,
                                result_query.dur_rep,
                                result_query.links_rep,
                                result_query.labor_resources_rep
                            FROM result_query
                                
                                """;

        return text;

    }



}
