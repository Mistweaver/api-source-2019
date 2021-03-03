package com.api.demo.pricedata.restpackages;

import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOffice;
import com.api.demo.pricedata.repositories.pricedata.PriceData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PriceDataWithOfficePackage {
	private SalesOffice office;
	private PriceData data;

	public PriceDataWithOfficePackage(SalesOffice _office, PriceData _data) {
		this.office = _office;
		this.data = _data;
	}
}
