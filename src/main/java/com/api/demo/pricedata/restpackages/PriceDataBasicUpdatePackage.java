package com.api.demo.pricedata.restpackages;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PriceDataBasicUpdatePackage {
	private String id;
	private String name;
	private String seriesName;
	private String activeDate;
	private String expirationDate;
	// private String basePrice;

	public PriceDataBasicUpdatePackage() {
		this.id = "";
		this.name = "";
		this.seriesName = "";
		this.activeDate = "";
		this.expirationDate = "";
		// this.basePrice = "";
	}
}
