package com.szymon_jasinski.eventplanner.services;

import com.szymon_jasinski.eventplanner.entities.User;
import com.szymon_jasinski.eventplanner.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User create(User user) {
        return userRepository.save(user);
    }

    public User get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(id,
                        User.class.getSimpleName()));
    }
}
