package com.api.demo.mongorepositories.applicationpackage.pricesheetchanges;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "changes")
public class Change extends BasicEntity {
    private String description;
    @Indexed
    private String locationId;
    @Indexed
    private String userId;

    public Change() {
        this.description = "";
        this.locationId = "";
        this.userId = "";
    }
}
