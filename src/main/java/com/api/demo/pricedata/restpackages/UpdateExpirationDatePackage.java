package com.api.demo.pricedata.restpackages;

import lombok.Getter;
import lombok.Setter;
import org.aspectj.weaver.ArrayReferenceType;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UpdateExpirationDatePackage {
	private List<String> priceDataIds;
	private String newExpirationDate;

	public UpdateExpirationDatePackage() {
		this.priceDataIds = new ArrayList<>();
		this.newExpirationDate = "";
	}

}
