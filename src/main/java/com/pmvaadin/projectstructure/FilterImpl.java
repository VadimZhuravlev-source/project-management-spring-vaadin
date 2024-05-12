package com.pmvaadin.projectstructure;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FilterImpl implements Filter {

    private String filterText = "";
    private boolean showOnlyProjects;

}
