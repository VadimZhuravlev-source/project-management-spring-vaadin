package com.pmvaadin.project.links;

import com.pmvaadin.project.links.entities.Link;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LinkValidationMessage {

    private boolean isOk = true;
    private String message = "";
    private Link tableRow;

}
