package com.pmvaadin.projectstructure.termscalculation;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import com.pmvaadin.projecttasks.entity.ScheduleMode;
import com.pmvaadin.projecttasks.links.entities.Link;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TermsCalculationImplTest {

    private TreeData treeDate = initiateTreeDate();

    @Test
    void calculate() {

        TermsCalculation termsCalculation = new TermsCalculationImpl();



    }

    private TreeData initiateTreeDate() {

        List<ProjectTask> projectTasks = new ArrayList<>();

        ProjectTask projectTask = getInstanceOfProjectTask(1, null, 12,
                LocalDateTime.of(2022, 1, 25, 12, 23, 11),
                LocalDateTime.of(2022, 1, 25, 12, 23, 11),
                2000, ScheduleMode.AUTO, 1);

        return new TreeData();

    }

    private ProjectTask getInstanceOfProjectTask(Integer id, Integer parentId, Integer childrenCount,
                                                 LocalDateTime start, LocalDateTime finish, long duration,
                                                 ScheduleMode mode, Integer calendarId) {
        ProjectTask projectTask = new ProjectTaskImpl();
        projectTask.setId(id);
        projectTask.setParentId(parentId);
        projectTask.setStartDate(start);
        projectTask.setFinishDate(finish);
        projectTask.setChildrenCount(childrenCount);
        projectTask.setDuration(duration);
        projectTask.setScheduleMode(mode);
        projectTask.setCalendarId(calendarId);
        return projectTask;
    }

    private class TreeData {
        List<ProjectTask> list = new ArrayList<>();
        List<Link> links = new ArrayList<>();
    }

}