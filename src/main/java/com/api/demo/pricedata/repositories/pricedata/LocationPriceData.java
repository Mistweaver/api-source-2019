package com.api.demo.pricedata.repositories.pricedata;

import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOffice;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LocationPriceData {
	private SalesOffice location;
	private List<SeriesData> activeData;
	private List<SeriesData> pendingData;
	private List<SeriesData> drafts;

	public LocationPriceData(SalesOffice office) {
		this.location = office;
		this.activeData = new ArrayList<>();
		this.pendingData = new ArrayList<>();
		this.drafts = new ArrayList<>();
	}

	public void addActiveData(PriceData data) {
		String dataSeriesName = data.getSeriesName();
		boolean seriesAlreadyExists = false;
		for(SeriesData sdata : activeData) {
			if(sdata.getSeriesName().equals(dataSeriesName)) {
				seriesAlreadyExists = true;
				sdata.addData(data);
			}
		}

		if(!seriesAlreadyExists) {
			SeriesData newSeriesData = new SeriesData(location);
			newSeriesData.setSeriesName(dataSeriesName);
			newSeriesData.addData(data);
			this.activeData.add(newSeriesData);
		}
	}

	public void addPendingData(PriceData data) {
		String dataSeriesName = data.getSeriesName();
		boolean seriesAlreadyExists = false;
		for(SeriesData sdata : pendingData) {
			if(sdata.getSeriesName().equals(dataSeriesName)) {
				seriesAlreadyExists = true;
				sdata.addData(data);
			}
		}

		if(!seriesAlreadyExists) {
			SeriesData newSeriesData = new SeriesData(location);
			newSeriesData.setSeriesName(dataSeriesName);
			newSeriesData.addData(data);
			this.pendingData.add(newSeriesData);
		}
	}

	public void addDraftData(PriceData data) {
		String dataSeriesName = data.getSeriesName();
		boolean seriesAlreadyExists = false;
		for(SeriesData sdata : drafts) {
			if(sdata.getSeriesName().equals(dataSeriesName)) {
				seriesAlreadyExists = true;
				sdata.addData(data);
			}
		}

		if(!seriesAlreadyExists) {
			SeriesData newSeriesData = new SeriesData(location);
			newSeriesData.setSeriesName(dataSeriesName);
			newSeriesData.addData(data);
			this.drafts.add(newSeriesData);
		}
	}




}
