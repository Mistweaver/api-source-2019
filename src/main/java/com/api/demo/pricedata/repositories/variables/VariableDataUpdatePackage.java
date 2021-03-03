package com.api.demo.pricedata.repositories.variables;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VariableDataUpdatePackage {
	List<String> priceDataIds;
	VariableData newData;
}
