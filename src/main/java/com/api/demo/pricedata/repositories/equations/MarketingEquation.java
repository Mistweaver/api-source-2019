package com.api.demo.pricedata.repositories.equations;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "marketingequations")
public class MarketingEquation extends BasicEntity {
	private String oldId;
	private String equationType;
	private String name;
	@Indexed(unique = true)
	private String key;

	private String equation;
	private String notes;
	private int roundingPosition;
	private String roundingDirection;		// UP or DOWN

	public MarketingEquation() {
		this.oldId = "";
		this.name = "";
		this.key = "";
		this.equation = "";
		this.notes = "";
		this.roundingPosition = -2;
		this.roundingDirection = "UP";
	}

	public void buildFromEquation(EquationData equationData) {
		this.oldId = equationData.getId();
		this.name = equationData.getName();
		this.key = equationData.getKey();
		this.equation = equationData.getEquation();
		this.notes = equationData.getNotes();
	}
}
