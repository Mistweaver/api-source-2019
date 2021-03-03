package com.api.demo.pricedata.restpackages;

// this package is used to create new drafts of price data

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateDraftForPriceDataPackage {
	private String draftDate;
	private List<String> locationIds;
	private List<String> priceDataIds;

	public CreateDraftForPriceDataPackage() {
		this.draftDate = "";
		this.locationIds = new ArrayList<>();
		this.priceDataIds = new ArrayList<>();
	}
}
