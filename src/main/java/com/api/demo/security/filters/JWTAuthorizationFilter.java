package com.api.demo.security.filters;

import com.api.demo.mongorepositories.applicationpackage.whitelist.WhiteList;
import com.api.demo.mongorepositories.applicationpackage.whitelist.WhiteListRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
    private static Logger logger = LoggerFactory.getLogger(JWTAuthorizationFilter.class);

    private List<String> IP_WHITELIST = new ArrayList();

    private WhiteListRepository whiteListRepository;

    public JWTAuthorizationFilter(AuthenticationManager authManager) {
        super(authManager);
    }

    public JWTAuthorizationFilter(AuthenticationManager authManager, WhiteListRepository whiteListRepository) {
        super(authManager);

        this.whiteListRepository = whiteListRepository;
        loadWhiteList();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) {

        /**
         * Log the header values.
         */
        Enumeration<String> headerNames = req.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String ne = headerNames.nextElement();
                //logger.info(ne + ": " + req.getHeader(ne));
            }
        }

        /**
         * Get the access token from the HTTP request header.
         */
        String accessToken = req.getHeader("Authorization");

        /**
         * If the access token doesn't exist, there is nothing to check. Skip the checks and chain the filter.
         */
        if(accessToken != null) {

            /**
             * Found an access token.  Remove the leading substring "Bearer" so the token can be decoded.
             */
            accessToken = accessToken.replaceFirst("Bearer ", "");
            // logger.info("access token: " + accessToken);

            /**
             * Decode the access token, and put the "claims" in a HashMap.
             */
            Jwt jwtToken = JwtHelper.decode(accessToken);
            String claims = jwtToken.getClaims();
            HashMap claimsMap = null;
            try {
                claimsMap = new ObjectMapper().readValue(claims, HashMap.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            /**
             * Retrieve the user's role from the access token claims.
             */
            String role = ((List<String>) claimsMap.get("roles")).get(0);
            // logger.info("role: " + role);
            GrantedAuthority authority = new SimpleGrantedAuthority(role);
            ArrayList<GrantedAuthority> authorityArrayList = new ArrayList<>();
            authorityArrayList.add(authority);

            /**
             * Log the access token claims info.
             */
            Iterator hmIterator = claimsMap.entrySet().iterator();
            while (hmIterator.hasNext()) {
                Map.Entry mapElement = (Map.Entry) hmIterator.next();
                // logger.info("**************");
                // logger.info(mapElement.getKey() + " : " + mapElement.getValue());
            }

            // logger.info("authorities" + ((List<String>) claimsMap.get("authorities")).get(0));

            /**
             * Check to see if the IP address of the request is authorized.
             */
            if (!ipIsAuthorized(req)) {
                loadWhiteList();
                if(!ipIsAuthorized(req)) {
                    throw new BadCredentialsException("Invalid IP Address");
                }
            }
        }

        /**
         * Chain the filter.
         */
        try {
            chain.doFilter(req, res);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServletException e) {
            e.printStackTrace();
        }

    }

    private boolean ipIsAuthorized(final HttpServletRequest request) {
        String remoteAddr = "";

        try {
            if (request != null) {
                String remoteAddrList = request.getHeader("X-FORWARDED-FOR");
                // logger.info("ip list: " + remoteAddrList);
                String[] remoteAddrs = {};
                if(remoteAddrList != null) {
                    remoteAddrs = remoteAddrList.split(",");
                    remoteAddr = remoteAddrs[0];
                } else {
                    remoteAddr = request.getRemoteAddr();
                }

                if (remoteAddr == null || "".equals(remoteAddr)) {
                    // logger.info("remote address null or blank");
                    // logger.info("Remote address: " + remoteAddr);
                    return false;
                }

                for(int i=0; i<IP_WHITELIST.size(); i++) {
                    // logger.info("ip wl entry: " + IP_WHITELIST.get(i));
                    if(IP_WHITELIST.get(i).equals(remoteAddr)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to read incoming IP Address: " + e.getMessage());
        }

        logger.warn("Invalid Ip Address: " + remoteAddr);
        return false;
    }


    private void loadWhiteList() {
        //logger.info("In load white list...");
        List<WhiteList> whiteList = whiteListRepository.findAll();
        for(WhiteList ip : whiteList) {
            //logger.info(ip.getDescription());
            //logger.info(ip.getIpAddress());
            this.IP_WHITELIST.add(ip.getIpAddress());
            this.IP_WHITELIST.add(ip.getIpv6Address());
        }
    }
}