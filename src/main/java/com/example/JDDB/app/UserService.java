package com.example.JDDB.app;


import com.example.JDDB.lib.DiscordRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final DiscordRepository<User, Long> userRepository;

    public UserService(DiscordRepository<User, Long> userRepository) {
        this.userRepository = userRepository;
    }

    public void addUser() {
        User user = new User();

        user.setAge(20);
        user.setEmail("example@email.com");

        userRepository.test(user);
//        userRepository.save(user);
    }
}
