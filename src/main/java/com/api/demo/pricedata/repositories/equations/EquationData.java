package com.api.demo.pricedata.repositories.equations;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EquationData {
	private String id;
	private String name;
	private String key;
	private String notes;
	private String equation;
	private int roundingPosition;
	private String roundingDirection;		// UP or DOWN

	public EquationData() {
		this.name = "";
		this.key = "";
		this.equation = "";
		this.notes = "";
		this.roundingPosition = -2;
		this.roundingDirection = "UP";
	}

	public void initializeEquationDTO(Equation equation, String eqnString) {
		this.id = equation.getId();
		this.name = equation.getName();
		this.key = equation.getKey();
		this.equation = eqnString;
		this.notes = equation.getNotes();
	}

	public void updateFromEquation(Equation equation) {
		this.id = equation.getId();
		this.name = equation.getName();
		this.key = equation.getKey();
	}
}
