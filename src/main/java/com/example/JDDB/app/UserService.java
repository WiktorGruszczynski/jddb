package com.example.JDDB.app;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void addUser() {
        User user = new User();

        user.setAge(20);
        user.setEmail("example@email.com");

        userRepository.save(user);
    }
}
