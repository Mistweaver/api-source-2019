package com.api.demo.mongorepositories.applicationpackage.optionsheets;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "optionsheets")
public class OptionSheet extends BasicEntity {
    @Indexed()
    private String locationId;
    @Indexed()
    private int month;
    @Indexed()
    private int year;
    @Indexed()
    private boolean active;
    @Indexed()
    private String seriesName;
    @Indexed()
    private String modelName;

    private float costMultiplier;

    private JSONObject options;
}
