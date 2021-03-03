package com.api.demo.pricedata.restpackages;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewDataForModelPackage {
	private String modelId;
	private String locationId;
	private String name;
	private String seriesName;
	private String draftDate;
	private String basePrice;
	private String existingDataId;
	private String templateId;
}
