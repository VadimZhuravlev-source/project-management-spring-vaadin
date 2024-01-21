package com.pmvaadin.security.frontend.elements;

import com.pmvaadin.common.ObjectGrid;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.frontend.elements.ProjectComboBox;
import com.pmvaadin.security.entities.User;
import com.pmvaadin.security.entities.UserProject;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.util.HashMap;
import java.util.Objects;

@SpringComponent
public class ProjectsTable extends ObjectGrid<UserProject> {

    private final ProjectComboBox projectComboBox;

    ProjectsTable(ProjectComboBox projectComboBox) {
        this.projectComboBox = projectComboBox;
        customizeBinder();
        customizeElement();
    }

    public void setUser(User user) {

        if (user == null)
            return;

        setInstantiatable(user::getUserProjectInstance);
        var projects = user.getProjects();
        if (projects != null)
            this.setItems(user.getProjects());
    }

    public boolean validate() {
        var items = getItems();
        var mapResource = new HashMap<ProjectTask, Boolean>();
        items.forEach(userProject -> {
            if (userProject.getProject() == null || userProject.getProjectId() == null) {
                grid.getEditor().editItem(userProject);
                throw new StandardError("The project can not be empty");
            }
            if (mapResource.containsKey(userProject.getProject())) {
                grid.getEditor().editItem(userProject);
                throw new StandardError("The table can not contain a labor resource duplicates");
            }
            mapResource.put(userProject.getProject(), true);
        });
        return true;
    }

    private void customizeElement() {
        setDeletable(true);
    }

    private void customizeBinder() {

        var nameColumn = addColumn(UserProject::getProject).
                setHeader("Resource");

        var projectField = projectComboBox.getInstance();
        projectField.setWidthFull();
        projectField.setAutofocus(false);
        addCloseHandler(projectField, editor);
        binder.forField(projectField)
                .withValidator(Objects::nonNull, "Can not be empty")
                .bind(UserProject::getProject,
                        (taskResource, project) -> {
                            taskResource.setProject(project);
                            taskResource.setProjectId(project.getId());
                        });
        nameColumn.setEditorComponent(projectField);

    }

}
