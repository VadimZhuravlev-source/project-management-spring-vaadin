package com.pmvaadin.projecttasks.common;

import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalToDoubleConverter implements Converter<Double, BigDecimal> {

    private NumberField numberField;

    public BigDecimalToDoubleConverter(NumberField numberField) {
        this.numberField = numberField;
    }

    @Override
    public Result<BigDecimal> convertToModel(Double aDouble, ValueContext valueContext) {
        double value;
        if (aDouble == null) value = 0;
        else value = aDouble;
        BigDecimal bigDecimal = BigDecimal.valueOf(value);
        BigDecimal scaledBigDecimal = bigDecimal.setScale(2, RoundingMode.CEILING);
        if (!scaledBigDecimal.equals(bigDecimal)) {
            Double newDouble = scaledBigDecimal.doubleValue();
            this.numberField.setValue(newDouble);
        }
        return Result.ok(bigDecimal);
    }

    @Override
    public Double convertToPresentation(BigDecimal bigDecimal, ValueContext valueContext) {
        if (bigDecimal == null) return 0d;
        return bigDecimal.doubleValue();
    }

}
