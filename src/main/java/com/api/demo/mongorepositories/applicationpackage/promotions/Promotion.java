package com.api.demo.mongorepositories.applicationpackage.promotions;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection="promotion")
public class Promotion extends BasicEntity {
    // Properties to query when looking for promotions
    @Indexed(unique = true)
    private String date;
    @Indexed
    private int month;
    @Indexed
    private int year;

    private String startDate;
    private String midMonthDate;
    private String endDate;

    private List<JSONObject> promoList;   // promotion name and the corresponding location ID


    public Promotion() {}

    public int getNextMonthInteger() {
        return (month + 1) == 12 ? 0 : month + 1;
    }

    public int getNextYearInteger() {
        return (month + 1) == 12 ? year + 1 : year;
    }

    public int getPreviousMonthInteger() {
        return (month - 1) == -1 ? 11 : month - 1;
    }

    public int getPreviousYearInteger() {
        return (month - 1) == -1 ? year - 1 : year;
    }

    // Months are zero indexed in the database, so query strings must reflect this format
    public String getNextMonthsQueryString() {
        return getNextMonthInteger() + "/" + getNextYearInteger();
    }

    public String getPreviousMonthsQueryString() {
        return getPreviousMonthInteger() + "/" + getPreviousYearInteger();
    }


    public void debug() {
        System.out.println("**********************");
        System.out.println("Promotion " + getId());
        System.out.println(date);
        System.out.println("Month: " + month);
        System.out.println("Year: " + year);
        System.out.println("Start: " + startDate);
        System.out.println("Mid month date: " + midMonthDate);
        System.out.println("End: " + endDate);
        System.out.println("**********************");
    }
}
