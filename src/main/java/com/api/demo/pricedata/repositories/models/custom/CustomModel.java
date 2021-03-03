package com.api.demo.pricedata.repositories.models.custom;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Getter
@Setter
@Document(collection = "custommodels")
public class CustomModel extends BasicEntity {
	@Indexed
	private String createdForPurchaseAgreementId;
	@Indexed()
	private String factoryId;
	@Indexed()
	private String modelNumber;
	@Indexed()
	private String type;

	@Indexed()
	private boolean retired;

	private float width;
	private float length1;
	private float length2;
	private float numberOfBathrooms;
	private float numberOfBedrooms;
	private float numberOfDens;
	private String extraFeatures;
	private String notes;
	private BigDecimal estimatedSquareFeet;

	// website variables
	private String imageUrl;
	private String blueprintUrl;

	public CustomModel() {
		this.factoryId = "";
		this.modelNumber = "";
		this.type = "";
		this.retired = false;
		this.width = 0;
		this.length1 = 0;
		this.length2 = 0;
		this.numberOfBathrooms = 0;
		this.numberOfBedrooms = 0;
		this.numberOfDens = 0;
		this.extraFeatures = "";
		this.notes = "";
		this.estimatedSquareFeet = new BigDecimal(0);

		this.imageUrl = "";
		this.blueprintUrl = "";
	}
}

