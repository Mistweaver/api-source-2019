package com.api.demo.mongorepositories.applicationpackage.pricesheets;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelProperty {
    private String key;
    private String value;
    private boolean hiddenValue;

    public ModelProperty() {
        this.key = "";
        this.value = "";
        this.hiddenValue = true;
    }
}
