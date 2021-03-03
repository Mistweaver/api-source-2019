package com.api.demo.mongorepositories.applicationpackage.whitelist;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection="whitelist")
public class WhiteList extends BasicEntity {
    private String ipAddress;
    private String ipv6Address;
    private String description;
    private String locationId;

    public WhiteList() {
        this.ipAddress = "";
        this.ipv6Address = "";
        this.description = "";
        this.locationId = "";
    }
}
