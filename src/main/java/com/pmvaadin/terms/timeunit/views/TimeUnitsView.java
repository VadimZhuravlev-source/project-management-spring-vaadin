package com.pmvaadin.terms.timeunit.views;

import com.pmvaadin.MainLayout;
import com.pmvaadin.common.services.ListService;
import com.pmvaadin.common.vaadin.ItemList;
import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.pmvaadin.terms.timeunit.services.TimeUnitService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "timeUnits", layout = MainLayout.class)
@PageTitle("Time units | PM")
@PermitAll
public class TimeUnitsView extends VerticalLayout {

    private TimeUnitForm editingForm;
    private final TimeUnitService timeUnitService;

    public TimeUnitsView(TimeUnitService timeUnitService) {

        this.timeUnitService = timeUnitService;
        if (!(timeUnitService instanceof ListService)) {
            return;
        }

        var list = new Table((ListService) timeUnitService);
        add(list);

    }

    private class Table extends ItemList<TimeUnit, TimeUnit> {

        Table(ListService<TimeUnit, TimeUnit> listService) {
            super(listService);
            configureGrid();
        }

        private void configureGrid() {

            this.grid.addColumn(TimeUnit::getName).setHeader("Name");
            this.addPredefinedColumn(TimeUnit::isPredefined);
            onMouseDoubleClick(this::openNewItem);

            beforeAddition(this::openNewItem);
            onCoping(this::openNewItem);

        }

        private void openNewItem(TimeUnit timeUnit) {
            openEditingForm(timeUnit);
        }

        private void openEditingForm(TimeUnit timeUnit) {
            editingForm = new TimeUnitForm(timeUnit);
            editingForm.addListener(TimeUnitForm.SaveEvent.class, this::saveEvent);
            editingForm.addListener(TimeUnitForm.CloseEvent.class, closeEvent -> closeEditor());
            editingForm.open();
            setDeletionAvailable(false);
        }

        private void saveEvent(TimeUnitForm.SaveEvent event) {
            editingForm.close();
            var timeUnit = event.getTimeUnit();
            if (timeUnit != null)
                timeUnitService.save(timeUnit);
            this.grid.getDataProvider().refreshAll();
        }

        private void closeEditor() {
            editingForm.close();
            setDeletionAvailable(true);
            this.grid.getDataProvider().refreshAll();
        }

    }

}
