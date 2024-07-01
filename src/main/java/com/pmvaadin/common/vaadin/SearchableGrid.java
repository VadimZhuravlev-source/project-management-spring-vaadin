package com.pmvaadin.common.vaadin;

import com.pmvaadin.common.services.ItemService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.value.ValueChangeMode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SearchableGrid<T> extends VerticalLayout {

    private final ItemFilter itemFilter = new ItemFilter();

    protected final ItemService<T> itemService;

    protected final Grid<T> grid = new Grid<>();
    protected final HorizontalLayout toolBar;

    protected final TextField searchField = new TextField();
    protected final Button refreshButton = new Button("Refresh", new Icon("lumo", "reload"));

    private final ConfigurableFilterDataProvider<T, Void, ItemFilter> filterDataProvider;

    public SearchableGrid(ItemService<T> itemService) {

        this.itemService = itemService;
        DataProvider dataProvider = new DataProvider(itemService);
        this.filterDataProvider = dataProvider.withConfigurableFilter();
        grid.setItems(filterDataProvider);

        customizeGrid();
        toolBar = getToolbar();

        VerticalLayout layout = new VerticalLayout(toolBar, grid);
        layout.setPadding(false);
        add(layout);

    }

    public Grid.Column<T> addFlagColumn(Predicate<T> predicate) {

        return this.grid.addComponentColumn((item) -> {

            if(!predicate.test(item)){
                return new Div();
            }
            var icon = VaadinIcon.CHECK.create();
            icon.setColor("green");
            return icon;

        }).setHeader("Predefined");

    }

    public void removeSelectionColumn() {
        grid.getElement().getNode().runWhenAttached(ui ->
                ui.beforeClientResponse(this, context ->
                        getElement().executeJs(
                                "if (this.querySelector('vaadin-grid-flow-selection-column')) {" +
                                        " this.querySelector('vaadin-grid-flow-selection-column').hidden = true }")));
    }

    public void showDialog(Throwable error) {
        var confDialog = new ConfirmDialog();
        confDialog.setText(error.getMessage());
        confDialog.open();
    }

    private void customizeGrid() {

        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        removeSelectionColumn();
        grid.addItemClickListener(this::onMouseClick);

    }

    private void onMouseClick(ItemClickEvent<T> event) {

        if (event == null) {
            return;
        }

        var item = event.getItem();

        if (item == null) {
            grid.deselectAll();
            return;
        }

        Set<T> newSelectedItems = new HashSet<>();
        if (event.isCtrlKey()) {
            newSelectedItems.addAll(grid.asMultiSelect().getSelectedItems());
        }

        if (newSelectedItems.contains(item))
            newSelectedItems.remove(item);
        else
            newSelectedItems.add(item);

        grid.deselectAll();
        grid.asMultiSelect().setValue(newSelectedItems);

    }

    private HorizontalLayout getToolbar() {

        var toolBar = new HorizontalLayout();

        searchField.setWidth("50%");
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> {
            itemFilter.setSearchTerm(e.getValue());
            filterDataProvider.setFilter(itemFilter);
        });
        refreshButton.addClickListener(e -> grid.getDataProvider().refreshAll());

        toolBar.add(searchField, refreshButton);

        return toolBar;

    }

    private class DataProvider extends AbstractBackEndDataProvider<T, ItemFilter> {

        private final ItemService<T> itemService;
        private final ItemFilter emptyFilter = new ItemFilter();

        DataProvider(ItemService<T> itemService) {
            this.itemService = itemService;
        }

        @Override
        protected Stream<T> fetchFromBackEnd(Query<T, ItemFilter> query) {

            var pageable = PageRequest.of(query.getPage(), query.getPageSize());
            var filter = query.getFilter().orElse(emptyFilter).getSearchTerm();

            var list = itemService.getItems(filter, pageable);
            return list.stream();

        }

        @Override
        protected int sizeInBackEnd(Query<T, ItemFilter> query) {

            var pageable = PageRequest.of(query.getPage(), query.getPageSize());
            var filter = query.getFilter().orElse(emptyFilter).getSearchTerm();

            return itemService.sizeInBackEnd(filter, pageable);

        }

    }

    @Setter
    @Getter
    private static class ItemFilter {
        private String searchTerm = "";
    }

}
