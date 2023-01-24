package com.pmvaadin.projecttasks.links;

import com.pmvaadin.projecttasks.links.entities.Link;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinkValidationImpl implements LinkValidation {

    @Override
    public LinkValidationMessage validate(List<? extends Link> links) {

        if (links.size() == 0) return new LinkValidationMessage();

        Map<Integer, Boolean> mapIdentity = new HashMap<>();
        boolean isOk = true;
        String message = "";
        Link tableRow = null;

        for (Link link: links) {
            if (link.getLinkedProjectTaskId() == null) {
                isOk = false;
                tableRow = link;
                message = getTextErrorNotFilledProjectTask();
                break;
            }
            if (link.getLinkType() == null) {
                isOk = false;
                tableRow = link;
                message = getTextErrorNotFilledLinkType();
                break;
            }
            if (mapIdentity.getOrDefault(link.getLinkedProjectTaskId(), false)) {
                isOk = false;
                tableRow = link;
                message = getTextErrorDuplicatedTasks();
                break;
            }
            mapIdentity.put(link.getLinkedProjectTaskId(), true);
        }

        return new LinkValidationMessage(isOk, message, tableRow);

    }

    private String getTextErrorNotFilledProjectTask() {
        return "In the predecessors, the project task is not filled";
    }

    private String getTextErrorNotFilledLinkType() {
        return "In the predecessors, the link type is not filled";
    }

    private String getTextErrorDuplicatedTasks() {
        return """
                An error occurred while establishing a connection between tasks.

                A double link from a predecessor task to a single successor task is not allowed.""";
    }

}
