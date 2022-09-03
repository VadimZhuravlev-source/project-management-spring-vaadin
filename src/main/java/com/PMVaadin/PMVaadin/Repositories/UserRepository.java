package com.PMVaadin.PMVaadin.Repositories;

import com.PMVaadin.PMVaadin.security.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByPhoneNumber(String phoneNumber);
}
