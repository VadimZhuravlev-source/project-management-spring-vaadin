package com.pmvaadin.common.services;

import java.util.Collection;

public interface ListService<T, I> extends ItemService<T> {

    I add();
    I get(T representation);
    boolean delete(Collection<T> items);
    I copy(T dTO);

}
