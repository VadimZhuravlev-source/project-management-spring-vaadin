package com.pmvaadin.common;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

public class IntegerToDoubleConverter implements Converter<Double, Integer> {

    @Override
    public Result<Integer> convertToModel(Double aDouble, ValueContext valueContext) {
        if (aDouble == null) return Result.ok(0);
        return Result.ok(aDouble.intValue());
    }

    @Override
    public Double convertToPresentation(Integer integer, ValueContext valueContext) {
        if (integer == null) return 0d;
        return integer.doubleValue();
    }

}
