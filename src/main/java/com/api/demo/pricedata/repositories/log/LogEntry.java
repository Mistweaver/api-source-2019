package com.api.demo.pricedata.repositories.log;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "pricedatalog")
public class LogEntry extends BasicEntity {
    @Indexed
    private String priceDataId;
    private String date;
    private String entry;
    private String dataBeforeChange;
    private String dataAfterChange;

    public LogEntry() {
        this.priceDataId = "";
        this.date = "";
        this.entry = "";
        this.dataBeforeChange = "";
        this.dataAfterChange = "";
    }


}
