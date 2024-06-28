package com.pmvaadin.project.dependencies;

import java.util.List;

public interface ProjectTasksIdConversion {

    <I> List<I> convert(String ids);

}
