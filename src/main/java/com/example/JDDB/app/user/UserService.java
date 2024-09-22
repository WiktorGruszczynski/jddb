package com.example.JDDB.app.user;




import com.example.JDDB.lib.core.repository.DiscordRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final DiscordRepository<User, String> userRepository;

    public UserService() {
        this.userRepository = new DiscordRepository<>(User.class);
    }


    public User addUser() {
        User user = userRepository.save(
                new User(
                        "Josh",
                        "josh@mail.com",
                        35
                )
        );

        System.out.println(user.getId());

        return user;
    }
}
