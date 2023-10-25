package com.pmvaadin.commonobjects.services;

import java.util.Collection;

public interface ListService<T, I> extends ItemService<T> {

    I add();
    boolean delete(Collection<T> items);
    I copy(T dTO);

}
