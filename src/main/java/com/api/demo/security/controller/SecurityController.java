package com.api.demo.security.controller;

import com.api.demo.mongorepositories.users.UserRepository;
import com.api.demo.security.utils.TokenUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class SecurityController {
    private static Logger logger = LoggerFactory.getLogger(SecurityController.class);
    private UserRepository applicationUserRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private TokenUtility tokenutility = new TokenUtility();


    public SecurityController(UserRepository applicationUserRepository,
                              BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.applicationUserRepository = applicationUserRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;

    }
}
