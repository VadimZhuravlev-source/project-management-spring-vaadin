package com.pmvaadin.security;

import com.pmvaadin.security.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class LoginUserDetails implements UserDetails {

    private static final String ROLE_PREFIX = "ROLE_";
    private final String name;
    private final String password;
    private final boolean isActive;
    private final Collection<? extends GrantedAuthority> authorities;

    public LoginUserDetails(User user) {
        this.name = user.getName();
        this.password = convertPasswordToString(user.getPassword());
        this.isActive = user.isActive();
        this.authorities = user.getRoles().stream()
                .map(userRole -> new SimpleGrantedAuthority(ROLE_PREFIX + userRole.getRole().name()))
                .toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return name;
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