package com.api.demo.security.utils;

import com.api.demo.mongorepositories.users.User;
import com.api.demo.mongorepositories.users.UserRepository;
import com.api.demo.security.configuration.SecurityConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Component
@Configuration
public class TokenUtility {

    private static Logger logger = LoggerFactory.getLogger(TokenUtility.class);
    private SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;
    Gson gson = new Gson();
    public TokenUtility() {}

    public String getUsernameFromToken(String token) {
        String username;
        try {
            final Claims claims = this.getAllClaimsFromToken(token);
            username = claims.getSubject();
        } catch (Exception e) {
            logger.error("Parsing username failed: " + e.getMessage());
            e.printStackTrace();
            username = null;
        }
        return username;
    }

    public String getUserIdFromToken(String token) {
        User user;
        String userId;
        try {
            final Claims claims = this.getAllClaimsFromToken(token);
            String userString = gson.toJson(claims.get("user"));
            user = gson.fromJson(userString, User.class);
        } catch (Exception e) {
            logger.error("Parsing user id failed: " + e.getMessage());
            user = null;
        }
        return user.getId();
    }
    /*********Throws null pointer when user is requested in rest controllers.  Don't know why ****/
    public User getUserFromToken(String token) {
        User user;

        try {
            final Claims claims = this.getAllClaimsFromToken(token);
            String userString = gson.toJson(claims.get("user"));
            user = gson.fromJson(userString, User.class);
        } catch (Exception e) {
            logger.error("Parsing user from token failed: " + e.getMessage());
            user = null;
        }
        return user;
    }

    public String getRoleFromRequest(HttpServletRequest request) {
        String role = "";
        try {
            String token = getToken(request);
            User user = getUserFromToken(token);
            // role = user.getRole();
        } catch (Exception e) {
            logger.error("Parsing role from token failed: " + e.getMessage());
            role = null;
        }
        return role;
    }

    public Date getExpirationDateFromToken(String token) {
        Date date;
        try {
            final Claims claims = this.getAllClaimsFromToken(token);
            date = claims.getExpiration();
        } catch (Exception e) {
            logger.error("Parsing expiration date from token failed: " + e.getMessage());
            e.printStackTrace();
            date = null;
        }
        return date;
    }

    public String getIpAddressFromToken(String token) {
        String address = "";
        try {
            final Claims claims = this.getAllClaimsFromToken(token);
            address = claims.get("ipaddr").toString();
        } catch (Exception e) {
            logger.error("Parsing IP address failed: " + e.getMessage());
            address = null;
        }
        return address;
    }

    public Date getIssuedAtDateFromToken(String token) {
        Date issueAt;
        try {
            final Claims claims = this.getAllClaimsFromToken(token);
            issueAt = claims.getIssuedAt();
        } catch (Exception e) {
            logger.error("Parsing issue date failed: " + e.getMessage());
            e.printStackTrace();
            issueAt = null;
        }
        return issueAt;
    }

    public String refreshToken(String token) {
        String refreshedToken;
        try {
            final Claims claims = this.getAllClaimsFromToken(token);
            claims.setIssuedAt(new Date(System.currentTimeMillis()));
            refreshedToken = Jwts.builder()
                    .setClaims(claims)
                    .setExpiration(generateExpirationDate())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .signWith( SIGNATURE_ALGORITHM, SecurityConstants.SECRET)
                    .compact();
        } catch (Exception e) {
            logger.error("Refreshing token failed: " + e.getMessage());
            e.printStackTrace();
            refreshedToken = null;
        }
        return refreshedToken;
    }

