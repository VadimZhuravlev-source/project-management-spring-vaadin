package com.pmvaadin.commonobjects.vaadin;

import com.pmvaadin.commonobjects.services.ItemService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.springframework.data.domain.PageRequest;

import java.util.stream.Stream;

public class SeachableItemList<T> extends VerticalLayout {

    private final ItemFilter itemFilter = new ItemFilter();

    private final DataProvider dataProvider;

    protected final ItemService<T> itemService;

    protected final Grid<T> grid = new Grid<>();
    protected final HorizontalLayout toolBar;

    private ConfigurableFilterDataProvider<T, Void, ItemFilter> filterDataProvider;

    public SeachableItemList(ItemService<T> itemService) {

        this.itemService = itemService;
        this.dataProvider = new DataProvider(itemService);
        this.filterDataProvider = dataProvider.withConfigurableFilter();
        grid.setItems(filterDataProvider);

        toolBar = getToolbar();

        VerticalLayout layout = new VerticalLayout(toolBar, grid);
        layout.setPadding(false);
        add(layout);

    }

    private HorizontalLayout getToolbar() {

        var toolBar = new HorizontalLayout();

        var searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> {
            itemFilter.setSearchTerm(e.getValue());
            filterDataProvider.setFilter(itemFilter);
        });
        toolBar.add(searchField);

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

    private static class ItemFilter {
        private String searchTerm = "";

        public void setSearchTerm(String searchTerm) {
            this.searchTerm = searchTerm;
        }

        public String getSearchTerm() {
            return searchTerm;
        }

    }

}
