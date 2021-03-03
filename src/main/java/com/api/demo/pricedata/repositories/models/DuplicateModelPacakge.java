package com.api.demo.pricedata.repositories.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DuplicateModelPacakge {
	private Model model;
	private List<Model> possibleDuplicates;
}
