package com.api.demo.pricedata.restpackages;

import com.api.demo.pricedata.repositories.equations.EquationData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RemoveMarketingEquationPackage {
	private EquationData equationToRemove;
	private List<String> priceDataIds;

	public RemoveMarketingEquationPackage() {
		this.equationToRemove = new EquationData();
		this.priceDataIds = new ArrayList<>();
	}
}
