package com.pmvaadin.common;

import java.util.List;

public interface ChangedTableData<T> {

    List<T> getNewItems();
    List<T> getChangedItems();
    List<T> getDeletedItems();

}
