package com.PMVaadin.PMVaadin.security;

import com.google.common.hash.Hashing;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;

@Configuration
public class CustomPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence password) {
        return Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString();
    }

    @Override
    public boolean matches(CharSequence password, String passwordDB) {
        return password.equals(passwordDB);
    }
}
