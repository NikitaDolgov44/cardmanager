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
@Profile("test")
public class TestSecurityConfig {
    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        UserDetails admin = User.withUsername("admin@test.com")
                .password("{noop}password")
                .authorities("ROLE_ADMIN")
                .build();

        UserDetails user = User.withUsername("user@test.com")
                .password("{noop}password")
                .authorities("ROLE_USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }
}
