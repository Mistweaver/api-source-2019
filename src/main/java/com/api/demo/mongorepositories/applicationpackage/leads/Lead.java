package com.api.demo.mongorepositories.applicationpackage.leads;

import com.api.demo.mongorepositories.BasicEntity;
import com.api.demo.controllers.DealStates;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection="lead")
public class Lead extends BasicEntity {
    @Indexed
    private String leadId;
    @Indexed
    private String firstName;
    @Indexed
    private String lastName;
    private String otherName;
    @Indexed
    private String emailAddress;
    @Indexed
    private String phone;

    @Indexed
    private String status;

    @Indexed()
    private String userId;
    private String leadSourceId;
    private String leadStatusId;
    @Indexed()
    private String locationId;
    private String deliveryStreet;
    private String deliveryCity;
    @Indexed
    private String deliveryState;
    private String deliveryZip;
    private String deliveryCountry;
    private String homeSite;
    private String cashBuyer;
    private String floorPlanId;
    private String bedroomId;
    private String modelOfInterest;
    private String extra;
    private boolean isDeadLead;

    public Lead() {
        this.status = DealStates.NEW.name();
    }


}
