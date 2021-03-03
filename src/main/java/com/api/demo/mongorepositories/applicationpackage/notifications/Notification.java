package com.api.demo.mongorepositories.applicationpackage.notifications;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@Document(collection="notification")
public class Notification extends BasicEntity {
    @Indexed()
    private String userId;
    private String userEmail;
    private String details;
    @Indexed
    private String type;
    private Date date;
    private boolean sent;
}
