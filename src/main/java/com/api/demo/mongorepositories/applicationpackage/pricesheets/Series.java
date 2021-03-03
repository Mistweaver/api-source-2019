package com.api.demo.mongorepositories.applicationpackage.pricesheets;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Series {
    private String seriesName;
    private String type;
    private String manufacturer;

    private Sheet tableData;

    private int modelNameIndex;
    private int modelNoIndex;
    private int sizeIndex;
    private int bedAndBathIndex;
    private int sqftIndex;
    private int basePriceIndex;
    private int msrpIndex;
    private int factoryDirectDiscountIndex;
    private int factoryDirectSaleIndex;
    private int pricePerSqFtIndex;

    private int factoryCostIndex;

    private int firstHalfDiscountIndex;
    private int secondHalfDiscountIndex;
}
