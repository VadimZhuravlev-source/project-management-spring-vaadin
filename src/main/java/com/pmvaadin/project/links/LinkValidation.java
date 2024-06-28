package com.pmvaadin.project.links;

import com.pmvaadin.project.links.entities.Link;

import java.util.List;

public interface LinkValidation {

    LinkValidationMessage validate(List<? extends Link> links);

}
