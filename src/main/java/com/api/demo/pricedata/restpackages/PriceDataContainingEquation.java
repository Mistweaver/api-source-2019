package com.api.demo.pricedata.restpackages;

import com.api.demo.pricedata.repositories.pricedata.PriceData;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOffice;
import com.api.demo.pricedata.repositories.equations.EquationData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PriceDataContainingEquation {
    public EquationData equation;
    public PriceData priceData;
    public SalesOffice salesOffice;

    public PriceDataContainingEquation(EquationData equation, SalesOffice salesOffice, PriceData priceData) {
        this.equation = equation;
        this.salesOffice = salesOffice;
        this.priceData = priceData;
    }
}
