package com.api.demo.mongorepositories.applicationpackage.pricesheets;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Sheet {
    private List<SheetRow> rows;
    private int[] columnWidths;
    private int[] rowHeights;
   /* public Sheet() {
        this.rows = new ArrayList<>();
        for(int i = 0; i < 26; i++) {
            this.rows.add(new SheetRow());
        }
    }*/
}
