package com.example.cardmanager.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public UserDetailsService testUserDetailsService() {
        UserDetails user = User.builder()
                .username("user@test.com")
                .password("{noop}password") // {noop} означает plain text пароль
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("admin@test.com")
                .password("{noop}password")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }
}
