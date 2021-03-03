package com.api.demo.pricedata.restpackages;

import com.api.demo.pricedata.repositories.equations.EquationData;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EquationDataUpdatePackage {
	List<String> priceDataIds;
	EquationData newData;
}
