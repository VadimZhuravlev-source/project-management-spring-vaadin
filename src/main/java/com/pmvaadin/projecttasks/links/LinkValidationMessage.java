package com.pmvaadin.projecttasks.links;

import com.pmvaadin.projecttasks.links.entities.Link;
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
