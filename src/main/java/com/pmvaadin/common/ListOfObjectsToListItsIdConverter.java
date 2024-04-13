package com.pmvaadin.common;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ListOfObjectsToListItsIdConverter {

    public static <I, R> String getIdsAsString(List<I> objects, Function<? super I, ? extends R> mapper) {
        var projectTaskIds = objects.stream().map(mapper).filter(Objects::nonNull).toList();
        String parameterValue = String.valueOf(projectTaskIds).replace('[', '{').replace(']', '}');
        parameterValue = "'" + parameterValue + "'";
        return parameterValue;
    }

}
