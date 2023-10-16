package com.pmvaadin.terms.calendars.view;

import java.util.function.Consumer;

public interface SelectionForm<T> {

    void addSelectionListener(Consumer<T> selection);

}
