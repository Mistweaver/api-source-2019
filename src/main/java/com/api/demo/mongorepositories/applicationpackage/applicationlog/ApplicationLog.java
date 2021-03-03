package com.api.demo.mongorepositories.applicationpackage.applicationlog;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection="applicationlog")
public class ApplicationLog extends BasicEntity {
    private String userEmail;
    private String resourceId;
    private String resourceType;
    private String path;
    private String actionType;
    private JSONObject data;
}
