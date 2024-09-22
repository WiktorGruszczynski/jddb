package com.example.JDDB.app.user;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService serverService) {
        this.userService = serverService;
    }

    @PostMapping
    public User addUser(@RequestBody User user){
        return userService.addUser(user);
    }

    @GetMapping
    public User getUserById(@RequestParam("id") String id){
        return userService.getUserById(id);
    }

    @GetMapping(path = "/getAll")
    public List<User> getAll(){
        return userService.getAll();
    }

}
