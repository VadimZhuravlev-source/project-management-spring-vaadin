package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import com.pmvaadin.projectview.ProjectTaskPropertyNames;
import com.pmvaadin.terms.calendars.services.CalendarService;
import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.pmvaadin.terms.timeunit.services.TimeUnitService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TreeHierarchyChangeServiceImpl implements TreeHierarchyChangeService {

    private ProjectTaskRepository projectTaskRepository;

    private final ProjectTaskPropertyNames propertyNames = new ProjectTaskPropertyNames();

    private CalendarService calendarService;

    private TimeUnitService timeUnitService;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
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

        fillChildrenCount(projectTasks);
        fillWbs(projectTasks, projectTask, chosenColumns);
        fillCalendar(projectTasks, chosenColumns);
        fillDurationOrTimeUnit(projectTasks, chosenColumns);
        fillLinks(projectTasks, chosenColumns);

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
            projectTask.setChildrenCount(childrenCount);
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
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            Query query = entityManager.createNativeQuery(queryText);
            rows = (List<Object[]>) query.getResultList();

        }finally {
            entityManager.close();
        }

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

    @Data
    @AllArgsConstructor
    private static class FetchedDataImpl implements FetchedData {
        private int childrenCountOfUpperLevel;
        List<ProjectTask> children;
    }

}
