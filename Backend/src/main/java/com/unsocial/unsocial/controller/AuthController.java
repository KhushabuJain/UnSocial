package com.unsocial.unsocial.controller;

import com.unsocial.unsocial.dto.LoginRequest;
import com.unsocial.unsocial.dto.SignupRequest;
import com.unsocial.unsocial.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    public AuthService service;

    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest req) {
        return service.signup(req);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest req) {
        return service.login(req);
    }
}