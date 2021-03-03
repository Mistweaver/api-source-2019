package com.api.demo.security.utils;

import com.microsoft.azure.spring.autoconfigure.aad.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    private static Logger logger = LoggerFactory.getLogger(AuditorAwareImpl.class);

    @Override
    public Optional<String> getCurrentAuditor() {
        // logger.info("Getting current Auditor");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            // logger.info(authentication.toString());
            if(authentication == null) {
                // logger.warn("AUTH IS NULL");
            } else if(!authentication.isAuthenticated()) {
                // logger.warn("NOT AUTHED");
            }
            return Optional.of("Machine Ghost");
        }

        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            // logger.info(userPrincipal.getName());
            Map<String, Object> map = userPrincipal.getClaims();
            // logger.info(map.toString());
            // logger.info(map.get("name").toString());
            return Optional.of(map.get("name").toString());
        } catch (Exception e) {
            logger.warn("Authentication type not of type Azure AD");
            if(!authentication.getPrincipal().toString().equals("anonymousUser")) {
                logger.error(e.getMessage());
            }
            return Optional.of(authentication.getPrincipal().toString());
        }

    }
}


