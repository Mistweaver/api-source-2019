package com.api.demo.pricedata.restpackages;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BasePricePercentageUpdatePackage {
	private List<String> priceDataIds;
	private String percentageChange;

	public BasePricePercentageUpdatePackage() {
		this.priceDataIds = new ArrayList<>();
		this.percentageChange = "0";
	}
}
