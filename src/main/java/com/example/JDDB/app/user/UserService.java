package com.example.JDDB.app.user;




import com.example.JDDB.lib.core.repository.DiscordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final DiscordRepository<User, String> userRepository;

    public UserService() {
        this.userRepository = new DiscordRepository<>(User.class);
    }


    public User addUser(User user) {
        return userRepository.save(user);
    }

    public User getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }
}
