package com.api.demo.pricedata.restpackages;

import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOffice;
import com.api.demo.pricedata.repositories.pricedata.PriceData;
import com.api.demo.pricedata.repositories.variables.VariableData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PriceDataContainingVariable {
    public VariableData variable;
    public PriceData priceData;
    public SalesOffice salesOffice;

    public PriceDataContainingVariable(VariableData variable, SalesOffice salesOffice, PriceData priceData) {
        this.variable = variable;
        this.salesOffice = salesOffice;
        this.priceData = priceData;
    }
}
