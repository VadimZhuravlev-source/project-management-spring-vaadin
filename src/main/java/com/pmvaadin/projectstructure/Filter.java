package com.pmvaadin.projectstructure;

public interface Filter {

    String getFilterText();
    void setFilterText(String filterText);
    boolean isShowOnlyProjects();
    void setShowOnlyProjects(boolean showOnlyProjects);

    default boolean applyFilter() {
        return getFilterText() != null && !getFilterText().trim().isEmpty() || isShowOnlyProjects();
    }

}
