package com.pmvaadin.projectstructure;

import com.pmvaadin.AppConfiguration;
import com.pmvaadin.projecttasks.dependencies.DependenciesService;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import com.pmvaadin.projecttasks.links.repositories.LinkRepository;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import com.pmvaadin.projecttasks.services.HierarchyService;
import com.pmvaadin.terms.calculation.TermCalculationData;
import com.pmvaadin.terms.calculation.TermsCalculation;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.services.TermCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManagerFactory;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectRecalculationImpl implements ProjectRecalculation {

    private ProjectTaskRepository projectTaskRepository;
    private HierarchyService hierarchyService;
    private LinkRepository linkRepository;
    private TermCalculationService calendarService;
    private DependenciesService dependenciesService;
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    public void setProjectTaskRepository(ProjectTaskRepository projectTaskRepository) {
        this.projectTaskRepository = projectTaskRepository;
    }

    @Autowired
    public void setHierarchyService(HierarchyService hierarchyService) {
        this.hierarchyService = hierarchyService;
    }

    @Autowired
    public void setLinkRepository(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Autowired
    public void setTermCalculationService(TermCalculationService calendarService) {
        this.calendarService = calendarService;
    }

    @Autowired
    public void setDependenciesService(DependenciesService dependenciesService) {
        this.dependenciesService = dependenciesService;
    }

    @Autowired
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    @Async
    public void recalculate(Set<ProjectTask> projects) {

        var counter = 0;
        var newProjects = projects;
        while (true) {
            var newProjects1 = new HashSet<ProjectTask>();
            for (var project : newProjects) {
                var projectsToRecalculate = calculateProject(project);
                newProjects1.addAll(projectsToRecalculate);
            }
            if (newProjects1.size() == 0 | counter++ > 1000) break;
            newProjects = newProjects1;
        }


    }

    @Override
    @Async
    public void recalculate(Calendar savedCalendar, Calendar oldCalendar) {
        // TODO defining of alterations of the DaysOfWeekSettings and the CalendarException between savedCalendar and oldCalendar,
        //  and finding of all the tasks that used this calendar
        var projects = getProjectsThatUsedCalendar(savedCalendar);
        recalculate(projects);
    }

    private Set<ProjectTask> calculateProject(ProjectTask projectTask) {

        var tasks = getAllTasksOfProject(projectTask);
        var tasksIds = tasks.stream().map(ProjectTask::getId).collect(Collectors.toSet());
        var entityManager = entityManagerFactory.createEntityManager();
        TermCalculationData termCalculationData = null;
        try {
            termCalculationData = dependenciesService.getAllDependenciesForTermCalc(entityManager, tasksIds);
        } finally {
            entityManager.close();
        }

        if (termCalculationData == null) return new HashSet<>(0);

        calendarService.fillCalendars(termCalculationData);

        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);
        TermsCalculation termsCalculation = context.getBean(TermsCalculation.class);
        var respond = termsCalculation.calculate(termCalculationData);

        if (respond.getChangedTasks().isEmpty()) return new HashSet<>(0);

        var savedTasks = projectTaskRepository.saveAll(respond.getChangedTasks());
        var ids = savedTasks.stream().map(ProjectTask::getId).collect(Collectors.toSet());

        TermCalculationData termCalculationData2;
        entityManager = entityManagerFactory.createEntityManager();
        try {
            termCalculationData2 = dependenciesService.getAllDependenciesForTermCalc(entityManager, ids);
        } finally {
            entityManager.close();
        }

        if (termCalculationData2 == null) return new HashSet<>(0);

        var respond2 = termsCalculation.calculate(termCalculationData2);
        projectTaskRepository.saveAll(respond2.getChangedTasks());

        var newRecalculatedProjects = respond.getRecalculatedProjects();
        newRecalculatedProjects.addAll(respond2.getRecalculatedProjects());

        return newRecalculatedProjects;

    }

    private List<ProjectTask> getAllTasksOfProject(ProjectTask project) {
        var projects = new ArrayList<ProjectTask>(1);
        projects.add(project);
        return hierarchyService.getElementsChildrenInDepth(projects);
    }

    private Set<ProjectTask> getProjectsThatUsedCalendar(Calendar calendar) {

        // TODO another way of a calculation of the tasks
        if (calendar.getId() == null) throw new IllegalArgumentException("Illegal parameter");

        var queryText = getQueryTextLookingForTasksOfCalendar();
        var convertedQueryText = queryText.replace(":id", calendar.getId().toString());

        List<ProjectTask> rows;
        var entityManager = entityManagerFactory.createEntityManager();
        try {

            var query = entityManager.createNativeQuery(convertedQueryText, ProjectTaskImpl.class);

            rows = (List<ProjectTask>) query.getResultList();

        } finally {
            entityManager.close();
        }

        if (rows == null) return new HashSet<>(0);

        return new HashSet<>(rows);

    }

    private String getQueryTextLookingForTasksOfCalendar() {

        return """
                WITH RECURSIVE tasks_that_used_calendar AS(
                	SELECT\s
                		id,
                		parent_id pid,
                		ARRAY[id] path,
                		is_project
                	FROM project_tasks\s
                	WHERE\s
                		calendar_id = :id
                	
                	UNION ALL
                	
                	SELECT
                		p.id,
                		p.parent_id,
                		tasks_that_used_calendar.path || p.parent_id path,
                		p.is_project
                	FROM
                		tasks_that_used_calendar
                	JOIN project_tasks p
                		ON p.id = tasks_that_used_calendar.pid
                			AND NOT (p.parent_id = ANY(tasks_that_used_calendar.path))
                	
                )
                SELECT DISTINCT
                		p.*
                	FROM
                		tasks_that_used_calendar
                	JOIN project_tasks p
                		ON p.id = tasks_that_used_calendar.id
                	WHERE
                		tasks_that_used_calendar.is_project OR tasks_that_used_calendar.pid IS NULL
                		""";

    }

}
