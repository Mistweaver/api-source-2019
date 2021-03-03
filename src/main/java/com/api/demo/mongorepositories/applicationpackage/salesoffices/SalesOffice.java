package com.api.demo.mongorepositories.applicationpackage.salesoffices;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(collection="salesoffices")
public class SalesOffice extends BasicEntity {
    @Indexed
    private int clientConsultantId;
    @Indexed
    private int avalaraId;
    @Indexed
    private String region;
    @Indexed
    private String locationCode;

    private String officeName;
    private String officeTitle;
    private String officeAddress;
    private String officeCity;
    private String officeCounty;
    private String officeState;
    private String officeZip;
    private String officePhoneNumber;
    private String officeFaxNumber;
    private String licenseNumber;
    private String[] manufacturerList;
    private List<String> factoryIDs;

    private String abbreviationHUD;
    private String abbreviationPM;

    private String webAddress;

    private String description;

    public SalesOffice() {
        this.clientConsultantId = 0;
        this.avalaraId = 0;
        this.region = "";
        this.locationCode = "";
        this.officeName = "";
        this.officeTitle = "";
        this.officeAddress = "";
        this.officeCity = "";
        this.officeCounty = "";
        this.officeState = "";
        this.officeZip = "";
        this.officePhoneNumber = "";
        this.officeFaxNumber = "";
        this.licenseNumber = "";
        this.factoryIDs = new ArrayList<String>();
    }

}
