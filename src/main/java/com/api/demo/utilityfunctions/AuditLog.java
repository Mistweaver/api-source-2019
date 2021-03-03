package com.api.demo.utilityfunctions;

import com.api.demo.mongorepositories.applicationpackage.applicationlog.ApplicationLog;
import com.api.demo.mongorepositories.applicationpackage.applicationlog.ApplicationLogRepository;
import com.google.gson.Gson;
import com.microsoft.azure.spring.autoconfigure.aad.UserPrincipal;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuditLog {
    private static Logger logger = LoggerFactory.getLogger(AuditLog.class);

    @Autowired
	ApplicationLogRepository applicationLogRepository;

    public void log(String path, String action, Object object) throws ParseException {

        /**
         * Convert the object to JSON for storage.
         */
        Gson gson = new Gson();
        JSONParser parser = new JSONParser();
        JSONObject objectJson = (JSONObject) parser.parse(gson.toJson(object));

        //logger.info("storedFileJSON: ");
        //logger.info(objectJson.toString());

        /**
         * Get the unique user ID (user email address) from the security authentication context.
         */
        String userEmail = "unknown";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            //logger.info(userPrincipal.getName());
            Map<String, Object> map = userPrincipal.getClaims();
            //logger.info(map.toString());
            userEmail = map.get("preferred_username").toString();
            //logger.info("user email: " + userEmail);
        } catch (Exception e) {
            logger.warn("Could not access user from authentication.");
        }

        /**
         * Create the log entry object.
         */
        ApplicationLog applicationLog = new ApplicationLog();

        /**
         * Populate the log entry object.
         */
        applicationLog.setActionType(action);
        applicationLog.setPath(path);
        applicationLog.setUserEmail(userEmail);
        applicationLog.setResourceType(object.getClass().getCanonicalName());
        applicationLog.setResourceId(objectJson.get("id").toString());
        applicationLog.setData(objectJson);

        /**
         * Insert the log entry into the database.
         */
        applicationLogRepository.insert(applicationLog);

    }
}
