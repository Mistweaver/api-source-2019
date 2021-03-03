package com.api.demo.pricedata.restpackages;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BasePriceUpdatePackage {
	private String id;
	private String basePrice;

	public BasePriceUpdatePackage() {
		this.id = "";
		this.basePrice = "";
	}
}
