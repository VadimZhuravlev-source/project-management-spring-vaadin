package com.pmvaadin.costs.labor.entities;

import java.text.DateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;

public interface LaborCostRepresentation {

    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, new Locale("en", "US"));

    Integer getId();
    String getName();
    LocalDate getDay();
    Date getDateOfCreation();
    default String getRepresentation() {
        return "Labor cost " + String.format("%06d" , getId()) + " created " + dateFormat.format(getDateOfCreation());
    };
}