    public String generateToken(Authentication auth, UserRepository userRepo, HttpServletRequest request) {
        String username = ((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername();

        // Save last user IP Address
        User user = userRepo.findByUsername(username);
       //  user.setLastIpAddress(getIpAddressOfRequest(request));
        userRepo.save(user);

        JSONObject jsonUser = new JSONObject();

        Date expirationDate = null;
        if(((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername().equals("cimacorpadministrator")) {
            // expirationDate = generateQueueExpirationDate();
        } else {
            expirationDate = generateExpirationDate();
        }
        try {
            return Jwts.builder()
                    .setIssuer(SecurityConstants.APP_NAME)
                    .claim("userId", user.getId())
                    .claim("user", jsonUser)
                    .setSubject(((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis()))

                    .setExpiration(expirationDate)
                    .signWith(SIGNATURE_ALGORITHM, SecurityConstants.SECRET)
                    .compact();
        } catch (Exception e) {
            logger.error("Token generation failed: " + e.getMessage());
            return null;
        }

    }

    public void decodeMicrosoftJWT(String token) {
        /**
         * Found an access token.  Remove the leading substring "Bearer" so the token can be decoded.
         */
        token = token.replaceFirst("Bearer ", "");
        // logger.info("access token: " + token);

        /**
         * Decode the access token, and put the "claims" in a HashMap.
         */
        Jwt jwtToken = JwtHelper.decode(token);
        String claims = jwtToken.getClaims();
        HashMap claimsMap = null;
        try {
            claimsMap = new ObjectMapper().readValue(claims, HashMap.class);
        } catch (JsonProcessingException e) {
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
    }

    public List<String> returnMicrosoftRolesFromToken(String token) {
        /**
         * Found an access token.  Remove the leading substring "Bearer" so the token can be decoded.
         */
        token = token.replaceFirst("Bearer ", "");
        // logger.info("access token: " + token);

        /**
         * Decode the access token, and put the "claims" in a HashMap.
         */
        Jwt jwtToken = JwtHelper.decode(token);
        String claims = jwtToken.getClaims();
        HashMap claimsMap = null;
        try {
            claimsMap = new ObjectMapper().readValue(claims, HashMap.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        /**
         * Retrieve the user's roles from the access token claims.
         */
        return (List<String>) claimsMap.get("roles");
        /*
        String role = ((List<String>) claimsMap.get("roles")).get(0);
        logger.info("role: " + role);
        GrantedAuthority authority = new SimpleGrantedAuthority(role);
        ArrayList<GrantedAuthority> authorityArrayList = new ArrayList<>();
        authorityArrayList.add(authority);*/
    }

    private Claims getAllClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(SecurityConstants.SECRET)
                    .parseClaimsJws(token.replace(SecurityConstants.TOKEN_PREFIX, ""))
                    .getBody();
        } catch(SignatureException e) {
            logger.error("Parsing claims from token failed: JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted: " + e.getMessage());
            e.printStackTrace();
            claims = null;
        } catch (Exception e) {
            logger.error("Parsing claims from token failed: " + e.getMessage());
            e.printStackTrace();
            claims = null;
        }
        return claims;
    }

    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME);
    }

    public boolean validateToken(HttpServletRequest request) {
        try {
            String token = getToken(request);
            // logger.info("token: " + token);
            if(token != null) {
                //logger.info("Token is not null, getting IP address");
                // final String ipaddress = getIpAddressFromToken(token);
                //logger.info("Parsed IP address in token validation: " + ipaddress);
                /*if(!ipaddress.equals(getIpAddressOfRequest(request))) {
                    logger.error("Requesting IP conflict with issued IP");
                    logger.error("Token IP address: " + ipaddress);
                    logger.error("IP Address of request: " + getIpAddressOfRequest(request));
                    // return false;
                }*/
                //logger.info("IP address correct.  Getting username from token");
                final String username = getUsernameFromToken(token);
                //logger.info("username: " + username);
                //final Date created = getIssuedAtDateFromToken(token);
                //return (username != null && username.equals(userDetails.getUsername()) && !isCreatedBeforeLastPasswordReset(created, user.getLastPasswordResetDate()));
                return (username != null);// && new Date(System.currentTimeMillis()).before(getExpirationDateFromToken(token)));
            }
            return false;
        } catch (Exception e) {
            logger.error("Token validation failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
        return (lastPasswordReset != null && created.before(lastPasswordReset));
    }

    public String getToken( HttpServletRequest request ) {

        try {
            String authHeader = getAuthHeaderFromHeader( request );
            if ( authHeader != null && authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
                return authHeader.substring(7);
            }
        } catch (Exception e) {
            logger.error("Failed to get token: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public String getAuthHeaderFromHeader( HttpServletRequest request ) {
        return request.getHeader(SecurityConstants.HEADER_STRING);
    }

}