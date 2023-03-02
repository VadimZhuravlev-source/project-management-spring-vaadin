package com.pmvaadin.projecttasks.links;

import com.pmvaadin.projecttasks.links.entities.Link;

import java.util.List;

public interface LinkValidation {

    LinkValidationMessage validate(List<? extends Link> links);

}
