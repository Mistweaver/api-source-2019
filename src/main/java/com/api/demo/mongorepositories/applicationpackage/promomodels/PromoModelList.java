package com.api.demo.mongorepositories.applicationpackage.promomodels;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection="promomodellist")
public class PromoModelList extends BasicEntity {
    @Indexed(unique = true)
    private String priceSheetId;
    private List<JSONObject> promoList;
    @Indexed
    private String listState;
}
