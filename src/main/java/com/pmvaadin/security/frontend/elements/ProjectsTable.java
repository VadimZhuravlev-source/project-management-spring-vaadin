package com.pmvaadin.security.frontend.elements;

import com.pmvaadin.common.ObjectGrid;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projecttasks.frontend.elements.ProjectComboBox;
import com.pmvaadin.security.entities.User;
import com.pmvaadin.security.entities.UserProject;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.util.HashSet;
import java.util.Objects;

@SpringComponent
public class ProjectsTable extends ObjectGrid<UserProject> {

    private final ProjectComboBox projectComboBox;

    ProjectsTable(ProjectComboBox projectComboBox) {
        this.projectComboBox = projectComboBox.getInstance();
        customizeBinder();
        customizeElement();
    }

    public ProjectsTable getInstance() {
        return new ProjectsTable(this.projectComboBox);
    }

    public void setUser(User user) {

        if (user == null)
            return;

        setInstantiatable(user::getUserProjectInstance);
        var projects = user.getProjects();
        if (projects != null)
            this.setItems(projects);
    }

    public boolean validate() {
        var items = getItems();
        var mapResource = new HashSet<>();
        items.forEach(userProject -> {
            if (userProject.getProject() == null || userProject.getProjectId() == null) {
                grid.getEditor().editItem(userProject);
                throw new StandardError("The project can not be empty");
            }
            if (mapResource.contains(userProject.getProject())) {
                grid.getEditor().editItem(userProject);
                throw new StandardError("The table can not contain a labor resource duplicates");
            }
            mapResource.add(userProject.getProject());
        });
        return true;
    }

    private void customizeElement() {
        setDeletable(true);
    }

    private void customizeBinder() {

        var nameColumn = addColumn(this::getTitle).
                setHeader("Project");

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

    private String getTitle(UserProject userProject) {
        if (userProject.getProject() == null)
            return "";
        return userProject.getProject().getName();
    }

}
