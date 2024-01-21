package com.pmvaadin.security.repositories;

import com.pmvaadin.security.entities.User;
import com.pmvaadin.security.entities.UserImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends Repository<UserImpl, Integer> {
    Optional<User> findByName(String name);
    <T> List<T> findByNameLikeIgnoreCase(String name, Pageable pageable, Class<T> type);
    <I> Optional<User> findById(I i);
    void deleteAllById(Iterable<?> ids);
    User save(User user);

    List<User> findByIdInAndIsPredefinedTrue(Collection<?> ids);

}
