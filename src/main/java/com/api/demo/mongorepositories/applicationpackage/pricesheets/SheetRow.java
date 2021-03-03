package com.api.demo.mongorepositories.applicationpackage.pricesheets;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SheetRow {
    private List<SheetCell> cells;

    /*public SheetRow() {
        this.cells = new ArrayList<>();
    }*/
}
