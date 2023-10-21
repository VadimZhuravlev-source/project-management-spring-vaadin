package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.LinkType;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import com.pmvaadin.projectview.ProjectTaskPropertyNames;
import com.pmvaadin.terms.calendars.services.CalendarService;
import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.pmvaadin.terms.timeunit.services.TimeUnitService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TreeHierarchyChangeServiceImpl implements TreeHierarchyChangeService {

    private ProjectTaskRepository projectTaskRepository;

    private final ProjectTaskPropertyNames propertyNames = new ProjectTaskPropertyNames();

    private CalendarService calendarService;

    private TimeUnitService timeUnitService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setProjectTaskRepository(ProjectTaskRepository projectTaskRepository) {
        this.projectTaskRepository = projectTaskRepository;
    }

    @Autowired
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Autowired
    public void setTimeUnitService(TimeUnitService timeUnitService) {
        this.timeUnitService = timeUnitService;
    }

    @Override
    public FetchedData getFetchedData(ProjectTask projectTask) {

        var projectTasks = getProjectTasks(projectTask);

        int childrenCountUpperLevel = fillChildrenCount(projectTasks);
        fillWbs(projectTasks, projectTask);
        return new FetchedDataImpl(childrenCountUpperLevel, projectTasks);
    }

    @Override
    public int getChildrenCount(ProjectTask projectTask) {

        if (projectTask == null || projectTask.getId() == null)
            return projectTaskRepository.getChildrenCount();

        return projectTaskRepository.getChildrenCount(projectTask.getId());

    }

    @Override
    public List<ProjectTask> getChildren(ProjectTask projectTask, List<String> chosenColumns) {

        var projectTasks = getProjectTasks(projectTask);

        fillData(projectTask, projectTasks, chosenColumns);

//        fillChildrenCount(projectTasks);
        fillWbs(projectTasks, projectTask, chosenColumns);
//        fillCalendar(projectTasks, chosenColumns);
//        fillDurationOrTimeUnit(projectTasks, chosenColumns);
//        fillLinks(projectTasks, chosenColumns);

        return projectTasks;

    }

    private void fillCalendar(List<ProjectTask> projectTasks, List<String> chosenColumns) {

        if (!chosenColumns.contains(propertyNames.getPropertyCalendar())) return;

        var calendarIds = projectTasks.stream().map(ProjectTask::getCalendarId).filter(Objects::nonNull).toList();
        var representations = calendarService.getRepresentationById(calendarIds);
        var defaultCalendar = calendarService.getDefaultCalendar();

        projectTasks.forEach(p -> {
            var rep = representations.getOrDefault(p.getId(), defaultCalendar.getRepresentation());
            p.setCalendarRepresentation(rep);
        });

    }

    private void fillDurationOrTimeUnit(List<ProjectTask> projectTasks, List<String> chosenColumns) {

        var fillDuration = chosenColumns.contains(propertyNames.getPropertyDurationRepresentation());
        var fillTimeUnit = chosenColumns.contains(propertyNames.getPropertyTimeUnit());
        if (!fillDuration && !fillTimeUnit) return;

        var ids = projectTasks.stream().map(ProjectTask::getTimeUnitId).filter(Objects::nonNull).toList();

        var timeUnitMap = timeUnitService.getTimeUnitsByIds(ids).stream()
                .collect(Collectors.toMap(TimeUnit::getId, t -> t));
        var defaultTimeUnit = timeUnitService.getPredefinedTimeUnit();

        projectTasks.forEach(p -> {
            var id = p.getTimeUnitId();
            var timeUnit = timeUnitMap.getOrDefault(id, defaultTimeUnit);
            if (timeUnit == null) return;
            if (fillTimeUnit) p.setTimeUnitRepresentation(timeUnit.toString());
            if (fillDuration) p.setDurationRepresentation(timeUnit.getDurationRepresentation(p.getDuration()));
        });

    }

    private void fillLinks(List<ProjectTask> projectTasks, List<String> chosenColumns) {

        if (!chosenColumns.contains(propertyNames.getPropertyLinks())) return;

    }

    private void fillWbs(List<ProjectTask> children, ProjectTask projectTask, List<String> chosenColumns) {

        if (!chosenColumns.contains(propertyNames.getPropertyWbs())) return;

        fillWbs(children, projectTask);

    }

    private List<ProjectTask> getProjectTasks(ProjectTask projectTask) {
        List<ProjectTask> projectTasks;
        if (Objects.isNull(projectTask) || Objects.isNull(projectTask.getId())) {
            projectTasks = projectTaskRepository.findByParentIdIsNullOrderByLevelOrderAsc();
        } else {
            projectTasks = projectTaskRepository.findByParentIdOrderByLevelOrderAsc(projectTask.getId());
        }
        return projectTasks;
    }

    private void fillWbs(List<ProjectTask> children, ProjectTask projectTask) {

        String wbs = "";
        if (!Objects.isNull(projectTask)) wbs = projectTask.getWbs() + ".";
        final String parentWbs = wbs;
        children.forEach(child -> child.setWbs(parentWbs + child.getLevelOrder()));

    }

    private int fillChildrenCount(List<ProjectTask> projectTasks) {

        Map<?, Integer> map = getProjectTasksChildrenCount(projectTasks);

        projectTasks.forEach(projectTask -> {
            int childrenCount = map.getOrDefault(projectTask.getId(), 0);
            if (childrenCount == 0) return;
            projectTask.setAmountOfChildren(childrenCount);
        });

        return map.getOrDefault(null, 0);

    }

    private <I> Map<I, Integer> getProjectTasksChildrenCount(List<ProjectTask> projectTasks) {

        String queryText =
                """
        SELECT
        	p.parent_id id,
            COUNT(p.id) children_count
        FROM project_tasks p
        WHERE p.parent_id = ANY(:pids)
        GROUP BY p.parent_id
        HAVING COUNT(p.id) > 0
        UNION
        SELECT
        	NULL id,
            COUNT(p.id) children_count
        FROM project_tasks p
        WHERE p.parent_id IS NULL
        """;

        var projectTaskIds = projectTasks.stream().map(ProjectTask::getId).filter(Objects::nonNull).toList();
        String parameterValue = String.valueOf(projectTaskIds).replace('[', '{').replace(']', '}');
        parameterValue = "'" + parameterValue + "'";

        queryText = queryText.replace(":pids", parameterValue);

        List<Object[]> rows;
//        EntityManager entityManager = entityManagerFactory.createEntityManager();
//        try {
        Query query = entityManager.createNativeQuery(queryText);
        rows = (List<Object[]>) query.getResultList();

//        }finally {
//            entityManager.close();
//        }

        Map<I, Integer> map = new HashMap<>(rows.size());

        var idIndex = 0;
        var countIndex = 1;

        for (Object[] row: rows) {
            I id = (I) row[idIndex];
            Integer count = ((BigInteger) row[countIndex]).intValue();
            map.put(id, count);
        }

        return map;

    }

    private void fillData(ProjectTask projectTask, List<ProjectTask> projectTasks, List<String> columns) {

        DataFromDataBase data = new DataFromDataBase(projectTask, projectTasks, columns);

        Map<?, Row> valueMap = data.executeQuery();

        projectTasks.forEach(projectTask1 -> {
            var row = valueMap.get(projectTask1.getId());
            if (row == null) return;
            fillAccordingValues(projectTask1, row, columns);

        });

    }

    private void fillAccordingValues(ProjectTask projectTask, Row row, List<String> columns) {

        projectTask.setAmountOfChildren(row.count());
        if (columns.contains(propertyNames.getPropertyCalendar())) projectTask.setCalendarRepresentation(row.cal());
        if (columns.contains(propertyNames.getPropertyTimeUnit())) projectTask.setTimeUnitRepresentation(row.time());
        if (columns.contains(propertyNames.getPropertyDurationRepresentation()))
            projectTask.setDurationRepresentation(new BigDecimal(row.dur()).setScale(2, RoundingMode.CEILING));

        fillLinkRepresentation(projectTask, row, columns);

    }

    private void fillLinkRepresentation(ProjectTask projectTask, Row row, List<String> columns) {

        if (!columns.contains(propertyNames.getPropertyLinks())) return;

        var linksRep = row.links();
        var links = linksRep.split(";");

        StringBuilder newLinks = new StringBuilder();
        for(String link: links) {
            var wbsWithType = link.split("-");
            if (wbsWithType.length <= 1) continue;

            var wbs = wbsWithType[0];
            var type = wbsWithType[1];
            var typeCode = Integer.valueOf(type);
            var linkType = LinkType.getByCode(typeCode);
            newLinks.append(wbs);
            newLinks.append(linkType.getShortRep());
            newLinks.append(";");
        }

        var lastIndex = newLinks.lastIndexOf(";");
        if (lastIndex > 0) newLinks.deleteCharAt(lastIndex);

        projectTask.setLinkRepresentation(newLinks.toString());

    }

    private class DataFromDataBase {

        private ProjectTask projectTask;
        private List<ProjectTask> projectTasks;
        private List<String> columns;
        private String queryText;

        DataFromDataBase(ProjectTask projectTask, List<ProjectTask> projectTasks, List<String> columns) {
            this.projectTask = projectTask;
            this.projectTasks = projectTasks;
            this.columns = columns;
            composeQueryText();
        }

        public <I> Map<?, Row> executeQuery() {

            List<Object[]> rows;
            Query query = entityManager.createNativeQuery(queryText);
            rows = (List<Object[]>) query.getResultList();

            var idIndex = 0;
            var countIndex = 1;
            var calIndex = 2;
            var timeIndex = 3;
            var durIndex = 4;
            var linksIndex = 5;
            Map<I, Row> map = new HashMap<>(rows.size());

            for (Object[] row: rows) {
                I id = (I) row[idIndex];
                var count = ((BigInteger) row[countIndex]).intValue();
                var calRep = Objects.toString(row[calIndex], "");
                var timeRep = Objects.toString(row[timeIndex], "");
                var durRep = Objects.toString(row[durIndex], "");
                var linkRep = Objects.toString(row[linksIndex], "");
                var rowMap = new Row(count, calRep, timeRep, durRep, linkRep);
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

        }

    }

    private record Row(int count, String cal, String time, String dur, String links) {}

    private String getQueryText() {
        String text =
        """
                WITH RECURSIVE found_ids AS (
                	
                	SELECT
                		project_tasks.id id
                		--calendar representation
                		,project_tasks.calendar_id
                		-- time unit representation
                		,project_tasks.time_unit_id
                		-- duration representation
                		,project_tasks.duration
                	FROM\s
                		project_tasks
                	WHERE
                		&condition
                		--project_tasks.parent_id IS NULL
                		--project_tasks.parent_id = ANY('{2}')
                	),
                	
                	-- amount of children
                	amount_of_children AS (
                	SELECT
                		found_ids.id id,
                		COUNT(project_tasks.id) amount
                	FROM\s
                		found_ids
                	LEFT JOIN project_tasks
                		ON found_ids.id = project_tasks.parent_id
                	GROUP BY\s
                		found_ids.id
                	),
                	
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
                			FROM found_ids
                		)	
                	),
                	
                	-- time unit representation
                	time_unit_representation AS (
                	SELECT
                		time_unit.id,
                		time_unit.name representation,
                		time_unit.number_of_hours
                	FROM
                		time_unit
                	WHERE
                		time_unit.id IN (
                			SELECT DISTINCT\s
                				time_unit_id\s
                			FROM found_ids
                		)	
                	),
                	
                	-- duration representation
                	durations AS (
                	SELECT
                		found_ids.id,
                		CASE\s
                			WHEN (time_unit_representation.number_of_hours <> 0 AND NOT time_unit_representation.number_of_hours IS NULL)
                			THEN found_ids.duration / (time_unit_representation.number_of_hours * 3600)\s
                			ELSE 0
                		END duration
                	FROM
                		found_ids
                	JOIN time_unit_representation
                		ON found_ids.time_unit_id = time_unit_representation.id
                	),
                	
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
                			FROM found_ids
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
                	
                	found_wbs AS (
                	SELECT
                		project_task id,
                		--id,
                		STRING_AGG(wbs || '-' || link_type, '; ') wbs
                	FROM found_links_wbs
                	WHERE\s
                		found_links_wbs.parent_id IS NULL
                	GROUP BY
                		project_task
                	),
                	
                	-- result data
                	results AS (
                	SELECT
                		found_ids.id,
                		amount_of_children.amount
                		,calendar_representation.representation calendar_rep
                		,time_unit_representation.representation time_rep
                		,durations.duration
                		,found_wbs.wbs
                	FROM	
                		found_ids
                	LEFT JOIN amount_of_children
                		ON found_ids.id = amount_of_children.id
                	LEFT JOIN calendar_representation
                		ON found_ids.calendar_id = calendar_representation.id
                	LEFT JOIN time_unit_representation
                		ON found_ids.time_unit_id = time_unit_representation.id
                	LEFT JOIN durations
                		ON found_ids.id = durations.id
                	LEFT JOIN found_wbs
                		ON found_ids.id = found_wbs.id
                	)
                	
                	SELECT * FROM results
                        
                        """;

        return text;

    }

    @Data
    @AllArgsConstructor
    private static class FetchedDataImpl implements FetchedData {
        private int childrenCountOfUpperLevel;
        List<ProjectTask> children;
    }

}
