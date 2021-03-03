package com.api.demo.pricedata.repositories.pricedata;

import com.api.demo.mongorepositories.applicationpackage.promotions.Promotion;
import com.api.demo.mongorepositories.applicationpackage.promotionmodellist.PromotionModelList;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOffice;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Response package sent back to the client containing all the necessary information about pricing at the
 * requested date and time
 */
@Getter
public class PriceDataResponsePackage {
	private Promotion promotion;
	private int currentPromotionHalf;
	private PromotionModelList promotionModelList;
	private SalesOffice office;
	private List<SeriesData> priceDataList;

	public PriceDataResponsePackage() {
		this.promotion = null;
		this.currentPromotionHalf = 0;
		this.promotionModelList = null;
		this.office = null;
		this.priceDataList = new ArrayList<>();
	}

	public void setPromotion(Promotion _promotion) {
		this.promotion = _promotion;
	}

	public void setPromotionHalf(int half) {
		this.currentPromotionHalf = half;
	}

	public void setPromotionModelList(PromotionModelList list) {
		this.promotionModelList = list;
	}

	public void setSalesOffice(SalesOffice _office) {
		this.office = _office;
	}

	public void setPriceDataList(List<PriceData> priceDataList) {

		for (int i = 0; i < priceDataList.size(); i++) {
			PriceData priceData = priceDataList.get(i);

			String dataSeriesName = priceData.getSeriesName();
			boolean seriesAlreadyExists = false;
			for (SeriesData seriesData : this.priceDataList) {
				if (seriesData.getSeriesName().equals(dataSeriesName)) {
					seriesAlreadyExists = true;
					seriesData.addData(priceData);
				}
			}

			if (!seriesAlreadyExists) {
				SeriesData newSeriesData = new SeriesData(this.office);
				newSeriesData.setSeriesName(dataSeriesName);
				newSeriesData.addData(priceData);
				this.priceDataList.add(newSeriesData);
			}
		}
	}
}