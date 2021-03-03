package com.api.demo.security.filters;

import com.api.demo.mongorepositories.users.User;
import com.api.demo.mongorepositories.users.UserRepository;
import com.api.demo.security.configuration.SecurityConstants;
import com.api.demo.security.utils.TokenUtility;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

import static java.util.Collections.emptyList;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    @Autowired
    private UserRepository userRepository;
    private static Logger logger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);
    @Autowired
    private TokenUtility tokenUtility = new TokenUtility();
    @Autowired
    private AuthenticationManager authenticationManager;
    public JWTAuthenticationFilter(AuthenticationManager authenticationManager, UserRepository repo) {
        this.authenticationManager = authenticationManager;
        this.userRepository = repo;

    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {
        try {
            User creds = new ObjectMapper().readValue(req.getInputStream(), User.class);
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getUsername(),
                            "",
                            new ArrayList<>())
            );
        } catch (IOException e) {
            logger.error("Authentication attempt failed: " + e.getMessage());
            e.printStackTrace();
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("", "",  emptyList()));
        } catch (InternalAuthenticationServiceException e) {
            logger.error("Internal Authentication Service Error: " + e.getMessage());
            e.printStackTrace();
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("", "",  emptyList()));
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication auth) throws IOException, ServletException {
        // logger.info("Building token from successful authentication");
        String token = tokenUtility.generateToken(auth, userRepository, req);
        res.addHeader("Access-Control-Expose-Headers", "Authorization");
        res.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + token);
        res.setContentType("application/json");
    }
}