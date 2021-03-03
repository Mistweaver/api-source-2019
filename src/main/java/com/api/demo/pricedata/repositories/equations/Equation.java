package com.api.demo.pricedata.repositories.equations;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "equations")
public class Equation extends BasicEntity {
	private String name;
	@Indexed(unique = true)
	private String key;
	private String notes;

	public Equation() {
		this.name = "";
		this.key = "";
		this.notes = "";
	}
}
