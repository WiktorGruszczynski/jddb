package com.example.JDDB.app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService serverService) {
        this.userService = serverService;
    }

    @GetMapping(path = "/user")
    public void addUser(){
        userService.addUser();
    }
}
