package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Repositories.UserRepository;
import com.PMVaadin.PMVaadin.security.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;

    public User getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Integer selectedID) {
        return userRepository.findById(selectedID).orElse(null);
    }
}
