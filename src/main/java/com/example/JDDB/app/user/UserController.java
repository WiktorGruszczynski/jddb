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

    @GetMapping(path = "/test")
    public List<User> test(){
        return userService.test();
    }

    @PostMapping
    public User addUser(@RequestBody User user){
        return userService.addUser(user);
    }

    @GetMapping(path = "/getUser")
    public User getById(@RequestParam String id){
        return userService.findById(id);
    }

    @GetMapping(path = "/del")
    public void deleteAll(){
        userService.deleteAll();
    }

    @GetMapping(path = "/count")
    public long count(){
        return userService.count();
    }

    @GetMapping(path = "/getAll")
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }
}