package com.example.JDDB.app.user;


import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User test() {
        Random random = new Random();
        String[] names = {
                "Adam",
                "John",
                "Albert",
                "Frederick",
                "Steven",
                "Donald",
                "Jimmy",
                "George",
                "Walter",
                "Tom",
                "Jerry",
                "James",
        };



        String name = names[random.nextInt(names.length)];



        return userRepository.save(new User(
                name,
                name + "@email.com",
                random.nextInt(12,350)
        ));
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }
}
