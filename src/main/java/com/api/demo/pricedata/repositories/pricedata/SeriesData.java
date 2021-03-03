package com.api.demo.pricedata.repositories.pricedata;

import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOffice;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SeriesData {
	private SalesOffice office;
	private String seriesName;
	private List<PriceData> priceData;


	public SeriesData(SalesOffice office) {
		this.office = office;
		this.seriesName = "";
		this.priceData = new ArrayList<>();
	}

	public void addData(PriceData data) {
		this.priceData.add(data);
	}
}
