package com.pmvaadin.common.services;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemService<T> {

    List<T> getItems(String filter, Pageable pageable);

    int sizeInBackEnd(String filter, Pageable pageable);

}
