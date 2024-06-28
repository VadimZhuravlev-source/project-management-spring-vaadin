package com.pmvaadin.project.tasks.frontend.elements;

import com.pmvaadin.common.ComboBoxWithButtons;
import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.pmvaadin.project.tasks.frontend.views.ProjectSelectionForm;
import com.pmvaadin.project.tasks.services.ComboBoxDataProvider;
import com.pmvaadin.project.tasks.services.ProjectTaskService;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.data.domain.PageRequest;

import java.util.stream.Stream;

@SpringComponent
public class ProjectComboBox extends ComboBoxWithButtons<ProjectTask> {

    private final ProjectTaskService service;
    private final ProjectSelectionForm selectionForm;

    public ProjectComboBox(ProjectTaskService service,
                           ProjectSelectionForm selectionForm) {

        this.service = service;
        this.selectionForm = selectionForm.newInstance();

        getSelectionAction().setVisible(true);

        this.selectionForm.addListener(ProjectSelectionForm.SelectEvent.class, event -> {
            var selectedItems = event.getSelectedItems();
            var selectedItemOpt = selectedItems.stream().findFirst();
            if (selectedItemOpt.isEmpty())
                return;
            var selectedItem = selectedItemOpt.get();
            if (selectedItem instanceof ProjectTask item)
                getComboBox().setValue(item);

        });
        this.selectionForm.addListener(ProjectSelectionForm.CloseEvent.class, event -> {
            event.getSource().close();
        });
        this.getSelectionAction().addClickListener(event -> this.selectionForm.open());

        this.setWidthFull();

        if (service instanceof ComboBoxDataProvider itemService)
            this.getComboBox().setDataProvider(getDataProvider(itemService), s -> s);

        this.getComboBox().setItemLabelGenerator(ProjectTask::getName);

    }

    public ProjectComboBox getInstance() {
        return new ProjectComboBox(service, selectionForm);
    }

    private DataProvider<ProjectTask, String> getDataProvider(ComboBoxDataProvider itemService) {

        return new DataProvider<>() {

            @Override
            public boolean isInMemory() {
                return false;
            }

            @Override
            public int size(Query<ProjectTask, String> query) {
                var pageable = PageRequest.of(query.getPage(), query.getPageSize());
                var filter = query.getFilter().orElse("");
                return itemService.sizeInBackEnd(filter, pageable);
            }

            @Override
            public Stream<ProjectTask> fetch(Query<ProjectTask, String> query) {
                var pageable = PageRequest.of(query.getPage(), query.getPageSize());
                var filter = query.getFilter().orElse("");
                return itemService.getItems(filter, pageable).stream();
            }

            @Override
            public void refreshItem(ProjectTask item) {

            }

            @Override
            public void refreshAll() {

            }

            @Override
            public Registration addDataProviderListener(DataProviderListener<ProjectTask> listener) {
                return null;
            }
        };

    }

}
