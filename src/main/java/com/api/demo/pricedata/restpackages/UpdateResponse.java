package com.api.demo.pricedata.restpackages;

import com.api.demo.pricedata.repositories.pricedata.PriceData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateResponse {

    private boolean updateSuccessful;
    private String causeOfFailure;
    private PriceData data;

    public UpdateResponse(boolean updateSuccessful, String causeOfFailure, PriceData data) {
        this.updateSuccessful = updateSuccessful;
        this.causeOfFailure = causeOfFailure;
        this.data = data;
    }
}
