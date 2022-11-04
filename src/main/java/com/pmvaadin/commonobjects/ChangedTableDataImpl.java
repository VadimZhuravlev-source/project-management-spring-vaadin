package com.pmvaadin.commonobjects;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChangedTableDataImpl<T> implements ChangedTableData<T> {

    private List<T> newItems;
    private List<T> changedItems;
    private List<T> deletedItems;

}
