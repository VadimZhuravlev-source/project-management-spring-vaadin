package com.pmvaadin.projecttasks.dependencies;

import com.pmvaadin.AppConfiguration;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.links.repositories.LinkRepository;
import com.pmvaadin.projecttasks.services.ProjectTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DependenciesServiceImpl implements DependenciesService {

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    private ProjectTaskService projectTaskService;
    private LinkRepository linkRepository;

    @Autowired
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Autowired
    public void setProjectTaskService(ProjectTaskService projectTaskService) {
        this.projectTaskService = projectTaskService;
    }

    @Autowired
    public void setLinkRepository(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    public  <I, L> DependenciesSet getAllDependencies(I pid, List<?> checkedIds) {

        String parameterValue = String.valueOf(checkedIds).replace('[', '{').replace(']', '}');

        List<Object[]> rows;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {

            Query query = entityManager.createNativeQuery(
                            "SELECT " +
                                    " dep.id," +
                                    " array_to_string(dep.path, ',') path," +
                                    " dep.is_cycle," +
                                    " dep.link_id" +
                                    " FROM get_all_dependencies(:pid, :checkedIds) dep"
                    )
                    .setParameter("pid", pid)
                    .setParameter("checkedIds", parameterValue);

            rows = query.getResultList();

        } catch (Exception e) {
            throw e;
        } finally {
            entityManager.close();
        }

        List<I> projectTaskIds = new ArrayList<>(rows.size());
        List<L> linkIds = new ArrayList<>();
        String path = "";
        boolean isCycle = false;
        for (Object[] row: rows) {
            I id = (I) row[0];
            L linkId = (L) row[3];
            if (linkId != null) linkIds.add(linkId);
            projectTaskIds.add(id);

            isCycle = isCycle || (boolean) row[2];
            if (isCycle) {
                path = (String) row[1];
                break;
            }
        }

        if (isCycle) {
            projectTaskIds.clear();
            linkIds.clear();
            ApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);
            ProjectTasksIdConversion idConversion = context.getBean(ProjectTasksIdConversion.class);
            projectTaskIds = idConversion.convert(path);
        }

        Map<?, ProjectTask> projectTasksMap = projectTaskService.getProjectTasksByIdWithFilledWbs(projectTaskIds);
        List<ProjectTask> projectTasks = projectTasksMap.values().stream().toList();
        List<Link> links = linkRepository.findAllById(linkIds);

        DependenciesSet dependenciesSet = new DependenciesSetImpl(projectTasks, links, isCycle);

        return dependenciesSet;

    }

}
