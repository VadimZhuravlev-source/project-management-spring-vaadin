package com.pmvaadin.security.services;

import com.pmvaadin.security.LoginUserDetails;
import com.pmvaadin.security.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LoginUserDetailsService implements UserDetailsService {
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        User foundUser = userService.getUserByPhoneNumber(phoneNumber);
        return new LoginUserDetails(foundUser);
    }
}