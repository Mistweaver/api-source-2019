package com.api.demo.mongorepositories.applicationpackage.notifications;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class NotificationController {
    private static Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Value("${notification.enable}")
    private boolean notificationEnable;

    @Value("${notification.url}")
    private String notificationUrl;

    @Value("${notification.api.key}")
    private String apiKey;

    @Autowired
    private NotificationRepository notificationRepository;
    private Date lastReminderCheck;
    private List<Notification> notifications = new ArrayList<>();
    @GetMapping("/checknotifications")
    public ResponseEntity<String> checkNotifications() {
        logger.warn("Checking notifications");
        //get current time
        Date currentCheckTime = new Date(System.currentTimeMillis());
        //check if system has been started
        if(lastReminderCheck != null) {
            // System.out.println("Last time exists");
            //get all notifications between last check
            notifications = notificationRepository.findByDateBetween(lastReminderCheck, currentCheckTime);
            // System.out.println(notifications);

        } else {
            // System.out.println("Checking current time: " + currentCheckTime);
            // get all notifications prior to current time
            // notifications = notificationRepository.findByReminderDateBefore(currentCheckTime);
            notifications = notificationRepository.findByDateBetween(new Date(System.currentTimeMillis() - 86400000), currentCheckTime);
            // System.out.println(notifications);
        }
        // for each reminder
        for(Notification notification : notifications) {
            //check if already sent or not
            if(!notification.isSent()) {
                // send notification email
                sendMail(notification);
                //set to false
                notification.setSent(true);
                //save updated notification
                notificationRepository.save(notification);
            }

        }
        lastReminderCheck = currentCheckTime;

        return new ResponseEntity<String>("Notifications sent", HttpStatus.OK);
    }

    public void sendMail(Notification notification) {
        JSONObject mailPackage = new JSONObject();
        mailPackage.put("to", notification.getUserEmail());
        mailPackage.put("from", notification.getUserEmail());
        mailPackage.put("subject", notification.getType());
        mailPackage.put("content", notification.getDetails());

        // being sent to '/relay/' via POST
        //api key Xi3e9FV0vV34EM1ALGyMy4BcAa7Kb7TpakVXxnFx

        URL obj;
        if(notificationEnable) {
            try {
                obj = new URL(notificationUrl);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("Content-type", "application/json");
                con.setRequestProperty("x-api-key", apiKey);
                // System.out.println(mailPackage.toString());
                // System.out.println(mailPackage);
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(mailPackage.toString());
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();
                logger.info("response code: " + Integer.toString(responseCode));
                if (responseCode != 200 || responseCode != 202) {
                    logger.error("Mail Send failed with response code: " + Integer.toString(responseCode));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
