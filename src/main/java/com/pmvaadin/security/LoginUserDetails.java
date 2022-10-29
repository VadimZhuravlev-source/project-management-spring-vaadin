package com.pmvaadin.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;

public class LoginUserDetails implements UserDetails {

    private static final String ROLE_PREFIX = "ROLE_";
    private String firstName;
    private String phoneNumber;
    private String password;
    private Role role;
    private boolean isActive;
    private Collection<? extends GrantedAuthority> authorities;

    public LoginUserDetails(User user) {
        this.firstName = user.getFirstName();
        this.phoneNumber = user.getPhoneNumber();
        this.password = convertPasswordToString(user.getPassword());
        this.isActive = user.isActive();
        this.role = user.getRole();
        this.authorities = getAuthorities();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(ROLE_PREFIX + role.name());
        return Collections.singletonList(authority);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return firstName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isActive;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isActive;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    private String convertPasswordToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}