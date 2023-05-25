package com.szymon_jasinski.eventplanner.services;

import com.szymon_jasinski.eventplanner.entities.User;
import com.szymon_jasinski.eventplanner.model.SecurityUser;
import com.szymon_jasinski.eventplanner.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email \"" + email + "\" not found"));
        return new SecurityUser(user);
    }
}
