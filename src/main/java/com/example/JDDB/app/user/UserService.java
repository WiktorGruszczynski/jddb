package com.example.JDDB.app.user;


import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public List<User> test() {
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


        List<User> users = new ArrayList<>();

        for (int i=0; i<5000; i++){
            String name = names[random.nextInt(names.length)];

            users.add(
                    new User(
                            name,
                            name + random.nextInt(1000) + "@gmail.com",
                            random.nextInt(18,90)
                    )
            );
        }

        return userRepository.saveAll(users);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public User addUser(User user) {
        return userRepository.save(user);
    }

    public User findById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public long count() {
        return userRepository.count();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
