package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projectstructure.Filter;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.LinkType;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import com.pmvaadin.projecttasks.services.role.level.calculation.ColumnsData;
import com.pmvaadin.projecttasks.services.role.level.calculation.Row;
import com.pmvaadin.projecttasks.services.role.level.security.ProjectTaskFilter;
import com.pmvaadin.projectview.ProjectTaskPropertyNames;
import com.pmvaadin.security.services.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class TreeHierarchyChangeServiceImpl implements TreeHierarchyChangeService {

    private final ProjectTaskRepository projectTaskRepository;
    private final UserService userService;

    private final ProjectTaskPropertyNames propertyNames = new ProjectTaskPropertyNames();

    //    @PersistenceContext
    private final EntityManager entityManager;

    @Autowired
    public TreeHierarchyChangeServiceImpl(EntityManager entityManager, ProjectTaskRepository projectTaskRepository,
                                          UserService userService) {
        this.entityManager = entityManager;
        this.projectTaskRepository = projectTaskRepository;
        this.userService = userService;
    }

//    @Autowired
//    public void setEntityManager(EntityManager entityManager) {
//        this.entityManager = entityManager;
//    }
//
//    @Autowired
//    public void setProjectTaskRepository(ProjectTaskRepository projectTaskRepository) {
//        this.projectTaskRepository = projectTaskRepository;
//    }
//
//    @Autowired
//    public void setUserService(UserService userService) {
//        this.userService = userService;
//    }

    @Override
    public FetchedData getFetchedData(ProjectTask projectTask, Filter filter) {

        var projectTasks = getProjectTasks(projectTask, filter);

        int childrenCountUpperLevel = fillChildrenCount(projectTasks);
        fillWbs(projectTasks, projectTask);
        return new FetchedDataImpl(childrenCountUpperLevel, projectTasks);
    }

    @Override
    public int getChildrenCount(ProjectTask projectTask, Filter filter) {

        return getCountChildrenOfParent(projectTask, filter);

    }

    @Override
    public List<ProjectTask> getChildren(ProjectTask projectTask, List<String> chosenColumns, Filter filter) {

        var projectTasks = getProjectTasks(projectTask, filter);

        fillData(projectTask, projectTasks, chosenColumns);

        return projectTasks;

    }

    private void fillWbs(List<ProjectTask> children, ProjectTask projectTask, List<String> chosenColumns) {

        if (!chosenColumns.contains(propertyNames.getPropertyWbs())) return;

        fillWbs(children, projectTask);

    }

    private int getCountChildrenOfParent(ProjectTask projectTask, Filter filter) {

        var roleLevelSecurity = new ProjectTaskFilter(entityManager, userService, projectTaskRepository);
        return roleLevelSecurity.getCountProjectTasks(projectTask, filter);

    }

    private List<ProjectTask> getProjectTasks(ProjectTask projectTask, Filter filter) {

        var roleLevelSecurity = new ProjectTaskFilter(entityManager, userService, projectTaskRepository);
        return roleLevelSecurity.getProjectTasks(projectTask, filter);

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
                            CAST(COUNT(p.id) AS INT) children_count
                        FROM project_tasks p
                        WHERE p.parent_id IS NULL
                        """;

        var projectTaskIds = projectTasks.stream().map(ProjectTask::getId).filter(Objects::nonNull).toList();
        String parameterValue = String.valueOf(projectTaskIds).replace('[', '{').replace(']', '}');
        parameterValue = "'" + parameterValue + "'";

        queryText = queryText.replace(":pids", parameterValue);

        List<Object[]> rows;

        Query query = entityManager.createNativeQuery(queryText);
        rows = (List<Object[]>) query.getResultList();

        Map<I, Integer> map = new HashMap<>(rows.size());

        var idIndex = 0;
        var countIndex = 1;

        for (Object[] row : rows) {
            I id = (I) row[idIndex];
            Integer count = ((Number) row[countIndex]).intValue();
            map.put(id, count);
        }

        return map;

    }

    private void fillData(ProjectTask projectTask, List<ProjectTask> projectTasks, List<String> columns) {

        ColumnsData data = new ColumnsData(projectTasks, columns, entityManager, propertyNames);

        Map<?, Row> valueMap = data.executeQuery();

        projectTasks.forEach(projectTask1 -> {
            var row = valueMap.get(projectTask1.getId());
            if (row == null) return;
            fillAccordingValues(projectTask1, row, columns);

        });

        fillWbs(projectTasks, projectTask, columns);

    }

    private void fillAccordingValues(ProjectTask projectTask, Row row, List<String> columns) {

        projectTask.setAmountOfChildren(row.count());
        if (columns.contains(propertyNames.getPropertyCalendar())) projectTask.setCalendarRepresentation(row.cal());
        if (columns.contains(propertyNames.getPropertyTimeUnit())) projectTask.setTimeUnitRepresentation(row.time());
        if (columns.contains(propertyNames.getPropertyDurationRepresentation()))
            projectTask.setDurationRepresentation(new BigDecimal(row.dur()).setScale(2, RoundingMode.CEILING));
        if (columns.contains(propertyNames.getPropertyLaborResources()))
            projectTask.setLaborResourceRepresentation(row.laborResources());

        fillLinkRepresentation(projectTask, row, columns);

    }

    private void fillLinkRepresentation(ProjectTask projectTask, Row row, List<String> columns) {

        if (!columns.contains(propertyNames.getPropertyLinks())) return;

        var linksRep = row.links();
        var links = linksRep.split(";");

        StringBuilder newLinks = new StringBuilder();
        for (String link : links) {
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

    @Data
    @AllArgsConstructor
    private static class FetchedDataImpl implements FetchedData {
        private int childrenCountOfUpperLevel;
        List<ProjectTask> children;
    }

}
