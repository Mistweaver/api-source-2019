package com.api.demo.security.configuration;


import com.api.demo.mongorepositories.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.Collections.emptyList;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    private static Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    public CustomUserDetailsService(UserRepository applicationUserRepository) {
        this.userRepository = applicationUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.api.demo.mongorepositories.users.User applicationUser;
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        try {
            applicationUser = userRepository.findByUsername(username);
            // authorities.add(new SimpleGrantedAuthority(applicationUser.getRole()));
        } catch (InternalAuthenticationServiceException e) {
            logger.error("An internal error occurred while trying to authenticate the user: " + e.getMessage());
            e.printStackTrace();
            return new User("", "", emptyList());
        }
        catch (UsernameNotFoundException e) {
            logger.error("Username " + username + " not found: " + e.getMessage());
            e.printStackTrace();
            return new User("", "", emptyList());
        }
        // return new User(applicationUser.getUsername(), applicationUser.getPassword(), true, applicationUser.isUserAccountEnabled(), true, applicationUser.isUserAccountUnlocked(), authorities);
        return new User(applicationUser.getUsername(), "", true, true, true, true, authorities);

    }
}
