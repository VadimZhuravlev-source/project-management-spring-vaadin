package com.pmvaadin.resources.frontend.elements;

import com.pmvaadin.common.ComboBoxWithButtons;
import com.pmvaadin.common.services.ListService;
import com.pmvaadin.resources.entity.LaborResource;
import com.pmvaadin.resources.frontend.views.LaborResourceForm;
import com.pmvaadin.resources.frontend.views.LaborResourceSelectionForm;
import com.pmvaadin.resources.services.LaborResourceService;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.data.domain.PageRequest;

import java.util.stream.Stream;

@SpringComponent
public class LaborResourceComboBox extends ComboBoxWithButtons<LaborResource> {

    private final LaborResourceService laborResourceService;
    private final LaborResourceSelectionForm laborResourceSelectionForm;
    private final LaborResourceForm laborResourceForm;
    private ListService<LaborResource, LaborResource> itemService;

    public LaborResourceComboBox(LaborResourceService laborResourceService,
                                 LaborResourceSelectionForm laborResourceSelectionForm,
                                 LaborResourceForm laborResourceForm) {

        this.laborResourceService = laborResourceService;
        this.laborResourceSelectionForm = laborResourceSelectionForm.getInstance();
        this.laborResourceForm = laborResourceForm;
        if (laborResourceService instanceof ListService<?, ?> itemService) {
            this.itemService = (ListService<LaborResource, LaborResource>) itemService;
        } else
            return;

        var dataProvider = getDataProvider();
        this.getComboBox().setDataProvider(dataProvider, s -> s);

        getSelectionAction().setVisible(true);
        getOpenAction().setVisible(true);
        this.laborResourceSelectionForm.addListener(LaborResourceSelectionForm.SelectEvent.class, event -> {
            var selectedItems = event.getSelectedItems();
            var selectedItemOpt = selectedItems.stream().findFirst();
            if (selectedItemOpt.isEmpty()) return;
            var selectedItem = selectedItemOpt.get();
            if (selectedItem instanceof LaborResource item)
                getComboBox().setValue(item);

        });
        this.getSelectionAction().addClickListener(event -> this.laborResourceSelectionForm.open());

        this.getOpenAction().addClickListener(event -> {
            var value = this.getComboBox().getValue();
            if (value == null) return;
            var item = this.itemService.get(value);
            this.laborResourceForm.read(item);
            this.laborResourceForm.open();
        });

        this.setHeightFull();
    }

    public LaborResourceComboBox getInstance() {
        return new LaborResourceComboBox(laborResourceService, laborResourceSelectionForm, laborResourceForm);
    }

    private DataProvider<LaborResource, String> getDataProvider() {

        return new DataProvider<LaborResource, String>() {

            @Override
            public boolean isInMemory() {
                return false;
            }

            @Override
            public int size(Query<LaborResource, String> query) {
                var pageable = PageRequest.of(query.getPage(), query.getPageSize());
                var filter = query.getFilter().orElse("");
                return itemService.sizeInBackEnd(filter, pageable);
            }

            @Override
            public Stream<LaborResource> fetch(Query<LaborResource, String> query) {
                var pageable = PageRequest.of(query.getPage(), query.getPageSize());
                var filter = query.getFilter().orElse("");
                return itemService.getItems(filter, pageable).stream();
            }

            @Override
            public void refreshItem(LaborResource item) {

            }

            @Override
            public void refreshAll() {

            }

            @Override
            public Registration addDataProviderListener(DataProviderListener<LaborResource> listener) {
                return null;
            }
        };

    }

}
