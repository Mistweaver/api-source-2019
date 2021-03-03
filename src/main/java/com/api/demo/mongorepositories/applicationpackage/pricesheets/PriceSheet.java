package com.api.demo.mongorepositories.applicationpackage.pricesheets;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Getter
@Setter
@Document(collection="pricesheets")
public class PriceSheet extends BasicEntity {
    @Indexed()
    private String locationId;
    @Indexed()
    private int month;
    @Indexed()
    private int year;

    private List<Series> seriesList;


    public PriceSheet() {
        Calendar cal = Calendar.getInstance();
        this.locationId = "";
        this.month = cal.get(Calendar.MONTH);
        this.year = cal.get(Calendar.YEAR);
        this.seriesList = new ArrayList<>();
    }

    public PriceSheet duplicateSheetForNextMonth() {
        PriceSheet duplicateSheet = new PriceSheet();
        duplicateSheet.locationId = this.locationId;
        if(this.month == 11) {
            duplicateSheet.month = 0;
            duplicateSheet.year += 1;
        } else {
            duplicateSheet.month += 1;
            duplicateSheet.year = this.year;
        }
        duplicateSheet.seriesList = this.seriesList;
        return duplicateSheet;
    }

    public PriceSheet copyPreviousSheetForCurrentMonth() {
        PriceSheet duplicateSheet = new PriceSheet();
        duplicateSheet.locationId = this.locationId;
        if(this.month == 11) {
            duplicateSheet.month = 0;
            duplicateSheet.year = this.year + 1;
        } else {
            duplicateSheet.month = this.month + 1;
            duplicateSheet.year = this.year;
        }
        duplicateSheet.seriesList = this.seriesList;
        return duplicateSheet;
    }

    public void addSeries(Series newSeries) {
        this.seriesList.add(newSeries);
    }

    public void deleteSeries(int index) {
        this.seriesList.remove(index);
    }

}
