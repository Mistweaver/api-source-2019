package com.api.demo.mongorepositories.applicationpackage.pricesheets;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SheetCell {
    private int rowIndex;
    private int columnIndex;
    private String value;
    private String displayValue;

    private String toolTip;
    private String textColor;
    private String backgroundColor;
    private String formatType;
    private int roundingPosition;

    /*public SheetCell() {
        this.rowIndex = 0;
        this.columnIndex = 0;
        this.value = "";
        this.displayValue = "";
    }*/
}
