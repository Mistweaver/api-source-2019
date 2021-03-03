package com.api.demo.pricedata.repositories.pricedata;

import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOffice;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LocationSeriesList {
	private SalesOffice office;
	private List<String> seriesAvailable;

	public LocationSeriesList(SalesOffice _office) {
		this.office = _office;
	}

	public void addSeries(String newSeriesName) {
		boolean seriesAlreadyExists = false;
		// check if series exists
		for(String series : seriesAvailable) {
			if(series.equals(newSeriesName)) {
				seriesAlreadyExists = true;
			}
		}

		if(!seriesAlreadyExists) {
			this.seriesAvailable.add(newSeriesName);
		}
	}
}
