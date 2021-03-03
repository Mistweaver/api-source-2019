package com.api.demo.pricedata.restpackages;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TemplateUpdatePackage {
	private String templateId;
	private List<String> dataToUpdateFromTemplate;
	private boolean copyVariables;
	private boolean copyEquations;

	public TemplateUpdatePackage() {
		this.templateId = "";
		this.copyEquations = false;
		this.copyVariables = false;
		this.dataToUpdateFromTemplate = new ArrayList<>();
	}
}
