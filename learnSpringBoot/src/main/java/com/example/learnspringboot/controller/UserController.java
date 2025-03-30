package com.example.learnspringboot.controller;

import com.example.learnspringboot.dto.UserRequest;
import com.example.learnspringboot.entity.User;
import com.example.learnspringboot.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    public ResponseEntity<User> createUser(@RequestBody @Valid UserRequest request) {
        User user = new User(request.getName(), request.getEmail());
        return ResponseEntity.ok(userService.createUser(user));
    }
}
