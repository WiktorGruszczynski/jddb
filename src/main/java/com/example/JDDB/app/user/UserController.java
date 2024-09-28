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

    @GetMapping
    public User test(){
        return userService.test();
    }

    @GetMapping(path = "/del")
    public void deleteAll(){
        userService.deleteAll();
    }

}