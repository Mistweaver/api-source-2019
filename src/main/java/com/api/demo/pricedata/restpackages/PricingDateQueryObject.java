package com.api.demo.pricedata.restpackages;

import com.api.demo.pricedata.repositories.pricedata.PriceData;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PricingDateQueryObject {
	List<PriceData> currentPricing;
	public PricingDateQueryObject() {

	}
}
