package com.PMVaadin.PMVaadin.security.Services;

import com.PMVaadin.PMVaadin.security.LoginUserDetails;
import com.PMVaadin.PMVaadin.security.User;
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