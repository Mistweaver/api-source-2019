package com.api.demo.pricedata.repositories.variables;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VariableData {
	private String id;
	private String name;
	private String key;
	private BigDecimal value;
	private String notes;

	public VariableData() {
		this.name = "";
		this.key= "";
		this.value= new BigDecimal(0);
		this.notes= "";
	}

	public void initializeFromVariable(Variable variable, BigDecimal _value) {
		this.id = variable.getId();
		this.name = variable.getName();
		this.key = variable.getKey();
		this.value = _value;
		this.notes = variable.getNotes();
	}

	public void updateFromVariable(Variable variable) {
		this.id = variable.getId();
		this.name = variable.getName();
		this.key = variable.getKey();
	}
}
