package com.api.demo.pricedata.repositories.factories;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection="factories")
public class Factory extends BasicEntity {

    @Indexed
    private String manufacturerName;
    @Indexed
    private String displayName;
    @Indexed
    private String uniqueName;

    private String street;
    private String city;
    @Indexed
    private String state;
    @Indexed
    private String zipCode;

    private String phoneNumber;
    private String faxNumber;

    private String printableEntityName;
    private String printableEntityAddress;
    private String printableEntityCity;
    private String addendumENewModelParagraph;
    private String addendumEShowModelParagraph;


    public Factory() {
        this.manufacturerName = "";
        this.displayName = "";
        this.uniqueName = "";
        this.street = "";
        this.city = "";
        this.state = "";
        this.zipCode = "";
        this.phoneNumber = "";
        this.faxNumber = "";

        this.printableEntityName = "";
        this.printableEntityAddress = "";
        this.printableEntityCity = "";
        this.addendumENewModelParagraph = "";
        this.addendumEShowModelParagraph = "";
    }
}
