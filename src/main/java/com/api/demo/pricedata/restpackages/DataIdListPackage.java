package com.api.demo.pricedata.restpackages;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/*** When you just need a list of price data ids to perform an action on ***/
@Getter
@Setter
public class DataIdListPackage {
    private List<String> priceDataIds;

    public DataIdListPackage() {
        this.priceDataIds = new ArrayList<>();
    }
}
