package com.example.JDDB.app.user;


import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;


@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User test() {
        Random random = new Random();

        return userRepository.save(new User(
                 String.valueOf(new Date().getTime()) + random.nextLong(1000, 9999),
                "Adam",
                "adam@mail.com",
                random.nextInt(18,80)
        ));
    }
}
