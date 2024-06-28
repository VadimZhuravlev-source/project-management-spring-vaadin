package com.pmvaadin.project.dependencies;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class ProjectTasksIdConversionWrapper implements ProjectTasksIdConversion{

    public <I> List<I> convert(String ids){

        String[] idsString = ids.trim().split(",");
        List<I> typedIds = new ArrayList<>(idsString.length);
        for (String idString: idsString) {
            try {
                I id = (I) Integer.valueOf(idString);
                typedIds.add(id);
            }catch (Exception ignored) {

            }
        }

        return typedIds;

    }

}
