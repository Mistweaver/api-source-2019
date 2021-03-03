package com.api.demo.pricedata.repositories.variables;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "variables")
public class Variable extends BasicEntity {
	private String name;
	@Indexed(unique = true)
	private String key;
	private String notes;

	public Variable() {
		this.name = "";
		this.key = "";
		this.notes = "";
	}
}
